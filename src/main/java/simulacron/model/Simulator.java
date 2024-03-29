package simulacron.model;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;
import simulacron.strategy.Strategy;
import simulacron.strategy.fate.KillFates;
import simulacron.strategy.fate.LinkStrategyFates;
import simulacron.util.Log;
import simulacron.util.config.Configuration;
import simulacron.metrics.*;
import ec.util.MersenneTwisterFast;


/**
 * Build a bipartite graph of platforms and apps linked by services provided/used. Create and
 * schedule entities (platforms and apps) as independent agents. Maintain the network topology in a
 * single data structure and takes care of its consistent updating (see comments about the start()
 * method). More info in the comments of the methods.
 * 
 * @author Marco Biazzini
 * @author Vivek Nallur
 * @author Hui Song
 * @author Andre Elie
 * @author Kwaku Yeboah-Antwi
 */
public class Simulator extends SimState {

/**
 * Initial number of platforms.
 */
int initPlatforms;

/**
 * Initial number of apps.
 */
int initApps;

/**
 * Initial total number of services
 */
int initServices;

/**
 * Maximum number of platforms.
 */
int maxPlatforms;

/**
 * Maximum number of apps.
 */
int maxApps;

/**
 * Maximum total number of services.
 */
int maxServices;

/**
 * Max number of links a platform bears without triggering some diversification rule.
 */
int platformMaxLoad;

/**
 * Min number of services a platform shall host.
 */
int platformMinSize;

/**
 * Maximum number of scheduled events per agent.
 */
double maxCycles;

/**
 * All the platforms' strategies must be in this array.
 */
public ArrayList<Strategy<? extends Steppable>> entityStrategies;

/**
 * All the platforms currently in the simulation must be in this array.
 */
public ArrayList<Platform> platforms;

/**
 * All the apps currently in the simulation must be in this array.
 */
public ArrayList<App> apps;

/**
 * All the services currently in the simulation must be in this array.
 */
public ArrayList<Service> services;

/**
 * The bipartite graph. An edge links a platform to an app. Edges are weighed according to the
 * number of services in common between the two entities.
 */
public Network bipartiteNetwork;

/**
 * Invisible agent that can affect the history of the simulation by injecting external events.
 */
public Fate fate;

public static MetricsMonitor metrics;

protected boolean changed;

public static Simulator INSTANCE = null;

protected boolean centralized;

private boolean supervised;

private static String configPath;

public int stepsPerCycle;

// service bundles for applications
ArrayList<ArrayList<Service>> serviceBundles;

private int nextBundle;

// multi run
static Calendar startingDate;

static int multiRunSeed = -1;

static String simulationDescription;

static int dataCycleStep;

static Method robustnessLinkingMethod;

static Method robustnessKillingMethod;

// Map<metricname, value>
static Map<String, Object> metricsSnapshot;

static List<Map<String, Object>> metricsSnapshotHistory;

// Map<strategy, results>
static Map<String, RobustnessResults> singleRunRobustnessByStrategy;

static List<RobustnessResults> robustnessHistory;

boolean weightedLinks = false;

boolean debug = true;

public CalcRobustness robust;


/**
 * Getters and setters. Any Java Bean getter/setter is auto-magically included in the GUI. If a
 * variable has an associated setter here, it can be modify at runtime via the Model tab of the GUI.
 */

public int getInitPlatforms() {
	return initPlatforms;
}


public int getInitApps() {
	return initApps;
}


public int getInitServices() {
	return initServices;
}


public int getMaxPlatforms() {
	return maxPlatforms;
}


public void setMaxPlatforms(int p) {
	maxPlatforms = p;
}


public int getMaxApps() {
	return maxApps;
}


public void setMaxApps(int p) {
	maxApps = p;
}


public int getMaxServices() {
	return maxServices;
}


public void setMaxServices(int p) {
	maxServices = p;
}


public int getPlatformMaxLoad() {
	return platformMaxLoad;
}


public void setPlatformMaxLoad(int newLoad) {
	platformMaxLoad = newLoad;
}


public int getPlatformMinSize() {
	return platformMinSize;
}


public void setPlatformMinSize(int minsize) {
	platformMinSize = minsize;
}


/**
 * Current number of platforms.
 */
public int getNumPlatforms() {
	return platforms.size();
}


/**
 * Current number of apps.
 */
public int getNumApps() {
	return apps.size();
}


/**
 * Current number of services.
 */
public int getNumServices() {
	return services.size();
}


public double getMaxCycles() {
	return maxCycles;
}


public void setMaxCycles(double d) {
	maxCycles = d;
}


public int getCurCycle() {
	return (int)Math.ceil(((double)schedule.getSteps() + 1) / stepsPerCycle);
}


public double getAvgPlatformDegree() {
	int sum = 0;
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumPlatforms() == 0) return 0.0;
	for (Platform p : platforms) {
		sum += p.getDegree();
	}
	return sum / getNumPlatforms();
}


public double getAvgAppDegree() {
	int sum = 0;
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumApps() == 0) return 0.0;
	for (App a : apps) {
		sum += a.getDegree();
	}
	return sum / getNumApps();
}


public double getAvgPlatformSize() {
	int sum = 0;
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumPlatforms() == 0) return 0.0;
	for (Platform p : platforms) {
		sum += p.getSize();
	}
	return sum / getNumPlatforms();
}


public double getAvgAppSize() {
	int sum = 0;
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumApps() == 0) return 0.0;
	for (App a : apps) {
		sum += a.getSize();
	}
	return sum / getNumApps();
}


/*
 * public double getRobustness() { if (schedule.getTime() <= Schedule.BEFORE_SIMULATION ||
 * getNumApps() == 0) return 0.0; try { return Robustness.calculateRobustness(this,
 * LinkStrategyFates.class.getDeclaredMethod("linkingA", simulacron.class),
 * KillFates.class.getDeclaredMethod("randomExact", simulacron.class, int.class)); } catch
 * (Exception e) { e.printStackTrace(); return -1; } }
 */


public double getShannon() {
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumApps() == 0) return 0.0;
	return (Double)metrics.getSnapshot().get(MetricsMonitor.SHANNON_PLATFORM);
}


public double getGiniSimpson() {
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumApps() == 0) return 0.0;
	return (Double)metrics.getSnapshot().get(MetricsMonitor.GS_PLATFORM);
}


public double getAveDiff() {
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumApps() == 0) return 0.0;
	return (Double)metrics.getSnapshot().get(MetricsMonitor.DIFF_PLATFORM);
}


/*
 * public double getMaxShannon() { if (schedule.getTime() <= Schedule.BEFORE_SIMULATION ||
 * getNumApps() == 0) return 0.0; return Math.log(this.getNumPlatforms()); } public double
 * getMinGiniSimpson() { if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumApps() == 0)
 * return 0.0; return 1 - 1 /
 * (Double)metrics.getSnapshot().get(MetricsMonitor.NUM_SPECIES_PLATFORM); }
 */


public int getCountSpecies() {
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumApps() == 0) return 0;
	return (Integer)metrics.getSnapshot().get(MetricsMonitor.NUM_SPECIES_PLATFORM);
}


public int getAliveAppsNumber() {
	if (schedule.getTime() <= Schedule.BEFORE_SIMULATION || getNumApps() == 0) return 0;
	int counter = 0;
	for (App app : apps) {
		if (app.isAlive()) {
			counter++;
		}
	}
	return counter;
}


/**
 * Dynamic persistent data structures should be created here.
 */
private void init() {
	// create fields (executed only once).
	// After stop or deserialization from checkpoint, only start() is called.
	bipartiteNetwork = new Network(false);
	platforms = new ArrayList<Platform>();
	apps = new ArrayList<App>();
	services = new ArrayList<Service>();
	entityStrategies = new ArrayList<Strategy<? extends Steppable>>();
	serviceBundles = new ArrayList<ArrayList<Service>>();
	try {
		if (configPath == null) {
			configPath = getClass().getResource("/neutralModel.conf").toString();
		}
		Configuration.setConfig(configPath);
	}
	catch (IOException e) {
		e.printStackTrace();
		System.err.println("ERROR : Configuration file not found.");
		System.exit(1);
	}
	stepsPerCycle = 0;
	INSTANCE = this;
	supervised = true;
}


public Simulator(long seed) {
	super(seed);
	init();
}


public Simulator(MersenneTwisterFast random) {
	super(random);
	init();
}


public Simulator(MersenneTwisterFast random, Schedule schedule) {
	super(random, schedule);
	init();
}


public Simulator(long seed, Schedule schedule) {
	super(seed, schedule);
	init();
}


/**
 * Creates a clone for the sole purpose of the extinction sequence in Robustness calculation
 * 
 * @return clone
 */
public Simulator extinctionClone() {
	Simulator clone = new Simulator(random());
	clone.bipartiteNetwork = new Network(bipartiteNetwork);
	clone.initPlatforms = initPlatforms;
	clone.initApps = initApps;
	clone.initServices = initServices;
	clone.maxPlatforms = maxPlatforms;
	clone.maxApps = maxApps;
	clone.maxServices = maxServices;
	clone.platformMaxLoad = platformMaxLoad;
	clone.platformMinSize = platformMinSize;
	clone.maxCycles = maxCycles;
	clone.entityStrategies = entityStrategies;

	for (Platform platform : platforms) {
		clone.platforms.add(new Platform(platform));
	}

	for (App app : apps) {
		clone.apps.add(new App(app));
	}

	clone.services = services;
	clone.fate = fate;
	clone.changed = changed;
	clone.centralized = true;
	clone.supervised = supervised;
	clone.weightedLinks = weightedLinks;
	clone.stepsPerCycle = stepsPerCycle;
	clone.serviceBundles = serviceBundles;
	clone.nextBundle = nextBundle;
	clone.debug = debug;
	return clone;
}


private void readConfig() {
	if (supervised) {
		try {
			Configuration.setConfig(configPath);
		}
		catch (IOException e) {
			System.err.println("WARNING : Configuration file not found. Using previous configuration.");
		}
	}
	int seed;
	if (multiRunSeed == -1) {
		seed = Configuration.getInt("seed", 0);
		multiRunSeed = seed;
	} else {
		seed = multiRunSeed;
	}
	if (seed != 0) {
		random().setSeed(seed);
	}
	if (debug) System.err.println("Config : seed = " + seed);
	supervised = Configuration.getBoolean("supervised");
	initApps = Configuration.getInt("init_apps");
	initPlatforms = Configuration.getInt("init_platforms");
	initServices = Configuration.getInt("init_services");
	maxCycles = Configuration.getDouble("max_cycles", Schedule.MAXIMUM_INTEGER - 1);
	maxApps = Configuration.getInt("max_apps", 0);
	if (maxApps == 0) maxApps = Integer.MAX_VALUE;
	maxPlatforms = Configuration.getInt("max_platforms", 0);
	if (maxPlatforms == 0) maxPlatforms = Integer.MAX_VALUE;
	maxServices = Configuration.getInt("max_services", 0);
	if (maxServices == 0) maxServices = Integer.MAX_VALUE;
	platformMaxLoad = Configuration.getInt("p_max_load");
	platformMinSize = Configuration.getInt("p_min_size");
	centralized = Configuration.getBoolean("centralized");
	weightedLinks = Configuration.getBoolean("weighted_links");
	// simulation description string
	simulationDescription = "Apps=" + initApps + ",Services=" + initServices + ",Cycles=" + maxCycles
	    + ",Platforms=" + initPlatforms + "/" + maxPlatforms + ",Load=" + platformMaxLoad;
}


@SuppressWarnings("unchecked")
protected void initFate() throws IllegalAccessException, InstantiationException,
		ClassNotFoundException {
	Strategy<?> st = getStrategy(Configuration.getString("fate.strategy", null));
	if (st != null)
		fate = new Fate((Strategy<Fate>)st);
	else
		fate = null;
}


// create initial platforms
protected void initPlatform() throws IllegalAccessException, InstantiationException,
		ClassNotFoundException {
	int c = getNumPlatforms();
	for (String kind : Configuration.getSpecies("platform")) {
		createEntities(kind, initPlatforms, platforms);
		if (debug) System.err.println("Config : INFO : created " + (getNumPlatforms() - c) 
				+ " new platforms of type " + kind);
		c = getNumPlatforms();
	}
}


// create initial apps
protected void initApp() throws IllegalAccessException, ClassNotFoundException,
		InstantiationException {
	int c = getNumApps();
	for (String s : Configuration.getSpecies("app")) {
		createEntities(s, initApps, apps);
		if (debug) System.err.println("Config : INFO : created " + (getNumApps() - c) + " new apps of type " + s);
		c = getNumApps();
	}
}


protected void initServices() {
	int c = Service.counter;
	for (String s : Configuration.getSpecies("service")) {
		long size = Math.round(initServices * Configuration.getDouble(s, 1));
		for (int i = 0; i < size && getNumServices() < initServices; i++) {
			services.add(new Service(Service.counter, Service.counter, 1, ServiceState.OK));
			Service.counter++;
		}
		if (debug) System.err.println("Config : INFO : created " + (Service.counter - c) 
				+ " new services of type " + s);
		c = Service.counter;
	}
}


public ArrayList<Service> nextBundle() {
	return serviceBundles.get(nextBundle++);
}


/**
 * This method is called ONCE at the beginning of every simulation. EVERY field, parameter,
 * structure etc. MUST be initialized here (and not in the constructor).
 */
public void start(String path) {
	configPath = path;
	if (debug) System.err.println("Config : INFO : Starting with config file: " + path);
	start();
}

public void start() {
	super.start();
	// reset all parameters and fields
	platforms.clear();
	apps.clear();
	services.clear();
	serviceBundles.clear();
	bipartiteNetwork.clear();
	entityStrategies.clear();
	changed = true;
	centralized = false;
	stepsPerCycle = 0;
	nextBundle = 0;
	Service.counter = 0;
	readConfig();
	if (services.isEmpty()) {
		initServices();
		for (int i = 0; i < initApps; i++) {
			serviceBundles.add(selectServices(0));
		}
		// limiting service list to the services actually required by apps
		Set<Service> requiredServices = new HashSet<Service>();
		for (ArrayList<Service> bundle : serviceBundles) {
			requiredServices.addAll(bundle);
		}
		int oldSize = services.size();
		services = new ArrayList<Service>(requiredServices);
//		Log.info("START: Compacted service list, size " + oldSize + " => " + services.size());
	}

	try {
		initFate();
		initPlatform();
		initApp();
		//metrics = MetricsMonitor.createMetricsInstance(this);
		metricsSnapshotHistory = new LinkedList<Map<String, Object>>();
		robustnessHistory = new LinkedList<RobustnessResults>();
		robustnessLinkingMethod = LinkStrategyFates.class.getDeclaredMethod("linkingC",
		    LinkStrategyFates
		    .getLinkingMethods().get("linkingC"));
		robustnessKillingMethod = KillFates.class.getDeclaredMethod("unattendedExact", KillFates
		    .getKillingMethods().get("unattendedExact"));
		robust = new CalcRobustness(INSTANCE, 3);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	if (!centralized) stepsPerCycle++;

	if (fate != null) {
		schedule.scheduleRepeating(schedule.getTime() + 1.3, fate, 1.0);
		stepsPerCycle++;
	}

	// An invisible model will printout the state of the graph after all the entities
	// (platform and apps) have done one step, but before the fate might do something.
	// Thus at each epoch the order of the events is: all the entities (randomly shuffled),
	// then the network printout, then fate.
	stepsPerCycle++;
	Steppable print = new Steppable() {

		public void step(SimState state) {
			if (debug) System.out.println("CYCLE " + getCurCycle() + " Apps alive: " + getAliveAppsNumber());
			if (changed) {
				if (debug) printoutNetwork();
			}
			changed = false;
//			System.out.println("METRICS: " + metrics.recordSnapshot());
			if (getCurCycle() + 1 == (int)getMaxCycles()) state.schedule.seal();
			/*if (getCurCycle() % dataCycleStep == 0) {
				System.out.println("CYCLE " + getCurCycle());
				metricsSnapshotHistory.add(metrics.getSnapshot());
				robustnessHistory.add(Robustness.calculateRobustness((simulacron)state,
				    robustnessLinkingMethod, robustnessKillingMethod));
				// System.out
				// .println("Robustness " + Robustness.calculateAllRobustness((simulacron)state));
			}*/
			if (state.schedule.scheduleComplete()) {
				// multi run results save
				//metricsSnapshot = metrics.getSnapshot();
//				singleRunRobustnessByStrategy = Robustness.calculateAllRobustness((simulacron)state);
				// multi run seed randomization
				multiRunSeed = random().nextInt();
//				System.out.println("\nEXTINCTION SEQUENCE: " + System.getProperty("line.separator") 
//						+ Extinction.ExtinctionReport((simulacron)state, 10) + System.getProperty("line.separator"));
				
			}
		}
	};
	schedule.scheduleRepeating(schedule.getTime() + 1.2, print, 1.0);

}


private static String organizeResults(String header,
    List<Map<String, Object>> metricsSnapshotHistory, List<RobustnessResults> robustnessHistory) {
	String result = "";
	int cycle;
	for (int i = 0; i < metricsSnapshotHistory.size(); i++) {
		cycle = i * dataCycleStep;
		result += header;
		result += cycle + ",";
		result += robustnessHistory.get(i).getRobustness() + ",";
		result += metricsSnapshotHistory.get(i).get(MetricsMonitor.SHANNON_PLATFORM) + ",";
		result += metricsSnapshotHistory.get(i).get(MetricsMonitor.NUM_PLATFORM) + ",";
		result += metricsSnapshotHistory.get(i).get(MetricsMonitor.NUM_APP_ALIVE) + ",";
		result += metricsSnapshotHistory.get(i).get(MetricsMonitor.NUM_SPECIES_PLATFORM) + ",";
		result += metricsSnapshotHistory.get(i).get(MetricsMonitor.MEAN_NUM_PLATFORM_PER_SPECIE) + ",";
		result += System.getProperty("line.separator");
	}
	return result;
}


public static String simulate(String[] args, int runsNumber, int currentConfig,
 String configFile,
    String title,
 String resultFolderPath,
    boolean metricsColumnWritten) {
	String resultsAsText = "";
	String allResults = "Configuration,Run,Cycle,Robustness,Shannon,NumPlat,NumAppAlive,NumSpecies,MeanSpecieSize"
	    + System.getProperty("line.separator");
	Map<String, DescriptiveStatistics> metricStat = new LinkedHashMap<String, DescriptiveStatistics>();
	Map<String, Map<String, DescriptiveStatistics>> robustnessStatByStrategy = new LinkedHashMap<String, Map<String, DescriptiveStatistics>>();
	boolean polarity = true;
	long startTime;
	for (int j = 0; j < runsNumber; j++) {
		startTime = System.currentTimeMillis();
		System.err.println("RUN: starting run " + (currentConfig + 1) + "." + (j + 1));
		// run execution
		doLoop(Simulator.class, args);
		// renewing seed value
		multiRunSeed += (polarity ? -1 : 1) * (currentConfig + j + 1);
		polarity = !polarity;
		allResults += organizeResults(configFile + "," + ("run" + j) + ",", metricsSnapshotHistory,
		    robustnessHistory);
		/*
		 * // gathering metrics for statistical calculation for (String metric :
		 * metricsSnapshot.keySet()) { if (!metricStat.containsKey(metric)) { metricStat.put(metric, new
		 * DescriptiveStatistics()); } if
		 * (metricsSnapshot.get(metric).getClass().getSimpleName().equalsIgnoreCase("double")) {
		 * metricStat.get(metric).addValue((double)metricsSnapshot.get(metric)); } else {
		 * metricStat.get(metric).addValue(new Double((int)metricsSnapshot.get(metric))); } } //
		 * gathering robustness results for statistical calculation for (String strategy :
		 * singleRunRobustnessByStrategy.keySet()) { RobustnessResults singleRunRobustness =
		 * singleRunRobustnessByStrategy.get(strategy); // robustness value if
		 * (!robustnessStatByStrategy.containsKey(strategy)) { robustnessStatByStrategy.put(strategy,
		 * new LinkedHashMap<String, DescriptiveStatistics>()); } Map<String, DescriptiveStatistics>
		 * robustnessStat = robustnessStatByStrategy.get(strategy); if
		 * (!robustnessStat.containsKey("robustness")) { robustnessStat.put("robustness", new
		 * DescriptiveStatistics()); }
		 * robustnessStat.get("robustness").addValue(singleRunRobustness.getRobustness()); // alive apps
		 * per step value int numberOfSteps = singleRunRobustness.getAliveAppsHistory().size(); for (int
		 * k = 0; k < numberOfSteps; k++) { String stepColumnName = "step" + Robustness.nameStep(k,
		 * numberOfSteps); if (!robustnessStat.containsKey(stepColumnName)) {
		 * robustnessStat.put(stepColumnName, new DescriptiveStatistics()); }
		 * robustnessStat.get(stepColumnName).addValue(
		 * singleRunRobustness.getAliveAppsHistory().get(k)); } robustnessStatByStrategy.put(strategy,
		 * robustnessStat); }
		 */
		System.err.println("RUN: ending run " + (currentConfig + 1) + "." + (j + 1) + " | duration = "
		    + (System.currentTimeMillis() - startTime));
	}


	/*
	 * // results output // metrics // column titles if (!metricsColumnWritten) { resultsAsText +=
	 * "configuration,"; for (String metricName : metricStat.keySet()) { resultsAsText += metricName +
	 * "Mean,"; } resultsAsText += "\n"; } // data resultsAsText += configFile + ","; for (String
	 * metricName : metricStat.keySet()) { resultsAsText += metricStat.get(metricName).getMean() +
	 * ","; } // resultsFileWriter.write("\n"); resultsAsText += "\n"; // robustness FileWriter
	 * robustnessFileWriter; String robustnessFileName = "robustness_" + startingDate.getTime() + "_"
	 * + configFile + "_" + title + ".csv"; File robustnessFile = new File(resultFolderPath + "/" +
	 * robustnessFileName); try { // simulation description robustnessFileWriter = new
	 * FileWriter(robustnessFile); // column titles robustnessFileWriter.write(simulationDescription +
	 * "\n\n"); robustnessFileWriter.write("strategy,P25,Min,Max,P75,Mean"); Map<String,
	 * DescriptiveStatistics> robustnessStat = robustnessStatByStrategy.values() .iterator().next();
	 * robustnessFileWriter.write("\n"); // data for (String strategy :
	 * robustnessStatByStrategy.keySet()) { robustnessStat = robustnessStatByStrategy.get(strategy);
	 * robustnessFileWriter.write(strategy + ",");
	 * robustnessFileWriter.write(robustnessStat.get("robustness").getPercentile(25) + ",");
	 * robustnessFileWriter.write(robustnessStat.get("robustness").getMin() + ",");
	 * robustnessFileWriter.write(robustnessStat.get("robustness").getMax() + ",");
	 * robustnessFileWriter.write(robustnessStat.get("robustness").getPercentile(75) + ",");
	 * robustnessFileWriter.write(robustnessStat.get("robustness").getMean() + ",");
	 * robustnessFileWriter.write("\n"); } robustnessFileWriter.write("\n\n\n");
	 * robustnessFileWriter.write("strategy,"); for (String robustnessStatName :
	 * robustnessStat.keySet()) { if (robustnessStatName.startsWith("step")) {
	 * robustnessFileWriter.write(robustnessStatName + "Mean,"); } } robustnessFileWriter.write("\n");
	 * // data for (String strategy : robustnessStatByStrategy.keySet()) { robustnessStat =
	 * robustnessStatByStrategy.get(strategy); robustnessFileWriter.write(strategy + ","); for (String
	 * robustnessStatName : robustnessStat.keySet()) { if (robustnessStatName.startsWith("step")) {
	 * robustnessFileWriter.write(robustnessStat.get(robustnessStatName).getMean() + ","); } }
	 * robustnessFileWriter.write("\n"); } robustnessFileWriter.flush(); robustnessFileWriter.close();
	 * } catch (IOException e) { Log.warn("In method main, can't write robustness result file");
	 * e.printStackTrace(); } return resultsAsText;
	 */
	return allResults;
}

public static void main() {
	main(null);
}

public static void main(String[] args) {
	if (args.length == 0) args = new String[]{"-quiet"};
	// loggers levels
	Log.ERROR();
	// run starting time
	startingDate = Calendar.getInstance();
	dataCycleStep = 10;
	// configuration files folder path
	String configFolderPath = null;
	String resultFolderPath = System.getProperty("user.dir");
	// number of runs for statistical calculation
	int runsNumber = 1;
	// experimentation title
	String title = null;
	// bag of configuration file names (to be sorted)
	Bag configList;
	// reading command line parameters
	for (int i = 0; i < args.length; i++) {
		if (args[i].equals("-help")) {
			System.out.println("Command line options:" + System.getProperty("line.separator")
			    + "  -help             displays this message" + System.getProperty("line.separator")
			    + "  -size             number of runs for statistical results"
			    + System.getProperty("line.separator")
			    + "  -configfolder     folder path to a set of .conf files (multirun)"
			    + System.getProperty("line.separator")
			    + "  -resultfolder     folder path to store the result files"
			    + System.getProperty("line.separator")
			    + "  -configuration    the configuration file path");
			break;
		} 
		if (args[i].equals("-configuration") && (i + 1) < args.length) {
			configPath = args[i + 1];
			System.err.println("COMMAND LINE PARAMETER: configuration file = "
			    + configPath);
		}
		if (args[i].equals("-configfolder") && (i + 1) < args.length) {
			configFolderPath = args[i + 1];
			System.err.println("COMMAND LINE PARAMETER: configuration files folder path = "
			    + configFolderPath);
		}
		if (args[i].equals("-resultfolder") && (i + 1) < args.length) {
			resultFolderPath = args[i + 1];
			System.err.println("COMMAND LINE PARAMETER: result files folder path = " + resultFolderPath);
		}
		if (args[i].equals("-size") && (i + 1) < args.length) {
			runsNumber = Integer.parseInt(args[i + 1]);
			System.err.println("COMMAND LINE PARAMETER: statistical sample size = " + runsNumber);
		}
		if (args[i].equals("-title") && (i + 1) < args.length) {
			title = args[i + 1];
			System.err.println("COMMAND LINE PARAMETER: title = " + title);
		}
	}

	// reading config folder to gather config files
	if (configFolderPath != null) {
		configList = new Bag();
		File confFolder = new File(configFolderPath);
		if (confFolder.isDirectory()) {
			File[] configFiles = confFolder.listFiles();
			for (int i = 0; i < configFiles.length; i++) {
				if (configFiles[i].getAbsolutePath().endsWith(".conf")) {
					configList.add(configFiles[i].getAbsolutePath());
					System.err.println("CONFIG: found configuration file " + configFiles[i].getName());
				}
			}
		}
		configList.sort();
		// multi run: #config files X #runs
		boolean metricsColumnWritten = false;
		/*
		 * String metricsFileName = "metrics_" + startingDate.getTime() + (title != null ? "_" + title :
		 * "") + ".csv"; File metricsFile = new File(resultFolderPath + "/" + metricsFileName);
		 */
		String resultsFileName = "results" + startingDate.getTime()
		    + (title != null ? "_" + title : "") + ".csv";
		File resultsFile = new File(resultFolderPath + "/" + resultsFileName);
		String resultsAsText = "";
		String allSimulationDescription = "";
		for (int currentConfigNumber = 0; currentConfigNumber < configList.size(); currentConfigNumber++) {
			configPath = (String)configList.get(currentConfigNumber);
			String currentConfigFile = new File(configPath).getName();
			if (currentConfigFile.endsWith(".conf"))
				currentConfigFile = currentConfigFile.substring(0, currentConfigFile.length() - 5);
			System.err.println("RUN: starting run for configuration " + configPath);
			resultsAsText += simulate(args, runsNumber, currentConfigNumber, currentConfigFile, title,
			    resultFolderPath,
			    metricsColumnWritten);
			multiRunSeed = -1;
			metricsColumnWritten = true;
			allSimulationDescription += simulationDescription + "\n";
		}
		/*
		 * try { FileWriter resultsFileWriter = new FileWriter(metricsFile);
		 * resultsFileWriter.write(allSimulationDescription); resultsFileWriter.write("\n");
		 * resultsFileWriter.write(resultsAsText); resultsFileWriter.flush(); resultsFileWriter.close();
		 * } catch (IOException e) { e.printStackTrace(); }
		 */
		try {
			FileWriter resultsFileWriter = new FileWriter(resultsFile);
			// resultsFileWriter.write(allSimulationDescription);
			// resultsFileWriter.write("\n");
			resultsFileWriter.write(resultsAsText);
			resultsFileWriter.flush();
			resultsFileWriter.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	} else {
		doLoop(Simulator.class, args);
	}
	System.exit(0);
}

public void end() throws Throwable {
	
	//System.exit(0);
	try {
	super.finish();
	super.finalize();
	finalize();
	} catch(Exception e) {
		System.out.println("Error Ending");
		e.printStackTrace();
	}
	if (debug) System.out.println("Ended Simulation");
}

public double robustnessScore(int trials) {
	robust.setRuns(trials);
	//CalcRobustness cRobust = new CalcRobustness(INSTANCE, trials);
//	System.out.println("\nEXTINCTION SEQUENCE: " + System.getProperty("line.separator") 
//			+ cRobust.returnRobustness() + System.getProperty("line.separator"));
//	double score = CalcRobustness.returnRobustness(INSTANCE, trials);
//	cRobust.finalize();
//	cRobust = null;
//	System.gc();
	return robust.returnRobustness();
	
}


/**
 * Returns a random selection of services. The number of services returned is specified by the given
 * argument. If the argument is <= 0, a gaussian-distributed number of randomly selected services is
 * returned. The number of services returned is always at least 1 and at most
 * {@link #getNumServices()}.
 * 
 * @param size
 *          Number of services to return.
 * @return A random selection of services.
 */
public ArrayList<Service> selectServices(int size) {
	ArrayList<Service> servs = new ArrayList<Service>();
	if (size < 1) size = (int)((random().nextGaussian() + 3) / 6 * (getNumServices() - 1) + 1);
	size = size < 1 ? 1 : size > getNumServices() ? getNumServices() : size;
	for (int j = 0; j < size; j++) {
		servs.add(services.get(random().nextInt(getNumServices())));
	}

	return servs;
}


public Service selectSingleService() {
	return services.get(random().nextInt(getNumServices()));
}


/**
 * This method should always be used by an entity to create a new entity (app, platform or fate). It
 * takes care of adding the entity to the global arrays, to the network and to schedule it with the
 * other entities.
 * 
 * @param entityName
 * @return
 */
public void createEntities(String entityName, long maxSize, List<? extends Entity> all)
		throws IllegalAccessException, InstantiationException, ClassNotFoundException {
	long bound = Math.round(maxSize * Configuration.getDouble(entityName + ".ratio"));
	for (int i = 0; i < bound; i++) {
		Entity entity = createEntity(entityName);

		addUnique(all, entity);
	}
	changed = true;
}


public Entity createEntity(String entityName) throws ClassNotFoundException,
		IllegalAccessException, InstantiationException {
	String className = Configuration.getString(entityName + ".class");
	Class<?> cl = Class.forName(className);
	Entity entity = (Entity)cl.newInstance();
	entity.init(entityName, this);
	bipartiteNetwork.addNode(entity);
	if (!centralized)
		entity.setStoppable(schedule.scheduleRepeating(Math.floor(schedule.getTime() + 1.0), entity,
				1.0));
	return entity;
}


public App createApp(String entityName) {
	App app = null;
	try {
		app = (App)createEntity(entityName);
	}
	catch (Exception e) {
		Log.error("createApp: error " + e.getMessage());
		return null;
	}
	addUnique(apps, app);
	return app;
}


public Platform createPlatform(String entityName) {
	Platform platform = null;
	try {
		platform = (Platform)createEntity(entityName);
	}
	catch (Exception e) {
		Log.error("createPlatform: error " + e.getMessage());
		return null;
	}
	addUnique(platforms, platform);
	return platform;
}


public static Strategy<? extends Steppable> getStrategy(String strategyName) {
	String id = "";
	for (String strategy : Configuration.getSpecies("strategy")) {
		if (strategyName.equals(Configuration.getString(strategy + ".name"))) {
			id = strategy;
			break;
		}
	}
	String className = Configuration.getString(id + ".class");
	Strategy<?> strategy = null;
	try {
		Class<?> cl = Class.forName(className);
		strategy = (Strategy<?>)cl.newInstance();
		strategy.init(id);
	}
	catch (Exception e) {
		Log.error("getStrategy: error " + e.getMessage());
		return null;
	}

	return strategy;
}


public static ArrayList<Service> removableServices(Bag needLinks, 
		ArrayList<Service> available, int howMany) {
	@SuppressWarnings("unchecked")
	ArrayList<Service> needed = new ArrayList<Service>(needLinks);
	return removableServices(needed, available, howMany);
}

public static ArrayList<Service> removableServices(ArrayList<Service> needLinks, 
		ArrayList<Service> available, int howMany) { 
	if (!Configuration.contains("weighted_links") || !Configuration.getBoolean("weighted_links")) 
		howMany = available.size();
	ArrayList<Service> removable = new ArrayList<Service>();
	if (howMany > 0 && needLinks.size() > 0 && available.size() > 0) {
		int counter = 0;
		Iterator<Service> needed = needLinks.iterator();
		while (needed.hasNext() && counter <= howMany) {
			Service next = needed.next();
			if (Collections.binarySearch(available, next) >= 0) {
				removable.add(next);
				counter++;
			}
		}
	}
	return removable;
}

/*
 * Return the list of strategies for each strategy type thats going
 * to be used for the extinction sequence
 * 
 * @param A string representing the strategy type
 * 		eg. "kill" or "link"
 */
public static ArrayList<Strategy<? extends Steppable>> getExtinctionStrategies(String strategyType) {
	ArrayList<Strategy<? extends Steppable>> strategies = new ArrayList<Strategy<? extends Steppable>>();
	
	try {
		String[] stratList = Configuration.getString("extinction." + strategyType).split(" ");
		for (String strat: stratList) {
			Strategy<? extends Steppable> strategy = (Strategy<? extends Steppable>)Simulator.getStrategy(strat);
			strategies.add(strategy);
		}
	}
	catch (Exception e) {
		Log.error("getExtinctionStrategies[" + strategyType + "strategies] : error " + e.getMessage());
		return null;
	}
	return strategies;
}

/*
 * Calculate the real edge weight of a possible edge between
 * two entities
 * 
 */
public int calcRealWeight(App app, Platform pltf, Object info) {
	int weight;
	if (info instanceof Number) {
		weight = ((Number)info).intValue();
	} else {
		weight = app.countCommonServices(pltf, null);	
	}
	if (weightedLinks && (weight + pltf.getDegree()) > getPlatformMaxLoad()) {
		weight = getPlatformMaxLoad() - pltf.getDegree();
	}
	return weight;
}

/*
 * Given two entities, figure out which is the app entity and 
 * which the platform entity, then calculate the possible edge weight 
 * between them
 */
public int returnRealEdgeWeight(Entity to, Entity from, Object info) {
	Platform pltf;
	App app;
	if (to instanceof Platform) {
		pltf = (Platform)to;
		app = (App)from;
	} else if (from instanceof Platform) {
		pltf = (Platform)from;
		app = (App)to;
	} else {
		return 0;
	}
	return calcRealWeight(app, pltf, info);
}

public void addEdge(Entity from, Entity to, Object info) {
	if (weightedLinks) {
		int edgeWeight = returnRealEdgeWeight(to, from, info);
		bipartiteNetwork.addEdge(from, to, edgeWeight);
		from.setDegree(from.degree + edgeWeight);
		to.setDegree(to.degree + edgeWeight);
	} else {
		bipartiteNetwork.addEdge(from, to, info);
		from.incDegree();
		to.incDegree();
	}
	changed = true;
}


public void removeEdge(Entity e, Edge edge) {
	Object rem = edge.getOtherNode(e);
	bipartiteNetwork.removeEdge(edge);
	if (weightedLinks) {
		int edgeWeight = (int)edge.getWeight();
		e.setDegree(e.degree - edgeWeight);
		((Entity)rem).setDegree(((Entity)rem).degree - edgeWeight);
	} else {
		e.decDegree();
		((Entity)rem).decDegree();
	}
	changed = true;
}


public void removeAllEdges() {
	bipartiteNetwork.removeAllEdges();
	for (Object n : bipartiteNetwork.getAllNodes()) {
		((Entity)n).degree = 0;
	}
	changed = true;
}


/**
 * Update existing links that have the argument at one end. This method should always be used by an
 * entity after triggering some diversification rule that doesn't modify edges directly, but implies
 * (as a side effect) that existing edges to/from the entity had become inconsistent and should be
 * fixed.
 * 
 * @param e
 */
public void updateLinks(Entity e) {
	// the graph is undirected, thus EdgesIn = EdgesOut
	Bag edges = bipartiteNetwork.getEdgesIn(e); // read-only!
	int w;
	Entity rem;
	Edge edge;
	int edgeWeight;
	for (Object o : edges) {
		edge = (Edge)o;
		rem = (Entity)edge.getOtherNode(e);
		edgeWeight = (int)edge.getWeight();
		w = e.countCommonServices(rem, null);
		if (edgeWeight != w) {
			bipartiteNetwork.removeEdge(edge);
			if (w > 0) {
				bipartiteNetwork.addEdge(e, rem, new Integer(w));
			} else {
				if (weightedLinks) {
					e.setDegree(e.degree - edgeWeight);
					rem.setDegree(rem.degree - edgeWeight);
				} else {
					e.decDegree();
					rem.decDegree();
				}
				
			}
			changed = true;
		}
	}
}


/**
 * Associate links to/from a NEWLY created entity. To be used only for a new entity, if needed. The
 * second argument is a list of existing entities to consider linking with.
 * 
 * @param e
 *          New entity to be introduced in the network.
 * @param entities
 *          The entities to establish links with.
 */
public void createLinks(Entity e, ArrayList<? extends Entity> entities) {
	int l, r, weight;
	for (Entity remote : entities) {
		weight = l = r = 0; // services are sorted according to their ID...
		while (l < e.services.size() && r < remote.services.size()) {
			if (e.services.get(l).equals(remote.services.get(r))) {
				weight++;
				l++;
				r++;
			} else if (e.services.get(l).compareTo(remote.services.get(r)) > 0) {
				r++;
			} else {
				l++;
			}
		}
		if (weight > 0) {
			addEdge(e, remote, new Integer(weight));
		}
	}

}


public void setLink(App app, Platform pltf){
	if (weightedLinks) {
		int edgeWeight = calcRealWeight(app, pltf, null);
		bipartiteNetwork.addEdge(app, pltf, edgeWeight);
		app.setDegree(app.degree + edgeWeight);
		pltf.setDegree(pltf.degree + edgeWeight);
	} else {
		bipartiteNetwork.addEdge(app, pltf, 1);
		app.degree ++;
		pltf.degree ++;
	}
	changed = true;
}


/**
 * Textual printout of the network
 */
private void printoutNetwork() { // TODO
	System.out.println(getPrintoutHeader() + bipartiteNetwork.toString());
	System.out.flush();
}


/**
 * Standard header for all printout lines.
 * 
 * @return The standard printout header.
 */
public String getPrintoutHeader() {
	return "Cycle " + getCurCycle() + " [" + schedule.getSteps() + "] : ";
}


public MersenneTwisterFast random() {
	return random;
}


/**
 * It gets a object from a given list, that matches the given target. The object is cast to the
 * runtime class of the variable to which is assigned to, with NO type check. The list must be
 * ordered according to the natural ordering of the items. If the list contains duplicates, the
 * element returned is the one that would be found by {@link java.util.Collections#binarySearch}.<br>
 * No type checking on the argument to search for is performed. Thus the caller must be sure that
 * the arguments are mutually comparable.
 * 
 * @param list
 *          The list that hosts the items
 * @param target
 *          An object comparable with the elements in the list
 * @return The object in the list that matches target, or null.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public static <T> T getElement(List list, Comparable target) {
	int i = Collections.binarySearch(list, target);
	if (i >= 0) {
		return (T)list.get(i);
	} else
		return null;
}


/**
 * It adds {@link java.lang.Comparable} objects (of any type) to the given list. The list will be
 * always ordered according to the natural ordering of the items. No duplicates are allowed in the
 * list, thus no addition occurs if an item is already in the list.<br>
 * No type checking on the objects being added is performed. Thus the caller must be sure that the
 * items being added are consistent with respect to their mutual comparison.
 * 
 * @param set
 *          The list that hosts the items
 * @param item
 *          The object to be added
 * @return The [0, set.size()) index of the item in the List.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public static int addUnique(List set, Comparable item) {
	int i = Collections.binarySearch(set, item);
	if (i < 0) {
		// found
		i = -i - 1;
		set.add(i, item);
		return i;
	} else {
		// not found
		return -1;
	}
}


/**
 * It provides the printout of the given data in the given output stream. If the argument is an
 * array, print one element per line, each line starting with the array index of the element. It
 * does not handle Interfaces and Enums.
 * 
 * @param data
 *          Data to be printed
 * @param trailer
 *          A string that will always be printed after the data
 * @param out
 *          Stream in which the data printout must be written
 */
@SuppressWarnings({"unchecked", "rawtypes"})
static public void printAny(Object data, String trailer, PrintStream out) {
	int size, i = 0;
	if (data == null) {
		out.print("NULL");
	} else if (data instanceof Map) {
		Entry ec = null;
		Iterator ecit = ((Map)data).entrySet().iterator();
		while (ecit.hasNext()) { // && i++ < 3
			ec = (Map.Entry)ecit.next();
			printAny(ec.getKey(), " :\n", out);
			printAny(ec.getValue(), "\n------------------------------", out);
		}
	} else if (data instanceof List) {
		List<Object> a = (List<Object>)data;
		size = a.size();
		for (i = 0; i < size; i++) { // && i < 5
			out.print(" entry # " + i + " : ");
			printAny(a.get(i), "\n", out);
		}
	} else if (data.getClass().isArray()) {
		Object e;
		size = Array.getLength(data);
		for (i = 0; i < size; i++) { // && i < 5
			e = Array.get(data, i);
			out.print(" [" + i + "] ");
			printAny(e, "\n", out);
		}
	} else if (data.getClass().isPrimitive()) {
		out.print(data);
	} else if (!(data.getClass().isEnum() || data.getClass().isInterface())) {
		out.print((data.getClass().cast(data)).toString());
	} else {
		out.println("\nERROR : cannot print " + data.getClass().toString() + " !");
	}
	out.print(trailer);
	out.flush();
}


public <T extends Entity> void removeEntity(ArrayList<T> eList, T entity) {
	eList.remove(Collections.binarySearch(eList, entity));
	if (!centralized) entity.stop();
	Bag edges = bipartiteNetwork.getEdgesIn(entity); // edgesIn = edgesOut
	for (Object o : edges) {
		if (weightedLinks) {
			Entity e = (Entity)((Edge)o).getOtherNode(entity);
			int edgeWeight = (int)(((Edge)o).getWeight());
			e.setDegree(e.degree - edgeWeight);
		} else {
			((Entity)((Edge)o).getOtherNode(entity)).decDegree();
		}
		
		
	}
	bipartiteNetwork.removeNode(entity);
	changed = true;
}

}
