package simulacron.strategy.extinction;

import simulacron.model.App;
import simulacron.model.Simulator;


public class AppOrphanExtinctionStrategy extends ExtinctionStrategy<App> {
	
	
public AppOrphanExtinctionStrategy(String n) {
	super(n);
	// TODO Auto-generated constructor stub
}


	@Override
	public boolean die(App app, Simulator graph) {
		if(app.services.size() == 0)
			return true;
	if (app.getDegree() == 0)
			return true;
		return false;
	}


@Override
public void evolve(Simulator graph, App agent) {
	agent.dead = die(agent, graph);

}

}
