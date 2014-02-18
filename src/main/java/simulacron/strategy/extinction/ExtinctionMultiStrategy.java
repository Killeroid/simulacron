package simulacron.strategy.extinction;


import java.util.List;

import simulacron.model.Simulator;
import simulacron.model.Entity;
import simulacron.model.StrategyFactory;


public class ExtinctionMultiStrategy extends ExtinctionStrategy<Entity> {


public List<ExtinctionStrategy<Entity>> killers;


protected ExtinctionMultiStrategy(String n) {
	super(n);
	// TODO Auto-generated constructor stub
}


public void initStrategies(Simulator graph) {
	this.killers = (List)StrategyFactory.fINSTANCE.createPlatformExtinctionStrategies();
}


@Override
public boolean die(Entity e, Simulator graph) {
	if (e.dead) return true;
	for (ExtinctionStrategy<Entity> killer : killers) {
		if (killer.die(e, graph)) {
			e.dead = true;
			return true;
		}
	}
	return false;
}


@Override
public void evolve(Simulator graph, Entity agent) {
	die(agent, graph);
	
}

}
