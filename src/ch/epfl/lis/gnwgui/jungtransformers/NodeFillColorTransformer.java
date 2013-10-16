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
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.collections15.Transformer;

import ch.epfl.lis.networks.Node;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

/** This transformer defines the inside color of the nodes belonging to the graph visualizations.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 * @param <V> Vertex
 * 
 */
public class NodeFillColorTransformer<V> implements Transformer<V,Paint> {
	
	/** Pick info */
	private PickedInfo<V> pi_ = null;
	
	/** Normal color of the graph vertices. */
    private Color defaultNodeColor = null;
    /** Color of the picked vertex. */
    private Color pickedColor_ = null;
    /** Color used for some vertices that could be a solution of a search with the search box.*/
    private Color possibleNodeColor_ = null;
    /** Color of a vertex found by the search box. Normally must be unique. */
    private Color solutionNodeColor_ = null;
    
    /** Nodes whose label match 100% the researched label. */
    private ArrayList<String> solutionNodes_ = null;
    /** Nodes whose label match in part the researched label. */
    private ArrayList<String> possibleNodes_ = null;
    
    /** Logger for this class */
    @SuppressWarnings("unused")
	private Logger log_ = Logger.getLogger(NodeFillColorTransformer.class.getName());
    
    
    /**
     * Constructor
     * @param pi
     */
    public NodeFillColorTransformer(PickedInfo<V> pi) {
        pi_ = pi;
        defaultNodeColor = new Color(113, 153, 255); 		// Blue light
        pickedColor_ = Color.YELLOW;
        possibleNodeColor_ = new Color(255, 186, 0); 		// Orange
        solutionNodeColor_ = new Color(255, 70, 0); 		// Red
//        solutionNodeColor_ = new Color(170, 255, 170); 	// Pistachio
    }
    
    public void setNodeFound(ArrayList<String> solutionNodes, ArrayList<String> possibleNodes) {
    	solutionNodes_ = solutionNodes;
    	possibleNodes_ = possibleNodes;
    }
    
    /**
     * Return a color used to paint the nodes. Different colors are returned following
     * particular cases.
     */
    public Paint transform(V v) {
    	if (pi_.isPicked(v))
    		return pickedColor_;
    	else if (solutionNodes_ != null || possibleNodes_ != null) {
    		Node n = (Node) v;
    		
    		// If the search string corresponds perfectly to some node's labels
			if (solutionNodes_.contains(n.getLabel()))
				return solutionNodeColor_;
			// If the search string is included in some node's labels
			else if (possibleNodes_.contains(n.getLabel()))
				return possibleNodeColor_;
			// Otherwise the default color is used to paint the node
			else
				return defaultNodeColor;
    	}
    	return defaultNodeColor;
    }
}
