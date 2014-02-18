package simulacron.strategy.extinction;

import simulacron.model.Simulator;
import simulacron.model.Entity;


public class AgingExtinctionStrategy extends ExtinctionStrategy<Entity> {
	
private int expectedAge;

	
public AgingExtinctionStrategy(int expectedAge) {
	super(""); // TODO
	this.expectedAge = expectedAge;

	}


public boolean die(Entity e, Simulator graph) {
	long steps = graph.getCurCycle();
	if (steps - e.getBirthCycle() >= expectedAge) {
			return true;
		}
		else
			return false;
		
	}


@Override
public void evolve(Simulator graph, Entity agent) {
	agent.dead = die(agent, graph);

}

}
