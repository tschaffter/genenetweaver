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

import java.util.ArrayList;


/**
 * 
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class BackgroundPrediction extends NetworkPrediction {

	/** The ranks assigned to the true edges of the gold standard */
	private ArrayList<Double> trueEdges_ = null;
	/** The ranks assigned to the back edges (edges i->j that are not part of the gold standard, but j->i is) */
	private ArrayList<Double> backEdges_ = null;
	/** The ranks assigned to the absent edges of the gold standard (excluding back edges, which are listed separately) */
	private ArrayList<Double> absentEdges_ = null;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
    /** Copy constructor */
    public BackgroundPrediction(NetworkPrediction c) {
    	
    	super(c);
    	initializeRankLists();
    }

    
	// ============================================================================
	// PRIVATE METHODS

    /** Initialize the rank lists (trueEdges_, backEdges_, and absentEdges_) */
    private void initializeRankLists() {
    
    	boolean predictAutoregulatoryInteractions = false;
    	if (R_[0][0] != -1)
    		predictAutoregulatoryInteractions = true;
    	
    	trueEdges_ = new ArrayList<Double>();
    	backEdges_ = new ArrayList<Double>();
    	absentEdges_ = new ArrayList<Double>();
    	
		for (int i=0; i<numGenes_; i++) {
			for (int j=0; j<numGenes_; j++) {
				// Continue if it's a self-loop and if they are not being predicted
				if (i == j && !predictAutoregulatoryInteractions)
					continue;
				
				if (A_[i][j])
					trueEdges_.add(R_[i][j]);
				else if (!A_[i][j] && A_[j][i])
					backEdges_.add(R_[i][j]);
				else if (!A_[i][j] && !A_[j][i])
					absentEdges_.add(R_[i][j]);
				else
					assert false;
			}
		}
		
		int numPossibleEdges = numGenes_*numGenes_;
		if (!predictAutoregulatoryInteractions)
			numPossibleEdges -= numGenes_;
		
		assert trueEdges_.size() == goldStandard_.getNumEdges();
		assert trueEdges_.size() + backEdges_.size() + absentEdges_.size() == numPossibleEdges;
		
    }

    
	// ============================================================================
	// SETTERS AND GETTERS

    public ArrayList<Double> getTrueEdges() { return trueEdges_; }
    public ArrayList<Double> getBackEdges() { return backEdges_; }
    public ArrayList<Double> getAbsentEdges() { return absentEdges_; }

}
