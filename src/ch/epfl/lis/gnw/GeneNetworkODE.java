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

import org.opensourcephysics.numerics.ODE;


/** ODE system that is used to simulated the gene networks.
 * 
 * This class extends org.opensourcephysics.numerics.ODE,
 * it represents the system of ordinary differential equations
 * that are used to simulate the gene network.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
public class GeneNetworkODE implements ODE {
	
	/** Reference to GeneNetwork system to integrate */
	private GeneNetwork grn_;
	/** State */
	private double[] state_;
	/** Previous state (used to check convergence) */
	private double[] previousState_;
	/** Absolute precision (see converged()) */
	private double absolutePrecision_;
	/** Relative precision (see converged()) */
	private double relativePrecision_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(GeneNetworkODE.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor, x0 is the current state of the network (initial conditions) */
	public GeneNetworkODE(GeneNetwork grn, double[] x0) {
		grn_ = grn;
		state_ = x0.clone(); 
		previousState_ = x0.clone();
		absolutePrecision_ = GnwSettings.getInstance().getAbsolutePrecision();
		relativePrecision_ = GnwSettings.getInstance().getRelativePrecision();
	}
	
	
	// ----------------------------------------------------------------------------

	/** 
	 * Gets the rate of change using the argument's state variables. This method may
	 * be invoked many times with different intermediate states as an ODESolver is 
	 * carrying out the solution.
	 */
	public void getRate(double[] state, double[] rate) {
		grn_.computeDxydt(state, rate);
	}
	
	
	// ----------------------------------------------------------------------------

	/** 
	 * Implements the method gsl_multiroot_test_delta() of GSL:
	 * This function tests for the convergence of the sequence by comparing the last step dx
	 * with the absolute error epsabs and relative error epsrel to the current position x.
	 * The test returns true if the following condition is achieved:
     * 		|dx_i| < epsabs + epsrel |x_i|
     * for each component of x and returns false otherwise.
	 */
	public boolean converged() {
		 
		for (int i=0; i<previousState_.length; i++) {
			
			double dxy = Math.abs(previousState_[i] - state_[i]); 
			
			if (dxy > absolutePrecision_ + relativePrecision_*Math.abs(state_[i])) {
				// remember point
				for (int k=0; k<previousState_.length; k++)
					previousState_[k] = state_[k];
				
				return false;
			}
		}
		return true;
	}
	
	
	// ----------------------------------------------------------------------------
	
	public void setGrn(GeneNetwork grn) { grn_ = grn; }
	public double[] getState() { return state_; }
	
}

