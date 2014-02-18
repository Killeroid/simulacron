package simulacron.strategy.environment;


import java.util.ArrayList;
import java.util.Collections;

import simulacron.model.App;
import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.strategy.AbstractStrategy;


/**
 * 
 */
public class appMerge extends AbstractStrategy<Fate> {

long counter;

private ArrayList<Integer> timing;

private String mergeType;


public appMerge(String type) {
	super("appMergeStrategy");

	timing = new ArrayList<Integer>(); // zipf-distributed sequence of timesteps.
	counter = 0; // how many times a new value has been added to timing
	if (type == "add" || type == "kill") {
		mergeType = type;
	} else {
		mergeType = "kill";
	}
}


@Override
public void evolve(Simulator graph, Fate agent) {
	if (counter < Math.min(graph.getMaxCycles(), Integer.MAX_VALUE) - 1 && timing.size() < 1000) {
		int n;
		do {
			n = graph.random.nextInt((int)(graph.getMaxCycles())) * graph.stepsPerCycle;
			// n = Distributions.nextZipfInt(1.1, graph.random);
		}
		while (n <= graph.schedule.getSteps());
		timing.add(n);
		Collections.sort(timing);
		counter++;
	}

	if (graph.schedule.getSteps() >= timing.get(0)) {
		timing.remove(0);
		merge(graph);
	}
	System.err.println(graph.getPrintoutHeader()
			+ "Fate : INFO : next new app merge will happen at cycle "
			+ (int)(timing.get(0) / graph.stepsPerCycle));
}

protected void merge(Simulator graph) {
	if (mergeType == "add" && graph.getNumApps() < graph.getMaxApps()) {
		App a = graph.apps.get(graph.random.nextInt(graph.getNumApps()));
		App b = graph.apps.get(graph.random.nextInt(graph.getNumApps()));
		App app = graph.createApp(a.getKind());
		app.addDependencies(b.getDependencies());
		System.out.println(graph.getPrintoutHeader() + "ENVIRONMENT : MERGE ADDED " + app.toString());
	} else if (mergeType == "kill" && graph.getNumApps() > 1) {
		App a = graph.apps.get(graph.random.nextInt(graph.getNumApps()));
		App b = graph.apps.get(graph.random.nextInt(graph.getNumApps()));
		a.addDependencies(b.getDependencies());
		graph.removeEntity(graph.apps, b);
	    System.out.println(graph.getPrintoutHeader() + "ENVIRONMENT : MERGE REMOVED " + b.toString());
	} else if (mergeType == "kill" && graph.getNumApps() > 0) {
		App a = graph.apps.get(graph.random.nextInt(graph.getNumApps()));
		graph.removeEntity(graph.apps, a);
	    System.out.println(graph.getPrintoutHeader() + "ENVIRONMENT : REMOVED " + a.toString());
	}
}
}
