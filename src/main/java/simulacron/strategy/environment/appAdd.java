package simulacron.strategy.environment;


import java.util.ArrayList;
import java.util.Collections;

import simulacron.model.App;
import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.strategy.AbstractStrategy;
import simulacron.strategy.Strategy;


/**
 * 
 */
public class appAdd extends AbstractStrategy<Fate> {

long counter;

private ArrayList<Integer> timing;

private Strategy<App> newAppStrategy;


public appAdd() {
	super("appAddStrategy");

	timing = new ArrayList<Integer>(); // zipf-distributed sequence of timesteps.
	counter = 0; // how many times a new value has been added to timing
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
		if (graph.getNumApps() < graph.getMaxApps()) {
			String kind = graph.apps.get(graph.random.nextInt(graph.getNumApps())).getKind();
			App app = graph.createApp(kind);
			// app.step(graph); // this is mandatory if Fate kills apps with 0 degree at each cycle
			System.out.println(graph.getPrintoutHeader() + "Fate : ADDED " + app.toString());
		}
	}
	System.err.println(graph.getPrintoutHeader()
			+ "Fate : INFO : next new app will come in at cycle "
			+ (int)(timing.get(0) / graph.stepsPerCycle));
}
}
