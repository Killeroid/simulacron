package simulacron.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import sim.engine.ParallelSequence;
import sim.engine.SimState;
import sim.engine.Steppable;
import simulacron.model.App;
import simulacron.strategy.Strategy;
import simulacron.model.BipartiteGraph;
import simulacron.model.Entity;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.util.config.Configuration;

public class Extinction11 implements Steppable{
	
	public ParallelSequence Extinctionthreads;
	public int cores;
	public ArrayList<Strategy<? extends Steppable>> killStrats = new ArrayList<Strategy<? extends Steppable>>();
	public ArrayList<Strategy<? extends Steppable>> linkStrats = new ArrayList<Strategy<? extends Steppable>>();
	public ArrayList<Strategy<? extends Steppable>> linkFateStrats = new ArrayList<Strategy<? extends Steppable>>();
	public Map<String, double[]> resultsMap = new HashMap<String, double[]>();


	public Extinction11(final int runs) {
		final int cores = (Runtime.getRuntime().availableProcessors() > 1) ? 2 : 1;
		if (Configuration.contains("extinction.kill")) {
			killStrats = BipartiteGraph.getExtinctionStrategies("kill");
		}

		if (Configuration.contains("extinction.link")) {
			linkStrats = BipartiteGraph.getExtinctionStrategies("link");
		}

		if (Configuration.contains("extinction.linkFate")) {
			linkFateStrats = BipartiteGraph.getExtinctionStrategies("linkFate");
		}
		Steppable[] threads = new Steppable[cores];
		
		
		
		
		for (int i = 0; i < cores; i++) {
			threads[i] = new Steppable() 
			{
				public void step(SimState state) {
					for (Strategy<? extends Steppable> kill : killStrats) {
						resultsMap.putAll(linking((BipartiteGraph)state, linkStrats.subList(0, (linkStrats.size()/cores)), kill, runs, false));
						resultsMap.putAll(linking((BipartiteGraph)state, linkFateStrats.subList(0, (linkFateStrats.size()/cores)), kill, runs, true));
					}
					
				}
			};
		}
		Extinctionthreads = new ParallelSequence(threads);

	}

	@Override
	public void step(SimState state) {
		// TODO Auto-generated method stub
		Extinctionthreads.step(state);

	}
	
	public void cleanup() {
		Extinctionthreads.cleanup();
	}
	
	Map<String, double[]> linking(BipartiteGraph graph, 
			List<Strategy<? extends Steppable>> linkStrats, Strategy<? extends Steppable> kill, int trials, boolean fate) {

		Map<String, double[]> resultsMap = new HashMap<String, double[]>();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		double[] statResults;

		for (Strategy<? extends Steppable> link : linkStrats) {
			statResults = new double[8];
			stats.clear();
			for (int count = 0; count < trials; count++) {
				BipartiteGraph clone = graph.extinctionClone();


				double robustness = 0;
				double maxRobustness = clone.getNumApps() * clone.getNumPlatforms();

				while (clone.getNumPlatforms() > 0) {
					clone.removeAllEdges();
					if (fate) {
						((Strategy<Fate>)link).evolve(clone,clone.fate);
					} else {
						for (App app : clone.apps) {
							app.setStrategy((Strategy<Entity>)link);
							app.step(clone);
							if (app.isAlive()) robustness += 1;
						}
					}
					((Strategy<Fate>)kill).evolve(clone,clone.fate);
				}
				stats.addValue(robustness/maxRobustness);
			}
			statResults[0] = stats.getMin();
			statResults[1] = stats.getPercentile(25);
			statResults[2] = stats.getPercentile(50);
			statResults[3] = stats.getPercentile(75);
			statResults[4] = stats.getMax();
			statResults[5] = stats.getMean();
			statResults[6] = stats.getSum() / stats.getN();
			statResults[7] = stats.getStandardDeviation();
			resultsMap.put(link.getClass().getSimpleName() + " - " + kill.getClass().getSimpleName(), statResults);
		}

		return resultsMap;
	}

}