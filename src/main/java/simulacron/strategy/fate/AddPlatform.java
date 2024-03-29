package simulacron.strategy.fate;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simulacron.model.Simulator;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.model.Service;
import simulacron.strategy.AbstractStrategy;


/**
 * User: Simon Date: 7/8/13 Time: 2:33 PM
 */
public class AddPlatform extends AbstractStrategy<Fate> {

public AddPlatform() {
	super("AddPlatform");
}


@Override
public void evolve(Simulator graph, Fate agent) {
	Map<List<Service>, Integer> species = getSpecies(graph);
	List<Service> p = selectRandomSpecies(species, graph.getNumPlatforms());

	if (species.get(p).equals(1)) {
		childrenPlatform(graph, p);
	} else {
		clonePlatform(graph, p);
	}
}


protected void clonePlatform(Simulator graph, List<Service> services) {
	String kind = graph.platforms.get(0).getKind();
	Platform platform = graph.createPlatform(kind);
	platform.setServices(services);
	// graph.createLinks(platform, graph);
	System.out.println(graph.getPrintoutHeader() + "Fate : ADDED " + platform);
}


protected void childrenPlatform(Simulator graph, List<Service> p) {
	String kind = graph.platforms.get(0).getKind();
	Platform platform = graph.createPlatform(kind);

	p.remove(graph.random.nextInt(p.size()));
	p.remove(graph.random.nextInt(p.size()));

	for (Service s : graph.selectServices(2)) {
		Simulator.addUnique(p, s);
	}
	platform.setServices(p);

	//System.out.println(graph.getPrintoutHeader() + "Fate : ADDED " + platform);
}


@Override
public void init(String stratId) {}


protected List<Service> selectRandomSpecies(Map<List<Service>, Integer> distribution, int nbPlatform) {
	List<List<Service>> list = new ArrayList<List<Service>>();

	for (List<Service> p : distribution.keySet()) {
		long bound = Math.round((double)nbPlatform / (double)distribution.get(p));
		for (int i = 0; i < bound; i++) {
			list.add(p);
		}
	}
	return list.get(Simulator.INSTANCE.random.nextInt(list.size()));
}


protected Map<List<Service>, Integer> getSpecies(Simulator graph) {
	Map<List<Service>, Integer> map = new HashMap<List<Service>, Integer>();

	for (Platform p : graph.platforms) {
		List<Service> pServices = p.getServices();
		Integer value = map.get(pServices);
		if (value != null)
			map.put(pServices, value++);
		else
			map.put(pServices, 1);
	}
	return map;
}
}
