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

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.random.Uniform;



/**
 *
 */
public class PerturbationDrug extends PerturbationMultifactorial {

	/** The name of the drug / perturbation */
	private String name_ = "";
	/** The id */
	private int id_ = -1;
	/** The lowest level of this perturbation in the compendium (used for normalization) */
	private double minLevel_ = -1;
	/** The highest level of this perturbation in the compendium (used for normalization) */
	private double maxLevel_ = -1;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	PerturbationDrug(String name, GeneNetwork grn) {
		super(grn);
		name_ = name;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Perturb at least GnwSettings.minNumberDirectDrugTargets_ TFs and at most the fraction
	 * of TFs specified by GnwSettings.maxFractionDirectDrugTargets_.
	 * 
	 */
	public void drugPerturbation() {
		
		GnwSettings set = GnwSettings.getInstance();
		Uniform uniform = set.getUniformDistribution();
		
		saveWildType();
		
		numPerturbations_ = 1;
		perturbations_ = new DenseDoubleMatrix2D(numPerturbations_, numGenes_);
		perturbations_.assign(0);
		
		// Choose the number of targets
		int minTargets = (int) (grn_.getNumTfs() * set.getMinFractionDirectTargets());
		int maxTargets = (int) (grn_.getNumTfs() * set.getMaxFractionDirectTargets());
		int numTargets = uniform.nextIntFromTo(minTargets, maxTargets); 
		
		for (int t=0; t<numTargets; t++) {
			// Choose the target (it may happen that a target is chosen twice, we don't care
			int tfIndex = grn_.getRandomTf();
			Gene tf = grn_.getGene(tfIndex);
			assert tf.getIsTf();
			
			double basal = tf.getBasalActivation();
			double delta;
			if (basal < 0.1)
				delta = 1 - basal;
			else if (basal > 0.9)
				delta = -basal;
			else {
				// Randomly set to 0 or 1
				if (uniform.nextIntFromTo(0, 1) == 0)
					delta = 1 - basal;
				else
					delta = -basal;
			}
			perturbations_.set(0, tfIndex, delta);
		}
	}

	
	// ----------------------------------------------------------------------------

	/** Set this as the new maxLevel_ if it is bigger than the previous one */
	public void updateRange(double level) {
		
		if (level == -1) // -1 means no level specified
			return;
		else if (level < 0)
			throw new IllegalArgumentException("Levels must be positive");
		
		// The first time the range is updated
		if (minLevel_ == -1) {
			assert maxLevel_ == -1;
			minLevel_ = level;
			maxLevel_ = level;
		
		} else if (level > maxLevel_) {
			maxLevel_ = level;
			
		} else if (level < minLevel_) {
			minLevel_ = level;
		}		
	}
	
	
	// ----------------------------------------------------------------------------

	/** Return a string array of the perturbation vector */
	public String[] toStringArray() {

		String[] data = new String[numGenes_];
		
		for (int i=0; i<numGenes_; i++)
			data[i] = Double.toString(perturbations_.get(0, i));
		
		return data;
	}

	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getName() { return name_; }
	public void setName(String name) { name_ = name; }

	public int getId() { return id_; }
	public void setId(int id) { id_ = id; }

	public double getMaxLevel() { return maxLevel_; }
	public void setMaxLevel(double maxLevel) { maxLevel_ = maxLevel; }

}
