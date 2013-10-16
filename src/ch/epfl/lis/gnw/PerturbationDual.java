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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;


/**
 * 
 */
public class PerturbationDual extends PerturbationSingleGene {

	/** The pairs of genes that are perturbed together */
	private ArrayList<int[]> pairs_;

	
	// ----------------------------------------------------------------------------
	
	/** Constructor */
	public PerturbationDual(GeneNetwork grn) {
		super(grn);
		pairs_ = null;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * 
	 */
	public void dualPerturbations(double perturbation, int numDualPerturbations) {
		
		saveWildType();
		
		GraphUtilities util = new GraphUtilities(grn_);
		pairs_ = util.getSortedCoregulatorPairs();

		if (pairs_.size() < 1)
			throw new RuntimeException("There is no pair of TFs co-regulating a target gene, deselect dualknockouts when generating datasets form this network");
		
		// if there are fewer pairs than numDualKnockouts, do only as many as we have
		if (pairs_.size() < numDualPerturbations)
			numPerturbations_ = pairs_.size();
		else
			numPerturbations_ = numDualPerturbations;
		
		perturbations_ = new DenseDoubleMatrix2D(numPerturbations_, numGenes_);
		// initialize matrix at wild-type (unperturbed)
		for (int p=0; p<numPerturbations_; p++)
			for (int g=0; g<numGenes_; g++)
				perturbations_.set(p, g, wildType_.get(g));
		
		// set the knockouts
		for (int p=0; p<numPerturbations_; p++) {
			int[] ko = pairs_.get(p);
			perturbations_.set(p, ko[0], perturbation*wildType_.get(ko[0]));
			perturbations_.set(p, ko[1], perturbation*wildType_.get(ko[1]));
		}
	}
	
	
//	----------------------------------------------------------------------------
	
	/**
	 * Print the perturbations to a file 
	 */
	public void printPerturbations(String directory, String postfix) {
		
		super.printPerturbations(directory, postfix);
		
		// if a perturbation was loaded, pairs=null
		if (pairs_ != null) {
			try {
				String filename = directory + grn_.getId() + "_" + postfix + "_indexes.tsv";
				log_.log(Level.INFO, "Writing file " + filename);
				FileWriter fw = new FileWriter(filename, false);

				// Header
				fw.write("\"G_i\"\t\"G_j\"\n");

				// Perturbations
				for (int p=0; p<numPerturbations_; p++) {
					int[] pair = pairs_.get(p);
					fw.write((pair[0]+1) + "\t" + (pair[1]+1) + "\n");
				}

				// Close file
				fw.close();

			} catch (IOException fe) {
				log_.log(Level.WARNING, "PerturbationDual.printPerturbations(): " + fe.getMessage(), fe);
				throw new RuntimeException();
			}
		}
	}
	
}
