package simulacron.strategy.environment;

import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.strategy.AbstractStrategy;
import simulacron.strategy.Strategy;
import simulacron.strategy.NullStrategy;
import simulacron.util.config.Configuration;

/**
 * Add merge classes for merging two apps
 */
public class Pressures extends AbstractStrategy<Fate> {
//protected Strategy<Fate> kill;
//protected Strategy<Fate> add;
protected Strategy<Fate> appKill;
protected Strategy<Fate> appAdd;
//protected Strategy<Fate> appLink;
protected Strategy<Fate> platformKill;
protected Strategy<Fate> platformAdd;
//protected Strategy<Fate> platformLink;


public Pressures() {
	super("Pressures");
}

@Override
public void evolve(Simulator graph, Fate agent)  {
	//kill.evolve(graph,agent);
	//add.evolve(graph,agent);
	appKill.evolve(graph,agent);
	appAdd.evolve(graph,agent);
	//appLink.evolve(graph,agent);
	platformKill.evolve(graph,agent);
	platformAdd.evolve(graph,agent);
	//platformLink.evolve(graph,agent);
	//graph.removeAllEdges();
}

@Override
public void init(String stratId) {
	//kill = (Strategy<Fate>) simulacron.getStrategy(Configuration.getString(stratId + ".kill"));
	//add = (Strategy<Fate>) simulacron.getStrategy(Configuration.getString(stratId + ".add"));
	appKill = getStrategy(stratId, "appKill");
	appAdd = getStrategy(stratId, "appAdd");
	//appLink = getStrategy(stratId, "appLink");
	platformKill = getStrategy(stratId, "platformKill");
	platformAdd = getStrategy(stratId, "platformAdd");
	//platformLink = getStrategy(stratId, "platformLink");
}

@SuppressWarnings("unchecked")
protected Strategy<Fate> getStrategy(String stratId, String stratUse) {
	Strategy<Fate> strat = (Strategy<Fate>) Simulator.getStrategy(Configuration.getString(stratId + "." + stratUse));
	if (strat == null) {
		System.out.print("No " + stratUse + " strategy specified. Using NullStrategy");
		try {
			strat = (Strategy<Fate>) NullStrategy.class.newInstance();
			strat.init("NullStrategy." + stratUse);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("Error initializing Nullstrategy as " + stratUse + " strategy");
			System.exit(0);
		}
	}
	return strat;
}
}