/*
Copyright (c) 2008-2010 Thomas Schaffter & Daniel Marbach

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

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.util.Animator;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import java.io.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;

import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;

import org.apache.commons.collections15.functors.ConstantTransformer;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import ch.epfl.lis.gnwgui.jungtransformers.ArrowShapeTransformer;
import ch.epfl.lis.gnwgui.jungtransformers.EdgeArrowColorTransformer;
import ch.epfl.lis.gnwgui.jungtransformers.EdgeTransformer;
import ch.epfl.lis.gnwgui.jungtransformers.NodeFillColorTransformer;
import ch.epfl.lis.gnwgui.jungtransformers.NodeLabelLabeller;
import ch.epfl.lis.gnwgui.windows.GraphViewerController;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.utilities.filefilters.FilterImageEPS;
import ch.epfl.lis.utilities.filefilters.FilterImageJPEG;
import ch.epfl.lis.utilities.filefilters.FilterImagePNG;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.Structure;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.screencap.EPSDump;
import edu.uci.ics.screencap.PNGDump;


/** Implements a graph visualization using the JUNG library.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class NetworkGraph {
	
	/** NetworkItem associated to this network representation */
	private NetworkElement item_ = null;
	/** Instance of the network */
	private Structure structure_ = null;
	/** Size of the network */
	private int netSize_ = 0;

	/** Graph */
	private Graph<Node,Edge> g_ = null;
	/** Network viewer */
	private VisualizationViewer<Node,Edge> vv_ = null;
	/** Handle the action from the mouse */
	@SuppressWarnings("rawtypes")
	private DefaultModalGraphMouse gm_ = null;

	/** Re-defines how to color of the nodes */
    private NodeFillColorTransformer<Node> nodeFillColorTransformer_ = null;
    
    /** Defines different arrow heads for the different type of interactions. */
    private ArrowShapeTransformer<Node,Edge> arrowTransformer_ = null;
    /**
     * Defines different color (can be used for edge and arrow head) for the different type of interactions.
     * This transformer is used for the edge and the outside shape of the arrow heads.
     */
    private EdgeArrowColorTransformer<Edge> edgeArrowColorTransformer_ = null;
    /**
     * Defines different color (can be used for edge and arrow head) for the different type of interactions.
     * This transformer is used for the inside part of the arrow heads.
     */
    private EdgeArrowColorTransformer<Edge> insideArrowColorTransformer_ = null;
    /** Defines different stroke edges for the different type of interactions (not used). */
    private EdgeTransformer<Edge> edgeTransformer_ = null;
    
    /** Set of all vertex that can be the final result of a search. */
    private ArrayList<String> possibleNodes_ = new ArrayList<String>();
    /** Eventually contains the final result of a search. */
    private ArrayList<String> solutionNodes_ = new ArrayList<String>();
    
    /** Transformer that *don't* show the vertex label. */
    private Transformer<Node,String> vs_none_ = null;
    /** Transformer that show the vertex label. */
    private Transformer<Node,String> vs_ = null;

    /** Defined the action relative to pick one/many vertex. */
    private PickedState<Node> pickedState_ = null;
    
    /** A controller that act on the network viewer. */
    private MyGraphViewerController control_ = null;
    
    /** Contains the network or the tree representation. */
    private JPanel screen_ = null;
    
    /** Represent the layout of the network representation */
    private AbstractLayout<Node,Edge> layout_ = null;
    
    /** Graph signature */
    private GraphSignature signature_ = null;
    
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(NetworkGraph.class.getName());
    
    
    // =======================================================================================
    // PUBLIC FUNCTIONS
    //

    /**
     * Constructor
     * @param item Network
     */
	public NetworkGraph(NetworkElement item) {
		item_ = item;
		initialize();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param networkViewer An existing graph visualization
	 */
	public NetworkGraph(NetworkGraph networkViewer) {
		this.item_ = networkViewer.item_.copy();
		initialize();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Copy function
	 * @return Copy the this object
	 */
	public NetworkGraph copy() {
		return new NetworkGraph(this);
	}

	
	// ----------------------------------------------------------------------------
	
	public void initialize() {
		
		if (item_ instanceof StructureElement) {
			structure_ = ((StructureElement)item_).getNetwork();
		} else if (item_ instanceof DynamicalModelElement) {
			structure_ = ((DynamicalModelElement)item_).getGeneNetwork();
		}
		
		netSize_ = structure_.getSize();
		control_ = new MyGraphViewerController();
		screen_ = new JPanel();
		screen_.setLayout(new BorderLayout());
        computeGraph();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Create the graph from the structure of a network.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void computeGraph() {
		
		if (structure_.isDirected())
			g_ = new DirectedSparseMultigraph<Node,Edge>();
		else
			g_ = new UndirectedSparseMultigraph<Node,Edge>();
		
		// The layout is instantiated after all nodes are added to the graph.
		// Otherwise the nodes are misplaced!
    	addVertices();
    	addEdges();
    	
    	//Layout v= new FadingNodeLayout( 10, new SpringLayout( g ));    	
        layout_ = new FRLayout<Node,Edge>(g_);
    	// sets the initial size of the layout space
        layout_.setSize(new Dimension(250,250));
        vv_ = new VisualizationViewer<Node,Edge>(layout_); // Graph layout
        pickedState_ = vv_.getPickedVertexState(); // Get information from the nodes picked
   
        // If node positions are present in the Structure object, e.g. loaded from
        // DOT files, the layout is remodelled.
        loadStructureLayout();
        
        
        // LABEL
        vs_none_ = new ConstantTransformer(null);
//        vs_ = new ToStringLabeller();
        vs_ = new NodeLabelLabeller();

        // NODES, EDGES AND ARROW HEADS
        // By default no distinction of arrow head
        arrowTransformer_ = new ArrowShapeTransformer<Node,Edge>(false);
        // By default no distinction of edge/outside arrow color and inside arrow color
        edgeArrowColorTransformer_ = new EdgeArrowColorTransformer<Edge>(false);
        insideArrowColorTransformer_ = new EdgeArrowColorTransformer<Edge>(false);
        // By default no distinction of stroke edges
        edgeTransformer_ = new EdgeTransformer<Edge>(false);
        // Defines the color of the nodes
        nodeFillColorTransformer_ = new NodeFillColorTransformer<Node>(pickedState_);
        
        vv_.setPreferredSize(new Dimension(400, 250));
        vv_.setBackground(Color.white);
        vv_.getRenderContext().setVertexFillPaintTransformer(nodeFillColorTransformer_);
        vv_.getRenderContext().setVertexLabelTransformer(vs_none_/*new ToStringLabeller()*/);
        vv_.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);
        vv_.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Node,Edge>());
    	
		vv_.getRenderContext().setEdgeArrowTransformer(arrowTransformer_);
    	vv_.getRenderContext().setEdgeDrawPaintTransformer(edgeArrowColorTransformer_); // edge color
    	vv_.getRenderContext().setArrowFillPaintTransformer(insideArrowColorTransformer_); // arrow inside color
    	vv_.getRenderContext().setArrowDrawPaintTransformer(edgeArrowColorTransformer_); // arrow outside color
    	vv_.getRenderContext().setEdgeStrokeTransformer(edgeTransformer_); // edge transformer
    	
        
        // Create a graph mouse and add it to the visualization component
        gm_ = new DefaultModalGraphMouse();
        gm_.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv_.setGraphMouse(gm_);
        
        setScreen(vv_); // Draw the viewer into the panel displayed
        addPrintAction(screen_); // Add the key action ALT-P to print the JPanel screen_
        
        // Finally, add the signature at the low-bottom corner of the graph visualization
//        addSignature(vv_);
        signature_ = new GraphSignature(vv_, new ImageIcon(GnwGuiSettings.getInstance().getGnwWatermarkImage()));
        try {
			signature_.addSignatureToContainer();
		} catch (Exception e) {
			log_.log(Level.WARNING, "NetworkGraph::computeGraph(): " + e.getMessage(), e);
		}
	}
	
	
	/**
	 * Take the positions saved in the individual nodes of the Structure
	 * and place the node on the graph accordingly.
	 */
	public void loadStructureLayout() {
		Node node;
		for (int i=0; i < netSize_; i++) {
			if ((node = structure_.getNode(i)) != null && node.getPosition() != null)
				layout_.setLocation(node, node.getPosition());
		}
	}
	

	/**
	 * Save the coordinates {x,y} of all nodes from the graph and save the positions
	 * in the individual node of the Structure. Don't capture an eventual rotation or
	 * distortion of the JUNG layout.
	 */
	public void saveStructureLayout() {
		Collection<Node> list = g_.getVertices();
		Iterator<?> it = list.iterator();
		Node node;
		
		while (it.hasNext()) {
			node = (Node) it.next();
			node.setPosition(new Point((int)layout_.getX(node), (int)layout_.getY(node)));
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Modify the way in which the user can interact with the graph.
	 * @param mode mode=0: transforming, mode=1: picking
	 */
	public void setGraphMouseMode(int mode) {

			if (gm_ == null) return;
			switch (mode) {
				case 0: gm_.setMode(ModalGraphMouse.Mode.TRANSFORMING); break;
				case 1: gm_.setMode(ModalGraphMouse.Mode.PICKING); break;
			}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Add the vertices contained in the structure to the graph. In this graph
	 * representation, a node (vertex) is merely a String.
	 */
	private void addVertices() {
		for (int i=0; i < netSize_; i++)
			g_.addVertex(structure_.getNode(i));
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Add the edges contained in the structure to the graph.
	 */
	private void addEdges() {
		Edge edge;
		for (int i=0; i < structure_.getNumEdges(); i++) {
			edge = structure_.getEdge(i);
			g_.addEdge(edge, edge.getSource(), edge.getTarget());
			
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * For the search part, this class has 2 group of vertices: the first contains
	 * the vertices (their index) that have their label associated to the search
	 * key entered by the user. The second one contains the vertices (generally
	 * only one) that have their label matching perfectly to the search key.
	 * This function classes all the vertices between these 3 groups following
	 * the result of the vertices search.
	 * @param selection Vector of <Integer> that contains the result of the search
	 * (see this.search())
	 */
	public void highlightVertexesSelection(int[] selection) {
		
	    possibleNodes_.clear(); // Clear the two groups of vertices
	    solutionNodes_.clear();

		for (int i=0; i < selection.length; i++) {
			if (selection[i] == 2) // Perfect match
				solutionNodes_.add(structure_.getNode(i).getLabel());
			else if (selection[i] == 1) // Is possible solution
				possibleNodes_.add(structure_.getNode(i).getLabel());
		}
		nodeFillColorTransformer_.setNodeFound(solutionNodes_, possibleNodes_);
		vv_.repaint();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Add a key listener to print the content of the JPanel in which the graph is drawn.
	 * @param jp JPanel that contains the network graph.
	 */
	@SuppressWarnings("serial")
	public void addPrintAction(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke("alt P");
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "printscreen");
	   jp.getActionMap().put("printscreen", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   item_.getNetworkViewer().getControl().printGraph();
		   }
	   });
	}
	
	
    // =======================================================================================
    // GETTERS AND SETTERS
    //
	
	public VisualizationViewer<Node,Edge> getVisualizationViewer() { return  vv_; }
	
	public MyGraphViewerController getControl() { return control_; }
	
	public void setScreen(JPanel screen) { screen_ = screen; }
	public void setScreen(VisualizationViewer<Node,Edge> visual) {
		screen_.removeAll();
		screen_.add(visual, BorderLayout.CENTER);
		visual.repaint();
		screen_.revalidate();
	}
	public JPanel getScreen() { return screen_; }
	
	
	
	
	
	
	
	
	
	
	/**
	 * This class provides many methods to act on a JUNG graph. Is derived from the class
	 * NetworkViewerControl that only defined the design (Swing components) of the controller.
	 * 
	 * @author Thomas Schaffter (firstname.lastname@gmail.com)
	 *
	 */
	public class MyGraphViewerController extends GraphViewerController {
		
		/** Serialization */
		private static final long serialVersionUID = 1L;
		
		/** 2 graph modes: transforming and picking. */
		private String[] interactionModeList_ = {"Move graph", "Move nodes"};
		/** */
		private String[] layoutList_ = {"KK layout", "FR layout", "Circle layout"};
		/** A listener on this document will customize the ssearch box. */
		private Document document_;
		/**
		 * The basic data used to set the "dictionary" of the search box.
		 * Here, the basic data are the ID of the vertex of the network graph.
		 */
		private String[] dataVertices_;
		
		
	    // =======================================================================================
        // PRIVATE FUNCIONS
        //
		
		/**
		 * Initialize the controller and all the listeners of the components.
		 */
		private void initialize() {
			
			// Defines the way the user can interact with the graph
			interactionMode_.setModel(new DefaultComboBoxModel<String>(interactionModeList_));
			interactionMode_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					interactionMode();
				}
			});
			
			layoutCombo_.setModel(new DefaultComboBoxModel<String>(layoutList_));
			layoutCombo_.setSelectedItem(layoutList_[1]);
			layoutCombo_.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					changeGraphLayout((String) layoutCombo_.getSelectedItem());
				}
			});
			
			// Allow the user to take a picture of the current content of visualization panel
			exportButton_.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					printGraph();
				}
			});
			
			// Implement action listeners for the vertex search field
			// First the search field must be initialized with all the vertex names
			dataVertices_ = new String[netSize_];
			for (int i=0; i < netSize_; i++) {
				dataVertices_[i] = structure_.getNode(i).getLabel();
			}
			search_.setData(dataVertices_);
			document_ = search_.getDocument();
			
			document_.addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent arg0) {
					processVerticesLabelSearch();
				}
				public void insertUpdate(DocumentEvent arg0) {
					processVerticesLabelSearch();
				}
				public void removeUpdate(DocumentEvent arg0) {
					processVerticesLabelSearch();
				}
			});
			
			displayLabels_.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					displayLabel();
				}
			});
			
			curvedEdges_.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setEdgeOrCurvedLine();
				}
			});
			
			distinguishByColor_.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					distinguishConnectionByColor();
				}
			});
			
			distinguishByArrowHead_.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					distinguishConnectionByArrowHead();
				}
			});
		}
		
        // =======================================================================================
        // PUBLIC FUNCIONS
        //
		
		/**
		 * Constructor
		 */
		public MyGraphViewerController() {
			super();
			initialize();
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Called when the user check the box "Distinguish signed interactions by color"
		 */
		public void distinguishConnectionByColor() {
			
        	if (vv_ == null) return;
        	
        	if (distinguishByColor_.isSelected()) {
            	edgeArrowColorTransformer_.distinguishConnectionByColor(true);
            	insideArrowColorTransformer_.distinguishConnectionByColor(true);
            	if (distinguishByArrowHead_.isSelected())
            		insideArrowColorTransformer_.setUnknownArrow(Color.WHITE);
        	}
        	else {
        		edgeArrowColorTransformer_.distinguishConnectionByColor(false);
        		insideArrowColorTransformer_.distinguishConnectionByColor(false);
            	if (distinguishByArrowHead_.isSelected())
            		insideArrowColorTransformer_.setUnknownArrow(Color.WHITE);
        	}
        	vv_.repaint();
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Called when the user check the box "Curved edges"
		 */
		public void setEdgeOrCurvedLine() {
			
        	if (vv_ == null) return;
        	
        	if (curvedEdges_.isSelected())
        		vv_.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<Node,Edge>());
        	else
        		vv_.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Node, Edge>());
      
        	vv_.repaint();
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Create a new layout according to the selected layout strategy.
		 */
		public void changeGraphLayout(String selection) {
			
			try {
				AbstractLayout<Node,Edge> l;
				
				if (selection.compareTo("FR layout") == 0)
					l = new FRLayout<Node,Edge>(g_);
				else if (selection.compareTo("Circle layout") == 0)
					l = new CircleLayout<Node,Edge>(g_);
				else if (selection.compareTo("KK layout") == 0)
					l = new KKLayout<Node,Edge>(g_);
				else
					throw new Exception("Invalid layout selected!");

				// Set the current layout based on the one currently used
                l.setInitializer(vv_.getGraphLayout());
                l.setSize(vv_.getSize());
				
                // Start the transition
				LayoutTransition<Node, Edge> lt = new LayoutTransition<Node,Edge>(vv_, vv_.getGraphLayout(), l);
				Animator animator = new Animator(lt);
				animator.start();
				vv_.getRenderContext().getMultiLayerTransformer().setToIdentity();
				vv_.repaint();
				layout_ = l; // Save the new layout reference
				
			} catch (Exception e) {
				log_.log(Level.WARNING, "NetworkGraph::changeGraphLayout() " + e.getMessage(), e);
			}
		}

		
		// ----------------------------------------------------------------------------
		
		/**
		 * Called when the user check the box "Distinguish signed interactions by arrow head"
		 */
		public void distinguishConnectionByArrowHead() {
			
			if (vv_ == null) return;
			
			if (distinguishByArrowHead_.isSelected()) {
				arrowTransformer_.distinguishConnectionByArrowHead(true);
				arrowTransformer_.setDualArrow(ArrowShapeTransformer.sphericalArraw_);
				insideArrowColorTransformer_.setUnknownArrow(Color.WHITE);
				
			}
			else {
				arrowTransformer_.distinguishConnectionByArrowHead(false);
				insideArrowColorTransformer_.setUnknownArrow(edgeArrowColorTransformer_.getUnknownArrow());
			}
			
			vv_.repaint();
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Called when the field of the search box is updated.
		 */
		public void processVerticesLabelSearch() {
			try {
				control_.getSearch().search();
			} catch (Exception e) {
				log_.log(Level.WARNING, "NetworkGraph::processVerticesLabelSearch(): " + e.getMessage(), e);
			}
			highlightVertexesSelection(control_.getSearch().getResult());
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Change the way in which the user can interact with the graphs.
		 */
		public void interactionMode() {
			if (interactionMode_.getSelectedIndex() == 0) { // Transforming mode
				setGraphMouseMode(0);
			} else if (interactionMode_.getSelectedIndex() == 1) { // Picking mode
				setGraphMouseMode(1);
			}
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Display or hide the labels of the vertices.
		 */
		public void displayLabel() {

        	if (vv_ == null) return;
        	
        	if (displayLabels_.isSelected()) {
        		vv_.getRenderContext().setVertexLabelTransformer(vs_);
        	} else {
        		vv_.getRenderContext().setVertexLabelTransformer(vs_none_);
        	}
        	vv_.repaint();
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Print the graph into 3 different format modifiable through the controller:
		 * EPS, PNG and JPEG
		 */
		public void printGraph() {
			
			String title = "Save As";
	    	
	        final JFileChooser fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			fc.addChoosableFileFilter(new FilterImagePNG());
			fc.addChoosableFileFilter(new FilterImageJPEG());
			fc.addChoosableFileFilter(new FilterImageEPS());
			fc.setSelectedFile(new File(item_.getLabel())); // filename proposition
	        
	        // Center the file chooser dialog in the center of the JPanel that displays the JUNG visualization.
	        int returnVal = fc.showDialog(NetworkGraph.this.screen_, title);
	    	
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            FileFilter filter = fc.getFileFilter();
	            
	            if (filter instanceof  FilterImagePNG) {
	            	String[] ext = {"png"};
	            	file = FilenameUtilities.addExtension(file, ext);
					if (FilenameUtilities.writeOrAbort(file.getAbsolutePath(), new JFrame())) {
						saveComponentAsPNG(file, vv_);
					}
	            }
	            else if (filter instanceof  FilterImageJPEG) {
	            	String[] ext = {"jpg", "jpeg"};
	            	file = FilenameUtilities.addExtension(file, ext);
					if (FilenameUtilities.writeOrAbort(file.getAbsolutePath(), new JFrame())) {
						saveComponentAsJPEG(file, vv_);
					}
	            }
	            else if (filter instanceof  FilterImageEPS) {
	            	String[] ext = {"eps"};
	            	file = FilenameUtilities.addExtension(file, ext);
					if (FilenameUtilities.writeOrAbort(file.getAbsolutePath(), new JFrame())) {
						saveComponentAsEPS(file, vv_);
					}
	            }
	            else
	            	return;
	        }
		}
		
		
		// ----------------------------------------------------------------------------

		/**
		 * Save a given Component as EPS image (uses EPSDump).
		 * @param file A file
		 * @param c The component to print.
		 */
		public void saveComponentAsEPS(File file, Component c) {
            EPSDump dumper = new EPSDump(false);
            
            // Little subtlety for EPS images
            // As EPS doesn't support transparency, the signature - actually font on a
            // transparency background - is replace by a signature with a background painted,
            // e.g. in white. As the signature is added as POST render, the misplaced part of
            // the graph will be erase by the signature rectangle.
            // After the export, the previous signature with transparency is reinitiated.
            signature_.changeSignature(new ImageIcon(GnwGuiSettings.getInstance().getGnwWatermarkNoTransparencyImage()));
//            dumper.textVector = true;
            try { dumper.dumpComponent(file, vv_); }
            catch (IOException e) {
            	log_.log(Level.WARNING, "NetworkGraph::saveComponentAsEPS(): " + e.getMessage(), e);
            }
            signature_.changeSignature(new ImageIcon(GnwGuiSettings.getInstance().getGnwWatermarkImage()));
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Save a given Component as PNG image (uses PNGDump).
		 * @param file A file
		 * @param c The component to print.
		 */
		public void saveComponentAsPNG(File file, Component c) {
			PNGDump dumper = new PNGDump();
            try { dumper.dumpComponent(file, vv_); }
            catch (IOException e) {
            	log_.log(Level.WARNING, "NetworkGraph::saveComponentAsPNG(): " + e.getMessage(), e);
            }
		}
		
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Save a given Component as JPEG image (uses).
		 * @param file A file
		 * @param c The component to print.
		 */
		public void saveComponentAsJPEG(File file, Component c) {
				BufferedImage myImage = new BufferedImage(c.getWidth(), c.getHeight(),
				        BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = myImage.createGraphics();
				c.paint(g2);
				try {
					OutputStream out = new FileOutputStream(file.getAbsolutePath());
					JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
					encoder.encode(myImage);
					out.close();
				} catch (Exception e) {
					log_.log(Level.WARNING, "NetworkGraph::saveComponentAsJPEG(): " + e.getMessage(), e);
				}
		}
		
		
        // =======================================================================================
        // GETTERS AND SETTERS
        //
		
		public String[] getDataVertices() { return dataVertices_; }
	}
}