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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import ch.epfl.lis.animation.Snake;
import ch.epfl.lis.gnwgui.GnwGuiSettings;
import ch.epfl.lis.gnwgui.windows.GenericWindow;


/** This dialog handles all the simulations parameters of steady-states and time-series experiments.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class SimulationWindow extends GenericWindow {
	
	protected JPanel runPanel_;
	protected JPanel snakePanel_;
	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	protected JButton dream4Settings_;
	protected JCheckBox normalizeNoise_;
	protected JRadioButton noNoise_;
	protected JCheckBox wtSS_;
	protected JCheckBox addLogNormalNoise_;
	protected JCheckBox addGaussianNoise_;
	protected JRadioButton useLogNormalNoise_;
	protected JRadioButton useMicroarrayNoise_;
	protected JRadioButton perturbationLoad_;
	protected JRadioButton perturbationNew_;
	protected JCheckBox timeSeriesAsDream4_;
	protected JCheckBox dualKnockoutTS_;
	protected JCheckBox multifactorialTS_;
	protected JCheckBox knockdownTS_;
	protected JCheckBox knockoutTS_;
	protected JCheckBox dualKnockoutSS_;
	protected JCheckBox multifactorialSS_;
	protected JSpinner logNormalNoise_;
	protected JSpinner numTimeSeries_;
	protected JCheckBox knockdownSS_;
	protected JCheckBox knockoutSS_;
	protected JComboBox<String> model_;
	protected JSpinner tmax_;
	protected JSpinner sdeDiffusionCoeff_;
	protected JButton browse_;
	
	protected JLabel perturbationsLabel_;
	protected JLabel numPointsPerSeriesLabel_;
	protected JLabel durationOfSeriesLabel_;
	protected JLabel numTimeSeriesLabel_;
	
	/** Contains the user path used to export the data. */
	protected JTextField userPath_;
	/** Contains the STD of the level of noise. */
	protected JSpinner gaussianNoise_;
	/** Number of points for each time series. */
	protected JSpinner numPointsPerTimeSeries_;
	
	/** Used to launch the benchmarks generation process. */
	protected JButton runButton_;
	/** Cancel button*/
	protected JButton cancelButton_;
	/** Contains the Run and Cancel button */
	protected JPanel validationPanel_;
	/**
	 * Contains:
	 * - Run button
	 * - Snake
	 */
	protected JPanel runButtonAndSnakePanel_;
	/** Display instead of the run button during process (wait). */
	protected Snake snake_;
	/** Main panel of the dialog */
	protected JPanel centerPanel_;
	/** Layout of the main panel of the dialog. */
	protected final CardLayout myCardLayout_ = new CardLayout();
	
	private ButtonGroup perturbationGroup = new ButtonGroup();
	private ButtonGroup noiseTypeGroup = new ButtonGroup();

    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(SimulationWindow.class.getName());

    
	// ============================================================================
	// PUBLIC METHODS
    
	/**
	 * Constructor
	 */
	public SimulationWindow(Frame aFrame) {
		
		super(aFrame, false);
		
		setSize(640, 810);
		setHeaderTitle("Set Parameters");
		setTitle("Generate Datasets");

		centerPanel_ = new JPanel();
		centerPanel_.setBorder(new EmptyBorder(20, 0, 0, 0));
		centerPanel_.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {7,0,7};
		gridBagLayout.rowHeights = new int[] {7,7,7,0,0,0,7,7};
		centerPanel_.setLayout(gridBagLayout);
		getContentPane().add(centerPanel_);

		final Component component_7 = Box.createVerticalGlue();
		final GridBagConstraints gridBagConstraints_12 = new GridBagConstraints();
		gridBagConstraints_12.gridx = 0;
		gridBagConstraints_12.gridy = 0;
		centerPanel_.add(component_7, gridBagConstraints_12);

		final JPanel optionsPanel = new JPanel();
		optionsPanel.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[] {0,7};
		gridBagLayout_1.rowHeights = new int[] {0,7,0,7};
		optionsPanel.setLayout(gridBagLayout_1);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 1;
		centerPanel_.add(optionsPanel, gridBagConstraints);

		final JPanel options = new JPanel();
		options.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout_2 = new GridBagLayout();
		gridBagLayout_2.columnWidths = new int[] {7,7,7,7,7,7,7,7,7,0,0,0,7,7,0,0,0,7,0,7};
		gridBagLayout_2.rowHeights = new int[] {7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,0,7,7,0,7,0,0,7,0,0,0,0,0,0,7};
		options.setLayout(gridBagLayout_2);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.anchor = GridBagConstraints.EAST;
		gridBagConstraints_2.gridy = 0;
		gridBagConstraints_2.gridx = 0;
		optionsPanel.add(options, gridBagConstraints_2);

		final JLabel label1 = new JLabel();
		label1.setText("Model");
		final GridBagConstraints gridBagConstraints_35 = new GridBagConstraints();
		gridBagConstraints_35.gridx = 0;
		gridBagConstraints_35.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints_35.gridwidth = 5;
		gridBagConstraints_35.gridy = 0;
		options.add(label1, gridBagConstraints_35);

		dream4Settings_ = new JButton();
		dream4Settings_.setText("<html>DREAM4<br>settings</html>");
		final GridBagConstraints gridBagConstraints_38 = new GridBagConstraints();
		gridBagConstraints_38.ipadx = 30;
		gridBagConstraints_38.ipady = 5;
		gridBagConstraints_38.gridheight = 4;
		gridBagConstraints_38.anchor = GridBagConstraints.SOUTHEAST;
		gridBagConstraints_38.gridwidth = 2;
		gridBagConstraints_38.gridy = 0;
		gridBagConstraints_38.gridx = 6;
		options.add(dream4Settings_, gridBagConstraints_38);

		final Component component = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.gridy = 1;
		gridBagConstraints_10.gridx = 0;
		options.add(component, gridBagConstraints_10);

		model_ = new JComboBox<String>();
		model_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_36 = new GridBagConstraints();
		gridBagConstraints_36.gridwidth = 5;
		gridBagConstraints_36.anchor = GridBagConstraints.WEST;
		gridBagConstraints_36.gridy = 2;
		gridBagConstraints_36.gridx = 1;
		options.add(model_, gridBagConstraints_36);
		
		final Component component_12 = Box.createHorizontalStrut(20);
		final GridBagConstraints gridBagConstraints_102 = new GridBagConstraints();
		gridBagConstraints_102.gridy = 2;
		gridBagConstraints_102.gridx = 0;
		options.add(component_12, gridBagConstraints_102);

		final Component component_1 = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.gridy = 3;
		gridBagConstraints_11.gridx = 0;
		options.add(component_1, gridBagConstraints_11);

		final JLabel label2 = new JLabel();
		final GridBagConstraints gridBagConstraints_31 = new GridBagConstraints();
		gridBagConstraints_31.gridx = 0;
		gridBagConstraints_31.gridwidth = 10;
		gridBagConstraints_31.anchor = GridBagConstraints.WEST;
		gridBagConstraints_31.gridy = 4;
		options.add(label2, gridBagConstraints_31);
		label2.setText("Experiments");

		final Component component_2 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_13 = new GridBagConstraints();
		gridBagConstraints_13.gridy = 5;
		gridBagConstraints_13.gridx = 0;
		options.add(component_2, gridBagConstraints_13);

		final JLabel label7 = new JLabel();
		label7.setText("Steady state");
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.gridy = 6;
		gridBagConstraints_6.gridx = 4;
		options.add(label7, gridBagConstraints_6);

		final JLabel label8 = new JLabel();
		label8.setText("Time series");
		final GridBagConstraints gridBagConstraints_46 = new GridBagConstraints();
		gridBagConstraints_46.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints_46.gridy = 6;
		gridBagConstraints_46.gridx = 5;
		options.add(label8, gridBagConstraints_46);

		final JLabel wildtypeLabel = new JLabel();
		wildtypeLabel.setText("Wild-type");
		final GridBagConstraints gridBagConstraints_22 = new GridBagConstraints();
		gridBagConstraints_22.gridwidth = 3;
		gridBagConstraints_22.anchor = GridBagConstraints.WEST;
		gridBagConstraints_22.gridy = 7;
		gridBagConstraints_22.gridx = 1;
		options.add(wildtypeLabel, gridBagConstraints_22);

		wtSS_ = new JCheckBox();
		wtSS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_32 = new GridBagConstraints();
		gridBagConstraints_32.gridy = 7;
		gridBagConstraints_32.gridx = 4;
		options.add(wtSS_, gridBagConstraints_32);

		final JLabel label3 = new JLabel();
		label3.setBackground(Color.WHITE);
		label3.setText("Knockout");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridwidth = 3;
		gridBagConstraints_1.anchor = GridBagConstraints.WEST;
		gridBagConstraints_1.gridy = 8;
		gridBagConstraints_1.gridx = 1;
		options.add(label3, gridBagConstraints_1);

		knockoutSS_ = new JCheckBox();
		knockoutSS_.setFocusable(false);
		knockoutSS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_33 = new GridBagConstraints();
		gridBagConstraints_33.gridy = 8;
		gridBagConstraints_33.gridx = 4;
		options.add(knockoutSS_, gridBagConstraints_33);

		knockoutTS_ = new JCheckBox();
		knockoutTS_.setFocusable(false);
		knockoutTS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_47 = new GridBagConstraints();
		gridBagConstraints_47.gridy = 8;
		gridBagConstraints_47.gridx = 5;
		options.add(knockoutTS_, gridBagConstraints_47);

		final JLabel label4 = new JLabel();
		label4.setBackground(Color.WHITE);
		label4.setText("Knockdowns");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridwidth = 3;
		gridBagConstraints_3.anchor = GridBagConstraints.WEST;
		gridBagConstraints_3.gridy = 9;
		gridBagConstraints_3.gridx = 1;
		options.add(label4, gridBagConstraints_3);

		knockdownTS_ = new JCheckBox();
		knockdownTS_.setFocusable(false);
		knockdownTS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_48 = new GridBagConstraints();
		gridBagConstraints_48.gridy = 9;
		gridBagConstraints_48.gridx = 5;
		options.add(knockdownTS_, gridBagConstraints_48);
		//gridBagConstraints_6.ipadx = 5;

		knockdownSS_ = new JCheckBox();
		knockdownSS_.setFocusable(false);
		knockdownSS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_43 = new GridBagConstraints();
		gridBagConstraints_43.gridy = 9;
		gridBagConstraints_43.gridx = 4;
		options.add(knockdownSS_, gridBagConstraints_43);

		final JLabel label5 = new JLabel();
		label5.setBackground(Color.WHITE);
		label5.setText("Multifactorial");
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridwidth = 3;
		gridBagConstraints_5.anchor = GridBagConstraints.WEST;
		gridBagConstraints_5.gridy = 10;
		gridBagConstraints_5.gridx = 1;
		options.add(label5, gridBagConstraints_5);

		multifactorialSS_ = new JCheckBox();
		multifactorialSS_.setFocusable(false);
		multifactorialSS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_44 = new GridBagConstraints();
		gridBagConstraints_44.gridy = 10;
		gridBagConstraints_44.gridx = 4;
		options.add(multifactorialSS_, gridBagConstraints_44);

		dualKnockoutSS_ = new JCheckBox();
		dualKnockoutSS_.setFocusable(false);
		dualKnockoutSS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_45 = new GridBagConstraints();
		gridBagConstraints_45.gridy = 11;
		gridBagConstraints_45.gridx = 4;
		options.add(dualKnockoutSS_, gridBagConstraints_45);

		dualKnockoutTS_ = new JCheckBox();
		dualKnockoutTS_.setFocusable(false);
		dualKnockoutTS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_50 = new GridBagConstraints();
		gridBagConstraints_50.gridy = 11;
		gridBagConstraints_50.gridx = 5;
		options.add(dualKnockoutTS_, gridBagConstraints_50);

		multifactorialTS_ = new JCheckBox();
		multifactorialTS_.setFocusable(false);
		multifactorialTS_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_49 = new GridBagConstraints();
		gridBagConstraints_49.gridy = 10;
		gridBagConstraints_49.gridx = 5;
		options.add(multifactorialTS_, gridBagConstraints_49);

		final JLabel label6 = new JLabel();
		label6.setText("Dual knockouts");
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.gridwidth = 3;
		gridBagConstraints_4.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints_4.anchor = GridBagConstraints.WEST;
		gridBagConstraints_4.gridy = 11;
		gridBagConstraints_4.gridx = 1;
		options.add(label6, gridBagConstraints_4);

		final Component component_3 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_14 = new GridBagConstraints();
		gridBagConstraints_14.gridy = 12;
		gridBagConstraints_14.gridx = 0;
		options.add(component_3, gridBagConstraints_14);

		timeSeriesAsDream4_ = new JCheckBox();
		timeSeriesAsDream4_.setFocusable(false);
		timeSeriesAsDream4_.setText("Time series as in DREAM4 (perturbation removed after t_max/2)");
		timeSeriesAsDream4_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_51 = new GridBagConstraints();
		gridBagConstraints_51.gridwidth = 9;
		gridBagConstraints_51.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints_51.anchor = GridBagConstraints.WEST;
		gridBagConstraints_51.gridy = 13;
		gridBagConstraints_51.gridx = 1;
		options.add(timeSeriesAsDream4_, gridBagConstraints_51);

		numTimeSeriesLabel_ = new JLabel();
		numTimeSeriesLabel_.setText("Number of time series");
		final GridBagConstraints gridBagConstraints_52 = new GridBagConstraints();
		gridBagConstraints_52.gridwidth = 4;
		gridBagConstraints_52.anchor = GridBagConstraints.WEST;
		gridBagConstraints_52.gridy = 14;
		gridBagConstraints_52.gridx = 2;
		options.add(numTimeSeriesLabel_, gridBagConstraints_52);
		
		final Component component_13 = Box.createHorizontalStrut(30);
		final GridBagConstraints gridBagConstraints_100 = new GridBagConstraints();
		gridBagConstraints_100.gridy = 14;
		gridBagConstraints_100.gridx = 1;
		options.add(component_13, gridBagConstraints_100);

		numTimeSeries_ = new JSpinner();
		numTimeSeries_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_53 = new GridBagConstraints();
		gridBagConstraints_53.ipadx = 14;
		gridBagConstraints_53.anchor = GridBagConstraints.EAST;
		gridBagConstraints_53.gridy = 14;
		gridBagConstraints_53.gridx = 7;
		options.add(numTimeSeries_, gridBagConstraints_53);

		final Component component_5 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.gridy = 15;
		gridBagConstraints_15.gridx = 0;
		options.add(component_5, gridBagConstraints_15);

		durationOfSeriesLabel_ = new JLabel();
		durationOfSeriesLabel_.setText("Duration of each time series (t_max)");
		final GridBagConstraints gridBagConstraints_28 = new GridBagConstraints();
		gridBagConstraints_28.gridwidth = 5;
		gridBagConstraints_28.anchor = GridBagConstraints.WEST;
		gridBagConstraints_28.gridy = 16;
		gridBagConstraints_28.gridx = 1;
		options.add(durationOfSeriesLabel_, gridBagConstraints_28);

		tmax_ = new JSpinner();
		tmax_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_30 = new GridBagConstraints();
		gridBagConstraints_30.ipadx = -30;
		gridBagConstraints_30.anchor = GridBagConstraints.EAST;
		gridBagConstraints_30.gridy = 16;
		gridBagConstraints_30.gridx = 7;
		options.add(tmax_, gridBagConstraints_30);

		numPointsPerSeriesLabel_ = new JLabel();
		numPointsPerSeriesLabel_.setText("Number of measured points per time series");
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.gridwidth = 5;
		gridBagConstraints_7.anchor = GridBagConstraints.WEST;
		gridBagConstraints_7.gridy = 17;
		gridBagConstraints_7.gridx = 1;
		options.add(numPointsPerSeriesLabel_, gridBagConstraints_7);

		numPointsPerTimeSeries_ = new JSpinner();
		numPointsPerTimeSeries_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.ipadx = -30;
		gridBagConstraints_8.anchor = GridBagConstraints.EAST;
		gridBagConstraints_8.gridy = 17;
		gridBagConstraints_8.gridx = 7;
		options.add(numPointsPerTimeSeries_, gridBagConstraints_8);

		final Component component_6 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_16 = new GridBagConstraints();
		gridBagConstraints_16.gridy = 18;
		gridBagConstraints_16.gridx = 0;
		options.add(component_6, gridBagConstraints_16);

		perturbationsLabel_ = new JLabel();
		perturbationsLabel_.setText("Perturbations for multifactorial, dual knockouts, and DREAM4 time series");
		final GridBagConstraints gridBagConstraints_54 = new GridBagConstraints();
		gridBagConstraints_54.anchor = GridBagConstraints.WEST;
		gridBagConstraints_54.gridwidth = 9;
		gridBagConstraints_54.gridy = 19;
		gridBagConstraints_54.gridx = 1;
		options.add(perturbationsLabel_, gridBagConstraints_54);

		perturbationNew_ = new JRadioButton();
		perturbationNew_.setFocusable(false);
		perturbationGroup.add(perturbationNew_);
		perturbationNew_.setBackground(Color.WHITE);
		perturbationNew_.setText("Generate new");
		final GridBagConstraints gridBagConstraints_55 = new GridBagConstraints();
		gridBagConstraints_55.gridwidth = 8;
		gridBagConstraints_55.anchor = GridBagConstraints.WEST;
		gridBagConstraints_55.gridy = 20;
		gridBagConstraints_55.gridx = 2;
		options.add(perturbationNew_, gridBagConstraints_55);

		perturbationLoad_ = new JRadioButton();
		perturbationLoad_.setFocusable(false);
		perturbationGroup.add(perturbationLoad_);
		perturbationLoad_.setBackground(Color.WHITE);
		perturbationLoad_.setText("Load from files");
		final GridBagConstraints gridBagConstraints_56 = new GridBagConstraints();
		gridBagConstraints_56.gridwidth = 8;
		gridBagConstraints_56.anchor = GridBagConstraints.WEST;
		gridBagConstraints_56.gridy = 21;
		gridBagConstraints_56.gridx = 2;
		options.add(perturbationLoad_, gridBagConstraints_56);

		final Component component_8 = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_20 = new GridBagConstraints();
		gridBagConstraints_20.gridy = 22;
		gridBagConstraints_20.gridx = 0;
		options.add(component_8, gridBagConstraints_20);

		final JLabel label13 = new JLabel();
		label13.setText("Noise");
		final GridBagConstraints gridBagConstraints_17 = new GridBagConstraints();
		gridBagConstraints_17.gridx = 0;
		gridBagConstraints_17.gridwidth = 10;
		gridBagConstraints_17.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints_17.gridy = 23;
		options.add(label13, gridBagConstraints_17);

		final Component component_9 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_23 = new GridBagConstraints();
		gridBagConstraints_23.gridy = 24;
		gridBagConstraints_23.gridx = 0;
		options.add(component_9, gridBagConstraints_23);

		final JLabel label14 = new JLabel();
		label14.setBackground(Color.WHITE);
		label14.setText("Noise in the dynamics of the networks (SDEs)");
		final GridBagConstraints gridBagConstraints_40 = new GridBagConstraints();
		gridBagConstraints_40.gridwidth = 9;
		gridBagConstraints_40.anchor = GridBagConstraints.WEST;
		gridBagConstraints_40.gridy = 25;
		gridBagConstraints_40.gridx = 1;
		options.add(label14, gridBagConstraints_40);

		final JLabel label15 = new JLabel();
		label15.setText("Coefficient of noise term");
		final GridBagConstraints gridBagConstraints_57 = new GridBagConstraints();
		gridBagConstraints_57.gridwidth = 4;
		gridBagConstraints_57.anchor = GridBagConstraints.WEST;
		gridBagConstraints_57.gridy = 26;
		gridBagConstraints_57.gridx = 2;
		options.add(label15, gridBagConstraints_57);

		sdeDiffusionCoeff_ = new JSpinner();
		sdeDiffusionCoeff_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_42 = new GridBagConstraints();
		gridBagConstraints_42.ipadx = 20;
		gridBagConstraints_42.anchor = GridBagConstraints.EAST;
		gridBagConstraints_42.gridy = 26;
		gridBagConstraints_42.gridx = 7;
		options.add(sdeDiffusionCoeff_, gridBagConstraints_42);

		final Component component_10 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_25 = new GridBagConstraints();
		gridBagConstraints_25.gridy = 27;
		gridBagConstraints_25.gridx = 0;
		options.add(component_10, gridBagConstraints_25);

		final JLabel label16 = new JLabel();
		label16.setText("Noise added after the simulation (measurement error)");
		final GridBagConstraints gridBagConstraints_58 = new GridBagConstraints();
		gridBagConstraints_58.anchor = GridBagConstraints.WEST;
		gridBagConstraints_58.gridwidth = 9;
		gridBagConstraints_58.gridy = 28;
		gridBagConstraints_58.gridx = 1;
		options.add(label16, gridBagConstraints_58);

		noNoise_ = new JRadioButton();
		noNoise_.setFocusable(false);
		noiseTypeGroup.add(noNoise_);
		noNoise_.setBackground(Color.WHITE);
		noNoise_.setText("None");
		final GridBagConstraints gridBagConstraints_34 = new GridBagConstraints();
		gridBagConstraints_34.anchor = GridBagConstraints.WEST;
		gridBagConstraints_34.gridwidth = 8;
		gridBagConstraints_34.gridy = 29;
		gridBagConstraints_34.gridx = 2;
		options.add(noNoise_, gridBagConstraints_34);

		useMicroarrayNoise_ = new JRadioButton();
		useMicroarrayNoise_.setFocusable(false);
		noiseTypeGroup.add(useMicroarrayNoise_);
		useMicroarrayNoise_.setBackground(Color.WHITE);
		useMicroarrayNoise_.setText("Model of noise in microarrays (used for DREAM4)");
		final GridBagConstraints gridBagConstraints_59 = new GridBagConstraints();
		gridBagConstraints_59.gridwidth = 8;
		gridBagConstraints_59.anchor = GridBagConstraints.WEST;
		gridBagConstraints_59.gridy = 30;
		gridBagConstraints_59.gridx = 2;
		options.add(useMicroarrayNoise_, gridBagConstraints_59);

		useLogNormalNoise_ = new JRadioButton();
		useLogNormalNoise_.setFocusable(false);
		noiseTypeGroup.add(useLogNormalNoise_);
		useLogNormalNoise_.setBackground(Color.WHITE);
		useLogNormalNoise_.setText("Add normal and/or log-normal noise");
		final GridBagConstraints gridBagConstraints_60 = new GridBagConstraints();
		gridBagConstraints_60.gridwidth = 8;
		gridBagConstraints_60.anchor = GridBagConstraints.WEST;
		gridBagConstraints_60.gridy = 31;
		gridBagConstraints_60.gridx = 2;
		options.add(useLogNormalNoise_, gridBagConstraints_60);

		addGaussianNoise_ = new JCheckBox();
		addGaussianNoise_.setFocusable(false);
		addGaussianNoise_.setBackground(Color.WHITE);
		addGaussianNoise_.setText("Add Gaussian noise with standard dev.");
		final GridBagConstraints gridBagConstraints_61 = new GridBagConstraints();
		gridBagConstraints_61.gridwidth = 4;
		gridBagConstraints_61.anchor = GridBagConstraints.WEST;
		gridBagConstraints_61.gridy = 32;
		gridBagConstraints_61.gridx = 3;
		options.add(addGaussianNoise_, gridBagConstraints_61);
		
		final Component component_14 = Box.createHorizontalStrut(30);
		final GridBagConstraints gridBagConstraints_101 = new GridBagConstraints();
		gridBagConstraints_101.gridy = 32;
		gridBagConstraints_101.gridx = 2;
		options.add(component_14, gridBagConstraints_101);

		gaussianNoise_ = new JSpinner();
		gaussianNoise_.setFocusable(false);
		
		final GridBagConstraints gridBagConstraints_21 = new GridBagConstraints();
		gridBagConstraints_21.ipadx = 20;
		gridBagConstraints_21.anchor = GridBagConstraints.EAST;
		gridBagConstraints_21.gridy = 32;
		gridBagConstraints_21.gridx = 7;
		options.add(gaussianNoise_, gridBagConstraints_21);

		addLogNormalNoise_ = new JCheckBox();
		addLogNormalNoise_.setFocusable(false);
		addLogNormalNoise_.setBackground(Color.WHITE);
		addLogNormalNoise_.setText("Add log-normal noise with standard dev.");
		final GridBagConstraints gridBagConstraints_18 = new GridBagConstraints();
		gridBagConstraints_18.gridwidth = 4;
		gridBagConstraints_18.anchor = GridBagConstraints.WEST;
		gridBagConstraints_18.gridy = 33;
		gridBagConstraints_18.gridx = 3;
		options.add(addLogNormalNoise_, gridBagConstraints_18);

		logNormalNoise_ = new JSpinner();
		logNormalNoise_.setFocusable(false);
		final GridBagConstraints gridBagConstraints_19 = new GridBagConstraints();
		gridBagConstraints_19.ipadx = 20;
		gridBagConstraints_19.anchor = GridBagConstraints.EAST;
		gridBagConstraints_19.gridy = 33;
		gridBagConstraints_19.gridx = 7;
		options.add(logNormalNoise_, gridBagConstraints_19);

		normalizeNoise_ = new JCheckBox();
		normalizeNoise_.setFocusable(false);
		normalizeNoise_.setBackground(Color.WHITE);
		normalizeNoise_.setText("Normalize after adding noise (as in DREAM4)");
		final GridBagConstraints gridBagConstraints_37 = new GridBagConstraints();
		gridBagConstraints_37.anchor = GridBagConstraints.WEST;
		gridBagConstraints_37.gridwidth = 8;
		gridBagConstraints_37.gridy = 34;
		gridBagConstraints_37.gridx = 2;
		options.add(normalizeNoise_, gridBagConstraints_37);

		final Component component_11 = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_29 = new GridBagConstraints();
		gridBagConstraints_29.gridy = 35;
		gridBagConstraints_29.gridx = 0;
		options.add(component_11, gridBagConstraints_29);

		final JLabel label17 = new JLabel();
		label17.setText("Output directory where the benchmark (dataset + network files) will be saved:");
		final GridBagConstraints gridBagConstraints_24 = new GridBagConstraints();
		gridBagConstraints_24.gridx = 1;
		gridBagConstraints_24.anchor = GridBagConstraints.WEST;
		gridBagConstraints_24.gridwidth = 9;
		gridBagConstraints_24.gridy = 36;
		options.add(label17, gridBagConstraints_24);

		userPath_ = new JTextField();
		userPath_.setBackground(Color.WHITE);
		userPath_.setEditable(false);
		userPath_.setColumns(30);
		final GridBagConstraints gridBagConstraints_26 = new GridBagConstraints();
		gridBagConstraints_26.fill = GridBagConstraints.BOTH;
		gridBagConstraints_26.ipadx = 335;
		gridBagConstraints_26.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints_26.gridwidth = 6;
		gridBagConstraints_26.anchor = GridBagConstraints.WEST;
		gridBagConstraints_26.gridy = 37;
		gridBagConstraints_26.gridx = 1;
		options.add(userPath_, gridBagConstraints_26);

		browse_ = new JButton();
		browse_.setText("Browse");
		final GridBagConstraints gridBagConstraints_27 = new GridBagConstraints();
		gridBagConstraints_27.anchor = GridBagConstraints.EAST;
		gridBagConstraints_27.gridy = 37;
		gridBagConstraints_27.gridx = 7;
		options.add(browse_, gridBagConstraints_27);

		validationPanel_ = new JPanel();
		validationPanel_.setLayout(new GridBagLayout());
		validationPanel_.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.ipadx = 40;
		gridBagConstraints_9.gridy = 2;
		gridBagConstraints_9.gridx = 1;
		centerPanel_.add(validationPanel_, gridBagConstraints_9);
		
		runButtonAndSnakePanel_ = new JPanel();
		runButtonAndSnakePanel_.setLayout(myCardLayout_);
		final GridBagConstraints gridBagConstraints_39 = new GridBagConstraints();
		gridBagConstraints_39.ipady = 15;
		gridBagConstraints_39.ipadx = 20;
		gridBagConstraints_39.gridx = 0;
		gridBagConstraints_39.gridy = 0;
		gridBagConstraints_39.insets = new Insets(5, 0, 5, 0);
		validationPanel_.add(runButtonAndSnakePanel_, gridBagConstraints_39);

		runPanel_ = new JPanel();
		runPanel_.setBackground(Color.WHITE);
		runPanel_.setLayout(new BorderLayout());
		runPanel_.setName("runPanel");
		runButtonAndSnakePanel_.add(runPanel_, runPanel_.getName());

		runButton_ = new JButton();
		runPanel_.add(runButton_);
		runButton_.setMnemonic(KeyEvent.VK_R);
		runButton_.setBackground(UIManager.getColor("Button.background"));
		runButton_.setName("computeButton");
		runButton_.setText("Simulate");

		snakePanel_ = new JPanel();
		snakePanel_.setLayout(new BorderLayout());
		snakePanel_.setName("snakePanel");
		runButtonAndSnakePanel_.add(snakePanel_, snakePanel_.getName());
		
		snake_ = new Snake();
		snakePanel_.add(snake_);
		snake_.setName("snake_");
		snake_.setBackground(Color.WHITE);

		final Component component_4 = Box.createHorizontalStrut(5);
		final GridBagConstraints gridBagConstraints_41 = new GridBagConstraints();
		gridBagConstraints_41.gridx = 1;
		gridBagConstraints_41.gridy = 0;
		gridBagConstraints_41.insets = new Insets(17, 5, 18, 0);
		validationPanel_.add(component_4, gridBagConstraints_41);

		final JPanel cancelPanel = new JPanel();
		cancelPanel.setBackground(Color.WHITE);
		cancelPanel.setLayout(new BorderLayout());
		final GridBagConstraints gridBagConstraints_62 = new GridBagConstraints();
		gridBagConstraints_62.ipady = 15;
		gridBagConstraints_62.ipadx = 20;
		validationPanel_.add(cancelPanel, gridBagConstraints_62);

		cancelButton_ = new JButton();
		cancelPanel.add(cancelButton_);
		cancelButton_.setMnemonic(KeyEvent.VK_C);
		cancelButton_.setBackground(UIManager.getColor("Button.background"));
		cancelButton_.setText("Cancel");

		setLocationRelativeTo(GnwGuiSettings.getInstance().getGnwGui().getFrame());
		
//		GnwGuiSettings settings = GnwGuiSettings.getInstance();
//		runButton_.setIcon(new ImageIcon(settings.getApplyIconPath()));
//		cancelButton.setIcon(new ImageIcon(settings.getCancelIconPath()));
	}
}
