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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.windows.GraphViewerWindow;
import ch.epfl.lis.imod.ImodNetwork;

/** Implements the actions linked to the graph visualization window.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class GraphViewer extends GraphViewerWindow {

	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Reference to the father network used to generate subnet(s). */
	private NetworkElement item_ = null;
	
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(GraphViewer.class.getName());


	// ============================================================================
	// PUBLIC METHODS
    
	public GraphViewer(Frame aFrame, NetworkElement item) {
		
		super(aFrame);
		
		item_ = item;
		
		// Interaction labels
		final JLabel labels = new JLabel(new ImageIcon(GnwGuiSettings.getInstance().getInteractionLabels()));
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.gridy = 7;
		gridBagConstraints_7.gridx = 1;
		leftPanel_.add(labels, gridBagConstraints_7);
		
//		// Space
//		final Component vspace = Box.createVerticalStrut(15);
//		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
//		gridBagConstraints_6.gridy = 8;
//		gridBagConstraints_6.gridx = 1;
//		leftPanel_.add(vspace, gridBagConstraints_6);
		
		// Help button
		JButton helpButton = new JButton();
		helpButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent arg0)
			{
				String text = "";
				text +=       "-----------------------\n";
				text +=       "GRAPH REPRESENTATION\n";
				text +=       "-----------------------\n";
				text +=       "Option \"Move graph\"\n";
				text +=       "-----------------------\n";
				text +=       "Left click + drag" + "\t" + "Move" + "\n";
				text +=		  "Left click + SHIFT" + "\t" + "Rotate" + "\n";
				text +=		  "Left click + CTRL" + "\t" + "Scale" + "\n";
				text +=		  "Mouse wheel" + "\t" + "Zoom" + "\n";
				text +=		  "\n";
				text +=       "Option \"Move nodes\"\n";
				text +=       "-----------------------\n";
				text +=       "Left click node + drag" + "\t" + "Move" + "\n";
				text +=		  "Left click + SHIFT" + "\t" + "Add to selection" + "\n";
				text +=		  "Left click + drag" + "\t" + "Select region" + "\n";
				text +=		  "Left click node + CTRL" + "\t" + "Center" + "\n";
				text +=		  "Mouse wheel" + "\t" + "Zoom" + "\n";
				text +=		  "\n";
				text +=       "Keyboard controls\n";
				text +=       "-----------------------\n";
				text +=		  "ALT-P" + "\t" + "Export as image\n";
				text +=		  "ESC" + "\t" + "Close window" + "\n";
				
				log_.log(Level.INFO, text);
				GnwConsoleHandler.getInstance().displayConsoleWindow(true);
			}
		});
		
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.gridy = 18;
		gridBagConstraints_11.gridx = 1;
		leftPanel_.add(helpButton, gridBagConstraints_11);
		helpButton.setBackground(UIManager.getColor("Button.background"));
		helpButton.setText("<html><center>Display help</center></html>");
		
		GnwGuiSettings settings = GnwGuiSettings.getInstance();
		helpButton.setIcon(new ImageIcon(settings.getMenuHelpImage()));
		
		
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
		
		
		if (item_.getNetworkViewer() == null) {
			item_.setNetworkViewer(new NetworkGraph(item_));
		}

		// Add and siplay the graph representation in the center part of the window
		centerPanel_.add(item_.getNetworkViewer().getScreen(), BorderLayout.CENTER);
		
		// Add and display the network viewer controls
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		leftPanel_.add(item_.getNetworkViewer().getControl(), gridBagConstraints);
	}
}