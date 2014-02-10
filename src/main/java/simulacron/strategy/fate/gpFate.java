package simulacron.strategy.fate;

import simulacron.model.BipartiteGraph;
import simulacron.model.Fate;
import simulacron.model.Platform;
import simulacron.strategy.AbstractStrategy;

//import com.simulacron.runner;


public class gpFate  extends AbstractStrategy<Fate> {
protected gpFate(String n) {
	super(n);
}

public gpFate() {
	super("gpFate");
}

@Override
public void evolve(BipartiteGraph graph, Fate agent) {
	String program = "(service_shove (simulacron_getMaxApps float_eq (simulacron_atMaxPlatforms) (platform_shove (integer_dup)) " 
			+ "(float_max exec_if simulacron_getPlatformSize)) (((exec_eq platform_rot platform_shove) simulacron_recombineBiggest) " 
			+ "((integer_gt ((integer_min) (platform_getAge) simulacron_inheritPlatform)) simulacron_split_rand simulacron_recombineBestWorst) " 
			+ "platform_yankdup integer_rot (float_fromboolean)) (((boolean_not) float_rot (boolean_dup float_swap)) float_pop " 
			+ "(simulacron_getInitApps) exec_s) (float_rot simulacron_getSmallestPlatform simulacron_split ((platform_yank platform_rot " 
			+ "(boolean_fromfloat) ((exec_noop) (service_swap (simulacron_getBiggestPlatform integer_gt)) (float_max "
			+ "(simulacron_replaceServices exec_s)) integer_shove) simulacron_removeServices)) (integer_add exec_nullJ (integer_gt))))";
	//graph = runner.runner(program, graph);
}

@Override
public void init(String stratId) {}
}