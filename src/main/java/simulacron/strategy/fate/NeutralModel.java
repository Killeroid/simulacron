package simulacron.strategy.fate;

import simulacron.model.BipartiteGraph;
import simulacron.model.Fate;
import simulacron.strategy.AbstractStrategy;
import simulacron.strategy.Strategy;
import simulacron.util.config.Configuration;

/**
 * User: Simon
 * Date: 9/25/13
 * Time: 4:05 PM
 */
public class NeutralModel extends AbstractStrategy<Fate> {
protected Strategy<Fate> kill;
protected Strategy<Fate> add;

public NeutralModel() {
	super("NeutralModel");
}

@Override
public void evolve(BipartiteGraph graph, Fate agent)  {
	kill.evolve(graph,agent);
	add.evolve(graph,agent);
	//graph.removeAllEdges();
}

@Override
public void init(String stratId) {
	kill = (Strategy<Fate>) BipartiteGraph.getStrategy(Configuration.getString(stratId + ".kill"));
	add = (Strategy<Fate>) BipartiteGraph.getStrategy(Configuration.getString(stratId + ".add"));
}
}
