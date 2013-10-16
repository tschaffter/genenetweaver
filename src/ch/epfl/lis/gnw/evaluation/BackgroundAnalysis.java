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
public class BackgroundAnalysis {

	/** The ranks assigned to the true edges of the batch */
	private ArrayList<Double> trueEdgesCorrected_ = null;
	/** The ranks assigned to the back edges (edges i->j that are not part of the gold standard, but j->i is) */
	private ArrayList<Double> backEdgesCorrected_ = null;
	/** The ranks assigned to the absent edges of the batch (excluding back edges, which are listed separately) */
	private ArrayList<Double> absentEdgesCorrected_ = null;
	
	/** Same as trueEdgesCorrected_, but as array (the statistical package used in the motif analysis uses arrays) */
	private double[] trueEdgesCorrectedArray_ = null;
	/** Same as backEdgesCorrected_, but as array (the statistical package used in the motif analysis uses arrays) */
	private double[] backEdgesCorrectedArray_ = null;
	/** Same as absentEdgesCorrected_, but as array (the statistical package used in the motif analysis uses arrays) */
	private double[] absentEdgesCorrectedArray_ = null;

	/** The median rank assigned to the true edges of the gold standard */
	private double medianTrueEdges_ = -1;
	/** The median rank assigned to the back edges (edges i->j that are not part of the gold standard, but j->i is) */
	private double medianBackEdges_ = -1;
	/** The median rank assigned to the absent edges of the gold standard (excluding back edges, which are listed separately) */
	private double medianAbsentEdges_ = -1;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
    /** 
	 * Constructor
     */
    public BackgroundAnalysis(ArrayList<PerformanceEvaluator> evaluators_) {
    	
    	trueEdgesCorrected_ = new ArrayList<Double>();
    	backEdgesCorrected_ = new ArrayList<Double>();
    	absentEdgesCorrected_ = new ArrayList<Double>();
    	
    	// Concatenate the vectors of each network of the batch
    	for (int i=0; i<evaluators_.size(); i++) {
    		BackgroundPrediction pred = evaluators_.get(i).getBackgroundPrediction();
    		trueEdgesCorrected_.addAll(pred.getTrueEdges());
    		backEdgesCorrected_.addAll(pred.getBackEdges());
    		absentEdgesCorrected_.addAll(pred.getAbsentEdges());
    	}
    	
    	// Correct the ranks
    	MathUtils.correctRanks(trueEdgesCorrected_);
    	MathUtils.correctRanks(backEdgesCorrected_);
    	MathUtils.correctRanks(absentEdgesCorrected_);
    	
    	// Compute the medians
    	medianTrueEdges_ = MathUtils.median(trueEdgesCorrected_);
    	medianBackEdges_ = MathUtils.median(backEdgesCorrected_);
    	medianAbsentEdges_ = MathUtils.median(absentEdgesCorrected_);

    	// Convert to arrays
		trueEdgesCorrectedArray_ = MathUtils.toArray(trueEdgesCorrected_);
		backEdgesCorrectedArray_ = MathUtils.toArray(backEdgesCorrected_);
		absentEdgesCorrectedArray_ = MathUtils.toArray(absentEdgesCorrected_);

    }


	// ============================================================================
	// SETTERS AND GETTERS

    public double getMedianTrueEdges() { return medianTrueEdges_; }
    public double getMedianBackEdges() { return medianBackEdges_; }
    public double getMedianAbsentEdges() { return medianAbsentEdges_; }

    public double[] getTrueEdgesCorrectedArray() { return trueEdgesCorrectedArray_; }
    public double[] getBackEdgesCorrectedArray() { return backEdgesCorrectedArray_; }
    public double[] getAbsentEdgesCorrectedArray() { return absentEdgesCorrectedArray_; }
    
}
