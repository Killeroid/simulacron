package simulacron.strategy.reproduction;

import java.util.List;

import simulacron.model.BipartiteGraph;
import simulacron.model.Entity;
import simulacron.strategy.AbstractStrategy;


public abstract class ReproStrategy<T extends Entity> extends AbstractStrategy<T> {

protected ReproStrategy(String n) {
	super(n);
	// TODO Auto-generated constructor stub
}


abstract public List<T> reproduce(T parent, BipartiteGraph state);


@Override
abstract public void evolve(BipartiteGraph state, T parent);

}
