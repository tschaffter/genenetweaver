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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.idesktop.IFolder;
import ch.epfl.lis.gnwgui.windows.OptionsWindow;
import ch.epfl.lis.gnwgui.windows.Wait;
import ch.epfl.lis.imod.ImodNetwork;

/**
 * Option menu for the elements on the desktop
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Options extends OptionsWindow
{	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** NetworkItem related to this option box. */
	private IElement item_ = null;
	
    /** Logger for this class */
	private static Logger log_ = Logger.getLogger(Options.class.getName());
	
	public static final Byte STATIC_NETWORK_MENU = 0;
	public static final Byte DYNAMICAL_NETWORK_MENU = 1;
	public static final Byte FOLDER_MENU = 2;
	public static final Byte MULTIPLE_SELECTION_MENU = 3;
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS

	public Options(Frame aFrame, IElement item)
	{
		super(aFrame);
		item_ = item;
		
		setHeaderContent();
		setActions();
	}
	
	public void setHeaderContent()
	{
		this.setTitle("Select a task");
		
		if (item_ instanceof StructureElement)
		{
			ImodNetwork network = ((StructureElement) item_).getNetwork();
			
			setHeaderTitle(item_.getLabel());
			setHeaderInfo(network.getSize() + " nodes, " + network.getNumEdges() + " edges");
			setMenu(Options.STATIC_NETWORK_MENU);
		}
		else if (item_ instanceof DynamicalModelElement)
		{
			GeneNetwork geneNetwork = ((DynamicalModelElement) item_).getGeneNetwork();
			
			setHeaderTitle(item_.getLabel());
			setHeaderInfo(geneNetwork.getSize() + " genes, " + geneNetwork.getNumEdges() + " interactions");
			setMenu(Options.DYNAMICAL_NETWORK_MENU);
		}
		else if (item_ instanceof Folder)
		{
			setHeaderTitle(item_.getLabel());
			setHeaderInfo(IFolder.getNumAllChildren(item_) + " items");
			setMenu(Options.FOLDER_MENU);
		}
		else if (item_ instanceof Multi)
		{
			setHeaderTitle("Multiple selection");
			setHeaderInfo(((Multi) item_).getNumElementsSelected() + " items");
			setMenu(Options.MULTIPLE_SELECTION_MENU);
		}
	}
	
	public Options(Frame aFrame)
	{
		super(aFrame);
		setActions();
	}
	
	
	private void setActions()
	{
		bAnonymize_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				dispose();
				GnwGuiSettings global = GnwGuiSettings.getInstance();
				global.getNetworkDesktop().displayAnonymizeNetworkDialog();
			}
		});
		
		bView_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				dispose();
				viewNetwork((NetworkElement) item_);
			}
		});
		
		bExport_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent arg0)
			{
				if (item_ instanceof NetworkElement)
					exportNetwork();
				else if (item_ instanceof Folder || item_ instanceof Multi)
				{
					dispose();
					((NetworkDesktop) GnwGuiSettings.getInstance().getNetworkDesktop()).displaySaveDialog();
				}
			}
		});
		
		bKinetic_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent arg0)
			{
				dispose();
				((NetworkDesktop) GnwGuiSettings.getInstance().getNetworkDesktop()).displayKineticModelGenerationDialog();
			}
		});
		
		bExtract_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent arg0)
			{
				dispose();
				subnetworkExtraction((NetworkElement) item_);
			}
		});
		
		bDelete_.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent e)
			{
				//CLOSE WINDOW
				dispose();
				
				//DISPLAY DELETE WINDOW
				if (item_ instanceof NetworkElement || item_ instanceof Folder || item_ instanceof Multi)
				{
					GnwGuiSettings global = GnwGuiSettings.getInstance();
					Delete dd = new Delete(global.getGnwGui().getFrame());
					dd.setVisible(true);
				}
			}
		});
		
		bRename_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent arg0)
			{
				dispose();
				GnwGuiSettings global = GnwGuiSettings.getInstance();
				global.getNetworkDesktop().displayRenameDialog();
			}
		});
		
		bDatasets_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent arg0)
			{
				try {
					dispose();
					generateDREAM3GoldStandard((NetworkElement) item_);
					
				} catch (Exception e) {
					log_.log(Level.WARNING, "Options::Options(): " + e.getMessage(), e);
				}
			}
		});
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Convert a structure into a dynamical model. The item of the structure on the
	 * desktop is also transformed into a dynamical model item.
	 */
	public void convertToDynamicalModel()
	{
		JOptionPane optionPane = new JOptionPane();
		Object msg[] = {"Do you want to remove autoregulatory interactions ?"};
		optionPane.setMessage(msg);
		optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		optionPane.setOptionType(JOptionPane.YES_NO_CANCEL_OPTION);
		JDialog dialog = optionPane.createDialog(this, "Dynamical model");
		dialog.setVisible(true);
		Object value = optionPane.getValue();
	   
		if (value == null || !(value instanceof Integer))
			return;
		
		int i = ((Integer)value).intValue();
		if (i == JOptionPane.CLOSED_OPTION || i == JOptionPane.CANCEL_OPTION)
		{
			return;
		}
		else if (i == JOptionPane.OK_OPTION)
		{
			// Close the options dialog
			enterAction();
			
			if (item_ instanceof StructureElement)
			{
				((StructureElement) item_).getNetwork().removeAutoregulatoryInteractions();
				log_.info("Removing autoregulatory interactions from network " + ((StructureElement) item_).getNetwork().getId());
			}
			else if (item_ instanceof DynamicalModelElement)
			{
				((DynamicalModelElement) item_).getGeneNetwork().removeAutoregulatoryInteractions();
				log_.info("Removing autoregulatory interactions from network " + ((DynamicalModelElement) item_).getGeneNetwork().getId());
			}
		}
		else if (i == JOptionPane.NO_OPTION)
			enterAction();
		
		Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
		wait.setTitle("Kinetic model");
		KineticModelGeneration kmg = new KineticModelGeneration(wait);
		kmg.element_ = (NetworkElement) item_;
		
		kmg.execute();
		// MUST BE CALLED AFTER EXECUTE
		if (kmg.displayWaitingBox())
			wait.start();
	}
	
	// ----------------------------------------------------------------------------

	/** Generate dynamical network models for a batch of networks */
	public static void convertAllToDynamicalModels()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		//Get all top selected elements
		ArrayList<IElement> topElements = global.getNetworkDesktop().getTopSelectedItems();
		
		boolean removeAutoregulatory = false;
		
		JOptionPane optionPane = new JOptionPane();
		Object msg[] = {"Do you want to remove autoregulatory interactions ?"};
		optionPane.setMessage(msg);
		optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		optionPane.setOptionType(JOptionPane.YES_NO_CANCEL_OPTION);
		JDialog dialog = optionPane.createDialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Dynamical model");
		dialog.setVisible(true);
		Object value = optionPane.getValue();
	   
		if (value == null || !(value instanceof Integer))
			return;
		
		int i = ((Integer)value).intValue();
		if (i == JOptionPane.CLOSED_OPTION || i == JOptionPane.CANCEL_OPTION)
		{
			return;
		}

		removeAutoregulatory = (i == JOptionPane.OK_OPTION);
		
		try
		{
			for(IElement itemIE : topElements)
				recursiveConversionToDynamicalModel(itemIE, removeAutoregulatory);
		}
		catch (Exception e)
		{
			log_.log(Level.WARNING, "Options::convertAllToDynamicalModels(): " + e.getMessage(), e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public static void recursiveConversionToDynamicalModel(IElement item, boolean removeAutoregulatory)
	{
		
		if (item.getClass() == Folder.class)
		{
			// Do nothing
		}
		else
		{
			if (removeAutoregulatory)
			{
				if (item instanceof StructureElement)
				{
					((StructureElement) item).getNetwork().removeAutoregulatoryInteractions();
					log_.info("Removing autoregulatory interactions from network " + ((StructureElement) item).getNetwork().getId());
				}
				else if (item instanceof DynamicalModelElement)
				{
					((DynamicalModelElement) item).getGeneNetwork().removeAutoregulatoryInteractions();
					log_.info("Removing autoregulatory interactions from network " + ((DynamicalModelElement) item).getGeneNetwork().getId());
				}
			}
			
			Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
			wait.setTitle("Kinetic model");
			KineticModelGeneration kmg = new KineticModelGeneration(wait);
			kmg.element_ = (NetworkElement) item;
			
			kmg.execute();
			// MUST BE CALLED AFTER EXECUTE
			if (kmg.displayWaitingBox())
				wait.start();
		}
		
		// We do not want the children of a network having their dynamical model generated
		if (item.hasChildren() && (item.getClass() != StructureElement.class || item.getClass() != DynamicalModelElement.class))
		{
			for(IElement e : item.getChildren())
				recursiveConversionToDynamicalModel(e, removeAutoregulatory);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open a dialog to generate benchmarks.
	 * @throws Exception
	 */
	public static void generateDREAM3GoldStandard(NetworkElement item) throws Exception
	{
		Simulation rd = new Simulation(new Frame(), item);
		rd.setVisible(true);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open a dialog to visualize the network graph.
	 * @param item
	 */
	public static void viewNetwork(NetworkElement item)
	{
		
		int numNodes = 0;
		
		if (item instanceof StructureElement)
			numNodes = ((StructureElement)item).getNetwork().getSize();
		else if (item instanceof DynamicalModelElement)
			numNodes = ((DynamicalModelElement)item).getGeneNetwork().getSize();
		
		if (numNodes >= 200)
		{
			String msg = "Large networks (> 200 nodes) can take some time to display. Continue ?";
			
			int n = JOptionPane.showConfirmDialog(
					new Frame(),
					msg.toString(),
				    "GNW message",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.QUESTION_MESSAGE);

			if (n == JOptionPane.NO_OPTION)
				return;
		}
		
		GraphViewer dialog = new GraphViewer(new Frame(), item);
		dialog.setVisible(true);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Open a dialog to extract one or several subnetworks.
	 */
	public static void subnetworkExtraction(NetworkElement item)
	{
		SubnetExtraction dialog = new SubnetExtraction(new Frame(), item);
//		escapeAction();
		dialog.setVisible(true);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Open a dialog to export the present network. */
	public void exportNetwork()
	{
		escapeAction();
		IONetwork.saveAs((NetworkElement) item_);
	}
	
	// ----------------------------------------------------------------------------
	
	public void setMenu(Byte menu)
	{
		topPanel_.removeAll();
		bottomPanel_.removeAll();
		
		if (menu == STATIC_NETWORK_MENU)
		{
			topPanel_.add(bRename_);
			topPanel_.add(bView_);
			topPanel_.add(bAnonymize_);
			topPanel_.add(bExtract_);
			bottomPanel_.add(bKinetic_);
			bottomPanel_.add(bDelete_);
			bottomPanel_.add(bExport_);
		}
		else if (menu == DYNAMICAL_NETWORK_MENU)
		{
			topPanel_.add(bRename_);
			topPanel_.add(bView_);
			topPanel_.add(bAnonymize_);
			topPanel_.add(bExtract_);
			bottomPanel_.add(bKinetic_);
			bottomPanel_.add(bDatasets_);
			bottomPanel_.add(bDelete_);
			bottomPanel_.add(bExport_);
		}
		else if (menu == FOLDER_MENU)
		{
			topPanel_.add(bRename_);
			topPanel_.add(bAnonymize_);
			bottomPanel_.add(bKinetic_);
			bottomPanel_.add(bDelete_);
			bottomPanel_.add(bExport_);
		}
		else if (menu == MULTIPLE_SELECTION_MENU)
		{
			topPanel_.add(bRename_);
			topPanel_.add(bAnonymize_);
			bottomPanel_.add(bKinetic_);
			bottomPanel_.add(bDelete_);
			bottomPanel_.add(bExport_);
		}
		else
			log_.log(Level.WARNING, "Options::setMenu(): Invalid menu");
		
		topPanel_.revalidate();
		bottomPanel_.revalidate();
		
		topPanel_.repaint();
		bottomPanel_.repaint();
	}
	
	
	// ----------------------------------------------------------------------------
	// PRIVATE CLASSES
	
	/**
	 * This class allow to generate a dynamical gene network model in a new thread.
	 * 
	 * @author Thomas Schaffter (firstname.name@gmail.com)
	 */
	private static class KineticModelGeneration extends SwingWorker<Void, Void>
	{
		/** Dialog displayed during the process */
		private Wait wDialog_;
		/** Do not display waiting box for network size < 200 */
		private int wDialogMinNetworkSize_ = 1000;
		
		/** Element to export */
		private NetworkElement element_;
		
		// ----------------------------------------------------------------------------
		// PUBLIC METHODS
	  
		public KineticModelGeneration(Wait gui)
		{
			this.wDialog_ = gui;
		}
		
		// ----------------------------------------------------------------------------
		// PROTECTED METHODS
		
		protected boolean displayWaitingBox()
		{
			int N = 0;
			if (element_ instanceof StructureElement)
				N = ((StructureElement) element_).getNetwork().getSize();
			else if (element_ instanceof DynamicalModelElement)
				N = ((DynamicalModelElement) element_).getGeneNetwork().getSize();
			else
				log_.log(Level.WARNING, "IONetwork::displayWaitingBox(): Unknown item type");
			
			return (N > wDialogMinNetworkSize_);
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected Void doInBackground() throws Exception
		{
			GnwGuiSettings global = GnwGuiSettings.getInstance();
			if (element_ instanceof StructureElement)
			{
				StructureElement staticNetwork = (StructureElement) element_;
				
				// Create a new dynamic network from a static one and initialize its parameters.
				DynamicalModelElement grnItem = new DynamicalModelElement(staticNetwork);
				grnItem.getGeneNetwork().randomInitialization();
				
				if (element_.hasChildren())
				{
					for (IElement e : element_.getChildren())
						grnItem.addChild(e);
				}
				
				// Delete the structure item and replace it by a new dynamical
				// network item on the desktop
				if (element_.getFather() != null) {
					int index = staticNetwork.getFather().getChildren().indexOf(element_);
					staticNetwork.getFather().getChildren().remove(element_);
					staticNetwork.getFather().getChildren().add(index, grnItem);
				}
				
				global.getNetworkDesktop().replaceItem(staticNetwork, grnItem);
			}
			else if (element_ instanceof DynamicalModelElement)
			{
				DynamicalModelElement grn = (DynamicalModelElement) element_;
				grn.getGeneNetwork().randomInitialization();
				grn.networkViewer_ = null; // delete the viewer to regenerate it next time
			}	
			
			return null;
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected void done()
		{
			wDialog_.stop();
		}
	}
}
