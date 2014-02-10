package simulacron.metrics;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import sim.engine.Steppable;
import simulacron.model.App;
import simulacron.strategy.Strategy;
import simulacron.model.BipartiteGraph;
import simulacron.model.Entity;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.util.config.Configuration;

public class Extinction {

	/*
	 * A simplified extinction sequence method
	 * Returns a string representing the results of the extinction sequence
	 * 
	 * @param The Bipartite graph to run the extinction sequence on
	 * @param The number of times to run every combination of strategies
	 */
	public static Map<String, double[]> runExtinctionSequence(BipartiteGraph graph) {
		return runExtinctionSequence(graph, Configuration.getInt("extinction.runs", 10));
	}


	public static Map<String, double[]> runExtinctionSequence(BipartiteGraph graph, int trials) {
		//System.out.println("Do we ever get here0.2");
		ArrayList<Strategy<? extends Steppable>> killStrats = null;
		ArrayList<Strategy<? extends Steppable>> linkStrats = null;
		ArrayList<Strategy<? extends Steppable>> linkFateStrats = null;

		if (Configuration.contains("extinction.kill")) {
			killStrats = BipartiteGraph.getExtinctionStrategies("kill");
		}

		if (Configuration.contains("extinction.link")) {
			linkStrats = BipartiteGraph.getExtinctionStrategies("link");
		}

		if (Configuration.contains("extinction.linkFate")) {
			linkFateStrats = BipartiteGraph.getExtinctionStrategies("linkFate");
		}

		if (killStrats == null || (linkStrats == null && linkFateStrats == null)) {
			Logger.getLogger(Robustness.class.getName()).log(Level.SEVERE,
					"[ERROR] Extinction Sequence: Could not run extinction sequence");
			return null;
		}


		Map<String, double[]> resultsMap = new HashMap<String, double[]>();

		for (Strategy<? extends Steppable> kill : killStrats) {
			if (linkStrats != null)
				resultsMap.putAll(linking(graph, linkStrats, kill, trials, false));
			if (linkFateStrats != null)
				resultsMap.putAll(linking(graph, linkFateStrats, kill, trials, true));

		}

		String result = System.getProperty("line.separator");
		result += "  Linking - Killing:\t [Min, Q1, Q2, Q3, Max, Mean, Avg, Std Dev]" + System.getProperty("line.separator");
		result += "  -------------------------------------------------------------------" + System.getProperty("line.separator");
		for (Map.Entry<String, double[]> pair: resultsMap.entrySet()) {
			result += "  " + pair.getKey() + ":\t " + Arrays.toString(pair.getValue())
					+ System.getProperty("line.separator");
		}
		return resultsMap;
	}
	
	public static Map<String, Double> returnRobustnessAvg(BipartiteGraph graph, int trials) {
		Map<String, double[]> resultsMap = runExtinctionSequence(graph, trials);
		Map<String, Double> results = new HashMap<String, Double>();
		for (Map.Entry<String, double[]> pair: resultsMap.entrySet()) {
			results.put(pair.getKey(), pair.getValue()[6]);
		}
		return results;
		
	}
	
	public static String ExtinctionReport(BipartiteGraph graph, int trials){
		Map<String, double[]> resultsMap = runExtinctionSequence(graph, trials);
		String result = System.getProperty("line.separator");
		result += "  Linking - Killing:\t [Min, Q1, Q2, Q3, Max, Mean, Avg, Std Dev]" + System.getProperty("line.separator");
		result += "  -------------------------------------------------------------------" + System.getProperty("line.separator");
		for (Map.Entry<String, double[]> pair: resultsMap.entrySet()) {
			result += "  " + pair.getKey() + ":\t " + Arrays.toString(pair.getValue())
					+ System.getProperty("line.separator");
		}
		return result;
	}

	/*
	 * Run non fate linking strategies
	 */
	@SuppressWarnings("unchecked")
	static Map<String, double[]> linking(BipartiteGraph graph, 
			ArrayList<Strategy<? extends Steppable>> linkStrats, Strategy<? extends Steppable> kill, int trials, boolean fate) {

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
					//System.out.println("Extinction, platforms remaining: " + clone.getNumPlatforms());
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