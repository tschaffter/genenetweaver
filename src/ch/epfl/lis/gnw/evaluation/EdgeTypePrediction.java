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

import ch.epfl.lis.gnw.HillGene;
import ch.epfl.lis.gnw.RegulatoryModule;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Structure;


/** Analyzes the prediction performance per network motif
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
public class EdgeTypePrediction extends NetworkPrediction {
	
	/** For every edge in goldStandard_, the rank, the indegree, and the outdegree */
	private double[][] rankVsDegree_;
	
	/**
	 * For every edge i, rankVsRegulatoryEffects_[i] gives:
	 * - The rank in the list of predictions
	 * - The sign
	 * - isDeactivator (1 if the edge is a deactivator of a module, 0 if not)
	 * - bindsAsComplex (true if the edge is part of a complex)
	 * - The Hill coefficient
	 * - The number of regulatory modules of the target gene
	 * - The number of target gene inputs that are independent of the it
	 * - The number of target gene inputs that act synergistically with it
	 * 
	 * All fields are initialized with setGoldStandard(), except the rank of course.
	 */
	private double[][] rankVsRegulatoryEffects_;
		
	/** Logger for this class */
    private static Logger log_ = Logger.getLogger(EdgeTypePrediction.class.getName());
	
    
	// ============================================================================
	// PUBLIC METHODS
	
    /** Constructor */
    public EdgeTypePrediction() { 
    	super();
    }
    
    /** Copy constructor */
    public EdgeTypePrediction(NetworkPrediction c) {
    	super(c);	
    	init(goldStandard_);
    }

    
	// ----------------------------------------------------------------------------


    /**
     * 
     */
    public void rankVsDegree() {
    	
    	int numEdges = goldStandard_.getNumEdges();
    	
    	for (int i=0; i<numEdges; i++) {
    		Edge edge = goldStandard_.getEdge(i);
			int source = goldStandard_.getIndexOfNode(edge.getSource());
			int target = goldStandard_.getIndexOfNode(edge.getTarget());
			
			double rank = R_[target][source];
			int truePositive = 0;
			if (rank <= numEdges)
				truePositive = 1;
			
			rankVsDegree_[i][0] = rank;
			rankVsDegree_[i][3] = truePositive;
    	}
    }

    
	// ----------------------------------------------------------------------------

    /**
     * 
     */
    public void rankVsRegulatoryEffects() {
    	
    	int numEdges = goldStandard_.getNumEdges();
    	
    	for (int i=0; i<numEdges; i++) {
    		Edge edge = goldStandard_.getEdge(i);
			int source = goldStandard_.getIndexOfNode(edge.getSource());
			int target = goldStandard_.getIndexOfNode(edge.getTarget());
			
			double rank = R_[target][source];
			int truePositive = 0;
			if (rank <= numEdges)
				truePositive = 1;
			
			rankVsRegulatoryEffects_[i][0] = rank;
			rankVsRegulatoryEffects_[i][8] = truePositive;
    	}
    }
    
    
	// ----------------------------------------------------------------------------

    /**
     * 
     */
    public void save() {
    	
    	try {
    		
    		String filename = predictionName_ + "_" + goldStandard_.getId() + "_rankVsDegree.tsv";
    		log_.log(Level.INFO, "Writing file " + filename);
    		FileWriter fw = new FileWriter(filename, false);

    		for (int i=0; i<rankVsDegree_.length; i++) {
    			String line = rankVsDegree_[i][0] + "\t" + rankVsDegree_[i][1] + "\t" + rankVsDegree_[i][2] + "\t" + rankVsDegree_[i][3] + "\n";
    			fw.write(line);
    		}
    		fw.close();

    		filename = predictionName_ + "_" + goldStandard_.getId() + "_rankVsRegulatoryEffects.tsv";
    		log_.log(Level.INFO, "Writing file " + filename);
    		fw = new FileWriter(filename, false);

    		for (int i=0; i<rankVsRegulatoryEffects_.length; i++) {
    			//Edge edge = goldStandard_.getEdge(i);
    			//fw.write(edge.getSource().getLabel() + "\t" + edge.getTarget().getLabel());

    			fw.write("" + rankVsRegulatoryEffects_[i][0]);
    			for (int j=1; j<rankVsRegulatoryEffects_[i].length; j++)
    				fw.write("\t" + rankVsRegulatoryEffects_[i][j]);
    			fw.write("\n");
    		}
    		fw.close();

    	} catch (Exception e) {
    		log_.log(Level.WARNING, "Error when trying to save edge type predictions: " + e.getMessage(), e);
    	}
    }
    
    
	// ============================================================================
	// PRIVATE METHODS

    /**
     * Initialize rankVsDegree_, rankVsRegulatoryEffects_
     */
    private void init(Structure goldStandard) {
    	    	
    	int numEdges = goldStandard.getNumEdges();
    	rankVsDegree_ = new double[numEdges][4];
    	rankVsRegulatoryEffects_ = new double[numEdges][9];
    	
		for (int i=0; i<numEdges; i++) {
			Edge edge = goldStandard.getEdge(i);
			HillGene source = (HillGene) edge.getSource();
			HillGene target = (HillGene) edge.getTarget();

			int numInputs = target.getInputGenes().size();
			int inputIndexOfThisEdge = target.getInputGenes().indexOf(source);
			int moduleOfThisEdge = -1;
			ArrayList<RegulatoryModule> modules = target.getRegulatoryModules();
			int lastGeneOfThisModule = -1;
			
			for (int m=0; m<modules.size(); m++) {
				lastGeneOfThisModule += modules.get(m).getNumInputs();
				if (inputIndexOfThisEdge <= lastGeneOfThisModule) {
					moduleOfThisEdge = m;
					break;
				}
			}
			assert moduleOfThisEdge != -1;
			
			RegulatoryModule module = modules.get(moduleOfThisEdge);
			int moduleInputIndex = module.getNumInputs() - (lastGeneOfThisModule - inputIndexOfThisEdge) - 1;
			
			int sign = 1;
			if (!module.getEdgeSigns().get(moduleInputIndex))
				sign = -1;
			rankVsRegulatoryEffects_[i][1] = sign;
			
			int deactivator = 0;
			if (moduleInputIndex >= module.getNumActivators())
				deactivator = 1;
			rankVsRegulatoryEffects_[i][2] = deactivator;
			
			int bindsAsComplex = 0;
			if (module.bindsAsComplex())
				bindsAsComplex = 1;
			rankVsRegulatoryEffects_[i][3] = bindsAsComplex;
			
			rankVsRegulatoryEffects_[i][4] = module.getN()[moduleInputIndex];
			rankVsRegulatoryEffects_[i][5] = modules.size();
			rankVsRegulatoryEffects_[i][6] = numInputs - module.getNumInputs();
			rankVsRegulatoryEffects_[i][7] = module.getNumInputs() - 1;
		}
				
		for (int i=0; i<numEdges; i++) {
    		Edge edge = goldStandard_.getEdge(i);
			int source = goldStandard_.getIndexOfNode(edge.getSource());
			int target = goldStandard_.getIndexOfNode(edge.getTarget());
			
			rankVsDegree_[i][1] = indegree_[target];
			rankVsDegree_[i][2] = outdegree_[source];
    	}
    }


}
