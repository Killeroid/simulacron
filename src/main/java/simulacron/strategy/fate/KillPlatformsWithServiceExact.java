package simulacron.strategy.fate;

import java.util.Collections;
import java.util.Comparator;

import sim.util.Bag;
import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.model.Service;
import simulacron.strategy.AbstractStrategy;
import simulacron.util.Log;
import simulacron.util.config.Configuration;

/**
 * Kill <code>amount</code> Platforms that contain a specific random Service
 * 
 *
 * @param amount
 */

public class KillPlatformsWithServiceExact  extends AbstractStrategy<Fate> {
	protected int amount;


	protected KillPlatformsWithServiceExact(String n) {
		super(n);
	}

	public KillPlatformsWithServiceExact() {
		super("KillPlatformsWithServiceExact");
	}

	@Override
	public void evolve(Simulator graph, Fate agent) {
		Service backdoor = graph.services.get(graph.random().nextInt(graph.getNumServices()));
		for (int i = graph.getNumPlatforms() - 1; i >= 0; i--) {
			int j = Collections.binarySearch(graph.platforms.get(i).getServices(), backdoor);
			if (j >= 0) {
				Platform killed = graph.platforms.get(i);
				graph.removeEntity(graph.platforms, killed);
				Log.debug("Platform <" + killed + "> has been killed by Unattended Service <" + backdoor
				    + ">");
				if (--amount <= 0) {
					break;
				}
			}
		}
	}

	@Override
	public void init(String stratId) {
		amount = (int) Configuration.getInt(stratId + ".amount");
	}
}