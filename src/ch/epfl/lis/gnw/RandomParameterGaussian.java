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

import cern.jet.random.Normal;


/** 
 * Generate random values from a Gaussian distribution within the interval
 * [min max].
 * 
 * If not set explicitly, the mean is the center of the interval (min+max)/2 and 
 * the standard deviation s (stdev_) so that the interval is 6 standard deviations
 * (max-min) = 6*s. This implies the following distribution of the generated values:
 * 
 *    ... mean     +1s     +2s     +3s (=max)
 *    ------|-------|-------|-------|--------->
 *    ...   | 34.1% | 13.6% | 2.1%  |
 *        
 * Set the appropriate flag to use a log-normal distributions (note, the  parameters
 * must be given on the linear scale, and the result is also returned on linear scale)
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 * 
 */
public class RandomParameterGaussian extends RandomParameter {

	/** The mean */
	private double mean_;
	/** The standard deviation */
	private double stdev_;
	/** Use a log-normal distribution */
	private boolean logNormal_;
		

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor, mean_ and stdev_ are set according to the specified interval (see class documentation above) */
	public RandomParameterGaussian(double min, double max, boolean logNormal) {
		logNormal_ = logNormal; 
		setMinMax(min, max); // also sets mean_ and stdev_ and transforms to log if necessary
	}
	
	/** Constructor, mean_ and stdev_ are set explicitly */
	public RandomParameterGaussian(double min, double max, double mean, double stdev, boolean logNormal) {
		logNormal_ = logNormal;
		setMinMax(min, max);
		setMeanStdev(mean, stdev);
	}

	
	// ----------------------------------------------------------------------------

	/** 
	 * Draw a new random number from the distribution. If the parameters were given on
	 * log-scale and paramsOnLogScale_ is set, the result is transformed back to linear scale.
	 */
	public double getRandomValue() {
		
		Normal normal = GnwSettings.getInstance().getNormalDistribution();
		double value;
		
		do {
			value = normal.nextDouble(mean_, stdev_);
		} while (value < min_ || value > max_);
	
		if (logNormal_)
			value = Math.pow(10.0, value);
		
		return value;
	}

		
	// ----------------------------------------------------------------------------

	/** Set min_, max_, and also mean_ and stdev_ accordingly (transforms to log if logNormal_ is set) */
	public void setMinMax(double min, double max) {
		
		if (logNormal_) {
			min_ = Math.log10(min);
			max_ = Math.log10(max);
		} else {
			min_ = min;
			max_ = max;	
		}
		mean_ = (min_+max_) / 2.0;
		stdev_ = (max_-min_) / 6.0;
	}
	
	
	// ----------------------------------------------------------------------------

	/** Set mean_ and stdev_ (transforms to log if logNormal_ is set) */
	public void setMeanStdev(double mean, double stdev) {
		
		if (logNormal_) {
			mean_ = Math.log10(mean);
			stdev_ = Math.log10(stdev);
		} else {
			mean_ = mean;
			stdev_ = stdev;	
		}
	}
	
	
	// ----------------------------------------------------------------------------

	/** Set the mean, transform to log if logNormal_ is set */
	public void setMean(double mean) {
		
		if (logNormal_)
			mean_ = Math.log10(mean);
		else
			mean_ = mean;
	}		

}
