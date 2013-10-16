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

package ch.epfl.lis.utilities.images;

import java.awt.Image;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.logging.Logger;

import javax.swing.JLabel;

/** Contains an image and its highlighted version.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class HighlightableImage
{
	/** Original image */
	private Image image_ = null;
	/** Highlighted image */
	private Image highlightedImage_ = null;
	
	/** Brightness coefficient to apply to image_. */
	protected int brightness_ = 20;
	/** Brightness filter */
	private ImageFilter filter_ = null;
	
	/** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(HighlightableImage.class.getName());
    
	// ----------------------------------------------------------------------------
	// PUBLIC
    
    public HighlightableImage(Image image)
    {
    	image_ = image;
    	process();
    }
    
    // ----------------------------------------------------------------------------
    
    public HighlightableImage(Image image, int brightness)
    {
    	image_ = image;
    	brightness_ = brightness;
    	process();
    }
    
    // ----------------------------------------------------------------------------
    
    /** Process the "highlightable" image. */
    public void process()
    {
    	filter_ = new BrightnessFilter(brightness_);
    	ImageProducer producer = image_.getSource();
    	producer = new FilteredImageSource(producer, filter_);
    	highlightedImage_ = (new JLabel()).createImage(producer);
    }
    
    // ----------------------------------------------------------------------------
    // GETTERS AND SETTERS
    
    
    public Image getImage() { return image_; }
    public Image getHighlightedImage() { return highlightedImage_; }
    
    
    // ----------------------------------------------------------------------------
    // PRIVATES CLASSES
    
    private class BrightnessFilter extends RGBImageFilter
    {
    	/** Coefficient of brightness */
    	int brightness;

    	// ----------------------------------------------------------------------------
    	// PUBLIC
    	
    	public BrightnessFilter(int b)
    	{
    		brightness = b;
    		canFilterIndexColorModel = true;
    	}

    	// ----------------------------------------------------------------------------
    	
    	/**
    	 * Multiply the RGB value of a pixel by a coefficient.
    	 * @param x X position of the pixel
    	 * @param y Y position of the pixel
    	 * @param rgb RGB value of the pixel
    	 * @return Return the new RGB value of the current pixel
    	 */
    	@Override
    	public int filterRGB(int x, int y, int rgb)
    	{
    		
    		// Get the individual colors
    		int r = (rgb >> 16) & 0xff;
    		int g = (rgb >> 8) & 0xff;
    		int b = (rgb >> 0) & 0xff;

    		// Calculate the brightness
    		r += (brightness * r) / 100;
    		g += (brightness * g) / 100;
    		b += (brightness * b) / 100;

    		// Check the boundaries
    		r = Math.min(Math.max(0, r), 255);
    		g = Math.min(Math.max(0, g), 255);
    		b = Math.min(Math.max(0, b), 255);
    		
    		// Return the result
    		return (rgb & 0xff000000) | (r << 16) | (g << 8) | (b << 0);
    	}
    }
}