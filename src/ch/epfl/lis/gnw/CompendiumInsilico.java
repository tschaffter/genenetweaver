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
import java.util.Iterator;
import java.util.LinkedHashMap;

import cern.colt.matrix.DoubleMatrix1D;



/** 
 * Generate in silico data corresponding to a compendium (used to generate the in silico
 * benchmarks of the DREAM5 network inference challenge)
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class CompendiumInsilico extends Compendium {
	
	/** 
	 * The wild type (unperturbed steady state) is used as initial condition to compute 
	 * the perturbed steady state for each condition
	 */
	private SteadyStateExperiment wildType_ = null;
	/** The wild type steady state */
	private DoubleMatrix1D xy0_ = null;
	/** Fraction of random interactions to add (to make networks more dense if a gold standard is assumed to be incomplete) */
	protected double fractionRandomEdges_ = 0;

	/** Map that indicates which gene should be perturbed if a gene is not part of the gold standard (used for in silico compendia) */
	protected LinkedHashMap<String, String> substitutions_ = new LinkedHashMap<String, String>();

	
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Default constructor
	 */
	public CompendiumInsilico(String name, 
			String experimentFile,
			String experimentDefFile,
			String goldStandardFile) {
		
		super(name,
				null,
				experimentFile,
				experimentDefFile,
				null,
				null,
				goldStandardFile);
		
		isInvivo_ = false;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Generate an in silico benchmark (create kinetic model, create perturbations for each experiment,
	 * run the experiments).
	 */
	public void generateInSilicoBenchmark(boolean anonymize) {

		anonymize_ = anonymize;
		initialize();
		
		// Generate the kinetic model, the perturbations, and run all experiments
		grn_.randomInitialization();
		generatePerturbations();
		runExperiments();

		// Write the SDE data without noise
		writer_.writeExpressionMatrix("_noexpnoise");
		
		// Add noise, compute averages, write files
		addNoise();
		computeAverageOfRepeats();
		
		// Remove gene labels (has to be done after the data is loaded) and write everything
		removeGeneLabels();
		writer_.write();
	}

		
	// ============================================================================
	// PRIVATE METHODS
	
	/** Initialize the drug and genetic perturbations */
	private void generatePerturbations() {
	
		// Create and initialize the multifactorial perturbations for the conditions
		conditionPerturbations_ = new PerturbationMultifactorial(grn_);
		conditionPerturbations_.multifactorialAllGenesWeak(conditions_.size());
		
		// Initialize the drug perturbations as strong multifactorial perturbations
		Iterator<PerturbationDrug> iter = drugPerturbationLookup_.values().iterator();
		while (iter.hasNext())
			iter.next().drugPerturbation();
		
		// Initialize the perturbation for every experiment
		for (int i=0; i<conditions_.size(); i++)
			conditions_.get(i).initializeExperimentPerturbations(conditionPerturbations_.getPerturbations().viewRow(i));
	}


	// ----------------------------------------------------------------------------

	/** Run all experiments */
	private void runExperiments() {

		// The wild type
		wildType_ = new SteadyStateExperiment(Solver.type.ODE, null, "wildtype");
		wildType_.setGrn(grn_);
		wildType_.run(null);
		xy0_ = BenchmarkGenerator.constructInitialConditionFromWildType(wildType_);
		
		for (int i=0; i<conditions_.size(); i++)
			conditions_.get(i).runExperiments(xy0_);

	}

	
	// ----------------------------------------------------------------------------

	/** Add experimental noise to the expression data */
	private void addNoise() {

		GnwSettings set = GnwSettings.getInstance();
		boolean addNoise = set.getAddNormalNoise() || set.getAddLognormalNoise() || set.getAddMicroarrayNoise(); 

		if (!addNoise)
			return;
		
		for (int c=0; c<conditions_.size(); c++) {
			ArrayList<Experiment> exps = conditions_.get(c).getExperiments();
			for (int e=0; e<exps.size(); e++)
				exps.get(e).addNoise();
		}
	}


	// ============================================================================
	// SETTERS AND GETTERS

	public void setFractionRandomInteractions(double fract) { fractionRandomEdges_ = fract; }

	public LinkedHashMap<String, String> getSubstitutions() { return substitutions_; }

	
}
