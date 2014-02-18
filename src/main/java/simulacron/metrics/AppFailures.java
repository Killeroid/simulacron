package simulacron.metrics;


import simulacron.model.Simulator;


public class AppFailures {

Simulator graph;

int totalAliveApps = 0;


public AppFailures(Simulator graph) {
	this.graph = graph;
}


public double calculateAliveAppsAverage() {
	totalAliveApps += graph.getAliveAppsNumber();
	return totalAliveApps / graph.getCurCycle();
}

}
