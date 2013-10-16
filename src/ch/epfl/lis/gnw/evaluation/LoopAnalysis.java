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

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsc.independentsamples.MannWhitneyTest;

import ch.epfl.lis.gnw.Parser;


/**
 * 
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class LoopAnalysis {

	/** The ranks assigned to the edges within strongly connected components of the batch */
	private ArrayList<Double> insideLoopsCorrected_ = null;
	/** The ranks assigned to edges outside strongly connected components */
	private ArrayList<Double> outsideLoopsCorrected_ = null;
	/** The ranks assigned to two-node feedback loops */
	private ArrayList<Double> twoNodeLoopsCorrected_ = null;
	
	/** Median rank assigned to the edges inside loops */
	private double medianInsideLoops_ = -1;
	/** Median rank assigned to edges outside loops */
	private double medianOutsideLoops_ = -1;
	/** Median rank assigned to two-node feedback loops */
	private double medianTwoNodeLoops_ = -1;
	
	/** P-value of divergence inside vs outside loops */
	private double pvalInsideVsOutside_ = -1;
	/** P-value of divergence two-node feedback vs outside loops */
	private double pvalTwoNodeVsOutside_ = -1;
	
	/** Logger for this class */
    private static Logger log_ = Logger.getLogger(LoopAnalysis.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
    /** 
	 * Constructor
     */
    public LoopAnalysis(ArrayList<PerformanceEvaluator> evaluators_) {
    	
    	insideLoopsCorrected_ = new ArrayList<Double>();
    	outsideLoopsCorrected_ = new ArrayList<Double>();
    	twoNodeLoopsCorrected_ = new ArrayList<Double>();
    	
    	// Concatenate the vectors of each network of the batch
    	for (int i=0; i<evaluators_.size(); i++) {
    		LoopPrediction pred = evaluators_.get(i).getLoopPrediction();
    		insideLoopsCorrected_.addAll(pred.getInsideLoops());
    		outsideLoopsCorrected_.addAll(pred.getOutsideLoops());
    		twoNodeLoopsCorrected_.addAll(pred.getTwoNodeLoops());
    	}
    	
    	// Correct the ranks
    	MathUtils.correctRanks(insideLoopsCorrected_);
    	MathUtils.correctRanks(outsideLoopsCorrected_);
    	MathUtils.correctRanks(twoNodeLoopsCorrected_);
    	
    	// Compute the medians
    	medianInsideLoops_ = MathUtils.median(insideLoopsCorrected_);
    	medianOutsideLoops_ = MathUtils.median(outsideLoopsCorrected_);
    	medianTwoNodeLoops_ = MathUtils.median(twoNodeLoopsCorrected_);

    	if (insideLoopsCorrected_.size() > 1 && outsideLoopsCorrected_.size() > 1) {
			MannWhitneyTest ranksum = new MannWhitneyTest(
											MathUtils.toArray(insideLoopsCorrected_),
											MathUtils.toArray(outsideLoopsCorrected_));
			pvalInsideVsOutside_ = ranksum.getSP();
		}
    	
    	if (twoNodeLoopsCorrected_.size() > 1 && outsideLoopsCorrected_.size() > 1) {
			MannWhitneyTest ranksum = new MannWhitneyTest(
											MathUtils.toArray(twoNodeLoopsCorrected_),
											MathUtils.toArray(outsideLoopsCorrected_));
			pvalTwoNodeVsOutside_ = ranksum.getSP();
		}
    }

    
	// ----------------------------------------------------------------------------

    /** Save all results to files */
    public void save(String name) {

    	String filename = name + "_loop_statistics.tsv";
    	
		try {
			FileWriter fw = new FileWriter(filename, false);
		
			String line =  "---         \tnumEdges\tmedian\tpval\n";
			fw.write(line);
			
			line = "outsideLoops\t" + outsideLoopsCorrected_.size() + "\t" + medianOutsideLoops_ + "\t---\n";
			fw.write(line);
			
			line = "insideLoops \t" + insideLoopsCorrected_.size() + "\t" + medianInsideLoops_ + "\t" + pvalInsideVsOutside_ + "\n";
			fw.write(line);
			
			line = "twoNodeLoops\t" + twoNodeLoopsCorrected_.size() + "\t" + medianTwoNodeLoops_ + "\t" + pvalTwoNodeVsOutside_ + "\n";
			fw.write(line);
			fw.close();
			
			filename = name + "_inside_loops.tsv";
			Parser.writeTSV(filename, Parser.toListOfStringArray(insideLoopsCorrected_));
			
			filename = name + "_outside_loops.tsv";
			Parser.writeTSV(filename, Parser.toListOfStringArray(outsideLoopsCorrected_));
			
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not write file " + filename, e);
		}
    }

    
	// ============================================================================
	// SETTERS AND GETTERS

    
    
}
