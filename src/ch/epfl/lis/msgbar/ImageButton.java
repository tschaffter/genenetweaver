/*
Copyright (c) 2010 Thomas Schaffter

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

package ch.epfl.lis.msgbar;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

/**
 * From http://www.rgagnon.com/javadetails/java-0245.html (active September 4th, 2010)
 */
public class ImageButton extends Canvas
{
	protected ActionListener actionListener = null;
	private int w,h;
	boolean clicked;
	boolean down;
	boolean enabled;
	Image UPimage;
	Image DOWNimage;
	Image disabledimage;
	
	private static final long serialVersionUID = 1L;
	
	// ============================================================================
	// PUBLIC FUNCTIONS

	public ImageButton(URL up_b, URL down_b)
	{
		clicked = false;
		down = false;
		enabled = true;
		InitImage(up_b, down_b);
		setSize(w, h);
		addMouseListener(new ImageButtonMouseListener());
		addMouseMotionListener(new ImageButtonMouseMotionListener());
    }
	
	// ----------------------------------------------------------------------------

	public void InitImage(URL up, URL down)
	{
		MediaTracker tracker;
		
		try
		{
			UPimage = getToolkit().getImage(up);
			DOWNimage = getToolkit().getImage(down);
      
			tracker = new MediaTracker(this);
			tracker.addImage(UPimage, 0);
			tracker.addImage(DOWNimage, 1);
			tracker.waitForAll();
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		disabledimage = createImage(new FilteredImageSource(UPimage.getSource(), new ImageButtonDisableFilter()));
		w = UPimage.getWidth(this);
		h = UPimage.getHeight(this);
    }
	
	// ----------------------------------------------------------------------------

	public void paint(Graphics g)
	{
	    int width = getWidth() - w;
	    int height = getHeight() - h;
		
		if (down)
			g.drawImage(DOWNimage, width/2, height/2, this);
		else
		{
			if (enabled)
				g.drawImage(UPimage, width/2, height/2, this);
			else
				g.drawImage(disabledimage, width/2, height/2, this);
		}
    }
  
	//----------------------------------------------------------------------------

	public void setEnabled(boolean b)
	{
		enabled = b;
		repaint();
    }
	
	// ----------------------------------------------------------------------------

	public boolean isEnabled()
	{
		return (enabled);
    }
	
	// ----------------------------------------------------------------------------

	public void addActionListener(ActionListener l)
	{
		actionListener = AWTEventMulticaster.add(actionListener, l);
    }
	
	// ----------------------------------------------------------------------------
	
	public void removeActionListener(ActionListener l)
	{
		actionListener = AWTEventMulticaster.remove(actionListener, l);
    }
	
	// ============================================================================
	// IMAGEBUTTONMOUSELISTENER CLASS

	public class ImageButtonMouseListener extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			Point p = e.getPoint();
			int x0 = (getWidth() - w)/2;
			int y0 = (getHeight() - h)/2;
			int x1 = (getWidth() + w)/2;
			int y1 = (getHeight() + h)/2;
			
			if ((p.x < x1) && (p.y < y1) && (p.x > x0) && (p.y > y0) && (enabled == true))
			{
				clicked = true;
				down = true;
				repaint();
			}
		}
		
		// ----------------------------------------------------------------------------
		
		public void mouseReleased(MouseEvent e)
		{
			Point p = e.getPoint();
			int x0 = (getWidth() - w)/2;
			int y0 = (getHeight() - h)/2;
			int x1 = (getWidth() + w)/2;
			int y1 = (getHeight() + h)/2;
			
			if (down)
			{
				down = false;
				repaint();
			}
			if ((p.x < x1) && (p.y < y1) && (p.x > x0) && (p.y > y0) && (clicked == true))
			{
				ActionEvent ae = new ActionEvent(e.getComponent(), 0, "click");
				if (actionListener != null)
					actionListener.actionPerformed(ae);
			}
			clicked = false;
		}
	}
	
	// ============================================================================
	// IMAGEBUTTONMOUSEMOTIONLISTENER CLASS
	
	public class ImageButtonMouseMotionListener extends MouseMotionAdapter
	{
		public void mouseDragged(MouseEvent e)
		{
			Point p = e.getPoint();
			int x0 = (getWidth() - w)/2;
			int y0 = (getHeight() - h)/2;
			int x1 = (getWidth() + w)/2;
			int y1 = (getHeight() + h)/2;
			
			if ((p.x < x1) && (p.y < y1) && (p.x > x0) && (p.y > y0) && (clicked == true))
			{
				if (down == false) {
					down = true;
					repaint();
				}
			} 
			else
			{
				if (down == true) {
					down = false;
					repaint();
				}
			}
		}
	}
	
	// ----------------------------------------------------------------------------

	public Dimension getPreferredSize()
	{
		return (new Dimension(UPimage.getWidth(this), UPimage.getHeight(this)));
    }
  
	//----------------------------------------------------------------------------

	public Dimension getMinimumSize() { return getPreferredSize(); }

	// ============================================================================
	// IMAGEBUTTONDISABLEFILTER CLASS
	
	class ImageButtonDisableFilter extends RGBImageFilter
	{
		public ImageButtonDisableFilter()
		{
			canFilterIndexColorModel = true;
		}
		
		//----------------------------------------------------------------------------
		
		public int filterRGB(int x, int y, int rgb)
		{
			return (rgb & ~0xff000000) | 0x80000000;
		}
	}
}
