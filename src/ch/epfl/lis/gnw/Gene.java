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

import cern.colt.matrix.DoubleMatrix1D;
import ch.epfl.lis.networks.Node;

import java.util.ArrayList;
import java.util.logging.Logger;


/** A gene is a node of a gene network.
 * 
 * It contains the gene parameters that are independent
 * of the regulation function. These parameters are the maximum transcription and translation
 * rates, and the mRNA and protein degradation rates. This class allows to compute the production
 * rate and to randomly initialize the dynamical model of the gene. Different models of gene
 * regulation could be implemented in subclasses of Gene. For now, only HillGene is implemented.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
abstract public class Gene extends Node {

	/** Maximum transcription rate */
	protected double max_ = -1;
	/** Degradation rate */
	protected double delta_ = -1;
	/** Maximum translation rate */
	protected double maxTranslation_ = -1;
	/** Protein degradation rate */
	protected double deltaProtein_ = -1;
	/** True if this is a regulatory gene (must be initialized explicitly by calling GeneNetwork.initializeTfFlags() */
	protected boolean isTf_ = false;
	/** Perturbation of basal transcription rate */
	//protected double perturbationBasalActivation_ = 0;
	
	/** Reference to the gene regulatory network of this gene */
	protected GeneNetwork grn_ = null;
	
	/** 
	 * An array with references to the inputs of this gene. The order defines which
	 * input goes where. E.g., inputGenes_[0] is the first input of the first module. 
	 */
	protected ArrayList<Gene> inputGenes_ = null;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(Gene.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public Gene() {
		super();
	}
	
	
	/** Constructor, sets the given gene network */
	public Gene(GeneNetwork grn) {
		super();
		grn_ = grn;
	}	
		
	
	// ----------------------------------------------------------------------------
	
	/** Random initialization of all parameters and the gene regulation function */
	public void randomInitialization() {
		
		// Initialize all Gene parameters (max_, delta_, maxTranslation_, and deltaProtein_)
		setRandomHalfLife();
		// Initialize the gene regulation function and its parameters in the subclass
		subclassRandomInitialization();
	}


	// ----------------------------------------------------------------------------

	/** Return the names and values of the gene's parameters (the first one is always the degradation rate) */
	public void compileParameters(ArrayList<String> names, ArrayList<Double> values) {
		
		names.clear();
		names.add("delta");
		names.add("max");
		
		values.clear();
		values.add(delta_);
		values.add(max_);

		if (GnwSettings.getInstance().getModelTranslation()) {
			names.add("deltaProtein");
			names.add("maxTranslation");
			values.add(deltaProtein_);
			values.add(maxTranslation_);
		}
		
		subclassCompileParameters(names, values);
	}

	
	// ----------------------------------------------------------------------------
	
	/** Initialization of the gene with the given list of parameters and inputs */
	public void initialization(ArrayList<String> paramNames, ArrayList<Double> paramValues, ArrayList<Gene> inputGenes) {
		
		inputGenes_ = inputGenes;
		// Initialize all Gene parameters (max_, delta_, maxTranslation_, and deltaProtein_)
		initializeHalfLife(paramNames, paramValues);
		// Initialize the gene regulation function and its parameters in the subclass
		subclassInitialization(paramNames, paramValues);
	}


	// ----------------------------------------------------------------------------
	
	/** Return the mRNA half-life, see setRandomHalfLife() for an explanation */
	public double getHalfLife() { return Math.log(2) / delta_; }
	
	/** Return the protein half-life, see setRandomHalfLife() for an explanation */
	public double getProteinHalfLife() { return Math.log(2) / deltaProtein_; }

	
	// ============================================================================
	// ABSTRACT METHODS

	/** Compute the mRNA production rate of this gene */
	abstract public double computeMRnaProductionRate(int geneIndex, DoubleMatrix1D c);
	/** Compute the mRNA degradation rate of this gene (>= 0) */
	abstract public double computeMRnaDegradationRate(double x);
	/** Compute the protein production rate of this gene */
	abstract public double computeProteinProductionRate(double x);
	/** Compute the protein degradation rate of this gene (>= 0) */
	abstract public double computeProteinDegradationRate(double y);
	/** Perturb the basal activation with this value (subclass defines what exactly that means) */
	abstract public void perturbBasalActivation(double deltaBasalActivation);
	/** Restore the wild-type basal activation */
	abstract public void restoreWildTypeBasalActivation();
	/** Return the basal activation */
	abstract public double getBasalActivation();
	/** Random initialization of the gene regulation function (inputIndexes_ needs to be set first!) */
	abstract protected void subclassRandomInitialization();
	/** Return the names and values of the subclass parameters */
	abstract protected void subclassCompileParameters(ArrayList<String> names, ArrayList<Double> values);
	/** Initialize the gene's parameters from an array (the number and type of params depends on the subclass) */	
	abstract protected void subclassInitialization(ArrayList<String> paramNames, ArrayList<Double> paramValues);
	
	
	// ============================================================================
	// PRIVATE METHODS

	/**
	 * Set random half-lives for this gene's products. In the non-dimensionalized
	 * model, max_ = delta_ and maxTranslation_ = deltaProtein_. We have exponential
	 * decay, the half-life and the decay rate are thus related by:
	 *    t_1/2 = ln(2) / delta
	 *    delta = ln(2) / (t_1/2)
	 */
	private void setRandomHalfLife() {
		
		GnwSettings uni = GnwSettings.getInstance();
		delta_ = Math.log(2) / uni.getRandomHalfLife();
		max_ = delta_;
		
		if (uni.getModelTranslation()) {
			deltaProtein_ = Math.log(2) / uni.getRandomHalfLife();
			maxTranslation_ = deltaProtein_;
		}
	}

	
	// ----------------------------------------------------------------------------
	
	/** Initialization of delta_, max_, deltaProtein_, and maxTranslation_ from the given list of parameters */
	private void initializeHalfLife(ArrayList<String> paramNames, ArrayList<Double> paramValues) {

		delta_ = paramValues.get(paramNames.indexOf("delta"));
		max_ = paramValues.get(paramNames.indexOf("max"));
		if (GnwSettings.getInstance().getModelTranslation()) {
			deltaProtein_ = paramValues.get(paramNames.indexOf("deltaProtein"));
			maxTranslation_ = paramValues.get(paramNames.indexOf("maxTranslation"));
		}
	}

		
	// ============================================================================
	// SETTERS AND GETTERS

	public void setMax(double d) { max_ = d; }
	public double getMax() { return max_; }

	public void setDelta(double d) { delta_ = d; }
	public double getDelta() { return delta_; }

	public void setMaxTranslation(double d) { maxTranslation_ = d; }
	public double getMaxTranslation() { return maxTranslation_; }

	public void setDeltaProtein(double d) { deltaProtein_ = d; }
	public double getDeltaProtein() { return deltaProtein_; }

	//public void setPerturbationBasalActivation(double d) { perturbationBasalActivation_ = d; }
	//public double getPerturbationBasalActivation() { return perturbationBasalActivation_; }
	
	public boolean getIsTf() { return isTf_; }
	public void setIsTf(boolean isTf) { isTf_ = isTf; }

	public ArrayList<Gene> getInputGenes() {return inputGenes_; }
	public void setInputGenes(ArrayList<Gene> inputs) { inputGenes_ = inputs; }
	
}
