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

import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.idesktop.IFolder;
import ch.epfl.lis.gnwgui.windows.RenameWindow;

/**
 * Display a dialog to rename an IElement.
 * Renaming of multiple element is done sequentially in a separated thread.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Rename extends RenameWindow
{
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Interactive element (network item are IElement in GNW) to rename. */
	private ArrayList<IElement> items_ = null;
	
	/** Document associated to the text field newName_ */
	private Document newNameDocument_;
	
    /** Logger for this class */
	private static Logger log_ = Logger.getLogger(Rename.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
    /** Constructor */
	public Rename(Frame aFrame, ArrayList<IElement> items)
	{		
		super(aFrame);
		
		items_ = items;

		if (items.size() == 1)
		{
			IElement item = items.get(0);
			if (item != null)
			{
				newName_.setText(item.getLabel());
				newName_.selectAll();
			}
			
			String title1, title2;
			title1 = title2 = "";
			if (item instanceof StructureElement)
			{
				ImodNetwork network = ((StructureElement)item).getNetwork();
				title1 = item.getLabel();
				title2 = network.getSize() + " nodes, " + network.getNumEdges() + " edges";
			}
			else if (item instanceof DynamicalModelElement)
			{
				GeneNetwork geneNetwork = ((DynamicalModelElement)item).getGeneNetwork();
				title1 = item.getLabel();
				title2 = geneNetwork.getSize() + " genes, " + geneNetwork.getNumEdges() + " interactions";
			}
			else if(item instanceof IFolder)
			{
				IFolder folder = ((IFolder)item);
				title1 = folder.getLabel();
				title2 = folder.getChildren().size() + " direct children";
			}
			setHeaderInfo(title1 + " (" + title2 + ")");
		}
		else
		{
			setHeaderInfo("Batch renaming " + items.size() + " items");
		}

		
		/**************************************************
		 * ACTIONS
		 *************************************************/
		
		// If text field empty, disable OK button
		newNameDocument_ = newName_.getDocument();
		newNameDocument_.addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent arg0)
			{
				applyButton.setEnabled(!newName_.getText().equals(""));
			}
			public void insertUpdate(DocumentEvent arg0)
			{
				applyButton.setEnabled(!newName_.getText().equals(""));
			}
			public void removeUpdate(DocumentEvent arg0)
			{
				applyButton.setEnabled(!newName_.getText().equals(""));
			}
		});
		
		applyButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				enterAction();
			}
		});

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				escapeAction();
			}
		});
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Redefines the function enterAction instantiated in the class DialogReactive.
	 * After validation, the related item is renamed.
	 */
	@Override
	public void enterAction()
	{
		// Rename all IElement in a same, new thread
		ItemRename ir = new ItemRename();
		
		ir.elements_ = items_;
		ir.filename_ = newName_.getText();
		
		ir.execute();
		dispose();
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS

	public void setNewName(String text) { newName_.setText(text); }
	public String getNewName() { return newName_.getText(); }
	
	
	// ============================================================================
	// PRIVATE CLASSES
	
	/**
	 * This class renames one or multiple IElement (NetworkElement, IFolder, etc.).
	 * This is done in an new thread.
	 * 
	 * @author Thomas Schaffter (firstname.name@gmail.com)
	 */
	private static class ItemRename extends SwingWorker<Void, Void>
	{
		/** Element to export */
		private ArrayList<IElement> elements_;
		
		/** Provided string (could contains '#' to replace by ascending numbers) */
		private String filename_;
		
		
		// ============================================================================
		// PUBLIC METHODS
	  
		public ItemRename() {}
		
		
		// ============================================================================
		// PROTECTED METHODS
		
		@Override
		protected Void doInBackground() throws Exception
		{
			for (int i = 0; i < elements_.size(); i++)
			{
				String str = filename_.replaceAll("#", Integer.toString(i + 1));
				rename(elements_.get(i), str);
				log_.log(Level.INFO, "Renaming to " + str);
			}
			
			return null;
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected void done() {}
		
		// ----------------------------------------------------------------------------
		
		protected void rename(IElement item, String str)
		{	
			// Update the name of the item
			item.setLabel(str);
			
			// For networks, update the ID property
			if (item instanceof StructureElement)
				((StructureElement)item).getNetwork().setId(str);
			else if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().setId(str);
			
			GnwGuiSettings.getInstance().getNetworkDesktop().recalculateColumnWidths(item);
			GnwGuiSettings.getInstance().getNetworkDesktop().repaintDesktop();
		}
	}
}
