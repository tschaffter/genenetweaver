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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Logger;
import javax.swing.Box;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import ch.epfl.lis.gnwgui.GnwGuiSettings;
import ch.epfl.lis.gnwgui.SearchBox;

/** Graphic components that control the graph representation.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class GraphViewerController extends JPanel {

	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Controller panel */
	protected JPanel controlerPanel_;
	
	/** Switch "Move graph"/"Move nodes" */
	protected JComboBox<String> interactionMode_;
	/** Selection of different graph layouts */
	protected JComboBox<String> layoutCombo_;
	
	/** Search node by id */
	protected SearchBox search_;
	
	/** To distinguish interactions by arrow shape */
	protected JCheckBox distinguishByArrowHead_;
	/** To distinguish interactions by color */
	protected JCheckBox distinguishByColor_;
	/** Enable/disable curved edges */
	protected JCheckBox curvedEdges_;
	/** Show/hide nodes' label */
	protected JCheckBox displayLabels_;
	
	/** Export graph as image */
	protected JButton exportButton_;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(GraphViewerController.class.getName());


	// ============================================================================
	// PUBLIC METHODS
    
    /**
     * Constructor
     */
	public GraphViewerController() {
		
		super();
		setBackground(Color.WHITE);
		
		controlerPanel_ = new JPanel();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {7,7};
		gridBagLayout.rowHeights = new int[] {7,7,7,7,7,7,7,7,7,7,0,7,0,7,7,0,7,7};
		controlerPanel_.setLayout(gridBagLayout);
		controlerPanel_.setBorder(new TitledBorder(null, "Visualization Controls", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Sans", Font.PLAIN, 12), null));
		controlerPanel_.setBackground(Color.WHITE);
		add(controlerPanel_);

		final Component component_8 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_17 = new GridBagConstraints();
		gridBagConstraints_17.gridx = 1;
		gridBagConstraints_17.gridy = 0;
		controlerPanel_.add(component_8, gridBagConstraints_17);

		interactionMode_ = new JComboBox<String>();
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.anchor = GridBagConstraints.WEST;
		gridBagConstraints_2.gridx = 1;
		gridBagConstraints_2.gridy = 1;
		controlerPanel_.add(interactionMode_, gridBagConstraints_2);
		interactionMode_.setModel(new DefaultComboBoxModel<String>(new String[] {"Transforming", "Picking", "Selecting a Seed"}));

		final Component component = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridx = 1;
		controlerPanel_.add(component, gridBagConstraints);

		layoutCombo_ = new JComboBox<String>();
		layoutCombo_.setModel(new DefaultComboBoxModel<String>(new String[] {"Layout"}));
		layoutCombo_.setActionCommand("layoutCombo");
		final GridBagConstraints gridBagConstraints_12 = new GridBagConstraints();
		gridBagConstraints_12.anchor = GridBagConstraints.WEST;
		gridBagConstraints_12.gridy = 3;
		gridBagConstraints_12.gridx = 1;
		controlerPanel_.add(layoutCombo_, gridBagConstraints_12);

		final Component component_3 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_19 = new GridBagConstraints();
		gridBagConstraints_19.gridy = 4;
		gridBagConstraints_19.gridx = 1;
		controlerPanel_.add(component_3, gridBagConstraints_19);

		final JLabel vertexSearchLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.anchor = GridBagConstraints.WEST;
		gridBagConstraints_4.gridy = 5;
		gridBagConstraints_4.gridx = 1;
		controlerPanel_.add(vertexSearchLabel, gridBagConstraints_4);
		vertexSearchLabel.setText("Node search:");

		final Component component_9 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_18 = new GridBagConstraints();
		gridBagConstraints_18.gridy = 6;
		gridBagConstraints_18.gridx = 1;
		controlerPanel_.add(component_9, gridBagConstraints_18);
		
		search_ = new SearchBox();
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.anchor = GridBagConstraints.WEST;
		gridBagConstraints_5.gridy = 7;
		gridBagConstraints_5.gridx = 1;
		controlerPanel_.add(search_, gridBagConstraints_5);
		search_.setColumns(12);

		final Component component_1 = Box.createVerticalStrut(20);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridy = 8;
		gridBagConstraints_1.gridx = 1;
		controlerPanel_.add(component_1, gridBagConstraints_1);

		displayLabels_ = new JCheckBox();
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.anchor = GridBagConstraints.WEST;
		gridBagConstraints_6.gridy = 9;
		gridBagConstraints_6.gridx = 1;
		controlerPanel_.add(displayLabels_, gridBagConstraints_6);
		displayLabels_.setFocusable(false);
		displayLabels_.setBackground(Color.WHITE);
		displayLabels_.setText("Display labels");

		curvedEdges_ = new JCheckBox();
		curvedEdges_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.anchor = GridBagConstraints.WEST;
		gridBagConstraints_7.gridy = 10;
		gridBagConstraints_7.gridx = 1;
		controlerPanel_.add(curvedEdges_, gridBagConstraints_7);
		curvedEdges_.setBackground(Color.WHITE);
		curvedEdges_.setText("Curved edges");

		final Component component_4 = Box.createVerticalStrut(20);
		final GridBagConstraints gridBagConstraints_13 = new GridBagConstraints();
		gridBagConstraints_13.gridy = 11;
		gridBagConstraints_13.gridx = 1;
		controlerPanel_.add(component_4, gridBagConstraints_13);

		final JLabel inhibitoryConnectionsLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.insets = new Insets(0, 0, 0, 5);
		gridBagConstraints_8.anchor = GridBagConstraints.WEST;
		gridBagConstraints_8.gridy = 12;
		gridBagConstraints_8.gridx = 1;
		controlerPanel_.add(inhibitoryConnectionsLabel, gridBagConstraints_8);
		inhibitoryConnectionsLabel.setBackground(Color.WHITE);
		inhibitoryConnectionsLabel.setText("Distinguish signed edges:");

		final Component component_2 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridy = 13;
		gridBagConstraints_3.gridx = 1;
		controlerPanel_.add(component_2, gridBagConstraints_3);

		distinguishByArrowHead_ = new JCheckBox();
		distinguishByArrowHead_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.anchor = GridBagConstraints.WEST;
		gridBagConstraints_9.gridy = 14;
		gridBagConstraints_9.gridx = 1;
		controlerPanel_.add(distinguishByArrowHead_, gridBagConstraints_9);
		distinguishByArrowHead_.setBackground(Color.WHITE);
		distinguishByArrowHead_.setText("by arrow head");

		distinguishByColor_ = new JCheckBox();
		distinguishByColor_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints_10.gridy = 15;
		gridBagConstraints_10.gridx = 1;
		controlerPanel_.add(distinguishByColor_, gridBagConstraints_10);
		distinguishByColor_.setBackground(Color.WHITE);
		distinguishByColor_.setText("by color");

		final Component component_10 = Box.createVerticalStrut(20);
		final GridBagConstraints gridBagConstraints_20 = new GridBagConstraints();
		gridBagConstraints_20.gridy = 16;
		gridBagConstraints_20.gridx = 1;
		controlerPanel_.add(component_10, gridBagConstraints_20);

		final Component component_5 = Box.createHorizontalStrut(10);
		final GridBagConstraints gridBagConstraints_14 = new GridBagConstraints();
		gridBagConstraints_14.gridy = 17;
		gridBagConstraints_14.gridx = 2;
		controlerPanel_.add(component_5, gridBagConstraints_14);

		final Component component_7 = Box.createHorizontalStrut(10);
		final GridBagConstraints gridBagConstraints_16 = new GridBagConstraints();
		gridBagConstraints_16.gridy = 18;
		gridBagConstraints_16.gridx = 0;
		controlerPanel_.add(component_7, gridBagConstraints_16);

		exportButton_ = new JButton();
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.gridy = 18;
		gridBagConstraints_11.gridx = 1;
		controlerPanel_.add(exportButton_, gridBagConstraints_11);
		exportButton_.setBackground(UIManager.getColor("Button.background"));
		exportButton_.setText("<html><center>Export<br>as image</center></html>");

		final Component component_6 = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.gridy = 19;
		gridBagConstraints_15.gridx = 1;
		controlerPanel_.add(component_6, gridBagConstraints_15);
		
		GnwGuiSettings settings = GnwGuiSettings.getInstance();
		exportButton_.setIcon(new ImageIcon(settings.getSnapshotImage()));
	}

	
	// ============================================================================
	// GETTERS AND SETTERS

	public JPanel getVisualControlPanel() { return controlerPanel_; }
	public void setVisualControlPanel(JPanel visualControlPanel_) { controlerPanel_ = visualControlPanel_; }
	
	public JComboBox<String> getInteractionMode() { return interactionMode_; }
	public void setInteractionMode(JComboBox<String> interactionMode) { interactionMode_ = interactionMode; }
	
	public JComboBox<String> getLayoutCombo() { return layoutCombo_; }
	public void setLayoutCombo(JComboBox<String> jcb) { layoutCombo_ = jcb; }
	
	public SearchBox getSearch() { return search_; }
	public void setSearch(SearchBox search) { search_ = search; }

}
