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


/** 
 * Class defining a bunch of useful things for the motif analysis.
 * 
 * There are 13 possible three-node motifs without autoregulatory loops, each motif has
 * an index as defined in the constructor MotifEvaluator(). The common ones are:
 * fan-out (0), fan-in (1), cascade (2), feed-forward loop (6).
 * 
 * Each motif has three nodes, defined as node 0, 1, and 2, and six possible types of edges.
 * The six edges are assigned the following indexes:
 * 0->1 (0), 0->2 (1), 1->0 (2), 1->2 (3), 2->0 (4), and 2->1 (5).
 * 
 * We define a standard way of representing each motif. E.g., for cascades, node 0 is the
 * first node, node 1 is in the middle, and node 2 is at the end of the cascade. In other words,
 * for every instance of a cascade in a network, we label the nodes such that the cascade has
 * links 0->1->2 (i.e., edges of type 0 and 3 are the true links of a cascade). E.g., given a
 * cascade 2->1->0 we would relabel node 2 as node 0, and node 0 as node 2, to map it to the 
 * standard way of representing a cascade. The standard representations and the mappings of any 
 * other representation to the standard representation are defined by initializeMotifIds(),
 * see also motifId_ and motifNodeAssociation_.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class MotifDefinitions {

	/** The unique instance of MotifDefinitions (Singleton design pattern) */
	private static MotifDefinitions instance_ = null;

	/** The number of three-node motif types, c.f. constructor MotifEvaluator() */
	private final int numMotifTypes_ = 13;
	/** The number of possible edges of a three-node motif, c.f. constructor MotifEvaluator() */
	private final int numEdgeTypes_ = 6;
	
	 /** 
	  * A motif has three nodes named 0, 1, and 2. The six possible edges of a motif are indexed 
	  * in the following order (we don't consider self-loops): e_01 e_02 e_10 e_12 e_20 e_21.
	  */
	public final int _01 = 0;
	public final int _02 = 1;
	public final int _10 = 2;
	public final int _12 = 3;
	public final int _20 = 4;
	public final int _21 = 5;
	
	/**
	 * A motif has three nodes named 0, 1, and 2. The connectivity of a motif can be described
	 * by 6 bits: e_01 e_02 e_10 e_12 e_20 e_21, where e_ij = 1 if there is an edge from i to j and
	 * 0 otherwise. This table contains the bit string defining the standard representation for each
	 * of the 13 motifs (e.g., "110000" for the fan-out).
	 */
	private String[]  motifDefs_ = null;
	
	/**
	 * A motif has three nodes named 0, 1, and 2. The connectivity of a motif can be described
	 * by 6 bits: e_01 e_02 e_10 e_12 e_20 e_21, where e_ij = 1 if there is an edge from i to j and
	 * 0 otherwise. Interpreting the 6 bits as a number gives an index m. Several different indexes
	 * m are associated to the same type of motif, one of them is taken as this motifs id.
	 * motifId_[m] is this id. See also comment for the class MotifEvaluator().
	 */
	private int[] motifId_;
	
	/**
	 * motifNodeAssociation_ indicates how the nodes of a motif m must be renamed to transform
	 * it into the motif motifId_[m]. See also comment for the class MotifEvaluator().
	 */
	private int[][] motifNodeAssociation_;
	
	/** For every motif type, the indexes of the true edges */
	private ArrayList<ArrayList<Integer>> indexTrueEdges_;
	/** The indexes of the back edges */
	private ArrayList<ArrayList<Integer>> indexBackEdges_;
	/** The indexes of the absent edges */
	private ArrayList<ArrayList<Integer>> indexAbsentEdges_;
	
	/** Logger for this class */
    private static Logger log_ = Logger.getLogger(MotifDefinitions.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Get the unique instance of MotifDefinitions (Singleton design pattern)
	 */
	static public MotifDefinitions getInstance() {
		
		if (instance_ == null)
			instance_ = new MotifDefinitions();

		return instance_;
	}
	
	// ----------------------------------------------------------------------------
    
    /**
     * Save motifs definitions to file
     */
    static public void saveMotifDefinitions(String filename) {
    	
    	try {
    		if (filename.compareTo("") == 0)
    			filename = "GNW_motif_definitions.txt";
    		log_.log(Level.INFO, "Writing file " + filename);
    		FileWriter fw = new FileWriter(filename, false);
    		
    		String text = "There are 13 possible three-node motifs without autoregulatory loops, each motif has ";
    		text += "an index as defined in the constructor MotifEvaluator(). The common ones are:\n";
    		text += "fan-out (0), fan-in (1), cascade (2), feed-forward loop (6).\n";
    		text += "\n";
    		text += "Each motif has three nodes, defined as node 0, 1, and 2, and six possible types of edges.\n";
    		text += "The six edges are assigned the following indexes:\n";
    		text += "0->1 (0), 0->2 (1), 1->0 (2), 1->2 (3), 2->0 (4), and 2->1 (5).\n";
    		text += "\n";
    		text += "Motif 0: 110000 (fan-out)\n";
    		text += "Motif 1: 001010 (fan-in)\n";
    		text += "Motif 2: 100100 (cascade)\n";
    		text += "Motif 3: 010110\n";
    		text += "Motif 4: 010011\n";
    		text += "Motif 5: 111010\n";
    		text += "Motif 6: 110100 (feed-forward loop; FFL)\n";
    		text += "Motif 7: 100110 (loop)\n";
    		text += "Motif 8: 110101\n";
    		text += "Motif 9: 001111\n";
    		text += "Motif 10: 110110\n";
    		text += "Motif 11: 111011\n";
    		text += "Motif 12: 111111 (fully connected)\n";
    		
    		fw.write(text);
    		fw.close();

    	} catch (Exception e) {
    		log_.log(Level.WARNING, "Error saving motif definitions:" + e.getMessage(), e);
    	}
    }


	// ============================================================================
	// PRIVATE METHODS
	
    /** Constructor */
    private MotifDefinitions() {
    	
    	// initialize motifId_ and motifNodeAssociation_
    	initializeMotifIds();
    	// initialize motifDefs_
    	initializeMotifDefs();
    	// initialize indexTrueEdges_, indexBackEdges_, and indexAbsentEdges_
    	initializeEdgeIndexes();
    }
    
    
	// ----------------------------------------------------------------------------

    /**
     * Initialize motifId_ and motifNodeAssociation_
     */
    private void initializeMotifIds() {
    	    	
    	int[] motif = new int[64];
    	int[][] assoc = new int[64][];
    	for (int i=0; i<motif.length; i++)
    		motif[i] = -1;
    	
    	int id; // the id of the motif
    	int m; // an isomorphic motif to id
    	int[] _012 = new int[] {0,1,2};
    	int[] _021 = new int[] {0,2,1};
    	int[] _102 = new int[] {1,0,2};
    	int[] _120 = new int[] {1,2,0};
    	int[] _201 = new int[] {2,0,1};
    	int[] _210 = new int[] {2,1,0};

    	// motif 1 (fan-out)
    	id = 0; 
    	m = Integer.parseInt("110000", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("001100", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("000011", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	
    	// motif 2 (fan-in)
    	id = 1; 
    	m = Integer.parseInt("001010", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("100001", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("010100", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	
    	// motif 3 (cascade)
    	id = 2;
    	m = Integer.parseInt("100100", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("010001", 2);
    	motif[m] = id;
    	assoc[m] = _021;
    	m = Integer.parseInt("000110", 2);
    	motif[m] = id;
    	assoc[m] = _201;
    	m = Integer.parseInt("011000", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("100010", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	m = Integer.parseInt("001001", 2);
    	motif[m] = id;
    	assoc[m] = _210;
    	
    	// motif 4
    	id = 3;
    	m = Integer.parseInt("010110", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("101001", 2);
    	motif[m] = id;
    	assoc[m] = _021;
    	m = Integer.parseInt("101010", 2);
    	motif[m] = id;
    	assoc[m] = _201;
    	m = Integer.parseInt("010101", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("100101", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	m = Integer.parseInt("011010", 2);
    	motif[m] = id;
    	assoc[m] = _210;
    	
    	// motif 5
    	id = 4;
    	m = Integer.parseInt("010011", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("101100", 2);
    	motif[m] = id;
    	assoc[m] = _021;
    	m = Integer.parseInt("111000", 2);
    	motif[m] = id;
    	assoc[m] = _201;
    	m = Integer.parseInt("000111", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("001101", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	m = Integer.parseInt("110010", 2);
    	motif[m] = id;
    	assoc[m] = _210;
    	
    	// motif 6
    	id = 5;
    	m = Integer.parseInt("111010", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("101101", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("010111", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	
    	// motif 7 (feed-forward loop)
    	id = 6;
    	m = Integer.parseInt("110100", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("110001", 2);
    	motif[m] = id;
    	assoc[m] = _021;
    	m = Integer.parseInt("011100", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("001110", 2);
    	motif[m] = id;
    	assoc[m] = _201;
    	m = Integer.parseInt("100011", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	m = Integer.parseInt("001011", 2);
    	motif[m] = id;
    	assoc[m] = _210;
    	
    	// motif 8 (loop)
    	id = 7;
    	m = Integer.parseInt("100110", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("011001", 2);
    	motif[m] = id;
    	assoc[m] = _021;
    	
    	// motif 9
    	id = 8;
    	m = Integer.parseInt("110101", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("011110", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("101011", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	
    	// motif 10
    	id = 9;
    	m = Integer.parseInt("001111", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("110011", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("111100", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	
    	// motif 11
    	id = 10;
    	m = Integer.parseInt("110110", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("111001", 2);
    	motif[m] = id;
    	assoc[m] = _021;
    	m = Integer.parseInt("011101", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("101110", 2);
    	motif[m] = id;
    	assoc[m] = _201;
    	m = Integer.parseInt("100111", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	m = Integer.parseInt("011011", 2);
    	motif[m] = id;
    	assoc[m] = _210;
    	
    	// motif 12
    	id = 11;
    	m = Integer.parseInt("111011", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	m = Integer.parseInt("111110", 2);
    	motif[m] = id;
    	assoc[m] = _021;
    	m = Integer.parseInt("101111", 2);
    	motif[m] = id;
    	assoc[m] = _102;
    	m = Integer.parseInt("111101", 2);
    	motif[m] = id;
    	assoc[m] = _201;
    	m = Integer.parseInt("011111", 2);
    	motif[m] = id;
    	assoc[m] = _120;
    	m = Integer.parseInt("110111", 2);
    	motif[m] = id;
    	assoc[m] = _210;
    	
       	// motif 13
    	id = 12;
    	m = Integer.parseInt("111111", 2); // standard representation
    	motif[m] = id;
    	assoc[m] = _012;
    	
    	motifId_ = motif;
    	motifNodeAssociation_ = assoc;

    }
    
    
    // ----------------------------------------------------------------------------
    
    /**
     * Initialize motifDefs_
     */
    private void initializeMotifDefs() {
    
    	motifDefs_ = new String[numMotifTypes_];
    	
    	motifDefs_[0] = "110000"; // fan-out
    	motifDefs_[1] = "001010"; // fan-in
    	motifDefs_[2] = "100100"; // cascade
    	motifDefs_[3] = "010110";
    	motifDefs_[4] = "010011";
    	motifDefs_[5] = "111010";
    	motifDefs_[6] = "110100"; // FFL
    	motifDefs_[7] = "100110"; // loop
    	motifDefs_[8] = "110101";
    	motifDefs_[9] = "001111";
    	motifDefs_[10] = "110110";
    	motifDefs_[11] = "111011";
    	motifDefs_[12] = "111111";
    }
	
    
	// ----------------------------------------------------------------------------
    
    /**
     * Initialize indexTrueEdges_, indexBackEdges_, and indexAbsentEdges_
     */
    private void initializeEdgeIndexes() {
    	
    	indexTrueEdges_ = new ArrayList<ArrayList<Integer>>();
    	indexBackEdges_ = new ArrayList<ArrayList<Integer>>();
    	indexAbsentEdges_ = new ArrayList<ArrayList<Integer>>();
    	
    	for (int i=0; i<numMotifTypes_; i++)
    		addEdgeIndexes(motifDefs_[i]);
    	
    }
    
    
	// ----------------------------------------------------------------------------

    private void addEdgeIndexes(String s) {
    	
    	assert s.length() == 6;
    	
    	ArrayList<Integer> etrue = new ArrayList<Integer>();
    	ArrayList<Integer> eback = new ArrayList<Integer>();
    	ArrayList<Integer> eabsent = new ArrayList<Integer>();

    	// Add the true edges of that motif
    	for (int i=0; i<s.length(); i++)
    		if (s.charAt(i) == '1')
    			etrue.add(i);
    	
    	// Add the back and absent edges
    	// 0->1 (edge 0)
    	if (s.charAt(_01) == '0') {
    		if (s.charAt(_10) == '1')
    			eback.add(_01);
    		else
    			eabsent.add(_01);
    	}
    	
    	// 0->2 (edge 1)
    	if (s.charAt(_02) == '0') {
    		if (s.charAt(_20) == '1')
    			eback.add(_02);
    		else
    			eabsent.add(_02);
    	}
    	
    	// 1->0 (edge 2)
    	if (s.charAt(_10) == '0') {
    		if (s.charAt(_01) == '1')
    			eback.add(_10);
    		else
    			eabsent.add(_10);
    	}
    	
    	// 1->2 (edge 3)
    	if (s.charAt(_12) == '0') {
    		if (s.charAt(_21) == '1')
    			eback.add(_12);
    		else
    			eabsent.add(_12);
    	}
    	
    	// 2->0 (edge 4)
    	if (s.charAt(_20) == '0') {
    		if (s.charAt(_02) == '1')
    			eback.add(_20);
    		else
    			eabsent.add(_20);
    	}
    	
    	// 2->1 (edge 5)
    	if (s.charAt(_21) == '0') {
    		if (s.charAt(_12) == '1')
    			eback.add(_21);
    		else
    			eabsent.add(_21);
    	}
    	
    	// assert that everything has been counted
    	assert etrue.size() + eback.size() + eabsent.size() == 6;
    	
    	// add to the lists
    	indexTrueEdges_.add(etrue);
    	indexBackEdges_.add(eback);
    	indexAbsentEdges_.add(eabsent);
    }


	// ============================================================================
	// GETTERS AND SETTERS

	public int getNumMotifTypes() { return numMotifTypes_; }
	public int getNumEdgeTypes() { return numEdgeTypes_; }

	public int getMotifId(int m) { return motifId_[m]; }

	public int[] getMotifNodeAssociation(int m) { return motifNodeAssociation_[m]; }
	
	public ArrayList<Integer> getIndexTrueEdges(int m) { return indexTrueEdges_.get(m); }
	public ArrayList<Integer> getIndexBackEdges(int m) { return indexBackEdges_.get(m); }
	public ArrayList<Integer> getIndexAbsentEdges(int m) { return indexAbsentEdges_.get(m); }

}
