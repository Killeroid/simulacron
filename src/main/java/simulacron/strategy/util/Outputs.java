package simulacron.strategy.util;


import simulacron.model.Simulator;


public class Outputs {

private static String separator = System.getProperty("line.separator") + "  ";

public static void consoleOutput(Simulator graph) {
	System.out.println(graph.getPrintoutHeader() + separator
			+ "Platforms: " + graph.platforms.size() + separator
			+ "Apps: " + graph.apps.size() + separator
			+ "Services: " + graph.services.size() + separator
			);
}
}
