package simulacron.strategy.reproduction;

import java.util.List;

import simulacron.model.Simulator;
import simulacron.model.Platform;
import ec.util.MersenneTwisterFast;

/**
 * This interface must be implemented by all reproduction strategies of Platform
 * @author Vivek Nallur
 */
public interface PlatformReproductionStrategy{
        public List<Platform> reproduce(Platform parent, Simulator state);
}
