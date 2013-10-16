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
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.random.Uniform;
import ch.epfl.lis.networks.Edge;


/**HillGene implements the gene regulation function of a gene.
 * 
 * The model is based on Hill type kinetics and principles of thermodynamics. It's quite tricky, 
 * especially the random initialization in order to achieve biologically plausible gene regulation
 * functions. This will be described in a paper as soon as possible.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class HillGene extends Gene {

	/** Relative activations for all possible states of the regulatory modules **/
	private double[] alpha_;
	/** The wild-type alpha_ vector (used as backup when alpha_ is perturbed */
	private double[] alphaWildType_;
	/** The regulatory modules (they are activated independently from each other) */
	private ArrayList<RegulatoryModule> regulatoryModules_;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private Logger log_ = Logger.getLogger(HillGene.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public HillGene() {
		super();
		alpha_ = null;
		alphaWildType_ = null;
		regulatoryModules_ = null;
	}
	
	/** Constructor, sets the gene network */
	public HillGene(GeneNetwork grn) {
		super(grn);
		alpha_ = null;
		alphaWildType_ = null;
		regulatoryModules_ = null;
	}
	

	// ----------------------------------------------------------------------------

	/**
	 * Return a string representation of the gene regulation function.
	 * The representation is: "no inputs" | (module {+ module}).
	 * 'module' is the string representation of regulatory module.
	 */
	public String toString() {
		 
		String grf = "no inputs";
		
		if (regulatoryModules_.size() > 0) {
			grf = regulatoryModules_.get(0).toString(1);
			int nextInputIndex = regulatoryModules_.get(0).getNumInputs() + 1;
			
			for (int i=1; i<regulatoryModules_.size(); i++) {
				grf += " + " + regulatoryModules_.get(i).toString(nextInputIndex);
				nextInputIndex += regulatoryModules_.get(i).getNumInputs();
			}
		}
		return grf;
	}

		
	// ----------------------------------------------------------------------------

	/**
	 * Compute the production rate of this gene
	 * @param geneIndex Index of this gene in the network that it belongs to
	 * @param c Current expression levels of all gene of the network
	 * @return Production rate of this gene
	 */
	public double computeMRnaProductionRate(int geneIndex, DoubleMatrix1D c) {
		
		int numModules = regulatoryModules_.size();
		// The mean activations of the modules
		double[] m = new double[numModules];
		// Index of the next input
		int nextInput = 0;
		
		// Compute the mean activations
		for (int i=0; i<numModules; i++) {
			RegulatoryModule module = regulatoryModules_.get(i);
			
			// get the inputs for this module
			double[] x = new double[module.getNumInputs()];
			for (int k=0; k<x.length; k++)
				x[k] = c.get( grn_.getIndexOfNode(inputGenes_.get(nextInput++)) );
			
			m[i] = regulatoryModules_.get(i).computeActivation(x);
		}
	
		// The relative activation of the gene
		double alpha = 0;
		double sum = 0;
		
		for (int i=0; i<alpha_.length; i++) {
			// binary representation of state i
			String s = Integer.toBinaryString(i); // note, leading 0's are not included
			double p = 1; // the probability of being in state i
			
			// if module j is active in this state, multiply with m_j, otherwise with (1-m_j)
			for (int j=0; j<numModules; j++) {
				// if s.length()-j-1 smaller than zero, the corresponding module is off (it's one of the leading zeros)
				if (s.length()-j-1 >= 0 && s.charAt(s.length()-j-1) == '1')	
					p *= m[j];
				else
					p *= 1 - m[j];
			}
			assert p >= 0 && p <= 1 : p;
			assert (sum += p) >= 0; // always true, just to compute the sum
			
			alpha += alpha_[i] * p;
		}
		
		assert sum < 1+1e-6 && sum > 1-1e-6 : sum;
		
		return max_ * alpha;
	}

	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Compute the mRNA degradation rate of this gene (>= 0)
	 * @param geneIndex Index of this gene in the network that it belongs to
	 * @param c Current expression levels of all gene of the network
	 */
	public double computeMRnaDegradationRate(double x) {
		
		return delta_*x;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Compute the protein production rate of this gene
	 */
	public double computeProteinProductionRate(double x) {
		
		return maxTranslation_*x;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Compute the protein degradation rate of this gene (>= 0)
	 */
	public double computeProteinDegradationRate(double y) {
		
		return deltaProtein_*y;
	}
	

	// ----------------------------------------------------------------------------

	/**
	 * Set the type (enhancing, inhibiting, dual, or unknown) of all input edges
	 * of this gene according to the dynamically model.
	 */
	public void setInputEdgeTypesAccordingToDynamicalModel() {
		
		// inputSigns is a vector of boolean indicating for each input the sign of its edge
		// (whether it has an enhancing or inhibitory effect on this gene)
		ArrayList<Boolean> inputSigns = new ArrayList<Boolean>();
		for (int i=0; i<regulatoryModules_.size(); i++)
			inputSigns.addAll(regulatoryModules_.get(i).getEdgeSigns());
		
		// a sign must be specified for each input
		int numInputs = inputSigns.size();
		assert numInputs == inputGenes_.size();
		
		// set the edges
		for (int i=0; i<numInputs; i++) {
			if (inputSigns.get(i))
				grn_.getEdge(inputGenes_.get(i), this).setType(Edge.ENHANCER);
			else
				grn_.getEdge(inputGenes_.get(i), this).setType(Edge.INHIBITOR);
		}
	}

	
	// ----------------------------------------------------------------------------

	/** Perturb the basal activation with this value */
	public void perturbBasalActivation(double deltaBasalActivation) {
		
		// first, backup the wild-type
		alphaWildType_ = new double[alpha_.length];
		for (int i=0; i<alpha_.length; i++)
			alphaWildType_[i] = alpha_[i];
		
		// Note: what we want to perturb is the basal transcription rate alpha_[0].
		// Remember that alpha_i = alpha_0 + something. Thus, if we perturb alpha_0,
		// it appears in all terms of the vector alpha_
		
		// first, adapt deltaBasalActivation so that alpha_0 is in [0 1]
		if (alpha_[0] + deltaBasalActivation > 1)
			deltaBasalActivation = 1 - alpha_[0];
		else if (alpha_[0] + deltaBasalActivation < 0)
			deltaBasalActivation = 0 - alpha_[0];		
		
		for (int i=0; i<alpha_.length; i++) {
			alpha_[i] += deltaBasalActivation;
			// truncate to [0 1]
			if (alpha_[i] < 0)
				alpha_[i] = 0;
			else if (alpha_[i] > 1)
				alpha_[i] = 1;
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/** Restore the wild-type basal activation */
	public void restoreWildTypeBasalActivation() {
		
		for (int i=0; i<alpha_.length; i++)
			alpha_[i] = alphaWildType_[i];
	}
	
	
	// ----------------------------------------------------------------------------

	public ArrayList<RegulatoryModule> getRegulatoryModules() {return regulatoryModules_;}
	public double getBasalActivation() { return alpha_[0]; }
	
	
	// ============================================================================
	// PRIVATE METHODS

	/** Random initialization of the HillGene (inputIndexes need to be set already) */
	protected void subclassRandomInitialization() {
		
		// initialize the number of inputs per module and the modules
		randomInitializationOfStructureAndModules();
		// initialize the gene activations
		randomInitializationOfAlpha();
	}
	
	
	// ----------------------------------------------------------------------------

	/** Random initialization the number of inputs per module and the modules */
	@SuppressWarnings("unchecked")
	private void randomInitializationOfStructureAndModules() {
	
		GnwSettings uni = GnwSettings.getInstance();
		Uniform uniform = uni.getUniformDistribution();
		int numInputsOfGene = inputGenes_.size();
		
		// RANDOM INITIALIZATION OF NUMBER OF MODULES AND NUMBER OF INPUTS PER MODULE

		ArrayList<Integer> numInputsPerModule = new ArrayList<Integer>(inputGenes_.size());
		
		// distribute inputs among modules
		for (int i=0; i<inputGenes_.size(); i++) {
			// randomly choose a module
			int k = uniform.nextIntFromTo(0, numInputsPerModule.size());

			// if k = numModules, create a new module 
			if (k == numInputsPerModule.size())
				numInputsPerModule.add(1);
			else // else add an input to the chosen module
				numInputsPerModule.set(k, numInputsPerModule.get(k)+1);
		}
		
		// RANDOM INITIALIZATION OF THE MODULES

		// The inputGenes_ are ordered. Since the first m inputs go to the
		// first module, and the second n inputs to the second module, etc.,
		// we need to randomize the order of the inputs. We put the list in a
		// clone and will add one gene after the other back to the original list.
		ArrayList<Gene> inputGenesClone = (ArrayList<Gene>) inputGenes_.clone();
		inputGenes_.clear();
		
		regulatoryModules_ = new ArrayList<RegulatoryModule>(numInputsPerModule.size());
		
		for (int m=0; m<numInputsPerModule.size(); m++) {
			
			int numModuleInputs = numInputsPerModule.get(m);
			// the input genes of this module
			ArrayList<Gene> inputsOfThisModule = new ArrayList<Gene>(numModuleInputs);
			// the type of the inputs (enhancing, inhibitory, etc.)
			ArrayList<Byte> edgeTypes = new ArrayList<Byte>(numModuleInputs);
			
			// randomly choose the inputs
			for (int i=0; i<numModuleInputs; i++) {
				int k = uniform.nextIntFromTo(0, inputGenesClone.size()-1); // inputGenesClone shrinks as we remove elements				
				inputsOfThisModule.add(inputGenesClone.get(k));
				inputGenesClone.remove(k);
			}

			// count the input edge types of this module
			int numActivators = 0;
			int numDeactivators = 0;
			int numUnknown = 0;
			for (int i=0; i<numModuleInputs; i++) {
				Byte type = grn_.getEdge(inputsOfThisModule.get(i), this).getType();
				// for now, we don't model dual regulation and set the sign to unknown
				if (type == Edge.DUAL)
					type = Edge.UNKNOWN;
				
				if (type == Edge.ENHANCER)
					numActivators++;
				else if (type == Edge.INHIBITOR)
					numDeactivators++;
				else if (type == Edge.DUAL || type == Edge.UNKNOWN)
					numUnknown++;
				else
					throw new RuntimeException("Unknown edge type");			
				
				edgeTypes.add(type);
			}
			assert numActivators + numDeactivators + numUnknown == numModuleInputs;
			
			// CREATE AND INITIALIZE THE MODULE
			
			RegulatoryModule module = new RegulatoryModule();
			regulatoryModules_.add(module);
			
			// If there are more deactivators than activators, we say the module is a repressor,
			// and the deactivators now are activators of this repressor. The point is, we want
			// in general more activators than deactivators of a module.
			boolean isEnhancer;
			if (numActivators > numDeactivators)
				isEnhancer = true;
			else if (numDeactivators > numActivators)
				isEnhancer = false;
			else {
				assert numActivators == numDeactivators;
				// chance of being enhancer / repressor is 0.5
				if (uniform.nextIntFromTo(0, 1) == 0)
					isEnhancer = true;
				else
					isEnhancer = false;
			}
			module.setIsEnhancer(isEnhancer);
			
			// chance of the module being a complex is 0.5
			if (uniform.nextIntFromTo(0, 1) == 0)
				module.setBindsAsComplex(true);
			else
				module.setBindsAsComplex(false);

			// if the module is a repressor, enhancing edges are deactivators and inhibitory edges activators
			Byte activatorType = Edge.ENHANCER;
			Byte deactivatorType = Edge.INHIBITOR;
			
			if (!isEnhancer) {
				activatorType = Edge.INHIBITOR;
				deactivatorType = Edge.ENHANCER;
				
				int tmp = numActivators;
				numActivators = numDeactivators;
				numDeactivators = tmp;
			}

			// determine the number of activators and deactivators
			
			// for unsigned structures (notably in the DREAM3 challenges), I set
			// at least half of the inputs to be activators
			if (!grn_.isSigned()) {
				assert numUnknown == numModuleInputs;
				numActivators = numModuleInputs - (numModuleInputs / 2); // note, 1/2 = 0, 3/2 = 1, ...				
				for (int i=0; i<numActivators; i++)
					edgeTypes.set(i, activatorType);	
			
			// else if the structure is signed, but all inputs are of unknown type, set at least
			// one activator
			} else if (numActivators == 0) {
				assert numDeactivators == 0;
				numActivators = 1;
				edgeTypes.set(0, activatorType);
			}
			
			// the resting unknown inputs are distributed randomly in the two sets
			for (int i=0; i<numModuleInputs; i++) {
				if (edgeTypes.get(i) == Edge.UNKNOWN) {
					if (uniform.nextIntFromTo(0, 1) == 0) {
						numActivators++;
						edgeTypes.set(i, activatorType);
					} else {
						numDeactivators++;
						edgeTypes.set(i, deactivatorType);
					}
				}
			}
			module.setNumActivators(numActivators);
			module.setNumDeactivators(numDeactivators);
			
			assert numActivators + numDeactivators == numModuleInputs;
			assert numActivators > 0;
			// rarely, there may be more deactivators than activators in signed networks because
			// of the unknown interactions that get initialized randomly
			//assert numActivators >= numDeactivators;

			// set the inputs *ordered* (first activators, then deactivators) 
			for (int i=0; i<numModuleInputs; i++)
				if (edgeTypes.get(i) == activatorType)
					inputGenes_.add(inputsOfThisModule.get(i));
			
			for (int i=0; i<numModuleInputs; i++)
				if (edgeTypes.get(i) == deactivatorType)
					inputGenes_.add(inputsOfThisModule.get(i));
			
			// finally, initialize the numerical parameters of the module
			module.randomInitializationOfParameters();
		}
		assert inputGenes_.size() == numInputsOfGene;
	}
	
	
	// ----------------------------------------------------------------------------

	/** Random initialization the alpha parameters for all possible states of the modules */
	private void randomInitializationOfAlpha() {

		GnwSettings uni = GnwSettings.getInstance();
		double weakActivation = uni.getWeakActivation();
		
		int numModules = regulatoryModules_.size();		
		int numStates = (int)Math.round(Math.pow(2, numModules));
		double[] dalpha = new double[numModules]; // the effect on alpha_0 of each module individually
		alpha_ = new double[numStates];
		
		// set the difference in gene activation due to any module alone
		for (int i=0; i<numModules; i++) {
			dalpha[i] = uni.getRandomDeltaActivation();
			if (!regulatoryModules_.get(i).isEnhancer())
				dalpha[i] *= -1;
		}
		
		// Compute the max positive and negative difference in gene activation, i.e.,
		// when all enhancers / repressors are active
		double maxDeltaPositive = 0;
		double maxDeltaNegative = 0;
		
		for (int i=0; i<numModules; i++) {
			if (dalpha[i] > 0)
				maxDeltaPositive += dalpha[i];
			else
				maxDeltaNegative += dalpha[i];
		}
		
		// Set alpha_0, the basal transcription rate
		if (numModules == 0) 			// Case 1: No inputs
			alpha_[0] = 1;
		else if (maxDeltaPositive == 0) // Case 2: There are only repressors
			alpha_[0] = 1;
		else if (maxDeltaNegative == 0) // Case 3: There are only enhancers
			alpha_[0] = uni.getRandomLowBasalRate();
		else 							// Case 4: There are enhancers and repressors
			alpha_[0] = uni.getRandomMediumBasalRate();
		
		// make sure that the activation goes at least up to 1 in the maximally activated state
		// (if there is at least one activator)
		if (maxDeltaPositive > 0 && alpha_[0] + maxDeltaPositive < 1) {
			// find the smallest positive dalpha
			double minPos = 1;
			int indexMinPos = -1;
			
			for (int i=0; i<numModules; i++) {
				if (dalpha[i] > 0 && dalpha[i] < minPos) {
					minPos = dalpha[i];
					indexMinPos = i;
				}
			}
			// increase the smallest dalpha so that: alpha_0 + maxDeltaPositive = 1
			dalpha[indexMinPos] += 1 - alpha_[0] - maxDeltaPositive;
		}
		
		// make sure that the activation falls within [0 weakActivation] in the maximally repressed state
		// (if there is a at least a repressor)
		if (maxDeltaNegative < 0 && alpha_[0] + maxDeltaNegative > weakActivation) {
			// find the weakest negative dalpha
			double minPos = -1;
			int indexMinPos = -1;
			
			for (int i=0; i<numModules; i++) {
				if (dalpha[i] < 0 && dalpha[i] > minPos) {
					minPos = dalpha[i];
					indexMinPos = i;
				}
			}
			// increase the weakest dalpha so that: (alpha_[0] + maxDeltaNegative) in [0 weakActivation]
			dalpha[indexMinPos] += - alpha_[0] - maxDeltaNegative + uni.getRandomLowBasalRate();
				//weakActivation - alpha_[0] - maxDeltaNegative - uni.getRandomLowBasalRate();
		}
		
		// Set the alpha for all possible states
		// State s is interpreted as a binary number, bit k indicates whether module k
		// is active (1) or inactive (0) in this state. State 0 (alpha_0) has already
		// been set
		for (int i=1; i<numStates; i++) {
			alpha_[i] = alpha_[0];
			String s = Integer.toBinaryString(i); // note, leading 0's are not included
			
			// if module j is active in this state, add its effect
			for (int j=0; j<numModules; j++) {
				// if s.length()-j-1 smaller than zero, the corresponding module is off (it's one of the leading zeros)
				if (s.length()-j-1 >= 0 && s.charAt(s.length()-j-1) == '1')	
					alpha_[i] +=  dalpha[j];
			}
			
			// truncate to [0 1]
			if (alpha_[i] < 0)
				alpha_[i] = 0;
			else if (alpha_[i] > 1)
				alpha_[i] = 1;
		}
	}

	
	// ----------------------------------------------------------------------------

	/** Return the names and the values all parameters (*not* including those of the superclass) */
	protected void subclassCompileParameters(ArrayList<String> names, ArrayList<Double> values) {
		
		// the structure (number of activators and deactivators per module)
		for (int i=0; i<regulatoryModules_.size(); i++) {
			names.add("bindsAsComplex_" + (i+1));
			names.add("numActivators_" + (i+1));
			names.add("numDeactivators_" + (i+1));
			
			double bindsAsComplex = 0; // false
			if (regulatoryModules_.get(i).bindsAsComplex())
				bindsAsComplex = 1;
			values.add(bindsAsComplex);
			values.add((double)regulatoryModules_.get(i).getNumActivators());
			values.add((double)regulatoryModules_.get(i).getNumDeactivators());
		}
		
		// alpha_
		for (int i=0; i<alpha_.length; i++) {
			names.add("a_" + i);
			values.add(alpha_[i]);
		}
		
		// k_
		int counter = 1;
		for (int i=0; i<regulatoryModules_.size(); i++) {
			double[] k = regulatoryModules_.get(i).getK();
			for (int j=0; j<k.length; j++) {
				names.add("k_" + counter++);
				values.add(k[j]);
			}
		}
		
		// n_
		counter = 1;
		for (int i=0; i<regulatoryModules_.size(); i++) {
			double[] n = regulatoryModules_.get(i).getN();
			for (int j=0; j<n.length; j++) {
				names.add("n_" + counter++);
				values.add(n[j]);
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/** 
	 * Initialize the gene regulation function from the given parameters 
	 */	
	protected void subclassInitialization(ArrayList<String> paramNames, ArrayList<Double> paramValues) {
		
		// create the modules
		regulatoryModules_ = new ArrayList<RegulatoryModule>();
		int numModules = 0;
		int index = paramNames.indexOf("bindsAsComplex_1");
		int inputCounter = 1;
		
		while (index != -1) {
			RegulatoryModule module = new RegulatoryModule();  
			regulatoryModules_.add(module);
			numModules++;
			
			// bindsAsComplex
			if (paramValues.get(index) == 1) // if bindsAsComplex == 1
				module.setBindsAsComplex(true); // the constructor initialized this with 'false'
			
			// numActivators and numDeactivators
			double numActivators = paramValues.get( paramNames.indexOf("numActivators_" + (numModules)) );
			double numDeactivators = paramValues.get( paramNames.indexOf("numDeactivators_" + (numModules)) );
			module.setNumActivators((int)numActivators);
			module.setNumDeactivators((int)numDeactivators);
			
			// k_ and n_
			int numInputs = (int) (numActivators + numDeactivators);
			double[] k = new double[numInputs];
			double[] n = new double[numInputs];
			for (int i=0; i<numInputs; i++) {
				k[i] = paramValues.get( paramNames.indexOf("k_" + inputCounter) );
				n[i] = paramValues.get( paramNames.indexOf("n_" + inputCounter) );
				inputCounter++;
			}
			module.setK(k);
			module.setN(n);
			
			// check whether there is another module
			index = paramNames.indexOf("bindsAsComplex_" + (numModules+1));
		}

		int numStates = (int)Math.round(Math.pow(2, numModules));
		alpha_ = new double[numStates];

		// Set the alpha for all possible states
		// State s is interpreted as a binary number, bit k indicates whether module k
		// is active (1) or inactive (0) in this state.
		for (int i=0; i<numStates; i++)
			alpha_[i] = paramValues.get( paramNames.indexOf("a_" + i) );
		
		// Find out which modules are repressors (default value of isEnhancer_ is 'true')
		// The state where only module k is active is state number 2^(k) (k starting at 0)
		for (int i=0; i<numModules; i++) {
			int s = (int)Math.round(Math.pow(2, i));
			if (alpha_[s] < alpha_[0])
				regulatoryModules_.get(i).setIsEnhancer(false);
		}		
	}

}
