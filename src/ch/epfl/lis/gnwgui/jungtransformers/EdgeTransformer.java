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

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.logging.Logger;

import org.apache.commons.collections15.Transformer;

import ch.epfl.lis.networks.Edge;

/** This transformer defines the edge shapes of the graph visualizations.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 * @param <E> Edge
 * 
 */
public class EdgeTransformer<E> implements Transformer<E,Stroke> {
	
	/** Enhancer connections type */
	private Byte enhancerType_ = null;
	/** Inhibitor connections type */
	private Byte inhibitorType_ = null;
	/** Dual connections type */
	private Byte dualType_ = null;
	/** Unknown connections type */
	private Byte unknownType_ = null;
	
	/** Arrow of the enhancer connections */
	private Stroke enhancerStroke_ = null;
	/** Arrow of the inhibitor connections */
	private Stroke inhibitoryStroke_ = null;
	/** Arrow of the dual connections */
	private Stroke dualStroke_ = null;
	/** Arrow of the unknown connections */
	private Stroke unknownStroke_ = null;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private Logger log_ = Logger.getLogger(EdgeTransformer.class.getName());
    
	
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
	public EdgeTransformer() {
		initialize();
		distinguishConnectionByEdge(true);
	}
	
	/**
	 * This constructor allows to distinguish or not the signed connections by their
	 * edge/arrow color.
	 * @param distinguishSignedConnections
	 */
	public EdgeTransformer(boolean distinguishSignedConnections) {
		initialize();
		distinguishConnectionByEdge(distinguishSignedConnections);
	}
	
	/**
	 * Constructor with edge/arrow color specified.
	 * @param enhancerStroke Stroke of the enhancer interactions
	 * @param inhibitorStroke Stroke of the inhibitory interactions
	 * @param dualColor Stroke of the dual interactions
	 * @param unknownStroke Stroke of the unknown interactions
	 */
	public EdgeTransformer(Stroke enhancerStroke, Stroke inhibitorStroke, Stroke dualColor, Stroke unknownStroke) {
		initialize();
		enhancerStroke_ = enhancerStroke;
		inhibitoryStroke_ = inhibitorStroke;
		dualStroke_ = dualColor;
		unknownStroke_ = unknownStroke;
	}

    /**
     * Return the color that should be used for each edge/arrow of the network.
     */
	public Stroke transform(E e) {
	
		Edge edge = (Edge) e;
		Byte type = edge.getType();
		
		if (type.equals(enhancerType_))
			return enhancerStroke_;
		else if (type.equals(inhibitorType_))
			return inhibitoryStroke_;
		else if (type.equals(dualType_))
			return dualStroke_;
		else if (type.equals(unknownType_))
			return unknownStroke_;
		else
			return getBasicStroke(10.0f);
	}
	
	/**
	 * Set if the transformer should distinguish each signed connection by a color.
	 * @param yes
	 */
	public void distinguishConnectionByEdge(boolean yes) {

		if (yes) {
			enhancerStroke_ = null;
			inhibitoryStroke_ = null;
			dualStroke_ = null;
			float[] dash = {21, 9, 3, 9};
			unknownStroke_ = getBasicStroke(dash);
//			unknownStroke_ = 
		}
		else {
			enhancerStroke_ = null;
			inhibitoryStroke_ = null;
			dualStroke_ = null;
			unknownStroke_ = null;
		}
	}
	
	public Stroke getBasicStroke(final float d) {
		float[] dash = {d};
		return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
	}
	
	public Stroke getBasicStroke(final float[] dash) {
		return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
	}
	
	public void setEnhancerArrow(Stroke stroke) { enhancerStroke_ = stroke; }
	public void setInhibitoryArrow(Stroke stroke) { inhibitoryStroke_ = stroke; }
	public void setDualArrow(Stroke stroke) { dualStroke_ = stroke; }
	public void setUnknownArrow(Stroke stroke) { unknownStroke_ = stroke; }
	
	public Stroke getEnhancerArrow() { return enhancerStroke_; }
	public Stroke getInhibitoryArrow() { return inhibitoryStroke_; }
	public Stroke getDualArrow() { return dualStroke_; }
	public Stroke getUnknownArrow() { return unknownStroke_; }
}
