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

import java.awt.*; 
import java.awt.event.*; 
import java.util.logging.Logger;

import javax.swing.JLabel;

/** Implements an interactive hypertext link.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
abstract public class HyperText extends JLabel implements MouseListener { 

	/** Serialization */
	private static final long serialVersionUID = 1L;

	/** Color of the hypertext */
	private Color linkColor_;
	/** Color of the hypertext (normal) */
	private final Color normal = Color.BLUE;
	/** Color of the hypertext when the mouse is over */
	private final Color hover = Color.BLUE;
	/** Color of the hypertext when the link is pressed. */
	private final Color press = Color.RED;
  
	/** Use to know if function action() should be launch.  */
	private boolean run_ = false;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(HyperText.class.getName());
  
    
	// ============================================================================
	// ABSTRACT METHODS
    
    // Action to do when the hypertext is clicked.
    abstract public void action();
    
    
	// ============================================================================
	// PUBLIC METHODS
  
    /**
     * Constructor
     * @param text Text of the hypertext
     */
    public HyperText(String text) {
    	super();
    	String formatedText = "<html><u>" + text + "</u></html>";
    	this.setText(formatedText);
    	this.addMouseListener(this); 
    	setForeground(normal); 
    	linkColor_ = normal; 
	}
    
    public HyperText(String text, String prefixNotLinked) {
    	super();
    	String formatedText = "<html>"+ prefixNotLinked +"<u>" + text + "</u></html>";
    	this.setText(formatedText);
    	this.addMouseListener(this); 
    	setForeground(normal); 
    	linkColor_ = normal; 
	}
    

	// ----------------------------------------------------------------------------
    
    /**
     * Paint
     */
	public void paint(Graphics g) { 
		super.paint(g);
		setForeground(linkColor_); 
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Called when a mouse button is released.
	 */
	public void mouseReleased(MouseEvent me) {
		linkColor_ = normal;
		repaint();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Called when a mouse button is pressed.
	 */
	public void mousePressed(MouseEvent me) { 
		linkColor_ = press;
		run_ = true;
		repaint();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Called when the mouse exit the field of the hypertext.
	 */
	public void mouseExited(MouseEvent me) { 
		run_ = false;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Called when the mouse enters in the field of the hypertext.
	 */
	public void mouseEntered(MouseEvent me) {
		linkColor_ = hover;
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		repaint();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Called when after a click (mouse button pressed and then released).
	 */
	public void mouseClicked(MouseEvent me) {
		if (run_)
			action();
		run_ = false;
	}
}
