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

package ch.epfl.lis.gnwgui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JPanel;

import ch.epfl.lis.gnwgui.GnwGuiSettings;
import ch.epfl.lis.gnwgui.windows.GenericWindow;

import javax.swing.border.TitledBorder;

/** Allows to visualize network graphs.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class GraphViewerWindow extends GenericWindow {

	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** The left panel contains the controler acting on the graph visualization. */
	protected JPanel leftPanel_;
	/** Central part of the window in which the graph is painted. */
	protected JPanel centerPanel_;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(GraphViewerWindow.class.getName());

    
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor
	 */
	public GraphViewerWindow(Frame aFrame) {
		super(aFrame, false);		
		setSize(840, 665);
		setHeaderTitle("Graph Representation");
		setTitle("Visualization");

		leftPanel_ = new JPanel();
		leftPanel_.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {7,0,7};
		gridBagLayout.rowHeights = new int[] {7,7,7,0,0,0,7,7,7};
		leftPanel_.setLayout(gridBagLayout);
		getContentPane().add(leftPanel_, BorderLayout.WEST);
		

		final Component component_6 = Box.createHorizontalStrut(30);
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.gridx = 1;gridBagConstraints_11.gridy = 0;
		gridBagConstraints_11.anchor = GridBagConstraints.NORTH;
		leftPanel_.add(component_6, gridBagConstraints_11);
	
		setLocationRelativeTo(GnwGuiSettings.getInstance().getGnwGui().getFrame());

		centerPanel_ = new JPanel();
		centerPanel_.setBackground(Color.WHITE);
		centerPanel_.setLayout(new BorderLayout());
		centerPanel_.setPreferredSize(new Dimension(0, 0));
		getContentPane().add(centerPanel_);

		final JPanel visualControlPanel = new JPanel();
		visualControlPanel.setBorder(new TitledBorder(null, "Visualization", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Sans", Font.PLAIN, 12), null));
		visualControlPanel.setBackground(Color.WHITE);
		final FlowLayout flowLayout_1 = new FlowLayout();
		flowLayout_1.setHgap(10);
		visualControlPanel.setLayout(flowLayout_1);
	}
}