package simulacron.strategy.extinction;

import simulacron.model.App;
import simulacron.model.BipartiteGraph;
import simulacron.model.Entity;

public interface AppExtinctionStrategy {
	 public boolean die(App app, BipartiteGraph graph);
}
