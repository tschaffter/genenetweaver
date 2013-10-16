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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.logging.Logger;

import org.apache.commons.collections15.Transformer;

import ch.epfl.lis.networks.Edge;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

/** This transformer defines the arrow shapes of the graph visualizations.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 * @param <V> Node (vertex)
 * @param <E> Edge
 * 
 */
public class ArrowShapeTransformer<V, E> implements Transformer< Context<Graph<V,E>,E>,Shape > {
	
	/** Enhancer connections type */
	private Byte enhancerType_ = null;
	/** Inhibitor connections type */
	private Byte inhibitorType_ = null;
	/** Dual connections type */
	private Byte dualType_ = null;
	/** Unknown connections type */
	private Byte unknownType_ = null;
	
	/** Notched arrow */
	public static Shape notchedArrow_ = ArrowFactory.getNotchedArrow(8, 9, 2);
	/** Circular arrow */
	public static Ellipse2D.Double sphericalArraw_ = getSphericalArrow(4);
	/** Square arrow */
	public static Rectangle squareArrow_ = getRectangularArrow(6, 6);
	/** Rectangular arrow */
	public static Rectangle rectangularArrow_ = getRectangularArrow(3, 14);
	
	/** Arrow of the enhancer connections */
	private Shape enhancerArrow_ = null;
	/** Arrow of the inhibitor connections */
	private Shape inhibitoryArrow_ = null;
	/** Arrow of the dual connections */
	private Shape dualArrow_ = null;
	/** Arrow of the unknown connections */
	private Shape unknownArrow_ = null;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private Logger log_ = Logger.getLogger(ArrowShapeTransformer.class.getName());
    
	
	/**
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
	public ArrowShapeTransformer() {
		initialize();
		distinguishConnectionByArrowHead(true);
	}
	
	/**
	 * This constructor allows to distinguish or not the signed connections by their
	 * arrow heads.
	 * @param distinguishSignedConnections
	 */
	public ArrowShapeTransformer(boolean distinguishSignedConnections) {
		initialize();
		distinguishConnectionByArrowHead(distinguishSignedConnections);
	}
	
	/**
	 * Constructor with arrow shapes specified.
	 * @param enhancerArrow
	 * @param inhibitorArrow
	 * @param unknownArrow
	 */
	public ArrowShapeTransformer(Shape enhancerArrow, Shape inhibitorArrow, Shape dualArrow, Shape unknownArrow) {
		initialize();
		enhancerArrow_ = enhancerArrow;
		inhibitoryArrow_ = inhibitorArrow;
		dualArrow_ = dualArrow;
		unknownArrow_ = unknownArrow;
	}
	
	/**
	 * Create a circular arrow.
	 * @param radius
	 * @return The circular arrow
	 */
	public static Ellipse2D.Double getSphericalArrow(float radius) {
    	Ellipse2D.Double arrow = new Ellipse2D.Double();
    	arrow.x = -2*radius;
    	arrow.y = -radius;
    	arrow.height = 2*radius;
    	arrow.width = 2*radius;
        return arrow;
    }
    
	/**
	 * Create a rectangular arrow.
	 * @param w
	 * @param h
	 * @return The rectangular arrow
	 */
    public static Rectangle getRectangularArrow(int w, int h) {
    	Rectangle arrow = new Rectangle();
    	arrow.x = -w;
    	arrow.y = -h/2;
    	arrow.height = h;
    	arrow.width = w;
    	return arrow;
    }

    /**
     * Return the arrow that should be used for each edge of the network.
     */
	public Shape transform(Context<Graph<V, E>, E> context) {

		Edge e = (Edge) context.element;
		Byte type = e.getType();
		
		if (type.equals(enhancerType_))
			return enhancerArrow_;
		else if (type.equals(inhibitorType_))
			return inhibitoryArrow_;
		else if (type.equals(dualType_))
			return dualArrow_;
		else if (type.equals(unknownType_))
			return unknownArrow_;
		else
			return unknownArrow_;
	}
	
	/**
	 * Set if the transformer should distinguish each signed connection by its arrow head.
	 * @param yes
	 */
	public void distinguishConnectionByArrowHead(boolean yes) {
		if (yes) {
			enhancerArrow_ = notchedArrow_;
			inhibitoryArrow_ = rectangularArrow_;
			dualArrow_ = squareArrow_;
			unknownArrow_ = sphericalArraw_;
		}
		else {
			enhancerArrow_ = notchedArrow_;
			inhibitoryArrow_ = notchedArrow_;
			dualArrow_ = notchedArrow_;
			unknownArrow_ = notchedArrow_;
		}
	}
	
	public void setEnhancerArrow(Shape shape) { enhancerArrow_ = shape; }
	public void setInhibitoryArrow(Shape shape) { inhibitoryArrow_ = shape; }
	public void setDualArrow(Shape shape) { dualArrow_ = shape; }
	public void setUnknownArrow(Shape shape) { unknownArrow_ = shape; }
	
	public Shape getEnhancerArrow() { return enhancerArrow_; }
	public Shape getInhibitoryArrow() { return inhibitoryArrow_; }
	public Shape getDualArrow() { return dualArrow_; }
	public Shape getUnknownArrow() { return unknownArrow_; }
}
