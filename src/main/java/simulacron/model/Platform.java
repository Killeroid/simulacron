package simulacron.model;


import java.util.List;

import sim.engine.SimState;
import sim.field.network.Edge;
import sim.util.Bag;
import simulacron.strategy.Strategy;
import simulacron.util.config.Configuration;


/**
 * Platforms are agents pro-actively modifying their state according to some diversification rules
 * to be included in their step() method. By modifying their state they may also affect the network
 * topology, which should be updated accordingly.
 * 
 * @author Marco Biazzini
 */
public class Platform extends Entity {


double pressure;

public String action;


public double getPressure() {
	return pressure;
}


public String getAction() {
	return action;
}


public Platform() {}

public Platform(int id) {}


public Platform(Platform platform) {
	super((Entity)platform);
	this.pressure = platform.pressure;
	this.action = platform.action;
}

public void init(String entityId, BipartiteGraph graph) {
	super.init(entityId, graph);
	int numberServices = Math.min(
	    Configuration.getInt(entityId + ".services", graph.getNumServices()), graph.getNumServices());
	// for (Service s : graph.selectServices(numberServices)) {
	// BipartiteGraph.addUnique(services, s);
	// }
	while (numberServices > 0) {
		numberServices -= BipartiteGraph.addUnique(services, graph.selectSingleService()) >= 0 ? 1 : 0;
	}
	pressure = 0;
	action = "none";
}


public Platform(int id, List<Service> servs, Strategy<Platform> strategy) {
	super(id, strategy);
	for (Service s : servs) {
		BipartiteGraph.addUnique(services, s);
	}
	pressure = 0;
	action = "none";
}


/*
 * (non-Javadoc)
 * @see simulacron.model.Entity#step(sim.engine.SimState)
 */
@SuppressWarnings("unchecked")
@Override
public void step(SimState state) {
	BipartiteGraph graph = (BipartiteGraph)state;
	action = "none";
	if (getDegree() >= graph.getPlatformMaxLoad() && getSize() > graph.getPlatformMinSize()) {
		strategy.evolve(graph, this);
	}
	pressure = ((double)getDegree()) / graph.getPlatformMaxLoad();
	if (pressure > 1.0) pressure = 1.0;

	// printoutCurStep(graph);
}

public boolean atMaxLoad(BipartiteGraph graph) {
	if (graph.weightedLinks) {
		int maxload = Configuration.getInt(kind + ".maxload", (graph.getPlatformMaxLoad() * Math.min(
			    Configuration.getInt(kind + ".services", graph.getNumServices()), graph.getNumServices()))); 
		int load = 0;
		Bag edges = graph.bipartiteNetwork.getEdgesIn(this);
		for (Object o : edges) {
			load += (int)(((Edge)o).getWeight());
		}
		return load >= maxload;
		
	} else {
		return degree >= graph.getPlatformMaxLoad();
	}
}

public int getLoad(BipartiteGraph graph) {
	int load = 0;
	if (graph.weightedLinks) {
		Bag edges = graph.bipartiteNetwork.getEdgesIn(this);
		for (Object o : edges) {
			load += (int)(((Edge)o).getWeight());
		}
		return load;
	} else {
		return degree;
	}
}

public double getPopularityByLoad(BipartiteGraph graph){
	int connections = 0;
	for (Platform p: graph.platforms) {
		connections += p.getLoad(graph);
	}
	return getLoad(graph) / connections;
}

public double getPopularityByPressure(BipartiteGraph graph){
	int connections = 0;
	for (Platform p: graph.platforms) {
		connections += p.getPressure();
	}
	return getPressure() / connections;
}

@Override
public String toString() {
	String res = super.toString();
	res += " ; pressure = " + pressure + " ; action = " + action;
	return res;
}

}
