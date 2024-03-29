package simulacron.strategy.reproduction;

import java.util.ArrayList;
import java.util.List;

import simulacron.model.Simulator;
import simulacron.model.Platform;


/**
 * This class implements the ReproductionStrategy with clonal reproduction
 * i.e., the Platform child that is created is exactly the same as the parent Platform. Also, this strategy results in only one child.
 * @author Vivek Nallur
 */
public class PlatformClonalReproduction extends ReproStrategy<Platform> {

protected PlatformClonalReproduction(String n) {
	super(n);
	// TODO Auto-generated constructor stub
}


public List<Platform> reproduce(Platform parent, Simulator state) {
	Platform child = state.createPlatform(""); // TODO
	child.setServices(parent.getServices());
	child.setStrategy(parent.getStrategy());
	child.setDegree(parent.getDegree());
			ArrayList<Platform> children = new ArrayList<Platform>();
			children.add(child);
			return children;
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
