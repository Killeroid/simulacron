package simulacron.strategy.fate;


import sim.util.Bag;

import simulacron.model.*;
import simulacron.strategy.Strategy;
import simulacron.strategy.AbstractStrategy;


public class FateAlmighty extends AbstractStrategy<Fate> {

Strategy<Fate> strategy;

public FateAlmighty(String n, Strategy<Fate> s) {
  super(n);
  strategy = s;
}


@Override
public void evolve(Simulator graph, Fate agent) {
  randomizedEntityEvolution(graph);
  strategy.evolve(graph, agent);
}


private void randomizedEntityEvolution(Simulator graph) {
  Bag entities = new Bag(graph.getNumApps() + graph.getNumPlatforms());
  for (App a : graph.apps)
    entities.add(a);
  for (Platform p : graph.platforms)
    entities.add(p);
  entities.shuffle(graph.random);
  for (Object o : entities)
    ((Entity)o).step(graph);
}

}
