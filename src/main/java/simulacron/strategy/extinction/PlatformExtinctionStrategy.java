package simulacron.strategy.extinction;

import simulacron.model.App;
import simulacron.model.Simulator;
import simulacron.model.Entity;
import simulacron.model.Platform;

public interface PlatformExtinctionStrategy {
	 public boolean die(Platform platform, Simulator graph);
}
