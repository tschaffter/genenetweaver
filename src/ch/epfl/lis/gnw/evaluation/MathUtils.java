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

package ch.epfl.lis.gnw.evaluation;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Class for some math functions, like min, median, etc.
 * 
 * @author Daniel Marbach (firstname.name@gmail.com)
 */
public class MathUtils {

	
	// ----------------------------------------------------------------------------

	/** Return the min value of the given list */
	static public double min(ArrayList<Double> v) {
		
		if (v.size() == 0)
			throw new IllegalArgumentException("The vector does not contain any elements");
		
		double min = v.get(0);
		for (int i=0; i<v.size(); i++)
			if (v.get(i) < min)
				min = v.get(i);
		
		return min;
	}
	

	// ----------------------------------------------------------------------------

	/** Return the median */
	static public double median(ArrayList<Double> v) {
		
		Collections.sort(v);
		
		if (v.size() % 2 == 1)
			return v.get((v.size()+1)/2-1);
		else {
			double lower = v.get(v.size()/2-1);
			double upper = v.get(v.size()/2);
		 
			return (lower + upper) / 2.0;
		}	
	}
    

	// ----------------------------------------------------------------------------

    /**
     * Correct the ranks to compute the medians correctly.
	 * Some teams have not included all links in the list of predictions, i.e.,
	 * they have not assigned a rank to the links at the bottom of the list. We
	 * have assigned the negative of the next rank to them:
	 *
	 * y1 = -.6 -.6 -.6 -.6 -.6 -.6 -.6 0.7 0.8 0.9 1.0
     *      |------ not predicted ----|
	 *
	 * We could also have randomly ordered the remaining links:
	 *
	 * y2 = 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
     *      |------ not predicted ----|
	 *
	 * median(y1) = 0.3
	 * median(y2) = 0.5
	 *
	 * We had to do version 1 in order to evaluate the performance without
	 * introducing possible bias due to the random ordering. However, the median
	 * (0.3 in the example above) is incorrect, because in any random ordering
	 * it would be 0.5. The input to this function are ranks done like the first
	 * version, the output is the median according to the second version. 
     */
    static public void correctRanks(ArrayList<Double> ranks) {

    	double min = min(ranks);
        
        while (min < -1e-12) {  
            
        	int numAssigned = 0;
        	for (int i=0; i<ranks.size(); i++)
        		if (ranks.get(i) == min)
        			numAssigned++;
            
            int counter = 1;
            double delta = - min / (numAssigned+1);
            
            for (int i=0; i<ranks.size(); i++) {
                if (ranks.get(i) == min) {
                   ranks.set(i, counter*delta);
                   counter++;
                }
            }
            
            min = min(ranks);
        }
    }

    
	// ----------------------------------------------------------------------------

    /** Convert an ArrayList<Double> to an array double[] */ 
    static public double[] toArray(ArrayList<Double> list) {
    	
    	double[] array = new double[list.size()];
    	
    	for (int i=0; i<list.size(); i++)
    		array[i] = list.get(i);
    	
    	return array;
    }
    
    
	// ----------------------------------------------------------------------------
    
    /** Concatenate the two arrays */
    /*private double[] concatenate(double[] a, double[] b) {
    	
    	int A = a.length;
    	int B = b.length;
    	
    	double[] ab = new double[A+B];
    	
    	for (int i=0; i<A; i++)
    		ab[i] = a[i];
    	
    	for (int i=0; i<B; i++)
    		ab[A+i] = b[i];
    	
    	return ab;
    }*/

    	


}
