package simulacron.strategy.fate;

import java.util.Comparator;

import sim.util.Bag;
import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.strategy.AbstractStrategy;
import simulacron.util.Log;
import simulacron.util.config.Configuration;

/**
 * Kill <code>amount</code> of the oldest Platforms
 * 
 *
 * @param amount
 */

public class PlatformObsolescenceExact  extends AbstractStrategy<Fate> {
	protected int amount;


	protected PlatformObsolescenceExact(String n) {
		super(n);
	}

	public PlatformObsolescenceExact() {
		super("PlatformObsolescenceExact");
	}

	@Override
	public void evolve(Simulator graph, Fate agent) {
		Bag platforms = new Bag(graph.platforms);
		platforms.sort(new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				return ((Platform)o1).getBirthCycle() - ((Platform)o2).getBirthCycle();
			}

		});
		for (int i = 0; i < amount; i++) {
			if (graph.getNumPlatforms() > 0) {
				graph.removeEntity(graph.platforms, (Platform)platforms.get(i));
				Log.debug("Platform <" + (Platform)platforms.get(i) + "> has been killed by ObsolescenceExact");
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