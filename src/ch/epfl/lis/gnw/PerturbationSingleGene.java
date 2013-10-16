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

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

public class PerturbationSingleGene extends Perturbation {

	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Constructor
	 */
	public PerturbationSingleGene(GeneNetwork grn) {
		super(grn);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Single-gene perturbations. N experiments, one for every gene. In experiment i,
	 * the maximum transcription rate of gene i is set to m_i = perturbation*m_i.
	 */
	public void singleGenePerturbations(double perturbation) {

		saveWildType();
		
		numPerturbations_ = numGenes_;
		perturbations_ = new DenseDoubleMatrix2D(numPerturbations_, numGenes_);

		// initialize matrix at wild-type (unperturbed)
		for (int p=0; p<numPerturbations_; p++)
			for (int g=0; g<numGenes_; g++)
				perturbations_.set(p, g, wildType_.get(g));
		
		// set diagonal elements to perturbation*m_i
		for (int g=0; g<numGenes_; g++)
			perturbations_.set(g, g, perturbation*wildType_.get(g));
	}
	
	
	// ----------------------------------------------------------------------------

	/** Apply the k'th perturbation to the grn_ */
	public void applyPerturbation(int k) {
		
		for (int i=0; i<numGenes_; i++)
			grn_.getGene(i).setMax( perturbations_.get(k,i) );
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** Save the wild-type of the network grn_ in wildType_ */
	protected void saveWildType() {
		
		wildType_ = new DenseDoubleMatrix1D(numGenes_);
		for (int i=0; i<numGenes_; i++)
			wildType_.set(i, grn_.getGene(i).getMax());
	}
	
	
	// ----------------------------------------------------------------------------

	/** Restore the values before perturbations were applied */
	public void restoreWildType() {
		
		for (int i=0; i<numGenes_; i++)
			grn_.getGene(i).setMax( wildType_.get(i) );
	}
}
