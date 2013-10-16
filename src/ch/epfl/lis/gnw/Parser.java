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

package ch.epfl.lis.gnw;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.networks.ios.TSVParser;


/** 
 * Extends the basic TSVParser to load and save some formats specific to GNW.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */ 
public class Parser extends TSVParser { 

	
	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(Parser.class.getName());
	
	
	// ============================================================================
	// PUBLIC FUNCTIONS
	
	/** Default constructor */
	//public Parser(){
		//super(null);
	//}
	
	/** Constructor */
	public Parser(Structure struct){
		super(struct);
	}
	
	/** Constructor */
	public Parser(ImodNetwork struct, URL absPath) {
		super(struct, absPath);
	}
	
	
	// ----------------------------------------------------------------------------

	/** Write the data to a TSV file */
	public static void writeTSV(String filename, ArrayList<String[]> data) {
		
		log_.info("Writing file " + filename);
		try {
			Parser.writeTSV(GnwSettings.getInstance().getURL(filename), data);
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not write file " + filename, e);
		}

	}
	
	
	// ----------------------------------------------------------------------------

	/** Convert an array list of array list of doubles to the format required by writeTSV() */
	public static ArrayList<String[]> toListOfStringArrays(ArrayList<ArrayList<Double>> data) {
		
		ArrayList<String[]> converted = new ArrayList<String[]>();
		for (int i=0; i<data.size(); i++) {
			ArrayList<Double> datal = data.get(i);
			String[] convl = new String[datal.size()];
			
			for (int j=0; j<datal.size(); j++)
				convl[j] = Double.toString(datal.get(j));
			 
			converted.add(convl);
		}
		return converted;
	}

	
	// ----------------------------------------------------------------------------

	/** Convert an array list of array list of doubles to the format required by writeTSV() */
	public static ArrayList<String[]> toListOfStringArray(ArrayList<Double> data) {
		
		ArrayList<String[]> converted = new ArrayList<String[]>();
		for (int i=0; i<data.size(); i++) {
			String[] str = new String[1];
			str[0] = Double.toString(data.get(i));
			converted.add(str);
		}
		return converted;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Read a RegulonDB flat file of TF-gene interactions.
	 * 
	 * Instruction for Release 6.7
	 * ===========================
	 * 
	 * 1. Same as for 6.2. It seems they have cleaned up the file: there are no more
	 * phantom genes and '+?' interactions.
	 * 2. Remove the header.
	 * 3. Remove obsolete interactions using: grep -v obsolete regulon.txt > regulonClean.tsv
	 * 
	 * Instructions for Release 6.2
	 * ============================
	 * 
	 * 1. Download the original file from RegulonDB
	 * (http://regulondb.ccg.unam.mx/html/Data_Sets.jsp, File 1. TF-gene interactions).
	 * It is in TSV format, the TF is in column 2, the target in column 4, and the type 
	 * of interaction in column 6 (+ activator, - repressor, +- dual, ? unknown). Note
	 * that they actually also have some "+?", it's not clear to me what that's supposed to
	 * mean, so I will treat them like unknowns. There are also some interactions of TFs with
	 * "Phantom genes", I also don't understand what that is and will ignore them.
	 * I didn't find any documentation on their formats (or on anything else) on their website...
	 * 2. Remove the header and the few lines with the phantom genes manually before parsing the file.
	 */
	public void readRegulonDB(String filename) {
		
		if (structure_ == null)
			throw new RuntimeException("No structure specified");

		try {
			ArrayList<String[]> rawData = readTSV(GnwSettings.getInstance().getURL(filename));

			for (int i=0; i < rawData.size(); i++) {

				// In Release 6.7 they got rid of these bars, there's one TF per line
				// The TF. Format: "geneA|" or "geneA|geneB|"
				//String tfIds[] = rawData.get(i)[1].split("\\|");
				//if (tfIds.length == 0)
					//GnwMain.error(new RuntimeException(), "Line " + (i+1) + ", column 2: missing '|'");
				//addNode(tfIds[0]);				
				//if (tfIds.length == 2)
					//addNode(tfIds[1]);			
				//if (tfIds.length > 2)
					//GnwMain.error(new RuntimeException(), "Line " + (i+1) + ", column 2: more than two '|'");

				// The TF
				String tfId = rawData.get(i)[1];
				Node tf = addNode(tfId);
				
				// The target
				String targetId = rawData.get(i)[3];
				Node target = addNode(targetId);

				// Add the edges
				String edgeType = rawData.get(i)[5];
				// We treat the +? and -? like a ?
				
				if (edgeType.equals("+?") || edgeType.equals("-?"))
					edgeType = "?";

				addEdge(tf, target, edgeType);

				//if (tfIds.length == 2)
					//addEdge(tfIds[1], targetId, rawData.get(i)[5]);

			}
				
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not parse RegulonDB file", e);
		}

		structure_.removeMultiEdges();
		structure_.setId(filename);
		structure_.setComment("");
		structure_.setDirected(true);
		structure_.setSigned(true);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Write the network structure to a file in the format used for the DREAM gold standards
	 */
	public void writeGoldStandard() {
		
		log_.log(Level.INFO, "Writing file " + absPath_.getPath());
		
		if (structure_ == null)
			throw new RuntimeException("No network structure specified");
		
		try { 
			FileWriter fw = new FileWriter(new File(absPath_.toURI()), false);
			int numNodes = structure_.getSize();
			boolean noSelfLoops = GnwSettings.getInstance().getIgnoreAutoregulatoryInteractionsInEvaluation();
			
			// Get the adjacency matrix
			GraphUtilities util = new GraphUtilities((ImodNetwork) structure_);
			boolean[][] A = util.getAdjacencyMatrix();
			
			// Write the present edges
			for (int i=0; i<numNodes; i++)
				for (int j=0; j<numNodes; j++)
					if (A[j][i] == true && (!noSelfLoops || i != j))
						fw.write(structure_.getNode(i).getLabel() + "\t" + structure_.getNode(j).getLabel() + "\t1\n");
			
			// Write the zero edges
			if (GnwSettings.getInstance().getAppendZeroInteractionsInGoldStandardFiles()) {
				for (int i=0; i<numNodes; i++)
					for (int j=0; j<numNodes; j++)
						if (A[j][i] == false && (!noSelfLoops || i != j))
							fw.write(structure_.getNode(i).getLabel() + "\t" + structure_.getNode(j).getLabel() + "\t0\n");
			}
			
			fw.close();

		} catch (IOException fe) {
			log_.log(Level.WARNING, "Could not write gold standard", fe);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			log_.log(Level.WARNING, "Could not write gold standard", e);
		}
	}

}
