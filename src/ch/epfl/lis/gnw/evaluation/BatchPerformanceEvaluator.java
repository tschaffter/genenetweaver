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

import ch.epfl.lis.gnwgui.NetworkDesktop;
import ch.epfl.lis.imod.ImodNetwork;


/**
 * Batch performance evaluator for a set of networks. If the corresponding flags
 * are set, it plots the ROC and PR curves and computes the area under the curves
 * for each network. The network-motif and edge-type analysis are done over the
 * complete set of networks (not for every network separately).
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
public class BatchPerformanceEvaluator {
	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(NetworkDesktop.class.getName());
	
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

	/** There is an evaluator for every gold standard */
	private ArrayList<PerformanceEvaluator> evaluators_ = null;
	/** Background prediction confidence */
	private BackgroundAnalysis backgroundAnalyzer_ = null;
	/** Does the network motif analysis */
	private MotifAnalysis motifAnalyzer_ = null;
	/** Does the loop analysis */
	private LoopAnalysis loopAnalyzer_ = null;
	/** Calculate ROC AND PR */
	private ScoreAnalysis scoreAnalyzer_ = null;
	
	/** Name used for saving results of analysis */
	private String predictionName_ = "";
		
	// ============================================================================
	// PUBLIC METHODS
	
    /**
     * Constructor
     */
    public BatchPerformanceEvaluator(ArrayList<ImodNetwork> goldStandards) {
    	
    	initializeEvaluators(goldStandards);
    }

    
	// ----------------------------------------------------------------------------
    
    /**
     * Instantiate all the evaluators.
     */
    public void initializeEvaluators(ArrayList<ImodNetwork> goldStandards) {
    	
    	evaluators_ = new ArrayList<PerformanceEvaluator>();
    	    		
    	for (int i=0; i<goldStandards.size(); i++) {
    		PerformanceEvaluator eval = new PerformanceEvaluator(goldStandards.get(i),
    																plotROC_,
    																AUROC_,
    																plotPR_,
    																AUPR_,
    																networkMotifAnalysis_,
    																edgeTypeAnalysis_);
    		
    		evaluators_.add(eval);
    	}
    }    
    

    // ----------------------------------------------------------------------------

    public void loadPredictions(ArrayList<URL> predictionFiles, boolean predictAutoregulatoryInteractions) {
    
    	// check that the number of predictions fits the number of gold standards
    	// (there may be zero predictions for a challenge)
    	if (predictionFiles.size() != 0 && predictionFiles.size() != evaluators_.size())
    		log_.log(Level.WARNING, "The number of predictions doesn't match the number of gold standards");
    	    	
    	for (int i=0; i<predictionFiles.size(); i++)
    		evaluators_.get(i).loadPrediction( predictionFiles.get(i), predictAutoregulatoryInteractions );
    		
    }

    
	// ----------------------------------------------------------------------------

    /**
     * Run performance analysis
     */
    public void run() {
    	
    	for (int i=0; i<evaluators_.size(); i++)
    		evaluators_.get(i).run();
    	
    	if (networkMotifAnalysis_ || edgeTypeAnalysis_)
    		backgroundAnalyzer_ = new BackgroundAnalysis(evaluators_);
    	
    	if (networkMotifAnalysis_) {
    		motifAnalyzer_ = new MotifAnalysis(this);
        	motifAnalyzer_.run();
    	}
    	
    	if (loopAnalysis_)
    		loopAnalyzer_ = new LoopAnalysis(evaluators_);
    	
    	if( plotROC_ || plotPR_) {
    		scoreAnalyzer_ = new ScoreAnalysis(this);
    		scoreAnalyzer_.run();
    	}	
    }
    
    
	// ----------------------------------------------------------------------------

    /** Set the name used as prefix when saving results of the evaluation */
    public void setPredictionName(String name) {
       	
    	predictionName_ = name;
    	
    	for (int i=0; i<evaluators_.size(); i++)
    		evaluators_.get(i).setPredictionName(name);
    }
        
    
	// ----------------------------------------------------------------------------

    /** Save all results to files */
    public void save() {
    	
    	if (networkMotifAnalysis_)
    		motifAnalyzer_.save(predictionName_);
    	if (loopAnalysis_)
    		loopAnalyzer_.save(predictionName_);
    }
    
    
	// ============================================================================
	// PRIVATE METHODS

    
	// ============================================================================
	// SETTERS AND GETTERS
	
	public boolean getPlotROC() { return plotROC_; }
	public boolean getAUROC_() { return AUROC_; }
	public boolean getPlotPR() { return plotPR_; }
	public boolean getAUPR() { return AUPR_; }
	public boolean getNetworkMotifAnalysis() { return networkMotifAnalysis_; }
	public boolean getEdgeTypeAnalysis() { return edgeTypeAnalysis_; }
	
	/** Also set the corresponding flag of all evaluators_ */
	public void setPlotROC(boolean plotROC) { 
		plotROC_ = plotROC;
		for (int i=0; i<evaluators_.size(); i++)
			evaluators_.get(i).setPlotROC(plotROC);
	}

	/** Also set the corresponding flag of all evaluators_ */
	public void setAUROC(boolean AUROC) { 
		AUROC_ = AUROC; 
		for (int i=0; i<evaluators_.size(); i++)
			evaluators_.get(i).setAUROC(AUROC);
	}
	
	/** Also set the corresponding flag of all evaluators_ */
	public void setPlotPR(boolean plotPR) { 
		plotPR_ = plotPR; 
		for (int i=0; i<evaluators_.size(); i++)
			evaluators_.get(i).setPlotPR(plotPR);
	}
	
	/** Also set the corresponding flag of all evaluators_ */
	public void setAUPR(boolean AUPR) { 
		AUPR_ = AUPR; 
		for (int i=0; i<evaluators_.size(); i++)
			evaluators_.get(i).setAUPR(AUPR);
	}
	
	/** Also set the corresponding flag of all evaluators_ */
	public void setNetworkMotifAnalysis(boolean networkMotifAnalysis) { 
		networkMotifAnalysis_ = networkMotifAnalysis;
		for (int i=0; i<evaluators_.size(); i++)
			evaluators_.get(i).setNetworkMotifAnalysis(networkMotifAnalysis);
	}
	
	/** Also set the corresponding flag of all evaluators_ */
	public void setEdgeTypeAnalysis(boolean edgeTypeAnalysis) { 
		edgeTypeAnalysis_ = edgeTypeAnalysis; 
		for (int i=0; i<evaluators_.size(); i++)
			evaluators_.get(i).setEdgeTypeAnalysis(edgeTypeAnalysis);
	}
	
	/** Also set the corresponding flag of all evaluators_ */
	public void setLoopAnalysis(boolean loopAnalysis) { 
		loopAnalysis_ = loopAnalysis; 
		for (int i=0; i<evaluators_.size(); i++)
			evaluators_.get(i).setLoopAnalysis(loopAnalysis);
	}
	
	public ArrayList<PerformanceEvaluator> getEvaluators() { return evaluators_; }
	public BackgroundAnalysis getBackgroundAnalyzer() { return backgroundAnalyzer_; }
	public MotifAnalysis getMotifAnalyzer() { return motifAnalyzer_; }
	public ScoreAnalysis getScoreAnalyzer() { return scoreAnalyzer_; }

}
