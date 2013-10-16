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
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;



/** 
 * A batch of experiments (used for DREAM5)
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class Condition {

	/** The author of these experiments */
	String author_ = "";
	/** The ID of the batch of this condition */
	int batchId_ = -1;
	/** The condition within the batch (if this is the second condition of a batch, batchConditionId_ would be 2) */
	int batchConditionId_ = -1;
	/** A unique ID for this specific condition (IDs start at 1) */
	int globalConditionId_ = -1;
	
	/** The experiments of this batch */
	private ArrayList<Experiment> experiments_ = new ArrayList<Experiment>();
	/** The experiments simulated with SDEs (only used for in silico compendia) */
	private ArrayList<Experiment> experimentsODE_ = null;
	/** The average of multiple repeats in experiments_ */
	private ArrayList<Experiment> experimentsAvg_ = null;

	/** The reference steady state of this condition (obtained by applying only the condition perturbation) */
	//private SteadyStateExperiment ssReference_ = null;
	/** The expression vector of the reference steady state of this condition */
	//private DoubleMatrix1D ssReferenceXy_ = null;
	
	/** Reference to the gene network */
	GeneNetwork grn_ = null;
	/** Reference to the compendium that this condition is part of */
	private Compendium compendium_ = null;

	/** Logger for this class */
	protected static Logger log_ = Logger.getLogger(Condition.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Default constructor
	 */
	public Condition(Compendium compendium) {
		grn_ = compendium.getGrn();
		compendium_ = compendium;
	}

	
	// ----------------------------------------------------------------------------
	
	/** Create a new experiment based on the string definition and add it to the batch, return this experiment */
	public Experiment addExperiment(ExperimentDefinition def) {
		
		// Set the batch author, id, and batch condition if this is the first experiment
		if (experiments_.size() == 0) {
			author_ = def.getAuthor();
			batchId_ = def.getBatchId();
			batchConditionId_ = def.getBatchCondition();
			
		// All experiments of the batch must have the same author, id, and batch condition
		} else if (!author_.equals(def.getAuthor())) {
			throw new RuntimeException("Authors don't match");
		} else if (batchId_ != def.getBatchId()) {
			throw new RuntimeException("Batch IDs don't match");
		} else if (batchConditionId_ != def.getBatchCondition()) {
			throw new RuntimeException("Batch conditions don't match");
		}

		// The experiment
		Experiment experiment = null;
		
		if (def.getTimePoints() == null) {
			// Create a new steady-state experiment
			experiment = new SteadyStateExperiment();
			experiment.setGrn(grn_);
			
		} else {
			assert def.getTimePoints().size() == 1;
			
			// Check whether this is an additional time point of an existing time series
			for (int i=0; i<experiments_.size(); i++) {
				TimeSeriesExperiment tsexp = null;
				
				if (TimeSeriesExperiment.class.isInstance(experiments_.get(i))) {
					tsexp = (TimeSeriesExperiment) experiments_.get(i);
				
					if (tsexp.getDefinition().matches(def)) {
						tsexp.addTimePoint(def.getTimePoints().first());
						return tsexp;
					}
				}
			}
			
			// It's a new time series (otherwise, we would have returned already above)
			experiment = new TimeSeriesExperiment();
			experiment.setGrn(grn_);
			experiment.setLabel(def.getDescription());
			((TimeSeriesExperiment)experiment).addTimePoint(def.getTimePoints().first());
		}
		
		experiment.setDefinition(def);
		experiments_.add(experiment);
		return experiment;
	}

	
	// ----------------------------------------------------------------------------

	/** Initialize the perturbation for every experiment of this condition */
	public void initializeExperimentPerturbations(DoubleMatrix1D conditionPerturbation) {
		
		LinkedHashMap<String, PerturbationDrug> drugPerturbationLookup = compendium_.getDrugPerturbationLookup();
		
		PerturbationMixed pertReference = new PerturbationMixed(grn_);
		pertReference.addToDeltaBasalActivation(conditionPerturbation, 1);
		
		//ssReference_ = new SteadyStateExperiment(Solver.type.ODE, pertReference, "Condition " + globalConditionId_ + ": reference");
		//ssReference_.setGrn(grn_);
		
		for (int e=0; e<experiments_.size(); e++) {
			Experiment exp = experiments_.get(e);
			ExperimentDefinition def = exp.getDefinition();
			
			// Create the perturbation and add it to the experiment
			PerturbationMixed pert = new PerturbationMixed(grn_);
			exp.setPerturbation(pert);
			
			// Add the perturbation associated with this condition
			pert.addToDeltaBasalActivation(conditionPerturbation, 1);
			
			// Add drug perturbations
			if (def.isDrugPerturbation()) {
				String[] drugs = def.getPerturbationVariables();
				ArrayList<Double> levels = def.getPerturbationLevels();

				assert levels.get(0) == -1 || levels.size() == drugs.length;
				
				for (int i=0; i<drugs.length; i++) {
					DoubleMatrix1D drugPerturbation = drugPerturbationLookup.get(drugs[i]).getPerturbations().viewRow(0);
					double level = 1;
					if (levels.get(0) != -1)
						level = levels.get(i);
					
					// Levels must be normalized at this point
					pert.addToDeltaBasalActivation(drugPerturbation, level);
				}
			}
			
			// Add genetic perturbations
			String[] deletions = def.getDeletedGenes();
			String[] overexpressions = def.getOverexpressedGenes();
			
			if (!deletions[0].equals("")) {
				for (int i=0; i<deletions.length; i++) {
					deletions[i] = getSubstitute(deletions[i]);
					pert.addGeneticPerturbation(deletions[i], true);
				}
			}
			
			if (!overexpressions[0].equals("")) {
				for (int i=0; i<overexpressions.length; i++) {
					overexpressions[i] = getSubstitute(overexpressions[i]);
					pert.addGeneticPerturbation(overexpressions[i], false);
				}
			}
			
		}
	}
	

	// ----------------------------------------------------------------------------
	
	/** Return true if there is no genetic or drug perturbation in this condition */
	public boolean isUnspecifiedPerturbation() {
		
		if (experiments_.size() < 0)
			throw new RuntimeException("There are no experiments in this batch");
		
		if (experiments_.size() > 1)
			return false;
		
		return experiments_.get(0).getDefinition().isUnspecifiedPerturbation();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** Return true if there a drug perturbation in this condition */
	public boolean containsDrugPerturbation() {
		
		if (experiments_.size() < 0)
			throw new RuntimeException("There are no experiments in this batch");
		
		for (int i=0; i<experiments_.size(); i++)
			if (experiments_.get(i).getDefinition().isDrugPerturbation())
				return true;
		
		return false;
	}

	
	// ----------------------------------------------------------------------------
	
	/** Return true if there a genetic perturbation in this condition */
	public boolean containsGeneticPerturbation() {
		
		if (experiments_.size() < 0)
			throw new RuntimeException("There are no experiments in this batch");
		
		for (int i=0; i<experiments_.size(); i++)
			if (experiments_.get(i).getDefinition().isGeneticPerturbation())
				return true;
		
		return false;
	}

	
	// ----------------------------------------------------------------------------
	
	/** Return true if there is a time series in this condition */
	public boolean containsTimeSeries() {
		
		if (experiments_.size() < 1)
			throw new RuntimeException("There are no experiments in this batch");
		
		for (int i=0; i<experiments_.size(); i++)
			if (TimeSeriesExperiment.class.isInstance(experiments_.get(i)))
				return true;
		
		return false;
	}

	
	// ----------------------------------------------------------------------------

	/** Run all experiments of this condition */
	public void runExperiments(DoubleMatrix1D xy0) {
		
		experimentsODE_ = new ArrayList<Experiment>();
		
		// Get the steady state associated with this condition in the absence of other perturbations
		//ssReference_.run(xy0);
		//ssReferenceXy_ = BenchmarkGenerator.constructInitialConditionFromWildType(ssReference_);
		
		for (int i=0; i<experiments_.size(); i++) {
			Experiment expSDE = experiments_.get(i);
			expSDE.setLabel("Condition " + globalConditionId_ + ": " + expSDE.getDefinition().getDescription());
			
			// Clone the experiment to run it first using ODEs
			Experiment expODE = expSDE.clone();
			experimentsODE_.add(expODE);
			
			// Set the solver type, set the repeats to 1 (no point in doing the deterministic simulation multiple times)
			expODE.setSolverType(Solver.type.ODE);
			expODE.setNumExperiments(1);
			expODE.run(xy0);
			//expODE.run(ssReferenceXy_);

			// Note, the time to convergence of the ODE has been magically transfered already because
			// expODE.timeToConvergenceODE == expSDE.timeToConvergenceODE (the two experiments are shallow copies!).
			expSDE.setSolverType(Solver.type.SDE);
			expSDE.run(xy0);
			//expSDE.run(ssReferenceXy_);
		}
		
	}
 
	
	// ----------------------------------------------------------------------------
	
	/** Compute the average of experiments with multiple repeats */
	public void computeAverageOfRepeats() {
		
		experimentsAvg_ = new ArrayList<Experiment>();
		
		for (int i=0; i<experiments_.size(); i++)
			experimentsAvg_.add(experiments_.get(i).computeAverageOfRepeats());
	}

	
	// ============================================================================
	// PRIVATE METHODS

	/** 
	 * If the given gene is not part of the network or is not a TF, randomly choose another TF as substitute.
	 * This function is only called when generating perturbations for an in silico compendium. For the same
	 * gene, always the same substitute is used (based on CompendiumInsilico.substitutionLookup_). 
	 */
	private String getSubstitute(String label) {

		assert !compendium_.getIsInvivo();
		int index = grn_.getIndexOfNode(label);
		
		// If the gene is not part of the compendium or is not a TF, randomly choose a TF 
		String substitute = label;
		
		if ((index == -1) || !grn_.getGene(index).getIsTf()) {
			LinkedHashMap<String, String> substitutionLookup = ((CompendiumInsilico) compendium_).getSubstitutions();
			substitute = substitutionLookup.get(label);
			
			if (substitute == null) {
				index = grn_.getRandomTf();
				substitute = grn_.getGene(index).getLabel();
				substitutionLookup.put(label, substitute);
			}
		}
		
		return substitute;
	}

	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getAuthor() { return author_; }
	public void setAuthor(String author) { author_ = author; }
	
	public int getBatchId() { return batchId_; }
	public void setBatchId(int id) { batchId_ = id; }

	public int getBatchConditionId() { return batchConditionId_; }
	public void setBatchConditionId(int id) { batchConditionId_ = id; }
	
	public int getGlobalConditionId() { return globalConditionId_; }
	public void setGlobalConditionId(int id) { globalConditionId_ = id; }

	public ArrayList<Experiment> getExperiments() { return experiments_; }
	public ArrayList<Experiment> getExperimentsAvg() { return experimentsAvg_; }
	public ArrayList<Experiment> getExperimentsODE() { return experimentsODE_; }
	

}
