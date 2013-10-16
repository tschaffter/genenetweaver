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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.random.Uniform;

import ch.epfl.lis.imod.ModularityDetector;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.Structure;


/** Implements the module extraction method from a source network.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
public class SubnetExtractor {
	
	/** Modularity detector used to compute modularities Q */
	private ModularityDetector myModularityDetector_;
	/** The source network from which subnets are extracted */
	ImodNetwork sourceNetwork_;
	/** Vertices are added using truncated selection with the given fraction, see GnwSettings.truncatedSelectionFraction. */
	double truncatedSelectionFraction_;
	/** The number of regulators in the extracted networks, set to 0 to disable control of number of regulators */
	private int numRegulators_;
	/** The outdegrees of the nodes, used for the neighbor selection to control the number of regulators */
	private int[] outdegrees_;

    /** Logger for this class */
    private Logger log_ = Logger.getLogger(SubnetExtractor.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor */
	public SubnetExtractor(ImodNetwork sourceNetwork) {
		myModularityDetector_ = null;
		sourceNetwork_ = sourceNetwork;
		truncatedSelectionFraction_ = GnwSettings.getInstance().getTruncatedSelectionFraction();
		numRegulators_ = GnwSettings.getInstance().getNumRegulators();
		
		if (numRegulators_ > 0) {
			GraphUtilities util = new GraphUtilities(sourceNetwork_);
			outdegrees_ = util.getOutdegrees();
		} else
			outdegrees_ = null;
		
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Extract a subnetwork consisting of all regulators of the source network, i.e., all
	 * nodes that have outdegree one or higher. Note that if a regulator only targets genes
	 * that are not regulators themselves, it has outdegree zero in the subnetwork of all
	 * regulators, but it has outdegree one or higher in the source network.
	 */
	public ImodNetwork extractRegulators() {

		String subnetName = sourceNetwork_.getId() + "-regulators";
		//log.log(Level.INFO, "Extraction of subnet '" + subnetName + "'...");
		
		// List of nodes that are regulators
		ArrayList<Node> regulators = new ArrayList<Node>();
		GraphUtilities util = new GraphUtilities(sourceNetwork_);
		int[] k = util.getOutdegrees();
		
		for (int i=0; i<k.length; i++) {
			if (k[i] > 0) {
				Node reg = sourceNetwork_.getNode(i);
				regulators.add(reg);
				//log.log(Level.INFO, "Added regulator '" + reg.getLabel() + "'");
			}
		}
	
		// Create the network structure from the list of node labels
		ImodNetwork output = sourceNetwork_.getSubnetwork(subnetName, regulators);

		output.setComment("");
		output.setDirected(sourceNetwork_.isDirected());
		output.setSigned(sourceNetwork_.isSigned());

		return output;
    }
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Extract multiple subnetworks from the given source network. For every subnetwork, a
	 * different random seed is randomly selected. 
	 * @param subnetName The name for the subnets ('-i' will be appended for the i'th subnet)
	 * @param subnetSize The desired size of the extracted subnetworks
	 * @param numSubnets The number of subnetsworks to be generated
	 * @return An array of extracted subnetworks
	 */
	public Structure[] runExtraction(String subnetName, int subnetSize, int numSubnets) {
		
		GnwSettings set = GnwSettings.getInstance();
		ArrayList<ArrayList<Node>> seeds = new ArrayList<ArrayList<Node>>();
		
		if (set.getNumSeedsFromStronglyConnectedComponents() > 0) {
			seeds = sampleSeedsFromStronglyConnectedComponents(numSubnets);
			//seeds = Dream4.getEcoliSeeds(sourceNetwork_);
		
		} else {
			// Pick a random vertex in the source network as seed
			for (int i=0; i<numSubnets; i++) {
				
				if (set.stopSubnetExtraction()) // stop extraction process during seed generation ?
					return null;
				
				ArrayList<Node> nextSeed = new ArrayList<Node>();
				int index = set.getUniformDistribution().nextIntFromTo(0, sourceNetwork_.getSize()-1);
				
				// TODO not very efficient if there are few regulators
				// if we need a regulator, try again until we get one
				if (numRegulators_ > 0) {
					while (outdegrees_[index] < 1)
						index = set.getUniformDistribution().nextIntFromTo(0, sourceNetwork_.getSize()-1);
				}
				nextSeed.add(sourceNetwork_.getNode(index));
				
				// TODO: to generate DREAM4-like benchmark
//				nextSeed.clear();
				// insilico_size100_1
//				nextSeed.add(sourceNetwork_.getNode("srlR"));
//				nextSeed.add(sourceNetwork_.getNode("gutM"));
//				nextSeed.add(sourceNetwork_.getNode("galR"));
//				nextSeed.add(sourceNetwork_.getNode("galS"));
//				nextSeed.add(sourceNetwork_.getNode("rob"));
//				nextSeed.add(sourceNetwork_.getNode("marR"));
//				nextSeed.add(sourceNetwork_.getNode("marA"));
				// insilico_size100_2
//				nextSeed.add(sourceNetwork_.getNode("hns"));
//				nextSeed.add(sourceNetwork_.getNode("gadX"));
//				nextSeed.add(sourceNetwork_.getNode("gadE"));
//				nextSeed.add(sourceNetwork_.getNode("gadW"));
//				nextSeed.add(sourceNetwork_.getNode("fnr"));
//				nextSeed.add(sourceNetwork_.getNode("arcA"));
//				nextSeed.add(sourceNetwork_.getNode("hipA"));
//				nextSeed.add(sourceNetwork_.getNode("hipB"));
//				nextSeed.add(sourceNetwork_.getNode("ihfB"));
//				nextSeed.add(sourceNetwork_.getNode("ihfA"));
				
				seeds.add(nextSeed);
			}
		}
		
		return runExtraction(subnetName, subnetSize, seeds);
    }
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Extract multiple subnetworks from the given source network. The number of extracted modules
	 * is given by the number of provided seeds (seeds.size()). 
	 * @param subnetName The name for the subnets ('-i' will be appended for the i'th subnet)
	 * @param seeds Specifies for every desired subnet the list of seed nodes
	 * @param subnetSize The desired size of the extracted subnetworks
	 * @return An array of extracted subnetworks
	 */
	public Structure[] runExtraction(String subnetName, int subnetSize, ArrayList<ArrayList<Node>> seeds) {
		
		GnwSettings set = GnwSettings.getInstance();
		int numSubnets = seeds.size();
		ImodNetwork[] output = new ImodNetwork[numSubnets];
		
		//output[0] = extractRegulators();
		//return output;
		
		for (int i=0; i < numSubnets; i++) {
			
			if (set.stopSubnetExtraction()) // exit extraction process ?
				return null;
			
			String subnetName_i = new String(subnetName + "-" + (i+1)); // set the name of the network
			output[i] = runExtraction(subnetName_i, seeds.get(i), subnetSize);
			
			GraphUtilities util2 = new GraphUtilities(output[i]);
			util2.getStronglyConnectedComponents();
		}
		return output;
    }
	
		
	// ============================================================================
	// PRIVATE METHODS
	
	/**
	 * Extract a subnetwork from the given source network. 
	 * @param subnetName The name for the subnets ('-i' will be appended for the i'th subnet)
	 * @param seeds Specifies for every desired subnet the list of seed nodes
	 * @param subnetSize The desired size of the extracted subnetworks
	 * @return An array of extracted subnetworks
	 */
	@SuppressWarnings("unchecked")
	private ImodNetwork runExtraction(String subnetName, ArrayList<Node> seeds, int subnetSize) {

		log_.log(Level.INFO, "Extraction of subnet '" + subnetName + "'...");
		if (subnetSize >= sourceNetwork_.getSize())
			throw new IllegalArgumentException("subnetSize must be smaller than the size of the source network");
		
		// check that numRegulators is not greater than subnetSize and that, if numRegulators is greater than zero
		// the seeds are regulators
		if (numRegulators_ > 0) {
			if (numRegulators_ > subnetSize)
				throw new IllegalArgumentException("numRegulators must be smaller or equal subnetSize");
			for (int i=0; i<seeds.size(); i++) {
				int index = sourceNetwork_.getIndexOfNode(seeds.get(i));
				if (outdegrees_[index] < 1)
					throw new IllegalArgumentException("if numRegulators is greater than 0, the seeds must be regulators");
			}
		}
		
		// copy the seeds to the subnet
		ArrayList<Node> subnet = (ArrayList<Node>) seeds.clone();
		ArrayList<Integer> subnetIndexes = sourceNetwork_.getIndexesFromNodes(subnet);

		myModularityDetector_ = new ModularityDetector(sourceNetwork_);
		myModularityDetector_.computeModularityMatrix(); // also sets temp_size and temp_B
		myModularityDetector_.setCurrentSubcommunityS(computeAppartenanceVector(subnetIndexes));
		double Q = myModularityDetector_.computeModularity();
		myModularityDetector_.setModularity(Q);

		// print the seeds
		for (int i=0; i<subnet.size(); i++)
			log_.log(Level.INFO, "Added seed '" + subnet.get(i).getLabel() + "'");
		log_.log(Level.INFO, "Modularity Q:\t" + Q);

		for (int i=seeds.size(); i<subnetSize; i++)
			addVertexToSubnet(subnet);
	
		// Create the network structure from the list of node labels
		ImodNetwork output = sourceNetwork_.getSubnetwork(subnetName, subnet);

		output.setComment("");
		output.setDirected(sourceNetwork_.isDirected());
		output.setSigned(sourceNetwork_.isSigned());

		return output;
    }

	
	// ----------------------------------------------------------------------------
	
	/**
	 * From a Network instance and a group of vertex represented by they label (id),
	 * this method selects the best neighbor vertex of the given group of vertex
	 * (following modularity criterion) and add it to this group (core list).
	 * @param Network - A Network instance
	 * @param ArrayList<String> - List of vertices id belonging to the Network
	 * instance.
	 */
	private void addVertexToSubnet(ArrayList<Node> subnet) {
		
		GnwSettings uni = GnwSettings.getInstance();

		// construct the appartenance vector for the subnet and set the current modularity
		ArrayList<Integer> subnetIndexes = sourceNetwork_.getIndexesFromNodes(subnet);
		myModularityDetector_.setCurrentSubcommunityS( computeAppartenanceVector(subnetIndexes) );
		myModularityDetector_.setCurrentSubcommunityQ( myModularityDetector_.getModularity() );
			
		ArrayList<Node> neighbors = getNeighbors(subnet);
		ArrayList<Integer> neighborIndexes;
		
		// if the number of neighbors is zero, that means the subnet is an island
		// in that case, we add all other nodes as neighbors
		if (neighbors.size() == 0) {
		
			neighborIndexes = new ArrayList<Integer>();
			DoubleMatrix1D s = myModularityDetector_.getCurrentSubcommunityS();
			
			for (int i=0; i<s.size(); i++)
				if (s.get(i) == 1)
					neighborIndexes.add(i);
		
		} else
			neighborIndexes = sourceNetwork_.getIndexesFromNodes(neighbors);
		
		int numNeighbors = neighborIndexes.size();
		
		ArrayList<Double> neighborModularities = new ArrayList<Double>(numNeighbors);
		// compute the network modularity when adding the neighbor i to the core
		for (int i=0; i < numNeighbors; i++) {
			double Q = myModularityDetector_.computeMovingVertexQ(neighborIndexes.get(i));
			neighborModularities.add(Q); // Save its fitness (modularity)
		}
		
		// sort the neighbors according to their modularity
		sortNeighbors(neighborIndexes, neighborModularities);

//		neighborIndexes.clear();
//		neighborModularities.clear();
//		neighborIndexes.add(0);
//		neighborIndexes.add(1);
//		neighborIndexes.add(2);
//		neighborIndexes.add(3);
//		neighborIndexes.add(4);
//		neighborIndexes.add(5);
//		neighborIndexes.add(6);
//		neighborIndexes.add(7);
//		neighborIndexes.add(8);
//		neighborIndexes.add(9);
//		
//		neighborModularities.add(1.1);
//		neighborModularities.add(1.1);
//		neighborModularities.add(1.1);
//		neighborModularities.add(3.3);
//		neighborModularities.add(4.4);
//		neighborModularities.add(4.4);
//		neighborModularities.add(4.4);
//		neighborModularities.add(4.4);
//		neighborModularities.add(8.8);
//		neighborModularities.add(9.9);
//		
//		sortNeighbors(neighborIndexes, neighborModularities);
//		for (int i=0; i<neighborIndexes.size(); i++)
//			System.out.println(neighborIndexes.get(i) + "\t" + neighborModularities.get(i));
//		
//		System.exit(0);
		
		// Select a vertex to add
		int truncationSize = (int)Math.round(truncatedSelectionFraction_ * neighborIndexes.size());
		// I tested, this works
		int selectedVertex = uni.getUniformDistribution().nextIntFromTo(0, truncationSize-1);

		// Set the new modularity
		myModularityDetector_.setModularity( neighborModularities.get(selectedVertex) );
		
		// Add the vertex
		Node vertexAdded = sourceNetwork_.getNode( neighborIndexes.get(selectedVertex) );
		subnet.add(vertexAdded);
		
		//log.log(Level.INFO, "Selected vertex index: " + selectedVertex + " / " + numNeighbors);
		log_.log(Level.INFO, "Added node '" + vertexAdded.getLabel() + "', modularity Q:\t" + neighborModularities.get(selectedVertex));
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Sort the map according to the VALUES as opposing to the casual sort done according
	 * to the KEYS of a map. A special comparator is implemented to sort according to the
	 * modularity values in descending order. If several entries have the same modularity
	 * (values), they are ordered randomly.
	 */
	private void sortNeighbors(ArrayList<Integer> neighborIndexes, ArrayList<Double> neighborModularities) {
		
		// put the values into a map
		Map<Integer, Double> neighborsMap = new HashMap<Integer, Double>();
		for (int i=0; i < neighborIndexes.size(); i++)
			neighborsMap.put(neighborIndexes.get(i), neighborModularities.get(i));
		
		// Sort the map (See function comments)
		DescendingModularitySorting comparator = new DescendingModularitySorting(neighborsMap);
		TreeMap<Integer, Double> descMap = new TreeMap<Integer, Double>(comparator);
		descMap.putAll(neighborsMap);		
		
		// Put the values back into the array lists
		neighborIndexes.clear();
		neighborModularities.clear();
		Iterator<?> it = descMap.entrySet().iterator();
	    while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	    	neighborIndexes.add( (Integer) pairs.getKey() );
	    	neighborModularities.add( (Double) pairs.getValue() );
	    }
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Get the neighboring nodes of the given subnet (the set of nodes connected by
	 * one link to the subnet, but not part of the subnet). If numRegulators > 0, only
	 * regulators are returned if the subnet size is still smaller than numRegulators,
	 * and only non-regulators are returned once the subnet size is greater or equal numRegulators.
	 */
	public ArrayList<Node> getNeighbors(ArrayList<Node> subnet) {
		
		ArrayList<Node> neighbors = new ArrayList<Node>();
		boolean[][] A = sourceNetwork_.getA();
		
		boolean getOnlyRegulators = (numRegulators_ > 0) && (subnet.size() < numRegulators_);
		boolean getOnlyNonRegulators = (numRegulators_ > 0) && (subnet.size() >= numRegulators_);
		
		for (int i=0; i < subnet.size(); i++) {
			ArrayList<Node> neighborsOfNode_i = new ArrayList<Node>();
			int node_i = sourceNetwork_.getIndexOfNode(subnet.get(i));

			for (int j=0; j<A.length; j++) {
    			if (A[j][node_i] && j != node_i) {
    				// skip node j as potential neighbor if it's a non-regulator and we are
    				// looking for regulators or vice-versa
    				if (!((getOnlyRegulators && outdegrees_[j] < 1) ||
    						(getOnlyNonRegulators && outdegrees_[j] >= 1) )) {
    					//if (sourceNetwork_.getNode("flhD") != sourceNetwork_.getNode(j) && 
    						//	sourceNetwork_.getNode("flhC") != sourceNetwork_.getNode(j))
    						neighborsOfNode_i.add(sourceNetwork_.getNode(j));
    				}
    			}
			}
			
			for (int j=0; j < neighborsOfNode_i.size(); j++) {
				if (!neighbors.contains(neighborsOfNode_i.get(j)))
					neighbors.add(neighborsOfNode_i.get(j));
			}
		}
		
		// remove all neighbors that belong to pool
		for (int i=0; i < subnet.size(); i++)
			if (neighbors.contains(subnet.get(i)))
				neighbors.remove(subnet.get(i));
		
		return neighbors;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Computes the appartenance vector s, where element s_i = -1 if node i is part
	 * of the subnet of the source network, and s_i = 1 if not.
	 * @param subnet - ArrayList<Integer> of all vertices of the subnet.
	 */
	private DoubleMatrix1D computeAppartenanceVector(ArrayList<Integer> subnet) {
		
		// initialize the appartenance vector s
		DoubleMatrix1D s = new DenseDoubleMatrix1D(sourceNetwork_.getSize());
		s.assign(1);
		// change all elements of the subnet
		for (int i=0; i < subnet.size(); i++)
			s.set(subnet.get(i), -1);
		
		return s;
	}
	
	
	// ----------------------------------------------------------------------------

	private ArrayList<ArrayList<Node>> sampleSeedsFromStronglyConnectedComponents(int numSubnets) {
		
		GnwSettings set = GnwSettings.getInstance();
		Uniform uni = set.getUniformDistribution();
		
		GraphUtilities util = new GraphUtilities(sourceNetwork_);
		ArrayList<ArrayList<Node>> components = util.getStronglyConnectedComponents();
		ArrayList<ArrayList<Node>> seeds = new ArrayList<ArrayList<Node>>();
		
		int totalComponentNodes = 0;
		for (int i=0; i<components.size(); i++)
			totalComponentNodes += components.get(i).size();
		
		int numSeedsFromComponents = set.getNumSeedsFromStronglyConnectedComponents();
		if (numSeedsFromComponents > totalComponentNodes) {
			log_.log(Level.INFO, "WARNING: Only " + totalComponentNodes + " nodes are part of densely connected components and were selected as seeds");
			numSeedsFromComponents = totalComponentNodes;
		}
		
		// create a set of seed nodes for every subnet
		// TODO implement a clean version of this for the next gnw release
		// this here is just a hack that works only for the specific case I needed
		// (one big component in yeast and drosophila net)
		ArrayList<Node> comp = components.get(0);
		int counter = uni.nextIntFromTo(0, comp.size()); // random start
		
		for (int i=0; i<numSubnets; i++) {
			
			// make a clone of the components so that we can remove things that we already added to the seed
			//ArrayList<ArrayList<Node>> componentsClone = (ArrayList<ArrayList<Node>>) components.clone();
			// select a component to add at random
			//int c = uni.nextIntFromTo(0, componentsClone.size()-1);
			
			ArrayList<Node> nextSeed = new ArrayList<Node>();
			for (int j=0; j<numSeedsFromComponents; j++) {
				int index = counter % comp.size();
				counter++;
				nextSeed.add(comp.get(index));
			}
			seeds.add(nextSeed);
		}
		
		// the seeds are copies of the original nodes, recover the original nodes
		ArrayList<ArrayList<Node>> originalSeeds = new ArrayList<ArrayList<Node>>();
		for (int i=0; i<seeds.size(); i++) {
			ArrayList<Node> nextSeed = seeds.get(i);
			ArrayList<Node> nextOriginalSeed = new ArrayList<Node>();
			for (int j=0; j<nextSeed.size(); j++) {
				nextOriginalSeed.add( sourceNetwork_.getNode(nextSeed.get(j).getLabel()) );
			}
			originalSeeds.add(nextOriginalSeed);
		}
		return originalSeeds;
	}
		

	// ============================================================================
	// PRIVATE CLASS

	/**
	 * Maps are usually sorted by keys, first of the two elements that compose
	 * an entry. This class allow to sort a map according to its values.
	 * Furthermore, if several entries have the same value, a standard sorting
	 * on the key would keep only one entry corresponding to this value and remove
	 * all the others. Here  are kept
	 * and are sorted in random order.
	 */
	private class DescendingModularitySorting implements Comparator<Integer> {

		private Map<Integer, Double> base_map;
		private Uniform uniform_;

		public DescendingModularitySorting(Map<Integer, Double> base_map) {
			this.base_map = base_map;
			uniform_ = GnwSettings.getInstance().getUniformDistribution();
		}

		public int compare(Integer arg0, Integer arg1) {

			if(!base_map.containsKey(arg0) || !base_map.containsKey(arg1))
				return 0;

			Double d0 = (Double) base_map.get(arg0);
			Double d1 = (Double) base_map.get(arg1);

			if (d0 + 1e-12 > d1 && d0 - 1e-12 < d1)
				return uniform_.nextBoolean() ? 1 : -1;
			else if (d0 < d1)
				return 1;
			else if (d0 > d1)
				return -1;
			else {
				assert false;
				return 0;
			}
		}
	}
	
}
