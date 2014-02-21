package simulacron.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Services are comparable, so that they can be kept in sorted array
 * (see the constructor of the entities, e.g. {@code Platform#Platform(int, java.util.List)})
 * to optimize the access and speed up the comparison of the entities.
 *
 * @author Marco Biazzini
 * 
 * Services can both depend on other services and also provide/subsume other services
 */
public class Service implements Comparable<Service> {

	/**
	 * counter for the services
	 */
	public static int counter;

	int id;
	int name;
	int version;
	ServiceState state;
	static int maxLastVersion = 1;
	Set<Service> dependencies = new HashSet<Service>(); //Services I require/depend on
	Set<Service> supersedes = new HashSet<Service>(); //Services I supersede/provide

	public Service(int id) {
		this.id = id;
		name = id;
		version = 1;
		state = ServiceState.OK;
	}

	public Service(int id, int name, int version, ServiceState state) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.state = state;
		maxLastVersion = Math.max(maxLastVersion,version);
	}
	
	/*
	 * Services that I depend on
	 */
	public List<Service> getDependencies() {
		return new ArrayList<Service>(this.dependencies);
	}
	

	/*
	 * Add new dependencies
	 * Check to make sure any dependency isnt already provided
	 */
	public void addDependencies(List<Service> new_deps) {
		this.dependencies.addAll(new_deps);
	}
	
	public void addDependencies(Service s) {
		this.dependencies.add(s);
	}
	
	public boolean dependsOn(Service s) {
		return (id == s.id || dependencies.contains(s));
	}
	
	
	
	/*
	 * Services that this provides
	 */
	public List<Service> provides() {
		return new ArrayList<Service>(this.supersedes);
	}

	
	public void addProvides(List<Service> srvcs) {
		this.supersedes.addAll(srvcs);
		if (this.dependencies.removeAll(this.supersedes)) {
			System.out.println("Servce " + id + " removed some dependencies");
		}
	}
	
	public void addProvides(Service srvc) {
		this.supersedes.add(srvc);
		if (this.dependencies.remove(srvc)) {
			System.out.println("Servce " + id + " removed this dependency");
		}
	}
	
	public boolean canProvide(Service s) {
		return (id == s.id || supersedes.contains(s));
	}
	
	public boolean canProvide(List<Service> srvs) {
		boolean provides = true;
		for (Service s: srvs) {
			if (canProvide(s)) {
				continue;
			} else {
				provides = false;
				break;
			}
		}
		return provides;
	}
	

	@Override
	public int compareTo(Service s) {
		return id - s.id;
	}

	public int getName() {
		return name;
	}

	public int getID() {
		return id;
	}

	public boolean equals(Object o) {
		if (o instanceof Service)
			return compareTo((Service) o) == 0;
		return false;
	}

	public Service newVersion() {
		Service srvc = new Service(id, name, version + 1, ServiceState.OK);
		srvc.addDependencies(getDependencies());
		srvc.addProvides(provides());
		return srvc;
	}
	
	public Service mergeService(Service s) {
		Service srvc = new Service(Service.counter);
		Service.counter++;
		//Add all services this service depends on
		srvc.addDependencies(getDependencies());
		srvc.addDependencies(s.getDependencies());
		//Add all services this services provides/subsumes
		srvc.addProvides(provides());
		srvc.addProvides(s.provides());
		return s;
	}

	@Override
	public String toString() {
		return name + ":" + version + ":" +state;
	}

	public double getPopularity(Simulator graph) {
		double appTotal = graph.apps.size();
		double appUsingNumber = 0;
		for (App app : graph.apps) {
			if (app.getServices().contains(this)) {
				appUsingNumber++;
			}
		}
		return appUsingNumber / appTotal;
	}

	public double getAvailability(Simulator graph) {
		double platformTotal = graph.platforms.size();
		double platformUsingNumber = 0;
		for (Platform platform : graph.platforms) {
			if (platform.getServices().contains(this)) {
				platformUsingNumber++;
			}
		}
		return platformUsingNumber / platformTotal;
	}
}
