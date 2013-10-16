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
import cern.jet.random.Normal;
import cern.jet.random.Uniform;

/**
 *
 */
public class PerturbationMultifactorial extends Perturbation {

	/** The probability that a gene is perturbed (for strong multifactorial perturbations) */
	private double perturbationProbability_ = GnwSettings.getInstance().getPerturbationProbability();;
	/** The standard deviation for the weak multifactorial perturbations */
	private double stdev_ = GnwSettings.getInstance().getMultifactorialStdev();;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	PerturbationMultifactorial() {
		super();
	}
	
	
	PerturbationMultifactorial(GeneNetwork grn) {
		super(grn);
	}

		
	// ----------------------------------------------------------------------------

	/**
	 * The basal transcription rate alpha_0 of all genes is sampled from a normal
	 * distribution with mean alpha_0 and standard deviation CV_.
	 */
	public void multifactorialAllGenesWeak(int numPerturbations) {
		
		saveWildType();
		
		numPerturbations_ = numPerturbations;
		perturbations_ = new DenseDoubleMatrix2D(numPerturbations_, numGenes_);

		Normal normal = GnwSettings.getInstance().getNormalDistribution();
		
		// generate perturbations
		for (int p=0; p<numPerturbations_; p++) {
			for (int g=0; g<numGenes_; g++) {
				perturbations_.set(p, g, normal.nextDouble(0, stdev_));
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * A perturbation is applied to a gene with probability perturbationProbability_.
	 * The basal activations alpha_0 are sampled from a uniform distribution in [0 1]
	 * independently of their unperturbed value. 
	 */
	public void multifactorialStrong(int numPerturbations) {
		
		saveWildType();
		
		numPerturbations_ = numPerturbations;
		perturbations_ = new DenseDoubleMatrix2D(numPerturbations_, numGenes_);

		Uniform uniform = GnwSettings.getInstance().getUniformDistribution();

		// generate perturbations
		for (int p=0; p<numPerturbations_; p++) {
			for (int g=0; g<numGenes_; g++) {
				if (uniform.nextDoubleFromTo(0, 1) < perturbationProbability_) {
					// the new basal activation
					double delta = uniform.nextDoubleFromTo(0, 1);
					// but actually we need the difference to the wild-type
					delta = delta - grn_.getGene(g).getBasalActivation();
					perturbations_.set(p, g, delta);
				} else
					perturbations_.set(p, g, 0);
			}
		}
	}
	

	// ----------------------------------------------------------------------------

	/** Apply the k'th perturbation to the grn_ */
	public void applyPerturbation(int k) {
		
		for (int i=0; i<numGenes_; i++)
			grn_.getGene(i).perturbBasalActivation( perturbations_.get(k,i) );
	}

	
	// ----------------------------------------------------------------------------
	
	/** Save the wild-type of the network grn_ in wildType_ */
	protected void saveWildType() {
		
		wildType_ = new DenseDoubleMatrix1D(numGenes_);
		for (int i=0; i<numGenes_; i++)
			wildType_.set(i, 0);
	}
	
	
	// ----------------------------------------------------------------------------

	/** Restore the values before perturbations were applied */
	public void restoreWildType() {
		
		for (int i=0; i<numGenes_; i++)
			grn_.getGene(i).restoreWildTypeBasalActivation();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * The max transcription rates of all genes are sampled from a normal
	 * distribution with mean m_i and standard deviation m_i*CV_.
	 */
	/*
	@Deprecated
	public void multifactorialMax(int numPerturbations) {
		
		perturbBasalActivation_ = false;
		saveWildType();
		
		numPerturbations_ = numPerturbations;
		perturbations_ = new DenseDoubleMatrix2D(numPerturbations_, numGenes_);

		// Generates random numbers from a normal distribution, resamples if value < min=0
		// The max is set to Double.MAX_VALUE. The mean and stdev will have to be set each time 
		RandomParameterGaussian rand = new RandomParameterGaussian(0, Double.MAX_VALUE, 0, 0, false);
		
		// generate perturbations
		for (int p=0; p<numPerturbations_; p++) {
			for (int g=0; g<numGenes_; g++) {
				double m = wildType_.get(g);
				double stdev = CV_*m;
				rand.setMeanStdev(m, stdev);
				perturbations_.set(p, g, rand.getRandomValue());
			}
		}
	}
	*/
}
