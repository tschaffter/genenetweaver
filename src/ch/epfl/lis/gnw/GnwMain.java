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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

import ch.epfl.lis.gnwgui.GnwGui;
import ch.epfl.lis.imod.LoggerManager;
import ch.epfl.lis.networks.Structure;


/** 
 * GNW command-line interface
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class GnwMain {

	/** Java simple argument parser */
	JSAP jsap_ = null;
	/** The parsed command line arguments */
	JSAPResult jsapResult_ = null;
	
    /** Packages to set up for logging. TODO: DELETE AND REPLACE WITH LOG4J */
    private static String[] packages2log = {"ch.epfl.lis.gnw", 
    										 "ch.epfl.lis.imod", 
    										 "ch.epfl.lis.networks", 
    										 "ch.epfl.lis.networks.ios",
    										 "ch.epfl.lis.sde"};
	
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(GnwMain.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Constructor 
	 */
	public GnwMain(String args[]) {
		
		setPackageLoggers();
		defineCommandLineArguments();
		parseCommandLineArguments(args);
	}

	
	// ----------------------------------------------------------------------------

	/** 
	 * Run all experiments, save the gold standards and the datasets.
	 */
	public static void main(String[] args) {
		
		try
		{
			GnwMain gnw = new GnwMain(args);
			gnw.run();
		}
		catch (Exception e)
		{
			log_.log(Level.SEVERE, "Simulation::run(): " + e.getMessage(), e);
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/** Print the error message, the stack trace of the exception, and exit */
//	public static void error(Exception e, String msg) {
//		
//		System.err.println("EXCEPTION: " + msg);
//		//if (!e.getMessage().equals(msg))
//			//System.err.println(e.getMessage());
//		error(e);
//	}

	
	// ----------------------------------------------------------------------------

	/** Print the stack trace of the exception and exit */
//	public static void error(Exception e) {
//		e.printStackTrace();
//		System.exit(-1);
//	}

	
	// ---------------------------------------------------------------------------
	
	/**
	 * Run GNW
	 */
	public void run() {
				
		if (jsapResult_.getBoolean("help"))
			printUsage();
		else if (jsapResult_.getString("regulondbFile") != null)
			parseRegulonDB();
		else
			dream5();
			
		log_.info("");
		log_.info("Done!");
	}

	
	// ---------------------------------------------------------------------------
	
	private void dream5() {
		
		//Compendium.removeRepeatsFromExperimentDescriptions("dream5/saureus/saureus_experiments.tsv");
		
		BenchmarkGeneratorDream5 generator = new BenchmarkGeneratorDream5();
		try {
			generator.generateBenchmark();
		} catch (Exception e) {
			log_.log(Level.SEVERE, "GnwMain::dream5(): " + e.getMessage(), e);
		}
	}

		
	// ---------------------------------------------------------------------------
	
	private void parseRegulonDB() {
				
		String filename = jsapResult_.getString("regulondbFile");
		String outputfile = "regulondb_parsed.tsv";
		log_.info("Reading file " + filename + " ...");
		
		Parser parser = new Parser(new Structure());
		parser.readRegulonDB(filename);
		
		log_.info("Writing file " + outputfile + " ...");
		try {
			parser.setAbsPath(GnwSettings.getInstance().getURL(outputfile));
			parser.write();
		}
		catch (Exception e) {
			log_.log(Level.SEVERE, "Could not write network", e);
		}
	}


	// ============================================================================
	// PRIVATE METHODS

	/** Define the command line arguments using Java Simple Argument Parser */
	private void defineCommandLineArguments() {
		
		jsap_ = new JSAP();
		
		// Help
        Switch help = new Switch("help");
        help.setLongFlag("help");
        help.setShortFlag('h');
        help.setHelp("Display usage");

		// Settings file argument
        FlaggedOption settingsFile = new FlaggedOption("settingsFile");
        settingsFile.setLongFlag("settings");
        settingsFile.setShortFlag('s'); 
		settingsFile.setHelp("Specify the settings file");
        settingsFile.setUsageName("file");
        
		// Parse the given regulonDB file
        FlaggedOption regulondbFile = new FlaggedOption("regulondbFile");
        regulondbFile.setLongFlag("regulondb");
        regulondbFile.setHelp("Specify a regulonDB file to be parsed (see comments for TSVParserGNW.readRegulonDB())");
        regulondbFile.setUsageName("file");
		
        try
        {
			jsap_.registerParameter(help);
			jsap_.registerParameter(settingsFile);
			jsap_.registerParameter(regulondbFile);
			
		} catch (JSAPException e)
		{
			log_.log(Level.SEVERE, "Could not define command line arguments", e);
		}
	}
	
	
	// ---------------------------------------------------------------------------

	private void parseCommandLineArguments(String args[]) {
		
		GnwSettings set = GnwSettings.getInstance();

		// parse the command line
		jsapResult_ = jsap_.parse(args);

		if (!jsapResult_.success()) {
			printUsage();
			System.exit(1);
		}

		set.loadSettings(jsapResult_.getString("settingsFile"));
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** Print usage */
	private void printUsage() {
		
		System.out.println();
        System.out.println("Usage: java -jar gnw.jar");
        System.out.println("                "
                            + jsap_.getUsage());
        System.out.println();
	}

	
	// ---------------------------------------------------------------------------
	
	/**
	 * TODO I copied this from gnwgui.Main. These loggers seem exceedingly complicated, should we
	 * get rid of them?
	 */
	private void setPackageLoggers() {
		
		// TODO: put the file outside the jar
		InputStream stream = GnwGui.class.getResourceAsStream("gnwguiLogSettings.txt");
		LoggerManager manager = new LoggerManager(stream); // load and apply the log settings files
		for (int i=0; i < packages2log.length; i++)
			manager.setLoggerPolicyForPackage(packages2log[i]);
	}
}
