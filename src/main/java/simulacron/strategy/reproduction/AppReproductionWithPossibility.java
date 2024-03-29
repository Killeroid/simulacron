package simulacron.strategy.reproduction;

import java.util.Collections;
import java.util.List;

import simulacron.model.App;
import simulacron.model.Simulator;


public class AppReproductionWithPossibility extends ReproStrategy<App> {

	private double possibility = 0;

private ReproStrategy<App> reproducer = null;


public AppReproductionWithPossibility(double possiblility, ReproStrategy<App> reproducer) {
	super(""); // TODO
		this.possibility = possiblility;
		this.reproducer = reproducer;
	}

	@Override
	public List<App> reproduce(App parent, Simulator state) {
		if(state.random.nextDouble() < possibility){
			return reproducer.reproduce(parent, state);
		}
		else
			return Collections.emptyList();
	}

    @Override
    public void evolve(Simulator graph, App agent) {
      reproduce(agent, graph);
    }


@Override
public void init(String stratId) {
	// TODO Auto-generated method stub

}
}
