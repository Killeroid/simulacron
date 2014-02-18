package simulacron.strategy.reproduction;


import java.util.ArrayList;
import java.util.List;

import simulacron.model.Simulator;
import simulacron.model.Platform;
import simulacron.model.StrategyFactory;
import simulacron.strategy.AbstractStrategy;


public class PlatformReproMultiStrategy extends AbstractStrategy<Platform> {

List<ReproStrategy<Platform>> reproducers;

protected PlatformReproMultiStrategy(String n) {
	super(n);
	// TODO Auto-generated constructor stub
}


public void initStrategies(Simulator graph) {
	this.reproducers = (List)StrategyFactory.fINSTANCE.createPlatformReproductionStrategy();
}


public List<Platform> reproduce(Platform p, Simulator state) {
	List<Platform> result = new ArrayList<Platform>();
	for (ReproStrategy<Platform> reproducer : reproducers) {
		result.addAll(reproducer.reproduce(p, state));
	}
	return result;
}


@Override
public void evolve(Simulator graph, Platform agent) {
	reproduce(agent, graph);
}

}
