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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.windows.DeleteWindow;

/**
 * Allows the user to erase one or multiple networks at once.
 * 
 * @author Gilles Roulet (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Delete extends DeleteWindow
{	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Selection array */
	private ArrayList<IElement> selected_;
		
    /** Logger for this class */
	private static Logger log_ = Logger.getLogger(Delete.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS

	public Delete(Frame aFrame)
	{
		super(aFrame);
		
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		NetworkDesktop nm = (NetworkDesktop)GnwGuiSettings.getInstance().getNetworkDesktop();
		ArrayList<IElement> children = null;
		
		selected_ = nm.getSelectedElements();
		
		//Gather all the networks that will be deleted (including children)
		for (int i = 0; i < selected_.size(); i++)
		{
			dlm.addElement(selected_.get(i).getLabel());
			
			children = null;
			children = nm.getAllChildren(selected_.get(i));
			
			if ( children != null)
			{
				for(int j=0;j<children.size();j++)
					dlm.addElement(children.get(j).getLabel());
			}
		}
		
		setHeaderTitle("Delete " + dlm.size() + " network(s)");
		
		
		/**************************************************
		 * ACTIONS
		 *************************************************/
		
		networkList_ = new JList<String>(dlm);
		
		// Disable selection of items
		networkList_.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				networkList_.clearSelection();
			}
			public void mousePressed(MouseEvent e)
			{
				networkList_.clearSelection();
			}
			public void mouseReleased(MouseEvent e)
			{
				networkList_.clearSelection();
			}
		});
		
		networkList_.addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseMoved(MouseEvent e)
			{
				networkList_.clearSelection();
			}
			public void mouseDragged(MouseEvent e)
			{
				networkList_.clearSelection();
			}
		});
		
		networkList_.setFocusable(false);
		listScrollPane_.setViewportView(networkList_);
		
		deleteButton_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				enterAction();
			}
		});
		
		cancelButton_.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent arg0)
			{
				escapeAction();
			}
		});
	}
	
	// ----------------------------------------------------------------------------
	
	/** Function called when ENTER is pressed. */
	protected void enterAction()
	{
		for (int i = 0; i < selected_.size(); i++)
		{
			if (selected_.get(i) != null)
			{
				GnwGuiSettings.getInstance().getNetworkDesktop().removeItemFromDesktop(selected_.get(i));
				log_.log(Level.INFO, "Removing " + selected_.get(i).getLabel() + " (and its content if any)");
			}
		}
		dispose();
	}
}
