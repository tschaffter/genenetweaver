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
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.random.Uniform;

import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.networks.ios.TSVParser;


/** 
 * A compendium of microarray experiments.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class CompendiumReader {
	
	/** Reference to the compendium */
	private Compendium comp_;
	/** The file with the expression matrix */
	private String expressionFile_;
	/** The file with the experiment descriptions */
	private String experimentFile_;
	/** The file with the experiment definitions */
	private String experimentDefFile_;
	/** The file with the list of genes */
	private String geneListFile_;
	/** The file with the list of TFs */
	private String tfListFile_;
	/** The file with the gold standard network */
	private String goldStandardFile_;

	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(CompendiumReader.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor
	 */
	CompendiumReader(Compendium compendium,
			String expressionFile,
			String experimentFile,
			String experimentDefFile,
			String geneListFile,
			String tfListFile,
			String goldStandardFile) {
		
		comp_ = compendium;
		expressionFile_ = expressionFile;
		experimentFile_ = experimentFile;
		experimentDefFile_ = experimentDefFile;
		geneListFile_ = geneListFile;
		tfListFile_ = tfListFile;
		goldStandardFile_ = goldStandardFile;
		
	}
	
	
	// ----------------------------------------------------------------------------

	/** Load the gene names, tf names, and gold standard edges (CompendiumInsilico overrides this method to load only the gold standard) */
	protected void loadNetwork() {

		if (comp_.isInvivo_) {
			// Load the genes and TFs of the compendium
			loadGenes();
			loadTfs();
			
			// Load the gold standard edges (nodes that are not in the compendium are skipped)
			if (goldStandardFile_ != null)
				loadGoldStandardEdges();
			
			// The TF flags have already been set, but this has to be called to set grn_.numTfs_
			comp_.grn_.markTfs();

		} else {
			// Load the gold standard nodes and edges
			loadGoldStandardNetwork();
			
			// Add random interactions
			double fracRandomEdges = ((CompendiumInsilico) comp_).fractionRandomEdges_; 
			if (fracRandomEdges > 0)
				comp_.grn_.addRandomEdges(fracRandomEdges);
			
			log_.info("Loaded " + comp_.grn_.getSize() + " genes (" + comp_.grn_.getNumTfs() + " TFs) and " + comp_.grn_.getNumEdges() + " edges from file " +  goldStandardFile_);
			if (GnwSettings.getInstance().getIgnoreAutoregulatoryInteractionsInEvaluation())
				log_.info("    - Autoregulatory interactions have been removed");
			if (fracRandomEdges > 0)
				log_.info("    - " + fracRandomEdges*100 + "% random interactions have been added");

		}
	}

	
	// ----------------------------------------------------------------------------

	/** Load all meta information */
	public void loadMetaInformation() {

		// Load the experiment definitions (creates the condition and experiment instances)
		loadExperimentDefs();
		// Set the number of repeats for every experiment
		initializeNumRepeats();

	}

	
	// ----------------------------------------------------------------------------

	/** Load the genes of this compendium (from a list of names) */
	public void loadGenes() {

		// Create a new network
		if (comp_.grn_ != null)
			throw new RuntimeException("Expected grn_ to be null");
		
		comp_.grn_ = new GeneNetwork();
		comp_.grn_.setId(comp_.name_);
		comp_.grn_.setDirected(true);
		comp_.geneNames_ = new ArrayList<String>();
		
		// Load the gene names
		ArrayList<String[]> geneNames = null;
		try {
			geneNames = Parser.readTSV(GnwSettings.getInstance().getURL(geneListFile_));
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load file " + geneListFile_, e);
		}

		log_.info("Loaded " + geneNames.size() + " genes from file " + geneListFile_);

		// Create and add the genes
		for (int i=0; i<geneNames.size(); i++) {
			String gene = geneNames.get(i)[0];
			
			if (comp_.grn_.containsNode(gene))
				throw new RuntimeException("Trying to add gene " + gene + ", but it is already in the network (possibly it's listed twice in " + geneListFile_);
			
			Gene newGene = new HillGene(comp_.grn_);
			newGene.setLabel(gene);
			comp_.grn_.addNode(newGene);
			comp_.geneNames_.add(gene);
		}

		// Reserves space and stuff
		comp_.grn_.setSize(comp_.grn_.getSize());
		
		assert comp_.grn_.getSize() == geneNames.size();
		assert comp_.grn_.getSize() == comp_.geneNames_.size();
	}

	
	// ----------------------------------------------------------------------------

	/** Mark all the genes in the given TF list as TFs */
	public void loadTfs() {
		
		//log.info("");
		//log.info("Augmenting gold standard with TFs from list " + filename);
		
		ArrayList<String[]> tfList = null;
		try {
			tfList = Parser.readTSV(GnwSettings.getInstance().getURL(tfListFile_));
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load file " + tfListFile_, e);
		}

		log_.info("Loaded " + tfList.size() + " TFs from file " + tfListFile_);

		for (int i=0; i<tfList.size(); i++) {
			String id = tfList.get(i)[0];
			Gene tf = (Gene) comp_.grn_.getNode(id);
			
			if (tf == null)
				throw new RuntimeException("TF " + id + " is not part of the network");
			else if (tf.getIsTf())
				throw new RuntimeException("TF " + id + " was already marked as TF (is it a duplicate in the list?)");
				//log.info(id);
			
			tf.setIsTf(true);
		}
		
		//log.info("");
	}

	
	// ----------------------------------------------------------------------------

	/** Increase the network size by adding decoy genes (used for DREAM5 challenge, makes cheating more difficult) */
	public void addDecoyGenes() {

		// The decoy TFs are a subset of the decoy genes
		int numDecoyTfs = (int) (comp_.fractionDecoyTfs_ * comp_.grn_.getNumTfs());
		int numDecoyGenes = (int) (comp_.fractionDecoyGenes_ * comp_.grn_.getSize());
		
		if (numDecoyGenes < numDecoyTfs)
			numDecoyGenes = numDecoyTfs;
		
		if (numDecoyGenes <= 0)
			return;
		
		log_.info("Adding " + comp_.fractionDecoyTfs_*100 + "% decoy TFs and " + comp_.fractionDecoyGenes_*100 + "% decoy genes");
		
		for (int i=0; i<numDecoyGenes; i++) {
			String label = "decoy" + (i+1); 
			Gene decoy = new HillGene(comp_.grn_);
			decoy.setLabel(label);
			comp_.grn_.addNode(decoy);
			//geneNames_.add(label);
			
			if (i<numDecoyTfs)
				decoy.setIsTf(true);
		}
		
		// Update the network size and the number of tfs
		comp_.grn_.setSize(comp_.grn_.getSize());
		comp_.grn_.markTfs();
	}

	
	// ----------------------------------------------------------------------------

	/** 
	 * Load the gold standard network of this compendium. Genes that have not previously been loaded
	 * will *not* be added (edges to/from these genes won't be added either). All regulators of the
	 * gold standard must be in the previously loaded list of TFs, otherwise an exception is thrown. 
	 */
	public void loadGoldStandardEdges() {
		
		if (comp_.grn_.getNumEdges() != 0)
			throw new RuntimeException("There are already edges in the gold standard of this compendium");
				
		Structure goldStandard = new Structure();
		try {
			goldStandard.load(GnwSettings.getInstance().getURL(goldStandardFile_), Structure.TSV);
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load file " + goldStandardFile_, e);
		}

		boolean noSelfLoops = GnwSettings.getInstance().getIgnoreAutoregulatoryInteractionsInEvaluation();
		int numSelfLoops = 0;
		
		HashSet<String> excludedGenes = new HashSet<String>();
		ArrayList<Edge> edges = goldStandard.getEdges();
		int numExcludedEdges = 0;
		
		for (int i=0; i<edges.size(); i++) {
			String tfStr = edges.get(i).getSource().getLabel();
			String targetStr = edges.get(i).getTarget().getLabel();
			
			Gene tf = (Gene) comp_.grn_.getNode(tfStr);
			Gene target = (Gene) comp_.grn_.getNode(targetStr);
			
			if (tf == null)
				excludedGenes.add(tfStr);
			if (target == null)
				excludedGenes.add(targetStr);
			if (tf == null || target == null) {
				numExcludedEdges++;
				continue;
			}
			
			if (noSelfLoops && tf == target) {
				numSelfLoops++;
				continue;
			}
			
			if (!tf.getIsTf())
				throw new RuntimeException("Regulator " + tfStr + " of the gold standard is not in the list of TFs");
			
			Edge e = new Edge(tf, target, edges.get(i).getType());
			comp_.grn_.addEdge(e);
		}
		
		comp_.grn_.setSigned(goldStandard.isSigned());

		assert comp_.grn_.getNumEdges() + numExcludedEdges + numSelfLoops == goldStandard.getNumEdges();
		
		log_.info("Loaded " + comp_.grn_.getNumEdges() + " edges from file " +  goldStandardFile_);
		log_.info("    - " + excludedGenes.size() + " genes (" + numExcludedEdges + " additional edges) that are not part of the compendium were ignored");
		if (numSelfLoops > 0)
			log_.info("    - " + numSelfLoops + " autoregulatory interactions were ingored");
	}
	
	
	// ----------------------------------------------------------------------------

	/** 
	 * Load the gold standard network of this compendium (nodes and edges). Use this for in silico networks, use
	 * loadGoldStandardEdges() for in vivo compendia (because they have more genes than the gold standard).
	 */
	public void loadGoldStandardNetwork() {
		
		if (comp_.grn_ != null)
			throw new RuntimeException("There is already a gold standard network in this compendium");
			
		Structure goldStandard = new Structure();
		try {
			goldStandard.load(GnwSettings.getInstance().getURL(goldStandardFile_), Structure.TSV);
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load file " + goldStandardFile_, e);
		}
		
		comp_.grn_ = new GeneNetwork(goldStandard);
		
		/*
		// Load an existing xml file, used for testing consistency with dream4
		comp_.grn_ = new GeneNetwork();
		try {
			comp_.grn_.load(GnwSettings.getInstance().getURL(goldStandardFile_), GeneNetwork.SBML);
		} catch (IOException e) {
			GnwMain.error(e);
		}
		*/
		
		comp_.grn_.setId(comp_.name_);
		comp_.grn_.setDirected(true);

		// Set the TF flags
		comp_.grn_.markTfs();

		boolean noSelfLoops = GnwSettings.getInstance().getIgnoreAutoregulatoryInteractionsInEvaluation();
		if (noSelfLoops)
			comp_.grn_.removeAutoregulatoryInteractions();
		
	}


	// ----------------------------------------------------------------------------

	/** Load the expression data */
	public void loadExpressionMatrix() {
		
		GnwSettings set = GnwSettings.getInstance();
		comp_.dataReady_ = true;
		
		// Rows are experiments and columns are genes. 
		// The columns are already returned in the right order, rows are given in the order of the file
		DoubleMatrix2D expressionMatrix = parseExpressionMatrix();
		
		// The expression matrix has values -1 for all decoy genes, fill these in with randomly sampled values
		randomlySampleValuesForDecoyGenes(expressionMatrix);
		
		// Load the experiment descriptions
		ArrayList<String[]> experiments = null;
		try {
			experiments = Parser.readTSV(set.getURL(experimentFile_));
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load compendium", e);
		}
		
		// Check that the dimensions of the data matrix is consistent with the number of genes and experiments
		//if (expressionMatrix.columns() != grn_.getSize())
			//throw new RuntimeException("The size of the data matrix (" + expressionMatrix.columns() + " genes) " +
					//"doesn't match the size of the loaded network (" + grn_.getSize() + " genes)");
		if (expressionMatrix.rows() != experiments.size())
			throw new RuntimeException("The size of the data matrix (" + expressionMatrix.rows() + " experiments) " +
					"doesn't match the number of the loaded experiment descriptions (" + experiments.size() + " experiments)");
		
		for (int i=0; i<expressionMatrix.rows(); i++) {
			String[] descr = experiments.get(i);
			String id = null;
			
			if (descr.length == 2)
				id = descr[0] + "\t" + descr[1];
			else if (descr.length == 3)
				id = descr[0] + "\t" + descr[1] + " " + descr[2];
			else
				throw new RuntimeException("Experiment descriptions must have 2 or 3 columns");

			// if the line ends with r1, r2, ..., get the repeat and remove it from the description
			int repeat = 1;
			boolean matches_rX = id.matches(".* r[1-9]"); 
			boolean matches_replicateX = id.matches(".*, replicate [1-9]");
			
			if (matches_rX || matches_replicateX) {
				int L = id.length();
				repeat = Integer.parseInt(id.substring(L-1, L));
				
				if (matches_rX)
					id = id.substring(0, L-3);
				else
					id = id.substring(0, L-13);
			}
			
			Experiment exp = comp_.experimentLookup_.get(id);
			if (exp == null)
				throw new RuntimeException("The following experiment was not found in the lookup table: " + id);
			
			if (TimeSeriesExperiment.class.isInstance(exp)) {
				Integer t = comp_.timePointLookup_.get(id);
				if (t == null)
					throw new RuntimeException("No time point was found in the lookup table for experiment: " + id);
				
				((TimeSeriesExperiment) exp).addData(expressionMatrix.viewRow(i), repeat, t);
				
			} else {
				((SteadyStateExperiment) exp).addData(expressionMatrix.viewRow(i), repeat);
			}	
		}
	}

	
	
	// ============================================================================
	// PRIVATE METHODS

	/** For every decoy gene, take the expression vector of another gene and shuffle it */
	private void randomlySampleValuesForDecoyGenes(DoubleMatrix2D expressionMatrix) {
		
		Uniform uni = GnwSettings.getInstance().getUniformDistribution();
		int numDecoys = comp_.grn_.getSize() - comp_.geneNames_.size();
		
		for (int i=0; i<numDecoys; i++) {
			
			// Label and index of the next decoy
			String decoyLabel = "decoy" + (i+1);
			int decoyIndex = comp_.grn_.getIndexOfNode(decoyLabel);
			
			// Label and index of the gene from which we sample the values
			String sampleLabel = comp_.geneNames_.get(uni.nextIntFromTo(0, comp_.geneNames_.size()-1));
			int sampleIndex = comp_.grn_.getIndexOfNode(sampleLabel);
			
			for (int j=0; j<expressionMatrix.rows(); j++) {
				assert expressionMatrix.get(j, decoyIndex) == -1;
				assert expressionMatrix.get(j, sampleIndex) >= 0;
				
				int k = uni.nextIntFromTo(0, expressionMatrix.rows()-1);
				double x = expressionMatrix.get(k, sampleIndex);
				expressionMatrix.set(j, decoyIndex, x);
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/** Load the experiment definitions */
	private void loadExperimentDefs() {
		
		GnwSettings set = GnwSettings.getInstance();
		ArrayList<String[]> experimentDef = null;
		
		// Read
		try {
			experimentDef = TSVParser.readTSV(set.getURL(experimentDefFile_));
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load compendium", e);
		}
		
		log_.info("Loaded experiment definitions from file " + experimentDefFile_);
		
		comp_.conditions_ = new ArrayList<Condition>();

		// Verify that there is a header
		if (!experimentDef.get(0)[0].startsWith("#"))
			throw new RuntimeException("Expected a header line starting with the character '#'");
		
		// Skip the header, start at 1
		for (int i=1; i<experimentDef.size(); i++) {
			
			ExperimentDefinition def = new ExperimentDefinition();
			String[] defStr = experimentDef.get(i);
			
			// In the yeast and staph compendia, the strain is a separate column that we merge with the description
			if (defStr.length == 11)
				defStr = mergeStrainAndDescription(defStr);
			
			def.parseDefinition(defStr);
			
			Condition cond = null;
			for (int j=0; j<comp_.conditions_.size(); j++) {
				if (def.sameCondition(comp_.conditions_.get(j).getExperiments().get(0).getDefinition())) {
					cond = comp_.conditions_.get(j);
					break;
				}
			}
						
			if (cond == null) {
				cond = new Condition(comp_);
				comp_.conditions_.add(cond);
			}
			
			// Create an experiment for this definition and add it to the condition
			Experiment exp = cond.addExperiment(def);

			// Add the description to the experiment and time point lookup tables
			String description = def.getAuthor() + "\t" + def.getDescription();
			
			if (comp_.experimentLookup_.containsKey(description) || comp_.timePointLookup_.containsKey(description))
				throw new RuntimeException("There is already an experiment with description: " + description);
			
			comp_.experimentLookup_.put(description, exp);
			
			if (def.getTimePoints() != null) {
				assert TimeSeriesExperiment.class.isInstance(exp);
				
				Integer t = def.getTimePoints().first();
				comp_.timePointLookup_.put(description, t);
			}
		}		
	}

	
	// ----------------------------------------------------------------------------

	/** In the yeast and staph compendia, the strain is a separate column that we merge with the description */
	private String[] mergeStrainAndDescription(String[] defStr) {

		assert defStr.length == 11;
		
		String[] merged = new String[10];
		merged[0] = defStr[0];
		merged[1] = defStr[1] + " " + defStr[2];
		
		for (int i=2; i<merged.length; i++)
			merged[i] = defStr[i+1];
		
		return merged;
	}


	// ----------------------------------------------------------------------------

	/** Initialize the number of repeats (Experiment.numExperiments_) for every experiment */
	private void initializeNumRepeats() {
		
		GnwSettings set = GnwSettings.getInstance();
		
		// Load the experiment descriptions
		ArrayList<String[]> experimentDescriptions = null;

		try {
			experimentDescriptions = Parser.readTSV(set.getURL(experimentFile_));
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load compendium", e);
		}
		
		for (int i=0; i<experimentDescriptions.size(); i++) {
			String[] descr = experimentDescriptions.get(i);
			String id = null;
			
			if (descr.length == 2)
				id = descr[0] + "\t" + descr[1];
			else if (descr.length == 3)
				id = descr[0] + "\t" + descr[1] + " " + descr[2];
			else
				throw new RuntimeException("Experiment descriptions must have 2 or 3 columns");
			
			// if the line ends with r1, r2, ..., get the repeat and remove it from the description
			int repeat = 1;
			boolean matches_rX = id.matches(".* r[1-9]"); 
			boolean matches_replicateX = id.matches(".*, replicate [1-9]");
			
			if (matches_rX || matches_replicateX) {
				int L = id.length();
				repeat = Integer.parseInt(id.substring(L-1, L));
				
				if (matches_rX)
					id = id.substring(0, L-3);
				else
					id = id.substring(0, L-13);
			}
			
			Experiment exp = comp_.experimentLookup_.get(id);
			if (exp == null)
				throw new RuntimeException("The following experiment was not found in the lookup table: " + id);
			
			if (TimeSeriesExperiment.class.isInstance(exp)) {
				Integer t = comp_.timePointLookup_.get(id);
				if (t == null)
					throw new RuntimeException("No time point was found in the lookup table for experiment: " + id);
				
				((TimeSeriesExperiment)exp).addRepeat(repeat, t);
				
			} else {
				((SteadyStateExperiment)exp).addRepeat(repeat);
			}
		}
		
		// Check that all time points of a time series experiments have the same number of repeats
		// => They don't
		/*
		for (int c=0; c<conditions_.size(); c++) {
			ArrayList<Experiment> experiments = conditions_.get(c).getExperiments();
			
			for (int e=0; e<experiments.size(); e++) {
				Experiment exp = experiments.get(e);

				if (TimeSeriesExperiment.class.isInstance(exp)) {
					ArrayList<Integer> repeats = ((TimeSeriesExperiment) exp).getRepeatsPerTimePoint();
					int r = exp.getNumExperiments();
				
					for (int i=0; i<repeats.size(); i++)
						if (r != repeats.get(i))
							throw new RuntimeException("Not all time points have the same number of repeats in experiment " + exp.getDefinition().getDescription());
				}
			}
		}
		*/
	}
	
	
	// ----------------------------------------------------------------------------

	/** 
	 * Load the expression matrix. In the dataFile, rows correspond to genes and experiments to columns.
	 * This function returns the transpose, i.e., a row for every experiment, and reorders the genes
	 * from the original (geneNames_) to the new order (grn_.getNodes()). 
	 */
	private DoubleMatrix2D parseExpressionMatrix() {
		
		GnwSettings set = GnwSettings.getInstance();
		ArrayList<String[]> data = null;
		
		try {
			data = TSVParser.readTSV(set.getURL(expressionFile_));
		} catch (Exception e) {
			log_.log(Level.WARNING, "Could not load compendium", e);
		}
		
		int numRealGenes = data.size();
		int numRealPlusDecoyGenes = comp_.grn_.getSize();
		int numExperiments = data.get(0).length;
		
		DoubleMatrix2D expressionMatrix = new DenseDoubleMatrix2D(numExperiments, numRealPlusDecoyGenes);
		expressionMatrix.assign(-1);
		
		for (int g=0; g<numRealGenes; g++) {
			String[] row_g = data.get(g);
			assert row_g.length == numExperiments;
			
			// The new index of gene g in the network
			int newIndex = comp_.grn_.getIndexOfNode(comp_.geneNames_.get(g));
			
			for (int e=0; e<numExperiments; e++)
				expressionMatrix.set(e, newIndex, Double.parseDouble(row_g[e]));
		}
		
		return expressionMatrix;
	}


	
	// ============================================================================
	// PRIVATE METHODS

	public String getGoldStandardFile() { return goldStandardFile_; }
	
}
