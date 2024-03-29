package simulacron.strategy.fate;

import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.strategy.AbstractStrategy;

/**
 * User: Simon
 * Date: 7/8/13
 * Time: 2:33 PM
 */
public class KillPlatform  extends AbstractStrategy<Fate> {
protected KillPlatform(String n) {
	super(n);
}

public KillPlatform() {
	super("KillStrategy");
}

@Override
public void evolve(Simulator graph, Fate agent) {
	if (graph.getNumPlatforms() > 0) {
		Platform a = graph.platforms.get(graph.random.nextInt(graph.getNumPlatforms()));
		graph.removeEntity(graph.platforms, a);

		//System.out.println(graph.getPrintoutHeader() + "Fate : REMOVED " + a);
	}
	else {
		//System.out.println(graph.getPrintoutHeader() + "Fate : NO PLATFORMS REMAINING ");
		//graph.schedule.clear();
	}
}

@Override
public void init(String stratId) {}
}