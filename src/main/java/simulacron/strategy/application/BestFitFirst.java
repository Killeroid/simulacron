package simulacron.strategy.application;

import simulacron.model.*;
import simulacron.strategy.AbstractStrategy;
import sim.field.network.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sim.util.Bag;



/**
 * Sort platforms by common services
 * Connect to platforms in that order until all services fulfilled
 * If not all services fulfilled, unlink app
 * 
 * @author Kwaku Yeboah-Antwi
 */


public class BestFitFirst extends AbstractStrategy<App> {

	public BestFitFirst() {
		super("BestFitFirst");
	}


	public void evolve(BipartiteGraph graph, App a) {
		ArrayList<Service> needLinks = new ArrayList<Service>(a.getServices());
		Bag platforms = new Bag(graph.platforms);
//		Bag platforms = new Bag();
//		for (Platform p : graph.platforms) {
//			if (a.countCommonServices(p, null) > 0 && p.getDegree() < graph.getPlatformMaxLoad()) {
//				platforms.add(p);
//				//System.out.println(p);
//			}
//		}
		
		TreeMap<Integer, Object[]> platformsSorted = sortConnectable(graph, a, needLinks, platforms);


		while((needLinks.size() > 0) && platformsSorted.size() > 0) {
			Map.Entry<Integer, Object[]> pltSet = platformsSorted.pollFirstEntry();
			Platform p = (Platform)(pltSet.getValue())[0];
			@SuppressWarnings("unchecked")
			List<Service> removable = (List<Service>)(pltSet.getValue())[1];
			if (removable.size() > 0) {
				needLinks.removeAll(removable);
				graph.addEdge(a, p, removable.size());
				//platforms.remove(p);
				platformsSorted = sortConnectable(graph, a, needLinks, platforms); 
			} else {
				break;
			}
			
		}

		a.dead = needLinks.size() != 0;
		//System.out.println(a + " dead:? " + a.dead);
		// If app does not have all service needs fulfilled, unlink it and make it dead
		if (!a.isAlive()) {
			// indirected network: no edgesOut
			for (Object edge : graph.bipartiteNetwork.getEdgesIn(a)) {
				graph.removeEdge(a, (Edge)edge);
			}
		}
		a.setRedundancy(graph);
	}
	
	private TreeMap<Integer, Object[]> sortConnectable(BipartiteGraph graph, App a, ArrayList<Service> needLinks, Bag platforms) {
		TreeMap<Integer, Object[]> platformsSorted = new TreeMap<Integer, Object[]>();
		if (needLinks.size() > 0 && platforms.size() > 0) {
			for (Object p : platforms) {
				List<Service> connectableServices = a.getCommonServices((Platform)p);
				connectableServices.retainAll(needLinks);
				if (connectableServices.size() > 0 && ((Platform)p).getDegree() < graph.getPlatformMaxLoad()) {
					Object[] result = {p, connectableServices};
					platformsSorted.put(-(connectableServices.size()), result);
				}
			}
			
		}
		return platformsSorted;
	}

	/*
	 * Sort a list of platforms by the number of available links it can provide out of the needed links
	 */
	private TreeMap<Integer, Object[]> sortByConnectable(BipartiteGraph graph, ArrayList<Service> needLinks, Bag platforms) {
		TreeMap<Integer, Object[]> platformsSorted = new TreeMap<Integer, Object[]>();
		if (needLinks.size() > 0 && platforms.size() > 0) {
			for (Object p : platforms) {
				ArrayList<Service> connectableServices = BipartiteGraph.removableServices(needLinks, ((Platform)p).getServices(), 
						(graph.getPlatformMaxLoad() - ((Platform)p).getDegree()));
				// Platforms with the highest number of connectable services first
				if (connectableServices.size() > 0) {
					Object[] result = {p, connectableServices};
					platformsSorted.put(-(connectableServices.size()), result);
				}
			}
		}
		return platformsSorted;
	}

	@Override
	public void init(String stratId) {
		this.name = "BestFitFirst";
	}

}