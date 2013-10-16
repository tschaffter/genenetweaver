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

package ch.epfl.lis.gnw.evaluation;

import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.epfl.lis.gnw.GraphUtilities;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.ios.TSVParser;


/**
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public abstract class NetworkPrediction {

	/** The gold standard network (this can be just a network structure or a dynamical model) */
	protected ImodNetwork goldStandard_ = null;
	/** The number of genes of the gold standard network */
	protected int numGenes_ = 0;
	/** Number of link to predict */
	protected int maxNumPredictions_ = 0;

	/** 
	 * The adjacency matrix of the gold standard.
	 * Watch out: A[i][j] = true if there is a link from gene *j* to gene i.
	 */
	protected boolean[][] A_ = null;
	/** The indegree of the nodes of the gold standard*/
	protected int[] indegree_ = null;
	/** The outdegree of the nodes of the gold standard */
	protected int[] outdegree_ = null;

	/** 
	 * This name will be appended to the id of the gold standard for the filenames
	 * when saving the results of the evaluation by save()
	 */
	protected String predictionName_ = "";
	
	/** 
	 * The normalized ranks of the link predictions. This is like a connectivity matrix, but it 
	 * contains the ranks of all links in the list of predictions. R_[*j*][i] is the rank of the link
	 * from gene i to gene j. The first link in the list (highest confidence level) has rank 1, the
	 * last link has rank 0. When the list of predictions is incomplete, the absent links are
	 * assigned rank position2rank(# links predicted).
	 */
	protected double[][] R_ = null;
	
	/** Logger for this class */
    private static Logger log_ = Logger.getLogger(NetworkPrediction.class.getName());
	
	// ============================================================================
	// PUBLIC METHODS
	
    /** Constructor */
    public NetworkPrediction() { }
    
    
    /** Copy constructor */
    public NetworkPrediction(NetworkPrediction c) {
    	
    	copy(c);
    }

    
	// ----------------------------------------------------------------------------

    /**
     * Load the network prediction, this must be done after the gold standard has been loaded.
     */
    public void loadPrediction(URL predictionFile, boolean predictAutoregulatoryInteractions) {

    	predictionName_ = FilenameUtilities.getFilenameWithoutPath(predictionFile.getFile());
    	// initialize R_
    	R_ = new double[numGenes_][numGenes_];
       	for (int i=0; i<numGenes_; i++)
    		for (int j=0; j<numGenes_; j++)
    			R_[i][j] = -1;
       	
       	maxNumPredictions_ = numGenes_ * numGenes_;
       	if (!predictAutoregulatoryInteractions)
       		maxNumPredictions_ -= numGenes_;

       	// the vector of gene labels, trust me, we need it later
       	ArrayList<String> geneLabels = goldStandard_.getAllNodesLabels();
       	
       	// parse the .tsv file
    	ArrayList<String[]> data = null;
		try {
			data = TSVParser.readTSV(predictionFile);
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not read prediction file: " + predictionFile.getPath(), e);
		}

		// Counts the predictions (non-empty lines, for some reason in DREAM4 link predictions are
		// separated by empty lines for several teams)
		int numPredictions = 0;
		
    	for (int i=0; i<data.size(); i++) {
    		// the next line
    		String[] line = data.get(i);
    		
    		// skip empty lines
    		if (line.length == 1 && line[0].equals(""))
    			continue;
    		else if (line.length != 3) { // each line must have 3 elments: geneA geneB confidenceLevel
    			log_.log(Level.WARNING, "Line " + (i+1) + " doesn't have three elements");
    			throw new RuntimeException(predictionFile + "\nLine " + (i+1) + " doesn't have three elements.");
    		}

    		// get the index of the regulator (source) and the target
    		int source = geneLabels.indexOf(line[0]);
    		int target = geneLabels.indexOf(line[1]);
    		
    		if (source == -1)
    			throw new RuntimeException("The following label was not found in the gold standard: " + line[0] + "\nFile: " + predictionName_);
    		else if (target == -1)
    			throw new RuntimeException("The following label was not found in the gold standard: " + line[1] + "\nFile: " + predictionName_);
    		else if (!predictAutoregulatoryInteractions && source == target)
    			throw new IllegalArgumentException("The list contains autoregulatory interactions but predictAutoregulatoryInteractions was set false");
    		else if (R_[target][source] != -1)
    			throw new IllegalArgumentException("The link '" + source + " -> " + target + " has been included twice");
    		
    		// set the rank of this link (ranks go from 1 ... 0)
    		//C_[target][source] = (maxNumPredictions_ - i - 1) / (double)(maxNumPredictions_ - 1.0);
    		R_[target][source] = position2rank(numPredictions++);
		}
    	
    	// if not all links are included, we set those that were omitted in the list of predictions
    	// to -minRank. Using -minRank (instead of +minRank) is a mere convention so that we know
    	// afterwards in the analysis that these ranks need to be treated specially. minRank is the
    	// rank that would have been assigned next after the last link that was in the list.
    	if (numPredictions != maxNumPredictions_) {
     		System.out.println("WARNING: not all links included in file " + predictionFile.getFile());
     		double minRank = position2rank(numPredictions);
    		assert minRank >= 0 && minRank < 1;
    		
    		for (int i=0; i<numGenes_; i++) {
    			for (int j=0; j<numGenes_; j++) {
    				if ((i!=j || predictAutoregulatoryInteractions) && R_[i][j] == -1) {
    					R_[i][j] = -minRank;
    				}
    			}
    		}
    	}
    }
    
    
    // ----------------------------------------------------------------------------
    
    /** Convert the position of a link in the prediction list into a rank [0,1] */
    public double position2rank(int position) {
    	if (maxNumPredictions_ <= 0)
    		throw new IllegalArgumentException("maxNumPredictions_ must be initialized");
    	
    	return (maxNumPredictions_ - position - 1) / (double)(maxNumPredictions_ - 1.0);
    }

    
    // ----------------------------------------------------------------------------

    public void resetPredictions() {
    	
    	R_ = null;
    	predictionName_ = "";
    	maxNumPredictions_ = 0;
    }

    
    // ----------------------------------------------------------------------------

    /** Copy the fields of the given network prediction */
    public void copy(NetworkPrediction c) {
    	
    	this.goldStandard_ = c.goldStandard_;
        this.numGenes_ = c.numGenes_;
    	this.maxNumPredictions_ = c.maxNumPredictions_;
    	
    	this.A_ = c.A_;
    	this.indegree_ = c.indegree_;
    	this.outdegree_ = c.outdegree_;
    	
    	this.predictionName_ = c.predictionName_;
    	this.R_ = c.R_;
    }
    

    // ----------------------------------------------------------------------------

    /** Set gold standard and related fields (numGenes_, A_, indegree_, outdegree_) */
    public void initialize(ImodNetwork goldStandard) {
    	
    	goldStandard_ = goldStandard;
    	numGenes_ = goldStandard.getSize();
    	
    	GraphUtilities util = new GraphUtilities(goldStandard);
    	A_ = util.getAdjacencyMatrix();
    	indegree_ = util.getIndegrees();
    	outdegree_ = util.getOutdegrees();
    }
    
    
	// ============================================================================
	// PRIVATE METHODS    

    
	// ============================================================================
	// SETTERS AND GETTERS

    public void setPredictionName(String name) {predictionName_ = name;}
    public String getPredictionName() { return predictionName_; }

    public void setR(double[][] ranks) {R_ = ranks;}
    public double[][] getR() { return R_; }
    
}
