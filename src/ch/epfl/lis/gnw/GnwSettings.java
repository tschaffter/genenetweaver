/*
Copyright (c) 2008-2010 Daniel Marbach & Thomas Schaffter

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://gnw.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.gnw;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import cern.jet.random.Uniform;
import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import ch.epfl.lis.sde.SdeSettings;


/** 
 * Offers global parameters (settings) and functions used by all classes of the
 * gnw package.
 * 
 * GnwSettings makes use of the Singleton design pattern: There's at most one
 * instance present, which can only be accessed through getInstance().
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class GnwSettings {	
	
	/** The unique instance of Universal (Singleton design pattern) */
	private static GnwSettings instance_ = null;
	
	/** Mersenne Twister random engine (should be used by all other random number generators) */
	private MersenneTwister mersenneTwister_;
	/** Uniform distribution random number generator */
	private Uniform uniformDistribution_;
	/** Normal distribution random number generator */
	private Normal normalDistribution_;

	// VARIOUS
	/** Current version of GNW */
	private String gnwVersion_ = "3.1.3 Beta";
	/** Seed for the random number generator. Set to -1 to use current time */
	private int randomSeed_ = -1;
	/** Default output directory to save stuff */
	private String outputDirectory_ = "";
	/** Model proteins and translation */
	private boolean modelTranslation_ = true;
	/** Set true to remove self-links (Gi->Gi) when generating kinetic models */
	//private boolean removeAutoregulatoryInteractionsFromGeneNetworks_;
	/** Set true to ignore self-links (Gi->Gi) when saving gold standards in DREAM format */
	private boolean ignoreAutoregulatoryInteractionsInEvaluation_;
	/** Set true to explicitly list all zero interactions when saving gold standards in DREAM format (DREAM3 and DREAM4 scripts require this) */
	private boolean appendZeroInteractionsInGoldStandardFiles_;
	/** Set true to export gene expression data in a different format than the default (genes in rows and experiments in columns) */
	private boolean outputGenesInRows_;
	
	// SUBNETWORK EXTRACTION
	/** The number of regulators in the extracted networks, set to 0 to disable control of number of regulators */
	private int numRegulators_ = -1;
	/** Vertices are added using truncated selection with the given fraction (0=greedy, 1=random selection) */
	private double truncatedSelectionFraction_ = 0.1;
	/** Number of seeds to be sampled from strongly connected components */
	private int numSeedsFromStronglyConnectedComponents_ = 0;
	
	// STEADY-STATE EXPERIMENTS
	/** Generate steady states for knockouts */
	private boolean ssKnockouts_ = false;
	/** Generate steady states knockdowns */
	private boolean ssKnockdowns_ = false;
	/** Generate steady states for multifactorial perturbations */
	private boolean ssMultifactorial_ = false;
	/** Generate steady states for perturbations as used in the DREAM4 time series */
	private boolean ssDREAM4TimeSeries_ = false;
	/** Generate steady states for dual knockouts */
	private boolean ssDualKnockouts_ = false;
	/**
	 * For deterministic simulations (ODEs), we return the steady-states as soon as convergence is reached.
	 * If there is no convergence until time maxtSteadyStateODE_, the values at this point are returned and a
	 * warning message is displayed.
	 */
	private int maxtSteadyStateODE_ = 2000;
	/** 
	 * For stochastic simulations (SDEs), we always return the values at maxtSteadyStateSDE_ and we
	 * don't test for convergence. 
	 */
	private int maxtSteadyStateSDE_ = -1;
	/**
	 * For SDEs, every experiment starts from an independently sampled wild-type steady state. Specify here
	 * how long the SDEs should be simulated from the previous wild-type to get a new independent sample.
	 * Note, this is here in the section steady-state experiments, but it's also used for the time series.
	 */
	private int mintSDE_ = 100;
	
	// TIME-SERIES EXPERIMENTS
	/** Generate time series for knockouts */
	private boolean tsKnockouts_ = false;
	/** Generate time series knockdowns */
	private boolean tsKnockdowns_ = false;
	/** Generate time series for multifactorial perturbations */
	private boolean tsMultifactorial_ = false;
	/** Generate time series as in DREAM4 */
	private boolean tsDREAM4TimeSeries_ = false;
	/** Generate time series for dual knockouts */
	private boolean tsDualKnockouts_ = false;
	/** Number of time-series experiments from different initial conditions */
	private int numTimeSeries_ = 10; 
	/** Number of measured points per time series (must be consistent with maxtTimeSeries_ and dt_, does *not* affect precision) */
	//private int numMeasuredPoints_ = 21;
	/** Default max duration time in time-series experiments (must be consistent with numTimePoints_ and dt_) */
	private int maxtTimeSeries_ = 1000;
	/** Time step for the time-series (must be consistent with numTimePoints_ and maxtTimeSeries_) */
	private int dt_ = 50;
	
	// PERTURBATIONS
	/** Standard deviation for multifactorial perturbations */
	private double multifactorialStdev_ = 0.33;
	/** The probability that a gene is perturbed (for DREAM4 time series) */
	private double perturbationProbability_ = 0.33;
	/** Set true to load the multifactorial perturbations from existing files */
	private boolean loadPerturbations_ = false;
	/** Min efficacy of gene deletions (set to 1.0 to set transcription rates to 0, set to 0.9 for 90% reduction) */
	private double minGeneDeletionEffect_ = 0.9;
	/** Max efficacy of gene deletions (set to 1.0 to set transcription rates to 0, set to 0.9 for 90% reduction) */
	private double maxGeneDeletionEffect_ = 1;
	/** Min efficacy of gene overexpressions (set to 0.5 to increase transcription rates by 50%) */
	private double minGeneOverexpressionEffect_ = 0.5;
	/** Max efficacy of gene overexpressions (set to 1.0 to increase transcription rates by 100%) */ 
	private double maxGeneOverexpressionEffect_ = 1.5;
	/** Min fraction of TFs that are direct targets of a drug perturbation */
	private double minFractionDirectTargets_ = 1;
	/** Max fraction of TFs that are direct targets of a drug perturbation */
	private double maxFractionDirectTargets_ = 0.1;
	
	// DETERMINISTIC MODEL (ODE)
	/** If set true, a deterministic simulation of the experiments is done using the ODE model */
	private boolean simulateODE_ = false;
	/** Absolute _or_ relative precision _per variable_ need to be satisfied for convergence */
	private double absolutePrecision_ = 0.00001;
	/** See absolutePrecision_, in addition, this is also the tolerance used for integration */ 
	private double relativePrecision_ = 0.001;
	
	// STOCHASTIC MODEL (SDE)
	/** If set true, a stochastic simulation of the experiments is done using the SDE model */ 
	private boolean simulateSDE_ = true;
	/** Time step used for integrating SDEs (internal dt used for integration, the measured points are defined by numMeasuredPoints) */
	private double timeStepSDE_ = 1.0;
	/** Coefficient of the noise term of the SDEs */
	private double noiseCoefficientSDE_ = 0.05;
	
	// EXPERIMENTAL NOISE
	/** Set true to add normal noise to the data */
	private boolean addNormalNoise_ = false;
	/** Set true to add lognormal noise to the data */
	private boolean addLognormalNoise_ = false;
	/** Set true to use a realistic model of microarray noise, similar to a mix of normal and lognormal */
	private boolean addMicroarrayNoise_ = true;
	/** The standard deviation of the normal noise */
	private double normalStdev_ = 0.025;
	/** The standard deviation of the lognormal noise */
	private double lognormalStdev_ = 0.075;
	/** Set true to normalize the datasets after adding the experimental noise */
	private boolean normalizeAfterAddingNoise_;
	
	// RANDOM PARAMETERS
	/** Half-lives in minutes, Dassow2000 use [5 100]. */
	private RandomParameter randomHalfLife_ = new RandomParameterGaussian(5, 50, false);
	/** Dissociation constants, Dassow2000 use [0.001 1] */
	private RandomParameter randomK_ = new RandomParameterUniform(0.01, 1);// DREAM3: (0.01, 1, 0, 0.2, false); lognormal: (0.1, 1, 0.1, 3.1623, true);
	/** Hill coefficients, Dassow2000 use [1 10] */
	private RandomParameter randomN_ = new RandomParameterGaussian(1, 10, 2, 2, false);
	/** Threshold for setting weak activations in random initialization */
	private double weakActivation_ = 0.25;
	/** The difference in gene activation due to a module */
	private RandomParameter randomDeltaActivation_ = new RandomParameterGaussian(weakActivation_, 1, false);
	/** Initialization of low basal rates (leakage) */
	private RandomParameter randomLowBasalRate_ = new RandomParameterGaussian(0, weakActivation_, 0, 0.05, false);//(0.001, 0.25, true);
	/** Initialization of medium basal rates */
	private RandomParameter randomMediumBasalRate_ = new RandomParameterGaussian(weakActivation_, 1-weakActivation_, false);	
	
	// PROCESS STATE
	private boolean stopSubnetExtraction_ = false;
	private boolean stopBenchmarkGeneration_ = false;
	
	/** URL to the last settings file opened */
	private URL lastSettingsURL_ = null;
	
	/** GNW signature (to display when exiting GNW) */
	private String signature_;
	
    /** Logger for this class */
	private static Logger log_ = Logger.getLogger(GnwSettings.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Default constructor
	 */
	public GnwSettings()
	{	
		initializeRandomNumberGenerators();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Get Universal instance
	 */
	static public GnwSettings getInstance()
	{	
		if (instance_ == null)
			instance_ = new GnwSettings();

		return instance_;
	}
	
	// ----------------------------------------------------------------------------

	/** Get a URL from a relative path */
	public URL getURL(String path)
	{	
		try
		{
			return new File(path).toURI().toURL();
		} 
		catch (MalformedURLException e)
		{
			log_.log(Level.WARNING, "GnwSettings::getURL(): Bad URL", e);
		}
		return null;
	}

	// ----------------------------------------------------------------------------
	
	/** Load GNW settings */
	public boolean loadSettings(String filename)
	{
		boolean loaded = false;
		
		try
		{
			//if (filename.isEmpty()) //isEmpty() introduced in Java 1.6
			if(filename.compareTo("") == 0)
				loadSettingsFromURL(getClass().getResource("rsc/settings.txt"));
			else
				loadSettingsFromURL(getURL(filename));
			
			log_.log(Level.INFO, "Done");
			loaded = true;

		} catch (Exception e) {
			//GnwMain.error(e, "Could not load settings file");
			log_.log(Level.SEVERE, "Could not load settings file.");
		}
		
		// Reinitialize the random number generators
		if (randomSeed_ != -1)
			initializeRandomNumberGenerators();
		
		return loaded;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Wrapper to load settings from file paths.
	 * @throws Exception 
	 */
	public void loadSettingsFromURL(URL url) throws IOException, Exception
	{
		lastSettingsURL_ = url;
		loadSettingsFromStream(lastSettingsURL_.openStream());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Reload the last opened settings */
	public void loadLastSettingsOpened() throws IOException, Exception
	{	
		loadSettingsFromStream(lastSettingsURL_.openStream());
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Load settings from InputStream. If the settings are load from a file,
	 * it is important to use the function loadSettingsFile(String filename) instead.
	 * @param stream
	 * @throws Exception
	 */
	public void loadSettingsFromStream(InputStream stream) throws Exception {

		Properties gnwSettings = new Properties();
		gnwSettings.load(stream);
		
		try
		{
			setParameterValues(gnwSettings);
			initializeRandomNumberGenerators(); // v3.0: reset RNG
		}
		catch (NullPointerException e)
		{
			log_.log(Level.SEVERE, "At least one parameter is missing from loaded settings file! See provided settings file on GNW website (gnw.sf.net).");
			System.exit(1);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Set the values of the main GNW parameters */
	public void setParameterValues(Properties gnwSettings) throws Exception {

		// VARIOUS
		randomSeed_ = Integer.valueOf(gnwSettings.getProperty("randomSeed"));
		outputDirectory_ = String.valueOf(gnwSettings.getProperty("outputDirectory"));
		if (outputDirectory_.equals("")) 
			setOutputDirectory(System.getProperty("user.dir")); // SF: user.home
		modelTranslation_ = gnwSettings.getProperty("modelTranslation").equals("0") ?  false : true;
		ignoreAutoregulatoryInteractionsInEvaluation_ = gnwSettings.getProperty("ignoreAutoregulatoryInteractionsInEvaluation").equals("0") ?  false : true;
		appendZeroInteractionsInGoldStandardFiles_ = gnwSettings.getProperty("appendZeroInteractionsInGoldStandardFiles").equals("0") ?  false : true;
		outputGenesInRows_ = gnwSettings.getProperty("outputGenesInRows").equals("0") ?  false : true;
		
		// SUBNETWORK EXTRACTION
		numRegulators_ = Integer.valueOf(gnwSettings.getProperty("numRegulators"));
		truncatedSelectionFraction_ = Double.valueOf(gnwSettings.getProperty("truncatedSelectionFraction"));
		numSeedsFromStronglyConnectedComponents_ = Integer.valueOf(gnwSettings.getProperty("numSeedsFromStronglyConnectedComponents"));
		
		// STEADY-STATE EXPERIMENTS
		ssKnockouts_ = gnwSettings.getProperty("ssKnockouts").equals("0") ?  false : true;
		ssKnockdowns_ = gnwSettings.getProperty("ssKnockdowns").equals("0") ?  false : true;
		ssMultifactorial_ = gnwSettings.getProperty("ssMultifactorial").equals("0") ?  false : true;
		ssDREAM4TimeSeries_ = gnwSettings.getProperty("ssDREAM4TimeSeries").equals("0") ?  false : true;
		ssDualKnockouts_ = gnwSettings.getProperty("ssDualKnockouts").equals("0") ?  false : true;
		maxtSteadyStateODE_ = Integer.valueOf(gnwSettings.getProperty("maxtSteadyStateODE"));
		maxtSteadyStateSDE_ = Integer.valueOf(gnwSettings.getProperty("maxtSteadyStateSDE"));
		mintSDE_ = Integer.valueOf(gnwSettings.getProperty("mintSDE"));

		// TIME-SERIES EXPERIMENTS
		tsKnockouts_ = gnwSettings.getProperty("tsKnockouts").equals("0") ?  false : true;
		tsKnockdowns_ = gnwSettings.getProperty("tsKnockdowns").equals("0") ?  false : true;
		tsMultifactorial_ = gnwSettings.getProperty("tsMultifactorial").equals("0") ?  false : true;
		tsDREAM4TimeSeries_ = gnwSettings.getProperty("tsDREAM4TimeSeries").equals("0") ?  false : true;
		tsDualKnockouts_ = gnwSettings.getProperty("tsDualKnockouts").equals("0") ?  false : true;
		numTimeSeries_ = Integer.valueOf(gnwSettings.getProperty("numTimeSeries"));
		//numMeasuredPoints_ = Integer.valueOf(gnwSettings.getProperty("numMeasuredPoints"));
		maxtTimeSeries_ = Integer.valueOf(gnwSettings.getProperty("maxtTimeSeries"));
		dt_ = Integer.valueOf(gnwSettings.getProperty("dt"));
		
		int numMeasuredPoints = (int)Math.round(maxtTimeSeries_/dt_) + 1;
		if (dt_*(numMeasuredPoints-1) != maxtTimeSeries_)
			throw new RuntimeException("Duration of time series (GnwSettings.maxtTimeSeries_) must be a multiple of the time step (GnwSettings.dt_)");
		
		// PERTURBATIONS
		multifactorialStdev_ = Double.valueOf(gnwSettings.getProperty("multifactorialStdev"));
		perturbationProbability_ = Double.valueOf(gnwSettings.getProperty("perturbationProbability"));
		loadPerturbations_ = gnwSettings.getProperty("loadPerturbations").equals("0") ?  false : true;
		minGeneDeletionEffect_ = Double.valueOf(gnwSettings.getProperty("minGeneDeletionEffect"));
		maxGeneDeletionEffect_ = Double.valueOf(gnwSettings.getProperty("maxGeneDeletionEffect"));
		minGeneOverexpressionEffect_ = Double.valueOf(gnwSettings.getProperty("minGeneOverexpressionEffect"));
		maxGeneOverexpressionEffect_ = Double.valueOf(gnwSettings.getProperty("maxGeneOverexpressionEffect"));
		minFractionDirectTargets_ = Double.valueOf(gnwSettings.getProperty("minFractionDirectTargets"));
		maxFractionDirectTargets_ = Double.valueOf(gnwSettings.getProperty("maxFractionDirectTargets"));

		// DETERMINISTIC MODEL (ODE)
		simulateODE_ = gnwSettings.getProperty("simulateODE").equals("0") ?  false : true;
		absolutePrecision_ = Double.valueOf(gnwSettings.getProperty("absolutePrecision"));
		relativePrecision_ = Double.valueOf(gnwSettings.getProperty("relativePrecision"));
		
		// STOCHASTIC MODEL (SDE)
		simulateSDE_ = gnwSettings.getProperty("simulateSDE").equals("0") ?  false : true;
		timeStepSDE_ = Double.valueOf(gnwSettings.getProperty("timeStepSDE"));
		noiseCoefficientSDE_ = Double.valueOf(gnwSettings.getProperty("noiseCoefficientSDE"));
		
		// EXPERIMENTAL NOISE
		addNormalNoise_ = gnwSettings.getProperty("addNormalNoise").equals("0") ?  false : true;
		addLognormalNoise_ = gnwSettings.getProperty("addLognormalNoise").equals("0") ?  false : true;
		addMicroarrayNoise_ = gnwSettings.getProperty("addMicroarrayNoise").equals("0") ?  false : true;
		normalStdev_ = Double.valueOf(gnwSettings.getProperty("normalStdev"));
		lognormalStdev_ = Double.valueOf(gnwSettings.getProperty("lognormalStdev"));
		normalizeAfterAddingNoise_ = gnwSettings.getProperty("normalizeAfterAddingNoise").equals("0") ?  false : true;
		
		signature_ = "\n";
		signature_ += "Thomas Schaffter (thomas.schaff...@gmail.com)\n";
		signature_ += "Daniel Marbach (daniel.marb...@gmail.com)\n";
		signature_ += "Gilles Roulet (gilles.rou...@gmail.com)\n";
		signature_ += "\n";
		signature_ += "http://tschaffter.ch/projects/gnw";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Get folder of the default GNW directory (in home) */
	public File getGnwDirectory()
	{
		File folder = new File(gnwDirectoryPath());
		return folder;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Get file associated to default settings file */
	public File getCustomGnwSettings()
	{
		File file = new File(personalGnwSettingsPath());
		return file;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Get path to default GNW directory (in home) */
	public String gnwDirectoryPath()
	{
		return System.getProperty("user.home")
				+ System.getProperty("file.separator")
				+ "gnw";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Get path to default settings file */
	public String personalGnwSettingsPath()
	{
		return gnwDirectoryPath()
				+ System.getProperty("file.separator")
				+ "settings.txt";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Return true if a custom default GNW settings file exists */
	public boolean personalGnwSettingsExist()
	{
		return (new File(personalGnwSettingsPath())).exists();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the user path. Could be with or without "/" terminal.
	 * @param absPath Absolute path
	 */
	public void setOutputDirectory(String absPath)
	{
		outputDirectory_ = absPath;
		String sep = System.getProperty("file.separator");
		if (outputDirectory_.charAt(outputDirectory_.length()-1) != sep.charAt(0))
			outputDirectory_ += sep;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Round a given double number with n decimals.
	 * @param a Double number to rounded
	 * @param n Number of decimals
	 * @return The rounded double number
	 */
	public static double floor(double a, int n)
	{	
		double p = Math.pow(10.0, n);
		return Math.floor((a*p)+0.5) / p;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Take a time in [ms] and convert it into [h min s ms].
	 * @param dt [ms]
	 * @return Time [h m s ms]
	 */
	public static String chronometer(long dt)
	{
		int numHours = 0;
		int numMinutes = 0;
		int numSeconds = 0;
		
		System.out.println(dt);
		
		numHours = (int)Math.floor(dt / 3600000.0);
		dt -= numHours * 3600000.0;
		
		numMinutes = (int)Math.floor(dt / 60000.0);
		dt -= numMinutes * 60000.0;
		
		numSeconds = (int)Math.floor(dt / 1000.0);
		dt -= numSeconds * 1000.0;
		
		String time = Integer.toString(numHours) + " h ";
		time += Integer.toString(numMinutes) + " min ";
		time += Integer.toString(numSeconds) + " s ";
		time += Integer.toString((int)dt) + " ms";
		
		return time;
	}
		
	// ----------------------------------------------------------------------------
	
	public static void printArray(double[] v)
	{	
		int size = v.length;
		
		for (int i=0; i < size; i++)
			System.out.print(v[i] + "\t");
		
		System.out.println("");
	}
	
	// ----------------------------------------------------------------------------

	/** Create new instances for the random number generators, initialize with randomSeed_ */
	private void initializeRandomNumberGenerators()
	{
		if (randomSeed_ == -1)
			mersenneTwister_ = new MersenneTwister(new java.util.Date());
		else
			mersenneTwister_ = new MersenneTwister(randomSeed_);
		uniformDistribution_ = new Uniform(mersenneTwister_);
		normalDistribution_ = new Normal(0, 1, mersenneTwister_); // mean 0, stdev 1

		// Use the same RNG for libsde
		SdeSettings.getInstance().setNormalDistribution(normalDistribution_);
	}
	
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public Uniform getUniformDistribution() { return uniformDistribution_; }
	public Normal getNormalDistribution() { return normalDistribution_; }

	public int getRandomSeed() { return randomSeed_; }
	public void setRandomSeed(int s) { randomSeed_ = s; }
	
	public int getNumRegulators() { return numRegulators_; }
	public void setNumRegulators(int numRegulators) { numRegulators_ = numRegulators; }
	
	public double getTruncatedSelectionFraction() { return truncatedSelectionFraction_; }
	public void setTruncatedSelectionFraction(double f) { truncatedSelectionFraction_ = f; }
	
	public int getNumSeedsFromStronglyConnectedComponents() { return numSeedsFromStronglyConnectedComponents_; }
	public void setNumSeedsFromStronglyConnectedComponents(int N) { numSeedsFromStronglyConnectedComponents_ = N; }

	public void setAbsolutePrecision(double value) { absolutePrecision_ = value; }
	public double getAbsolutePrecision() { return absolutePrecision_; }
	
	public void setRelativePrecision(double value) { relativePrecision_ = value; }
	public double getRelativePrecision() { return relativePrecision_; }
	
	public void setAddNormalNoise(boolean b) { addNormalNoise_ = b; }
	public boolean getAddNormalNoise() { return addNormalNoise_; }
	
	public void setAddLognormalNoise(boolean b) { addLognormalNoise_ = b; }
	public boolean getAddLognormalNoise() { return addLognormalNoise_; }
	
	public void setAddMicroarrayNoise(boolean b) { addMicroarrayNoise_ = b; }
	public boolean getAddMicroarrayNoise() { return addMicroarrayNoise_; }
	
	public void setNormalStdev(double s) { normalStdev_ = s; }
	public double getNormalStdev() { return normalStdev_; }
	
	public void setLognormalStdev(double s) { lognormalStdev_ = s; }
	public double getLognormalStdev() { return lognormalStdev_; }
	
	public void setNormalizeAfterAddingNoise(boolean b) { normalizeAfterAddingNoise_ = b; }
	public boolean getNormalizeAfterAddingNoise() { return normalizeAfterAddingNoise_; }
	
	public void setNumTimeSeries(int n) { numTimeSeries_ = n; }
	public int getNumTimeSeries() { return numTimeSeries_; }

	public void setMaxtTimeSeries(int maxt) { maxtTimeSeries_ = maxt; }
	public int getMaxtTimeSeries() { return maxtTimeSeries_; }
	
	public void setMaxtSteadyStateODE(int maxt) { maxtSteadyStateODE_ = maxt; }
	public int getMaxtSteadyStateODE() { return maxtSteadyStateODE_; }
	
	public void setMaxtSteadyStateSDE(int maxt) { maxtSteadyStateSDE_ = maxt; }
	public int getMaxtSteadyStateSDE() { return maxtSteadyStateSDE_; }
	
	public void setMintSDE(int mint) { mintSDE_ = mint; }
	public int getMintSDE() { return mintSDE_; }
	
	public void setDt(int dt) { dt_ = dt; }
	public int getDt() { return dt_; }
	
	public double getMultifactorialStdev() { return multifactorialStdev_; }
	public void setMultifactorialStdev(double cv) { multifactorialStdev_ = cv; }

	public double getPerturbationProbability() { return perturbationProbability_; }
	public void setPerturbationProbability(double p) { perturbationProbability_ = p; }
	
	public void setLoadPerturbations(boolean b) { loadPerturbations_ = b; }
	public boolean getLoadPerturbations() { return loadPerturbations_; }

	public double getMinGeneDeletionEffect() { return minGeneDeletionEffect_; }
	public void setMinGeneDeletionEffect(double minGeneDeletionEffect) { minGeneDeletionEffect_ = minGeneDeletionEffect; }

	public double getMaxGeneDeletionEffect() { return maxGeneDeletionEffect_; }
	public void setMaxGeneDeletionEffect(double maxGeneDeletionEffect) { maxGeneDeletionEffect_ = maxGeneDeletionEffect; }

	public double getMinGeneOverexpressionEffect() { return minGeneOverexpressionEffect_; }
	public void setMinGeneOverexpressionEffect(double minGeneOverexpressionEffect) { minGeneOverexpressionEffect_ = minGeneOverexpressionEffect; }

	public double getMaxGeneOverexpressionEffect() { return maxGeneOverexpressionEffect_; }
	public void setMaxGeneOverexpressionEffect(double maxGeneOverexpressionEffect) { maxGeneOverexpressionEffect_ = maxGeneOverexpressionEffect; }

	public double getMinFractionDirectTargets() { return minFractionDirectTargets_; }
	public void setMinFractionDirectTargets(double min) { minFractionDirectTargets_ = min; }

	public double getMaxFractionDirectTargets() { return maxFractionDirectTargets_; }
	public void setMaxFractionDirectTargets(double max) { maxFractionDirectTargets_ = max; }

	public void setTimeStepSDE(double dt) { timeStepSDE_ = dt; }
	public double getTimeStepSDE() { return timeStepSDE_; }
	
	public void setNoiseCoefficientSDE(double coeff) { noiseCoefficientSDE_ = coeff; }
	public double getNoiseCoefficientSDE() { return noiseCoefficientSDE_; }
	
	public void setModelTranslation(boolean b) { modelTranslation_ = b; }
	public boolean getModelTranslation() { return modelTranslation_; }
	
	public void setIgnoreAutoregulatoryInteractionsInEvaluation(boolean b) { ignoreAutoregulatoryInteractionsInEvaluation_ = b; }
	public boolean getIgnoreAutoregulatoryInteractionsInEvaluation() { return ignoreAutoregulatoryInteractionsInEvaluation_; }

	public void setAppendZeroInteractionsInGoldStandardFiles(boolean b) { appendZeroInteractionsInGoldStandardFiles_ = b; }
	public boolean getAppendZeroInteractionsInGoldStandardFiles() { return appendZeroInteractionsInGoldStandardFiles_; }

	public void setOutputGenesInRows(boolean b) { outputGenesInRows_ = b; }
	public boolean getOutputGenesInRows() { return outputGenesInRows_; }
	
	public void setSimulateODE(boolean b) { simulateODE_ = b; }
	public boolean getSimulateODE() { return simulateODE_; }
	
	public void setSimulateSDE(boolean b) { simulateSDE_ = b; }
	public boolean getSimulateSDE() { return simulateSDE_; }
	
	public double getRandomHalfLife() { return randomHalfLife_.getRandomValue();	}
	public void setRandomHalfLife(RandomParameter r) {randomHalfLife_ = r; }

	public double getRandomK() { return randomK_.getRandomValue(); }
	public void setRandomK(RandomParameter r) { randomK_ = r; }

	public double getRandomN() { return randomN_.getRandomValue(); }
	public void setRandomN(RandomParameter r) { randomN_ = r; }

	public double getRandomDeltaActivation() { return randomDeltaActivation_.getRandomValue(); }
	public void setRandomDeltaActivation(RandomParameter r) { randomDeltaActivation_ = r; }

	public double getWeakActivation() { return weakActivation_; }
	public void setWeakActivation(double w) { weakActivation_ = w; }

	public double getRandomLowBasalRate() { return randomLowBasalRate_.getRandomValue(); }
	public void setRandomLowBasalRate(RandomParameter r) { randomLowBasalRate_ = r; }

	public double getRandomMediumBasalRate() { return randomMediumBasalRate_.getRandomValue(); }
	public void setRandomMediumBasalRate(RandomParameter r) { randomMediumBasalRate_ = r; }
	
	public void generateSsKnockouts(boolean b) { ssKnockouts_ = b; }
	public boolean generateSsKnockouts() { return ssKnockouts_; }
	
	public void generateSsKnockdowns(boolean b) { ssKnockdowns_ = b; }
	public boolean generateSsKnockdowns() { return ssKnockdowns_; }
	
	public void generateSsMultifactorial(boolean b) { ssMultifactorial_ = b; }
	public boolean generateSsMultifactorial() { return ssMultifactorial_; }
	
	public void generateSsDREAM4TimeSeries(boolean b) { ssDREAM4TimeSeries_ = b; }
	public boolean generateSsDREAM4TimeSeries() { return ssDREAM4TimeSeries_; }
	
	public void generateSsDualKnockouts(boolean b) { ssDualKnockouts_ = b; }
	public boolean generateSsDualKnockouts() { return ssDualKnockouts_; }
	
	public void generateTsKnockouts(boolean b) { tsKnockouts_ = b; }
	public boolean generateTsKnockouts() { return tsKnockouts_; }
	
	public void generateTsKnockdowns(boolean b) { tsKnockdowns_ = b; }
	public boolean generateTsKnockdowns() { return tsKnockdowns_; }
	
	public void generateTsMultifactorial(boolean b) { tsMultifactorial_ = b; }
	public boolean generateTsMultifactorial() { return tsMultifactorial_; }
	
	public void generateTsDREAM4TimeSeries(boolean b) { tsDREAM4TimeSeries_ = b; }
	public boolean generateTsDREAM4TimeSeries() { return tsDREAM4TimeSeries_; }
	
	public void generateTsDualKnockouts(boolean b) { tsDualKnockouts_ = b; }
	public boolean generateTsDualKnockouts() { return tsDualKnockouts_; }
	
	//public void setNumMeasuredPoints(int num) { numMeasuredPoints_ = num; }
	//public int getNumMeasuredPoints() { return numMeasuredPoints_; }
	
	public String getOutputDirectory() { return outputDirectory_; }
	
	public String getGnwVersion() { return gnwVersion_; }
	
	public void setLastSettingsURL(URL url) { lastSettingsURL_ = url; }
	public URL getLastSettingsURL() { return lastSettingsURL_; }
	
	public void stopSubnetExtraction(boolean b) { stopSubnetExtraction_ = b; }
	public boolean stopSubnetExtraction() { return stopSubnetExtraction_; }
	
	public void stopBenchmarkGeneration(boolean b) { stopBenchmarkGeneration_ = b; }
	public boolean stopBenchmarkGeneration() { return stopBenchmarkGeneration_; }
	
	public void setSignature(String signature) { signature_ = signature; }
	public String getSignature() { return signature_; }
}
