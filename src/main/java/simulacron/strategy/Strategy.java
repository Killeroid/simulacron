package simulacron.strategy;

import simulacron.model.Simulator;
import sim.engine.Steppable;

/**
 * User: Simon
 * Date: 7/8/13
 * Time: 10:11 AM
 */
public interface Strategy<T extends Steppable> {

    /**
     * @param agent the model which is evolve
     * @param graph The Simulator.
     */
    public void evolve(Simulator graph, T agent);

void init(String stratId);

}
