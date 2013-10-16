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
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.random.Uniform;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.imod.ImodNetwork;

import jigcell.sbml2.Compartment;
import jigcell.sbml2.KineticLaw;
import jigcell.sbml2.Model;
import jigcell.sbml2.Parameter;
import jigcell.sbml2.Reaction;
import jigcell.sbml2.SBMLLevel2Document;
import jigcell.sbml2.Species;
import jigcell.sbml2.ModifierSpeciesReference;


/** This class represents a gene network.
 * 
 * It extends ImodNetwork, thus it contains all the
 * genes and the edges. Furthermore, it implements the state (gene and protein expression
 * levels) and functions to compute the production rates and to load and save the network
 * in SBML format.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */ 
public class GeneNetwork extends ImodNetwork { 

	/** Current gene expression levels */
	private DoubleMatrix1D x_ = null;
	/** Current protein expression levels */
	private DoubleMatrix1D y_ = null;
	/** The number of regulators in the network (must be initialized using markTfs()) */
	private int numTfs_ = -1;
	
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(GeneNetwork.class.getName());
	/** SBML file format (see load()) */
	public static final int SBML = 4;
		
	
	// ============================================================================
	// INITIALIZATION
	
	/**
	 * Default constructor
	 */
	public GeneNetwork() {
		super();
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Constructor from a network (copies the structure of the given network)
	 */
	public GeneNetwork(Structure network) {

		// initialize with members of network, except for the nodes and the edges
		super(network);
		nodes_.clear();
		edges_.clear();
		
		for (int i=0; i < network.getSize(); i++) {
			HillGene g = new HillGene(this);
			g.setLabel(network.getNode(i).getLabel());
			nodes_.add(g);
		}
		
		Node source, target;
		Edge edge;
		
		for (int i=0; i < network.getNumEdges(); i++) {
			edge = network.getEdge(i);
			// Get the nodes in this.nodes_ and *not* in network.nodes_
			source = getNode(edge.getSource().getLabel());
			target = getNode(edge.getTarget().getLabel());
			edges_.add( new Edge(source, target, edge.getType()) ); //, edge.getLabel()) );
		}
		setSize(getSize());
	}
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Set network size, reserve space in x_ and y_ (does not affect members of the superclass)
	 * @param numGenes Size of the network
	 */
	public void setSize(int numGenes) {
		
		x_ = new DenseDoubleMatrix1D(numGenes);
		x_.assign(-1);
		if (GnwSettings.getInstance().getModelTranslation()) {
			y_ = new DenseDoubleMatrix1D(numGenes);
			y_.assign(-1);
		} else
			y_ = x_;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Based on W_, determine for each gene which are its inputs
	 */
	public void initializeInputWiring() {
		
		int size = getSize();
		
		for (int i=0; i < size; i++) {
			ArrayList<Gene> inputs = getInputs((Gene)nodes_.get(i));
			((Gene)nodes_.get(i)).setInputGenes(inputs);
		}
	}

		
	// ----------------------------------------------------------------------------
	
	/**
	 * Random initialization of the dynamical model (based on the fixed topology defined by W_)
	 */
	public void randomInitialization() {
		
		log_.log(Level.INFO, "\nRandom initialization of " + id_);

		// set the inputs for the genes based on W_
		initializeInputWiring();
		int size = nodes_.size();

		Gene gene = null;
		for (int i=0; i < size; i++) {
			gene = (Gene)nodes_.get(i);
			gene.randomInitialization();
			log_.log(Level.INFO, id_ + ": " + gene.getLabel() + ": " + gene.toString());
		}
		// If the network was unsigned before the initialization, we have to set the types
		// of the edges (enhancing / inhibiting) accordingly. If the network was signed,
		// this just sets the same values that are already there.
		setEdgeTypesAccordingToDynamicalModel();
		signed_ = true;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Compute the rate of change of all state variables x (and y if translation is modelled).
	 * The result is returned concatenated into one array [dxdt dydt].
	 * @param xy Used to set the gene expressions x and protein concentrations y (if translation)
	 * @param dxydt Variations of x and y (output)
	 */
	public void computeDxydt(double[] xy, double[] dxydt) {
		
		boolean modelTranslation = GnwSettings.getInstance().getModelTranslation();
		//double[] dxydt = new double[xy.length];
		int size = getSize();
		
		for (int i=0; i<size; i++)
			x_.set(i, xy[i]);
		
		if (modelTranslation)
			for (int i=0; i<size; i++)
				y_.set(i, xy[size+i]);
		else
			y_ = x_;
		
		// dxydt temporarily used to store the production rates of mRNA
		computeMRnaProductionRates(dxydt);
		
		for (int i=0; i < size; i++)
			dxydt[i] = dxydt[i] - ((Gene)nodes_.get(i)).computeMRnaDegradationRate(x_.get(i));
		
		if (modelTranslation)
			for (int i=0; i<size; i++)
				dxydt[size+i] = ((Gene)nodes_.get(i)).getMaxTranslation()*x_.get(i) - ((Gene)nodes_.get(i)).computeProteinDegradationRate(y_.get(i));
	}

	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Load a gene network from an SBML file. Overrides Structure.load(). Format must
	 * be equal GeneNetwork.SBML. Note, the SBML file must be in the exact same format
	 * as the SBML files produced by writeSBML(). In particular, we assume that reactions are listed
	 * *ordered* as we do in writeSBML().
	 * @param filename URL to the file describing the network to load
	 * @param format File format (GML, DOT, etc.)
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public void load(URL filename, int format) throws IOException {
		
		if (format != SBML)
			throw new IllegalArgumentException("GeneNetwork.load(): format must be equal GeneNetwork.SBML");
		
		// ----------------------
		// Load the SBML document
		SBMLLevel2Document sbml2Doc = null;
		Model model = null;
		
		File f = new File(filename.getPath());
		id_ = f.getName();
		
		URLConnection uc = filename.openConnection();

		sbml2Doc = SBMLLevel2Document.readDocument(uc.getInputStream());
		model = sbml2Doc.getModel();
		
		List<Species> species = model.getSpecies();
		int size = species.size();

		// -----------------------------------------
		// Set the network size and create the genes
		// do not count the species _void_
		setSize(species.size()-1);

		for (int g=0; g < size; g++) {
			if (!species.get(g).getId().equals("_void_")) {
				HillGene hg = new HillGene(this);
				hg.setLabel(species.get(g).getId());
				nodes_.add(hg);
			}
		}
		
		ArrayList<String> parameterNames = new ArrayList<String>(); // the names of the parameters
		ArrayList<Double> parameterValues = new ArrayList<Double>(); // the values of the parameters
		ArrayList<Gene> inputIndexes = new ArrayList<Gene>(); // the indexes of the inputs
		
		// 2 loops for one gene: both synthesis and degradation reactions
		// (we assume that reactions are listed *ordered* as we do in writeSBML())
		int counter = 0;
		for (
		Iterator<?> i = model.getReactions().iterator(); i.hasNext(); ) {
			
			// get the next reaction and the modifiers
			Reaction r = (Reaction) i.next();
			Gene target = (Gene)getNode(getGeneReactantId(r));
			
			if (target == null)
				log_.log(Level.WARNING, "Error: " + getGeneReactantId(r));
	
			// get all the genes modifying the target gene
			List<?> modifiers = r.getModifier();

			// Set edges (if this is a degradation reaction, there are no modifiers and this loop is not entered)
			for (
			Iterator<?> j = modifiers.iterator(); j.hasNext();) {
				ModifierSpeciesReference speciesReference = (ModifierSpeciesReference) j.next();
				Gene source = (Gene)getNode(speciesReference.getSpecies());
				inputIndexes.add(source);
				
				// The edge type is unknown for now, it is initialized later
				edges_.add(new Edge(source, target, Edge.UNKNOWN));
			}
		
			// compile parameters list of gene
			for (
			Iterator<?> k = r.getKineticLaw().getParameter().iterator(); k.hasNext();) {
				Parameter param = (Parameter)k.next();
				parameterNames.add(param.getId());
				parameterValues.add(param.getValue());
			}
			
			// in the second iteration for this gene
			if (counter%2 == 1) {
				// set parameters in gene
				target.initialization(parameterNames, parameterValues, inputIndexes);
				inputIndexes = new ArrayList<Gene>(); // don't clear because the reference was copied to the gene
				parameterNames.clear(); // reset (they were not copied)
				parameterValues.clear();
			}
			counter++;
		}
		setEdgeTypesAccordingToDynamicalModel();
		signed_ = true;
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Set the type (enhancing, inhibiting, dual, or unknown) of all edges according
	 * to the dynamically model.
	 */
	public void setEdgeTypesAccordingToDynamicalModel() {
		
		for (int i=0; i<nodes_.size(); i++) {
			((HillGene)nodes_.get(i)).setInputEdgeTypesAccordingToDynamicalModel();
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Return a list of all genes that perturbe the gene given in parameter.
	 * @param gene Gene perturbed
	 * @return Instance of all genes that perturbed the gene given in parameter.
	 */
	public ArrayList<Gene> getInputs(Gene gene) {
		
		ArrayList<Gene> inputs = new ArrayList<Gene>();

		for (int i=0; i < edges_.size(); i++) {
			if (edges_.get(i).getTarget().getLabel().equals(gene.getLabel())) {
				String source = edges_.get(i).getSource().getLabel();
				inputs.add((Gene) getNode(source));
			}
		}
		
		return inputs;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return gene id of a reaction.
	 * @param r Instance of Reaction
	 * @return Gene id of the reaction r
	 */
	public String getGeneReactantId(Reaction r) {
		
		// Example: G1_synthesis or G1_degradation -> id = "G1"
		//StringTokenizer st = new StringTokenizer(r.getId(), "_");
		//String id = st.nextToken();
		// problem: if the id also has '_', the above code fails
		String id = r.getId();
		id = id.substring(0, id.lastIndexOf("_"));
		return id;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Save the gene network to an SBML file. If the argument is null, use the network id.
	 * @param filename URL to the file describing the network to load
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public int writeSBML(URL filename) throws IOException {
		
		Model model = new Model(id_);
		model.getNotes ().add (comment_); // save network description
		
		log_.log(Level.INFO, "Writing file " + filename.getPath());
		
		int size = getSize();
		
		Compartment compartment = new Compartment ("cell", "cell");
		compartment.setSize (1);
		model.addCompartment (compartment);

		// Hack in pattern expression in sbml2 library to allow any char to compose
		// the identifier of a gene (hack in top of SBaseId.java)
		Species[] species = new Species[size+1];
  
		for (int s=0; s < size; s++) { // save gene as species
//			species[s] = new Species(nodeIds_.get(s), nodeIds_.get(s));
			species[s] = new Species(nodes_.get(s).getLabel(), nodes_.get(s).getLabel());
			species[s].setCompartment(compartment);
			//species[s].setInitialAmount(?); // maybe save the wild-type steady state?
			model.addSpecies(species[s]);
		}
		
		// create the void species
		species[size] = new Species("_void_", "_void_");
		species[size].setCompartment(compartment);
		species[size].setInitialAmount(0);
		species[size].setBoundaryCondition(true);
		species[size].setConstant(true);
		model.addSpecies(species[size]);
  
  
		// SET SYNTHESIS AND DEGRADATION REACTIONS FOR EVERY GENE
		for (int i=0; i<size; i++) {

			// the ID of gene i
//			String currentGeneID = nodeIds_.get(i);
			String currentGeneID = nodes_.get(i).getLabel();
			// The modifiers (regulators) of gene i
			ArrayList<Gene> inputIndexes = ((Gene)nodes_.get(i)).getInputGenes();
  
			// SYNTHESIS REACTION
			KineticLaw kineticLaw = new KineticLaw ();
			String reactionId = currentGeneID + "_synthesis";
			String name = reactionId + ": " + ((Gene)nodes_.get(i)).toString();
			Reaction reaction = new Reaction (reactionId, name);
			reaction.setReversible (false);
			reaction.addReactant(species[size]);
			reaction.addProduct(species[i]);
			for (int r=0; r<inputIndexes.size(); r++) // set gene modifiers
//				reaction.addModifier(species[inputIndexes.get(r)]);
				reaction.addModifier(species[nodes_.indexOf(inputIndexes.get(r))]);
  
			ArrayList<String> names = new ArrayList<String>(); // parameters names
			ArrayList<Double> values = new ArrayList<Double>(); // parameters values
			((Gene)nodes_.get(i)).compileParameters(names, values);
			
			// save gene parameters (note, the first param is the degradation rate)
			for (int p=1; p<names.size(); p++) {
				String id = names.get(p);// + "_" + currentGeneID;
				kineticLaw.addParameter(new Parameter(id, id, values.get(p)));
			}
			reaction.setKineticLaw(kineticLaw);
			model.addReaction(reaction);
  
			// DEGRADATION REACTION
			kineticLaw = new KineticLaw ();
			reactionId = currentGeneID + "_degradation";
			reaction = new Reaction (reactionId, reactionId);
			reaction.setReversible(false);
			reaction.addReactant(species[i]);
			reaction.addProduct(species[size]);
  
			String id = names.get(0);
			kineticLaw.addParameter(new Parameter(id, id, values.get(0)));
			
			reaction.setKineticLaw (kineticLaw);
			model.addReaction (reaction);
		}
		
		try
		{
			// PRINT FILE
			SBMLLevel2Document sbml2Doc = new SBMLLevel2Document(model);
			if (filename == null)
				filename = new URL(id_ + ".xml");
			sbml2Doc.writeDocument(new FileWriter(new File(filename.toURI()), false));
		}
		catch (URISyntaxException e)
		{
			log_.log(Level.WARNING, "URISyntaxException", e);
		}
		
		return 0;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Get a string with all gene labels separated by tabs (often used as header when printing data) */
	public String getHeader(boolean includeProteins) {

		String header = nodes_.get(0).getLabel();
		for (int i=1; i<nodes_.size(); i++)
			header += "\t" + nodes_.get(i).getLabel();
		
		if (includeProteins)
			for (int i=1; i<nodes_.size(); i++)
				header += "\t" + nodes_.get(i).getLabel() + "_prot";
		header += "\n";
		return header;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** Get the header as an array of String */
	public String[] getHeaderArray() {

		String[] header = new String[nodes_.size()];
		
		for (int i=0; i<nodes_.size(); i++)
			header[i] = nodes_.get(i).getLabel();
		
		return header;
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Create a subnetwork from a list of nodes.
	 * @param name Name of the new sub-network
	 * @param seed Group of nodes
	 * @return Sub-network
	 */
	public GeneNetwork getSubnetwork(String name, ArrayList<Node> seed)
	{
		GeneNetwork network = new GeneNetwork();
		network.id_ = name;
	
		// Copies of all the nodes
		for (int i=0; i < seed.size(); i++) {
			Node gene = seed.get(i);
			if (gene instanceof HillGene) {
//				System.out.println("GeneHill");
				network.addNode( ((HillGene) gene).copy() );
			}
		}
		
		// Set the topology (edges)
		Node source, target;
		Edge edge;
		
		for (int i=0; i < edges_.size(); i++)
		{
			edge = edges_.get(i);
			// Search in network if the source and target nodes exist
			source =  network.getNode( edge.getSource().getLabel() );
			target =  network.getNode( edge.getTarget().getLabel() );
			
			if (source != null && target != null)
				network.addEdge( new Edge(source, target, edge.getType()) ); //, edge.getLabel()) );
		}
		return network;
	}	
		
	
	// ----------------------------------------------------------------------------

	/** Set the flags Gene.isTf_ of all regulators, set numTfs_ */
	public void markTfs() {
		
		for (int i=0; i<edges_.size(); i++)
			((Gene) edges_.get(i).getSource()).setIsTf(true);

		numTfs_ = 0;
		for (int i=0; i<nodes_.size(); i++)
			if (getGene(i).getIsTf())
				numTfs_++;
	}

	
	// ----------------------------------------------------------------------------

	/** Get the index of a randomly selected TF */
	public int getRandomTf() {
		
		if (numTfs_ < 1)
			throw new RuntimeException("There are no TFs in the network (make sure markTfs() has been called)");
		
		int tfIndex = GnwSettings.getInstance().getUniformDistribution().nextIntFromTo(0, numTfs_-1);
		int tfCount = -1;
		int index = 0;

		for (; index<nodes_.size(); index++) {
			if (getGene(index).getIsTf()) {
				tfCount++;
				if (tfCount == tfIndex)
					break;
			}
		}
		
		assert index < nodes_.size();
		return index;
	}
	
	
	// ----------------------------------------------------------------------------

	/** Add the give fraction of random edges to the network (outgoing edges are only added to existing regulators, no duplicate edges will be created) */
	public void addRandomEdges(double fractionRandomEdges) {
		
		int numRandomEdges = (int) (fractionRandomEdges * edges_.size());
		Uniform uni = GnwSettings.getInstance().getUniformDistribution();
		boolean noSelfLoops = GnwSettings.getInstance().getIgnoreAutoregulatoryInteractionsInEvaluation();
		
		int counter = 0;
		while (counter < numRandomEdges) {
			
			Node tf = nodes_.get(getRandomTf());
			Node gene = nodes_.get( uni.nextIntFromTo(0, nodes_.size()-1) );
			
			if (noSelfLoops && tf == gene)
				continue;
			
			boolean duplicateEdge = false;
			for (int j=0; j<edges_.size(); j++) {
				if (edges_.get(j).getSource() == tf && edges_.get(j).getTarget() == gene) {
					duplicateEdge = true;
					break;
				}
			}
			if (duplicateEdge)
				continue;
			
			addEdge(new Edge(tf, gene));
			counter++;
		}
	}

	
	// ============================================================================
	// PRIVATE FUNCTIONS
		
	/**
	 * Compute the production rate for all the genes. This function is identical to
	 * the one below, except that the argument is passed as double[] instead of
	 * DoubleMatrix1d. We need both because the SDE implementation works with
	 * DoubleMatrix1d and the ODE implementation works with arrays.
	 * NOTE: x_ must be set before calling this function.
	 * @param productionRates Return the production rates of all the genes
	 */
	protected void computeMRnaProductionRates(double[] productionRates) {
		
		int size = getSize();
		
		for (int i=0; i < size; i++) {
			if (GnwSettings.getInstance().getModelTranslation())
				productionRates[i] = ((Gene)nodes_.get(i)).computeMRnaProductionRate(i, y_);
			else
				productionRates[i] = ((Gene)nodes_.get(i)).computeMRnaProductionRate(i, x_);
		}
	}
	

	/**
	 * Compute the production rate for all the genes. This function is identical to
	 * the one above, except that the argument is passed as DoubleMatrix1d instead of
	 * double[]. We need both because the SDE implementation works with
	 * DoubleMatrix1d and the ODE implementation works with arrays.
	 * NOTE: x_ must be set before calling this function.
	 * @param productionRates Return the production rates of all the genes
	 */
	protected void computeMRnaProductionRates(DoubleMatrix1D productionRates) {
		
		int size = getSize();
		
		for (int i=0; i < size; i++) {
			if (GnwSettings.getInstance().getModelTranslation())
				productionRates.set(i, ((Gene)nodes_.get(i)).computeMRnaProductionRate(i, y_));
			else
				productionRates.set(i, ((Gene)nodes_.get(i)).computeMRnaProductionRate(i, x_));
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	protected void computeProteinProductionRates(DoubleMatrix1D productionRates) {
		
		int size = getSize();
		
		for (int i=0; i<size; i++)
			productionRates.set(i, ((Gene)nodes_.get(i)).computeProteinProductionRate(x_.get(i)));
	}
	
	
	// ----------------------------------------------------------------------------
	
	protected void computeMRnaDegradationRates(DoubleMatrix1D degradationRates) {
		
		int size = getSize();
		
		for (int i=0; i<size; i++)
			degradationRates.set(i, ((Gene)nodes_.get(i)).computeMRnaDegradationRate(x_.get(i)));
	}
	
	
	// ----------------------------------------------------------------------------
	
	protected void computeProteinDegradationRates(DoubleMatrix1D degradationRates) {
		
		int size = getSize();
		
		for (int i=0; i<size; i++)
			degradationRates.set(i, ((Gene)nodes_.get(i)).computeProteinDegradationRate(y_.get(i)));
	}
	
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public Gene getGene(int i) { return (Gene)nodes_.get(i); }
	
	public DoubleMatrix1D getX() { return x_; }
	public void setX(DoubleMatrix1D x) { x_ = x; }
	
	public DoubleMatrix1D getY() { return y_; }
	public void setY(DoubleMatrix1D y) { y_ = y; }
	
	public int getNumTfs() { return numTfs_; }
	
}
