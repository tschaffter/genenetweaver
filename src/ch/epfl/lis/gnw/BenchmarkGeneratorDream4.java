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

import java.util.ArrayList;
import java.util.logging.Level;
import java.io.*;
import java.net.URL;

import cern.colt.matrix.DoubleMatrix1D;


/** Class used to generate the DREAM3 and DREAM4 in silico challenges.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class BenchmarkGeneratorDream4 extends BenchmarkGenerator
{
	/** The steady-state experiments */
	ArrayList<SteadyStateExperiment> steadyStateExperiments_;
	/** If both GnwSettings.simulateODE_ and simulateSDE_ are set, these are the simulations using ODEs */
	ArrayList<SteadyStateExperiment> steadyStateExperimentsODE_;
	/** The time-series experiments */
	ArrayList<TimeSeriesExperiment> timeSeriesExperiments_;
	/** If both GnwSettings.simulateODE_ and simulateSDE_ are set, these are the simulations using ODEs */
	ArrayList<TimeSeriesExperiment> timeSeriesExperimentsODE_;
	/** The perturbations applied to the time-series */
	Perturbation timeSeriesPerturbations_;	
	
	// ============================================================================
	// PUBLIC METHODS
	
	public BenchmarkGeneratorDream4(GeneNetwork grn)
	{	
		super(grn);
		steadyStateExperiments_ = new ArrayList<SteadyStateExperiment>();
		steadyStateExperimentsODE_ = new ArrayList<SteadyStateExperiment>();
		timeSeriesExperiments_ = new ArrayList<TimeSeriesExperiment>();
		timeSeriesExperimentsODE_ = new ArrayList<TimeSeriesExperiment>();
		wildTypeODE_ = null;
		timeSeriesPerturbations_ = null;
		outputDirectory_ = "";
	}
	
	// ----------------------------------------------------------------------------

	/** 
	 * Run all experiments, save the gold standards and the datasets.
	 */
	public void generateBenchmark() throws IllegalArgumentException, CancelException, Exception
	{	
		//GraphUtilities util = new GraphUtilities(grn_);
		//util.anonymizeGenes();
		
		log_.log(Level.INFO, "\nStarting benchmark generation ...\n");
		
		checkForInterruption();
		
		// save DREAM gold standard
		String filename = outputDirectory_ + grn_.getId() + "_goldstandard.tsv";		
		URL url = new File(filename).toURI().toURL();
		Parser parser = new Parser(grn_, url);
		parser.writeGoldStandard();
		
		checkForInterruption();
		
		// save signed network
		filename = outputDirectory_ + grn_.getId() + "_goldstandard_signed.tsv";
		url = new File(filename).toURI().toURL();
		grn_.saveTSV(url);
		
		checkForInterruption();
		
		// save the complete network in smbl2
		filename = outputDirectory_ + grn_.getId() + ".xml";
		url = new File(filename).toURI().toURL();
		grn_.writeSBML(url);

		log_.log(Level.INFO, "");
		
		checkForInterruption();
		
		// create and run the experiments
		// loadInitialConditions("tmp/InSilicoSize10-Yeast3-initial-conditions.tsv");
		runAll();
		
		checkForInterruption();
		
		GnwSettings set = GnwSettings.getInstance();
		boolean addExperimentalNoise = set.getAddNormalNoise() || set.getAddLognormalNoise() || set.getAddMicroarrayNoise(); 
		
		// print the data
		String postfix = "";
		if (addExperimentalNoise)
			postfix = "_noexpnoise";
		
		checkForInterruption();
		
		for (int i=0; i<steadyStateExperiments_.size(); i++) {
			SteadyStateExperiment exp = steadyStateExperiments_.get(i);
			exp.printAll(outputDirectory_, postfix);
			String label = exp.getLabel();
			if (label == "multifactorial" || label == "dream4_timeseries" || label == "dualknockouts")
				exp.getPerturbation().printPerturbations(outputDirectory_, exp.getLabel());
		}
		
		checkForInterruption();
		
		for (int i=0; i<steadyStateExperimentsODE_.size(); i++)
			steadyStateExperimentsODE_.get(i).printAll(outputDirectory_, "_nonoise");

		checkForInterruption();
		
		for (int i=0; i<timeSeriesExperiments_.size(); i++) {
			TimeSeriesExperiment exp = timeSeriesExperiments_.get(i);
			exp.printAll(outputDirectory_, postfix);
			String label = exp.getLabel();
			if (label == "dream4_timeseries" || label == "multifactorial_timeseries" || label == "dualknockout_timeseries")
				exp.getPerturbation().printPerturbations(outputDirectory_, exp.getLabel());
		}
		
		checkForInterruption();
		
		for (int i=0; i<timeSeriesExperimentsODE_.size(); i++)
			timeSeriesExperimentsODE_.get(i).printAll(outputDirectory_, "_nonoise");

		checkForInterruption();
		
		// add noise, normalize, and print
		if (addExperimentalNoise) {
			
			addExperimentalNoise();
			if (set.getNormalizeAfterAddingNoise())
				normalize();
			
			for (int i=0; i<steadyStateExperiments_.size(); i++)
				steadyStateExperiments_.get(i).printAll(outputDirectory_, "");
			for (int i=0; i<timeSeriesExperiments_.size(); i++)
				timeSeriesExperiments_.get(i).printAll(outputDirectory_, "");
		}
	}

		
	// ============================================================================
	// PRIVATE METHODS

	/** 
	 * Run all experiments. If the timeSeriesExperiments_ have
	 * already been created (e.g. with loadInitialConditions()), they are run as is.
	 * Otherwise, they are created with random initial conditions according to the
	 * settings in GnwSettings.
	 * @throws CancelException, Exception 
	 */
	private void runAll() throws IllegalArgumentException, CancelException, Exception {

		GnwSettings set = GnwSettings.getInstance();
		
		// The user should have selected either ODEs, SDEs, or both
		if (!set.getSimulateODE() && !set.getSimulateSDE())
			throw new IllegalArgumentException("At least one of simulateODE_ and simulateSDE_ must be selected in GnwSettings");
		
		runSteadyStateExperiments();
		checkForInterruption();
		runTimeSeriesExperiments();
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Create and run all steady-state experiments
	 * @throws CancelException, Exception 
	 */
	private void runSteadyStateExperiments() throws IllegalArgumentException, CancelException, Exception {
		
		GnwSettings set = GnwSettings.getInstance();
				
		// First, ODEs are always simulated (even if it's not set in the GnwSettings), because
		// we use the ODE wild-type as intial condition for the SDE wild-type, and the time-to-
		// convergence of the ODE as limit for the SDEs
		
		checkForInterruption();
		
		// the wild-type
		SteadyStateExperiment wt = new SteadyStateExperiment(Solver.type.ODE, null, "wildtype");
		wt.setGrn(grn_);
		wt.run(null);
		wildTypeODE_ = constructInitialConditionFromWildType(wt);
		steadyStateExperiments_.add(wt);
				
		checkForInterruption();
		
		// knockouts
		if (set.generateSsKnockouts()) {
			PerturbationSingleGene knockouts = new PerturbationSingleGene(grn_);
			knockouts.singleGenePerturbations(0);
			createAndRunSsExperiment(Solver.type.ODE, knockouts, "knockouts");
		}
		
		checkForInterruption();
		
		// knockdowns
		if (set.generateSsKnockdowns()) {
			PerturbationSingleGene knockdowns = new PerturbationSingleGene(grn_);
			knockdowns.singleGenePerturbations(0.5);
			createAndRunSsExperiment(Solver.type.ODE, knockdowns, "knockdowns");
		}
		
		checkForInterruption();

		// multifactorial weak (DREAM4)
		if (set.generateSsMultifactorial()) {
			PerturbationMultifactorial multifact = new PerturbationMultifactorial(grn_);

			String label = "multifactorial";
			if (!set.getLoadPerturbations())
				multifact.multifactorialAllGenesWeak(grn_.getSize());
			else
				multifact.loadPerturbations(outputDirectory_, label);
						
			createAndRunSsExperiment(Solver.type.ODE, multifact, label);
		}
		
		checkForInterruption();
		
		// multifactorial strong (not used for steady-states in DREAM4)
		if (set.generateSsDREAM4TimeSeries()) {
			PerturbationMultifactorial multifact = new PerturbationMultifactorial(grn_);
			
			String label = "dream4_timeseries";
			if (!set.getLoadPerturbations())
				multifact.multifactorialStrong(grn_.getSize());
			else
				multifact.loadPerturbations(outputDirectory_, "dream4_timeseries");

			createAndRunSsExperiment(Solver.type.ODE, multifact, label);
		}
		
		checkForInterruption();
		
		// dual knockouts
		if (set.generateSsDualKnockouts()) {
			PerturbationDual dualKnockouts = new PerturbationDual(grn_);
			
			String label = "dualknockouts";
			if (!set.getLoadPerturbations())
				dualKnockouts.dualPerturbations(0, grn_.getSize());
			else
				dualKnockouts.loadPerturbations(outputDirectory_, label);
			
			createAndRunSsExperiment(Solver.type.ODE, dualKnockouts, label);
		}
		
		checkForInterruption();
		
		// if we want to simulate SDEs
		if (set.getSimulateSDE()) {
			
			// above, we have saved the ODE experiments temporarily in steadyStateExperiments_
			steadyStateExperimentsODE_ = steadyStateExperiments_;
			steadyStateExperiments_ = new ArrayList<SteadyStateExperiment>();
			
			// start at 1 because the wild-type is already done
			for (int i=0; i<steadyStateExperimentsODE_.size(); i++) {
				
				checkForInterruption();
				
				SteadyStateExperiment ssODE = steadyStateExperimentsODE_.get(i);
				Perturbation perturbation = ssODE.getPerturbation();
				String label = ssODE.getLabel();
				
				SteadyStateExperiment ss = new SteadyStateExperiment(Solver.type.SDE, perturbation, label);
				ss.setGrn(grn_);
				
				// the wild-type needs only a short simulation time
				if (i == 0) {
					if (label != "wildtype")
						throw new RuntimeException("The first ODE experiment must be the wild-type");
					ss.setMaxtSDE(set.getMintSDE()); // set maxt to a short time because we already initialized with wt
				} else
					ss.setTimeToConvergenceODE(ssODE.getTimeToConvergenceODE());
				
				ss.run(wildTypeODE_);
				steadyStateExperiments_.add(ss);
			}
			
			if (!set.getSimulateODE())
				steadyStateExperimentsODE_ = new ArrayList<SteadyStateExperiment>();
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Run all time-series experiments
	 * @throws CancelException 
	 */
	private void runTimeSeriesExperiments() throws CancelException {
		
		if (steadyStateExperiments_ == null)
			throw new RuntimeException("The wild-type must be simulated to run time-series experiments");
		
		// wild-type will be used as initial condition below (note, this is the SDE wild-type if SDEs are used)
		SteadyStateExperiment wildType = steadyStateExperiments_.get(0);
		if (wildType.getLabel() != "wildtype")
			throw new RuntimeException("The first steady-state experiment must be the wild-type");
		DoubleMatrix1D xy0 = constructInitialConditionFromWildType(wildType);
		
		GnwSettings set = GnwSettings.getInstance();
		Solver.type simulationType = Solver.type.ODE; 
		if (set.getSimulateSDE())
			simulationType = Solver.type.SDE;		
		
		checkForInterruption();
		
		// knockouts
		if (set.generateTsKnockouts()) {
			PerturbationSingleGene knockouts = new PerturbationSingleGene(grn_);
			knockouts.singleGenePerturbations(0);
			createAndRunTsExperiment(simulationType, knockouts, false, "knockout_timeseries", xy0);
		}
		
		checkForInterruption();
		
		// knockdowns
		if (set.generateTsKnockdowns()) {
			PerturbationSingleGene knockdowns = new PerturbationSingleGene(grn_);
			knockdowns.singleGenePerturbations(0.5);
			createAndRunTsExperiment(simulationType, knockdowns, false, "knockdown_timeseries", xy0);
		}
		
		checkForInterruption();
		
		// multifactorial weak
		if (set.generateTsMultifactorial()) {
			PerturbationMultifactorial multifact = null;
			String label = "multifactorial_timeseries";
			
			if (!set.getLoadPerturbations()) {
				// use the same perturbations as for the steady-state experiments, if they were simulated
				for (int i=0; i<steadyStateExperiments_.size(); i++) {
					
					checkForInterruption();
					
					if (steadyStateExperiments_.get(i).getLabel() == "multifactorial") {
						multifact = (PerturbationMultifactorial) steadyStateExperiments_.get(i).getPerturbation();
						break;
					}
				}
				if (multifact == null) {
					multifact = new PerturbationMultifactorial(grn_);
					multifact.multifactorialAllGenesWeak(set.getNumTimeSeries());
				}
			} else {
				multifact = new PerturbationMultifactorial(grn_);
				multifact.loadPerturbations(outputDirectory_, "multifactorial");
			}
			createAndRunTsExperiment(simulationType, multifact, false, label, xy0);
		}
		
		checkForInterruption();
		
		// multifactorial strong
		if (set.generateTsDREAM4TimeSeries()) {
			PerturbationMultifactorial multifact = null;
			String label = "dream4_timeseries";
			
			if (!set.getLoadPerturbations()) {
				// use the same perturbations as for the steady-state experiments, if they were simulated
				for (int i=0; i<steadyStateExperiments_.size(); i++) {
					
					checkForInterruption();
					
					if (steadyStateExperiments_.get(i).getLabel() == "dream4_timeseries") {
						multifact = (PerturbationMultifactorial) steadyStateExperiments_.get(i).getPerturbation();
						break;
					}
				}
				if (multifact == null) {
					multifact = new PerturbationMultifactorial(grn_);
					multifact.multifactorialStrong(set.getNumTimeSeries());
				}
			} else {
				multifact = new PerturbationMultifactorial(grn_);
				multifact.loadPerturbations(outputDirectory_, label);
			}
			createAndRunTsExperiment(simulationType, multifact, true, label, xy0);
		}
		
		checkForInterruption();
		
		// dual knockouts
		if (set.generateTsDualKnockouts()) {
			PerturbationDual dualKnockouts = null;
			String label = "dualknockout_timeseries";
			
			if (!set.getLoadPerturbations()) {
				// use the same perturbations as for the steady-state experiments, if they were simulated
				for (int i=0; i<steadyStateExperiments_.size(); i++) {
					
					checkForInterruption();
					
					if (steadyStateExperiments_.get(i).getLabel() == "dualknockouts") {
						dualKnockouts = (PerturbationDual) steadyStateExperiments_.get(i).getPerturbation();
						break;
					}
				}
				if (dualKnockouts == null) {
					dualKnockouts = new PerturbationDual(grn_);
					dualKnockouts.dualPerturbations(0, grn_.getSize());
				}
			} else {
				dualKnockouts = new PerturbationDual(grn_);
				dualKnockouts.loadPerturbations(outputDirectory_, "dualknockouts");
			}
			createAndRunTsExperiment(simulationType, dualKnockouts, false, label, xy0);
		}
		
		checkForInterruption();
		
		// if we want to simulate both SDEs and ODEs
		if (set.getSimulateSDE() && set.getSimulateODE()) {
			
			for (int i=0; i<timeSeriesExperiments_.size(); i++) {
				
				checkForInterruption();
				
				Perturbation perturbation = timeSeriesExperiments_.get(i).getPerturbation();
				String label = timeSeriesExperiments_.get(i).getLabel();
				boolean restoreWildTypeAtHalftime = timeSeriesExperiments_.get(i).getRestoreWildTypeAtHalftime();
				TimeSeriesExperiment ts = new TimeSeriesExperiment(Solver.type.ODE, perturbation, restoreWildTypeAtHalftime, label);
				ts.initializeEquallySpacedTimePoints();
				ts.setGrn(grn_);
				ts.run(wildTypeODE_);
				timeSeriesExperimentsODE_.add(ts);
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/** Create and run a steady-state experiment, add it to steadyStateExperiments_ */
	private void createAndRunSsExperiment(Solver.type simulationType, Perturbation perturbation, String label) {
		SteadyStateExperiment ss = new SteadyStateExperiment(simulationType, perturbation, label);
		ss.setGrn(grn_);
		ss.run(wildTypeODE_);
		steadyStateExperiments_.add(ss);
	}
	
	
	// ----------------------------------------------------------------------------

	/** Create and run a time-series experiment, add it to timeSeriesExperiments_ */
	private void createAndRunTsExperiment(Solver.type simulationType, Perturbation perturbation, boolean restoreWildTypeAtHalftime, String label, DoubleMatrix1D xy0) {
		TimeSeriesExperiment ts = new TimeSeriesExperiment(simulationType, perturbation, restoreWildTypeAtHalftime, label);
		ts.initializeEquallySpacedTimePoints();
		ts.setGrn(grn_);
		ts.run(xy0);
		timeSeriesExperiments_.add(ts);
	}
		
		
	// ----------------------------------------------------------------------------

	/**
	 * Add log-normal noise to the data
	 */
	private void addExperimentalNoise() {

		for (int i=0; i<steadyStateExperiments_.size(); i++)
			steadyStateExperiments_.get(i).addNoise();

		for (int i=0; i<timeSeriesExperiments_.size(); i++)
			timeSeriesExperiments_.get(i).addNoise();
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Normalize the data (divide by the maximum)
	 */
	private void normalize() {

		double max = 0.;
	
		// find the maximum concentration value off all experiments
		for (int i=0; i<steadyStateExperiments_.size(); i++) {
			double max_i = steadyStateExperiments_.get(i).getMaximumConcentration();
			if (max_i > max)
				max = max_i;
		}

		for (int i=0; i<timeSeriesExperiments_.size(); i++) {
			double max_i = timeSeriesExperiments_.get(i).getMaximumConcentration();
			if (max_i > max)
				max = max_i;
		}

		log_.log(Level.INFO, "Normalizing with respect to max = " + Double.toString(max));
		
		// save the coefficient
		String filename = outputDirectory_ + grn_.getId() + "_normalization_constant.tsv";
		log_.log(Level.INFO, "Writing file " + filename);
		try {
			FileWriter fw = new FileWriter(filename, false);
			fw.write(Double.toString(max) + "\n");
			fw.close();
		} catch (IOException e) {
			log_.log(Level.INFO, "Error writing file, exception " + e.getMessage(), e);
			throw new RuntimeException();
		}

		// normalize according to this max
		for (int i=0; i<steadyStateExperiments_.size(); i++)
			steadyStateExperiments_.get(i).normalize(max);
		for (int i=0; i<steadyStateExperimentsODE_.size(); i++)
			steadyStateExperimentsODE_.get(i).normalize(max);

		if (timeSeriesExperiments_ != null) {
			for (int i=0; i<timeSeriesExperiments_.size(); i++)
				timeSeriesExperiments_.get(i).normalize(max);
		}
	}
	

	// ----------------------------------------------------------------------------

	
	/** Test whether the user has interrupted the process (e.g. using "cancel" in the GUI) */
	private void checkForInterruption() throws CancelException {
		
		if (GnwSettings.getInstance().stopBenchmarkGeneration())
			throw new CancelException("Benchmark generation canceled!");
	}
}
