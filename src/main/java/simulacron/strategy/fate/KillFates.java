package simulacron.strategy.fate;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import sim.util.Bag;
import simulacron.model.Simulator;
import simulacron.model.Platform;
import simulacron.model.Service;
import simulacron.strategy.util.Metrics;
import simulacron.util.Log;


public class KillFates {

public KillFates() {}


public static Map<String, Class[]> getKillingMethods() {
	Map<String, Class[]> results = new HashMap<String, Class[]>();
	Class[] args = new Class[2];
	args[0] = Simulator.class;
	args[1] = int.class;
	results.put("randomExact", args);
	args = new Class[2];
	args[0] = Simulator.class;
	args[1] = int.class;
	results.put("obsolescenceExact", args);
	args = new Class[2];
	args[0] = Simulator.class;
	args[1] = int.class;
	results.put("unattendedExact", args);
	return results;
}


public static void disconnected(Simulator graph) {
	Set<Platform> platformToKill = new HashSet<Platform>();
	for (Platform platform : graph.platforms) {
		if (platform.getDegree() == 0) {
			platformToKill.add(platform);
		}
	}
	for (Platform platform : platformToKill) {
		graph.removeEntity(graph.platforms, platform);
		Log.debug("Platform <" + platform + "> has been killed by disconnected");
	}
}


/**
 * Kill randomly <code>populationKillRatio</code>% of the Platforms
 * 
 * @param graph
 * @param populationKillRatio
 */
public static void random(Simulator graph, double populationKillRatio) {
	for (int i = 0; i < graph.getNumPlatforms() * populationKillRatio; i++) {
		Platform killed = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
		graph.removeEntity(graph.platforms, killed);
		Log.debug("Platform <" + killed + "> has been killed by Random");
	}
}


/**
 * Kill randomly <code>amount</code> Platforms
 * 
 * @param graph
 * @param amount
 */
public static void randomExact(Simulator graph, int amount) {
	for (int i = 0; i < amount; i++) {
		Platform killed = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
		graph.removeEntity(graph.platforms, killed);
		Log.debug("Platform <" + killed + "> has been killed by RandomExact");
	}
}


/**
 * Kill <code>populationKillRatio</code>% of the Platforms containing the Service
 * <code>backdoor</code>
 * 
 * @param graph
 * @param backdoor
 * @param populationKillRatio
 */
public static void backdoor(Simulator graph, Service backdoor, double populationKillRatio) {
	int counter = (int)(graph.getNumPlatforms() * populationKillRatio) + 1;
	for (int i = graph.getNumPlatforms() - 1; i >= 0; i--) {
		if (Collections.binarySearch(graph.platforms.get(i).getServices(), backdoor) >= 0) {
			Platform killed = graph.platforms.get(i);
			graph.removeEntity(graph.platforms, killed);
			Log.debug("Platform <" + killed + "> has been killed by Backdoor Service <"
			        + backdoor + ">");
			if (--counter <= 0) {
				break;
			}
		}
	}
}


/**
 * Kill <code>amount</code> Platforms containing the Service <code>backdoor</code>
 * 
 * @param graph
 * @param backdoor
 * @param amount
 */
public static void backdoor(Simulator graph, Service backdoor, int amount) {
	for (int i = graph.getNumPlatforms() - 1; i >= 0; i--) {
		if (Collections.binarySearch(graph.platforms.get(i).getServices(), backdoor) >= 0) {
			Platform killed = graph.platforms.get(i);
			graph.removeEntity(graph.platforms, killed);
			Log.debug("Platform <" + killed + "> has been killed by BackdoorExact Service <" + backdoor
			    + ">");
			if (--amount <= 0) {
				break;
			}
		}
	}
}


/**
 * Kill <code>populationKillRatio</code>% of the oldest Platforms
 * 
 * @param graph
 * @param populationKillRatio
 */

public static void obsolescence(Simulator graph, double populationKillRatio) {
	int counter = (int)(graph.getNumPlatforms() * populationKillRatio);
	Bag platforms = new Bag(graph.platforms);
	platforms.sort(new Comparator() {

		@Override
		public int compare(Object o1, Object o2) {
			return ((Platform)o1).getBirthCycle() - ((Platform)o2).getBirthCycle();
		}

	});
	for (int i = 0; i < counter; i++) {
		graph.removeEntity(graph.platforms, (Platform)platforms.get(i));
		Log.debug("Platform <" + (Platform)platforms.get(i) + "> has been killed by Obsolescence");
	}
}


/**
 * Kill <code>amount</code> of the oldest Platforms
 * 
 * @param graph
 * @param amount
 */
public static void obsolescenceExact(Simulator graph, int amount) {
	Bag platforms = new Bag(graph.platforms);
	platforms.sort(new Comparator() {

		@Override
		public int compare(Object o1, Object o2) {
			return ((Platform)o1).getBirthCycle() - ((Platform)o2).getBirthCycle();
		}

	});
	for (int i = 0; i < amount; i++) {
		graph.removeEntity(graph.platforms, (Platform)platforms.get(i));
		Log.debug("Platform <" + (Platform)platforms.get(i) + "> has been killed by ObsolescenceExact");
	}
}


/**
 * Kill <code>populationKillRatio</code>% of the Platforms that contain a specific random Service
 * 
 * @param graph
 * @param populationKillRatio
 */
public static void unattended(Simulator graph, double populationKillRatio) {
	Service backdoor = graph.services.get(graph.random().nextInt(graph.getNumServices()));
	int counter = (int)(graph.getNumPlatforms() * populationKillRatio);
	for (int i = graph.getNumPlatforms() - 1; i >= 0; i--) {
		int j = Collections.binarySearch(graph.platforms.get(i).getServices(), backdoor);
		if (j >= 0) {
			Platform killed = graph.platforms.get(i);
			graph.removeEntity(graph.platforms, killed);
			Log.debug("Platform <" + killed + "> has been killed by Unattended Service <"
			        + backdoor + ">");
			if (--counter <= 0) {
				break;
			}
		}
	}
}


/**
 * Kill <code>amount</code> Platforms that contain a specific random Service
 * 
 * @param graph
 * @param amount
 */
public static void unattendedExact(Simulator graph, int amount) {
	Service backdoor = graph.services.get(graph.random().nextInt(graph.getNumServices()));
	for (int i = graph.getNumPlatforms() - 1; i >= 0; i--) {
		int j = Collections.binarySearch(graph.platforms.get(i).getServices(), backdoor);
		if (j >= 0) {
			Platform killed = graph.platforms.get(i);
			graph.removeEntity(graph.platforms, killed);
			Log.debug("Platform <" + killed + "> has been killed by Unattended Service <" + backdoor
			    + ">");
			if (--amount <= 0) {
				break;
			}
		}
	}
}

public static ArrayList getContains(Simulator graph, Service srv) {
	double platformTotal = graph.platforms.size();
	ArrayList<Platform> contains = new ArrayList<Platform>();
	for (Platform platform : graph.platforms) {
		if (platform.getServices().contains(srv)) {
			contains.add(platform);
		}
	}
	return contains;
}

public static void killBySpecies(Simulator graph, int amount) {
	Service backdoor = graph.services.get(graph.random().nextInt(graph.getNumServices()));
	while (graph.getNumPlatforms() > 0) {
		ArrayList killplatforms = getContains(graph, backdoor);
		
		for(Object p: killplatforms) {

			graph.removeEntity(graph.platforms, (Platform)p);
			Log.debug("Platform <" + (Platform)p + "> has been killed by Unattended Service <" + backdoor
			    + ">");
			if (--amount <= 0) {
				break;
			}
		}
	}
}


/**
 * Kill a random platform and transfer its Services to another one
 * 
 * @param graph
 */
public static void concentrationRandom(Simulator graph) {
	if (graph.getNumPlatforms() == 0) {
		Log.debug("No more platforms");
		return;
	}
	Platform condemned = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
	graph.removeEntity(graph.platforms, condemned);
	Platform augmented = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
	for (Service s : condemned.getServices()) {
		Simulator.addUnique(augmented.getServices(), s);
	}
	Log.debug("Platform <" + condemned + "> has been killed by Concentration and its Services <"
	        + condemned.getServices() + "> added to Platform <" + augmented + ">");
}


public static void concentrationSmallInBig(Simulator graph) {
	Platform condemned = Metrics.getSmallestPlatform(graph);
	graph.removeEntity(graph.platforms, condemned);
	Platform augmented = Metrics.getBiggestPlatform(graph);
	for (Service s : condemned.getServices()) {
		Simulator.addUnique(augmented.getServices(), s);
	}
	Log.debug("Platform <" + condemned + "> has been killed by Concentration2 and its Services <"
	        + condemned.getServices() + "> added to Platform <" + augmented + ">");
}


public static void concentrationSmallInSmall(Simulator graph) {
	Platform condemned = Metrics.getSmallestPlatform(graph);
	graph.removeEntity(graph.platforms, condemned);
	Platform augmented = Metrics.getSmallestPlatform(graph);
	for (Service s : condemned.getServices()) {
		Simulator.addUnique(augmented.getServices(), s);
	}
	Log.debug("Platform <" + condemned + "> has been killed by Concentration3 and its Services <"
	        + condemned.getServices() + "> added to Platform <" + augmented + ">");
}


/**
 * <code>populationKillRatio</code>% of the Platforms containing more services than
 * <code>maxServices</code> are killed
 * 
 * @param graph
 * @param maxServices
 * @param populationKillRatio
 */
public static void gasFactory(Simulator graph, int maxServices, double populationKillRatio) {
	int counter = (int)(graph.getNumPlatforms() * populationKillRatio);
	for (int i = graph.getNumPlatforms() - 1; i >= 0; i--) {
		if (graph.platforms.get(i).getSize() >= maxServices) {
			Platform killed = graph.platforms.get(i);
			graph.removeEntity(graph.platforms, killed);
			Log.debug("Platform <" + killed + "> has been killed by GasFactory");
			if (--counter <= 0) {
				break;
			}
		}
	}
}


/**
 * <code>amount</code> Platforms containing more services than <code>maxServices</code> are killed
 * 
 * @param graph
 * @param maxServices
 * @param amount
 */
public static void gasFactoryExact(Simulator graph, int maxServices, int amount) {
	for (int i = graph.getNumPlatforms() - 1; i >= 0; i--) {
		if (graph.platforms.get(i).getSize() >= maxServices) {
			Platform killed = graph.platforms.get(i);
			graph.removeEntity(graph.platforms, killed);
			Log.debug("Platform <" + killed + "> has been killed by GasFactory");
			if (--amount <= 0) {
				break;
			}
		}
	}
}


/**
 * Kills all Platforms with no connection (dead)
 * 
 * @param graph
 */
public static void serveOrDie(Simulator graph) {
	Bag platforms = new Bag(graph.platforms);
	for (Object platform : platforms) {
		if (!((Platform)platform).isAlive()) {
			graph.removeEntity(graph.platforms, (Platform)platform);
			Log.debug("Platform <" + platform + "> has been killed by ServeOrDie");
		}
	}
}
}
