package simulacron.metrics;




import simulacron.model.App;
import simulacron.strategy.Strategy;
import simulacron.model.Simulator;
import simulacron.model.Entity;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.util.config.Configuration;

public class CalcRobustness {

	/*
	 * A simplified extinction sequence method
	 * Returns a string representing the results of the extinction sequence
	 * 
	 * @param The Bipartite graph to run the extinction sequence on
	 * @param The number of times to run every combination of strategies
	 */

	Simulator state;
	Strategy<App> linkStrat;
	double[] results; 
	int runs;

	public void setRuns(int trials) {
		runs = trials;
	}

	public CalcRobustness(Simulator graph, int trials) {
		state = graph;
		linkStrat = (Strategy<App>)state.getStrategy("BestFitFirst");
		runs = trials;
		results = new double[runs];
	}

	/*
	 * Calculate robustness by removing one platform at a time
	 */
	protected void sequence() {
		System.out.println("Calculating robustness...");
		for (int count = 0; count < runs; count++) {
				Simulator clone = state.extinctionClone();


				double robustness = 0;
				double maxRobustness = clone.getNumApps() * clone.getNumPlatforms();

				while (clone.getNumPlatforms() > 0) {
					//System.out.println("Run " + count + ", Platforms remaining: " + clone.getNumPlatforms());
					clone.removeAllEdges();
					
						for (App app : clone.apps) {
							app.setStrategy(linkStrat);
							app.step(clone);
							if (app.isAlive()) robustness += 1;
						}
						Platform a = clone.platforms.get(clone.random().nextInt(clone.getNumPlatforms()));
						clone.removeEntity(clone.platforms, a);
				}
				results[count] = robustness/maxRobustness;
				try {
					clone.end();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
				}
			}
		System.out.println("Done calculating robustness...");
	}
	
	public double returnRobustness() {
		double sum = 0;
		sequence();
		
		for (double result: results) {
			sum += result;
		}
		return sum/runs;
		
	}
	
	public void finalize() throws Throwable {
		state = null;
		linkStrat = null;
		//runs = null;
		results = null;
		super.finalize();
	}

}