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
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import ch.epfl.lis.sde.Sde;

/** SDE system that is used to simulated the gene networks.
 * 
 * Represents the system of stochastic differential equations
 * that are used to simulate the gene network.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
public class GeneNetworkSDE extends Sde {

	/** Reference to GeneNetwork system to integrate */
	private GeneNetwork grn_;
	/** Network size */
	private int networkSize_;
	/** Support vector V (production) */
	private DoubleMatrix1D V_;
	/** Support vector D (degradation) */
	private DoubleMatrix1D D_;
	
	
    // =======================================================================================
    // PUPBLICS METHODS
	
	/**
	 * Default constructor
	 */
//	public GeneNetworkSDE(GeneNetwork grn) {
//		
//		this(grn,Sde.ITO, null);
//	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
//	public GeneNetworkSDE(GeneNetwork grn, int scheme) {
//		
//		this(grn, scheme, null);
//	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
//	public GeneNetworkSDE(GeneNetwork grn, int scheme, DoubleMatrix1D X0) {
//		
//		// dimension depends if translation is model or not
//		super(GnwSettings.getInstance().getModelTranslation() ? grn.getSize()*2 : grn.getSize(), scheme, X0);
//		init(grn);
//	}
	public GeneNetworkSDE(GeneNetwork grn) {
		
		// dimension depends if translation is model or not
		super(GnwSettings.getInstance().getModelTranslation() ? grn.getSize()*2 : grn.getSize());
		init(grn);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Initialisation specific to this SDE system
	 */
	public void init(GeneNetwork grn) {
		grn_ = grn;
		networkSize_ = grn_.getSize();
		id_ = grn_.getId();
		
		V_ = new DenseDoubleMatrix1D(networkSize_);
		D_ = new DenseDoubleMatrix1D(networkSize_);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Computes the drift coefficients F and diffusion coefficient G at a given time.
	 * Take into account is translation is modelled or not.
	 * 
	 * @throws Exception
	 */
	public void getDriftAndDiffusion(final double t, final DoubleMatrix1D Xin,
			DoubleMatrix1D F, DoubleMatrix2D G) throws Exception {
		
		GnwSettings settings = GnwSettings.getInstance();
		
		grn_.setX(Xin.viewPart(0, networkSize_)); // set current mRNA concentrations [X]
		if (settings.getModelTranslation())
			grn_.setY(Xin.viewPart(networkSize_, networkSize_)); // set current protein concentrations [Y]
		grn_.computeMRnaProductionRates(V_); // get mRNA production rates
		grn_.computeMRnaDegradationRates(D_); // get mRNA degradation rates
		
		double m = GnwSettings.getInstance().getNoiseCoefficientSDE();
		Double sqrt = 0.;
		
		// Transcription
		for (int i=0; i<networkSize_; i++) {
			
			// Set drift vector
			F.set(i, V_.get(i)-D_.get(i));
			
			// Set diffusion matrix
			sqrt = Math.sqrt(V_.get(i) + D_.get(i));
			if (sqrt.isNaN()) {
				throw new Exception("NaN mRNA");
			}
			else
				G.set(i, i, m*sqrt);
		}
		
		// Translation (if modelled)
		if (settings.getModelTranslation()) {
			
			grn_.computeProteinProductionRates(V_); // get protein production rates
			grn_.computeProteinDegradationRates(D_); // get protein degradation rates
			
			int index = 0;
			
			for (int i=0; i<networkSize_; i++) {
				
				index = i+networkSize_;
				
				// Set drift vector
				F.set(index, V_.get(i)-D_.get(i));
				
				// Set diffusion matrix
				sqrt = Math.sqrt(V_.get(i) + D_.get(i));
				if (sqrt.isNaN())
					throw new Exception("NaN protein");
				else
					G.set(index, index, m*sqrt);
			}
		}
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public void setGeneNetwork(GeneNetwork grn) { grn_ = grn; }
	public GeneNetwork getGeneNetwork() { return grn_; }
}
