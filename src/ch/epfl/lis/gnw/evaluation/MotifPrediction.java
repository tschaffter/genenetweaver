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
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * This class is used to analyze prediction performance in the context of network motifs
 * (only three-node motifs, no autoregulatory loops).
 * 
 * The representation of motifs is defined in MotifDefinitions.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class MotifPrediction extends NetworkPrediction {

	/**
	 * For every motif of the gold standard, we list the ranks of the 6 possible links.
	 * The first column is reserved for the motif number. This table will be used to
	 * compare the prediction confidences of links pertaining to different motif types
	 * (median, divergence of median, etc.)
	 */
	private ArrayList< ArrayList<Double> > motifRanks_ = null;
	
	/**
	 * The table motifRanks_ lists the prediction confidence for links of every motif instance,
	 * but contains no information about overlapping motifs. The following table defines for
	 * every edge, which motifs it belongs to, and which type of edge it is in these motifs.
	 * edgeTypes[i][j][m][k] is the number of times edge i->j is an edge of type k in
	 * motif m. E.g., if edge i->j is the first link of two cascades i->j->k and i->j->l, then 
	 * edgeTypes_[i][j][2][0]=2 (cascades have index 2 and the first link of a cascade has 
	 * index 0, see the introductory comment for the class above).
	 */
	//private int[][][][] edgeTypes_ = null;
	
	/** For every motif, the number of non-overlapping instances */
	private ArrayList<Integer> numNonOverlappingInstances_ = null;
	/** For every motif, the number of instances (including overlapping instances) */
	private ArrayList<Integer> numInstances_ = null;
	/** For every motif, the nodes that are part of at least one instance (used to count the non-overlapping instances) */
	private ArrayList<TreeSet<Integer>> motifNodes_ = null;
	
	/** 
	 * For every motif, the number of occurrences and the indegree and outdegree of its nodes (total count, 
	 * to be divided by the first column to get the average degrees).
	 */
	private ArrayList<ArrayList<Double>> motifDegrees_ = null;

    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(MotifPrediction.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
    /** Constructor */
    public MotifPrediction() {}
    
    /** Copy constructor */
    public MotifPrediction(NetworkPrediction c) {
    	
    	super();
    	copy(c);	
    }


    // ----------------------------------------------------------------------------

    /**
     * Count / identify all triads (three-node motifs) in the network.
     */
    public void motifProfile() {
    	
    	// Initialize
    	init();
    	
    	// In order not to count the same motif several times, we associate each motif
    	// with a single node. This is the node that has the smallest index of the three nodes.
    	
    	// Count the motifs of node g (only those in which i has the smallest index of the three nodes)
    	for (int g=0; g<numGenes_; g++) {
    		
    		// Construct the set of neighbors N of g with index larger than g
    		ArrayList<Integer> N = new ArrayList<Integer>();
    		
    		for (int i=g+1; i<numGenes_; i++)
    			if (A_[g][i] || A_[i][g])
    				N.add(i);
    		
    		// Count the motifs with the two nodes {g,n} (only those that do not include a neighbor of
    		// g with a smaller index than n)
    		for (int nIndex=0; nIndex<N.size(); nIndex++) {
    			int n = N.get(nIndex); // the node n
    			
    			// Construct the set of all neighbors M of {g,n}. However, don't include neighbors of g
    			// with a smaller index than n (they have already been done in a previous iteration of this loop)
        		ArrayList<Integer> M = new ArrayList<Integer>();
        		
        		// neighbors of g with index larger than n
        		for (int i=nIndex+1; i<N.size(); i++)
        			M.add( N.get(i) );
        		
        		// neighbors of n with index larger than g
        		// the neighbors of g have already been added, thus, don't add if it is in N
    			for (int i=g+1; i<numGenes_; i++) {
    				if ((A_[n][i] || A_[i][n]) && i!=n && !N.contains(i))
        				M.add(i);
    			}
     			
    			// now we can count all motifs {g,n,m} with m in M.
    			for (int mIndex=0; mIndex<M.size(); mIndex++) {
    				int m = M.get(mIndex); // the node m
    				
    				// check our assumptions: g should be smaller than n and m
    				assert g<n && g<m;
    				// if m is a neighbor of g, its index must be larger than n 
    				assert (!A_[g][m] && !A_[m][g]) || n<m;
    				
    				int[] nodes = new int[] {g,n,m};
    				countMotif(nodes);
    			}
    		}
    	}
    }
	    
    
	// ----------------------------------------------------------------------------

    /**
     * 
     */
    public void save() {
    	
    	try {
    		
    		String filename = predictionName_ + "_" + goldStandard_.getId() + "_motifProfile.tsv";
    		log_.log(Level.INFO, "Writing file " + filename);
    		FileWriter fw = new FileWriter(filename, false);

    		for (int i=0; i<motifRanks_.size(); i++) {
    			ArrayList<Double> r = motifRanks_.get(i);
    			String line =  r.get(0) + "\t" + r.get(1) + "\t" + r.get(2) + "\t" + r.get(3) + "\t" + r.get(4) + "\t" + r.get(5) + "\t" + r.get(6) + "\n";
    			fw.write(line);
    		}
    		fw.close();
    		
    		
    		filename = goldStandard_.getId() + "_motifAvgDegree.tsv";
			log_.log(Level.INFO, "Writing file " + filename);
    		fw = new FileWriter(filename, false);

    		for (int i=0; i<motifDegrees_.size(); i++) {
    			ArrayList<Double> d = motifDegrees_.get(i);
    			String line =  d.get(0) + "\t" + d.get(1) + "\t" + d.get(2) + "\t" + d.get(3) + "\t" + d.get(4) + "\t" + d.get(5) + "\t" + d.get(6) + "\n";
    			fw.write(line);
    		}
    		fw.close();
    		
    		
    		/*
    		filename = predictionName_ + "_" + goldStandard_.getId() + "_motifEdgeTypes.tsv";
    		log_.log(Level.INFO, "Writing file " + filename);
    		fw = new FileWriter(filename, false);

    		
    		int numMotifTypes = MotifDefinitions.getInstance().getNumMotifTypes();
    		int numEdgeTypes = MotifDefinitions.getInstance().getNumEdgeTypes();
    		
    		// for every edge ...
    		for (int i=0; i<numGenes_; i++) {
    			for (int j=0; j<numGenes_; j++) {
    				// ... except auto-regulatory edges ...
    				if (i == j)
    					continue;

    				// ... write one line, the first column is the prediction confidence,
    				// followed by the motif edge types of this edge
    				String line = Double.toString(R_[j][i]);

    				for (int m=0; m<numMotifTypes; m++)
    					for (int e=0; e<numEdgeTypes; e++)
    						line += "\t" + edgeTypes_[i][j][m][e];
    				line += "\n";
    				fw.write(line);    			
    			}
    		}
    		fw.close();
    		*/
    		
    		/*
    		filename = predictionName_ + "_" + goldStandard_.getId() + "_falsePositives.tsv";
    		log.log(Level.INFO, "Writing file " + filename);
    		fw = new FileWriter(filename, false);

    		for (int i=0; i<falsePositives_.size(); i++)
    			fw.write(falsePositives_.get(i)[0] + "\t" + falsePositives_.get(i)[1] + "\n");
    		fw.close();
			*/

    	} catch (Exception e) {
    		log_.log(Level.WARNING, "Error saving motif predictions:" + e.getMessage(), e);
    	}
    }

    
	// ============================================================================
	// PRIVATE METHODS
    
    /** Count the occurrence of the given triad and update the prediction statistics accordingly */
    private void countMotif(int[] nodes) {
    	
    	int m = getMotifNumber(nodes);
    	int id = MotifDefinitions.getInstance().getMotifId(m);
    	int[] map = MotifDefinitions.getInstance().getMotifNodeAssociation(m);
    	
    	// reorder the nodes according to the map
    	int[] reorderedNodes = new int[3];
    	reorderedNodes[ map[0] ] = nodes[0];
    	reorderedNodes[ map[1] ] = nodes[1];
    	reorderedNodes[ map[2] ] = nodes[2];
    	int a = reorderedNodes[0];
    	int b = reorderedNodes[1];
    	int c = reorderedNodes[2];
    	
    	// assert that the given nodes actually correspond to the specified motif
    	assert assertMotif(a, b, c, id);
    	
    	// Count the non-overlapping motif instances
    	countNonOverlappingInstance(nodes, id);
    	// Count all motif instances (even overlapping instances)
    	// TODO: update this counter at the same time than non-overlapping instances
    	countInstance(nodes, id);
    	
    	try
    	{
	    	// Add a new row to motifRanks for this motif instance. The first
	    	// column is the motif id, followed by the ranks of the six edge types.
	    	ArrayList<Double> edgeRanks = new ArrayList<Double>();
	    	edgeRanks.add((double)id);
	    	edgeRanks.add(R_[b][a]);
	    	edgeRanks.add(R_[c][a]);
	    	edgeRanks.add(R_[a][b]);
	    	edgeRanks.add(R_[c][b]);
	    	edgeRanks.add(R_[a][c]);
	    	edgeRanks.add(R_[b][c]);
	    	
	    	motifRanks_.add(edgeRanks);
	    	
	    	// for every possible edge of this motif, increase the corresponding counter in edgeTypes_
	    	/*
	    	edgeTypes_[a][b][id][0]++;
	    	edgeTypes_[a][c][id][1]++;
	    	edgeTypes_[b][a][id][2]++;
	    	edgeTypes_[b][c][id][3]++;
	    	edgeTypes_[c][a][id][4]++;
	    	edgeTypes_[c][b][id][5]++;
	    	*/
	    	
	    	// update the degree statistics. this is a hack and should not be done here
	    	// (should be done only once when loading the goldstandard and not every time
	    	// a prediction is analyzed)
	    	ArrayList<Double> avgDegree = motifDegrees_.get(id);
	    	avgDegree.set(0, avgDegree.get(0) + 1); // count the motif
	    	avgDegree.set(1, avgDegree.get(1) + indegree_[a]);
	    	avgDegree.set(2, avgDegree.get(2) + indegree_[b]);
	    	avgDegree.set(3, avgDegree.get(3) + indegree_[c]);
	    	avgDegree.set(4, avgDegree.get(4) + outdegree_[a]);
	    	avgDegree.set(5, avgDegree.get(5) + outdegree_[b]);
	    	avgDegree.set(6, avgDegree.get(6) + outdegree_[c]);
    	}
    	catch (NullPointerException e)
    	{
    		// do nothing
    		// We are here most likely because the motif prediction is run without having any prediction loaded.
    		// This is the case when one wants only to count the number of motifs in a gold standard.
    	}
    }
    
    
	// ----------------------------------------------------------------------------

    /**
     * Count the given motif instance if none of its nodes is part of such a motif yet.
     * (It's an approximate implementation, because we just use it as a quick and dirty 
     * way of getting a sense how many "independent" samples of a motif there are.
     */
    private void countNonOverlappingInstance(int[] nodes, int id) {
    	
    	// If one of these nodes is already part of such a motif, return
    	for (int i=0; i<nodes.length; i++)
    		if (motifNodes_.get(id).contains(nodes[i]))
    			return;
    	
    	// Otherwise, count this nonoverlapping instance
    	numNonOverlappingInstances_.set(id, numNonOverlappingInstances_.get(id)+1);
    	
    	for (int i=0; i<nodes.length; i++)
    		motifNodes_.get(id).add(nodes[i]);
    }
    	
    // ----------------------------------------------------------------------------
    
    /**
     * Count the given motif instance EVEN IF at least one of its nodes is already part of such a motif.
     * (It's an approximate implementation, because we just use it as a quick and dirty 
     * way of getting a sense how many "independent" samples of a motif there are.
     */
    private void countInstance(int[] nodes, int id)
    {	
    	// Otherwise, count this nonoverlapping instance
    	numInstances_.set(id, numInstances_.get(id)+1);
    }
    	
	// ----------------------------------------------------------------------------

    /**
     * Get the id, which is the number corresponding to the binary string:
     * ab ac ba bc ca cb, where ij=1 if the there is a link from i to j.
     */
    private int getMotifNumber(int[] nodes) {
    	
    	int a = nodes[0];
    	int b = nodes[1];
    	int c = nodes[2];
    	
    	// check that {a,b,c} is actually a connected triad
    	assert A_[a][b] || A_[b][a] || A_[a][c] || A_[c][a]; // a is connected with b or c
    	assert A_[b][a] || A_[a][b] || A_[b][c] || A_[c][b]; // b is connected with a or c
    	assert A_[c][a] || A_[a][c] || A_[c][b] || A_[b][c]; // c is connected with a or b
    	
    	int id = 0;
    	if (A_[b][c])
    		id += 1;
    	if (A_[a][c])
    		id += 2;
    	if (A_[c][b])
    		id += 4;
    	if (A_[a][b])
    		id += 8;
    	if (A_[c][a])
    		id += 16;
    	if (A_[b][a])
    		id += 32;
    	
    	return id;
    }
    

	// ----------------------------------------------------------------------------

    /** Assert that the given nodes correspond to the specified motif */
    private boolean assertMotif(int a, int b, int c, int id) {
        	
    	switch (id) {
    	case 0:  assert  A_[b][a] &&  A_[c][a] && !A_[a][b] && !A_[c][b] && !A_[a][c] && !A_[b][c]; break;
    	case 1:  assert !A_[b][a] && !A_[c][a] &&  A_[a][b] && !A_[c][b] &&  A_[a][c] && !A_[b][c]; break;
    	case 2:  assert  A_[b][a] && !A_[c][a] && !A_[a][b] &&  A_[c][b] && !A_[a][c] && !A_[b][c]; break;
    	case 3:  assert !A_[b][a] &&  A_[c][a] && !A_[a][b] &&  A_[c][b] &&  A_[a][c] && !A_[b][c]; break;
    	case 4:  assert !A_[b][a] &&  A_[c][a] && !A_[a][b] && !A_[c][b] &&  A_[a][c] &&  A_[b][c]; break;
    	case 5:  assert  A_[b][a] &&  A_[c][a] &&  A_[a][b] && !A_[c][b] &&  A_[a][c] && !A_[b][c]; break;
    	case 6:  assert  A_[b][a] &&  A_[c][a] && !A_[a][b] &&  A_[c][b] && !A_[a][c] && !A_[b][c]; break;
    	case 7:  assert  A_[b][a] && !A_[c][a] && !A_[a][b] &&  A_[c][b] &&  A_[a][c] && !A_[b][c]; break;
    	case 8:  assert  A_[b][a] &&  A_[c][a] && !A_[a][b] &&  A_[c][b] && !A_[a][c] &&  A_[b][c]; break;
    	case 9:  assert !A_[b][a] && !A_[c][a] &&  A_[a][b] &&  A_[c][b] &&  A_[a][c] &&  A_[b][c]; break;
    	case 10: assert  A_[b][a] &&  A_[c][a] && !A_[a][b] &&  A_[c][b] &&  A_[a][c] && !A_[b][c]; break;
    	case 11: assert  A_[b][a] &&  A_[c][a] &&  A_[a][b] && !A_[c][b] &&  A_[a][c] &&  A_[b][c]; break;
    	case 12: assert  A_[b][a] &&  A_[c][a] &&  A_[a][b] &&  A_[c][b] &&  A_[a][c] &&  A_[b][c]; break;
    	default: assert false;
    	}
    	return true;
    }
        
    
	// ----------------------------------------------------------------------------

    /**
     * Initialize a bunch of stuff
     */
    private void init() {
    	
    	MotifDefinitions def = MotifDefinitions.getInstance();
    	int numMotifTypes = def.getNumMotifTypes();
    	int numEdgeTypes = def.getNumEdgeTypes();
    	
    	// initialize motifRanks_ and edgeTypes_
    	motifRanks_ =  new ArrayList< ArrayList<Double> >();
    	/*
    	edgeTypes_ = new int[numGenes_][numGenes_][numMotifTypes][numEdgeTypes];
    	for (int i=0; i<numGenes_; i++)
    		for (int j=0; j<numGenes_; j++)
    			for (int m=0; m<numMotifTypes; m++)
    				for (int e=0; e<numEdgeTypes; e++)
    					edgeTypes_[i][j][m][e] = 0;
    	*/
    	
    	// initialize motifAvgDegree_
    	motifDegrees_ = new ArrayList< ArrayList<Double> >();
    	for (int i=0; i<numMotifTypes; i++) {
    		ArrayList<Double> d = new ArrayList<Double>();
    		for (int j=0; j<numEdgeTypes+1; j++)
    			d.add(0.0);
    		motifDegrees_.add(d);
    	}
    	
    	// initialize numNonOveralppingInstances_ and motifNodes_
    	numNonOverlappingInstances_ = new ArrayList<Integer>();
    	numInstances_ = new ArrayList<Integer>();
    	motifNodes_ = new ArrayList<TreeSet<Integer>>();
    	for (int i=0; i<numMotifTypes; i++) {
    		numNonOverlappingInstances_.add(0);
    		numInstances_.add(0);
    		motifNodes_.add(new TreeSet<Integer>());
    	}
     }
    
    // ----------------------------------------------------------------------------
   
    /**
     * Save to file the number of non-overlapping instances for each motifs 
     */
    public void saveCountNonOverlappingInstances(String filename) {
    	
    	try {
    		if (filename.compareTo("") == 0)
    			filename = predictionName_ + "_" + goldStandard_.getId() + "_numNonOverlappingMotifs.tsv";
    		log_.log(Level.INFO, "Writing file " + filename);
    		FileWriter fw = new FileWriter(filename, false);
    		
    		for (int m = 0; m < numNonOverlappingInstances_.size(); m++)
    			fw.write(numNonOverlappingInstances_.get(m) + "\n");

    		fw.close();

    	} catch (Exception e) {
    		log_.log(Level.WARNING, "Error saving number of non-overlapping motif instances:" + e.getMessage(), e);
    	}
    }
    
    // ----------------------------------------------------------------------------
    
    /**
     * Save to file the number of non-overlapping instances for each motifs 
     */
    public void saveCountInstances(String filename) {
    	
    	try {
    		if (filename.compareTo("") == 0)
    			filename = predictionName_ + "_" + goldStandard_.getId() + "_numMotifs.tsv";
    		log_.log(Level.INFO, "Writing file " + filename);
    		FileWriter fw = new FileWriter(filename, false);
    		
    		for (int m = 0; m < numInstances_.size(); m++)
    			fw.write(numInstances_.get(m) + "\n");

    		fw.close();

    	} catch (Exception e) {
    		log_.log(Level.WARNING, "Error saving number of motif instances:" + e.getMessage(), e);
    	}
    }
    
	// ============================================================================
	// GETTERS AND SETTERS

    public ArrayList<ArrayList<Double>> getMotifRanks() { return motifRanks_; }
    public ArrayList<Integer> getNumNonOverlappingInstances() { return numNonOverlappingInstances_; }
    public ArrayList<Integer> getNumInstances() { return numInstances_; }
    public ArrayList<ArrayList<Double>> getMotifDegrees() { return motifDegrees_; }

}
