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

import jsc.independentsamples.MannWhitneyTest;

import ch.epfl.lis.gnw.Parser;


/**
 * Implements the network motif analysis. Read also the comments for the class MotifDefinitions.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class MotifAnalysis {

	/** The motif predictions for each network of the batch */
	private ArrayList<MotifPrediction> motifPredictions_ = null;
	
	/** The total number of instances for each motif */
	int[] numMotifInstances_ = null;
	/** The total number of non-overlapping instances for each motif */
	int[] numNonOverlappingMotifInstances_ = null;
	/** The average indegree and outdegree of the three nodes for every motif type */
	ArrayList<ArrayList<Double>> avgDegrees_ = null;
	
	/** The background confidence */
	BackgroundAnalysis background_ = null;

	/** Median prediction confidence (rank) for the motif edges */
	private ArrayList<ArrayList<Double>> medianRanks_ = null;
	/** Divergence of median prediction confidence (rank) for the motif edges */
	private ArrayList<ArrayList<Double>> divMedianRanks_ = null;
	/** P-values for the divergence */
	private ArrayList<ArrayList<Double>> pvals_ = null;
	
	/** Motif definitions */
	private MotifDefinitions mdef_ = MotifDefinitions.getInstance();
	
	
	// ============================================================================
	// PUBLIC METHODS
	
    /**
     * Constructor
     */
    public MotifAnalysis(BatchPerformanceEvaluator batch) {
    	
    	background_ = batch.getBackgroundAnalyzer();
    	motifPredictions_ = new ArrayList<MotifPrediction>();
    	
    	ArrayList<PerformanceEvaluator> evaluators = batch.getEvaluators();
    	for (int i=0; i<evaluators.size(); i++)
    		motifPredictions_.add(evaluators.get(i).getMotifPrediction());
    }
    
    // ----------------------------------------------------------------------------
    
    /** Run the network motif analysis (compute medianRanks_, divMedianRanks_, and pvals_) */
    @SuppressWarnings("unchecked")
	public void run() {
    	
    	int numEdgeTypes = mdef_.getNumEdgeTypes();
    	int numMotifTypes = mdef_.getNumMotifTypes();
    	
    	// initialize numMotifInstances_ and avgDegrees_
    	countMotifInstances();
    	computeAvgDegrees();
    	
    	medianRanks_ = new ArrayList< ArrayList<Double> >();
    	divMedianRanks_ = new ArrayList< ArrayList<Double> >();
    	pvals_ = new ArrayList< ArrayList<Double> >();
    	
    	// A vector of zeros, used for initializion below
    	ArrayList<Double> minusOnes = new ArrayList<Double>();
    	for (int i=0; i<numEdgeTypes; i++)
    		minusOnes.add(-1.0);
    	
    	for (int m=0; m<numMotifTypes; m++) {
    		medianRanks_.add((ArrayList<Double>) minusOnes.clone());
    		divMedianRanks_.add((ArrayList<Double>) minusOnes.clone());
    		pvals_.add((ArrayList<Double>) minusOnes.clone());
    	}
    	
    	// for every motif, do the analysis
    	for (int m=0; m<numMotifTypes; m++)
    		analyzeMotif(m);
    }
    
    
	// ----------------------------------------------------------------------------

    /** Save all results to files */
    public void save(String name) {

    	// Write medians
    	String filename = name + "_motif_confidence.tsv";
		Parser.writeTSV(filename, Parser.toListOfStringArrays(medianRanks_));
		
		// Write divergences
		filename = name + "_motif_confidence_divergence.tsv";
		Parser.writeTSV(filename, Parser.toListOfStringArrays(divMedianRanks_));

		// Write p-values
		filename = name + "_motif_pvalues.tsv";
		Parser.writeTSV(filename, Parser.toListOfStringArrays(pvals_));
		
		// Write number of motif instances
		filename = name + "_motif_instances.tsv";
		
		ArrayList<String[]> out = new ArrayList<String[]>();
		String[] line1 = new String[numMotifInstances_.length];
		String[] line2 = new String[numMotifInstances_.length];
		out.add(line1);
		out.add(line2);
		
		for (int i=0; i<numMotifInstances_.length; i++) {
			line1[i] = Integer.toString(numMotifInstances_[i]);
			line2[i] = Integer.toString(numNonOverlappingMotifInstances_[i]);
		}

		Parser.writeTSV(filename, out);

		// Write avg degrees
		filename = name + "_motif_avg_degrees.tsv";
		Parser.writeTSV(filename, Parser.toListOfStringArrays(avgDegrees_));
    }

    
	// ============================================================================
	// PRIVATE METHODS

    /** Count the number of instances for each motif (initialize numMotifInstances) */
    public void countMotifInstances() {
    	
    	int M = mdef_.getNumMotifTypes();

    	// Initialize at 0
    	numMotifInstances_ = new int[M];
    	numNonOverlappingMotifInstances_ = new int[M];
    	
    	for (int i=0; i<M; i++) {
    		numMotifInstances_[i] = 0;
    		numNonOverlappingMotifInstances_[i] = 0;
    	}
    	
    	// For all networks of the batch, count the instances
    	for (int i=0; i<motifPredictions_.size(); i++) {    		
    		ArrayList<ArrayList<Double>> motifRanks = motifPredictions_.get(i).getMotifRanks();
			for (int k=0; k<motifRanks.size(); k++)
				numMotifInstances_[(int) Math.round(motifRanks.get(k).get(0))]++;
			
			ArrayList<Integer> nonOverlapping_i = motifPredictions_.get(i).getNumNonOverlappingInstances();
			
			for (int k=0; k<M; k++)
				numNonOverlappingMotifInstances_[k] += nonOverlapping_i.get(k);
    	}
    }
    
    
	// ----------------------------------------------------------------------------

    /** 
     * Compute the average indegree and outdegree for the nodes of every motif type.
     * (Quick and dirty, we don't take symmetry into account).
     */
    private void computeAvgDegrees() {
    	
    	int M = mdef_.getNumMotifTypes();

    	// Initialize at 0
    	avgDegrees_ = new ArrayList<ArrayList<Double>>();
    	for (int m=0; m<M; m++) {
    		ArrayList<Double> zeros = new ArrayList<Double>();
    		for (int n=0; n<6; n++)
    			zeros.add(0.0);
    		avgDegrees_.add(zeros);
    	}
    	
    	// For all networks of the batch, count the instances
    	for (int i=0; i<motifPredictions_.size(); i++) {
			ArrayList<ArrayList<Double>> degrees_i = motifPredictions_.get(i).getMotifDegrees();
			
			for (int m=0; m<M; m++)
				for (int n=0; n<6; n++)
					avgDegrees_.get(m).set(n, avgDegrees_.get(m).get(n) + degrees_i.get(m).get(n+1));
    	}
    	
    	// Divide by the number of instances
    	for (int m=0; m<M; m++)
    		for (int n=0; n<6; n++)
    			avgDegrees_.get(m).set(n, avgDegrees_.get(m).get(n) / numMotifInstances_[m]);
    			
    }
    
    
	// ----------------------------------------------------------------------------
    
    /** Perform network motif analysis for motif m */
    private void analyzeMotif(int m) {

    	if (numMotifInstances_[m] == 0)
    		return;
    	
    	int numEdgeTypes = mdef_.getNumEdgeTypes();
    	
    	// The ranks assigned to the different edge types of this motif
    	ArrayList<ArrayList<Double>> ranks = new ArrayList<ArrayList<Double>>();
    	for (int i=0; i<numEdgeTypes; i++)
    		ranks.add(new ArrayList<Double>());
    	    	
    	// For all networks of the batch, aggregate the ranks of this motif type in the six vectors
    	for (int i=0; i<motifPredictions_.size(); i++) {
			
    		ArrayList<ArrayList<Double>> ranksAllMotifs = motifPredictions_.get(i).getMotifRanks();
			
			for (int k=0; k<ranksAllMotifs.size(); k++) {
				ArrayList<Double> mk = ranksAllMotifs.get(k);

				if (mk.get(0) == m) {
					for (int l=0; l<numEdgeTypes; l++)
						ranks.get(l).add(mk.get(l+1));					
				}
			}
    	}
    	
    	// For symmetrical motifs, some edge types are equivalent
    	concatenateEquivalentEdges(m, ranks);
    	
    	// Note, for symmetrical motifs we call the function twice on the same vector,
    	// that's no problem because the second time the function will not change anything 
    	for (int i=0; i<numEdgeTypes; i++)
    		MathUtils.correctRanks(ranks.get(i));
    	    	
    	// Compute medians
    	for (int i=0; i<numEdgeTypes; i++)
    		medianRanks_.get(m).set(i, MathUtils.median(ranks.get(i)));
    	    	
    	ArrayList<Integer> indexTrueEdges = mdef_.getIndexTrueEdges(m);
    	ArrayList<Integer> indexBackEdges = mdef_.getIndexBackEdges(m);
    	ArrayList<Integer> indexAbsentEdges = mdef_.getIndexAbsentEdges(m);
    	
    	// Divergence for the true edges
    	computeDivergence(m, indexTrueEdges, ranks, 
    						background_.getTrueEdgesCorrectedArray(), 
    						background_.getMedianTrueEdges());
    	
    	// Divergence for the back edges
    	computeDivergence(m, indexBackEdges, ranks, 
							background_.getBackEdgesCorrectedArray(), 
							background_.getMedianBackEdges());
    	
    	// Divergence for the absent edges
    	computeDivergence(m, indexAbsentEdges, ranks, 
							background_.getAbsentEdgesCorrectedArray(), 
							background_.getMedianAbsentEdges());
    	
    }
    
    
	// ----------------------------------------------------------------------------
    
    /** If two edge types are equivalent (symmetry), their ranks are concatenated */
    private void concatenateEquivalentEdges(int m, ArrayList<ArrayList<Double>> ranks) {
    	
    	// Symmetrical motifs have only three types of edges
    	switch (m) {

    	// Bilateral symmetry
    	case 0: 
    	case 1:
    	case 5:
    	case 8:
    	case 9:
    		// 0->1 and 0->2
    		ranks.get(mdef_._01).addAll(ranks.get(mdef_._02));
    		ranks.set(mdef_._02, ranks.get(mdef_._01));
    		
    		// 1->0 and 2->0
    		ranks.get(mdef_._10).addAll(ranks.get(mdef_._20));
    		ranks.set(mdef_._20, ranks.get(mdef_._10));

        	// 1->2 and 2->1
    		ranks.get(mdef_._12).addAll(ranks.get(mdef_._21));
    		ranks.set(mdef_._21, ranks.get(mdef_._12));
    		break;
    	
    	// Loop
    	case 7:
    		// 0->1 and 1->2 and 2->0
    		ranks.get(mdef_._01).addAll(ranks.get(mdef_._12));
    		ranks.get(mdef_._01).addAll(ranks.get(mdef_._20));
    		ranks.set(mdef_._12, ranks.get(mdef_._01));
    		ranks.set(mdef_._20, ranks.get(mdef_._01));
    		
    		// 1->0 and 2->1 and 0->2
    		ranks.get(mdef_._10).addAll(ranks.get(mdef_._21));
    		ranks.get(mdef_._10).addAll(ranks.get(mdef_._02));
    		ranks.set(mdef_._21, ranks.get(mdef_._10));
    		ranks.set(mdef_._02, ranks.get(mdef_._10));
    		break;
    		
    	// Fully connected triad
    	case 12:
    		// All edges are equivalent
    		ranks.get(mdef_._01).addAll(ranks.get(mdef_._02));
    		ranks.get(mdef_._01).addAll(ranks.get(mdef_._10));
    		ranks.get(mdef_._01).addAll(ranks.get(mdef_._12));
    		ranks.get(mdef_._01).addAll(ranks.get(mdef_._20));
    		ranks.get(mdef_._01).addAll(ranks.get(mdef_._21));
    		
    		ranks.set(mdef_._02, ranks.get(mdef_._01));
    		ranks.set(mdef_._10, ranks.get(mdef_._01));
    		ranks.set(mdef_._12, ranks.get(mdef_._01));
    		ranks.set(mdef_._20, ranks.get(mdef_._01));
    		ranks.set(mdef_._21, ranks.get(mdef_._01));
    		break;
    	}
    }

        
	// ----------------------------------------------------------------------------

    /** Compute divergence and pvalues */
    private void computeDivergence(int m, ArrayList<Integer> indexes, 
    								ArrayList<ArrayList<Double>> ranks, 
    								double[] backgroundRanks,
    								double backgroundMedian) {
    	
    	// Median and divergence for motif m
    	ArrayList<Double> median = medianRanks_.get(m);
    	ArrayList<Double> divMedian = divMedianRanks_.get(m);
    	ArrayList<Double> pvals = pvals_.get(m);
    	double sp = 0.0;

    	for (int i=0; i<indexes.size(); i++) {
    		int k = indexes.get(i);
    		divMedian.set(k, median.get(k) - backgroundMedian);
    		
    		if (ranks.get(k).size() > 1) {
    			MannWhitneyTest ranksum = new MannWhitneyTest(
    											MathUtils.toArray(ranks.get(k)),
    											backgroundRanks);
    			
    			// BUG FIXED
    			// Overflow problem led to p-value = 2 !!
    			// Discovered by Thomas
    			// Fixed by Thomas
    			sp = ranksum.getSP();
    			pvals.set(k, (sp < 1e-200 || sp == 2.0) ? 0.0 : sp);
    		}
    	}
    }
    
    
	// ============================================================================
	// SETTERS AND GETTERS
	
	public int[] getNumMotifInstances() { return numMotifInstances_; }
	public ArrayList<ArrayList<Double>> getMedianRanks() { return medianRanks_; }
	public ArrayList<ArrayList<Double>> getDivMedianRanks() { return divMedianRanks_; }
	public ArrayList<ArrayList<Double>> getPvals() { return pvals_; }

}
