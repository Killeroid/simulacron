package simulacron.strategy.fate;

import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.strategy.Strategy;
import simulacron.strategy.AbstractStrategy;


/**
 * User: Simon
 * Date: 7/8/13
 * Time: 2:26 PM
 */
public class FateStrategy extends AbstractStrategy<Fate> {

Strategy<Fate> killAppStrategy;
Strategy<Fate> addAppStrategy;


    public FateStrategy(String n, Strategy<Fate> add, Strategy<Fate> kill) {
      super(n);
        killAppStrategy = kill; //new simulacron.strategy.NullStrategy<Fate>()
        addAppStrategy = add;
    }

    @Override
    public void evolve(Simulator graph, Fate agent) {
        killAppStrategy.evolve(graph,agent);
        addAppStrategy.evolve(graph,agent);
    }
}
