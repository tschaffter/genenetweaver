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

package ch.epfl.lis.gnwgui.jungtransformers;

import java.awt.Color;
import java.awt.Paint;
import java.util.logging.Logger;

import org.apache.commons.collections15.Transformer;

import ch.epfl.lis.networks.Edge;

/** This transformer defines the color of the interactions of the graph visualizations.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 * @param <E> Edge
 * 
 */
public class EdgeArrowColorTransformer<E> implements Transformer<E,Paint> {
	
	/** Enhancer connections type */
	private Byte enhancerType_ = null;
	/** Inhibitor connections type */
	private Byte inhibitorType_ = null;
	/** Dual connections type */
	private Byte dualType_ = null;
	/** Unknown connections type */
	private Byte unknownType_ = null;
	
	/** Arrow of the enhancer connections */
	private Color enhancerColor_ = null;
	/** Arrow of the inhibitor connections */
	private Color inhibitoryColor_ = null;
	/** Arrow of the dual connections */
	private Color dualColor_ = null;
	/** Arrow of the unknown connections */
	private Color unknownColor_ = null;

    /** Logger for this class */
    @SuppressWarnings("unused")
	private Logger log_ = Logger.getLogger(EdgeArrowColorTransformer.class.getName());
    
	
	/**ArrowColorTransformer
	 * Initialization
	 */
	private void initialize() {
		enhancerType_ = Edge.ENHANCER;
		inhibitorType_ = Edge.INHIBITOR;
		dualType_ = Edge.DUAL;
		unknownType_ = Edge.UNKNOWN;
	}
	
	/**
	 * Constructor
	 */
	public EdgeArrowColorTransformer() {
		initialize();
		distinguishConnectionByColor(true);
	}
	
	/**
	 * This constructor allows to distinguish or not the signed connections by their
	 * edge/arrow color.
	 * @param distinguishSignedConnections
	 */
	public EdgeArrowColorTransformer(boolean distinguishSignedConnections) {
		initialize();
		distinguishConnectionByColor(distinguishSignedConnections);
	}
	
	/**
	 * Constructor with edge/arrow color specified.
	 * @param enhancerColor Color of the enhancer interactions
	 * @param inhibitorColor Color of the inhibitory interactions
	 * @param unknownColor Color of the unknown interactions
	 */
	public EdgeArrowColorTransformer(Color enhancerColor, Color inhibitorColor, Color dualColor, Color unknownColor) {
		initialize();
		enhancerColor_ = enhancerColor;
		inhibitoryColor_ = inhibitorColor;
		dualColor_ = dualColor;
		unknownColor_ = unknownColor;
	}

    /**
     * Return the color that should be used for each edge/arrow of the network.
     */
	public Paint transform(E e) {
	
		Edge edge = (Edge) e;
		Byte type = edge.getType();
		
		if (type.equals(enhancerType_))
			return enhancerColor_;
		else if (type.equals(inhibitorType_))
			return inhibitoryColor_;
		else if (type.equals(dualType_))
			return dualColor_;
		else if (type.equals(unknownType_))
			return unknownColor_;
		else
			return Color.BLACK;
	}
	
	/**
	 * Set if the transformer should distinguish each signed connection by a color.
	 * @param yes
	 */
	public void distinguishConnectionByColor(boolean yes) {
		if (yes) {
			enhancerColor_ = new Color(0, 0, 130); //Color.BLACK;
			inhibitoryColor_ = Color.RED;
			dualColor_ = Color.MAGENTA;
			unknownColor_ = Color.LIGHT_GRAY; //new Color(120,120,120);
		}
		else {
			enhancerColor_ = Color.BLACK;
			inhibitoryColor_ = Color.BLACK;
			dualColor_ = Color.BLACK;
			unknownColor_ = Color.BLACK;
		}
	}
	
	public void setEnhancerArrow(Color color) { enhancerColor_ = color; }
	public void setInhibitoryArrow(Color color) { inhibitoryColor_ = color; }
	public void setDualArrow(Color color) { dualColor_ = color; }
	public void setUnknownArrow(Color color) { unknownColor_ = color; }
	
	public Color getEnhancerArrow() { return enhancerColor_; }
	public Color getInhibitoryArrow() { return inhibitoryColor_; }
	public Color getDualArrow() { return dualColor_; }
	public Color getUnknownArrow() { return unknownColor_; }
}