package simulacron.metrics;


import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import simulacron.model.Simulator;


public class RobustnessRun implements Callable<RobustnessResults> {

Simulator graph;

Method linkingMethod;

Method killingMethod;


public RobustnessRun(Simulator graph, Method linkingMethod, Method killingMethod) {
	this.graph = graph;
	this.linkingMethod = linkingMethod;
	this.killingMethod = killingMethod;
}


@Override
public RobustnessResults call() {
	return Robustness.calculateRobustness(graph, linkingMethod, killingMethod);
}

}
