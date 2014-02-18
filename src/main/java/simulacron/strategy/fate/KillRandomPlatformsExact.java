package simulacron.strategy.fate;

import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.strategy.AbstractStrategy;
import simulacron.util.Log;
import simulacron.util.config.Configuration;

/**
 * Kill randomly <code>amount</code> Platforms
 * 
 *
 * @param amount
 */

public class KillRandomPlatformsExact  extends AbstractStrategy<Fate> {
	protected int amount;


	protected KillRandomPlatformsExact(String n) {
		super(n);
	}

	public KillRandomPlatformsExact() {
		super("KillRandomPlatformsExact");
	}

	@Override
	public void evolve(Simulator graph, Fate agent) {
		for (int i = 0; i < amount; i++) {
			if (graph.getNumPlatforms() > 0) {
				Platform killed = graph.platforms.get(graph.random().nextInt(graph.getNumPlatforms()));
				graph.removeEntity(graph.platforms, killed);
				Log.debug("Platform <" + killed + "> has been killed by RandomExact");
			}
			else {
				System.out.println(graph.getPrintoutHeader() + "Fate : NO PLATFORMS REMAINING ");
				break;
			}
		}
	}

	@Override
	public void init(String stratId) {
		amount = (int) Configuration.getInt(stratId + ".amount");
	}
}