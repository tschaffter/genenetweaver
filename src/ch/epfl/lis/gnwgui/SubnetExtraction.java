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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.SubnetExtractor;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.windows.SubnetExtractionWindow;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.Structure;


/** This dialog allows to extract subnetworks from a source network.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class SubnetExtraction extends SubnetExtractionWindow {
	
	/** Serialization */
	private static final long serialVersionUID = 1L;

	/** Reference to the father network used to generate subnet(s). */
	private NetworkElement item_ = null;
	
	/** Document associated to the text field subnetRootName_ */
	private Document subnetRootNameDocument_;
	
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(SubnetExtraction.class.getName());


	// ============================================================================
	// PUBLIC METHODS
    
	/**
	 * Constructor
	 */
	public SubnetExtraction(Frame aFrame, NetworkElement item) {
		
		super(aFrame);
		item_ = item;
		
		// Set model of "subnet size" spinner
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setStepSize(1);
		int maxSize = (item instanceof StructureElement) ?
				((StructureElement) item).getNetwork().getSize() :
				((DynamicalModelElement) item).getGeneNetwork().getSize();
		model.setMaximum(maxSize);
		int effectiveSize = (10 <= maxSize) ? 10 : maxSize;
		model.setValue(effectiveSize);
		subnetSize_.setModel(model);
		
		// Set model of "number of subnets" spinner
		model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setMaximum(20);
		model.setStepSize(1);
		model.setValue(10);
		numberSubnets_.setModel(model);
		
		// Set model of "random among top" spinner
		model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setMaximum(100);
		model.setStepSize(1);
		model.setValue(20);
		randomAmongTopSpinner_.setModel(model);
		
		// Set model of "From strongly connected components"
		model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setMaximum(maxSize);
		model.setStepSize(1);
		model.setValue(10);
		numStronglyConnected_.setModel(model);
		
		// Set model of "From strongly connected components"
		model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setMaximum((Integer) subnetSize_.getModel().getValue());
		model.setStepSize(1);
		model.setValue(10);
		numRegulators_.setModel(model);
		
		// add tooltips for all elements of the window
		addTooltips();
		
		String title1, title2;
		title1 = title2 = "";
		if (item_ instanceof StructureElement) {
			ImodNetwork network = ((StructureElement)item_).getNetwork();
			title1 = item_.getLabel();
			title2 = network.getSize() + " nodes, " + network.getNumEdges() + " edges";
		} else if (item_ instanceof DynamicalModelElement) {
			GeneNetwork geneNetwork = ((DynamicalModelElement)item_).getGeneNetwork();
			title1 = item_.getLabel();
			title2 = geneNetwork.getSize() + " genes, " + geneNetwork.getNumEdges() + " interactions";
		}
		setHeaderInfo(title1 + " (" + title2 + ")");
		
		
		/**
		 * ACTIONS
		 */
		subnetRootNameDocument_ = subnetRootName_.getDocument();
		subnetRootNameDocument_.addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {
				runButton_.setEnabled(!subnetRootName_.getText().equals(""));
			}
			public void insertUpdate(DocumentEvent arg0) {
				runButton_.setEnabled(!subnetRootName_.getText().equals(""));
			}
			public void removeUpdate(DocumentEvent arg0) {
				runButton_.setEnabled(!subnetRootName_.getText().equals(""));
			}
		});
		
		
		extractAllRegulators_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				disableExtractionOptions();
			}
		});
		
		
		
		randomVertex_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				updateSeedControls();
			}
		});
		
		selectionFromList_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				updateSeedControls();
			}
		});
		
		greedy_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				updateNeighborSelectionControls();
			}
		});
		
		randomAmongTop_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				updateNeighborSelectionControls();
			}
		});
		
		stronglyConnected_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				updateSeedControls();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				GnwSettings.getInstance().stopSubnetExtraction(true);
				escapeAction();
			}
		});
		
		runButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				run();
			}
		});
		
		subnetSize_.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateRegulatorsOptions();
			}
		});
		
		
		numRegulators_.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateRegulatorsOptions();
			}
		});
		
		useNumRegulators_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				numRegulators_.setEnabled(useNumRegulators_.isSelected());
			}
		});
		
		
//		useNumRegulators_.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent arg0) {
//				numRegulators_.setEnabled(useNumRegulators_.isSelected());
//			}
//		});
		
		subnetIdProposal();
		setListVerticesID();
		updateSeedControls();
		updateNeighborSelectionControls();
		
		useNumRegulators_.setSelected(false);
		numRegulators_.setEnabled(false);
	}
	
	
	public void updateRegulatorsOptions() {
		
		Integer value = (Integer) numRegulators_.getModel().getValue();
		if ((Integer) numRegulators_.getModel().getValue() > (Integer) subnetSize_.getModel().getValue())
			value = (Integer) subnetSize_.getModel().getValue();
			
		SpinnerNumberModel model = (SpinnerNumberModel) numRegulators_.getModel();
		model.setMinimum(1);
		model.setMaximum((Integer) subnetSize_.getModel().getValue());
		model.setStepSize(1);
		model.setValue(value);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This function defines a root name for all the subnetworks.
	 */
	public void subnetIdProposal() {
		subnetRootName_.setText(/*"sub-" + */item_.getLabel());
		subnetRootName_.selectAll();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Run the extraction process when the user type ENTER.
	 */
	public void enterAction() {
		run();
	}
	
	
	public void disableExtractionOptions() {
		
		boolean b = extractAllRegulators_.isSelected();
	
		
		subnetSize_.setEnabled(!b);
		numberSubnets_.setEnabled(!b);
		
		useNumRegulators_.setEnabled(!b);
		numRegulators_.setEnabled(useNumRegulators_.isSelected() && !b);

		randomVertex_.setEnabled(!b);
		selectionFromList_.setEnabled(!b);
		listVerticesID_.setEnabled(selectionFromList_.isSelected() && !b);
		stronglyConnected_.setEnabled(!b);
		numStronglyConnected_.setEnabled(stronglyConnected_.isSelected() && !b);
		
		greedy_.setEnabled(!b);
		randomAmongTop_.setEnabled(!b);
		randomAmongTopSpinner_.setEnabled(randomAmongTop_.isSelected() && !b);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set a Combobox filled with all the name of the node of the network
	 * alphabetically sorted.
	 */
	public void setListVerticesID() {
		ArrayList<String> list = null;
		String[] listStr = null;
		if (item_ instanceof StructureElement)
			list = ((StructureElement) item_).getNetwork().getAllNodesLabels();
		else if (item_ instanceof DynamicalModelElement)
			list = ((DynamicalModelElement)item_).getGeneNetwork().getAllNodesLabels();
		
		if (list == null || list.size() == 0)
			return;
		
		listStr = new String[list.size()];
		for (int i=0; i < listStr.length; i++)
			listStr[i] = list.get(i);
		
		// Sort the vertices ids in alphabetical order.
		Arrays.sort(listStr);
		listVerticesID_.setModel(new DefaultComboBoxModel<String>(listStr));
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Following the seed selection strategy selected, some components of the dialog
	 * are updated (mainly enable/disable these components).
	 */
	public void updateSeedControls() {
		if (randomVertex_.isSelected()) {
			listVerticesID_.setEnabled(false);
			numStronglyConnected_.setEnabled(false);
		}
		else if (selectionFromList_.isSelected()) {
			listVerticesID_.setEnabled(true);
			numStronglyConnected_.setEnabled(false);
		}
		else if (stronglyConnected_.isSelected()) {
			numStronglyConnected_.setEnabled(true);
			listVerticesID_.setEnabled(false);
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Following the neighbors selection strategy selected, some components of the dialog
	 * are updated (mainly enable/disable these components).
	 */
	public void updateNeighborSelectionControls() {
		if (greedy_.isSelected()) {
			randomAmongTopSpinner_.setEnabled(false);
		}
		else if (randomAmongTop_.isSelected()) {
			randomAmongTopSpinner_.setEnabled(true);
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Run the subnets extraction process using the user-defined parameters.
	 */
	public void run() {
		
		GnwSettings uni = GnwSettings.getInstance();
		int poolSize = (Integer)numberSubnets_.getModel().getValue();
		int subnetSize = (Integer)subnetSize_.getModel().getValue();
		Structure network = null;
		
		if (item_ instanceof StructureElement)
			network = ((StructureElement)item_).getNetwork();
		else if (item_ instanceof DynamicalModelElement)
			network = ((DynamicalModelElement)item_).getGeneNetwork();
		
		// Start by building the seed(s)
		ArrayList<ArrayList<Node>> seeds = null;
		ArrayList<Node> miniSeed = null;
		
		// DANIEL
		uni.setNumSeedsFromStronglyConnectedComponents(0);
		
		// Seed = vertex selected in the list
		if (selectionFromList_.isSelected()) {
			seeds = new ArrayList<ArrayList<Node>>();
			miniSeed = new ArrayList<Node>();
			miniSeed.add(network.getNode((String)listVerticesID_.getModel().getSelectedItem()));
			for (int i=0; i < poolSize; i++) {
				seeds.add(miniSeed);
			}
		} else if (stronglyConnected_.isSelected()) {
			int numStronglyConnected = (Integer) numStronglyConnected_.getModel().getValue();
			uni.setNumSeedsFromStronglyConnectedComponents(numStronglyConnected);
		}
				
		if (useNumRegulators_.isSelected()) {
			int numRegulators = (Integer) numRegulators_.getModel().getValue();
			uni.setNumRegulators(numRegulators);
		}
		
		// Set now the selection option
		if (greedy_.isSelected())
			uni.setTruncatedSelectionFraction(0);
		else if (randomAmongTop_.isSelected()) {
			double value = ((Integer)randomAmongTopSpinner_.getValue()) / 100.;
			uni.setTruncatedSelectionFraction(value);
		}

		boolean extractAllRegulators = extractAllRegulators_.isSelected();
		NetworkExtractionThread generator = new NetworkExtractionThread(network, seeds, subnetSize, poolSize, extractAllRegulators);
		uni.stopSubnetExtraction(false); // reset
		generator.start();
	}
	
	
	// ----------------------------------------------------------------------------

	/** Add tooltips for all elements of the window */
	private void addTooltips() {
		
		extractAllRegulators_.setToolTipText(
				"<html>Extract all regulators of the network, i.e., all nodes that have at least<br>" +
				"one outgoing link <i>in the source network</i>. E.g., if the source network is<br>" +
				"<i>Ecoli</i>, the extracted subnetwork would consist of all <i>E.coli</i> transcription factors.</html>");
		useNumRegulators_.setToolTipText(
				"<html>Select to specify the minimum number of regulators (nodes with at least one outgoing<br>" +
				"link <i>in the source network</i>) that should be included in the extracted subnetworks</html>");
		numRegulators_.setToolTipText(
				"<html>The minimum number of regulators (nodes with at least one outgoing link<br>" +
				"<i>in the source network</i>) to be included in the extracted subnetworks</html>");
		randomVertex_.setToolTipText(
				"<html>Randomly choose a seed node to<br>" +
				"start subnetwork extraction</html>");
		selectionFromList_.setToolTipText(
				"<html>Manually select a seed node to<br>" +
				"start subnetwork extraction</html>");
		greedy_.setToolTipText(
				"<html>When growing the subnetwork, always add<br>" +
				"nodes that lead to the highest modularity <i>Q</i></html>");
		randomAmongTop_.setToolTipText(
				"<html>When growing the subnetwork, add nodes from the top <i>k</i> percent of highest modularity <i>Q</i>.<br>" +
				"Set to 100% for random subnetwork extraction (add random neighboring nodes). Setting this<br>" +
				"parameter to 0% is equivalent to selecting the greedy strategy above.</html>");
		stronglyConnected_.setToolTipText(
				"<html>Add the specified number of nodes from the largest strongly connected component of the<br>" +
				"graph as seeds. <b>Warning</b>: this works fine if the network has a single strongly connected component,<br>" +
				"as <i>Yeast</i> does. However, if there are several stongly connected components, the smaller ones will<br>" +
				"never be sampled (to be corrected in the next version).</html>");
		numStronglyConnected_.setToolTipText(
				"<html>The number of nodes to be added as seed nodes<br>" +
				"from the largest strongly connected component</html>");
		subnetRootName_.setToolTipText(
				"<html>Specify a name for the subnetwork. If several subnetworks<br>" +
				"are being extracted, they will be named as follows:<br>" +
				"- <i>name</i>-1<br>" +
				"- <i>name</i>-2<br>" +
				"- etc.</html>");
		subnetSize_.setToolTipText(
				"<html>Extract subnetworks of the given size</html>");
		numberSubnets_.setToolTipText(
				"<html>Number of subnetworks to be extracted</html>");
		listVerticesID_.setToolTipText(
				"<html>Select a seed node to<br>" +
				"start subnetwork extraction</html>");
		randomAmongTopSpinner_.setToolTipText(
				"<html>When growing the subnetwork, add nodes from the top <i>k</i> percent of highest modularity <i>Q</i>.<br>" +
				"Set to 100% for random subnetwork extraction (add random neighboring nodes). Setting this<br>" +
				"parameter to 0% is equivalent to selecting the greedy strategy above.</html>");
		runButton_.setToolTipText(
				"<html>Set parameters to the given values<br>" +
				"and start subnetwork extraction</html>");
		cancelButton.setToolTipText(
				"<html>Abort (the thread may finish the<br>" +
				"current subnetwork before it exits)</html>");
		
		// tooltips disappear only after 10s
		ToolTipManager.sharedInstance().setDismissDelay(10000);
		
	}
	
	
	
	
	
	/** Implements a thread to run the subnetwork extractions.
	 * 
	 * @author Thomas Schaffter (firstname.name@gmail.com)
	 *
	 */
	public class NetworkExtractionThread implements Runnable {

		/** Main Thread */
		private Thread myThread_;
		/** Given network from which subnets will be extracted. */
		private Structure network_ = null;
		/** List of computed subnets. */
		private Structure[] output_ = null;
		/** Specifies the different seeds used for each subnet extractions. */
		private ArrayList<ArrayList<Node>> seeds_ = null;
		/** Size N of each subnet generated. */
		private int size_;
		/** Number of subnets to be generated */
		private int numSubnets_;
		/** Set true to extract all regulators of the network */
		boolean extractAllRegulators_;
		/** Subnets extractor */
		private SubnetExtractor extractor_ = null;
		
		// ============================================================================
		// PUBLIC METHODS
		
		/**
		 * Constructor
		 */
		public NetworkExtractionThread(Structure network, ArrayList<ArrayList<Node>> seeds, int size, int numSubnets, boolean extractAllRegulators) {
			super();
			myThread_ = null;
			network_ = network;
			seeds_ = seeds;
			size_ = size;
			numSubnets_ = numSubnets;
			extractAllRegulators_ = extractAllRegulators;
		}
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Start function
		 */
		public void start() {
			// If myThread_ is null, we start it!
			if (myThread_ == null) {
				myThread_ = new Thread(this);
				myThread_.start();
			}
		}
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Stop function
		 */
		public void stop() {
			myThread_ = null;
		}
		
		// ----------------------------------------------------------------------------
		
		/**
		 * Run function
		 */
		public void run()
		{
         	 try
         	 {
         		 if (network_.getSize() == size_)
         		 {
         			 String msg = "Subnetwork size must be smaller than original network size !";
         			 JOptionPane.showMessageDialog(new Frame(), msg, "GNW message", JOptionPane.INFORMATION_MESSAGE);
         			 log_.log(Level.INFO, msg);
         			 
         			 return;
         		 }
         		 
         		 
             	// Display the waiting item during the process instead of the compute button
             	 snake_.start();
             	 myCardLayout.show(cards, snakePanel_.getName());
         		 
         		 extractor_ = new SubnetExtractor((ImodNetwork)network_);
         		 
         		 if (extractAllRegulators_)
         		 {
         			 output_ = new Structure[1];
         			 output_[0] = extractor_.extractRegulators();
         		 }
         		 else if (seeds_ == null)
         			 output_ = extractor_.runExtraction(subnetRootName_.getText(), size_, numSubnets_);
         		 else
         			output_ = extractor_.runExtraction(subnetRootName_.getText(), size_, seeds_);
         		 
         		 if (output_ != null)
         			 done();
         		 else
         			 log_.info("Subnetwork extraction canceled !");
         		 
         	 } catch (OutOfMemoryError e) {
         		 JOptionPane.showMessageDialog(new Frame(), "Out of memory, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
         		 log_.warning("There is not enough memory available to run this program.\n" +
					"Quit one or more programs, and then try again.\n" +
					"If enough amounts of RAM are installed on this computer, try to run the program " +
					"with the command-line argument -Xmx1024m to use maximum 1024Mb of memory, " +
					"-Xmx2048m to use max 2048Mb, etc.");
			
			} catch (RuntimeException e)
			{
				JOptionPane.showMessageDialog(new Frame(), "Runtime exception, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
				log_.log(Level.WARNING, e.getMessage(), e);
				
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(new Frame(), "Error encountered, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
				log_.log(Level.WARNING, e.getMessage(), e);
				//log.warning(e.getMessage());
			}
		}
		
		// ----------------------------------------------------------------------------
		
		/**
		 * This function is called at this end of the function run().
		 */
	    public void done() {
    	
	        GnwGuiSettings global = GnwGuiSettings.getInstance();
	        
	        // For each subnet found, a structure or dynamical model item is created
	        // and place on the desktop
	        for (int i=0; i < output_.length; i++) {
	        	if (output_[i] instanceof ImodNetwork) {
	            	StructureElement structure = new StructureElement(output_[i].getId(), global.getNetworkDesktop());
	            	structure.setNetwork((ImodNetwork)output_[i]);
	            	structure.setFather(item_);
	            	item_.addChild(structure);
	        	} else if (output_[i] instanceof GeneNetwork) {
	        		DynamicalModelElement dynamicNetwork = new DynamicalModelElement(output_[i].getId(), global.getNetworkDesktop());
	        		dynamicNetwork.setGeneNetwork((GeneNetwork)output_[i]);
	        		dynamicNetwork.setFather(item_);
	        		item_.addChild(dynamicNetwork);
	        	}
	        }
	
	        // Display all the subnets found on the desktop as children of the mother network
	        // used to generate them.
	        global.getNetworkDesktop().displayChildrenOf(item_);
			snake_.stop();
			myCardLayout.show(cards, runPanel_.getName());
			
			// close the window
			 SubnetExtraction.this.escapeAction();
	
			// If some sunet sizes < size_, the user should be informed.
//			if (extractor_.getUnderSized()) {
//				JOptionPane.showMessageDialog(global.getGnwGui().getFrame(), "<html>At least one network generated is under-sized, " +
//						"<br>i.e. extraction of a module not linked with the rest of the network.</html>");
//			}
			
			log_.log(Level.INFO, "Done!");
	    }
	}
}
