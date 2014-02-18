package simulacron.strategy.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sim.util.Bag;
import simulacron.model.App;
import simulacron.model.Simulator;
import simulacron.model.Platform;
import simulacron.model.Service;


public class Metrics {

public static double getPlatformEfficiency(Simulator graph, Platform platform) {
	return graph.bipartiteNetwork.getEdges(platform, null).size() / platform.getSize();
}


public static int getPlatformPopularity(Simulator graph, Platform platform) {
	Bag uniqueEdges = graph.bipartiteNetwork.getEdges(platform, null);
	for (Object edge : graph.bipartiteNetwork.getEdges(platform, null)) {
		uniqueEdges.removeMultiply(edge);
	}
	return uniqueEdges.size();
}


public static Platform getSmallestPlatform(Simulator graph) {
	Map<Integer, Platform> platformBySizeSorted = new TreeMap<Integer, Platform>();
	for (Platform p : graph.platforms) {
		platformBySizeSorted.put(p.getSize(), p);
	}
	return platformBySizeSorted.entrySet().iterator().next().getValue();
}


public static Platform getBiggestPlatform(Simulator graph) {
	Map<Integer, Platform> platformBySizeSorted = new TreeMap<Integer, Platform>();
	for (Platform p : graph.platforms) {
		// using negative size so the biggest platforms are first
		platformBySizeSorted.put(-p.getSize(), p);
	}
	return platformBySizeSorted.entrySet().iterator().next().getValue();
}


public static List<List<Service>> getSpecies(Simulator graph) {
	List<List<Service>> result = new ArrayList<List<Service>>();

	return result;
}
}
