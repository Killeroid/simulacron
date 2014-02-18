package simulacron.strategy.reproduction;

import java.util.List;

import simulacron.model.Simulator;
import simulacron.model.Entity;

public interface ReproductionStrategy {
	public List<Entity> reproduce(Entity parent, Simulator state);
}
