package simulacron.strategy.reproduction;

import java.util.ArrayList;
import java.util.List;

import simulacron.model.App;
import simulacron.model.Simulator;


/**
 * Randomly choose one of the registered speciation strategies
 * to change its dna
 *
 * @author hui song
 *
 */

public class AppSpeciationReproductionByDNA extends ReproStrategy<App> {
	
	DNASpeciation speciator = null;
	
	public AppSpeciationReproductionByDNA(DNASpeciation speciator){
	super(""); // TODO
		this.speciator = speciator;
	}


	public List<App> reproduce(App parent, Simulator state) {
	App child = state.createApp(""); // TODO
	child.setServices(this.speciator.speciate(parent.getDependencies(), state.services));
	child.setStrategy(parent.getStrategy());
		ArrayList<App> children = new ArrayList<App>();
		children.add(child);
		return children;
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
