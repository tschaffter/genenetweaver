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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnw.GraphUtilities;
import ch.epfl.lis.gnw.evaluation.MotifDefinitions;
import ch.epfl.lis.gnw.evaluation.MotifPrediction;
import ch.epfl.lis.gnwgui.idesktop.IBin;
import ch.epfl.lis.gnwgui.idesktop.IDesktop;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.idesktop.IFolder;
import ch.epfl.lis.gnwgui.windows.Wait;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.utilities.IO;
import ch.epfl.lis.utilities.Unzipper;
import ch.epfl.lis.utilities.Zipper;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.utilities.filefilters.FilterGnwDesktopZIP;

/**
 * Extends a iDesktop to display open, bin and network icons.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class NetworkDesktop extends IDesktop
{
	/** Import item */
	private IElement import_ = null;
	/** New Folder item */
	private IElement newFolder_ = null;
	/** Export network desktop content */
	private IElement exportDesktop_ = null;
	/** Instance of the bin which will be placed on the desktop. */
	private IBin bin_ = null;

	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(NetworkDesktop.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor */
	public NetworkDesktop(String name)
	{
		super(name);
		init();
	}

	// ----------------------------------------------------------------------------

	/** Initialization of the desktop */
	@SuppressWarnings("serial")
	public void init()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		
		/**********************************************************************
		 * Create and initialize an item which will be used to import the
		 * networks onto the desktop, and finally place it on the desktop.
		 *********************************************************************/
		
		import_ = new IElement("Open", this)
		{
			@Override
			public void leftMouseButtonInvocationSimple()
			{	
				IONetwork.open();
			}
			public void wheelMouseButtonInvocation()
			{
				IONetwork.open();
			}

			public void rightMouseButtonInvocation()
			{	
				IONetwork.open();
			}

			protected void leftMouseButtonInvocationDouble()
			{	
				IONetwork.open();
			}
			
			public IElement copyElement() { return null; }
		};
		import_.setItemIcon(new ImageIcon(global.getImportNetworkIcon()).getImage());
		import_.setDestroyable(false);
		import_.setToolTipText("<html>Import a network structure or a kinetic network model</html>");
		addItemOnDesktop(import_);
		
		newFolder_ = new IElement("New Folder",this)
		{
			protected void wheelMouseButtonInvocation() {}
			protected void rightMouseButtonInvocation()
			{
				NewFolder nf = new NewFolder(GnwGuiSettings.getInstance().getGnwGui().getFrame());
				nf.setVisible(true);
			}
			
			protected void leftMouseButtonInvocationSimple()
			{
				NewFolder nf = new NewFolder(GnwGuiSettings.getInstance().getGnwGui().getFrame());
				nf.setVisible(true);
			}
			protected void leftMouseButtonInvocationDouble()
			{
				NewFolder nf = new NewFolder(GnwGuiSettings.getInstance().getGnwGui().getFrame());
				nf.setVisible(true);
			}
			@Override
			public IElement copyElement() {return null;}
		};
		newFolder_.setItemIcon(new ImageIcon(global.getFolderIcon()).getImage());
		newFolder_.setDestroyable(false);
		newFolder_.setToolTipText("<html>Create a new folder");
		addItemOnDesktop(newFolder_);
		
		exportDesktop_ = new IElement("Export Desktop", this)
		{
			protected void wheelMouseButtonInvocation() {}
			protected void rightMouseButtonInvocation()
			{
				exportDesktopInterface();
			}
			
			protected void leftMouseButtonInvocationSimple()
			{
				exportDesktopInterface();
			}
			protected void leftMouseButtonInvocationDouble()
			{
				exportDesktopInterface();
			}
			@Override
			public IElement copyElement() {return null;}
		};
		exportDesktop_.setItemIcon(new ImageIcon(global.getExportNetworkDesktopImage()).getImage());
		exportDesktop_.setDestroyable(false);
		exportDesktop_.setToolTipText("<html>Export all the networks on the desktop to a ZIP archive</html>");
		addItemOnDesktop(exportDesktop_);
		
		
		/**
		 * Create and initialize a bin and place it on the desktop.
		 */
		bin_ = new IBin(this, "Recycle Bin");
		bin_.setEmptyIcon(new ImageIcon(global.getBinEmptyIcon()).getImage());
		bin_.setFilledIcon(new ImageIcon(global.getBinFullIcon()).getImage());
		bin_.setToolTipText("Drag-and-drop networks to delete (cannot be undone)");
		addItemOnDesktop(bin_);

	

		displayOptionsDialog(desktopPanel_);
		displayRenameDialog(desktopPanel_);
		displayExtractionDialog(desktopPanel_);
		displayVisualizationDialog(desktopPanel_);
		displayOpenDialog(desktopPanel_);
		displaySaveDialog(desktopPanel_);
		displayBenchmarkDialog(desktopPanel_);
		displayKineticModelGenerationDialog(desktopPanel_);
		anonymizeNetwork(desktopPanel_);
		countNumMotifs(desktopPanel_);
		computeConnectivityDensity(desktopPanel_);
		computeModularity(desktopPanel_);
	}
	
	// ----------------------------------------------------------------------------
	
	public void exportDesktopInterface()
	{
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Export Desktop",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);

		dialog.addFilter(new FilterGnwDesktopZIP());

		dialog.setAcceptAllFileFilterUsed(false);
		dialog.setSelection("gnw_desktop.zip");
		dialog.display();
		
		if (dialog.getSelection() != null)
		{
			log_.log(Level.INFO, "Export desktop");

			String selection = dialog.getSelection();
			String absPath = FilenameUtilities.getDirectory(selection) + "/";
			String zipFilename = FilenameUtilities.getFilenameWithoutPath(selection);
			zipFilename = Zipper.appendZipExtensionIfRequired(zipFilename);
			
			Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
			wait.setTitle("Export desktop");
			DesktopExport de = new DesktopExport(wait);
			
			de.setAbsPath(absPath);
			de.setZipFilename(zipFilename);
			
			de.execute();
			wait.start();
			
			log_.log(Level.INFO, "All the networks have been saved to the ZIP archive " + absPath + zipFilename);
			log_.log(Level.INFO, "Done");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void importDesktopInterface(String zipFileAbsPath)
	{
		log_.log(Level.INFO, "Importing GNW desktop content");
		
		importDesktop(zipFileAbsPath);
		
		log_.log(Level.INFO, "Done");
	}
	
	// ----------------------------------------------------------------------------
	
	public void exportDesktop(String absPath, String zipFilename)
	{
		try
		{			
			String tmp = absPath + "gnw_desktop_tmp";
			File tmpFolder = new File(tmp);
			tmpFolder.mkdir();
			
			tmp = tmp + "/gnw_desktop";
			File desktopFolder = new File(tmp);
			desktopFolder.mkdir();
			
			NetworkDesktopMap map = new NetworkDesktopMap(this);
			map.encode(tmp + "/map.xml");
			
			ArrayList<IElement> list = content_.get(0);

			for (IElement element : list)
				saveDesktopRecursively(element, tmp + "/", map);
			
			String zipFile = absPath + zipFilename;
			Zipper.zipFolder(tmp, zipFile);
			System.gc();
			if(!IO.deleteFolder(tmpFolder))
				throw new Exception("Unable to delete tmp folder " + tmpFolder.getAbsolutePath());
			
		}
		catch(Exception e)
		{
			log_.log(Level.WARNING, "NetworkDesktop::exportDesktop(): " + e.getMessage(), e);
		} 
	}
	
	// ----------------------------------------------------------------------------
	
	public void importDesktop(String zipFileAbsPath)
	{
		try
		{
			// log_.log(Level.INFO, "Empty desktop");
			emptyDesktop();
			
			// log_.log(Level.INFO, "Extract networks from zip archive");
			String destAbsPath = FilenameUtilities.getDirectory(zipFileAbsPath) + "/";
			String tmp = destAbsPath + "gnw_desktop_tmp";
			File tmpFolder = new File(tmp);
			tmpFolder.mkdir();
			
	        Unzipper unzip = new Unzipper();
	        String unZipFile = zipFileAbsPath;  
	        String unZipOutFolder = tmp + "/";
	        unzip.recursiveUnzip(new File(unZipFile), new File(unZipOutFolder));
	       
			// log_.log(Level.INFO, "Load networks");
			File gnwDesktop = new File(tmp + "/gnw_desktop");
			if (gnwDesktop.exists() && gnwDesktop.isDirectory())
			{
				NetworkDesktopMap map = new NetworkDesktopMap(this);
				map.decode(tmp + "/gnw_desktop/map.xml");
				
				refreshDesktop();
			}
			else
				throw new Exception("Unable to access " + gnwDesktop.getAbsolutePath());
			
			System.gc();
			// remove extracted files
			if(!IO.deleteFolder(tmpFolder))
				throw new Exception("Unable to delete tmp folder " + tmpFolder.getAbsolutePath());
		}
		catch (Exception e)
		{
			log_.log(Level.WARNING, "NetworkDesktop::importDesktop(): " + e.getMessage(), e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void loadDesktopRecursively(String path, IFolder folder) throws Exception
	{
		File file = new File(path);
		
		if (!file.exists())
			throw new Exception("File/folder " + file.toString() + " doesn't exist");
		
		String name = FilenameUtilities.getFilenameWithoutExtension(path);
		URL url = GnwSettings.getInstance().getURL(path);
		
		if (file.isFile())
		{	
			Integer format = null;
			
			if ( (format = IONetwork.isStructureExtension(url, null)) != null)
			{
				StructureElement element = IONetwork.loadStructureItem(name, url, format);
				IONetwork.printOpeningInfo(element);
				
				if (folder == null)
					this.addItemOnDesktop(element);
				else
					this.addItemOnDesktop(element, folder);
				
			}
			else if ( (format = IONetwork.isDynamicalNetworkExtension(url, null)) != null)
			{
				DynamicalModelElement element = IONetwork.loadDynamicNetworkItem(name, url, format);
				IONetwork.printOpeningInfo(element);
				
				if (folder == null)
					this.addItemOnDesktop(element);
				else
					this.addItemOnDesktop(element, folder);
			}
		}
		else if (file.isDirectory())
		{
			String[] children = file.list();
			java.util.Arrays.sort(children, String.CASE_INSENSITIVE_ORDER);
			IFolder newFolder = null;
			
			if (name != "gnw_desktop")
				newFolder = new IFolder(name, this);
			
			if (folder == null)
				addItemOnDesktop(newFolder);
			else
				folder.addChild(newFolder);
			
			for (int i = 0; i < children.length; i++)
				loadDesktopRecursively(path + "/" + children[i], newFolder);
		}
	}
	
	// ----------------------------------------------------------------------------

	public void saveDesktopRecursively(IElement element, String root, NetworkDesktopMap map) throws Exception
	{
		if (element instanceof StructureElement)
		{
			URL url = GnwSettings.getInstance().getURL(root + map.getNextFilename());
			IONetwork.exportTSVStructure((StructureElement) element, url);
		}
		else if (element instanceof DynamicalModelElement)
		{
			URL url = GnwSettings.getInstance().getURL(root + map.getNextFilename());
			IONetwork.exportSBMLGeneRegulatoryNetwork((DynamicalModelElement) element, url);
		}
		
		if (element.hasChildren() || element instanceof IFolder)
		{
//			File f = new File(root + element.getLabel());
//			f.mkdir();
			
			for (int i = 0; i < element.getChildren().size(); i++)
				saveDesktopRecursively(element.getChildren().get(i), root, map);
				//saveDesktopRecursively(element.getChildren().get(i), root + element.getLabel() + "/", map);
		}
	}

	// ----------------------------------------------------------------------------

	/**
	 * Action to execute when an item is released. For instance, if the item is released on
	 * the bin and is destroyable, the item is remove from the desktop.
	 */
	public void itemReleased(IElement item) {
		Folder folder = null;
		boolean movedToAFolder = false;
		if (isItemOnAnother(item, bin_)) {
			if (!item.equals(bin_) && item.isDestroyable()) {
				bin_.addItemIntoBin(item);
				removeItemFromDesktop(item); // Step2: Remove the item from the desktop
				return;
			}
		}
		
		//Check for folder move
		IElement oldDisplayChildrenOf = getDisplayChildrenOf();
		for(int i=0;i < content_.size();i++) {
			for(int j=0;j< content_.get(i).size();j++) {
				if ( content_.get(i).get(j).getClass() == Folder.class && content_.get(i).get(j) != item) {
					if( !item.getChildren().contains(content_.get(i).get(j)) && item.getFather() != content_.get(i).get(j) && isItemOnAnother(item, content_.get(i).get(j))) {
						folder = (Folder)content_.get(i).get(j);
						
						moveItemToFolder(item, folder);
						movedToAFolder = true;
						displayChildrenOf(folder);
						displayChildrenOf(oldDisplayChildrenOf);
						if ( getDisplayChildrenOf() == item) {
							displayChildrenOf(folder);
						}
						return;
					}
				}
			}
		}

		if ( !isItemOnAnother(item) && !movedToAFolder && isItemOnColumn(item) != getElementPosition(item).x && isItemOnColumn(item) >= 0) {
			int col = isItemOnColumn(item);
			//log_.log(Level.INFO,"Moving " + item.getLabel() + " from column " + getElementPosition(item).x + " to column " + isItemOnColumn(item));

			if (col == 0) {
				content_.get(0).add(item);
				if ( item.getFather() != null)
					item.getFather().getChildren().remove(item);
				
				item.setFather(null);
			}
			else {
				if ( content_.get(col).get(0).getFather() != item) {
					if ( item.getFather() != null)
						item.getFather().getChildren().remove(item);
					IElement father = content_.get(col).get(0).getFather();
					
					//log_.log(Level.INFO,"New father: " + father.getLabel());
					content_.get(getElementPosition(item).x).remove(item);
					item.setFather(father);
					father.addChild(item);
					
					if( father != null)
						displayChildrenOf(father);
				}
			}
		}
		if( getDisplayChildrenOf() != null )			
			displayChildrenOf(getDisplayChildrenOf());
		
		repaintDesktop();
		desktopPanel_.repaint();		
	}

	// ----------------------------------------------------------------------------
	
	/**
	 * Move to a folder
	 */
	
	public void moveItemToFolder(IElement item, IFolder folder) {
		
		if ( item.getFather()!=null) {
			
			item.getFather().getChildren().remove(item);
		}		
		Point pos = getElementPosition(item);
		
		if (pos == null)
			return;
		
		int c = (int) pos.getX();
		content_.get(c).remove(item);
		
		
		folder.addChild(item);
		
		item.setFather(folder);
		//GnwGuiSettings.getInstance().getNetworkDesktop().displayChildrenOf(folder);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Show the options window when the user presses ENTER on an element from
	 * the desktop.
	 */
	@SuppressWarnings("serial")
	public void displayOptionsDialog(JComponent jp)
	{
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_OPTIONS_DIALOG");
		jp.getActionMap().put("DISPLAY_OPTIONS_DIALOG", new AbstractAction()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				displayOptionsDialog();
			}
		});
	}

	// ----------------------------------------------------------------------------

	/**
	 * Action to do when F2 or R are pressed.
	 */
	@SuppressWarnings("serial")
	public void displayRenameDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
		KeyStroke k2 = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_RENAME_DIALOG");
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k2, "DISPLAY_RENAME_DIALOG2");

		jp.getActionMap().put("DISPLAY_RENAME_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayRenameDialog();
			}
		});

		jp.getActionMap().put("DISPLAY_RENAME_DIALOG2", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayRenameDialog();
			}
		});
	}

	// ----------------------------------------------------------------------------

	/**
	 * Action to do when E is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayExtractionDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_EXTRACTION_DIALOG");
		jp.getActionMap().put("DISPLAY_EXTRACTION_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayExtractionDialog();
			}
		});
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Action to do when K is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayKineticModelGenerationDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_K, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_KINETIC_MODEL_GENERATION_DIALOG");
		jp.getActionMap().put("DISPLAY_KINETIC_MODEL_GENERATION_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayKineticModelGenerationDialog();
			}
		});
	}

	// ----------------------------------------------------------------------------

	/**
	 * Action to do when V is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayVisualizationDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_V, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_VISUALIZATION_DIALOG");
		jp.getActionMap().put("DISPLAY_VISUALIZATION_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayVisualizationDialog();
			}
		});
	}

	// ----------------------------------------------------------------------------

	/**
	 * Action to do when O (letter) is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayOpenDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_O, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_OPEN_DIALOG");
		jp.getActionMap().put("DISPLAY_OPEN_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayOpenDialog();
			}
		});
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Action to do when A (letter) is pressed.
	 */
	@SuppressWarnings("serial")
	public void anonymizeNetwork(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "ANONYMIZE_NETWORK");
		jp.getActionMap().put("ANONYMIZE_NETWORK", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayAnonymizeNetworkDialog();
				//anonymizeNetwork();
			}
		});
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Action to do when M (letter) is pressed.
	 */
	@SuppressWarnings("serial")
	public void countNumMotifs(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_M, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "ANALYZE_MOTIFS");
		jp.getActionMap().put("ANALYZE_MOTIFS", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				countNumMotifs();
			}
		});
	}

	// ----------------------------------------------------------------------------

	/**
	 * Action to do when S is pressed.
	 */
	@SuppressWarnings("serial")
	public void displaySaveDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_SAVE_DIALOG");
		jp.getActionMap().put("DISPLAY_SAVE_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displaySaveDialog();
			}
		});
	}

	// ----------------------------------------------------------------------------

	/** Action to do when B is pressed. */
	@SuppressWarnings("serial")
	public void displayBenchmarkDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_BENCHMARK_DIALOG");
		jp.getActionMap().put("DISPLAY_BENCHMARK_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayBenchmarkDialog();
			}
		});
	}
	
	// ----------------------------------------------------------------------------

	/** Action to do when C is pressed. */
	@SuppressWarnings("serial")
	public void computeConnectivityDensity(JComponent jp)
	{
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_C, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_CONNECTIVITY_DENSITY");
		jp.getActionMap().put("DISPLAY_CONNECTIVITY_DENSITY", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				computeConnectivityDensity();
			}
		});
	}
	
	// ----------------------------------------------------------------------------

	/** Action to do when Y is pressed. */
	@SuppressWarnings("serial")
	public void computeModularity(JComponent jp)
	{
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_MODULARITY");
		jp.getActionMap().put("DISPLAY_MODULARITY", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				computeModularity();
			}
		});
	}

	// ----------------------------------------------------------------------------

	/** Calls the options window of the selected element. */
	public void displayOptionsDialog()
	{
		IElement element = IElement.curItem;

		int N = getNumberOfSelectedElements();
		if (N == 0)
			return;

		if (element != null && element != bin_)
		{
			Options dialog = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), element);
			dialog.setVisible(true);
			return;
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public static void anonymizeNetwork(NetworkElement item)
	{
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Save lookup table",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);
		dialog.setSelection(item.getLabel() + "_gene_names.txt");
		dialog.display();
	
		if (dialog.getSelection() != null)
		{			
			Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
			wait.setTitle("Anonymize Network");
			NetworkAnonymize na = new NetworkAnonymize(wait);
			
			na.element_ = item;		
			na.filename_ = dialog.getSelection();
			
			na.execute();
			// MUST BE CALLED AFTER EXECUTE
			if (na.displayWaitingBox())
				wait.start();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void computeConnectivityDensity()
	{
		IElement element = IElement.curItem;

		int N = getNumberOfSelectedElements();
		if (N == 0)
			return;

		if (element != null && element != bin_ &&
								element != import_ &&
								element != exportDesktop_ &&
								element != newFolder_ &&
								element.getClass() != IFolder.class)
		{
			ImodNetwork network = null;
			if (element instanceof StructureElement)
				network = ((StructureElement) element).getNetwork();
			else if (element instanceof DynamicalModelElement)
				network = ((DynamicalModelElement) element).getGeneNetwork();
			
			GraphUtilities util = new GraphUtilities(network);
			double C = util.computeConnectivityDensity();
			
			log_.log(Level.INFO, "Connectivity density of " + network.getId() + ": " + C);
		}
	}
	
	public void computeModularity()
	{
		IElement element = IElement.curItem;

		int N = getNumberOfSelectedElements();
		if (N == 0)
			return;

		if (element != null && element != bin_ &&
								element != import_ &&
								element != exportDesktop_ &&
								element != newFolder_ &&
								element.getClass() != IFolder.class)
		{
			ImodNetwork network = null;
			if (element instanceof StructureElement)
				network = ((StructureElement) element).getNetwork();
			else if (element instanceof DynamicalModelElement)
				network = ((DynamicalModelElement) element).getGeneNetwork();
			
			GraphUtilities util = new GraphUtilities(network);
			ArrayList<Double> mod = null;
			try
			{
				mod = util.computeModularity();
				
				Double Q = mod.get(0);
//				Integer numModules = mod.get(1).intValue();
				
				log_.log(Level.INFO, "Modularity of " + network.getId() + ": " + Q.toString());
//				log_.log(Level.INFO, "Number of indivisible modules: " + numModules.toString());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// ----------------------------------------------------------------------------

	public static void anonymizeAllSelectedNetworks()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		
		//Get all elements to anonymize
		ArrayList<IElement> topElements = global.getNetworkDesktop().getTopSelectedItems();
		
		//Open dialog
		IODialog dialog = new IODialog(global.getGnwGui().getFrame(), "Select Folder",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);
		
		dialog.selectOnlyFolder(true);
		
		dialog.display();
		
		try
		{
			if (dialog.getSelection() != null)
			{
				for(IElement itemIE : topElements)
					recursiveAnonymize(itemIE, dialog.getSelection());
			}
		} catch (Exception e)
		{
			log_.log(Level.WARNING, "NetworkDesktop::anonymizeAllSelectedNetwork(): " + e.getMessage(), e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public static void recursiveAnonymize(IElement item, String path)
	{
		
		if (item.getClass() == Folder.class)
		{
			path += "/" +((IFolder)item).getLabel();
			IONetwork.createFolder(path);
		}
		else
		{
			Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
			wait.setTitle("Anonymize Network");
			NetworkAnonymize na = new NetworkAnonymize(wait);
			
			na.element_ = (NetworkElement) item;		
			na.filename_ = path + "/" + item.getLabel() + "_gene_names.txt";
			
			na.execute();
			// MUST BE CALLED AFTER EXECUTE
			if (na.displayWaitingBox())
				wait.start();
			
			// modify path if current element has children elements
			path += "/" + item.getLabel();
		}
		
		if (item.hasChildren())
		{
			IONetwork.createFolder(path);
			for(IElement e : item.getChildren())
				recursiveAnonymize(e, path);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void countNumMotifs()
	{
		IElement element = IElement.curItem;
		if (element == import_ || element == bin_ || element == exportDesktop_)
			return;

		if (element != null)
		{
			IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Save num motifs",
					GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);
			dialog.setSelection(element.getLabel() + "_numMotifs.txt");
			dialog.display();
			
			String filename = "";
			if (dialog.getSelection() != null)
				filename = dialog.getSelection();
			
			dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Save num non-overlapping motifs",
					GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);
			dialog.setSelection(element.getLabel() + "_numNonOverlappingMotifs.txt");
			dialog.display();
			
			String filename_nonOverlapping = "";
			if (dialog.getSelection() != null)
				filename_nonOverlapping = dialog.getSelection();
			
			if (filename.compareTo("") == 0 && filename_nonOverlapping.compareTo("") == 0)
				return;
			
			// Get the ImodNetwork
			ImodNetwork gold = null;
			if (element instanceof StructureElement)
				gold = ((StructureElement) element).getNetwork();
			else if (element instanceof DynamicalModelElement)
				gold = ((DynamicalModelElement) element).getGeneNetwork();
			
			Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
			wait.setTitle("Count Motifs");
			MotifCount mc = new MotifCount(wait);
			
			mc.setGoldStandard(gold);
			mc.setNumMotifsFilename(filename);
			mc.setNumNonOverlappingMotifsFilename(filename_nonOverlapping);
			
			mc.execute();
			// MUST BE CALLED AFTER EXECUTE
			if (mc.displayWaitingBox())
				wait.start();
		}
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Open the dialog "Rename" if the selected item is a network.
	 */
	public void displayRenameDialog()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		ArrayList<IElement> items = null;

		if (getNumberOfSelectedElements() == 1)
		{
			IElement element = IElement.curItem;
			items = new ArrayList<IElement>();
			
			if (element == null || element == import_ || element == bin_ || element == newFolder_)
				return;
			
			items.add(element);
		}
		else if (getNumberOfSelectedElements() > 1 )
			items = global.getNetworkDesktop().getSelectedElements();

		Rename dialog = new Rename(global.getGnwGui().getFrame(), items);
		dialog.setVisible(true);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Open the dialog "Subnet Extractions" if the selected item is a network.
	 */
	public void displayExtractionDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_) {
			if (element instanceof StructureElement)
				Options.subnetworkExtraction((StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				Options.subnetworkExtraction((DynamicalModelElement) element);
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Display the dialog to generate dynamical gene network models */
	public void displayKineticModelGenerationDialog()
	{
		IElement element = IElement.curItem;

		if (getNumberOfSelectedElements() == 1)
		{
			if (element != null && element != import_ && element != bin_ && element.getClass() != Folder.class)
			{
				Options options = null;
				if (element instanceof StructureElement)
					options = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (StructureElement) element);
				else if (element instanceof DynamicalModelElement)
					options = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (DynamicalModelElement) element);
				
				if (options != null)
					options.convertToDynamicalModel();
			}
			else if (element.getClass() == Folder.class)
				Options.convertAllToDynamicalModels();
		}
		else if (getNumberOfSelectedElements() > 1 )
			Options.convertAllToDynamicalModels();
	}

	// ----------------------------------------------------------------------------

	/**
	 * Open the "Visualization" dialog if the selected item is a network.
	 */
	public void displayVisualizationDialog()
	{
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_)
		{
			if (element instanceof StructureElement)
				Options.viewNetwork((StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				Options.viewNetwork((DynamicalModelElement) element);
		}
	}

	// ----------------------------------------------------------------------------

	/**
	 * Open the "Opening network" dialog.
	 */
	public void displayOpenDialog()
	{
		IONetwork.open();
	}
	// ----------------------------------------------------------------------------

	/**
	 * Display the dialog to anonymize the gene name of one or several networks.
	 */
	public void displayAnonymizeNetworkDialog()
	{
		IElement element = IElement.curItem;

		if (getNumberOfSelectedElements() == 1)
		{
			if (element != null && element != import_ && element != bin_ && element.getClass() != Folder.class)
			{
				if (element instanceof StructureElement)
					anonymizeNetwork((StructureElement) element);
				else if (element instanceof DynamicalModelElement)
					anonymizeNetwork((DynamicalModelElement) element);
			}
			else if ( element.getClass() == Folder.class)
				anonymizeAllSelectedNetworks();
		}
		else if (getNumberOfSelectedElements() > 1 )
		{
			anonymizeAllSelectedNetworks();
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Open the "Saving network" dialog. */
	public void displaySaveDialog()
	{
		IElement element = IElement.curItem;

		if (getNumberOfSelectedElements() == 1)
		{
			if (element != null && element != import_ && element != bin_ && element.getClass() != Folder.class)
			{
				if (element instanceof StructureElement)
					IONetwork.saveAs((StructureElement) element);
				else if (element instanceof DynamicalModelElement)
					IONetwork.saveAs((DynamicalModelElement) element);
			}
			else if ( element.getClass() == Folder.class)
				IONetwork.saveAllSected();
		}
		else if (getNumberOfSelectedElements() > 1 )
		{
			IONetwork.saveAllSected();
		}
	}

	// ----------------------------------------------------------------------------

	/**
	 * Open the "Benchmark generator" dialog if the selected item is a dynamical model.
	 */
	public void displayBenchmarkDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_ && element instanceof DynamicalModelElement) {
			try {
				Options.generateDREAM3GoldStandard((DynamicalModelElement) element);
			} catch (Exception e) {
				log_.log(Level.WARNING, "NetworkDesktop::displayBenchmarkDialog(): " + e.getMessage(), e);
			}
		}
	}

	// ----------------------------------------------------------------------------

	public void multipleElementsSelectionMenu()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		Options od = new Options(global.getGnwGui().getFrame(), new Multi());
		od.setMenu(Options.MULTIPLE_SELECTION_MENU);
		od.setVisible(true);
	}
	
	
	// ============================================================================
	// PRIVATE CLASSES
	
	/**
	 * This class save the content of the network desktop to ZIP file (independent thread)
	 * 
	 * @author Thomas Schaffter (firstname.name@gmail.com)
	 */
	private class DesktopExport extends SwingWorker<Void, Void>
	{
		/** Dialog displayed during the process */
		private Wait wDialog_;
		
		/** Folder where the desktop will be exported */
		private String absPath_;
		
		/** Zip filename */
		private String zipFilename_;
		
		
		// ============================================================================
		// PUBLIC METHODS
	  
		public DesktopExport(Wait gui)
		{
			this.wDialog_ = gui;
		}
		
		
		// ============================================================================
		// PROTECTED METHODS
	  
		@Override
		protected Void doInBackground() throws Exception
		{
			exportDesktop(absPath_, zipFilename_);
			
			return null;
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected void done()
		{
			wDialog_.stop();
		}
		
		// ============================================================================
		// SETTERS AND GETTERS
		
		public void setAbsPath(String path) { absPath_ = path; }
		public void setZipFilename(String filename) { zipFilename_ = filename; }
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This class counts the number of 3-node motifs (triads) in a network (independent thread)
	 * 
	 * @author Thomas Schaffter (firstname.name@gmail.com)
	 */
	public static class MotifCount extends SwingWorker<Void, Void>
	{
		/** Dialog displayed during the process */
		private Wait wDialog_;
		/** Do not display waiting box for network size < 1000 */
		private int wDialogMinNetworkSize_ = 1000;
		
		/** Gold standard */
		private ImodNetwork gold_;
		/** Filename to save motif instances */
		private String numMotifsFilename_;
		/** Filename to save non-overlapping motif instances */
		private String numNonOverlappingMotifsFilename_;
		
		
		// ============================================================================
		// PUBLIC METHODS
	  
		public MotifCount(Wait gui)
		{
			this.wDialog_ = gui;
		}
		
		
		// ============================================================================
		// PROTECTED METHODS
		
		protected boolean displayWaitingBox()
		{
			return (gold_.getSize() > wDialogMinNetworkSize_);
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected Void doInBackground() throws Exception
		{
			MotifPrediction mp = new MotifPrediction();
			mp.initialize(gold_);
			mp.motifProfile(); // counts num motifs
			
			// save num motifs to file(s)
			if (numMotifsFilename_.compareTo("") != 0)
				mp.saveCountInstances(numMotifsFilename_);
			if (numNonOverlappingMotifsFilename_.compareTo("") != 0)
				mp.saveCountNonOverlappingInstances(numNonOverlappingMotifsFilename_);
			
			// save the motif definitions
			if (numMotifsFilename_.compareTo("") != 0 || numNonOverlappingMotifsFilename_.compareTo("") != 0)
			{
				String path = FilenameUtilities.getDirectory(numMotifsFilename_);
				String filename = path + "/GNW_motif_definitions.txt";
				MotifDefinitions.saveMotifDefinitions(filename);
			}
			
			return null;
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected void done()
		{
			if (wDialog_ != null)
				wDialog_.stop();
		}
		
		
		// ============================================================================
		// SETTERS AND GETTERS
		
		public void setGoldStandard(ImodNetwork gold) { gold_ = gold; }
		public void setNumMotifsFilename(String filename) { numMotifsFilename_ = filename; }
		public void setNumNonOverlappingMotifsFilename(String filename) { numNonOverlappingMotifsFilename_ = filename; }
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This class must be used to anonymize the gene names of a network.
	 * This is done in an independent thread.
	 * 
	 * @author Thomas Schaffter (firstname.name@gmail.com)
	 */
	private static class NetworkAnonymize extends SwingWorker<Void, Void>
	{
		/** Dialog displayed during the process */
		private Wait wDialog_;
		/** Do not display waiting box for network size < 200 */
		private int wDialogMinNetworkSize_ = 5000;
		
		/** Element to export */
		private NetworkElement element_;
		
		/** Absolute path to the network file */
		private String filename_;
		
		
		// ============================================================================
		// PUBLIC METHODS
	  
		public NetworkAnonymize(Wait gui)
		{
			this.wDialog_ = gui;
		}
		
		
		// ============================================================================
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
			if (element_ instanceof StructureElement)
			{
				GraphUtilities util = new GraphUtilities(((StructureElement) element_).getNetwork());
				util.anonymizeGenes(filename_);
			}
			else if (element_ instanceof DynamicalModelElement)
			{
				GraphUtilities util = new GraphUtilities(((DynamicalModelElement) element_).getGeneNetwork());
				util.anonymizeGenes(filename_);
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
