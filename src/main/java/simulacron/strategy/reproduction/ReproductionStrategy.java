package simulacron.strategy.reproduction;

import java.util.List;

import simulacron.model.BipartiteGraph;
import simulacron.model.Entity;

public interface ReproductionStrategy {
	public List<Entity> reproduce(Entity parent, BipartiteGraph state);
}
