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
import java.util.logging.Logger;


/** A HillGene is independently regulated by one ore more regulatory modules.
 *  
 * A module is either an enhancer or a repressor (defined by isEnhancer_). The module is controlled
 * synergistically by its activators, it is active only if all of them
 * are bound together. Optionally, it can have deactivators, which synergistically
 * silence the module. Note that the activators of a repressor module are
 * actually repressors of the gene.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
public class RegulatoryModule {

	/** The type of the module (true = enhancer, false = repressor) */
	private boolean isEnhancer_;
	/** True if the activators first form a hetero-oligomer, and this complex binds to the promoter */
	private boolean bindsAsComplex_;
	/** The activators of this module */
	private int numActivators_;	
	/** The deactivators of this module */
	private int numDeactivators_;
	/** Dissociation constants k for each state */
	private double[] k_;
	/** Hill coefficients for the regulators (activators and deactivators) */
	private double[] n_;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private Logger log_ = Logger.getLogger(RegulatoryModule.class.getName());
    
    
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor
	 */
	public RegulatoryModule() {
		isEnhancer_ = true;
		bindsAsComplex_ = false;
		numActivators_ = -1;
		numDeactivators_ = -1;
		k_ = null;
		n_ = null;
	}
	
		
	// ----------------------------------------------------------------------------
	
	/**
	 * Return a string representation of this regulatory module.
	 * The syntax is: [~] (activator {*activator} {~deactivator})
	 * activator = 1, 2, ..., numActivators
	 * deactivator = numActivators+1, numActivators+2, ..., numInputs
	 * Instead of starting at 1, the inputs are numbered starting at nextInputIndex.
	 */
	public String toString(int nextInputIndex) {

		String mod = "";
		if (!isEnhancer_) 
			mod += "~";		
		
		mod += "(" + nextInputIndex;		
		for (int i=1; i<numActivators_; i++)
			mod += "*" + (nextInputIndex + i);
		
		if (numDeactivators_ > 0) {
			mod += "~" + (nextInputIndex + numActivators_);
			for (int i=numActivators_+1; i<numActivators_+numDeactivators_; i++)
				mod += "~" + (nextInputIndex + i);
		}
		mod += ")";
		
		return mod;
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns an array of booleans, where element i is true if input i is an enhancer
	 * and false if it is an inhibitor of the gene (not of the regulatory module).
	 * Note, an enhancing input can be either an activator of an enhancing module, or
	 * a deactivator of a repressor module.
	 */
	public ArrayList<Boolean> getEdgeSigns() {

		ArrayList<Boolean> edgeSigns = new ArrayList<Boolean>(5);
		if (isEnhancer_) {
			for (int i=0; i<numActivators_; i++)
				edgeSigns.add(true);
			for (int i=0; i<numDeactivators_; i++)
				edgeSigns.add(false);
		} else {
			for (int i=0; i<numActivators_; i++)
				edgeSigns.add(false);
			for (int i=0; i<numDeactivators_; i++)
				edgeSigns.add(true);	
		}
		
		return edgeSigns;
	}
	

	// ----------------------------------------------------------------------------

	/** 
	 * Compute the activation of this module as a function of the regulator concentrations x.
	 * x must be ordered, first come the numActivators_ activators, then the deactivators.
	 */
	public double computeActivation(double[] x) {
		
		int numInputs = numActivators_ + numDeactivators_;
		assert x.length == numInputs;
		
		// define xi_i := (x_i/k_i)^n_i
		double[] xi = new double[numInputs];
		for (int i=0; i<numInputs; i++) {
			assert x[i] >= 0.0 : x[i];
			xi[i] = Math.pow(x[i] / k_[i], n_[i]);
		}
		
		// compute the numerator
		double multiplyActivators = 1;
		for (int i=0; i<numActivators_; i++)
			multiplyActivators *= xi[i];
		double numerator = multiplyActivators;
		
		// compute the partition function
		double denominator = 1;
		
		if (bindsAsComplex_) {
			// if it is a complex, there are three states of the module,
			// either the activated complex is bound, the deactivated complex is bound, or it is not bound
			
			// activated complex bound
			denominator += multiplyActivators;
			
			if (numDeactivators_ > 0) { // BUG FIXED: this if was not here in v1
				double multiplyAllInputs = multiplyActivators;
				for (int j=numActivators_; j<numInputs; j++)
					multiplyAllInputs *=  xi[j];
				denominator += multiplyAllInputs;
			}
			
		} else {
			// I was actually computing (x0+1)(x1+1) ... = 1 + x0 + x1 + x0x1 + ... in the latter form!
			/*int numStates = (int)Math.round(Math.pow(2, numInputs));
			for (int i=1; i<numStates; i++) {
				String s = Integer.toBinaryString(i); // note, leading 0's are not included
				int slength = s.length();
				
				// if input j is active in this state i, add it to the term
				double term = 1;
				for (int j=0; j<numInputs; j++) {
					// if s.length()-j-1 smaller than zero, the corresponding entry is one of the leading zeros
					if (slength-j-1 >= 0 && s.charAt(slength-j-1) == '1')	
						term *=  xi[j];
				}
				denominator += term;
			}*/
			// ok, this *is* arguably faster
			for (int i=0; i<numInputs; i++)
				denominator *=  (xi[i] + 1);
		}
		double activation = numerator / denominator;
		
		assert activation >= 0 && activation <= 1;
		return activation;
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Random initialization of parameter values. isEnhancer_, numActivators_, and
	 * numDeactivators_ must already be set. If the module is a complex, the k_'s 
	 * are interpreted as follows:
	 * The activation is half-maximal if x1 = k1, x2 = k2, ..., kN = kN, i.e.,
	 * k_complex = k1^n1 * k2^n2 * ... * kN^nN. In the computation of the activation,
	 * there will be only two states, the complex is bound or not.
	 */
	public void randomInitializationOfParameters() {
		
		GnwSettings uni = GnwSettings.getInstance();
		int numInputs = numActivators_ + numDeactivators_;
		
		k_ = new double[numInputs];
		n_ = new double[numInputs];
		for (int i=0; i<numInputs; i++) {
			k_[i] = uni.getRandomK();
			n_[i] = uni.getRandomN();
		}
	}
	

	// ============================================================================
	// SETTERS AND GETTERS

	public boolean isEnhancer() { return isEnhancer_; }
	public void setIsEnhancer(boolean b) { isEnhancer_ = b; }
	
	public boolean bindsAsComplex() { return bindsAsComplex_; }
	public void setBindsAsComplex(boolean b) { bindsAsComplex_ = b; }

	public int getNumInputs() { return numActivators_ + numDeactivators_; }

	public int getNumActivators() {return numActivators_; }
	public void setNumActivators(int n) { numActivators_ = n; }

	public int getNumDeactivators() { return numDeactivators_;	}
	public void setNumDeactivators(int n) {	numDeactivators_ = n; }
	
	public double[] getK() { return k_; }
	public void setK(double[] k) { k_ = k; }
	
	public double[] getN() { return n_; }
	public void setN(double[] n) { n_ = n; }
	
}
