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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;

import ch.epfl.lis.animation.Snake;
import ch.epfl.lis.gnwgui.GnwGuiSettings;
import ch.epfl.lis.gnwgui.windows.GenericWindow;

//import com.jgoodies.forms.factories.DefaultComponentFactory;


/** This dialog is used to extract subnetworks from a network.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class SubnetExtractionWindow extends GenericWindow {
	
	protected JPanel snakePanel_;
	protected JPanel runPanel_;
	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Defines the size of the subnet(s). */
	protected JPanel sizeSubnetPanel_;
	
	/** If checked, all the regulators will be extracted */
	protected JCheckBox extractAllRegulators_;
	/** If checked, use the numRegulators_value  */
	protected JCheckBox useNumRegulators_;
	/** Number of gene regulators in the extracted network */
	protected JSpinner numRegulators_;
	
	/** ButtonGroup1 */
	protected ButtonGroup buttonGroup1_ = new ButtonGroup();
	/** ButtonGroup1: Seed = random node */
	protected JRadioButton randomVertex_;
	/** ButonsGroup1: Seed = node selected in the list */
	protected JRadioButton selectionFromList_;
	
	/** ButtonGroup2 */
	protected ButtonGroup buttonGroup2_ = new ButtonGroup();
	/** ButtonGroup2: Select greedy neighbour selection strategy */
	protected JRadioButton greedy_;
	/** ButtonGroup2: Select random among the top */
	protected JRadioButton randomAmongTop_;
	/** ButtonGroup2: Select N strongly connected components */
	protected JRadioButton stronglyConnected_;
	
	/** Contains the number of strongly connected components to take */
	protected JSpinner numStronglyConnected_;

	/** Root name of the subnet(s). */
	protected JTextField subnetRootName_;
	/** Size in node of the subnet(s). */
	protected JSpinner subnetSize_;
	/** Number of subnet(s) to generate. */
	protected JSpinner numberSubnets_;
	/** List of all the node labels. */
	protected JComboBox<String> listVerticesID_;
	/**
	 * Define the probability to take a random neighbour (100%) versus always take the
	 * best neighbour with the modularity as criterion.
	 */
	protected JSpinner randomAmongTopSpinner_;
	
	/** Main panel of the dialog. */
	protected JPanel centerPanel_;

	/** Run the extraction of subnets process. */
	protected JButton runButton_;
	/** Cancel button */
	protected JButton cancelButton;
	/** Contains the Run and Cancel buttons. */
	protected JPanel actionPanel;
	/** Contains the Run button and the Snake. */
	protected JPanel cards;
	/** Snake displayed during the process (wait item). */
	protected Snake snake_;
	/**
	 * Contains:
	 * - Run button
	 * - Snake
	 */
	protected CardLayout myCardLayout = new CardLayout();
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(SubnetExtractionWindow.class.getName());

    
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor
	 */
	public SubnetExtractionWindow(Frame aFrame) {
		
		super(aFrame, false);
		setSize(580, 510);
		setHeaderTitle("Set Parameters");
		setTitle("Subnetwork Extraction");

		centerPanel_ = new JPanel();
		centerPanel_.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {7,0,7};
		gridBagLayout.rowHeights = new int[] {7,7,7,0,0,0,7,7};
		centerPanel_.setLayout(gridBagLayout);
		getContentPane().add(centerPanel_);

		final Component component_6 = Box.createHorizontalStrut(30);
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.gridy = 0;
		gridBagConstraints_11.gridx = 0;
		centerPanel_.add(component_6, gridBagConstraints_11);

		final JPanel optionsPanel = new JPanel();
		optionsPanel.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[] {0,7};
		gridBagLayout_1.rowHeights = new int[] {0,7,0,7};
		optionsPanel.setLayout(gridBagLayout_1);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 1;
		centerPanel_.add(optionsPanel, gridBagConstraints);

		final JPanel options = new JPanel();
		options.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout_2 = new GridBagLayout();
		gridBagLayout_2.columnWidths = new int[] {0,7,0,7,7};
		gridBagLayout_2.rowHeights = new int[] {7,7,7,7,0,7,7,7,7,7,0,0,0,7};
		options.setLayout(gridBagLayout_2);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.anchor = GridBagConstraints.WEST;
		gridBagConstraints_2.gridy = 0;
		gridBagConstraints_2.gridx = 0;
		optionsPanel.add(options, gridBagConstraints_2);

		final Component component_3 = Box.createVerticalStrut(30);
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.gridx = 2;
		gridBagConstraints_10.gridy = 0;
		options.add(component_3, gridBagConstraints_10);

		final JLabel nameLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_31 = new GridBagConstraints();
		gridBagConstraints_31.anchor = GridBagConstraints.WEST;
		gridBagConstraints_31.gridx = 0;
		gridBagConstraints_31.gridy = 1;
		options.add(nameLabel, gridBagConstraints_31);
		nameLabel.setText("Subnetwork name:");

		subnetRootName_ = new JTextField();
		final GridBagConstraints gridBagConstraints_25 = new GridBagConstraints();
		gridBagConstraints_25.anchor = GridBagConstraints.WEST;
		gridBagConstraints_25.gridwidth = 3;
		gridBagConstraints_25.gridy = 1;
		gridBagConstraints_25.gridx = 2;
		options.add(subnetRootName_, gridBagConstraints_25);
		subnetRootName_.setColumns(25);

		final Component component_1 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_21 = new GridBagConstraints();
		gridBagConstraints_21.gridy = 2;
		gridBagConstraints_21.gridx = 0;
		options.add(component_1, gridBagConstraints_21);

		extractAllRegulators_ = new JCheckBox();
		extractAllRegulators_.setFocusable(false);
		extractAllRegulators_.setBackground(Color.WHITE);
		extractAllRegulators_.setText("Extract all regulators");
		final GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.anchor = GridBagConstraints.WEST;
		gridBagConstraints_8.gridy = 3;
		gridBagConstraints_8.gridx = 0;
		options.add(extractAllRegulators_, gridBagConstraints_8);

		final Component component_15 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_32 = new GridBagConstraints();
		gridBagConstraints_32.gridy = 4;
		gridBagConstraints_32.gridx = 0;
		options.add(component_15, gridBagConstraints_32);

		final JLabel sizeOfSubnetsLabel = new JLabel();
		sizeOfSubnetsLabel.setText("Size of subnetworks:");
		final GridBagConstraints gridBagConstraints_16 = new GridBagConstraints();
		gridBagConstraints_16.gridy = 5;
		gridBagConstraints_16.anchor = GridBagConstraints.WEST;
		options.add(sizeOfSubnetsLabel, gridBagConstraints_16);

		final Component component_10 = Box.createHorizontalStrut(20);
		final GridBagConstraints gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.gridy = 5;
		gridBagConstraints_15.gridx = 1;
		options.add(component_10, gridBagConstraints_15);			

		sizeSubnetPanel_ = new JPanel();
		sizeSubnetPanel_.setFocusable(false);
		sizeSubnetPanel_.setBackground(Color.WHITE);
		final FlowLayout flowLayout = new FlowLayout();
		flowLayout.setHgap(0);
		flowLayout.setVgap(0);
		sizeSubnetPanel_.setLayout(flowLayout);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridwidth = 3;
		gridBagConstraints_1.anchor = GridBagConstraints.WEST;
		gridBagConstraints_1.gridy = 5;
		gridBagConstraints_1.gridx = 2;
		options.add(sizeSubnetPanel_, gridBagConstraints_1);

		subnetSize_ = new JSpinner();
		sizeSubnetPanel_.add(subnetSize_);
		subnetSize_.setBackground(Color.WHITE);

		final Component component_2 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_24 = new GridBagConstraints();
		gridBagConstraints_24.gridy = 6;
		gridBagConstraints_24.gridx = 0;
		options.add(component_2, gridBagConstraints_24);

		useNumRegulators_ = new JCheckBox();
		useNumRegulators_.setFocusable(false);
		useNumRegulators_.setBackground(Color.WHITE);
		useNumRegulators_.setText("Include at least N regulators");
		final GridBagConstraints gridBagConstraints_23 = new GridBagConstraints();
		gridBagConstraints_23.anchor = GridBagConstraints.WEST;
		gridBagConstraints_23.gridy = 7;
		gridBagConstraints_23.gridx = 0;
		options.add(useNumRegulators_, gridBagConstraints_23);

		numRegulators_ = new JSpinner();
		numRegulators_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_26 = new GridBagConstraints();
		gridBagConstraints_26.anchor = GridBagConstraints.WEST;
		gridBagConstraints_26.gridy = 7;
		gridBagConstraints_26.gridx = 2;
		options.add(numRegulators_, gridBagConstraints_26);

		final Component component_11 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_17 = new GridBagConstraints();
		gridBagConstraints_17.gridy = 8;
		gridBagConstraints_17.gridx = 0;
		options.add(component_11, gridBagConstraints_17);

		final JLabel numberOfSubnetsLabel = new JLabel();
		numberOfSubnetsLabel.setText("Number of subnetworks:");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.anchor = GridBagConstraints.WEST;
		gridBagConstraints_3.gridy = 9;
		gridBagConstraints_3.gridx = 0;
		options.add(numberOfSubnetsLabel, gridBagConstraints_3);

		numberSubnets_ = new JSpinner();
//		numberSubnets_.setValue(10);
		final GridBagConstraints gridBagConstraints_13 = new GridBagConstraints();
		gridBagConstraints_13.anchor = GridBagConstraints.WEST;
		gridBagConstraints_13.gridy = 9;
		gridBagConstraints_13.gridx = 2;
		options.add(numberSubnets_, gridBagConstraints_13);

		final Component component_12 = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_18 = new GridBagConstraints();
		gridBagConstraints_18.gridy = 10;
		gridBagConstraints_18.gridx = 0;
		options.add(component_12, gridBagConstraints_18);

		final JLabel seedLabel = new JLabel();
		seedLabel.setText("Seed:");
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.anchor = GridBagConstraints.WEST;
		gridBagConstraints_4.gridy = 11;
		gridBagConstraints_4.gridx = 0;
		options.add(seedLabel, gridBagConstraints_4);

		randomVertex_ = new JRadioButton();
		randomVertex_.setSelected(true);
		randomVertex_.setBackground(Color.WHITE);
		randomVertex_.setFocusable(false);
		buttonGroup2_.add(randomVertex_);
		randomVertex_.setText("Random vertex");
		final GridBagConstraints gridBagConstraints_19 = new GridBagConstraints();
		gridBagConstraints_19.anchor = GridBagConstraints.WEST;
		gridBagConstraints_19.gridy = 11;
		gridBagConstraints_19.gridx = 2;
		options.add(randomVertex_, gridBagConstraints_19);

		final Component component_16 = Box.createVerticalStrut(2);
		final GridBagConstraints gridBagConstraints_34 = new GridBagConstraints();
		gridBagConstraints_34.gridy = 12;
		gridBagConstraints_34.gridx = 2;
		options.add(component_16, gridBagConstraints_34);

		selectionFromList_ = new JRadioButton();
		selectionFromList_.setBackground(Color.WHITE);
		selectionFromList_.setFocusable(false);
		buttonGroup2_.add(selectionFromList_);
		selectionFromList_.setText("Selection from list");
		final GridBagConstraints gridBagConstraints_20 = new GridBagConstraints();
		gridBagConstraints_20.anchor = GridBagConstraints.WEST;
		gridBagConstraints_20.gridy = 13;
		gridBagConstraints_20.gridx = 2;
		options.add(selectionFromList_, gridBagConstraints_20);

		final Component component_8 = Box.createHorizontalStrut(10);
		final GridBagConstraints gridBagConstraints_14 = new GridBagConstraints();
		gridBagConstraints_14.gridy = 13;
		gridBagConstraints_14.gridx = 3;
		options.add(component_8, gridBagConstraints_14);

		listVerticesID_ = new JComboBox<String>();
		listVerticesID_.setModel(new DefaultComboBoxModel<String>(new String[] {"Vertex selection"}));
		final GridBagConstraints gridBagConstraints_22 = new GridBagConstraints();
		gridBagConstraints_22.anchor = GridBagConstraints.WEST;
		gridBagConstraints_22.gridy = 13;
		gridBagConstraints_22.gridx = 4;
		options.add(listVerticesID_, gridBagConstraints_22);

		final Component component = Box.createVerticalStrut(2);
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridy = 14;
		gridBagConstraints_5.gridx = 2;
		options.add(component, gridBagConstraints_5);

		stronglyConnected_ = new JRadioButton();
		buttonGroup2_.add(stronglyConnected_);
		stronglyConnected_.setBackground(Color.WHITE);
		stronglyConnected_.setFocusable(false);
		stronglyConnected_.setText("From strongly connected comp.");
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.anchor = GridBagConstraints.WEST;
		gridBagConstraints_6.gridy = 15;
		gridBagConstraints_6.gridx = 2;
		options.add(stronglyConnected_, gridBagConstraints_6);

		numStronglyConnected_ = new JSpinner();
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.anchor = GridBagConstraints.WEST;
		gridBagConstraints_7.gridy = 15;
		gridBagConstraints_7.gridx = 4;
		options.add(numStronglyConnected_, gridBagConstraints_7);

		final Component component_14 = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_27 = new GridBagConstraints();
		gridBagConstraints_27.gridy = 17;
		gridBagConstraints_27.gridx = 2;
		options.add(component_14, gridBagConstraints_27);

		final JLabel neighborSelectionLabel = new JLabel("Neighbor selection:");//DefaultComponentFactory.getInstance().createLabel("Neighbor selection:");
		final GridBagConstraints gridBagConstraints_28 = new GridBagConstraints();
		gridBagConstraints_28.anchor = GridBagConstraints.WEST;
		gridBagConstraints_28.gridy = 18;
		gridBagConstraints_28.gridx = 0;
		options.add(neighborSelectionLabel, gridBagConstraints_28);

		greedy_ = new JRadioButton();
		greedy_.setSelected(true);
		buttonGroup1_.add(greedy_);
		greedy_.setFocusable(false);
		greedy_.setBackground(Color.WHITE);
		greedy_.setText("Greedy");
		final GridBagConstraints gridBagConstraints_29 = new GridBagConstraints();
		gridBagConstraints_29.anchor = GridBagConstraints.WEST;
		gridBagConstraints_29.gridy = 18;
		gridBagConstraints_29.gridx = 2;
		options.add(greedy_, gridBagConstraints_29);

		final Component component_18 = Box.createVerticalStrut(2);
		final GridBagConstraints gridBagConstraints_36 = new GridBagConstraints();
		gridBagConstraints_36.gridy = 19;
		gridBagConstraints_36.gridx = 2;
		options.add(component_18, gridBagConstraints_36);

		randomAmongTop_ = new JRadioButton();
		buttonGroup1_.add(randomAmongTop_);
		randomAmongTop_.setFocusable(false);
		randomAmongTop_.setBackground(Color.WHITE);
		randomAmongTop_.setText("Random among top (%)");
		final GridBagConstraints gridBagConstraints_30 = new GridBagConstraints();
		gridBagConstraints_30.anchor = GridBagConstraints.WEST;
		gridBagConstraints_30.gridy = 20;
		gridBagConstraints_30.gridx = 2;
		options.add(randomAmongTop_, gridBagConstraints_30);

		randomAmongTopSpinner_ = new JSpinner();
//		final SpinnerNumberModel spinnerNumberModel_7 = new SpinnerNumberModel();
//		spinnerNumberModel_7.setValue(new Integer(20));
//		spinnerNumberModel_7.setStepSize(new Integer(1));
//		spinnerNumberModel_7.setMinimum(new Integer(1));
//		spinnerNumberModel_7.setMaximum(new Integer(100));
//		randomAmongTopSpinner_.setModel(spinnerNumberModel_7);
		final GridBagConstraints gridBagConstraints_33 = new GridBagConstraints();
		gridBagConstraints_33.anchor = GridBagConstraints.WEST;
		gridBagConstraints_33.gridy = 20;
		gridBagConstraints_33.gridx = 4;
		options.add(randomAmongTopSpinner_, gridBagConstraints_33);

		final Component component_7 = Box.createVerticalStrut(20);
		final GridBagConstraints gridBagConstraints_12 = new GridBagConstraints();
		gridBagConstraints_12.gridy = 3;
		gridBagConstraints_12.gridx = 0;
		optionsPanel.add(component_7, gridBagConstraints_12);

		actionPanel = new JPanel();
		actionPanel.setLayout(new GridBagLayout());
		actionPanel.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.ipadx = 40;
		gridBagConstraints_9.gridy = 1;
		gridBagConstraints_9.gridx = 1;
		centerPanel_.add(actionPanel, gridBagConstraints_9);

		cards = new JPanel();
		cards.setLayout(myCardLayout);
		final GridBagConstraints gridBagConstraints_35 = new GridBagConstraints();
		gridBagConstraints_35.ipadx = 20;
		gridBagConstraints_35.ipady = 15;
		gridBagConstraints_35.gridx = 0;
		gridBagConstraints_35.gridy = 0;
		gridBagConstraints_35.insets = new Insets(5, 5, 5, 0);
		actionPanel.add(cards, gridBagConstraints_35);

		runPanel_ = new JPanel();
		runPanel_.setBackground(Color.WHITE);
		runPanel_.setLayout(new BorderLayout());
		runPanel_.setName("runPanel");
		cards.add(runPanel_, runPanel_.getName());

		runButton_ = new JButton();
		runPanel_.add(runButton_);
		runButton_.setMnemonic(KeyEvent.VK_E);
		runButton_.setBackground(UIManager.getColor("Button.background"));
		runButton_.setName("computeButton");
		runButton_.setText("Extract");

		snakePanel_ = new JPanel();
		snakePanel_.setLayout(new BorderLayout());
		snakePanel_.setBackground(Color.WHITE);
		snakePanel_.setName("snakePanel");
		cards.add(snakePanel_, snakePanel_.getName());

		snake_ = new Snake();
		snake_.setLayout(new GridBagLayout());
		snakePanel_.add(snake_);
		snake_.setName("snake_");
		snake_.setBackground(Color.WHITE);

		final Component component_4 = Box.createHorizontalStrut(5);
		final GridBagConstraints gridBagConstraints_37 = new GridBagConstraints();
		gridBagConstraints_37.gridx = 1;
		gridBagConstraints_37.gridy = 0;
		gridBagConstraints_37.insets = new Insets(17, 5, 18, 0);
		actionPanel.add(component_4, gridBagConstraints_37);

		final JPanel cancelPanel = new JPanel();
		cancelPanel.setBackground(Color.WHITE);
		cancelPanel.setLayout(new BorderLayout());
		final GridBagConstraints gridBagConstraints_38 = new GridBagConstraints();
		gridBagConstraints_38.ipadx = 20;
		gridBagConstraints_38.ipady = 15;
		gridBagConstraints_38.gridx = 2;
		gridBagConstraints_38.gridy = 0;
		gridBagConstraints_38.insets = new Insets(5, 5, 5, 5);
		actionPanel.add(cancelPanel, gridBagConstraints_38);

		cancelButton = new JButton();
		cancelPanel.add(cancelButton);
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.setBackground(UIManager.getColor("Button.background"));
		cancelButton.setText("Cancel");

		this.setLocationRelativeTo(GnwGuiSettings.getInstance().getGnwGui().getFrame());
	}
}
