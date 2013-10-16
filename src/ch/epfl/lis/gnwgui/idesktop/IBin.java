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

package ch.epfl.lis.gnwgui.idesktop;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.logging.Logger;

import ch.epfl.lis.utilities.images.HighlightableImage;

/**
 * Recycle bin
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class IBin extends IElement
{
    /** Default serialization */
	private static final long serialVersionUID = 1L;
	
	/** Icon to display when the bin is empty. */
	private HighlightableImage emptyIcon_ = null;
	/** Icon to display when the bin is not empty. */
	private HighlightableImage filledIcon_ = null;
	
	/** Is the bin empty ? */
	private boolean emtpy_ = true;
	/** Content of the bin (not used actually) */
	private ArrayList<IElement> binContent_ = null;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(IBin.class.getName());

    // ----------------------------------------------------------------------------
    // PUBLIC FUNCTIONS
	
	public IBin(IDesktop desk, String label)
	{	
		super(label, desk);
		initialize();
	}
	
	// ----------------------------------------------------------------------------

	public void initialize()
	{	
		binContent_ = new ArrayList<IElement>();
		emptyIcon_ = setDefaultIcon(Color.GRAY);
		filledIcon_ = setDefaultIcon(Color.GRAY);
		setItemIcon(emptyIcon_);
		emtpy_ = true;
		destroyable_ = false;
		
		repaint();
	}
	
	// ----------------------------------------------------------------------------
	
    public void addItemIntoBin(IElement item)
    {
    	emtpy_ = false;
    	setItemIcon(filledIcon_);
    	repaint();
    }
    
 // ----------------------------------------------------------------------------

    public void clearBin()
    {
    	binContent_.clear();
    	emtpy_ = true;
    	setItemIcon(emptyIcon_);
    	repaint();
    }
    
    // ----------------------------------------------------------------------------
    
	@Override
	public IElement copyElement()
	{
		return null;
	}
	
	// ----------------------------------------------------------------------------

	@Override
	protected void leftMouseButtonInvocationSimple() {}
	
	@Override
	protected void leftMouseButtonInvocationDouble() {}

	@Override
	protected void wheelMouseButtonInvocation() {}
	
	@Override
	protected void rightMouseButtonInvocation() {}
    
    /// ----------------------------------------------------------------------------
    // GETTERS AND SETTERS
    
    public void setEmptyIcon(Image icon)
    {
    	emptyIcon_ = new HighlightableImage(icon);
    	
    	if (emtpy_)
    		setItemIcon(emptyIcon_);
    	
    	repaint();
    }
    
    // ----------------------------------------------------------------------------
    
    public void setFilledIcon(Image icon) {
    	filledIcon_ = new HighlightableImage(icon);
    	if (!emtpy_)
    		setItemIcon(filledIcon_);
    	repaint();
    }
    
    // ----------------------------------------------------------------------------
}
