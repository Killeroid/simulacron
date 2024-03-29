package simulacron.strategy.platform;


import java.util.ArrayList;
import java.util.Arrays;

import sim.field.network.Edge;
import sim.util.Bag;
import simulacron.model.App;
import simulacron.model.Simulator;
import simulacron.model.Platform;
import simulacron.model.Service;
import simulacron.strategy.AbstractStrategy;


/**
 * User: Simon Date: 7/8/13 Time: 10:21 AM
 */
public class CloneMutate extends AbstractStrategy<Platform> {

double factor;


public CloneMutate() {
	super("Clone");
}


public CloneMutate(String n, double m) {
	super(n);
	factor = m > 0.4 ? 0.4 : m;
}


@Override
public void evolve(Simulator graph, Platform platform) {
	clone_Mutate(graph, platform);
}


/**
 * Clone this instance and mutate both this instance and the clone, so that one randomly chosen
 * service in each instance (not the same in both) is removed. Then update the network so that the
 * apps link to the proper instance(s).
 * 
 * @param graph
 */
private void clone_Mutate(Simulator graph, Platform platform) {
	int csize, i;
	Bag temp = new Bag();
	Bag edges = graph.bipartiteNetwork.getEdges(this, null); // read-only!
	ArrayList<App> ents = new ArrayList<App>();
	for (Object o : edges) {
		ents.add((App)((Edge)o).getOtherNode(this));
	}
	for (i = 0; i < platform.getSize(); i++)
		temp.add(i);
	temp.shuffle(graph.random);
	Object[] set = temp.toArray();
	csize = (int)Math.round(platform.getSize() * factor);
	csize = csize < 1 ? 1 : csize;

	// generate a clone that has all the services of this platform but size * factor.
	ArrayList<Service> servs = new ArrayList<Service>(platform.getServices());
	Arrays.sort(set, 0, csize);
	for (i = csize - 1; i >= 0; i--)
		servs.remove(((Integer)set[i]).intValue());
	@SuppressWarnings("unchecked")
	String kind = graph.platforms.get(0).getKind();
	Platform p = graph.createPlatform(kind);
	graph.createLinks(p, ents);

	// remove different size * factor services from this platform
	Arrays.sort(set, csize, csize * 2);
	for (i = (csize * 2) - 1; i >= csize; i--)
		platform.getServices().remove(((Integer)set[i]).intValue());
	graph.updateLinks(platform);
	platform.action = "clone_mutate";
	System.err.println(graph.getPrintoutHeader() + "CloneMutate : INFO : ADDED " + p.toString());
}
}
