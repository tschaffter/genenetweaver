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

import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.random.Uniform;


/**
 *
 */
public class PerturbationMixed extends Perturbation {
	
	/** The perturbations applied to the max transcription rates (wild type is saved in Perturbation.wildType_) */
	protected DoubleMatrix1D deltaMax_ = null;
	/** The perturbations applied to the basal transcription rates (wild type is saved by the genes themselves) */
	protected DoubleMatrix1D deltaBasalActivation_ = null;
	
	/** Min efficacy of gene deletions (set to 1.0 to set transcription rates to 0, set to 0.9 for 90% reduction) */
	protected double minGeneDeletionEffect_ = -1;
	/** Max efficacy of gene deletions (set to 1.0 to set transcription rates to 0, set to 0.9 for 90% reduction) */
	protected double maxGeneDeletionEffect_ = -1;
	/** Min efficacy of gene overexpressions (set to 0.5 to increase transcription rates by 50%) */
	protected double minGeneOverexpressionEffect_ = -1;
	/** Max efficacy of gene overexpressions (set to 1.0 to increase transcription rates by 100%) */ 
	protected double maxGeneOverexpressionEffect_ = -1;

	/** Logger for this class */
    protected static Logger log_ = Logger.getLogger(PerturbationMixed.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
	PerturbationMixed(GeneNetwork grn) {
		
		super(grn);
		
		// There's always only one perturbation for this class
		numPerturbations_ = 1;
		
		deltaMax_ = new DenseDoubleMatrix1D(numGenes_);
		deltaMax_.assign(0);
		
		deltaBasalActivation_ = new DenseDoubleMatrix1D(numGenes_);
		deltaBasalActivation_.assign(0);
		
		GnwSettings set = GnwSettings.getInstance();
		minGeneDeletionEffect_ = set.getMinGeneDeletionEffect();
		maxGeneDeletionEffect_ = set.getMaxGeneDeletionEffect();
		minGeneOverexpressionEffect_ = set.getMinGeneOverexpressionEffect();
		maxGeneOverexpressionEffect_ = set.getMaxGeneOverexpressionEffect();

		saveWildType();
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Add the given perturbation vector to the first perturbation. Used in DREAM5 challenge to add
	 * perturbations of conditions and drugs. When called the first time, initializes the perturbations
	 * with the given vector and saves the wild type.
	 */
	public void addToDeltaBasalActivation(DoubleMatrix1D perturbation, double scale) {

		if (perturbation.size() != numGenes_)
			throw new IllegalArgumentException("The given perturbation vector does not match the number of genes");
		
		for (int i=0; i<numGenes_; i++) {
			double delta = deltaBasalActivation_.get(i) + (scale*perturbation.get(i));
			deltaBasalActivation_.set(i, delta);
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/** 
	 * Delete the given gene. If there is no such gene in the network, or if the gene isn't a regulator,
	 * randomly choose a regulator to delete and display a warning message.
	 * Returns the label of the deleted TF. 
	 */
	public void addGeneticPerturbation(String label, boolean isDeletion) {

		Uniform uniform = GnwSettings.getInstance().getUniformDistribution();
		
		// The index of the perturbed gene
		int index = grn_.getIndexOfNode(label);
		
		// If the gene is not part of the compendium, randomly choose a TF to delete
		if (index == -1)
			throw new RuntimeException("Target of genetic perturbation " + label + " is not part of the compendium");
		else if (!grn_.getGene(index).getIsTf())
			throw new RuntimeException("Target of genetic perturbation " + label + " is not a TF");

		// The efficacy of the deletion
		double efficacy;
		double delta;
		if (isDeletion) {
			efficacy = uniform.nextDoubleFromTo(minGeneDeletionEffect_, maxGeneDeletionEffect_);
			delta = - efficacy * wildType_.get(index);
		} else {
			efficacy = uniform.nextDoubleFromTo(minGeneOverexpressionEffect_, maxGeneOverexpressionEffect_);
			delta = efficacy * wildType_.get(index);
		}
		
		deltaMax_.set(index, delta);
	}

	
	// ----------------------------------------------------------------------------

	/** Apply the k'th perturbation to the grn_ */
	public void applyPerturbation(int k) {
		
		for (int i=0; i<numGenes_; i++) {
			grn_.getGene(i).setMax( wildType_.get(i) + deltaMax_.get(i) );
			grn_.getGene(i).perturbBasalActivation( deltaBasalActivation_.get(i) );
		}
	}

	
	// ----------------------------------------------------------------------------
	
	/** Save the wild type max transcription rates (no need to save basal activations, it's done by the genes) */
	protected void saveWildType() {
		
		wildType_ = new DenseDoubleMatrix1D(numGenes_);
		for (int i=0; i<numGenes_; i++)
			wildType_.set(i, grn_.getGene(i).getMax());
	}
	
	
	// ----------------------------------------------------------------------------

	/** Restore wild type max transcription rates and basal activations */
	public void restoreWildType() {
		
		for (int i=0; i<numGenes_; i++) {
			grn_.getGene(i).setMax( wildType_.get(i) );
			grn_.getGene(i).restoreWildTypeBasalActivation();
		}
	}
		
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Load perturbations from the given file.
	 */
	public void loadPerturbations(String label) {
		
		throw new RuntimeException("Not yet implemented");
	}

	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public DoubleMatrix1D getDeltaMax() { return deltaMax_; }
	public DoubleMatrix1D getDeltaBasalActivation() { return deltaBasalActivation_; }


}
