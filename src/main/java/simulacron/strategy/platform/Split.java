package simulacron.strategy.platform;


import java.util.ArrayList;

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
public class Split extends AbstractStrategy<Platform> {

double ratio;


public Split() {
	super("Split");
}


public Split(String n, double r) {
	super(n);
	ratio = r;
}


@Override
public void evolve(Simulator graph, Platform platform) {
	split_Part(graph, platform);
}


/**
 * Split this platform in two and partition the services, so that the most common services among the
 * linked application are kept in this instance and the other half of the services are removed and
 * assigned to the newly created platform.
 * 
 * @param graph
 */
private void split_Part(Simulator graph, Platform platform) {
	Bag edges = graph.bipartiteNetwork.getEdges(this, null); // read-only!
	ArrayList<App> ents = new ArrayList<App>();
	for (Object o : edges) {
		ents.add((App)((Edge)o).getOtherNode(this));
	}
	// get the services used by the apps, sorted from the most to the least common
	ArrayList<Service> sortedServices = platform.sortServices(edges);
	int splitIndex = (int)Math.round(sortedServices.size() * ratio);

	// split the platform and keep here only the most shared half of the services
	@SuppressWarnings("unchecked")
	String kind = graph.apps.get(0).getKind();
	Platform p = graph.createPlatform(kind);
	p.setServices(sortedServices.subList(splitIndex, sortedServices.size()));
	graph.createLinks(p, ents);

	for (int i = splitIndex; i < sortedServices.size(); i++) {
		platform.getServices().remove(sortedServices.get(i)); // FIXME : make it O(log(services)) !!!
	}
	graph.updateLinks(platform);
	platform.action = "split_part";
	System.err.println(graph.getPrintoutHeader() + "Split : NEW " + p.toString());
}
}
