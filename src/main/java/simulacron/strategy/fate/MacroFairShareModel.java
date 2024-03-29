package simulacron.strategy.fate;

import java.util.Comparator;

import sim.util.Bag;
import simulacron.model.App;
import simulacron.model.Simulator;
import simulacron.model.Entity;
import simulacron.model.Fate;
import simulacron.strategy.AbstractStrategy;
import simulacron.strategy.Strategy;
import simulacron.util.config.Configuration;

/* Apps with the least amount of services are assigned 
 * to the most powerful nodes first in that order.
 * Uses the MacroFairShare strategy
 * 
 * @author Kwaku Yeboah-Antwi
 */


public class MacroFairShareModel extends AbstractStrategy<Fate> {
protected Strategy<Fate> kill;
protected Strategy<Fate> add;
protected Strategy<App> link;

public MacroFairShareModel() {
	super("MacroFairShareModel");
}

@Override
public void evolve(Simulator graph, Fate agent)  {
	LeastResourceHungryFirst(graph, link);
	kill.evolve(graph,agent);
	add.evolve(graph,agent);
}

@Override
public void init(String stratId) {
	kill = (Strategy<Fate>) Simulator.getStrategy(Configuration.getString(stratId + ".kill"));
	add = (Strategy<Fate>) Simulator.getStrategy(Configuration.getString(stratId + ".add"));
	link = new simulacron.strategy.application.MacroFairShare();
}

private void LeastResourceHungryFirst(Simulator graph, Strategy<App> Link) {
	Bag entities = new Bag(graph.getNumApps());
	for (App a : graph.apps)
		entities.add(a);
	entities.sort(new Comparator<Entity>() {

		@Override
		public int compare(Entity e, Entity e2) {
			return e.getSize() - e2.getSize();
		}
	});
	for (Object o : entities) {
		((Entity)o).setStrategy(link);
		((Entity)o).step(graph);
	}

}
}
