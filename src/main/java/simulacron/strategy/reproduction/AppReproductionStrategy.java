package simulacron.strategy.reproduction;

import java.util.List;

import simulacron.model.App;
import simulacron.model.Simulator;

/**
 * This is the interface that all App reproduction strategies must implement
 * @author Vivek Nallur
 * @author Hui Song
 */
public interface AppReproductionStrategy{
        public List<App> reproduce(App parent, Simulator state);
}
