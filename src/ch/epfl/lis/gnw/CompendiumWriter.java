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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;


/** 
 * A compendium of microarray experiments.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class CompendiumWriter {
	
	
	/** Reference to the compendium */
	private Compendium comp_;
	/** The output directory */
	String outputDir_ = GnwSettings.getInstance().getOutputDirectory();

	/** Logger for this class */
	protected static Logger log_ = Logger.getLogger(CompendiumWriter.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor */
	CompendiumWriter(Compendium comp) {
		comp_ = comp;
	}
	
	
	// ----------------------------------------------------------------------------

	/** Write the complete set of files for a compendium */
	public void write() {
		
		writeTfList();
		writeGoldStandard();

		writeExpressionMatrix("");
		writeExperimentDefs();
		
		writeExpressionMatrixAvg();
		writeExperimentDefsAvg();
		
		if (!comp_.isInvivo_) {
			// Experiment definitions are the same as those of the average profiles
			writeExpressionMatrixODE();
			writePerturbations();
		}

		if (comp_.anonymize_) {
			writeAnonymizedPerturbations();
			writeAnonymizedConditions();
		}
		
	}
	
	
	// ----------------------------------------------------------------------------

	/** Write the expression matrix (same order as experiment definitions!) */
	public void writeExpressionMatrix(String postfix) {
		
		ArrayList<String[]> expressionMatrix = new ArrayList<String[]>();
		expressionMatrix.add(comp_.grn_.getHeaderArray());
		
		for (int i=0; i<comp_.conditions_.size(); i++)
			expressionMatrix.addAll(
					expressionMatrixToString(comp_.conditions_.get(i).getExperiments()) );

		String filename = outputDir_ + comp_.name_ + "_expression_data" + postfix + ".tsv";
		Parser.writeTSV(filename, expressionMatrix);
		
		if (GnwSettings.getInstance().getOutputGenesInRows()) {
			expressionMatrix = transposeExpressionMatrix(expressionMatrix, assembleExperimentDefs());
		
			filename = outputDir_ + comp_.name_ + "_expression_data" + postfix + "_t.tsv";
			Parser.writeTSV(filename, expressionMatrix);
		}

	}
	
	
	// ============================================================================
	// PRIVATE METHODS

	private void writeAnonymizedConditions() {

		// Print the condition list
		String filename = outputDir_ + comp_.name_ + "_experiment_ids.tsv";
		ArrayList<String[]> data = new ArrayList<String[]>();
		
		String[] header = new String[3];
		header[0] = "#ID";
		header[1] = "Batch_ID";
		header[2] = "Batch_condition_ID";
		data.add(header);

		for (int i=0; i<comp_.conditions_.size(); i++) {
			String[] line = new String[3];
			line[0] = Integer.toString(comp_.conditions_.get(i).getGlobalConditionId());
			line[1] = Integer.toString(comp_.conditions_.get(i).getBatchId());
			line[2] = Integer.toString(comp_.conditions_.get(i).getBatchConditionId());
			data.add(line);
		}
		Parser.writeTSV(filename, data);		
	}


	// ----------------------------------------------------------------------------

	private void writeAnonymizedPerturbations() {

		String filename = outputDir_ + comp_.name_ + "_drug_perturbations_ids.tsv";
		ArrayList<String[]> data = new ArrayList<String[]>();
		
		String[] header = new String[3];
		header[0] = "#ID";
		header[1] = "Name";
		header[2] = "Max_level";
		data.add(header);

		Iterator<PerturbationDrug> iter = comp_.drugPerturbationLookup_.values().iterator();
		
		while (iter.hasNext()) {
			PerturbationDrug var = iter.next();
			
			String[] line = new String[3];
			line[0] = "P" + Integer.toString(var.getId());
			line[1] = var.getName();
			line[2] = Double.toString(var.getMaxLevel());
			if (line[2].equals("-1.0"))
				line[2] = "NA";

			data.add(line);
		}
		Parser.writeTSV(filename, data);		
	}

	
	// ----------------------------------------------------------------------------

	/** Write the average expression matrix (same order as experiment definitions!) */
	private void writeExpressionMatrixAvg() {
		
		ArrayList<String[]> expressionMatrix = new ArrayList<String[]>();
		expressionMatrix.add(comp_.grn_.getHeaderArray());
		
		for (int i=0; i<comp_.conditions_.size(); i++)
			expressionMatrix.addAll(
					expressionMatrixToString(comp_.conditions_.get(i).getExperimentsAvg()) );

		String filename = outputDir_ + comp_.name_ + "_expression_data_avg.tsv";
		Parser.writeTSV(filename, expressionMatrix);
		
		if (GnwSettings.getInstance().getOutputGenesInRows()) {
			expressionMatrix = transposeExpressionMatrix(expressionMatrix, assembleExperimentDefsAvg());
			
			filename = outputDir_ + comp_.name_ + "_expression_data_avg_t.tsv";
			Parser.writeTSV(filename, expressionMatrix);
		}
	}

	
	// ----------------------------------------------------------------------------

	/** Write the noise-free expression matrix obtained using ODEs (only for in silico compendia) */
	private void writeExpressionMatrixODE() {
		
		ArrayList<String[]> expressionMatrix = new ArrayList<String[]>();
		expressionMatrix.add(comp_.grn_.getHeaderArray());
		
		for (int i=0; i<comp_.conditions_.size(); i++)
			expressionMatrix.addAll(
					expressionMatrixToString(comp_.conditions_.get(i).getExperimentsODE()) );

		String filename = outputDir_ + comp_.name_ + "_expression_data_nonoise.tsv";
		Parser.writeTSV(filename, expressionMatrix);

		if (GnwSettings.getInstance().getOutputGenesInRows()) {
			expressionMatrix = transposeExpressionMatrix(expressionMatrix, assembleExperimentDefsAvg());
			
			filename = outputDir_ + comp_.name_ + "_expression_data_nonoise_t.tsv";
			Parser.writeTSV(filename, expressionMatrix);
		}
	}

	
	// ----------------------------------------------------------------------------
	
	/** Return string representation of the expression matrix */
	private ArrayList<String[]> transposeExpressionMatrix(ArrayList<String[]> M, ArrayList<String[]> experimentDefs) {
		
		ArrayList<String[]> Mt = new ArrayList<String[]>();
		
		experimentDefs.remove(0); // remove the header
		
		int numExperiments = experimentDefs.size();
		int numGenes = comp_.grn_.getSize();
		
		assert numExperiments == M.size()-1;
		assert numGenes == M.get(0).length;
		
		// Construct the header (experiment descriptions)
		String[] header = new String[numExperiments+1];
		header[0] = "";
		
		for (int e=0; e<numExperiments; e++) {
			String[] def = experimentDefs.get(e);

			header[e+1] = def[0];
			for (int i=1; i<def.length; i++)
				header[e+1] += "_" + def[i];
		}
		Mt.add(header);
		
		// Add the rows, one for each gene
		for (int g=0; g<numGenes; g++) {
			String[] row = new String[numExperiments+1];
			row[0] = M.get(0)[g];
			
			for (int e=0; e<numExperiments; e++)
				row[e+1] = M.get(e+1)[g];
			
			Mt.add(row);
		}
		
		return Mt;
	}

	
	// ----------------------------------------------------------------------------
	
	/** Return string representation of the expression matrix */
	private ArrayList<String[]> expressionMatrixToString(ArrayList<Experiment> experiments) {
		
		ArrayList<String[]> expressionMatrix = new ArrayList<String[]>();
	
		for (int i=0; i<experiments.size(); i++)
			expressionMatrix.addAll(experiments.get(i).expressionMatrixToString());
		
		return expressionMatrix;
	}

	
	// ----------------------------------------------------------------------------

	/** Write the experiment definitions (same order as expression matrix!) */
	private void writeExperimentDefs() {
				
		String filename = outputDir_ + comp_.name_ + "_chip_features.tsv";
		Parser.writeTSV(filename, assembleExperimentDefs());
	}

	
	// ----------------------------------------------------------------------------

	/** Write the experiment definitions (same order as expression matrix!) */
	private ArrayList<String[]> assembleExperimentDefs() {
		
		ArrayList<String[]> experimentDefs = new ArrayList<String[]>();
		experimentDefs.add(getExperimentDefsHeader());
		
		for (int i=0; i<comp_.conditions_.size(); i++)
			experimentDefs.addAll(
					experimentDefsToString(comp_.conditions_.get(i).getExperiments(), comp_.conditions_.get(i).getGlobalConditionId(), comp_.anonymize_) );
		
		return experimentDefs;
	}

	
	// ----------------------------------------------------------------------------

	/** Write the experiment definitions (same order as expression matrix!) */
	private void writeExperimentDefsAvg() {
				
		String filename = outputDir_ + comp_.name_ + "_chip_features_avg.tsv";
		Parser.writeTSV(filename, assembleExperimentDefsAvg());
	}

	
	// ----------------------------------------------------------------------------

	/** Write the experiment definitions (same order as expression matrix!) */
	private ArrayList<String[]> assembleExperimentDefsAvg() {
		
		ArrayList<String[]> experimentDefs = new ArrayList<String[]>();
		experimentDefs.add(getExperimentDefsHeader());
		
		for (int i=0; i<comp_.conditions_.size(); i++) {
			ArrayList<String[]> defs_i = experimentDefsToString(comp_.conditions_.get(i).getExperimentsAvg(), comp_.conditions_.get(i).getGlobalConditionId(), comp_.anonymize_);
			experimentDefs.addAll(defs_i);
			
			// In experimentsAvg_, there is only one repeat per experiment (the average)
			// We want to write the number of repeats in experiments_ instead
			ArrayList<Experiment> exps_i = comp_.conditions_.get(i).getExperiments();
			int R = defs_i.get(0).length - 1;
			
			int counter = 0;
			for (int e=0; e<exps_i.size(); e++) {
				
				if (SteadyStateExperiment.class.isInstance(exps_i.get(e))) {
					defs_i.get(counter++)[R] = Integer.toString(exps_i.get(e).getNumExperiments());
				
				} else {
					TimeSeriesExperiment ts = (TimeSeriesExperiment) exps_i.get(e);
					for (int t=0; t<ts.getNumTimePoints(); t++)
						defs_i.get(counter++)[R] = Integer.toString(ts.getRepeatsPerTimePoint().get(t));
				}
			}
			assert counter == defs_i.size();
		}
		
		return experimentDefs;
	}

	
	// ----------------------------------------------------------------------------

	/** Write the experiment definitions (same order as expression matrix!) */
	private String[] getExperimentDefsHeader() {
		
		// The header line
		String[] header = new String[8];
		header[0] = "#Experiment";
		header[1] = "Perturbations";
		header[2] = "PerturbationLevels";
		header[3] = "Treatment";
		header[4] = "DeletedGenes";
		header[5] = "OverexpressedGenes";
		header[6] = "Time";
		header[7] = "Repeat";
		
		return header;
	}

	
	// ----------------------------------------------------------------------------
	
	/** Get the experiment definitions (same order as expression matrix!) */
	public ArrayList<String[]> experimentDefsToString(ArrayList<Experiment> experiments, int globalConditionId, boolean anonymized) {
		
		ArrayList<String[]> experimentDefs = new ArrayList<String[]>();
		
		for (int i=0; i<experiments.size(); i++) {
			
			ExperimentDefinition def = experiments.get(i).getDefinition();
			
			String[] defStr = new String[8];
			int col = 0;
			//expDef[index++] = author_;
			//expDef[index++] = description_;
			//expDef[index++] = Integer.toString(batchId_);
			//expDef[index++] = Integer.toString(batchCondition_);
			defStr[col++] = Integer.toString(globalConditionId);
			
			String[] vars = def.getPerturbationVariables().clone();
			if (anonymized)
				anonymizePerturbationVariables(vars);
			defStr[col++] = array2String(vars);
			
			defStr[col++] = arrayList2String(def.getPerturbationLevels(), 2);
			defStr[col++] = Integer.toString(def.getTreatment());
			defStr[col++] = array2String(def.getDeletedGenes());
			defStr[col++] = array2String(def.getOverexpressedGenes());
			defStr[col++] = "";
			defStr[col] = "";

			for (int k=0; k<defStr.length; k++)
				if (defStr[k].equals("") || defStr[k].startsWith("-1"))
					defStr[k] = "NA";
			
			ArrayList<String[]> defStrArray = new ArrayList<String[]>();

			// if it's a steady-state experiment, add a definition for every repeat
			if (def.getTimePoints() == null) {
				for (int r=0; r<experiments.get(i).getNumExperiments(); r++) {
					String[] defi = defStr.clone();
					defi[7] = Integer.toString(r+1);
					defStrArray.add(defi);
				}

			// if it's a time series, add a definition for every time point and repeat
			} else {
				ArrayList<Integer> repeatsPerTimePoint = ((TimeSeriesExperiment) experiments.get(i)).getRepeatsPerTimePoint();
				Iterator<Integer> iter = def.getTimePoints().iterator();
				int index = 0;
				
				while (iter.hasNext()) {
					double t = iter.next();
					if (anonymized && comp_.isInvivo_)
						t = t / 30.0;
										
					for (int r=0; r<repeatsPerTimePoint.get(index); r++) {
						String[] defi = defStr.clone();
						defi[6] = String.format("%.2f", t);
						defi[7] = Integer.toString(r+1);
						defStrArray.add(defi);
					}
					index++;
				}
			}
			experimentDefs.addAll(defStrArray);
		}
		
		return experimentDefs;
	}

	
	// ----------------------------------------------------------------------------

	
	/** Return a comma separated list of the doubles in the list */
	private String arrayList2String(ArrayList<Double> array, int precision) {
		if (array.size() == 0)
			throw new RuntimeException("Array has length 0");
		
		String format = "%." + precision + "f";
		String concat = String.format(format, array.get(0));
		for (int i=1; i<array.size(); i++)
			concat = concat + "," + String.format(format, array.get(0));
		
		return concat;
	}

	
	// ----------------------------------------------------------------------------

	/** Return a comma separated list of the strings in the array */
	private String array2String(String[] array) {
		if (array.length == 0)
			throw new RuntimeException("Array has length 0");
		
		String concat = array[0];
		for (int i=1; i<array.length; i++)
			concat = concat + "," + array[i];
		
		return concat;
	}

	
	// ----------------------------------------------------------------------------

	/** Replace the names of the perturbation variables by their ID */
	private void anonymizePerturbationVariables(String[] variableNames) {
		
		//String[] variableIds = new String[variableNames.length];
		if (variableNames == null || variableNames.length == 0)
			throw new RuntimeException("Array of variable names is empty");
		
		if (variableNames[0].equals(""))
			return;
		
		for (int i=0; i<variableNames.length; i++) {
			PerturbationDrug var = comp_.drugPerturbationLookup_.get(variableNames[i]);
			if (var == null)
				throw new RuntimeException("Perturbation variable '" + variableNames[i] + "' not found");
			
			variableNames[i] = "P" + Integer.toString(var.getId()); 
		}
	}


	// ----------------------------------------------------------------------------

	/** Write all perturbations */
	private void writePerturbations()
	{	
		writeDrugPerturbations();
		writeConditionPerturbations();
		writeExperimentPerturbations();
	}

	
	// ----------------------------------------------------------------------------

	/** Write the perturbation vectors associated with the drugs */
	private void writeDrugPerturbations() {
		
		String filename = outputDir_ + comp_.name_ + "_drug_perturbations.tsv";
		ArrayList<String[]> data = new ArrayList<String[]>();

		// The header line
		data.add(comp_.grn_.getHeaderArray());

		Iterator<PerturbationDrug> iter = comp_.drugPerturbationLookup_.values().iterator();
		while (iter.hasNext())
			data.add(iter.next().toStringArray());
		
		Parser.writeTSV(filename, data);		
	}


	// ----------------------------------------------------------------------------

	/** Write the perturbation vectors associated with the drugs */
	private void writeConditionPerturbations()
	{	
		comp_.conditionPerturbations_.printPerturbations(outputDir_, "experiment");	
	}

	
	// ----------------------------------------------------------------------------

	/** Write the final perturbation vectors for each experiment */
	private void writeExperimentPerturbations() {
		
		ArrayList<String[]> deltaBasal = new ArrayList<String[]>();
		ArrayList<String[]> deltaMax = new ArrayList<String[]>();

		// The header line
		deltaBasal.add(comp_.grn_.getHeaderArray());
		deltaMax.add(comp_.grn_.getHeaderArray());

		for (int c=0; c<comp_.conditions_.size(); c++) {
			ArrayList<Experiment> exps = comp_.conditions_.get(c).getExperiments();
			
			for (int e=0; e<exps.size(); e++) {
				PerturbationMixed pert = (PerturbationMixed) exps.get(e).getPerturbation();

				deltaBasal.add( toStringArray(pert.getDeltaBasalActivation()) );
				deltaMax.add( toStringArray(pert.getDeltaMax()) );
			}
		}
		
		String filename = outputDir_ + comp_.name_ + "_chip_perturbations.tsv";
		Parser.writeTSV(filename, deltaBasal);
		
		filename = outputDir_ + comp_.name_ + "_chip_perturbations_genetic.tsv";
		Parser.writeTSV(filename, deltaMax);
	}

	
	// ----------------------------------------------------------------------------
	
	/** Return a string array of the given vector of doubles */
	private String[] toStringArray(DoubleMatrix1D x) {

		String[] str = new String[x.size()];
		
		for (int i=0; i<x.size(); i++)
			str[i] = Double.toString(x.get(i));
		
		return str;
	}


	// ----------------------------------------------------------------------------

	/** Write the list of TFs */
	private void writeTfList() {
		
		ArrayList<String[]> tfList = new ArrayList<String[]>();
		
		// For every node, check whether it's in the TF list or a regulator in the gold standard
		for (int i=0; i<comp_.grn_.getSize(); i++) {
			if (comp_.grn_.getGene(i).getIsTf()) {
				String[] tf = { comp_.grn_.getGene(i).getLabel() };
				tfList.add(tf);
			}
		}
		
		String filename = outputDir_ + comp_.name_ + "_transcription_factors.tsv";
		Parser.writeTSV(filename, tfList);
	}
	
	
	// ----------------------------------------------------------------------------

	/** Write the gold standard */
	private void writeGoldStandard() {
	
		GnwSettings set = GnwSettings.getInstance();
		
		// save DREAM gold standard
		String filename = outputDir_ + comp_.name_ + "_gold_standard.tsv";
		Parser parser = new Parser(comp_.grn_, set.getURL(filename));
		parser.writeGoldStandard();
		
		// save signed network
		if (comp_.grn_.isSigned()) {
			filename = outputDir_ + comp_.name_ + "_gold_standard_signed.tsv";
			try {
				comp_.grn_.saveTSV(set.getURL(filename));
			} catch (Exception e) {
				log_.log(Level.WARNING, "Could not save signed gold standard", e);
			}
		}
		
		// save sbml model
		if (!comp_.isInvivo_) {
			filename = outputDir_ + comp_.name_ + ".xml";
			try {
				comp_.grn_.writeSBML(set.getURL(filename));
			} catch (IOException e) {
				log_.log(Level.WARNING, "Could not save SBML model", e);
			}
		}

	}


}
