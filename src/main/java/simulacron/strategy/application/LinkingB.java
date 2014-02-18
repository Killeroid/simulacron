package simulacron.strategy.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import simulacron.model.*;
import simulacron.strategy.AbstractStrategy;
import sim.field.network.Edge;
import sim.util.Bag;


/**
 * Sort platforms in ascending order. 
 * Scan the list of services 
 * Add edge every time you find a platform 
 * that can support that service
 * 
 * 
 * @author Kwaku Yeboah-Antwi
 */
public class LinkingB extends AbstractStrategy<App> {

	public LinkingB() {
		super("LinkingB");
	}


	public void evolve(Simulator graph, App e) {
		//Remove all links to app
		Bag edges = graph.bipartiteNetwork.getEdgesIn(e);
		for (Object edge : edges) {
			graph.removeEdge(e, (Edge)edge);
		}

		//If there are platforms, try and link to them
		if (graph.getNumPlatforms() > 0) {
			Bag platforms = new Bag(graph.platforms);
			//Sort platform in ascending order
			platforms.sort(new Comparator<Entity>() {

				@Override
				public int compare(Entity e, Entity e2) {
					return e.getDegree() - e2.getDegree();
				}
			});

			Bag needLinks = new Bag(e.getServices());
			@SuppressWarnings("rawtypes")
			Iterator iterator = platforms.iterator();
			while((needLinks.size() > 0) && iterator.hasNext()) {
				Platform p = (Platform)iterator.next();
				ArrayList<Service> removable = Simulator.removableServices(needLinks, p.getServices(), 
						(graph.getPlatformMaxLoad() - p.getDegree()));
				if (removable.size() > 0) {
					needLinks.removeAll(removable);
					graph.addEdge(e, p, removable.size());
				}
			}
			e.dead = needLinks.size() != 0;
			// If app does not have all service needs fulfilled, unlink it and make it dead
			if (!e.isAlive()) {
				// indirected network: no edgesOut
				for (Object edge : graph.bipartiteNetwork.getEdgesIn(e)) {
					graph.removeEdge(e, (Edge)edge);
				}
			}
			e.setRedundancy(graph);

		}

	}


	@Override
	public void init(String stratId) {
		this.name = "LinkingB";
	}

}