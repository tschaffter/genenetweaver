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

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.epfl.lis.imod.Imod;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.imod.ImodSettings;
import ch.epfl.lis.imod.ModularityDetector;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.Structure;

/**
 * Implements some functions to analyze structural properties of networks.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class GraphUtilities {

	/** The network that is being analyzed */
	ImodNetwork network_;
	/** The strongly connected components of the network */
	private ArrayList<ArrayList<Node>> components_;
	
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(GraphUtilities.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
    /** Constructor */
	public GraphUtilities(ImodNetwork network)
	{
		network_ = network;
		components_ = null;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return the adjacency matrix A of the network. A[i][j] = true if there is a link
	 * from gene *j* to gene i.
	 */
	public boolean[][] getAdjacencyMatrix()
	{	
		int numNodes = network_.getSize();
		int numEdges = network_.getNumEdges();
		boolean[][] A = new boolean[numNodes][numNodes];
		
		for (int i = 0; i < numNodes; i++)
			for (int j = 0; j < numNodes; j++)
				A[i][j] = false;
		
		for (int i = 0; i < numEdges; i++)
		{
			Edge edge = network_.getEdge(i);
			int sourceIndex = network_.getIndexOfNode(edge.getSource());
			int targetIndex = network_.getIndexOfNode(edge.getTarget());
			A[targetIndex][sourceIndex] = true;
		}
		return A;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Return the indegrees (number of inputs) of the nodes */
	public int[] getIndegrees()
	{	
		int numNodes = network_.getSize();
		int numEdges = network_.getNumEdges();
		int[] indegrees = new int[numNodes];
		
		for (int i = 0; i < numNodes; i++)
			indegrees[i] = 0;
		
		for (int i = 0; i < numEdges; i++)
		{
			Edge edge = network_.getEdge(i);
			indegrees[network_.getIndexOfNode(edge.getTarget())]++;
		}
		return indegrees;
	}

	// ----------------------------------------------------------------------------
	
	/** Return the outdegrees (number of outputs) of the nodes */
	public int[] getOutdegrees()
	{	
		int numNodes = network_.getSize();
		int numEdges = network_.getNumEdges();
		int[] outdegrees = new int[numNodes];
		
		for (int i = 0; i < numNodes; i++)
			outdegrees[i] = 0;
		
		for (int i = 0; i < numEdges; i++)
		{
			Edge edge = network_.getEdge(i);
			outdegrees[network_.getIndexOfNode(edge.getSource())]++;
		}
		return outdegrees;
	}
	
	// ----------------------------------------------------------------------------

	/** Returns the strongly connected components of the graph (network) */
	public ArrayList<ArrayList<Node>> getStronglyConnectedComponents()
	{
		// Only nodes that are regulators (outdegree greater than zero) can potentially be
		// part of a strongly connected component. Thus, we first extract the network of
		// all regulators to decrease computation time afterwards.
		SubnetExtractor extractor = new SubnetExtractor(network_);
		ImodNetwork networkOfRegulators = extractor.extractRegulators(); 
		
		// The strongly connected components
		components_ = new ArrayList<ArrayList<Node>>();
		ArrayList<Node> nextComponent = new ArrayList<Node>();
		ArrayList<Node> visited = new ArrayList<Node>();
		ArrayList<Integer> visitedDfsnum = new ArrayList<Integer>();

		// Add a virtual root node, which has links to all other nodes, so that the graph is connected
		Node rootNode = new Node();
		for (int i = 0; i < networkOfRegulators.getSize(); i++)
			networkOfRegulators.addEdge(new Edge(rootNode, networkOfRegulators.getNode(i)));
		networkOfRegulators.addNode(rootNode);
		
		// Get the strongly connected components
		Tree tree = new Tree();
		tree.visit(0, rootNode, networkOfRegulators, nextComponent, visited, visitedDfsnum);
		
		// Print
		log_.log(Level.INFO, "Identified " + components_.size() + " strongly connected component(s)");
		
		for (int i = 0; i < components_.size(); i++)
		{
			ArrayList<Node> comp = components_.get(i);
			String compString = comp.get(0).getLabel();
			for (int j = 1; j < comp.size(); j++)
				compString += " " + comp.get(j).getLabel();
			
			log_.log(Level.INFO, (i+1) + ": " + compString);
		}
		log_.log(Level.INFO, ""); // makes a new line

		return components_;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Evaluate for every pair of regulators the number of targets that they have in
	 * common, return the sorted list of pairs (only include those that do have common targets) 
	 */
	public ArrayList<int[]> getSortedCoregulatorPairs()
	{	
		ArrayList<int[]> pairs = new ArrayList<int[]>();
		
		int numGenes = network_.getSize();
		boolean[][] A = getAdjacencyMatrix();
		
		int[][] numCommonTargets = new int[numGenes][numGenes];
		for (int i=0; i<numGenes; i++)
			for (int j=0; j<numGenes; j++)
				numCommonTargets[i][j] = 0;
		
		// count the number of common targets for all pairs
		for (int g=0; g<numGenes; g++) {
			// for all targets of that gene (i.e., all genes with A[i][g]=true)
			for (int i=0; i<numGenes; i++) {
				if (A[i][g]) {
					// check who else regulates that gene
					for (int j=g+1; j<numGenes; j++) { // note, we only fill the upper triangular part of the matrix
						if (A[i][j])
							numCommonTargets[g][j]++;
					}
				}
			}
		}
		
		// make the list of all pairs with common targets
		for (int i=0; i<numGenes; i++) {
			for (int j=i+1; j<numGenes; j++) {
				if (numCommonTargets[i][j] > 0) {
					int[] thisPair = new int[3];
					thisPair[0] = i;
					thisPair[1] = j;
					thisPair[2] = numCommonTargets[i][j];
					pairs.add(thisPair);
				}
			}
		}
		
		// Sort the pairs
		Collections.sort(pairs, new PairsComparator());

		return pairs;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Replace the true names of the genes by G1, G2, ..., and save the mapping:
	 * G1	"true name of gene 1"
	 * G2	"true name of gene 2"
	 * etc.
	 */
	public void anonymizeGenes(String filename)
	{
		try
		{
			if (filename.compareTo("") == 0)
				filename = GnwSettings.getInstance().getOutputDirectory() + network_.getId() + "_gene_names.tsv";

			log_.log(Level.INFO, "Writing file " + filename);
			
			FileWriter fw = new FileWriter(filename, false);
			// the header
			fw.write("\"Old\"\t\"New\"\n");

			int numGenes = network_.getSize();
			for (int i=0; i<numGenes; i++)
				fw.write("G" + (i+1) + "\t" + network_.getNode(i).getLabel() + "\n");
			
			fw.close();
			
			// replace the names
			for (int i=0; i<numGenes; i++)
				network_.getNode(i).setLabel("G" + (i+1));

		} catch (Exception e) {
			log_.log(Level.WARNING, "GraphUtilities::anonymizeGenes(): " + e.getMessage(), e);
			throw new RuntimeException();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return the connectivity density of the network.
	 * Connectivity density = num_edges / num_nodes^2
	 */
	public double computeConnectivityDensity()
	{
		return network_.getNumEdges() / Math.pow(network_.getSize(), 2);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Run modular detection. Return the modularity of the network as well as the
	 * number of instances of indivisible communities in the network (returned
	 * as a Double).
	 * @throws Exception 
	 */
	public ArrayList<Double> computeModularity() throws Exception
	{
		ImodSettings global = ImodSettings.getInstance();
		global.setUseNewman(true);
		global.setUseMovingVertex(false);
		global.setUseGlobalMovingVertex(false);
		
		Imod imod = new Imod();
		global.setImod(imod);
		
		ModularityDetector md = new ModularityDetector(network_);
//		CommunityRoot root = new CommunityRoot(md, network_);
		imod.setMyModularityDetector(md);
		
		
		Double Q = imod.runModularityDetection(network_);
//		double numModules = root.getNumCommunities();
		
		// Save results
		ArrayList<Double> result = new ArrayList<Double>();
		result.add(Q);
//		result.add(numModules);
		
		return result;
	}
	
	// ----------------------------------------------------------------------------

	public void setNetwork(ImodNetwork network) {network_ = network;}
	
	
	// ============================================================================
	// PRIVATE CLASSES
	
	/**
	 * Comparator used by getTopOverlappingRegulatorPairs() to sort the pairs
	 */
	private class PairsComparator implements Comparator<int[]>
	{
		//private Uniform uniform_ = GnwSettings.getInstance().getUniformDistribution();

		public int compare(int[] a, int[] b) {
			//if (a[2] == b[2])
				//return uniform_.nextBoolean() ? 1 : -1;
			if (a[2] < b[2])
				return 1;
			else if (a[2] > b[2])
				return -1;
			else
				return 0;
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Tree for a depth-first search (DFS) of the graph, used by getStronglyConnectedComponents().
	 * The pseudo-code of this algorithm is given at the end of this file.
	 */
	private class Tree
	{
		
		/** The node of the network corresponding to this node of the tree */
		private Node node_;
		/** The 'low' variable that is used to determine if the node is a head (see pseudo-code) */
		private int low_;
		/** The subtrees of this tree */
		private ArrayList<Tree> subtrees_;
		

		// ============================================================================
		// PUBLIC METHODS

		/** Constructor */
		public Tree() {
			node_ = null;
			low_ = -1;
			subtrees_ = null;
		}
		
		// ----------------------------------------------------------------------------

		/** 
		 * Recursive visit (depth-first search).
		 * @param dfsnum The node visited is the dfsnum'th node
		 * @param node The node being visited
		 * @param network The network. Note that nodes will be deleted once a densely connected component
		 * has been identified. Thus, one should pass a clone of the network and not the original network.
		 * @param L The next densely connected component
		 * @param visited A list of nodes that have already been visited
		 * @param visitedDfsnum visited[i] was the visitedDfsnum[i]'th node visited
		 */
		public void visit(int dfsnum, Node node, Structure network, ArrayList<Node> L, ArrayList<Node> visited, ArrayList<Integer> visitedDfsnum) {
			
			node_ = node;
			L.add(node_);
			assert !visited.contains(node_);
			visited.add(node_);
			visitedDfsnum.add(dfsnum);
			
			low_ = dfsnum++;
			subtrees_ = new ArrayList<Tree>();

			// get all targets of this node
			ArrayList<Node> directTargets = new ArrayList<Node>();
			
			for (int i=0; i<network.getNumEdges(); i++) {
				Node source = network.getEdge(i).getSource();
				Node target = network.getEdge(i).getTarget();
				
				if (source == node_ && target != node_) {
					assert !directTargets.contains(target);
					directTargets.add(target);
				}
			}
			
			// visit the neighbors that have not yet been visited
			for (int i=0; i<directTargets.size(); i++) {
				Node n = directTargets.get(i);
				
				if (!visited.contains(n)) {
					Tree subtree = new Tree();
					subtrees_.add(subtree);
					subtree.visit(dfsnum, n, network, L, visited, visitedDfsnum);
					low_ = min(low_, subtree.getLow());
				} else {
					low_ = min(low_, visitedDfsnum.get(visited.indexOf(n)));
				}
			}
			
			// check if the node is a head of a densely connected component
			if (low_ == visitedDfsnum.get(visited.indexOf(node_))) {
				
				// if the densely connected components consists of only this node,
				// remove the node from the component list and from the graph
				if (node_ == L.get(L.size()-1)) {
					L.remove(L.size()-1);
					network.removeNode(node_);
					
				} else {
					ArrayList<Node> component = new ArrayList<Node>();
					do {
						Node n = L.remove(L.size()-1);
						network.removeNode(n);
						component.add(n);
						
					} while (node_ != component.get(component.size()-1));					
					components_.add(component);
				}
			}
		}
	
		// ----------------------------------------------------------------------------

		/** Return the smaller of the two numbers */
		private int min(int a, int b) {
			if (a <= b)
				return a;
			else
				return b;
		}
			
		public int getLow() { return low_; }
	
	}
}


/*
 * Pseudo-code of the algorithm for detecting strongly connected components.
 * Taken from David Eppstein's course (http://www.ics.uci.edu/~eppstein/161/960220.html).
 * It's the algorithm invented by Bob Tarjan in 1972, which is linear in time.
 */

/*
Strong connectivity algorithm
Define the DFS numbering dfsnum(v) to be the number of vertices visited before v in the DFS. Then if there is a back or 
cross edge out of the subtree of v, it's to something visited before v and therefore with a smaller dfsnum. We use this 
by defining the low value low(v) to be the smallest dfsnum of a vertex reachable by a back or cross edge from the subtree 
of v. If there is no such edge, low(v)=dfsnum(v). Then rephrasing what we've seen so far, v is a head of a component 
exactly when low(v)=dfsnum(v). The advantage of using these definitions is that dfsnum(v) is trivial to calculate as we 
perform the DFS, and low(v) is easily computed by combining the low values from the children of v with the values coming 
from back or cross edges out of v itself.

We use one more simple data structure, a stack L (represented as a list) which we use to identify the subtree rooted at 
a vertex. We simply push each new vertex onto L as we visit it; then when we have finished visiting a vertex, its subtree 
will be everything pushed after it onto L. If v is a head, and we've already deleted the other heads in that subtree, the 
remaining vertices left on L will be exactly the component [v].

We are now ready to describe the actual algorithm. It simply performs a DFS, keeping track of the low and dfsnum values 
defined above, using them to identify heads of components, and when finding a head deleting the whole component from the 
graph, using L to find the vertices of the component.

    DFS(G)
    {
    make a new vertex x with edges x->v for all v
    initialize a counter N to zero
    initialize list L to empty
    build directed tree T, initially a single vertex {x}
    visit(x)
    }

    visit(p)
    {
    add p to L
    dfsnum(p) = N
    increment N
    low(p) = dfsnum(p)
    for each edge p->q
        if q is not already in T
        {
        add p->q to T
        visit(q)
        low(p) = min(low(p), low(q))
        } else low(p) = min(low(p), dfsnum(q))

    if low(p)=dfsnum(p)
    {
        output "component:"
        repeat
        remove last element v from L
        output v
        remove v from G
        until v=p
    }
    }
*/