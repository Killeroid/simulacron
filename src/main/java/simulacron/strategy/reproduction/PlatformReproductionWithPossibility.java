package simulacron.strategy.reproduction;

import java.util.Collections;
import java.util.List;

import simulacron.model.Simulator;
import simulacron.model.Platform;


public class PlatformReproductionWithPossibility extends ReproStrategy<Platform> {
	
	private double possibility = 0;

private ReproStrategy<Platform> reproducer = null;
	

public PlatformReproductionWithPossibility(double possiblility, ReproStrategy<Platform> reproducer) {
	super(""); // TODO
		this.possibility = possiblility;
		this.reproducer = reproducer;
	}

	@Override
	public List<Platform> reproduce(Platform parent, Simulator state) {
	if (state.random.nextDouble() < possibility) {
			 return reproducer.reproduce(parent, state);
		 }
		 else
			 return Collections.emptyList();
	}

    @Override
    public void evolve(Simulator graph, Platform agent) {
      reproduce(agent, graph);
    }


@Override
public void init(String stratId) {
	// TODO Auto-generated method stub

}
}
