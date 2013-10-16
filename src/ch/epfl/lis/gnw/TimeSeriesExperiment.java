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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;


/** Time course experiments, see documentation for details.
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Daniel Marbach (firstname.name@gmail.com)
 *
 */
public class TimeSeriesExperiment extends Experiment {

	/** Time series data */
	private ArrayList<DoubleMatrix2D> timeSeries_ = null;
	/** Protein data */
	private ArrayList<DoubleMatrix2D> timeSeriesProteins_ = null;
	/** Set true to remove the perturbation after maxt/2 */
	private boolean restoreWildTypeAtHalftime_ = false;
	/** An explicit list of time points (tree set is used for a sorted list) */
	private TreeSet<Integer> timePoints_ = new TreeSet<Integer>();
	/** Number of repeats for every time point */
	private ArrayList<Integer> repeatsPerTimePoint_ = null;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public TimeSeriesExperiment() {
		
		super();
	}

	
	/** Constructor with initialization */
	public TimeSeriesExperiment(Solver.type solverType, Perturbation perturbation, boolean restoreWildTypeAtHalftime, String label) {
		
		super(solverType, perturbation, label);
		restoreWildTypeAtHalftime_ = restoreWildTypeAtHalftime;
	}
	

	/** Copy constructor */
	public TimeSeriesExperiment(TimeSeriesExperiment exp) {
		
		super(exp);
		restoreWildTypeAtHalftime_ = exp.getRestoreWildTypeAtHalftime();
		timePoints_ = exp.getTimePoints();
		repeatsPerTimePoint_ = exp.getRepeatsPerTimePoint();
	}
	

	// ----------------------------------------------------------------------------

	/** Clone this experiment (shallow copy of everything except data and parameters from GnwSettings) */
	public Experiment clone() {
		
		return new TimeSeriesExperiment(this);
	}

	
	// ----------------------------------------------------------------------------
	
	/** Return a clone of this experiment with the average of the repeats */
	public Experiment computeAverageOfRepeats() {
		
		TimeSeriesExperiment expAvg = (TimeSeriesExperiment) clone();
		expAvg.setNumExperiments(1);
		
		ArrayList<DoubleMatrix2D> ts = new ArrayList<DoubleMatrix2D>();
		DoubleMatrix2D avg = new DenseDoubleMatrix2D(timePoints_.size(), numGenes_);
		ts.add(avg);
		expAvg.setTimeSeries(ts);
		
		for (int t=0; t<timePoints_.size(); t++) {
			for (int g=0; g<numGenes_; g++) {
				int numRepeats = repeatsPerTimePoint_.get(t);

				double x = 0;
				for (int r=0; r<numRepeats; r++)
					x += timeSeries_.get(r).get(t, g);
				x = x / numRepeats;

				avg.set(t, g, x);
			}
		}
		return expAvg;
	}

	
	// ----------------------------------------------------------------------------

	/**
	 * Run all experiments
	 */
	public void run(DoubleMatrix1D xy0) {
		
		xy0_ = xy0;
		
		String simulationType = "ODEs";
		if (solverType_ == Solver.type.SDE)
			simulationType = "SDEs";
		log_.log(Level.INFO, "Simulating time-series \"" + label_ + "\" using " + simulationType + " ...");

		boolean simulateLoadedExperiments = (timeSeries_ != null);
		if (simulateLoadedExperiments)
			throw new RuntimeException("NEEDS TO BE FIXED, NOT FUNCTIONAL");
		
		if (!simulateLoadedExperiments) {
			timeSeries_ = new ArrayList<DoubleMatrix2D>();
			if (modelTranslation_)
				timeSeriesProteins_ = new ArrayList<DoubleMatrix2D>();
		}
		
		// create and run the time series experiments
		for (int i=0; i<numExperiments_; i++) {
			log_.log(Level.INFO, "Simulating time-series number " + (i+1) + " ...");
			integrate(i);
		}
		log_.log(Level.INFO, "");
		
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Return the expression data as an array list of strings.
	 * Must be same order as experiment definitions in Condition.experimentDefsToString()
	 */
	public ArrayList<String[]> expressionMatrixToString() {
		
		ArrayList<String[]> expressionMatrix = new ArrayList<String[]>();
		/*
		if (GnwSettings.getInstance().getOutputGenesInRows()) {
			// For every gene
			for (int g=0; g<numGenes_; g++) {
				ArrayList<String> row = new ArrayList<String>();
				
				// For every time point
				for (int t=0; t<timePoints_.size(); t++) {
					// For every repeat
					for (int r=0; r<repeatsPerTimePoint_.get(t); r++) {
						row.add( String.format("%.7f", timeSeries_.get(r).get(t, g)) );
					}
				}
				expressionMatrix.add((String[]) row.toArray());
			}
			
			
		} else {*/
			// For every time point
			for (int t=0; t<timePoints_.size(); t++) {
				// For every repeat
				for (int r=0; r<repeatsPerTimePoint_.get(t); r++) {
					String[] row = new String[numGenes_];
				
					for (int g=0; g<numGenes_; g++)
						row[g] = String.format("%.7f", timeSeries_.get(r).get(t, g)); //Double.toString(timeSeries_.get(r).get(t, g));
				
					expressionMatrix.add(row);
				}
			}
		
		return expressionMatrix;
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the initial conditions xy0 (the vector is copied)
	 */
	public void setInitialConditions(DoubleMatrix1D xy0) {
		
		int length = numGenes_;
		if (modelTranslation_)
			length *= 2;
		
		if (xy0.size() != length)
			throw new IllegalArgumentException("TimeSeriesExperiment:setInitialConditions(): " +
					"xy0.length = " + xy0.size() + " doesn't match the number of state variables " + length);
		
		xy0_ = new DenseDoubleMatrix1D(length);
		xy0_.assign(xy0);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Add experimental noise to the data
	 */
	public void addNoise() {

		for (int i=0; i<timeSeries_.size(); i++)
			addNoise(timeSeries_.get(i));
		
		if (modelTranslation_)
			for (int i=0; i<timeSeriesProteins_.size(); i++)
				addNoise(timeSeriesProteins_.get(i));
		
		noiseHasBeenAdded_ = true;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Get the maximum concentration in the time series.
	 * For now, only get the max concentration between mRNA levels. Later, perhaps
	 * leave the choice to the user if he prefer, e.g., to normalise by the mRNA
	 * OR protein levels.
	 */
	public double getMaximumConcentration() {

		double max = 0;
		
		for (int i=0; i<timeSeries_.size(); i++) {
			double max_i = getMaximumConcentration(timeSeries_.get(i));
			if (max_i > max)
				max = max_i;
		}
		return max;
	}

	
	// ----------------------------------------------------------------------------

	/** Normalize (i.e. divide by) the given maximum value */
	public void normalize(double max) {
		
		for (int i=0; i<timeSeries_.size(); i++)
			normalize(timeSeries_.get(i), max);
			
		if (modelTranslation_)
			for (int i=0; i<timeSeriesProteins_.size(); i++)
				normalize(timeSeriesProteins_.get(i), max);
	}
	
	
	// ----------------------------------------------------------------------------

	/** 
	 * Print all the trajectories to a single file, the initial conditions are printed
	 * to a separate file. Protein trajectories are only printed if translation is
	 * modelled. Append the given string to the filenames (e.g. "-nonoise"). 
	 */
	public void printAll(String directory, String postfix) {
		
		if (timeSeries_.size() < 1)
			return;
		
		printTrajectories(directory, postfix + "_" + label_, timeSeries_);    // print mRNA time courses
		if (modelTranslation_)
			printTrajectories(directory, postfix + "_proteins_" + label_, timeSeriesProteins_); // print protein time courses
	}

	
	// ----------------------------------------------------------------------------
	
	/** Set maxt_ and numTimePoints_ according to GnwSettings (checks that they are consistent with GnwSettings.dt_) */
	public void initializeEquallySpacedTimePoints() {
		
		int dt = GnwSettings.getInstance().getDt();
		int maxt = GnwSettings.getInstance().getMaxtTimeSeries();
		int numTimePoints = (int)Math.round(maxt/(double)dt) + 1;

		if (dt*(numTimePoints-1) != maxt)
			throw new RuntimeException("maxt must be a multiple of dt");
		
		timePoints_ = new TreeSet<Integer>();
		
		for (int i=0; i<numTimePoints; i++)
			timePoints_.add(i*dt);
	}
	
	

	// ============================================================================
	// PRIVATE FUNCTIONS
		
	/**
	 * Run the numerical integration of the k'th time-series and add the results to timeSeries_ and timeSeriesProteins_.
	 * The wild-type is restored after the experiments.
	 */
	private void integrate(int k) {

		//if (GnwSettings.getInstance().getDt()*(numTimePoints_-1) != maxt_)
			//throw new RuntimeException("dt * (numTimePoints-1) != maxt");
		
		if (xy0_ == null)
			throw new RuntimeException("No initial condition set");
		if (timePoints_ == null || timePoints_.size() == 0)
			throw new RuntimeException("No time points set");
		//if (timePoints_.last() <= 0)
			//throw new IllegalArgumentException("Duration (maxt) must be greater than 0!");
		
		// allocate space
		DoubleMatrix2D ts = new DenseDoubleMatrix2D(timePoints_.size(), numGenes_);
		DoubleMatrix2D tsProteins = null;
		if (modelTranslation_)
			tsProteins = new DenseDoubleMatrix2D(timePoints_.size(), numGenes_);

		Solver solver = new Solver(solverType_, grn_, xy0_.toArray());
		double t = 0;
		
		// for SDEs, simulate the wild-type for a short time to get a new independent sample
		if (solverType_ == Solver.type.SDE) {
			double tlim = GnwSettings.getInstance().getMintSDE(); //timePoints_.last()/10.0;
			do {
				try {
					t += solver.step();
				} catch (Exception e) {
					log_.log(Level.WARNING, "TimeSeriesExperiment.integrate(): Exception in phase 0, t = " + t + ":" + e.getMessage(), e);
					throw new RuntimeException();
				}
			} while (t < tlim);

			// set this sample as the new initial condition
			// This is dangerous, because the user doesn't expect run(xy0) to modify xy0. mintSDE should be set long
			// enough to get an independent sample and we don't need this here (otherwise, clone xy0)
			//xy0_.assign(solver.getState()); 
		}
		
		// Set first line of the time series dataset (at t=0)
		int pt = 0;
		if (timePoints_.first() == 0) {
			for (int i=0; i<numGenes_; i++)
				ts.set(pt, i, xy0_.get(i));
			if (modelTranslation_)
				for (int i=0; i<numGenes_; i++)
					tsProteins.set(pt, i, xy0_.get(numGenes_+i));
			pt++;
		}
		
		// apply perturbation
		perturbation_.applyPerturbation(k);
		t = 0; // reset time, the time-series only really starts here
		int dt = GnwSettings.getInstance().getDt(); // step size of the solvers (not integration step size)
		
		//if (dt <= 0 || dt > timePoints_.last())
			//throw new IllegalArgumentException("Interval between two measuread points must be >0 and <maxt.");
		
		// replace that by being sure that dt is a multiple of the internal integration step-size
		// as dt is calculated from maxt_ and numTimePoints there must be no problem (see reported bug)
		//double frac = timePoints_.last()/dt;
		//if (frac - (int)frac != 0)
			//throw new IllegalArgumentException("Duration (maxt) must be a multiple of numTimePoints-1.");
		
		double tlim = timePoints_.last()/2.0 - 1e-12;
		boolean wildTypeRestored = false;
		
		Iterator<Integer> iter = timePoints_.iterator();
		// If the first point is 0, we have taken care of that above and skip it
		if (pt == 1)
			iter.next();
			
		//do {
		while (iter.hasNext()) {
			
			double t1 = t;
			double t2 = iter.next();

			do {
				try {
					// For ODEs: this steps the time by dt_, but using an adaptive internal step size
					// to guarantee the specified tolerance (getRate() may be called several times for one step)
					// For SDEs: this steps the time by dt_, the solver integrates with a smaller, fixed step size
					// defined in SDESettings by dt_*multiplier_ (SDESettings.dt_ != TimeSeriesExperiment.dt_)
					t += solver.step();
					
					if (t == t1 + dt)
						t1 = t;
					else
						throw new RuntimeException("Solver failed to step time by dt, expected t = " + (t1+dt) + ", obtained t = " + t);

				} catch (Exception e) {
					log_.log(Level.WARNING, "TimeSeriesExperiment.integrate(): Exception at t = " + t + ":" + e.getMessage(), e);
					throw new RuntimeException();
				}
						
			} while (t < t2 - 1e-12);
				
			if (t > t2 + 1e-12)
				throw new RuntimeException("Time points must be a multiple of the step size. Attempting to step to time point " + t2 + " failed: t = " + t);
			
			if (restoreWildTypeAtHalftime_ && t >= tlim && !wildTypeRestored) {
				perturbation_.restoreWildType();
				wildTypeRestored = true;
			}
			
			// Save the state of the result
			double[] xy = solver.getState();
			for (int g=0; g<numGenes_; g++)
				ts.set(pt, g, xy[g]);

			if (modelTranslation_)
				for (int g=0; g<numGenes_; g++)
					tsProteins.set(pt, g, xy[numGenes_+g]);
			
			pt++;
		} //while (t < timePoints_.last());

		assert t == timePoints_.last() : "t=" + t + " maxt=" + timePoints_.last();
		assert pt == timePoints_.size();
		
		// make sure the wild-type is restored
		if (!wildTypeRestored)
			perturbation_.restoreWildType();
		
		// add the new time-series data to the array lists
		timeSeries_.add(ts);
		if (modelTranslation_)
			timeSeriesProteins_.add(tsProteins);
		
		//getSDESolver()
		//if (solverType_ == Solver.type.SDE && solver.getXNegativeCounter() > 0)
			//log_.log(Level.INFO, "SDE: " + solver.getXNegativeCounter() + " times a concentration became negative due to noise and was set to 0");

	}

	
	// ----------------------------------------------------------------------------

	/** 
	 * Print all the trajectories to a single file, the initial conditions are printed
	 * to a separate file. If the argument is set true, the protein instead of the
	 * mRNA concentrations are printed. append the given string to the filenames (e.g. "-nonoise"). 
	 */
	private void printTrajectories(String directory, String postfix, ArrayList<DoubleMatrix2D> timeSeries) {
				
		try { 
			// Filename
			String filename = directory + grn_.getId() + postfix + ".tsv";
			FileWriter fw = new FileWriter(filename, false);
			
			// Header
			fw.write("\"Time\"\t");
			fw.write(grn_.getHeader(false));

			// For every time series...
			for (int i=0; i<timeSeries.size(); i++) {

				// The data
				DoubleMatrix2D data = timeSeries.get(i);
				//double dt = GnwSettings.getInstance().getDt();

				fw.write("\n");
				
				Iterator<Integer> iter = timePoints_.iterator();
				int tp = 0;
				
				//for (int tp=0; tp<timePoints_.size(); tp++) {
				while (iter.hasNext()) {
					//fw.write(Double.toString(tp*dt));
					fw.write(iter.next().toString());

					for (int g=0; g<numGenes_; g++)
						fw.write("\t" + String.format("%.7f", data.get(tp, g))); //Double.toString(data.get(tp, g)));
					fw.write("\n");
					tp++;
				}
			}

			fw.close();
			log_.log(Level.INFO, "Writing file " + filename);

		} catch (IOException fe) {
			log_.log(Level.WARNING, "TimeSeriesExperiment:printDataset(): " + fe.getMessage(), fe);
			throw new RuntimeException();
		}
	}

	
	// ----------------------------------------------------------------------------

	/** Add experimental noise to the given data */
	private void addNoise(DoubleMatrix2D ts) {
		
		for (int i=0; i<timePoints_.size(); i++)
			for (int j=0; j<numGenes_; j++)
				ts.set(i, j, addNoise(ts.get(i,j)));
	}
	
	
	// ----------------------------------------------------------------------------

	/** Get the maximum concentration in the given time series. */
	public double getMaximumConcentration(DoubleMatrix2D ts) {

		double max = 0;
		
		for (int i=0; i<timePoints_.size(); i++)
			for (int j=0; j<numGenes_; j++)
				if (ts.get(i,j) > max)
					max = ts.get(i,j);
		
		return max;
	}
	
	
	// ----------------------------------------------------------------------------

	/** Normalize (i.e. divide by) the given maximum value */
	public void normalize(DoubleMatrix2D ts, double max) {
		
		for (int i=0; i<timePoints_.size(); i++)
			for (int j=0; j<numGenes_; j++)
				ts.set(i, j, ts.get(i,j)/max);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** Add a time point, gives an error if this time point is already present */
	public void addTimePoint(int t) {
		
		if (timePoints_.contains(t))
			throw new RuntimeException("Time point " + t + " was already specified");
		else
			timePoints_.add(t);
	}
	
	
	// ----------------------------------------------------------------------------

	/** Add the given data to this experiment (used when loading the data from a compendium) */
	public void addData(DoubleMatrix1D data, int repeat, int timePoint) {
		
		int index = getIndexOfTimePoint(timePoint);
		if (repeat > repeatsPerTimePoint_.get(index))
			throw new RuntimeException("Trying to add repeat " + repeat + " but this time point has only " + repeatsPerTimePoint_.get(index));
		if (data.size() != numGenes_)
			throw new RuntimeException("Trying to add data for " + data.size() + " genes, expected " + numGenes_ + " genes");
		
		// Create data matrix and initialize
		if (timeSeries_ == null) {
			timeSeries_ = new ArrayList<DoubleMatrix2D>();
			for (int i=0; i<numExperiments_; i++) {
				DoubleMatrix2D tsi = new DenseDoubleMatrix2D(timePoints_.size(), numGenes_);
				tsi.assign(-1);
				timeSeries_.add(tsi);
			}
		}

		DoubleMatrix2D ts = timeSeries_.get(repeat-1);
		
		for (int i=0; i<numGenes_; i++)
			ts.set(index, i, data.get(i));
	}

	
	// ----------------------------------------------------------------------------

	/** Set numExperiments_ equal max(numExperiments_, repeat) */
	public void addRepeat(int repeat, Integer timePoint) {

		if (!timePoints_.contains(timePoint))
			throw new RuntimeException("Time point " + timePoint + " is not part of this time series");

		// Initialize the vector with the number of repeats per time point
		if (repeatsPerTimePoint_ == null) {
			repeatsPerTimePoint_ = new ArrayList<Integer>();
			for (int i=0; i<timePoints_.size(); i++)
				repeatsPerTimePoint_.add(0);
		}
		
		int index = getIndexOfTimePoint(timePoint);
		repeatsPerTimePoint_.set(index, repeatsPerTimePoint_.get(index)+1);
		
		// Update the number of time series
		if (repeat > numExperiments_)
			numExperiments_ = repeat;
	}

	
	// ----------------------------------------------------------------------------

	/** Get the index of this time point in timePoints_, throws and exception if not found */
	private int getIndexOfTimePoint(double t) {
		
		// Get the index of this time point
		Iterator<Integer> iter = timePoints_.iterator();
		int index = 0;
		// Note, we already checked above that the time point is actually in timePoints_
		while (iter.hasNext()) {
			if (iter.next() == t)
				return index;
			else
				index++;
		}

		throw new RuntimeException("Time point " + t + " is not part of this time series");
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS

	public int getNumTimePoints() { return timePoints_.size(); }
	
	public void setTimeSeries(ArrayList<DoubleMatrix2D> ts) { timeSeries_ = ts; } 
	public ArrayList<DoubleMatrix2D> getTimeSeries() { return timeSeries_; }
	
	public ArrayList<DoubleMatrix2D> getTimeSeriesProteins() { return timeSeriesProteins_; }
	public boolean getRestoreWildTypeAtHalftime() { return restoreWildTypeAtHalftime_; }
	//public DoubleMatrix1D getXy0() { return xy0_; }
	TreeSet<Integer> getTimePoints() { return timePoints_; }
	ArrayList<Integer> getRepeatsPerTimePoint() { return repeatsPerTimePoint_; }
	
	/** Overrides Experiment.setDefinition(), also sets the time points */
	public void setDefinition(ExperimentDefinition definition) { 
		definition_ = definition;
		definition_.setTimePoints(timePoints_);
	}

	/** Overrides Experiment.setNumExperiments() to also set the repeats per time point accordingly */
	public void setNumExperiments(int numExperiments) { 
		numExperiments_ = numExperiments;
		
		repeatsPerTimePoint_ = new ArrayList<Integer>();
		for (int i=0; i<timePoints_.size(); i++)
			repeatsPerTimePoint_.add(numExperiments);			
	}

	
	// ============================================================================
	// DEPRECATED
	
	/**
	 * Set the initial conditions from an array of strings (the concentrations)
	 */
	@Deprecated
	public void setInitialConditions(String[] xy0) {
		
		int length = numGenes_;
		if (modelTranslation_)
			length *= 2;
		
		if (xy0.length != length)
			throw new IllegalArgumentException("TimeSeriesExperiment:setInitialConditions(): " +
					"xy0.length = " + xy0.length + " doesn't match the number of state variables " + length);
		
		xy0_ = new DenseDoubleMatrix1D(length);
		for (int i=0; i<length; i++)
			xy0_.set(i, Double.parseDouble(xy0[i]));
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Deprecated, this is how it was done in DREAM3.
	 * Set random initial conditions based on a standard condition (the wild-type).
	 * The initial condition for gene i is x_i(0) = wildType_i + epsilon, where
	 * epsilon is a random number from a Gaussian distribution with mean zero and
	 * standard deviation as specified in Universal::perturbationStdev_.  
	 */
	/*
	@Deprecated
	public void setRandomInitialConditions(DoubleMatrix1D wildType) {
		
		if (wildType.size() != numGenes_)
			throw new IllegalArgumentException("TimeSeriesExperiment::setRandomInitialConditions(): " +
					"the size of wildType does not correspond with the number of genes!");
		
		// The distribution for the perturbation: limited in [0 1], the mean will be the corresponding wild-type
		double stdev = GnwSettings.getInstance().getPerturbationStdev();
		RandomParameterGaussian randomInitialCondition = new RandomParameterGaussian(0, 1, 0, stdev, false);
		
		// Allocate space
		int length = numGenes_;
		if (modelTranslation_)
			length *= 2;
		xy0_ =  new DenseDoubleMatrix1D(length);
		
		// Set random initial conditions for the mRNA (x)
		for (int i=0; i<numGenes_; i++) {
			randomInitialCondition.setMean(wildType.get(i));
			xy0_.set(i, randomInitialCondition.getRandomValue());
		}
		
		// Corresponding initial conditions for the proteins (as if the above initial condition would be held fixed)
		// 0 = mTranslation*x_i - deltaProt*y_i  =>  y_i = mTranslation*x_i / deltaProt
		if (modelTranslation_) {
			for (int i=0; i<numGenes_; i++) {
				double m = grn_.getGene(i).getMaxTranslation();
				double d = grn_.getGene(i).getDeltaProtein();
				xy0_.set(numGenes_+i, m*xy0_.get(i) / d);
			}
		}
	}	
	*/
	
}
