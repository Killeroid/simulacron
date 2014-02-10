package simulacron.strategy.application;

import simulacron.model.*;
import simulacron.strategy.AbstractStrategy;
import sim.field.network.Edge;
import sim.util.Bag;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Randomly shuffle all platforms. 
 * Scan the list of services 
 * Add edge every time you find a platform 
 * with that can support that service
 * 
 * 
 * @author Kwaku Yeboah-Antwi
 */
public class LinkingA extends AbstractStrategy<App> {

	public LinkingA() {
		super("LinkingA");
	}


	public void evolve(BipartiteGraph graph, App app) {
		//Remove all links to app
		Bag edges = graph.bipartiteNetwork.getEdgesIn(app);
		for (Object edge : edges) {
			graph.removeEdge(app, (Edge)edge);
		}

		//If there are platforms, try and link to them
		if (graph.getNumPlatforms() > 0) {
			Bag platforms = new Bag(graph.platforms);
			platforms.shuffle(graph.random);
			Bag needLinks = new Bag(app.getServices());
			@SuppressWarnings("rawtypes")
			Iterator iterator = platforms.iterator();
			while((needLinks.size() > 0) && iterator.hasNext()) {
				Platform p = (Platform)iterator.next();
				ArrayList<Service> removable = BipartiteGraph.removableServices(needLinks, p.getServices(), 
						(graph.getPlatformMaxLoad() - p.getDegree()));
				if (p.getDegree() < graph.getPlatformMaxLoad() && removable.size() > 0) {
					needLinks.removeAll(removable);
					graph.addEdge(app, p, removable.size());
				}
			}
			app.dead = needLinks.size() != 0;
			// If app does not have all service needs fulfilled, unlink it and make it dead
			if (!app.isAlive()) {
				// indirected network: no edgesOut
				for (Object edge : graph.bipartiteNetwork.getEdgesIn(app)) {
					graph.removeEdge(app, (Edge)edge);
				}
			}
			app.setRedundancy(graph);

		}
		

	}


	@Override
	public void init(String stratId) {
		this.name = "LinkingA";
	}

}