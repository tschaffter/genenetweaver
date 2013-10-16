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

package ch.epfl.lis.gnwgui;

import javax.swing.JTextField;

import java.awt.Color;
import java.util.logging.Logger;

/**  Implements of a text search box.
 * 
 * This component is a JTextField used by the user to find a key word among
 * a list of String. The search process highlights 3 cases:
 * 0 - string[i] doesn't contain the key word
 * 1 - string[i] contains the key word
 * 2 - string[i] is the key word (perfect match)
 * The background colour of the JTextField changes following the result of the search.
 * By default: green if perfect match, red if no solution and white otherwise.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class SearchBox extends JTextField {

	/** Serialization */
	private static final long serialVersionUID = 1L;

	/** Background colour of the search field if a perfect match is found. */
	private Color foundColour_;			
	/** Background colour of the search field if 0 solution is found. */					
	private Color notFoundColour_;
	/** Book, data to process during search */
	private String[] book_;
	/** Vector result (size = book_.length)*/
	private int[] result_;
	/** Single result */
	private int singleResult_;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(SearchBox.class.getName());
	
	// ============================================================================
	// PUBLIC METHODS
	//
	
	/**
	 * Default constructor
	 */
	public SearchBox() {
		init();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Initialization method
	 */
	public void init() {
		foundColour_ = new Color(170, 255, 170);		/** Green */
		notFoundColour_ = new Color(255, 170, 170);	/** Red */
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the data to see during a key word search.
	 * @param data String data
	 */
	public void setData(String[] data) {
		book_ = data;
		result_ = new int[book_.length];
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Return the result vector of a key word search. Elements of this vector could
	 * take 3 values:
	 * 0 - data[i] doesn't contain the key word
	 * 1 - data[i] contains the key word
	 * 2 - data[i] is the key word 
	 * @return The vector result
	 */
	public int[] getResult() {
		return result_;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Process the key word search.
	 */
	public void search() throws Exception {
		
		if (book_ == null) {
			System.out.println("Data is null");
			throw new Exception("SearchField:search(): Data must been first" +
					"initialized!");
		}
		
		// We start to suppose that there is no solution.
		singleResult_ = 0;
		// Reinitialise the result_ vector (necessary if key word size == 0)
		for (int i=0; i < book_.length; i++)
			result_[i] = 0;
		
		// If key is empty, stop the search
		if (getText().length() == 0) {
			setSearchFieldColor();
			return;
		}
		
		for (int i=0; i < book_.length; i++) {
			
			// If key is strictly longer than list[i] -> no match
			if (getText().length() > book_[i].length()) {
				result_[i] = 0;
			}
			// If list[i] == key
			if (book_[i].compareTo(getText()) == 0) {
				result_[i] = 2;
				singleResult_ = -1;
			}
			// If the string[i] contains key
			// Use regular expression
			else if (book_[i].matches("(?i).*" + getText() +".*")) {
				result_[i] = 1;
				if (singleResult_ != -1) {
					singleResult_++;
				}
			}
		}
		setSearchFieldColor();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Set the colour of the field search relative to the result of the search.
	 */
	public void setSearchFieldColor() {

		if (singleResult_ == 0 && getText().length() != 0)
			setBackground(notFoundColour_);
		else if (singleResult_ == -1)
			setBackground(foundColour_);
		else
			setBackground(Color.white);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Return true if there is a perfect match.
	 * @return Boolean
	 */
	public boolean isGreen() {
		if (getBackground() == foundColour_) {
			return true;
		}
		return false;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return the current text in the search field.
	 * @return key word
	 */
	public String getKey() {
		return getText();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Get a data in the book specified by its index
	 * @param index Index of the data in the book
	 * @return Data
	 */
	public String getDataAt(int index) {
		return book_[index];
	}
}
