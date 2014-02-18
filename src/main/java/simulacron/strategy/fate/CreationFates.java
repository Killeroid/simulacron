package simulacron.strategy.fate;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sim.util.Bag;
import simulacron.model.App;
import simulacron.model.Simulator;
import simulacron.model.Platform;
import simulacron.model.Service;
import simulacron.util.Log;


public class CreationFates {

public CreationFates() {}


public static void cloningMutateRandom(Simulator graph, double populationCreationRatio,
    double servicesSizeVariation) {
	if (graph.getNumPlatforms() * (1 + populationCreationRatio) <= graph.getMaxPlatforms()) {
		for (int i = 0; i < graph.getNumPlatforms() * populationCreationRatio; i++) {
			Platform source = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
			Platform clone = graph.createPlatform(source.getKind());
			clone.getServices().clear();
			int cloneSize = Math.min(
			    graph.random().nextInt((int)(source.getSize() * servicesSizeVariation)),
			    graph.getNumServices());
			List<Service> cloneServices = new ArrayList<Service>();
			for (int j = 0; j < cloneSize; j++) {
				cloneServices.add(graph.services.get(graph.random().nextInt(graph.getNumServices())));
			}
			for (Service s : cloneServices) {
				Simulator.addUnique(clone.getServices(), s);
			}
			clone.setStrategy(source.getStrategy());
			Log.debug("Platform <" + source + "> has been cloned");
		}
	} else {
		Log.debug("CloningMutate: max number of Platforms reached");
	}
}


public static void split(Simulator graph, double servicesTransmitted, double populationCreationRatio) {
	for (int i = 0; i < graph.getNumPlatforms() * populationCreationRatio; i++) {
		if (graph.getNumPlatforms() < graph.getMaxPlatforms()) {
			Platform source = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
			Platform clone1 = graph.createPlatform(source.getKind());
			clone1.getServices().clear();
			Platform clone2 = graph.createPlatform(source.getKind());
			clone2.getServices().clear();
			Bag sourceServices = new Bag(source.getServices());
			sourceServices.shuffle(graph.random());
			for (int j = 0; j < sourceServices.size() * servicesTransmitted; j++) {
				Simulator.addUnique(clone1.getServices(), (Service)sourceServices.get(j));
			}
			sourceServices.shuffle(graph.random());
			for (int j = 0; j < sourceServices.size() * servicesTransmitted; j++) {
				Simulator.addUnique(clone2.getServices(), (Service)sourceServices.get(j));
			}
			clone1.setStrategy(source.getStrategy());
			clone2.setStrategy(source.getStrategy());
			graph.removeEntity(graph.platforms, source);
			Log.debug("Platform <" + source + "> has been split into platforms <" + clone1 + "> & <"
			    + clone2 + ">");
		} else {
			Log.debug("Split: max number of Platforms reached");
		}
	}
}


public static void splitExact(Simulator graph, double servicesTransmitted, int amount) {
	if (graph.getNumPlatforms() < graph.getMaxPlatforms()) {
		for (int i = 0; i < amount; i++) {
			Platform source = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
			Platform clone1 = graph.createPlatform(source.getKind());
			clone1.getServices().clear();
			Platform clone2 = graph.createPlatform(source.getKind());
			clone2.getServices().clear();
			Bag sourceServices = new Bag(source.getServices());
			sourceServices.shuffle(graph.random());
			for (int j = 0; j < sourceServices.size() * servicesTransmitted; j++) {
				Simulator.addUnique(clone1.getServices(), (Service)sourceServices.get(j));
			}
			sourceServices.shuffle(graph.random());
			for (int j = 0; j < sourceServices.size() * servicesTransmitted; j++) {
				Simulator.addUnique(clone2.getServices(), (Service)sourceServices.get(j));
			}
			clone2.setStrategy(source.getStrategy());
			graph.removeEntity(graph.platforms, source);
			Log.debug("Platform <" + source + "> has been split into platforms <" + clone1 + "> & <"
			    + clone2 + ">");
		}
	} else {
		Log.debug("SplitExact: max number of Platforms reached");
	}
}
}
