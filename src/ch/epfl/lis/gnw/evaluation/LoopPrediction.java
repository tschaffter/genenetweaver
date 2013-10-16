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
import ch.epfl.lis.gnw.GraphUtilities;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Node;


/** 
 * Analyze prediction confidence of feedback loops and strongly connected components
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class LoopPrediction extends NetworkPrediction {
	
	/** Ranks assigned to edges that are not part of a strongly connected component */
	private ArrayList<Double> insideLoops_ = null;
	/** Ranks assigned to edges in strongly connected components */
	private ArrayList<Double> outsideLoops_ = null;
	/** Ranks assigned to edges in two-node feedback loops */
	private ArrayList<Double> twoNodeLoops_ = null;
    
	/** The strongly connected components of the gold standard */
	ArrayList<ArrayList<Node>> components_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
    /** Copy constructor */
    public LoopPrediction(NetworkPrediction c) {
    	
    	super(c);
    	initializeInsideOutsideLoops();
    	initializeTwoNodeLoops();
    }

    
    // ============================================================================
	// PRIVATE METHODS

    /** Initialize insideLoops_ and outsideLoops_ */
    private void initializeInsideOutsideLoops() {
    
    	insideLoops_ = new ArrayList<Double>();
    	outsideLoops_ = new ArrayList<Double>();
    	
    	GraphUtilities util = new GraphUtilities(goldStandard_);
    	components_ = util.getStronglyConnectedComponents();
    	
    	ArrayList<Edge> edges = goldStandard_.getEdges(); 
    	for (int i=0; i<edges.size(); i++) {
    		Edge e = edges.get(i);
    		int source = goldStandard_.getIndexOfNode(e.getSource());
    		int target = goldStandard_.getIndexOfNode(e.getTarget());
    		
    		if (isInsideLoop(e))
    			insideLoops_.add(R_[target][source]);
    		else
    			outsideLoops_.add(R_[target][source]);
    	}
		
		assert insideLoops_.size() + outsideLoops_.size() == goldStandard_.getNumEdges();
		
    }

    
	// ----------------------------------------------------------------------------

    /** Returns true if both the source and the target of this edge are in the same strongly connected component */
    private boolean isInsideLoop(Edge e) {
    	
    	String source = e.getSource().getLabel();
    	String target = e.getTarget().getLabel();
    	
    	for (int i=0; i<components_.size(); i++) {
    		ArrayList<Node> comp = components_.get(i);
    		
    		boolean sourceFound = false;
    		boolean targetFound = false;
    		
    		for (int n=0; n<comp.size(); n++) {
    			if (comp.get(n).getLabel().equals(source))
    				sourceFound = true;
    			if (comp.get(n).getLabel().equals(target))
    				targetFound = true;
    		}
    		
    		if (sourceFound && targetFound)
    			return true;
    		
    		// Somehow that didn't work
    		//if (comp.contains(source) && comp.contains(target))
    			//return true;
    	}
    	return false;
    }
    
    
    // ----------------------------------------------------------------------------
    
    /** Initialize twoNodeLoops_ */
    private void initializeTwoNodeLoops() {  
    	
    	twoNodeLoops_ = new ArrayList<Double>();

    	GraphUtilities util = new GraphUtilities(goldStandard_);
		boolean[][] A = util.getAdjacencyMatrix();

		for (int i=0; i<A.length; i++) {
			for (int j=0; j<i; j++) {
				if (A[i][j] && A[j][i]) {
					twoNodeLoops_.add(R_[i][j]);
					twoNodeLoops_.add(R_[j][i]);
				}
			}
		}
    }

    
	// ============================================================================
	// SETTERS AND GETTERS

    public ArrayList<Double> getInsideLoops() { return insideLoops_; }
    public ArrayList<Double> getOutsideLoops() { return outsideLoops_; }
    public ArrayList<Double> getTwoNodeLoops() { return twoNodeLoops_; }

}
