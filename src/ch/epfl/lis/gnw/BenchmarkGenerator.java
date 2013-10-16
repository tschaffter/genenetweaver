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

import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/** Class used to generate the DREAM3 and DREAM4 in silico challenges.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
abstract public class BenchmarkGenerator
{
	/** The gene network */
	protected GeneNetwork grn_ = null;
	/** The wild-type obtained using ODEs is often used as initial condition */
	protected DoubleMatrix1D wildTypeODE_ = null;
	
	/** Output directory */
	protected String outputDirectory_;
	
    /** Logger for this class */
    protected static Logger log_ = Logger.getLogger(BenchmarkGenerator.class.getName());

	// ============================================================================
	// PUBLIC METHODS
	
	public BenchmarkGenerator(GeneNetwork grn)
	{
		grn_ = grn;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Run all experiments, save the gold standards and the datasets.
	 */
	abstract public void generateBenchmark() throws IllegalArgumentException, CancelException, Exception;
	
	// ============================================================================
	// PROTECTED METHODS

	/**
	 * The given experiment should be the wild-type, returns the concatenated mRNA
	 * and protein concentrations
	 */
	static public DoubleMatrix1D constructInitialConditionFromWildType(SteadyStateExperiment wildType)
	{	
		DoubleMatrix1D x = wildType.getSsPerturbation().viewRow(0);
		DoubleMatrix1D y = wildType.getSsPerturbationProteins().viewRow(0);

		if (GnwSettings.getInstance().getModelTranslation())
		{
			DoubleMatrix1D xy0 = new DenseDoubleMatrix1D(2*wildType.getGrn().getSize());
			
			for (int i=0; i<x.size(); i++)
				xy0.set(i, x.get(i));
			
			for (int i=0; i<y.size(); i++)
				xy0.set(x.size()+i, y.get(i));
			
			return xy0;
			
		} else
			return x;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setOutputDirectory(String path) { outputDirectory_ = path; }
	public String getOutputDirectory() { return outputDirectory_; }
}
