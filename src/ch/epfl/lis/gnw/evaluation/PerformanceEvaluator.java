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

import ch.epfl.lis.imod.ImodNetwork;


/** Class used to do a "local" performance analysis of a set of predictions.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
public class PerformanceEvaluator extends NetworkPrediction {
	
	/** Set true to plot the ROC curves */
	boolean plotROC_ = false;
	/** Set true to compute the area under the ROC curves */
	boolean AUROC_ = false;
	/** Set true to plot the PR curves */
	boolean plotPR_ = false;
	/** Set true to compute the area under the PR curve */
	boolean AUPR_ = false;
	/** Set true to perform network motif analysis */
	boolean networkMotifAnalysis_ = false;
	/** Set true to perform edge type analysis (won't be added to the GUI) */
	boolean edgeTypeAnalysis_ = false;
	/** Set true to analyze prediction confidence of loops */
	boolean loopAnalysis_ = false;

	/** For the network motif analysis */
	MotifPrediction motifPrediction_ = null;
	/** For the edge type analysis */
	EdgeTypePrediction edgeTypePrediction_ = null;
	/** Background prediction confidences (mainly used for motif analysis) */
	BackgroundPrediction backgroundPrediction_ = null;
	/** Analyze prediction confidence of loops */
	LoopPrediction loopPrediction_ = null;
	
	
	/** GNW3: ADD CLASSES FOR ROC AND PR METRICS */
	Score score_ = null;
	// ...
	// ...
	
	
	// ============================================================================
	// PUBLIC METHODS
	
    /**
     * Constructor
     */
    public PerformanceEvaluator(ImodNetwork goldStandard, boolean plotROC, boolean AUROC, boolean plotPR, boolean AUPR, boolean networkMotifAnalysis, boolean edgeTypeAnalysis) {
    	
    	super();
    	
    	initialize(goldStandard);
    	
    	plotROC_ = plotROC;
    	AUROC_ = AUROC;
    	plotPR_ = plotPR;
    	AUPR_ = AUPR;
    	networkMotifAnalysis_ = networkMotifAnalysis;
    	edgeTypeAnalysis_ = edgeTypeAnalysis;
    }

    
	// ----------------------------------------------------------------------------

    /**
     * Run performance analysis
     */
    public void run() {
    	
    	// The background prediction confidence
    	if (networkMotifAnalysis_ || edgeTypeAnalysis_)
        	backgroundPrediction_ = new BackgroundPrediction(this);

    	if (networkMotifAnalysis_) {
        	motifPrediction_ = new MotifPrediction(this);
        	motifPrediction_.motifProfile();
    		//motifPrediction_.save();
    	}
    	
    	if (edgeTypeAnalysis_) {
        	edgeTypePrediction_ = new EdgeTypePrediction(this);
        	//edgeTypeEvaluator_.rankVsDegree();
    		//edgeTypeEvaluator_.rankVsRegulatoryEffects();
    		//edgeTypeEvaluator_.falsePositives();
    		//edgeTypeEvaluator_.save();
    	}
    	
    	if (loopAnalysis_) {
    		loopPrediction_ = new LoopPrediction(this);
    	}
    	
    	
     	// GNW3: complete for ROC and PR
     	// ...
    	if( plotROC_ || plotPR_ || AUPR_ || AUROC_) {
    		score_ = new Score(this);
    	}
    }

    	
	// ============================================================================
	// SETTERS AND GETTERS

	public boolean getPlotROC() { return plotROC_; }
	public void setPlotROC(boolean plotROC) { plotROC_ = plotROC; }

	public boolean getAUROC_() { return AUROC_; }
	public void setAUROC(boolean AUROC) { AUROC_ = AUROC; }

	public boolean getPlotPR() { return plotPR_; }
	public void setPlotPR(boolean plotPR) { plotPR_ = plotPR; }

	public boolean getAUPR() { return AUPR_; }
	public void setAUPR(boolean AUPR) { AUPR_ = AUPR; }

	public boolean getNetworkMotifAnalysis() { return networkMotifAnalysis_; }
	public void setNetworkMotifAnalysis(boolean networkMotifAnalysis) { networkMotifAnalysis_ = networkMotifAnalysis; }

	public boolean getEdgeTypeAnalysis() { return edgeTypeAnalysis_; }
	public void setEdgeTypeAnalysis(boolean edgeTypeAnalysis) { edgeTypeAnalysis_ = edgeTypeAnalysis; }

	public boolean getLoopAnalysis() { return loopAnalysis_; }
	public void setLoopAnalysis(boolean loopAnalysis) { loopAnalysis_ = loopAnalysis; }

	
	public MotifPrediction getMotifPrediction() { return motifPrediction_; } 
	public EdgeTypePrediction getEdgeTypePrediction() { return edgeTypePrediction_; }
	public BackgroundPrediction getBackgroundPrediction() { return backgroundPrediction_; }
	public LoopPrediction getLoopPrediction() { return loopPrediction_; }
	public Score getScore() { return score_; }

}
