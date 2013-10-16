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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.ios.TSVParser;


/** 
 * A compendium of microarray experiments.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class Compendium {
	
	/** The name of the compendium */
	String name_ = "compendium";
	/** Handles loading of data */
	CompendiumReader reader_ = null;
	/** Handles writing of data */
	CompendiumWriter writer_ = null;
	
	/** 
	 * The compendium is a list of experiments. An experiment is either a single measurement,
	 * which is assumed to be at steady-state, or a time series. Both steady-state and
	 * time-series experiments can have multiple repeats. 
	 */
	protected ArrayList<Condition> conditions_ = null;
	
	/** The gene network */
	protected GeneNetwork grn_ = null;

	/** The names of the genes (ordered as in the gene names file, should be same order as used in gene expression matrix file) */
	protected ArrayList<String> geneNames_ = null;
	
	/** True if this is real microarray data that has been loaded from a file */
	protected boolean isInvivo_ = true;
	/** True if the data has been loaded / generated (means no shuffling anymore please) */
	protected boolean dataReady_ = false;
	/** True if data should be anonymized for the DREAM challenge */
	protected boolean anonymize_ = false;
	/** Fraction of decoy TFs to add (to make cheating more difficult in DREAM5) */
	protected double fractionDecoyTfs_ = 0;
	/** Fraction of decoy genes to add (to make cheating more difficult in DREAM5) */
	protected double fractionDecoyGenes_ = 0;
    
	/** The perturbations associated with the conditions */
	protected PerturbationMultifactorial conditionPerturbations_ = null;
	/** Map of all perturbation variables (drug / environmental perturbations) */
	protected LinkedHashMap<String, PerturbationDrug> drugPerturbationLookup_ = new LinkedHashMap<String, PerturbationDrug>();
	/** Lookup table to get an experiment from its original unformatted string description */
	protected LinkedHashMap<String, Experiment> experimentLookup_ = new LinkedHashMap<String, Experiment>();
	/** Lookup table to get the time point of an experiment description */
	protected LinkedHashMap<String, Integer> timePointLookup_ = new LinkedHashMap<String, Integer>();
	
	/** Logger for this class */
	protected static Logger log_ = Logger.getLogger(Compendium.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor */
	Compendium(String name, 
			String expressionFile,
			String experimentFile,
			String experimentDefFile,
			String geneListFile,
			String tfListFile,
			String goldStandardFile) {
		
		name_ = name;
		
		reader_ = new CompendiumReader(this, 
				expressionFile,
				experimentFile,
				experimentDefFile,
				geneListFile,
				tfListFile,
				goldStandardFile);
		
		writer_ = new CompendiumWriter(this);
	}
	
	
	// ----------------------------------------------------------------------------

	/** Generate an in vivo benchmark (load experiment definitions, expression data, anonymize, compute averages, and write files) */
	public void generateBenchmark(boolean anonymize) {
		
		anonymize_ = anonymize;
		
		// Load experiment definitions and expression data, compute averages
		initialize();
		reader_.loadExpressionMatrix();
		computeAverageOfRepeats();
		
		// Remove gene labels (has to be done after the data is loaded) and write everything
		removeGeneLabels();
		writer_.write();

	}
	
	
	// ----------------------------------------------------------------------------

	/** Load meta information and the gold standard, organize the compendium, create instances for perturbations */
	protected void initialize() {

		// Load the network (gene names, tfs, gold standard---CompendiumInsilico overrides to only load goldstandard)
		reader_.loadNetwork();
		// Add decoys
		reader_.addDecoyGenes();

		// Load the experiment definitions
		reader_.loadMetaInformation();
		
		if (anonymize_) {
			// Anonymize conditions
			shuffleConditions();
			reorganizeConditionsByType();
			// Anonymize genes
			shuffleGenes();
			reorganizeGenesByType();
		}

		initializeGlobalConditionIds();
		initializeDrugPerturbations();
		normalizePerturbationLevels();
		
	}

	
	// ----------------------------------------------------------------------------
	
	/** Compute the average of experiments with multiple repeats */
	public void computeAverageOfRepeats() {
		
		for (int i=0; i<conditions_.size(); i++)
			conditions_.get(i).computeAverageOfRepeats();
	}

	
	// ============================================================================
	// STATIC METHODS

	/** Load Diogo's experiment descriptions, remove repeats, and save to a file */
	static public void removeRepeatsFromExperimentDescriptions(String filename) {
		
		try {
			ArrayList<String[]> data = TSVParser.readTSV(GnwSettings.getInstance().getURL(filename));
			int l = 0;
			
			while (l<data.size()) {
				String[] line = data.get(l);
				String exp = line[line.length-1];
				
				// if the line ends with r2, r3, ..., remove it
				if (exp.matches(".*r[2-9]") || exp.matches(".*, replicate [2-9]")) {
					data.remove(l);
				// else, just remove the (optional) r1 at the end
				} else {
					if (exp.matches(".* r1"))
						line[line.length-1] = exp.substring(0, exp.length()-3);
					else if (exp.matches(".*, replicate 1"))
						line[line.length-1] = exp.substring(0, exp.length()-13);
					l++;
				}
			}
			
			// Write the descriptions to a file
			TSVParser.writeTSV(GnwSettings.getInstance().getURL("experiments_without_repeats.tsv"), data);
			
		} catch (Exception e) {
			log_.log(Level.WARNING, "Compendium::removeRepeatsFromExperimentDescriptions(): " + e.getMessage(), e);
		}
	}


	// ============================================================================
	// PRIVATE / PROTECTED METHODS

	/** Initialize the globalConditionId_ of the conditions based on their index in conditions_ (starting at id 1) */
	private void initializeGlobalConditionIds() {

		for (int i=0; i<conditions_.size(); i++)
			conditions_.get(i).setGlobalConditionId(i+1);
	}

	
	// ----------------------------------------------------------------------------

	/** Shuffle the conditions for anonymization */
	private void shuffleConditions() {
		Collections.shuffle(conditions_);
	}
	
	
	// ----------------------------------------------------------------------------

	/** Shuffle the genes for anonymization */
	private void shuffleGenes() {
		
		if (dataReady_)
			throw new RuntimeException("Genes can't be shuffled after the data has been loaded");
		
		Collections.shuffle(grn_.getNodes());
	}


	// ----------------------------------------------------------------------------

	/** Removes gene labels but only if anonymize_ is true */
	protected void removeGeneLabels() {
		
		//GraphUtilities util = new GraphUtilities(grn_);
		//util.anonymizeGenes();
		
		if (!anonymize_)
			return;
		
		ArrayList<String[]> geneNames = new ArrayList<String[]>();
		String[] header = { "#ID", "Name" };
		geneNames.add(header);
		
		// replace the names
		for (int i=0; i<grn_.getSize(); i++) {
			String id = "G" + (i+1);
			String name = grn_.getNode(i).getLabel();
			String[] nextLine = { id, name };
			
			geneNames.add(nextLine);
			grn_.getNode(i).setLabel(id);
		}
		
		// update the experiment definitions (ids of deleted and overexpressed genes)
		for (int c=0; c<conditions_.size(); c++) {
			ArrayList<Experiment> exp = conditions_.get(c).getExperiments();
			
			for (int e=0; e<exp.size(); e++) {
				ExperimentDefinition def = exp.get(e).getDefinition();
				anonymizeGenes(def.getDeletedGenes(), geneNames);
				anonymizeGenes(def.getOverexpressedGenes(), geneNames);
			}
		}
		
		Parser.writeTSV(GnwSettings.getInstance().getOutputDirectory() + name_ + "_gene_ids.tsv", geneNames);
	}


	// ----------------------------------------------------------------------------
	
	/** Replace the gene names in the given array with the IDs in the names table */
	private void anonymizeGenes(String[] genes, ArrayList<String[]> names) {
		
		if (genes[0].equals(""))
			return;
		
		for (int i=0; i<genes.length; i++) {
			boolean found = false;
			
			for (int j=0; j<names.size(); j++) {
				if (genes[i].equals(names.get(j)[1])) {
					genes[i] = names.get(j)[0];
					found = true;
					break;
				}
			}
			if (!found)
				throw new RuntimeException("Gene " + genes[i] + " not found");
		}
	}
	

	// ----------------------------------------------------------------------------

	/** 
	 * Order batches by type (always list first steady-state and then time-series batches):
	 * 1. No named perturbation
	 * 2. Only drug perturbation
	 * 3. Only genetic perturbation
	 * 4. Drug + genetic perturbations
	 */
	@SuppressWarnings("unchecked")
	private void reorganizeConditionsByType() {

		ArrayList<Condition> oldList = (ArrayList<Condition>) conditions_.clone();
		int numExperimentBatches = conditions_.size();
		conditions_.clear();
		
		// 1. Unspecified perturbations steady-states
		for (int i=0; i<oldList.size();) { // no i++
			Condition batch = oldList.get(i); 
			
			if (batch.isUnspecifiedPerturbation() && !batch.containsTimeSeries()) {
				conditions_.add(batch);
				oldList.remove(i);
			} else {
				i++;
			}
		}
		
		// 1. Unspecified perturbations time-series
		for (int i=0; i<oldList.size();) { // no i++
			Condition batch = oldList.get(i); 
			
			if (batch.isUnspecifiedPerturbation()) {
				conditions_.add(batch);
				oldList.remove(i);
			} else {
				i++;
			}
		}

		// 2. Only drug perturbation steady-states
		for (int i=0; i<oldList.size();) { // no i++
			Condition batch = oldList.get(i);
			
			if (batch.containsDrugPerturbation() && !batch.containsGeneticPerturbation() && !batch.containsTimeSeries()) {
				conditions_.add(oldList.get(i));
				oldList.remove(i);
			} else {
				i++;
			}
		}
		
		// 2. Only drug perturbation time-series
		for (int i=0; i<oldList.size();) { // no i++
			Condition batch = oldList.get(i);
			
			if (batch.containsDrugPerturbation() && !batch.containsGeneticPerturbation()) {
				conditions_.add(oldList.get(i));
				oldList.remove(i);
			} else {
				i++;
			}
		}

		// 3. Only gene genetic perturbation steady-states
		for (int i=0; i<oldList.size();) { // no i++
			Condition batch = oldList.get(i);
			
			if (!batch.containsDrugPerturbation() && batch.containsGeneticPerturbation() && !batch.containsTimeSeries()) {
				conditions_.add(oldList.get(i));
				oldList.remove(i);
			} else {
				i++;
			}
		}
		
		// 3. Only genetic perturbation time-series
		for (int i=0; i<oldList.size();) { // no i++
			Condition batch = oldList.get(i);
			
			if (!batch.containsDrugPerturbation() && batch.containsGeneticPerturbation()) {
				conditions_.add(oldList.get(i));
				oldList.remove(i);
			} else {
				i++;
			}
		}

		// 4. Drug and genetic perturbation steady-states
		for (int i=0; i<oldList.size();) { // no i++
			Condition batch = oldList.get(i);
			
			if (batch.containsDrugPerturbation() && batch.containsGeneticPerturbation() && !batch.containsTimeSeries()) {
				conditions_.add(oldList.get(i));
				oldList.remove(i);
			} else {
				i++;
			}
		}

		// 4. Drug and genetic perturbation time-series
		for (int i=0; i<oldList.size();) { // no i++
			Condition batch = oldList.get(i);
			
			if (batch.containsDrugPerturbation() && batch.containsGeneticPerturbation()) {
				conditions_.add(oldList.get(i));
				oldList.remove(i);
			} else {
				i++;
			}
		}

		assert oldList.size() == 0;
		assert numExperimentBatches == conditions_.size();
		
		initializeGlobalConditionIds();
	}
	
	
	// ----------------------------------------------------------------------------

	/** Order genes such that regulators come first in the list */
	private void reorganizeGenesByType() {

		if (dataReady_)
			throw new RuntimeException("Genes can't be reordered after the data has been loaded");
		
		ArrayList<Node> oldList = grn_.getNodes();
		ArrayList<Node> newList = new ArrayList<Node>();

		// First add all the regulators
		for (int i=0; i<oldList.size(); i++)
			if (((Gene) oldList.get(i)).getIsTf())
				newList.add(oldList.get(i));
		
		// Then add all the non-regulators
		for (int i=0; i<oldList.size(); i++)
			if (!((Gene) oldList.get(i)).getIsTf())
				newList.add(oldList.get(i));
		
		grn_.setNodes(newList);
		
		assert newList.size() == oldList.size();
	}

		
	// ----------------------------------------------------------------------------

	/** Create the perturbationVariables_ */
	private void initializeDrugPerturbations() {

		drugPerturbationLookup_.clear();
		int nextId = 1; // The ID of the next perturbation variable
		
		// For all conditions
		for (int i=0; i<conditions_.size(); i++) {
			ArrayList<Experiment> experiments = conditions_.get(i).getExperiments();
			
			// For all experiments
			for (int j=0; j<experiments.size(); j++) {
				ExperimentDefinition def = experiments.get(j).getDefinition();
				String[] varNames = def.getPerturbationVariables();
				ArrayList<Double> varLevels = def.getPerturbationLevels();
				
				if (varNames[0].equals(""))
					continue;

				for (int k=0; k<varNames.length; k++) {
					// Get the variable of this id
					PerturbationDrug var = drugPerturbationLookup_.get(varNames[k]);
					// If it's not yet in the map, create a new one and add it
					if (var == null) {
						var = new PerturbationDrug(varNames[k], grn_);
						var.setId(nextId++);
						drugPerturbationLookup_.put(varNames[k], var);
					}
					// Update the min and max level
					if (varLevels.get(0) != -1) {
						var.updateRange(varLevels.get(k));
					}
				}
			}
		}
	}
		
	
	// ----------------------------------------------------------------------------

	/** Normalize the levels of all perturbation variables by dividing them by their maximum */
	private void normalizePerturbationLevels() {

		for (int i=0; i<conditions_.size(); i++) {
			ArrayList<Experiment> experiments = conditions_.get(i).getExperiments();
			
			// For all experiments
			for (int j=0; j<experiments.size(); j++) {
				ExperimentDefinition def = experiments.get(j).getDefinition();
				String[] varNames = def.getPerturbationVariables();
				ArrayList<Double> varLevels = def.getPerturbationLevels();
				
				if (varNames[0].equals("") || varLevels.get(0) == -1)
					continue;

				assert varNames.length == varLevels.size();
				
				for (int k=0; k<varNames.length; k++) {
					// Get the variable of this id
					PerturbationDrug var = drugPerturbationLookup_.get(varNames[k]);
					if (var == null) 
						throw new RuntimeException("Unknown perturbation variable: " + varNames[k]);
					
					// Normalize
					varLevels.set(k, varLevels.get(k)/var.getMaxLevel());
				}
			}
		}
	}

	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getName() { return name_; }
	public void setName(String name) { name_ = name; }

	public ArrayList<Condition> getConditions() { return conditions_; }
	
	public void setAnonymize(boolean anonymize) { anonymize_ = anonymize; }

	public boolean getIsInvivo() { return isInvivo_; }
	
	public void setFractionDecoyTfs(double frac) { fractionDecoyTfs_ = frac; }
	public void setFractionDecoyGenes(double frac) { fractionDecoyGenes_ = frac; }
	
	public GeneNetwork getGrn() { return grn_; }
	public void setGrn(GeneNetwork grn) { grn_ = grn; }

	public LinkedHashMap<String, PerturbationDrug> getDrugPerturbationLookup() {
		return drugPerturbationLookup_;
	}


}
