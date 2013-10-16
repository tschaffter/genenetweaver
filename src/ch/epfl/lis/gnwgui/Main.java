package ch.epfl.lis.gnwgui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math.ConvergenceException;

import ch.epfl.lis.gnw.BenchmarkGeneratorDream4;
import ch.epfl.lis.gnw.CancelException;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnw.Parser;
import ch.epfl.lis.gnw.SubnetExtractor;
import ch.epfl.lis.gnw.evaluation.PerformanceEvaluator;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.imod.LoggerManager;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.Structure;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.Silver;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

/**
 * Instantiation of the GUI interface of GNW
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 *
 */
public class Main {
	
    /** Packages to set up for logging. TODO: DELETE AND REPLACE WITH LOG4J */
    private static String[] packages2log = {"ch.epfl.lis.gnw", 
    										 "ch.epfl.lis.gnwgui", 
    										 "ch.epfl.lis.gnwgui.filefilters", 
    										 "ch.epfl.lis.gnwgui.idesktop", 
    										 "ch.epfl.lis.gnwgui.jungtransformers", 
    										 "ch.epfl.lis.gnwgui.windows", 
    										 "ch.epfl.lis.imod", 
    										 "ch.epfl.lis.networks", 
    										 "ch.epfl.lis.networks.ios",
    										 "ch.epfl.lis.sde"};
    
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(Main.class.getName());
    

	// ============================================================================
	// MAIN METHOD
    
	public static void main(String[] args) {
		
		try {
			setPackageLoggers();
			parseArguments(args);
			setPlatformPreferences();
			setLookAndFeel();
			
			GnwGui gui = new GnwGui();
			gui.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ---------------------------------------------------------------------------
	
	public static void test()
	{	
		System.out.println("TEST starts...");
	}
	
	// ---------------------------------------------------------------------------
	
	public static JSAP buildArgumentParser() throws JSAPException, Exception
	{
		JSAP jsap = new JSAP();
		
        FlaggedOption gnwSettings = new FlaggedOption("gnwSettings")
        							.setStringParser(JSAP.STRING_PARSER)
//        							.setDefault("")
        							.setRequired(false)
        							.setShortFlag('c') 
        							.setLongFlag("config-file");
        gnwSettings.setHelp("Specifies settings file.");
        jsap.registerParameter(gnwSettings);
        
        // for some reason, long flags don't work in jnlp file -> to investigate
        FlaggedOption gnwServerSettings = new FlaggedOption("gnwServerSettings")
									.setStringParser(JSAP.STRING_PARSER)
									.setRequired(false)
									.setShortFlag('z');
        gnwServerSettings.setHelp("Specifies server settings file.");
        jsap.registerParameter(gnwServerSettings);
        
        Switch extract = new Switch("extract")
									.setShortFlag('e') 
									.setLongFlag("extract");
        extract.setHelp("Extracts subnetworks.");
        jsap.registerParameter(extract);
        
        Switch simulate = new Switch("simulate")
									.setShortFlag('s') 
									.setLongFlag("simulate");
        simulate.setHelp("Simulates dynamical networks.");
        jsap.registerParameter(simulate);
        
        Switch transform = new Switch("transform")
        							.setShortFlag('t') 
									.setLongFlag("transform");
        transform.setHelp("Convert a network from one type to another.");
		jsap.registerParameter(transform);
		
        Switch evaluate = new Switch("evaluate")
									.setLongFlag("evaluate");
        evaluate.setHelp("Evaluate the performance of a network prediction.");
		jsap.registerParameter(evaluate);
        
        FlaggedOption inputNetwork = new FlaggedOption("inputNetwork")
									.setStringParser(JSAP.STRING_PARSER)
									.setRequired(false)
									.setLongFlag("input-net");
        inputNetwork.setHelp("Loads structure or dynamical network.");
		jsap.registerParameter(inputNetwork);
		
        FlaggedOption networkName = new FlaggedOption("networkName")
									.setStringParser(JSAP.STRING_PARSER)
									.setRequired(false)
									.setLongFlag("network-name");
        networkName.setHelp("Specifies the subnetworks root name.");
		jsap.registerParameter(networkName);
		
        FlaggedOption inputNetworkFormat = new FlaggedOption("inputNetworkFormat")
									.setStringParser(JSAP.INTEGER_PARSER)
									.setRequired(false)
									.setLongFlag("input-net-format");
        inputNetworkFormat.setHelp("Specifies the format of the network to load (0=TSV, 1=GML, 2=DOT 3=TSV DREAM, 4=SBML).");
		jsap.registerParameter(inputNetworkFormat);
		
        Switch extractAllRegulators = new Switch("extractAllRegulators")
									.setLongFlag("extract-all-regulators");
        extractAllRegulators.setHelp("Extracts all the regulators of the network.");
		jsap.registerParameter(extractAllRegulators);
		
        FlaggedOption subnetworkSize = new FlaggedOption("subnetworkSize")
									.setStringParser(JSAP.INTEGER_PARSER)
									.setDefault("10")
									.setRequired(false)
									.setLongFlag("subnet-size");
        subnetworkSize.setHelp("Size of the subnetworks extracted.");
        jsap.registerParameter(subnetworkSize);
        
        FlaggedOption numSubnetworks = new FlaggedOption("numSubnetworks")
										.setStringParser(JSAP.INTEGER_PARSER)
										.setDefault("10")
										.setRequired(false)
										.setLongFlag("num-subnets");
        numSubnetworks.setHelp("Number of subnetworks to extracte.");
		jsap.registerParameter(numSubnetworks);
        
        FlaggedOption minNumRegulators = new FlaggedOption("minNumRegulators")
									.setStringParser(JSAP.INTEGER_PARSER)
									.setDefault("10")
									.setRequired(false)
									.setLongFlag("min-num-regulators");
        minNumRegulators.setHelp("Select to specify the minimum number of regulators.");
		jsap.registerParameter(minNumRegulators);
		
        Switch randomSeed = new Switch("randomSeed")
									.setLongFlag("random-seed");
        randomSeed.setHelp("Uses random seed to extract subnetworks.");
		jsap.registerParameter(randomSeed);
		
        FlaggedOption seed = new FlaggedOption("seed")
									.setStringParser(JSAP.STRING_PARSER)
									.setRequired(false)
									.setLongFlag("seed");
		seed.setHelp("Specifies the node name to be used as seed.");
		jsap.registerParameter(seed);
		
		FlaggedOption sccSeed = new FlaggedOption("sccSeed")
									.setStringParser(JSAP.INTEGER_PARSER)
									.setDefault("10")
									.setRequired(false)
									.setLongFlag("scc-seed");
		sccSeed.setHelp("Adds the specified number of nodes from the largest strongly connected component of the graph as seed.");
		jsap.registerParameter(sccSeed);
		
        Switch greedySelection = new Switch("greedySelection")
									.setLongFlag("greedy-selection");
        greedySelection.setHelp("Greedy selection.");
		jsap.registerParameter(greedySelection);
		
		FlaggedOption ratSelection = new FlaggedOption("ratSelection")
									.setStringParser(JSAP.INTEGER_PARSER)
									.setDefault("20")
									.setRequired(false)
									.setLongFlag("rat-selection");
		ratSelection.setHelp("Specifies random among top selection (%).");
		jsap.registerParameter(ratSelection);
		
		FlaggedOption outputNetworkFormat = new FlaggedOption("outputNetworkFormat")
									.setStringParser(JSAP.INTEGER_PARSER)
									.setRequired(false)
									.setLongFlag("output-net-format");
		outputNetworkFormat.setHelp("Specifies the format of the network(s) to save (0=TSV, 1=GML, 2=DOT 3=TSV DREAM, 4=SBML).");
		jsap.registerParameter(outputNetworkFormat);
		
        FlaggedOption outputPath = new FlaggedOption("outputPath")
									.setStringParser(JSAP.STRING_PARSER)
									.setDefault(".")
									.setRequired(false)
									.setLongFlag("output-path");
        outputPath.setHelp("Specifies the path where files must be saved (without final slash).");
        jsap.registerParameter(outputPath);
        
        Switch keepSelfInteractions = new Switch("keepSelfInteractions")
									.setLongFlag("keep-self-interactions");
        keepSelfInteractions.setHelp("Keeps autoregulatory interactions when dynamical networks are generated.");
		jsap.registerParameter(keepSelfInteractions);
		
//        Switch auroc = new Switch("auroc")
//									.setLongFlag("auroc");
//        auroc.setHelp("Area Under the Receiver Operating Characteristic Curve.");
//		jsap.registerParameter(auroc);
//		
//        Switch aupr = new Switch("aupr")
//									.setLongFlag("aupr");
//		aupr.setHelp("Area Under the Precision and Recall Curve.");
//		jsap.registerParameter(aupr);
		
        FlaggedOption goldstandard = new FlaggedOption("goldstandard")
									.setStringParser(JSAP.STRING_PARSER)
									.setRequired(false)
									.setLongFlag("goldstandard");
        goldstandard.setHelp("Specifies the goldstandard network (source network).");
		jsap.registerParameter(goldstandard);
		
		FlaggedOption prediction = new FlaggedOption("prediction")
									.setStringParser(JSAP.STRING_PARSER)
									.setRequired(false)
									.setLongFlag("prediction");
		prediction.setHelp("Specifies the network prediction.");
		jsap.registerParameter(prediction);
		
		
        
        Switch useNativeLaf = new Switch("useNativeLaf") 
									.setLongFlag("native-laf");
        useNativeLaf.setHelp("Uses Swing native look & feel.");
        jsap.registerParameter(useNativeLaf);
        
        Switch test = new Switch("test")
									.setLongFlag("test");
        test.setHelp("Tests the implementation of GNW.");
        jsap.registerParameter(test);
        
        return jsap;
	}
	
	// ---------------------------------------------------------------------------
	
	public static void displayUsage(JSAP jsap)
	{
		GnwSettings uni = GnwSettings.getInstance();
		
    	log_.log(Level.INFO, "");
    	log_.log(Level.INFO, "GeneNetWeaver (GNW) is a tool for the automatic generation of in silico gene networks and reverse engineering benchmarks.");
    	log_.log(Level.INFO, "Version: " + uni.getGnwVersion());
    	log_.log(Level.INFO, "Authors: Thomas Schaffter (thomas.schaff..@gmail.com), Daniel Marbach (daniel.marb...@gmail.com), Gilles Roulet (gilles.rou...@gmail.com)");
    	log_.log(Level.INFO, "Web page: http://lis.epfl.ch/gnw");
    	log_.log(Level.INFO, "");
    	log_.log(Level.INFO, "Usage: java -jar gnw.jar " + jsap.getUsage());
    	log_.log(Level.INFO, "");
    	log_.log(Level.INFO, "Examples:");
    	log_.log(Level.INFO, "  Extraction: java -jar gnw-standalone.jar --extract -c settings.txt --input-net ecoli_transcriptional_network_regulonDB_6_7.tsv --random-seed --greedy-selection --subnet-size=50 --num-subnets=10 --output-net-format=4 --keep-self-interactions --output-path .");
    	log_.log(Level.INFO, "  Simulation: java -jar gnw-standalone.jar --simulate -c settings.txt --input-net InSilicoSize10-Yeast1.xml --output-path /home/thomas/devel/gnw-3_0/datasets");
    	log_.log(Level.INFO, "  Evaluation: java -jar gnw-standalone.jar --evaluate --goldstandard InSilicoSize10-Yeast1.xml --prediction InSilicoSize10-Yeast1-prediction.tsv");
    	log_.log(Level.INFO, "  Conversion (here TSV to DOT format): java -jar gnw-standalone.jar --transform -c settings.txt --input-net ecoli_transcriptional_network_regulonDB_6_7.tsv --output-net-format=2");
    	log_.log(Level.INFO, "");
    	log_.log(Level.INFO, jsap.getHelp());	// show full help as well
	}
	
	// ---------------------------------------------------------------------------
	
	public static boolean useCommandLineInterface(JSAP jsap, JSAPResult settings) throws Exception
	{
		int count = 0;
		
		if (settings.getBoolean("extract")) count++;
		if (settings.getBoolean("simulate")) count++;
		if (settings.getBoolean("evaluate")) count++;
		if (settings.getBoolean("transform")) count++;
		
		if (count > 1)
		{
			log_.log(Level.INFO, "Only one of the following options must be specified:");
			log_.log(Level.INFO, "                " + jsap.getByID("extract").getSyntax());
			log_.log(Level.INFO, "                " + jsap.getByID("simulate").getSyntax());
			log_.log(Level.INFO, "                " + jsap.getByID("evaluate").getSyntax());
			log_.log(Level.INFO, "                " + jsap.getByID("transform").getSyntax());
			
			System.exit(1);
		}

		return (count == 1);
	}
	
	// ---------------------------------------------------------------------------
	
	public static NetworkElement loadNetwork(JSAP jsap, JSAPResult settings, String absPath, Integer format) throws Exception
	{
		NetworkElement element = null; // required, will contain the network
		String name = null; // optional
		
		if (settings.userSpecified("networkName")) {
			name = settings.getString("networkName");
		}
		else if ( settings.getBoolean("evaluate") ) {
			name = FilenameUtilities.getFilenameWithoutExtension(settings.getString("goldstandard"));
		}
		else {
			name = FilenameUtilities.getFilenameWithoutPath(settings.getString("inputNetwork"));
			name = FilenameUtilities.getFilenameWithoutExtension(name);
		}
		
		URL url = GnwSettings.getInstance().getURL(absPath);
		element = IONetwork.loadItem(name, url, format);
		
		return element;
	}
	
	// ---------------------------------------------------------------------------
	
	public static void transformNetwork(JSAP jsap, JSAPResult settings, NetworkElement element) throws 	MalformedURLException,
																											IOException,
																											Exception
	{
		if (!settings.userSpecified("outputNetworkFormat"))
		{
			log_.log(Level.INFO, jsap.getByID("outputNetworkFormat").getSyntax() + " required!");
			System.exit(1);
		}
		
		ImodNetwork inetwork = null;
		GeneNetwork gnetwork = null;
		String id = null;
		URL url = null;
		
		if (element instanceof StructureElement)
		{
			inetwork = ((StructureElement)element).getNetwork();
			id = inetwork.getId();
		}
		else if (element instanceof DynamicalModelElement)
		{
			gnetwork = ((DynamicalModelElement)element).getGeneNetwork();
			id = gnetwork.getId();
		}
		
		
		if (settings.getInt("outputNetworkFormat") == ImodNetwork.TSV)
		{
			url = GnwSettings.getInstance().getURL(settings.getString("outputPath") + "/" + id + ".tsv");
			
			if (inetwork != null) inetwork.saveTSV(url);
			else if (gnetwork != null) gnetwork.saveTSV(url);
		}
		else if (settings.getInt("outputNetworkFormat") == 3) // TSV with null interactions written
		{
			url = GnwSettings.getInstance().getURL(settings.getString("outputPath") + "/" + id + ".tsv");
			Parser parser = null;
			
			if (inetwork != null) parser = new Parser(inetwork, url);
			else if (gnetwork != null) parser = new Parser(gnetwork, url);
			
			parser.writeGoldStandard();
		 }
		 else if (settings.getInt("outputNetworkFormat") == ImodNetwork.GML)
		 {
			 log_.log(Level.INFO, "Writing file " + settings.getString("outputPath") + "/" + id + ".gml");
			 
			 url = GnwSettings.getInstance().getURL(settings.getString("outputPath") + "/" + id + ".gml");
			 
			 if (inetwork != null) inetwork.saveGML(url);
			 else if (gnetwork != null) gnetwork.saveGML(url);
		 }
		 else if (settings.getInt("outputNetworkFormat") == ImodNetwork.DOT)
		 {
			 log_.log(Level.INFO, "Writing file " + settings.getString("outputPath") + "/" + id + ".dot");
			 
			 url = GnwSettings.getInstance().getURL(settings.getString("outputPath") + "/" + id + ".dot");
			 
			 if (inetwork != null) inetwork.saveDOT(url);
			 else if (gnetwork != null) gnetwork.saveDOT(url);
		 }	 
		 else if (settings.getInt("outputNetworkFormat") == GeneNetwork.SBML)
		 {
			 if (inetwork != null)
			 {
				 // set dynamical model
				 
  				if (!settings.userSpecified("keepSelfInteractions"))
  				{
  					log_.log(Level.INFO, "Removing autoregulatory interactions from " + id);
  					inetwork.removeAutoregulatoryInteractions();
  				}
  				
  				log_.log(Level.INFO, "Setting dynamical model for " + id);
  				GeneNetwork grn = new GeneNetwork(inetwork);
  				grn.randomInitialization();
  				
  				// If we are here it means that the networks must be saved to SBML files
  				url = GnwSettings.getInstance().getURL(settings.getString("outputPath") + "/" + id + ".xml");
  				grn.writeSBML(url);
			 }
			 else
			 {
				 log_.log(Level.INFO, "Network provided is already a dynamical network. Nothing to do!");
				 System.exit(0);
			 }
		 } 
	}
	
	// ---------------------------------------------------------------------------
	
	public static void extractNetworks(JSAP jsap, JSAPResult settings, NetworkElement element) throws Exception
	{
		int count = 0;
		if (settings.userSpecified("randomSeed")) count++;
		if (settings.userSpecified("seed")) count++;
		if (settings.userSpecified("sccSeed")) count++;
		
		if (count != 1)
		{
			if (count > 1)
				log_.log(Level.INFO, "Only one of the following options must be specified:");
			else if (count < 1)
				log_.log(Level.INFO, "One of the following options must be specified:");
			
			log_.log(Level.INFO, "                " + jsap.getByID("randomSeed").getSyntax());
			log_.log(Level.INFO, "                " + jsap.getByID("seed").getSyntax());
			log_.log(Level.INFO, "                " + jsap.getByID("sccSeed").getSyntax());
			
			System.exit(1);
		}
		
		count = 0;
		if (settings.userSpecified("greedySelection")) count++;
		if (settings.userSpecified("ratSelection")) count++;
		
		if (count != 1)
		{
			if (count > 1)
				log_.log(Level.INFO, "Only one of the following options must be used:");
			else if (count < 1)
				log_.log(Level.INFO, "One of the following options must be used:");
			
			log_.log(Level.INFO, "                " + jsap.getByID("greedySelection").getSyntax());
			log_.log(Level.INFO, "                " + jsap.getByID("ratSelection").getSyntax());
			
			System.exit(1);
		}
	
		GnwSettings uni = GnwSettings.getInstance();
		ImodNetwork structure = null;
		
		// Load network 
		if (element instanceof StructureElement)
			structure = ((StructureElement)element).getNetwork();
		else if (element instanceof DynamicalModelElement)
			structure = ((DynamicalModelElement)element).getGeneNetwork();
		
		// Start by building the seed(s)
		ArrayList<ArrayList<Node>> seeds = null;
		ArrayList<Node> miniSeed = null;
			
		// DANIEL
		uni.setNumSeedsFromStronglyConnectedComponents(0);
		
		int subnetSize = settings.getInt("subnetworkSize");
		int poolSize = settings.getInt("numSubnetworks");
		
		if (settings.userSpecified("randomSeed"))
		{
			// do nothing, already implemented downstream
		}
		else if (settings.userSpecified("seed")) // Seed specified
		{
			String seedName = settings.getString("seed");
			seeds = new ArrayList<ArrayList<Node>>();
			miniSeed = new ArrayList<Node>();
			
			// check that the provided seed is valid
			if (!structure.containsNode(seedName))
				throw new Exception("Invalid seed \"" + seedName + "\" (not included in the network)!");
			
			miniSeed.add(structure.getNode(seedName));
			for (int i = 0; i < poolSize; i++)
				seeds.add(miniSeed);
		}
		else if (settings.userSpecified("sccSeed"))
		{
			int numStronglyConnected = settings.getInt("sccSeed");
			uni.setNumSeedsFromStronglyConnectedComponents(numStronglyConnected);
		}
				
		if (settings.userSpecified("minNumRegulators"))
		{
			int numRegulators = settings.getInt("minNumRegulators");
			uni.setNumRegulators(numRegulators);
		}
		
		// Set now the selection option
		if (settings.userSpecified("greedySelection"))
			uni.setTruncatedSelectionFraction(0);
		else if (settings.userSpecified("ratSelection"))
		{
			double value = (settings.getInt("ratSelection")) / 100.;
			uni.setTruncatedSelectionFraction(value);
		}
		
		try
		{
			// BEGIN EXTRACTION
			String name = structure.getId();

			if (structure.getSize() == subnetSize)
			{
				log_.log(Level.INFO, "Subnetwork size must be smaller than original network size!");
				System.exit(1);
			}
	 		 
	 		SubnetExtractor extractor_ = new SubnetExtractor(structure);
	 		Structure[] output_ = null;
	 		 
	 		if (settings.userSpecified("extractAllRegulators"))
	 		{
	 			output_ = new Structure[1];
			 	output_[0] = extractor_.extractRegulators();
	 		}
	 		else if (seeds == null) // random seed
	 			output_ = extractor_.runExtraction(name, subnetSize, poolSize);
	 		else
	 			output_ = extractor_.runExtraction(name, subnetSize, seeds);
 		
 		
	 		// SAVE NETWORKS
	 		if (output_ != null)
	 		{
	 			for (int i = 0; i < output_.length; i++)
	 			{
	 				NetworkElement e = new StructureElement(name);
 					((StructureElement)e).setNetwork(new ImodNetwork(output_[i]));
 					
 					transformNetwork(jsap, settings, e);
	 			}
	 		}
	 		else
	 			throw new Exception("Error occured during subnetworks extraction!");
		}
    	 catch (OutOfMemoryError e)
    	 {
    		 throw new Exception("There is not enough memory available to run this program.\n" +
				"Quit one or more programs, and then try again.\n" +
				"If enough amounts of RAM are installed on this computer, try to run the program " +
				"with the command-line argument -Xmx1024m to use maximum 1024Mb of memory, " +
				"-Xmx2048m to use max 2048Mb, etc.");
		}
	}
	
	// ---------------------------------------------------------------------------
	
	public static void simulateNetwork(JSAP jsap, JSAPResult settings, NetworkElement element)
	{
		if ( !(element instanceof DynamicalModelElement) )
		{
			log_.log(Level.INFO, "Network provided is not a dynamical network. Nothing to simulate!");
			System.exit(1);
		}
		
		GnwSettings uni = GnwSettings.getInstance();
		uni.stopBenchmarkGeneration(false); // reset
		
		log_.log(Level.WARNING, "WARNING: ABSOLUTE PATH is required for simulation output directory!");
		
		GeneNetwork grn = ((DynamicalModelElement)element).getGeneNetwork();
		BenchmarkGeneratorDream4 benchmarkGenerator_ = new BenchmarkGeneratorDream4(grn);
		
		try
		{
			benchmarkGenerator_.generateBenchmark();
			log_.log(Level.INFO, "Done!");
		}
		catch (OutOfMemoryError e)
		{
			log_.log(Level.SEVERE, "There is not enough memory available to run this program.\n" +
					"Quit one or more programs, and then try again.\n" +
					"If enough amounts of RAM are installed on this computer, try to run the program " +
					"with the command-line argument -Xmx1024m to use maximum 1024Mb of memory, " +
					"-Xmx2048m to use max 2048Mb, etc.", e);
		}
		catch (IllegalArgumentException e)
		{
			log_.log(Level.SEVERE, "Main::simulateNetwork(): " + e.getMessage(), e);
		}
		catch (CancelException e)
		{
			log_.log(Level.SEVERE, "Main::simulateNetwork(): " + e.getMessage(), e);
		}
		catch (ConvergenceException e)
		{
			log_.log(Level.SEVERE, "Main::simulateNetwork(): " + e.getMessage(), e);
		}
		catch (RuntimeException e)
		{
			log_.log(Level.SEVERE, "Main::simulateNetwork(): " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			log_.log(Level.SEVERE, "Main::simulateNetwork(): " + e.getMessage(), e);
		}
	}
	
	// ---------------------------------------------------------------------------
	
	public static void evaluatePrediction(JSAP jsap, JSAPResult settings, NetworkElement goldstandard, String prediction)
	{	
//		if (  !settings.getBoolean("auroc") && ! settings.getBoolean("aupr")) {
//			displayUsage(jsap);
//			return;
//		}
		ImodNetwork goldStandard = null;
		if ( goldstandard.getClass() == DynamicalModelElement.class)
			goldStandard = ((DynamicalModelElement)goldstandard).getGeneNetwork();
		if ( goldstandard.getClass() == StructureElement.class)
			goldStandard = ((StructureElement)goldstandard).getNetwork();
		
		PerformanceEvaluator pe = new PerformanceEvaluator(goldStandard, false, true, false, true, false, false);
		
			try {
				pe.loadPrediction((new File(prediction)).toURI().toURL(), false);
			} catch (MalformedURLException e) {
				log_.log(Level.WARNING,"Unable to get URL for the prediction file!");
				e.printStackTrace();
				return;
			}
		pe.run();
		pe.getScore().run();
		
//		if (settings.userSpecified("aupr"))
//		{
//			double aupr = pe.getScore().getAUPR();
//			log_.log(Level.INFO, "AUPR = " + aupr);
//		}
//		
//		if (settings.userSpecified("auroc"))
//		{
//			double auroc = pe.getScore().getAUROC();
//			log_.log(Level.INFO, "AUROC = " + auroc);
//		}
	}
	
	// ---------------------------------------------------------------------------
	
	/**
	 * This function defines which are the command line arguments that can be
	 * specified. Make use of the JSAP library.
	 * 
	 * JSAP - Java Simple Argument Parser
	 * http://martiansoftware.com/jsap/doc/
	 */
	public static void parseArguments(String args[]) throws Exception
	{
		GnwSettings uni = GnwSettings.getInstance();
		
		try
		{
			JSAP jsap = buildArgumentParser();
			JSAPResult settings = jsap.parse(args);
		
	        if (!settings.success())
	        {
	        	displayUsage(jsap);
	        	System.exit(1);
	        }
	        
	        boolean settingsLoaded = false;
	        
	        if (settings.userSpecified("gnwSettings"))
	        {
	        	log_.log(Level.INFO, "Loading specified settings: " + settings.getString("gnwSettings"));
	        	settingsLoaded = (settingsLoaded || uni.loadSettings(settings.getString("gnwSettings")));
	        }
	        
	        if (uni.personalGnwSettingsExist() && !settingsLoaded)
	        {
	        	log_.log(Level.INFO, "Loading found local settings: " + uni.personalGnwSettingsPath());
	        	settingsLoaded = (settingsLoaded || uni.loadSettings(uni.personalGnwSettingsPath()));
	        }
	        
	        if (!settingsLoaded)
	        {
	        	//log_.log(Level.INFO, "Loading server settings: " + settings.getString("gnwServerSettings"));
	        	log_.log(Level.INFO, "Loading default settings file");
	        	settingsLoaded = (settingsLoaded || uni.loadSettings(""/*settings.getString("gnwServerSettings")*/));
	        }

	        // should never arrive here
	        if (!settingsLoaded)
	        {
	        	log_.log(Level.INFO, "GNW requires a settings file to run.");
				log_.log(Level.INFO, uni.getSignature());
				System.exit(0);
	        }
	        
	        
	        if (useCommandLineInterface(jsap, settings))
	        {
				// Here means that we are in command-line mode
				uni.setOutputDirectory(settings.getString("outputPath"));
				
				String inputNetwork = null;
				String goldstandard = null;
				String prediction = null;
				Integer format = null;
				
				if (settings.getBoolean("extract") || settings.getBoolean("simulate") || settings.getBoolean("transform"))
				{
					if (!settings.userSpecified("inputNetwork"))
					{
						log_.log(Level.INFO, jsap.getByID("inputNetwork").getSyntax() + " required!");
						System.exit(1);
					}
					inputNetwork = settings.getString("inputNetwork");
				}
				else if (settings.getBoolean("evaluate"))
				{
					if (!settings.userSpecified("goldstandard"))
					{
						log_.log(Level.INFO, jsap.getByID("goldstandard").getSyntax() + " required!");
						System.exit(1);
					}
					
					if (!settings.userSpecified("prediction"))
					{
						log_.log(Level.INFO, jsap.getByID("prediction").getSyntax() + " required!");
						System.exit(1);
					}
					
					inputNetwork = settings.getString("goldstandard"); 
					
					goldstandard = settings.getString("goldstandard"); // absolute path to goldstandard
					prediction = settings.getString("prediction"); // absolute path to prediction file
				}
				
				if (settings.userSpecified("inputNetworkFormat"))
					format = settings.getInt("inputNetworkFormat");
				
				if (settings.getBoolean("extract"))
				{
					NetworkElement element = loadNetwork(jsap, settings, inputNetwork, format);
					extractNetworks(jsap, settings, element);
				}
				else if (settings.getBoolean("simulate"))
				{
					NetworkElement element = loadNetwork(jsap, settings, inputNetwork, format);
					simulateNetwork(jsap, settings, element);
				}
				else if (settings.getBoolean("transform"))
				{
					NetworkElement element = loadNetwork(jsap, settings, inputNetwork, format);
					transformNetwork(jsap, settings, element);
				}
				else if (settings.getBoolean("evaluate"))
				{
					NetworkElement element1 = loadNetwork(jsap, settings, goldstandard, format);
					//GenericElement element2 = loadNetwork(jsap, settings, prediction, format);
					evaluatePrediction(jsap, settings, element1, prediction);
				}
				
				log_.log(Level.INFO, uni.getSignature());
				System.exit(0);
	        }
			   	
	        if (settings.userSpecified("useNativeLaf"))
	        	GnwGuiSettings.getInstance().useSwingNativeLookAndFeel(settings.getBoolean("useNativeLaf"));
	        if (settings.userSpecified("test"))
	        {
	        	test();
	        	System.exit(0);
	        }
		}
		catch (Exception e)
		{
			log_.log(Level.SEVERE, "Main::parseArguments(): " + e.getMessage(), e);
			
			System.exit(1);
		}
	}

	// ---------------------------------------------------------------------------
	
	/**
	 * Specify which package are listened for exceptions
	 * TODO: replace JUL by log4j
	 */
	public static void setPackageLoggers()
	{
		// TODO: put the file outside the jar
		InputStream stream = GnwGui.class.getResourceAsStream("gnwguiLogSettings.txt");
		LoggerManager manager = new LoggerManager(stream); // load and apply the log settings files
		for (int i=0; i < packages2log.length; i++)
			manager.setLoggerPolicyForPackage(packages2log[i]);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set preferences according to the running OS.
	 */
	public static void setPlatformPreferences() {
		
		if (GnwGuiSettings.getInstance().isMac()) {
			// combine application menu bar with Mac OS menu bar
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GeneNetWeaver");
		} else if (GnwGuiSettings.getInstance().isWin()) {}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the application look&feel
	 */
	public static void setLookAndFeel() {
		
		boolean defautlLAF = GnwGuiSettings.getInstance().useSwingNativeLookAndFeel();
		
		try {
			if (defautlLAF) {
				// use the native look and feel
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			else {
				PlasticLookAndFeel.setPlasticTheme(new Silver());
				UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
				PlasticXPLookAndFeel lookXP = new PlasticXPLookAndFeel();
				UIManager.setLookAndFeel(lookXP);
			}
		} catch (ClassNotFoundException e) {
			log_.log(Level.WARNING, "Java Look&Feel: class not found\n" + e.getMessage(), e);
		} catch (InstantiationException e) {
			log_.log(Level.WARNING, "Java Look&Fell: unable to instantiate\n: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			log_.log(Level.WARNING, "Java Look&Feel: illegal access\n" + e.getMessage(), e);
		} catch (UnsupportedLookAndFeelException e) {
			log_.log(Level.WARNING, "Java Look&Feel: unsupported look&feel\n" + e.getMessage(), e);
		} catch (Exception e) {
			log_.log(Level.WARNING, "Java Look&Fell: exception\n" + e.getMessage(), e);
		}
	}
}
