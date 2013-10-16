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

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import ch.epfl.lis.gnwgui.idesktop.IElement;

/**
 * Allows the user to erase multiple elements (networks, folders) at once.
 * 
 * @author Gilles Roulet (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Multi extends IElement
{	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Selection array */
	private ArrayList<IElement> selected_;
	
	/** Number of elements selected */
	private int numElementsSelected_;
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS

	public Multi()
	{	
		super ("multi", null);
		
		DefaultListModel<String> dlm = new DefaultListModel<String>();
		NetworkDesktop nm = (NetworkDesktop)GnwGuiSettings.getInstance().getNetworkDesktop();
		ArrayList<IElement> children = null;
		
		selected_ = nm.getSelectedElements();
		boolean add;
		
		// Gather all the networks that will be deleted or exported (including children)
		for (int i = 0; i < selected_.size(); i++)
		{
			add = true;
			
			for (int k = 0; k < dlm.size(); k++)
			{
				if ( ((String)dlm.elementAt(k)).trim().equals(selected_.get(i).getLabel()) )
					add = false;
			}
			
			if (add)
				dlm.addElement(selected_.get(i).getLabel());
			
			children = null;
			children = nm.getAllChildren(selected_.get(i));
			
			if ( children != null)
			{
				for(int j = 0;j < children.size(); j++)
				{
					add = true;
					
					for (int k = 0; k < dlm.size(); k++)
					{	
						if ( ((String)dlm.elementAt(k)).trim().equals(children.get(j).getLabel()) )
							add = false;
					}
					if (add)
						dlm.addElement(children.get(j).getLabel());
				}
			}
		}
		
		numElementsSelected_ = dlm.size();
//		
//		setHeaderTitle("Multiple selection");
//		setHeaderInfo(dlm.size() + " items");
//		new JList(dlm);
	
//		
//		
//		/**
//		 * DUPLICATE
//		 */
//
//		duplicateSelection.addActionListener(new ActionListener() {
//			public void actionPerformed(final ActionEvent e) {
//								
//				dispose();
//			}
//			
//		});
//		
//		/**
//		 * EXPORT
//		 */
//		
//		exportStructure_.addActionListener(new ActionListener() {
//			public void actionPerformed(final ActionEvent arg0) {
//				//CLOSE WINDOW
//				dispose();
//				//DISPLAY SAVE DIALOG
//				((NetworkDesktop)GnwGuiSettings.getInstance().getNetworkDesktop()).displaySaveDialog();
//				
//			}
//		});
//		
//		/**
//		 * DELETE
//		 */
//		deleteSelection.addActionListener(new ActionListener() {
//			public void actionPerformed(final ActionEvent arg0) {
//				
//				//CLOSE WINDOW
//				dispose();
//				//DISPLAY DELETE WINDOW
//				GnwGuiSettings global = GnwGuiSettings.getInstance();
//				Delete dd = new Delete(global.getGnwGui().getFrame());
//
//				dd.setVisible(true);
//			}
//		});
		
				
	}

	@Override
	public IElement copyElement() { return null; }
	
	@Override
	protected void leftMouseButtonInvocationSimple() {}

	@Override
	protected void leftMouseButtonInvocationDouble()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		Options od = new Options(global.getGnwGui().getFrame(), this);
		od.setMenu(Options.MULTIPLE_SELECTION_MENU);
		this.mouseOverIcon_ = false;
		od.setVisible(true);
	}

	@Override
	protected void rightMouseButtonInvocation()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		Options od = new Options(global.getGnwGui().getFrame(), this);
		od.setMenu(Options.MULTIPLE_SELECTION_MENU);
		this.mouseOverIcon_ = false;
		od.setVisible(true);
	}

	@Override
	protected void wheelMouseButtonInvocation() {}
	
	
	public int getNumElementsSelected() { return numElementsSelected_; }
}
