package simulacron.model;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import sim.field.network.Edge;
import sim.util.Bag;
import simulacron.model.App;
import simulacron.model.Simulator;
import simulacron.model.Platform;
import simulacron.model.Entity;
import simulacron.model.Service;
import simulacron.strategy.NullStrategy;
import simulacron.metrics.SpeciesAndPopulation;
import sim.engine.SimState;
import sim.engine.Steppable;

/*
 * Helper functions for evolving strategies
 * 
 */

public class helpers {

	/*
	 * Return the number of links that would exist between this platform
	 * and the other entities connected to it if its services were 
	 * updated to x services
	 */
	public static int countLinks(Platform platform, List<Service> servs, Simulator graph) {
		// the graph is undirected, thus EdgesIn = EdgesOut
		Platform p = new Platform(platform);
		p.setServices(servs);
		Bag edges = graph.bipartiteNetwork.getEdgesIn(p); // read-only!
		App app;
		Edge edge;
		int degree = 0;
		for (Object o : edges) {
			edge = (Edge)o;
			app = (App)edge.getOtherNode(p);
			int updatedegree = graph.calcRealWeight(app, p, null);
			degree += updatedegree;
			if (updatedegree > 0) {
				graph.bipartiteNetwork.addEdge(app, platform, updatedegree);
			}
		}
		return degree;
	}

	
	/*
	 * Splits the platform's services randomly, creates a new platform
	 * with those services and then adds that platform to the current population 
	 * and then returns the new platform. The services are ranked by most
	 * popular first before splitting
	 */



	public static Simulator splitPlatform(Simulator graph, Platform platform) {
		return splitPlatform(graph, platform, graph.random.nextInt(platform.getSize()));
	}

	/*
	 * Splits the platform's services according to the ratio, creates a new platform
	 * with those services and then adds that platform to the current population 
	 * and then returns the new platform
	 */
	public static Simulator splitPlatform(Simulator graph, Platform platform, int ratio) {
		if (graph.getNumPlatforms() <= graph.getMaxPlatforms()) {
			Bag edges = graph.bipartiteNetwork.getEdges(platform, null); // read-only!
			int splitIndex = 0;
			ArrayList<App> ents = new ArrayList<App>();
			for (Object o : edges) {
				ents.add((App)((Edge)o).getOtherNode(platform));
			}
			// get the services used by the apps, sorted from the most to the least common
			ArrayList<Service> sortedServices = platform.sortServices(edges);
			if (ratio > 0 && ratio != sortedServices.size()) { 
				splitIndex = sortedServices.size() - (int)Math.round(sortedServices.size() % ratio);
			} else {
				splitIndex = (int)Math.round(sortedServices.size() / 2) + 1;
			}

			if (splitIndex >= sortedServices.size() || sortedServices.size() == 1 || splitIndex < 1 ) {
				//clonePlatform(graph, platform);
				return graph;
			} else {

//				// split the platform and keep here only the most shared half of the services
//				String kind = platform.getKind();
//				Platform p = graph.createPlatform(kind);
//				p.setServices(sortedServices.subList(splitIndex, sortedServices.size()));
//				//graph.createLinks(p, ents);

				for (int i = splitIndex; i < sortedServices.size(); i++) {
					platform.getServices().remove(sortedServices.get(i));
				}
				graph.updateLinks(platform);

				//p.action = "Created from split";
				//p.setDegree(countLinks(platform, p.getServices(), graph));
				//graph.removeEntity(graph.platforms, platform);
				//platform.action = "split_part";
				//System.err.println(graph.getPrintoutHeader() + "Split : NEW " + p.toString());
			}
		}
		return graph;
	}

	/*
	 * Clones the platform, adds it to the population
	 * and then returns the updated graph
	 */
	public static Simulator clonePlatform(Simulator graph, Platform platform) {
		if (graph.getNumPlatforms() <= graph.getMaxPlatforms()) {
			String kind = platform.getKind();
			Platform newPlatform = graph.createPlatform(kind);
			newPlatform.setServices(platform.getServices());
			// graph.createLinks(platform, graph);
			platform.action = "cloned";
			newPlatform.action = "Created from cloning";
			//newPlatform.setDegree(countLinks(platform, newPlatform.getServices(), graph));
			//System.out.println(graph.getPrintoutHeader() + " Diversim Cloned: " + platform + "and created: " + newPlatform);
		}
		return graph;
	}

	/*
	 * Creates a new platform from the combination of best performing
	 * services in the two parents platforms.The combination is done with 
	 * two ratios randomly generated. Returns the new platform
	 */
	public static Simulator recombinePlatforms(Simulator graph, Platform platformA, Platform platformB) {
		return recombinePlatforms(graph, platformA, platformB, graph.random.nextInt(platformA.getSize()), 
				graph.random.nextInt(platformB.getSize()), "Created by: Random Recombination");
	}

	/*
	 * Creates a new platform from the combination of best performing
	 * services in the two parents platforms.The combination is done with 
	 * the two ratios Returns the new platform
	 */

	public static Simulator recombinePlatforms(Simulator graph, Platform platformA, Platform platformB, int ratioA, int ratioB) {
		return recombinePlatforms(graph, platformA, platformB, ratioA, ratioB, "Created by: Recombination");
	}

	public static Simulator recombinePlatforms(Simulator graph, Platform platformA, Platform platformB, 
			int ratioA, int ratioB, String action) {
		if (graph.getNumPlatforms() <= graph.getMaxPlatforms()) {
			//String kind = platformA.getKind();
			//Platform platform = graph.createPlatform(kind);

			Bag edgesA = graph.bipartiteNetwork.getEdges(platformA, null); // read-only!
			Bag edgesB = graph.bipartiteNetwork.getEdges(platformB, null); // read-only!
			//int splitIndexA = 0;
			//int splitIndexB = 0;

			//get the services used by the apps, sorted from the most to the least common
			ArrayList<Service> sortedServicesA = platformA.sortServices(edgesA);
			ArrayList<Service> sortedServicesB = platformB.sortServices(edgesB);
			//Get the split points for each list of services. if ratios are too big, just get half from each list
			int splitIndexA = ((ratioA >= 1 && ratioA < sortedServicesA.size()) ? ratioA : ((int)Math.round(sortedServicesA.size() / 2) + 1));
			int splitIndexB = ((ratioB >= 1 && 
					ratioB < (graph.getPlatformMaxLoad() - splitIndexA) && 
					ratioB < sortedServicesB.size()) ? ratioB : ((int)Math.round(sortedServicesB.size() / 2)  + 1));
			
			

			//Combine both lists of services
			List<Service> services = sortedServicesA.subList(0, splitIndexA);
			platformA.services.retainAll(services);
			//services.addAll(sortedServicesB.subList(0, splitIndexB));

			platformA.setServices(sortedServicesB.subList(0, splitIndexB));

			platformA.action = action;
			//platform.setDegree(countLinks(platformA, platform.getServices(), graph));
			//graph.removeEntity(graph.platforms, platformA);
			//platformB.action = "recombine";
		}
		return graph;
	}

	public static Simulator killPlatform(Simulator graph, Platform platform) {
		graph.removeEntity(graph.platforms, platform);
		return graph;
	}

	public static ArrayList<Platform> getPlatforms(Simulator graph) {
		return graph.platforms;
	}

	public static Simulator connectAllApps(Simulator graph) {
		graph.removeAllEdges(); //Start with a clean slate
		for (App a : graph.apps) {
			Bag needLinks = new Bag(a.getServices());
			Bag platforms = new Bag(graph.platforms);
			int counter = platforms.size();
			while((needLinks.size() > 0) && counter > 0) {
				counter--;
				Platform p = ((Platform)platforms.get(counter));
				if (p.getDegree() <= graph.getPlatformMaxLoad() && needLinks.removeAll(p.getServices())) {
					graph.addEdge(a, p, p.countCommonServices(a, null));
				}
			}
			a.dead = needLinks.size() != 0;
			if (!a.isAlive()) {
				// indirected network: no edgesOut
				for (Object edge : graph.bipartiteNetwork.getEdgesIn(a)) {
					graph.removeEdge(a, (Edge)edge);
				}
			}
		}
		return graph;
	}

	public static Boolean killGraph(Simulator graph) {
		Steppable kill = new Steppable() {
			public void step(SimState state) {
				state.kill();
				//System.out.println("Ending Simulation");
			}
		};
		return graph.schedule.scheduleOnce(graph.schedule.getTime(), kill);
	}

	public static Platform getPlatform(int ID, Simulator graph) {
		Platform p = new Platform(ID);
		//Platform p = ((Platform)new Entity(ID));
		return Simulator.getElement(graph.platforms, p);
	}

	public static Simulator mutate(Simulator graph, Platform platform, int ratio) {
		Bag edges = graph.bipartiteNetwork.getEdges(platform, null);
		ArrayList<Service> sortedServices = platform.sortServices(edges);
		int splitIndex;
		if (ratio > 0 && ratio != sortedServices.size()) { 
			splitIndex = sortedServices.size() - (int)Math.round(sortedServices.size() % ratio);
		} else {
			splitIndex = (int)Math.round(sortedServices.size() / 2) + 1;
		}
		
		

		if (splitIndex == sortedServices.size() || sortedServices.size() == 1 || splitIndex == 0 ) {
			platform.getServices().clear();
			platform.setServices(graph.selectServices(platform.getSize()));
		} else {
			platform.getServices().retainAll(sortedServices.subList(0, splitIndex));
			int servicesneeded = sortedServices.size() - splitIndex;
			platform.setServices(graph.selectServices(servicesneeded));
		}
		graph.updateLinks(platform);
		platform.action = "mutate";
		return graph;
	}
	

	public static Simulator recombineBest(Simulator graph, Platform platform) {
		Bag platforms = new Bag(graph.platforms);
		platforms.sort(new Comparator<Entity>() {

			@Override
			public int compare(Entity e, Entity e2) {
				return e.getDegree() - e2.getDegree();
			}
		});

		if (platforms.size() < 1) return graph;
		
		Platform B = ((Platform)platforms.get(0));
		if (platform == B) return graph;
		
		int splitIndexA = (int)Math.round(platform.getSize() / 2) + 1;
		int splitIndexB = (B.getSize() > 1 && (int)Math.round(B.getSize() / 2) < (graph.getPlatformMaxLoad() - splitIndexA)) 
				? (int)Math.round(B.getSize() / 2) : (((graph.getPlatformMaxLoad() - splitIndexA - 1) % B.getSize()) + 1);
		
		platform.services.retainAll(platform.services.subList(0, splitIndexA));
//		System.out.println("IndexA: " + splitIndexA + " IndexB: " + splitIndexB);
//		System.out.println("Platform B: " + B);
		if (B.getSize() > 0 && splitIndexB > 0) platform.setServices(B.services.subList(0, splitIndexB));
		platform.action = "recombineBest";
		
		return graph;
	}

	public static Simulator recombineWorst(Simulator graph, Platform platform) {
		Bag platforms = new Bag(graph.platforms);
		platforms.sort(new Comparator<Entity>() {

			@Override
			public int compare(Entity e, Entity e2) {
				return e2.getDegree() - e.getDegree();
			}
		});
		Platform B = ((Platform)platforms.get(0));
		if (platform == B) return graph;
		
		int splitIndexA = (int)Math.round(platform.getSize() / 2) + 1;
		int splitIndexB = (B.getSize() > 1 && (int)Math.round(B.getSize() / 2) < (graph.getPlatformMaxLoad() - splitIndexA)) 
				? (int)Math.round(B.getSize() / 2) : (((graph.getPlatformMaxLoad() - splitIndexA - 1) % B.getSize()) + 1);
		
		platform.services.retainAll(platform.services.subList(0, splitIndexA));
//		System.out.println("IndexA: " + splitIndexA + " IndexB: " + splitIndexB);
//		System.out.println("Platform B: " + B);
		if (B.getSize() > 0 && splitIndexB > 0) platform.setServices(B.services.subList(0, splitIndexB));
		platform.action = "recombineWorst";
		
		return graph;
	}

	public static Simulator recombineBestWorst(Simulator graph) {
		Bag platforms = new Bag(graph.platforms);
		platforms.sort(new Comparator<Entity>() {

			@Override
			public int compare(Entity e, Entity e2) {
				return e.getDegree() - e2.getDegree();
			}
		});
		
		if (platforms.size() < 2) return graph;

		Platform best = ((Platform)platforms.get(0));
		Platform worst = ((Platform)platforms.get(platforms.size() - 1));
		
		if (best == worst) return graph;
		
		int splitIndexA = (int)Math.round(best.getSize() / 2) + 1;
		int splitIndexB = (worst.getSize() > 1 && (int)Math.round(worst.getSize() / 2) < (graph.getPlatformMaxLoad() - splitIndexA)) 
				? (int)Math.round(worst.getSize() / 2) : (((graph.getPlatformMaxLoad() - splitIndexA) % worst.getSize()) + 1);
		
		Platform platform = graph.createPlatform(best.getKind());
//		System.out.println("IndexA: " + splitIndexA + " IndexB: " + splitIndexB);
//		System.out.println("Best: " + best + " Worst: " + worst);
		platform.setServices(best.services.subList(0, splitIndexA));
		if (worst.getSize() > 0 && splitIndexB > 0) platform.setServices(worst.services.subList(0, splitIndexB));
		platform.action = "createdfrom_Best_Worst";

		return graph;
	}

	public static Simulator replaceServices(Simulator graph, Platform platform, List<Service> services) {
		if (services.size() > 0) {
			platform.getServices().clear();
			for (Service s: services) {
				if (platform.getSize() < graph.getPlatformMaxLoad()) {
					Simulator.addUnique(platform.services, s);
				} else {
					break;
				}
			}
			platform.action = "replaced_services";
			graph.updateLinks(platform);
		}
		return graph;
	}
	
	public static Simulator addServices(Simulator graph, Platform platform, List<Service> services) {
		if (services.size() > 0) {
			
			for (Service s: services) {
				if (platform.getSize() < graph.getPlatformMaxLoad()) {
					Simulator.addUnique(platform.services, s);
				} else {
					break;
				}
			}
			platform.action = "add_services";
			graph.updateLinks(platform);
		}
		return graph;
	}
	
	public static Simulator removeServices(Simulator graph, Platform platform, List<Service> services) {
		if (services.size() > 0) {
			platform.services.removeAll(services);
			platform.action = "remove_services";
			graph.updateLinks(platform);
		}
		return graph;
	}

	public static Simulator inheritPlatform(Simulator graph, Platform platformA, Platform platformB) {
		List<Service> services = platformB.services.subList(0, platformB.services.size());
		return replaceServices(graph, platformA, services);
	}

	public static Simulator recombineBiggest(Simulator graph, Platform platform) {
		Bag platforms = new Bag(graph.platforms);
		platforms.sort(new Comparator<Entity>() {

			@Override
			public int compare(Entity e, Entity e2) {
				return e.getSize() - e2.getSize();
			}
		});
		
		if (platforms.size() < 1) return graph;

		Platform B = ((Platform)platforms.get(0));
		if (platform == B) return graph;
		
		int splitIndexA = (int)Math.round(platform.getSize() / 2) + 1;
		int splitIndexB = (B.getSize() > 1 && (int)Math.round(B.getSize() / 2) < (graph.getPlatformMaxLoad() - splitIndexA)) 
				? (int)Math.round(B.getSize() / 2) : (((graph.getPlatformMaxLoad() - splitIndexA - 1) % B.getSize()) + 1);
		
		platform.services.retainAll(platform.services.subList(0, splitIndexA));
//		System.out.println("IndexA: " + splitIndexA + " IndexB: " + splitIndexB);
//		System.out.println("Platform B: " + B);
		if (B.getSize() > 0 && splitIndexB > 0) platform.setServices(B.services.subList(0, splitIndexB));
		platform.action = "recombinationBiggest";
		
		return graph;
	}

	public static Simulator recombineSmallest(Simulator graph, Platform platform) {
		Bag platforms = new Bag(graph.platforms);
		platforms.sort(new Comparator<Entity>() {

			@Override
			public int compare(Entity e, Entity e2) {
				return e2.getSize() - e.getSize();
			}
		});
		
		if (platforms.size() < 1) return graph;
		
		Platform B = ((Platform)platforms.get(0));
		
		if (platform == B) return graph;
		
		int splitIndexA = (int)Math.round(platform.getSize() / 2) + 1;
		int splitIndexB = (B.getSize() > 1 && (int)Math.round(B.getSize() / 2) < (graph.getPlatformMaxLoad() - splitIndexA)) 
				? (int)Math.round(B.getSize() / 2) : (((graph.getPlatformMaxLoad() - splitIndexA - 1) % B.getSize()) + 1);
		
		platform.services.retainAll(platform.services.subList(0, splitIndexA));
//		System.out.println("IndexA: " + splitIndexA + " IndexB: " + splitIndexB);
//		System.out.println("Platform B: " + B);
		if (B.getSize() > 0 && splitIndexB > 0) platform.setServices(B.services.subList(0, splitIndexB));
		platform.action = "recombinationSmallest";
		
		return graph;
	}

	public static Simulator recombineBiggestSmallest(Simulator graph) {
		Bag platforms = new Bag(graph.platforms);
		platforms.sort(new Comparator<Entity>() {

			@Override
			public int compare(Entity e, Entity e2) {
				return e.getSize() - e2.getSize();
			}
		});
		
		if (platforms.size() < 2) return graph;

		Platform best = ((Platform)platforms.get(0));
		Platform worst = ((Platform)platforms.get(platforms.size() - 1));
		
		if (best == worst) return graph;
		
		int splitIndexA = (int)Math.round(best.getSize() / 2) + 1;
		int splitIndexB = (worst.getSize() > 1 && (int)Math.round(worst.getSize() / 2) < (graph.getPlatformMaxLoad() - splitIndexA)) 
				? (int)Math.round(worst.getSize() / 2) : (((graph.getPlatformMaxLoad() - splitIndexA) % worst.getSize()) + 1);
//		int splitIndexB = (worst.getSize() >= 1 && (int)Math.round(worst.getSize() / 2) < (graph.getPlatformMaxLoad() - splitIndexA)) 
//				? (int)Math.round(worst.getSize() / 2) : (worst.getSize() - ((graph.getPlatformMaxLoad() - splitIndexA - 1) % worst.getSize()));
		
		Platform platform = graph.createPlatform(best.getKind());
//		System.out.println("IndexA: " + splitIndexA + " IndexB: " + splitIndexB);
//		System.out.println("Biggest: " + best + " Smallest: " + worst);
		platform.setServices(best.services.subList(0, splitIndexA));
		if (worst.getSize() > 0 && splitIndexB > 0) platform.setServices(worst.services.subList(0, splitIndexB));
		platform.action = "createdfrom_Biggest_Smallest";

		return graph;
	}

	public static int connectedPlatforms(Simulator graph) {
		int connectedPlatforms = 0;
		for (Platform p: graph.platforms) {
			if (p.getDegree() > 0) connectedPlatforms++;
		}
		return connectedPlatforms;
	}

	//public static int getValue(Bag required, Platform p) {
	//	int score = 0;
	//	Iterator iter = required.iterator();
	//	for (int i = 0; i < required.size(); i++) {
	//		if (i > 0) return i;
	//	}
	//	return score;
	//}
	//
	//public static List<Platform> knapsack(List<Platform> platforms, int weight, List<Platform> success, List<Service> required) {
	//	if (weight == 0 || required.size() <= 0) {
	//		System.out.println("Success");
	//		return success;
	//	} else if (platforms.size() <= 0) {
	//		System.out.println("Failure");
	//	}
	//	
	//	if (platforms.size() > weight) {
	//		return knapsack(platforms.subList(0, platforms.size() - 1), weight, success, required);
	//		
	//	} else {
	//		success.add(platforms.get(platforms.size() - 1));
	//		return knapsack(platforms.subList(0, platforms.size() - 1), weight - 1, success, required);
	//	}
	//
	//}
	//
	//public static Map<Object, Map<Integer, List<Platform>>> initsacks(Simulator graph) {
	//	Map<Object, Map<Integer, List<Platform>>> links = new HashMap<Object, Map<Integer, List<Platform>>>();
	//	Map<App, BigInteger> apps = new SpeciesAndPopulation<App>(graph.apps).encodeEntityList(graph.apps);
	//	for (App a: apps.keySet()) {
	//		
	//		Map<Integer, List<Platform>> platforms = links.get(key)
	//	}
	//	
	//	return links;
	//}
	//
	//public static List<Platform> ksack(List<Platform> platforms, App app) {
	//	Map<Integer, List<Platform>> freqs = new HashMap<Integer, List<Platform>>();
	//	
	//	for (int x = 0; x < platforms.size(); x++) {
	//		int value = platforms.get(x).countCommonServices(app, null);
	//		List<Platform> p = freqs.get(value);
	//		if (p == null) {
	//			p = new ArrayList<Platform>();
	//			freqs.put(value, p);	
	//		}
	//		p.add(platforms.get(x));
	//
	//	}
	//  
	//	
	//	
	//	return platforms;
	//}
	//
	//public static Bag getBestPlatforms(Simulator graph, App e, Bag platforms, Bag success, int index, int depth, int alpha, int beta) {
	//	if (depth == 0 || index == platforms.size()) {
	//		return success;
	//	}
	//	
	//	
	//	//ArrayList<Platform> platforms = new ArrayList<Platform>();
	//	
	//	return platforms;
	//}
	//
	//public static ArrayList<Service> getbestServices(Simulator graph, App e) {
	//	final Entity app = ((Entity)e);
	//	ArrayList<Service> services = new ArrayList<Service>();
	//	
	//	
	////If there are platforms, try and link to them
	//	if (graph.getNumPlatforms() > 0) {
	//		Bag platforms = new Bag(graph.platforms);
	//		//Sort platform in descending order
	//		platforms.sort(new Comparator<Platform>() {
	//
	//			@Override
	//			public int compare(Platform p1, Platform p2) {
	//				return p2.countCommonServices(app, null) - p1.countCommonServices(app, null);
	//			}
	//		});
	//		
	//		Bag needLinks = new Bag(e.getServices());
	//		int counter = platforms.size();
	//		while((needLinks.size() > 0) && counter > 0) {
	//			counter--;
	//			Platform p = ((Platform)platforms.get(counter));
	//			if (p.getDegree() <= graph.getPlatformMaxLoad() && needLinks.removeAll(p.getServices())) {
	//				graph.addEdge(e, p, p.countCommonServices(e, null));
	//			}
	//		}
	//		
	//	}
	//	
	//	return services;
	//}

}