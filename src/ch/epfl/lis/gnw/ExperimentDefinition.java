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
import java.util.Arrays;
import java.util.TreeSet;
import java.util.logging.Logger;



/** 
 * Description of a microarray experiment.
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class ExperimentDefinition {
	
	/** The author */
	private String author_ = "";
	/** Text description */
	private String description_ = "";
	/** The batch id of this experiment (mandatory) */
	private int batchId_ = -1;
	/** The condition in the experiment batch (mandatory) */
	private int batchConditionId_ = -1;
	/** The IDs of the perturbation variables (optional) */
	private String[] perturbationVariables_ = null;
	/** The level of the perturbation (optional) */
	private ArrayList<Double> perturbationLevels_ = null;
	/** The type of treatment / how the perturbation was applied (optional) */
	private int treatment_ = -1;
	/** The IDs of the deleted genes (optional) */
	private String[] deletedGenes_ = null;
	/** A comma-separated list of overexpressed genes (optional) */
	private String[] overexpressedGenes_ = null;
	/** An explicit list of time points (only defined for time series experiments) */
	private TreeSet<Integer> timePoints_ = null;

	/** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(SteadyStateExperiment.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
    /** Default constructor */
	public ExperimentDefinition() { }
		
	
	// ----------------------------------------------------------------------------
	
	/** Parse the given definition */
	public void parseDefinition(String[] def) {

		if (def.length != 10)
			throw new RuntimeException("Experiment definition should have 10 fields: " + def);
		
		author_ = def[0];
		description_ = def[1];
		batchId_ = Integer.parseInt(def[2]);
		batchConditionId_ = Integer.parseInt(def[3]);
		perturbationVariables_ = def[4].split(",");

		// The level of the perturbation is optional
		perturbationLevels_ = new ArrayList<Double>();
		
		if (!def[5].equals("")) {
			String[] levels = def[5].split(",");
			for (int i=0; i<levels.length; i++)
				perturbationLevels_.add(Double.parseDouble(levels[i]));
		} else {
			perturbationLevels_.add(-1.0);
		}
		
		// Either no level is specified, or a level for each perturbation is given
		assert (perturbationLevels_.get(0) == -1) || (perturbationLevels_.size() == perturbationVariables_.length);
		
		// Treatment is also optional
		treatment_ = -1;
		if (!def[6].equals(""))
			treatment_ = Integer.parseInt(def[6]);
		
		deletedGenes_ = def[7].split(",");
		overexpressedGenes_ = def[8].split(",");
		
		// Time is optional
		timePoints_ = null;
		if (!def[9].equals("")) {
			timePoints_ = new TreeSet<Integer>();
			timePoints_.add(Integer.parseInt(def[9]));
		}		
	}

		
	// ----------------------------------------------------------------------------

	/** Returns true if the given definition matches this definition */
	public boolean matches(ExperimentDefinition def) {
		
		return sameCondition(def) &&
			Arrays.equals(perturbationVariables_, def.getPerturbationVariables()) &&
			perturbationLevels_.equals(def.getPerturbationLevels()) &&
			treatment_ == def.getTreatment() &&
			Arrays.equals(deletedGenes_, def.getDeletedGenes()) &&
			Arrays.equals(overexpressedGenes_, def.getOverexpressedGenes());
	}

	
	// ----------------------------------------------------------------------------

	/** Returns true if the given definition has the same batchId and batchCondition */
	public boolean sameCondition(ExperimentDefinition def) {
		
		boolean isMatch = batchId_ == def.getBatchId() &&
			batchConditionId_ == def.getBatchCondition();
		
		if (isMatch)
			assert author_.equals(def.getAuthor());
		// note, the description must not necessarily match because of the time
		
		return isMatch;
	}

	
	// ----------------------------------------------------------------------------

	/** Return true if there is no genetic or drug perturbation */
	public boolean isUnspecifiedPerturbation() {
		return perturbationVariables_[0].equals("") && 
			deletedGenes_[0].equals("") &&
			overexpressedGenes_[0].equals("");
	}
	
	
	// ----------------------------------------------------------------------------

	/** Return true if this is a drug perturbation */
	public boolean isDrugPerturbation() {
		return !perturbationVariables_[0].equals(""); 
	}
	

	// ----------------------------------------------------------------------------

	/** Return true if this is a genetic perturbation */
	public boolean isGeneticPerturbation() {
		return !deletedGenes_[0].equals("") || !overexpressedGenes_[0].equals("");
	}
	
	
	// ============================================================================
	// PRIVATE METHODS


	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getAuthor() { return author_; }
	public void setAuthor(String author) { author_ = author; }

	public String getDescription() { return description_; }
	public void setDescription(String description) { description_ = description; }

	public int getBatchId() { return batchId_; }
	public void setBatchId(int batchId) { batchId_ = batchId; }

	public int getBatchCondition() { return batchConditionId_; }
	public void setBatchCondition(int condition) { batchConditionId_ = condition; }

	public String[] getPerturbationVariables() { return perturbationVariables_; }
	public void setPerturbationVariables(String[] perturbationVariables) { perturbationVariables_ = perturbationVariables; }

	public ArrayList<Double> getPerturbationLevels() { return perturbationLevels_; }
	public void setPerturbationLevels(ArrayList<Double> perturbationLevels) { perturbationLevels_ = perturbationLevels; }

	public int getTreatment() { return treatment_; }
	public void setTreatment(int treatment) { treatment_ = treatment; }

	public String[] getDeletedGenes() { return deletedGenes_; }
	public void setDeletedGenes(String[] deletedGenes) { deletedGenes_ = deletedGenes; }

	public String[] getOverexpressedGenes() { return overexpressedGenes_; }
	public void setOverexpressedGenes(String[] overexpressedGenes) { overexpressedGenes_ = overexpressedGenes; }

    public TreeSet<Integer> getTimePoints() { return timePoints_; }
	public void setTimePoints(TreeSet<Integer> timePoints) { timePoints_ = timePoints; }
}
