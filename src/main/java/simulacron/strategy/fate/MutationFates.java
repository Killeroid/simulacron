package simulacron.strategy.fate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sim.util.Bag;
import simulacron.model.Simulator;
import simulacron.model.Platform;
import simulacron.model.Service;
import simulacron.util.Log;


public class MutationFates {

public MutationFates() {}


public static void bugCorrected(Simulator graph) {
	Service obsolete = graph.services.get(graph.random().nextInt(graph.getNumServices()));
	Service replacement = graph.services.get(graph.random().nextInt(graph.getNumServices()));
	for (Platform platform : graph.platforms) {
		int i = Collections.binarySearch(platform.getServices(), obsolete);
		if (i >= 0) {
			platform.getServices().remove(i);
			Simulator.addUnique(platform.getServices(), replacement);
			Log.debug("Platform <" + platform + "> has mutated: Service <" + obsolete
			    + "> replaced with Service <" + replacement + "> by BugCorrected");
		}
	}
}


public static void upgrade(Simulator graph, int upgradedServicesNumber) {
	Platform upgradedPlatform = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
	List<Service> removedServices = new ArrayList<Service>();
	for (int i = 0; i < Math.min(upgradedServicesNumber, upgradedPlatform.getSize()); i++) {
		removedServices.add(upgradedPlatform.getServices().get(
		    graph.random().nextInt(graph.getNumServices())));
	}
	List<Service> upgradedServices = new ArrayList<Service>();
	for (int i = 0; i < Math.min(upgradedServicesNumber, graph.getNumServices()); i++) {
		upgradedServices.add(graph.services.get(graph.random().nextInt(graph.getNumServices())));
	}
	upgradedPlatform.getServices().removeAll(removedServices);
	// upgradedPlatform.getServices().addAll(upgradedServices);
	for (Service service : upgradedServices) {
		Simulator.addUnique(upgradedPlatform.getServices(), service);
	}
	Log.debug("Platform <" + upgradedPlatform + "> has upgraded Services <" + removedServices
	    + "> into <"
	        + upgradedServices + "> by Upgrade");
}


public static void random(Simulator graph, double populationSize, double mutationSize) {
	int counterPopulation = Math.max((int)(graph.getNumPlatforms() * populationSize), 1);
	Bag platforms = new Bag(graph.platforms);
	platforms.shuffle(graph.random());
	for (Object platform : platforms) {
		int counterMutation = Math.max((int)(((Platform)platform).getSize() * mutationSize),
		    1);
		int removedServices = counterMutation;
		Bag services = new Bag(((Platform)platform).getServices());
		services.shuffle(graph.random());
		for (Object service : services) {
			int i = Collections.binarySearch(((Platform)platform).getServices(), (Service)service);
			if (i >= 0) {
				((Platform)platform).getServices().remove(i);
				int initialServiceSize = ((Platform)platform).getSize();
				while (initialServiceSize == ((Platform)platform).getSize()) {
					Service replacement = graph.services.get(graph.random().nextInt(graph.getNumServices()));
					Simulator.addUnique(((Platform)platform).getServices(), replacement);
				}
				counterMutation--;
			}
			if (counterMutation <= 0) {
				break;
			}
		}
		Log.debug("Platform <" + platform + "> has mutated " + removedServices + " Services by Random");
		if (--counterPopulation <= 0) {
			break;
		}
	}

}
}
