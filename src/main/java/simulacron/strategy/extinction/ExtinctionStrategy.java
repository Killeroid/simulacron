package simulacron.strategy.extinction;


import simulacron.model.Simulator;
import simulacron.model.Entity;
import simulacron.strategy.AbstractStrategy;


public abstract class ExtinctionStrategy<T extends Entity> extends AbstractStrategy<T> {

protected ExtinctionStrategy(String n) {
	super(n);
	// TODO Auto-generated constructor stub
}


@Override
abstract public void evolve(Simulator graph, T agent);


abstract public boolean die(T entity, Simulator graph);

}
