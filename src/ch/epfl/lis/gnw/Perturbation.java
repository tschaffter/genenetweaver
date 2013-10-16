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
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.networks.ios.TSVParser;


/**
 * Class offers functionalities to apply perturbations to a gene network. 
 * The following types of perturbations are offered: single-gene, two-gene,
 * and multifactorial. In single-gene perturbation, only one m_i is perturbed at
 * a time. For two-gene perturbations, two m_i are perturbed. 
 * @author Daniel Marbach
 */
public abstract class Perturbation {
	
	/** The gene network to which the perturbations are being applied */
	protected GeneNetwork grn_ = null;
	/** The size of the network */
	protected int numGenes_ = -1;
	/** The wild-type (so that it can be restored after applying perturbations) */
	protected DoubleMatrix1D wildType_ = null;
	/** The number of different multifactorial perturbations */
	protected int numPerturbations_ = -1;
	/** perturbations_(k, i) is the perturbed value of m_i in perturbation k */
	protected DoubleMatrix2D perturbations_ = null;
	
	/** Logger for this class */
    protected static Logger log_ = Logger.getLogger(Perturbation.class.getName());
	
    
    // ============================================================================
	// ABSTRACT METHODS
    
	/** Apply the k'th perturbation to the grn_ */
	public abstract void applyPerturbation(int k);
	/** Save the wild-type of the network grn_ in wildType_ */
	protected abstract void saveWildType();
	/** Restore the values before perturbations were applied */
	public abstract void restoreWildType();
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public Perturbation() { }
	
	
	/** Constructor with initialization */
	public Perturbation(GeneNetwork grn) {
		grn_ = grn;
		numGenes_ = grn_.getSize();
	}
	

	// ----------------------------------------------------------------------------
	
	/**
	 * Print the perturbations to a file 
	 */
	public void printPerturbations(String directory, String postfix) {
		
		try {
			String filename = directory + grn_.getId() + "_" + postfix + "_perturbations.tsv";
			log_.log(Level.INFO, "Writing file " + filename);
			FileWriter fw = new FileWriter(filename, false);
			
			// Header
			fw.write(grn_.getHeader(false));
			
			// Perturbations
			for (int p=0; p<numPerturbations_; p++) {
				for (int i=0; i<numGenes_-1; i++)
					fw.write(Double.toString(perturbations_.get(p, i)) + "\t");
				fw.write(Double.toString(perturbations_.get(p, numGenes_-1)) + "\n");
			}

			// Close file
			fw.close();

		} catch (IOException fe) {
			log_.log(Level.WARNING, "MultifactorialPerturbation.printPerturbations(): " + fe.getMessage(), fe);
			throw new RuntimeException();
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Load perturbations from the given file.
	 */
	public void loadPerturbations(String directory, String label) {
		
		try
		{
			URL url = GnwSettings.getInstance().getURL(directory + grn_.getId() + "_" + label + "_perturbations.tsv");
			ArrayList<String[]> data = TSVParser.readTSV(url);
			// the first line is the header
			numPerturbations_ = data.size() - 1;
			perturbations_ = new DenseDoubleMatrix2D(numPerturbations_, numGenes_);
			
			// get the wild-type from the network
			saveWildType();
			
			for (int l=1; l<data.size(); l++) {
				String[] line = data.get(l);
				for (int i=0; i<line.length; i++)
					perturbations_.set(l-1, i, Double.valueOf(line[i]));
			}

		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load perturbations: " + e.getMessage(), e);
			throw new RuntimeException("Could not load perturbations");
		}
	}
	

	// ============================================================================
	// SETTERS AND GETTERS
	
	public DoubleMatrix2D getPerturbations() { return perturbations_; }
	public int getNumPerturbations() { return numPerturbations_; }
	
}
