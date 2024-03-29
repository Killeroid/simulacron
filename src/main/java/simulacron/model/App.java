package simulacron.model;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sim.engine.SimState;
import simulacron.strategy.Strategy;


/**
 * @author Marco Biazzini
 * @author Vivek Nallur
 * @author Hui Song
 *
 * In each step, an App has the authority may choose to reproduce itself,
 *         either via clone or speciation, decided by the {@link #reproducers}, which are all
 *         instances of @ AppReproductionStrategy} . An App dies if a instance of @
 *         AppExtinctionStrategy} in {@link #killers} suggests so. A {@link #dead} app will not
 *         reproduce, and will also be removed from the bipartite graph.
 */
public class App extends Entity {

private double redundancy = 0;


public List<Service> getDependencies() {
	return this.services;
}


public void addDependencies(List<Service> new_deps) {
	Set<Service> all_services = new HashSet<Service>(this.services);
	all_services.addAll(new_deps);
	this.services.clear();
	this.services.addAll(all_services);
}


public void removeDependencies(List<Service> obs_deps) {
	// The removeAll method should ideally execute in O(n) time
	// however at least as of java 1.7, it does not. Inserting
	// the obsolete_dependencies into a HashSet ensures that array
	// compaction happens only once, and thus is reasonably optimal

	this.services.removeAll(new HashSet(obs_deps));
}


public double getRedundancy() {
	return redundancy > 0 ? redundancy : 0;
}

public void setRedundancy(Simulator graph) {
	redundancy = ((double)degree) / graph.getNumPlatforms();
}

public App(int id, List<Service> servs, Strategy<App> strategy) {
	super(id, strategy);
	for (Service s : servs) {
		Simulator.addUnique(services, s);
	}
}


public App() {};

public App(int id) {};

public App(App app) {
    super((Entity)app);
    this.redundancy = app.redundancy;
}


public void init(String entityId, Simulator graph) {
	super.init(entityId, graph);
	// int nSer = Configuration.getInt(entityId + ".services");
	for (Service s : graph.nextBundle()) {
		Simulator.addUnique(services, s);
	}
}


/*
 * (non-Javadoc)
 * @see simulacron.model.Entity#step(sim.engine.SimState)
 */
@SuppressWarnings("unchecked")
@Override
public void step(SimState state) {
	Simulator graph = (Simulator)state;
	strategy.evolve(graph, this);

	setRedundancy(graph);
	if (redundancy > 1.0) redundancy = 1.0;
	// printoutCurStep(graph);
}


@Override
public String toString() {
	String res = super.toString();
	res += " ; redundancy = " + redundancy;
	return res;
}
}
