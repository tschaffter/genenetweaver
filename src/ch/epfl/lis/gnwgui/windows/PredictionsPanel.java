package ch.epfl.lis.gnwgui.windows;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import ch.epfl.lis.animation.Snake;
import ch.epfl.lis.utilities.gui.ICFocusPolicy;

import javax.swing.*;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

/**  Class containing all the graphical elements for the predictions analysis
 * 
 * @author Gilles Roulet (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */

public abstract class PredictionsPanel extends JPanel {

	private JCheckBox useBonferroni_;
	private JSpinner significanceLevelSpinner_;
	private JLabel significanceLabel_;
	private static final long serialVersionUID = 1L;

	protected Snake snake_;
	protected JPanel snakePanel_;
	protected JButton runButton_;
	protected JButton cancelButton_;
	protected JButton generateFromFileButton_;
	protected JButton openLastReportButton_;
	protected JPanel runPanel_;
	protected JPanel runCardPanel_;
	protected CardLayout runCardLayout_;
	protected JSpinner maxDivergenceSpinner_;
	protected JTextField processIdEdit_;
	protected JTextField evalAuthor_;
	protected JTextArea note_;
	protected JLabel maxDivergenceOfLabel_;

	/** Tabbed pane for all predictions */
	protected JTabbedPane tabbedPane_;
	protected PredictionTab tab_;

	/** Checkboxes */
	protected JCheckBox openPdfReportCheckBox_;
	protected JCheckBox networkMotifAnalysisCheckBox_;
	protected JCheckBox receiverOperatingCharacteristicCheckBox_;
	protected JCheckBox precisionAndRecallCheckBox_;
	protected JCheckBox plotPrCurvesCheckBox_;
	protected JCheckBox includeROCPRBarPlotsOverview_;
	protected JCheckBox generatePdfReport_;
	protected JCheckBox downloadFigureArchiveCheckBox_;
	
	/** Empty panel when there is no tab */
	protected JPanel emptyPanel = new JPanel();
	
	// ============================================================================
	// PUBLIC

	public PredictionsPanel() {
		super();
		setLayout(new BorderLayout());

		this.setLayout(new BorderLayout());
		this.setName("evaluationPanel");
		this.setBackground(Color.WHITE);

		UIManager.put("TabbedPane.contentBorderInsets", new Insets( 10, 5, 5, 5) );
		tabbedPane_ = new JTabbedPane();
		tabbedPane_.setUI( new MetalTabbedPaneUI());

		tabbedPane_.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane_.setBackground(Color.WHITE);
		tabbedPane_.setFocusable(false);
		add(tabbedPane_);

		final JPanel evalSouthPanel = new JPanel();
		evalSouthPanel.setLayout(new BorderLayout());
		add(evalSouthPanel, BorderLayout.SOUTH);
		evalSouthPanel.setBackground(Color.WHITE);

		final JPanel panel = new JPanel();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0,0,0,0,0,0,0,0,0,0};
		gridBagLayout.rowHeights = new int[] {0,7,7,7,7,7,7,7,7,7};
		panel.setLayout(gridBagLayout);
		evalSouthPanel.add(panel);
		panel.setBackground(Color.WHITE);

		final Component component_2 = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_21 = new GridBagConstraints();
		gridBagConstraints_21.gridy = 0;
		gridBagConstraints_21.gridx = 0;
		panel.add(component_2, gridBagConstraints_21);

		networkMotifAnalysisCheckBox_ = new JCheckBox();
		networkMotifAnalysisCheckBox_.setSelected(true);
		networkMotifAnalysisCheckBox_.setBackground(Color.WHITE);
		networkMotifAnalysisCheckBox_.setText("Network motifs analysis");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.anchor = GridBagConstraints.WEST;
		gridBagConstraints_2.gridwidth = 4;
		gridBagConstraints_2.gridy = 1;
		gridBagConstraints_2.gridx = 0;
		panel.add(networkMotifAnalysisCheckBox_, gridBagConstraints_2);

		final JPanel divergencePanel = new JPanel();
		divergencePanel.setLayout(new BorderLayout());
		divergencePanel.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.fill = GridBagConstraints.BOTH;
		gridBagConstraints_3.anchor = GridBagConstraints.WEST;
		gridBagConstraints_3.gridwidth = 4;
		gridBagConstraints_3.gridy = 2;
		gridBagConstraints_3.gridx = 1;
		panel.add(divergencePanel, gridBagConstraints_3);

		maxDivergenceOfLabel_ = new JLabel("Max value on color scale (divergence):");
		maxDivergenceOfLabel_.setBackground(Color.WHITE);
		maxDivergenceOfLabel_.setToolTipText("<html>Maximum value on the color scale of the divergence of prediction confidence<br>" +
											"for motif edges (this parameter only affects the visualization of the motifs)</html>");
		divergencePanel.add(maxDivergenceOfLabel_);

		maxDivergenceSpinner_ = new JSpinner(new SpinnerNumberModel(0.3,0.05,1.0,0.05));
		maxDivergenceSpinner_.setPreferredSize(new Dimension(60, 22));
		divergencePanel.add(maxDivergenceSpinner_, BorderLayout.EAST);

		final JLabel reportIdLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_12 = new GridBagConstraints();
		gridBagConstraints_12.gridy = 2;
		gridBagConstraints_12.gridx = 6;
		panel.add(reportIdLabel, gridBagConstraints_12);
		reportIdLabel.setText("Report ID:");

		processIdEdit_ = new JTextField();
		processIdEdit_.addKeyListener(new KeyAdapter()
		{
			public void keyTyped(KeyEvent evt)
			{
				if (processIdEdit_.getText().length() >= 30)
					evt.consume();
			}
		});
		processIdEdit_.setColumns(20);
		processIdEdit_.setText("myReport");
		final GridBagConstraints gridBagConstraints_13 = new GridBagConstraints();
		gridBagConstraints_13.fill = GridBagConstraints.BOTH;
		gridBagConstraints_13.weighty = 1;
		gridBagConstraints_13.weightx = 1;
		gridBagConstraints_13.anchor = GridBagConstraints.WEST;
		gridBagConstraints_13.gridy = 2;
		gridBagConstraints_13.gridx = 8;
		panel.add(processIdEdit_, gridBagConstraints_13);
		processIdEdit_.setMinimumSize(new Dimension(200, 22));
		processIdEdit_.setPreferredSize(new Dimension(200, 22));

		final JPanel significanceLevelPanel = new JPanel();
		significanceLevelPanel.setBackground(Color.WHITE);
		significanceLevelPanel.setLayout(new BorderLayout());
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.fill = GridBagConstraints.BOTH;
		gridBagConstraints_5.anchor = GridBagConstraints.WEST;
		gridBagConstraints_5.gridwidth = 4;
		gridBagConstraints_5.gridy = 3;
		gridBagConstraints_5.gridx = 1;
		panel.add(significanceLevelPanel, gridBagConstraints_5);

		significanceLabel_ = new JLabel();
		
		significanceLabel_.setText("Significance level (critical p-value):");
		significanceLabel_.setToolTipText("<html>Divergence of prediction confidence with p-values above this threshold will be marked<br>" +
												"as not significant (this parameter only affects the visualization of the motifs)</html>");
		significanceLevelPanel.add(significanceLabel_);

		significanceLevelSpinner_ = new JSpinner(new SpinnerNumberModel(0.01,0,1,0.01));
		significanceLevelSpinner_.setToolTipText("<html>Divergence of prediction confidence with p-values above this threshold will be marked<br>" +
														"as not significant (this parameter only affects the visualization of the motifs)</html>");
		significanceLevelSpinner_.setPreferredSize(new Dimension(60, 22));
		significanceLevelPanel.add(significanceLevelSpinner_, BorderLayout.EAST);

		useBonferroni_ = new JCheckBox();
		useBonferroni_.setBackground(Color.WHITE);
		useBonferroni_.setSelected(true);
		useBonferroni_.setText("Use Bonferroni correction");
		useBonferroni_.setToolTipText("<html>Use Bonferroni correction for multiple hypothesis testing when evaluating significance of divergence<br>" +
											"of prediction confidence (this parameter only affects the visualization of the motifs)</html>");
		final GridBagConstraints gridBagConstraints_17 = new GridBagConstraints();
		gridBagConstraints_17.anchor = GridBagConstraints.WEST;
		gridBagConstraints_17.gridwidth = 4;
		gridBagConstraints_17.gridy = 4;
		gridBagConstraints_17.gridx = 1;
		panel.add(useBonferroni_, gridBagConstraints_17);

		final Component component_9 = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_24 = new GridBagConstraints();
		gridBagConstraints_24.gridy = 5;
		gridBagConstraints_24.gridx = 0;
		panel.add(component_9, gridBagConstraints_24);

		receiverOperatingCharacteristicCheckBox_ = new JCheckBox();
		receiverOperatingCharacteristicCheckBox_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				updateGui();
			}
		});
		receiverOperatingCharacteristicCheckBox_.setSelected(true);
		receiverOperatingCharacteristicCheckBox_.setBackground(Color.WHITE);
		receiverOperatingCharacteristicCheckBox_.setText("Receiver operating characteristic (ROC)");
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.anchor = GridBagConstraints.WEST;
		gridBagConstraints_4.gridwidth = 6;
		gridBagConstraints_4.gridy = 6;
		gridBagConstraints_4.gridx = 0;
		panel.add(receiverOperatingCharacteristicCheckBox_, gridBagConstraints_4);

		final JLabel noteLabel = new JLabel("Note:");
		final GridBagConstraints gridBagConstraints_16 = new GridBagConstraints();
		gridBagConstraints_16.anchor = GridBagConstraints.WEST;
		gridBagConstraints_16.gridy = 4;
		gridBagConstraints_16.gridx = 6;
		panel.add(noteLabel, gridBagConstraints_16);

		precisionAndRecallCheckBox_ = new JCheckBox();
		precisionAndRecallCheckBox_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				updateGui();
			}
		});
		precisionAndRecallCheckBox_.setSelected(true);
		precisionAndRecallCheckBox_.setBackground(Color.WHITE);
		precisionAndRecallCheckBox_.setText("Precision-recall (PR)");
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.anchor = GridBagConstraints.WEST;
		gridBagConstraints_6.gridwidth = 4;
		gridBagConstraints_6.gridy = 7;
		gridBagConstraints_6.gridx = 0;
		panel.add(precisionAndRecallCheckBox_, gridBagConstraints_6);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(0, 100));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.gridheight = 7;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridx = 6;
		panel.add(scrollPane, gridBagConstraints);

		note_ = new JTextArea();
		note_.addKeyListener(new KeyAdapter()
		{
			public void keyTyped(KeyEvent evt)
			{
				if (note_.getText().length() >= 1000)
					evt.consume();
			}
		});
		note_.setLineWrap(true);
		scrollPane.setViewportView(note_);

		plotPrCurvesCheckBox_ = new JCheckBox();
		plotPrCurvesCheckBox_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				updateGui();
			}
		});
		plotPrCurvesCheckBox_.setSelected(true);
		plotPrCurvesCheckBox_.setBackground(Color.WHITE);
		plotPrCurvesCheckBox_.setText("Plot ROC/PR curves");
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.gridwidth = 4;
		gridBagConstraints_7.anchor = GridBagConstraints.WEST;
		gridBagConstraints_7.gridy = 9;
		gridBagConstraints_7.gridx = 1;
		panel.add(plotPrCurvesCheckBox_, gridBagConstraints_7);

		final JPanel buttonPanel = new JPanel();
		final GridLayout gridLayout = new GridLayout(0, 1);
		gridLayout.setVgap(5);
		buttonPanel.setLayout(gridLayout);
		buttonPanel.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_20 = new GridBagConstraints();
		gridBagConstraints_20.fill = GridBagConstraints.BOTH;
		gridBagConstraints_20.gridheight = 11;
		gridBagConstraints_20.gridy = 1;
		gridBagConstraints_20.gridx = 10;
		panel.add(buttonPanel, gridBagConstraints_20);

		runCardPanel_ = new JPanel();
		buttonPanel.add(runCardPanel_);
		runCardPanel_.setMaximumSize(new Dimension(60, 0));
		runCardPanel_.setPreferredSize(new Dimension(80, 0));
		runCardLayout_ = new CardLayout();
		runCardPanel_.setLayout(runCardLayout_);

		runPanel_ = new JPanel();
		runPanel_.setLayout(new BorderLayout());
		runPanel_.setBackground(Color.WHITE);
		runPanel_.setName("runPanel");
		runCardPanel_.add(runPanel_, runPanel_.getName());

		runButton_ = new JButton();
		runButton_.setName("computeButton");
		runButton_.setMnemonic(KeyEvent.VK_E);
		runButton_.setText("Generate");
		runPanel_.add(runButton_);

		snakePanel_ = new JPanel();
		snakePanel_.setLayout(new BorderLayout());
		snakePanel_.setBackground(Color.WHITE);
		snakePanel_.setName("snakePanel");
		runCardPanel_.add(snakePanel_, snakePanel_.getName());

		snake_ = new Snake();
		snake_.setLayout(new GridBagLayout());
		snake_.setName("snake_");
		snake_.setBackground(Color.WHITE);
		snakePanel_.add(snake_);

		cancelButton_ = new JButton();
		buttonPanel.add(cancelButton_);
		cancelButton_.setMinimumSize(new Dimension(60, 0));
		cancelButton_.setPreferredSize(new Dimension(80, 0));
		cancelButton_.setText("Cancel");

		generateFromFileButton_ = new JButton();
		buttonPanel.add(generateFromFileButton_);
		generateFromFileButton_.setText("Generate from XML");

		openLastReportButton_ = new JButton();
		buttonPanel.add(openLastReportButton_);
		openLastReportButton_.setText("Open last PDF report");
		openLastReportButton_.setEnabled(false);

		generatePdfReport_ = new JCheckBox();
		generatePdfReport_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				updateGui();
			}
		});
		generatePdfReport_.setSelected(true);
		generatePdfReport_.setBackground(Color.WHITE);
		generatePdfReport_.setText("Generate PDF report");
		final GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.anchor = GridBagConstraints.WEST;
		gridBagConstraints_8.gridwidth = 5;
		gridBagConstraints_8.gridy = 8;
		gridBagConstraints_8.gridx = 0;
		panel.add(generatePdfReport_, gridBagConstraints_8);

		final Component component_5 = Box.createHorizontalStrut(30);
		final GridBagConstraints gridBagConstraints_14 = new GridBagConstraints();
		gridBagConstraints_14.gridy = 1;
		gridBagConstraints_14.gridx = 5;
		panel.add(component_5, gridBagConstraints_14);
		final JLabel usernameLabel = new JLabel("Author:");
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.anchor = GridBagConstraints.WEST;
		gridBagConstraints_10.gridy = 1;
		gridBagConstraints_10.gridx = 6;
		panel.add(usernameLabel, gridBagConstraints_10);

		final Component component_6 = Box.createHorizontalStrut(5);
		final GridBagConstraints gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.gridy = 1;
		gridBagConstraints_15.gridx = 7;
		panel.add(component_6, gridBagConstraints_15);

		evalAuthor_ = new JTextField();
		evalAuthor_.addKeyListener(new KeyAdapter()
		{
			public void keyTyped(KeyEvent evt)
			{
				if (evalAuthor_.getText().length() >= 30)
					evt.consume();
			}
		});
		evalAuthor_.setColumns(20);
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.fill = GridBagConstraints.BOTH;
		gridBagConstraints_11.weighty = 1;
		gridBagConstraints_11.weightx = 1;
		gridBagConstraints_11.anchor = GridBagConstraints.WEST;
		gridBagConstraints_11.gridy = 1;
		gridBagConstraints_11.gridx = 8;
		panel.add(evalAuthor_, gridBagConstraints_11);
		evalAuthor_.setPreferredSize(new Dimension(200, 22));
		evalAuthor_.setMinimumSize(new Dimension(200, 22));

		final Component component = Box.createHorizontalStrut(30);
		final GridBagConstraints gridBagConstraints_18 = new GridBagConstraints();
		gridBagConstraints_18.gridy = 1;
		gridBagConstraints_18.gridx = 9;
		panel.add(component, gridBagConstraints_18);

		final JPanel hSpace1 = new JPanel();
		final FlowLayout flowLayout_1 = new FlowLayout();
		flowLayout_1.setVgap(0);
		flowLayout_1.setHgap(0);
		hSpace1.setLayout(flowLayout_1);
		final GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.gridy = 2;
		gridBagConstraints_9.gridx = 0;
		panel.add(hSpace1, gridBagConstraints_9);

		final Component component_4 = Box.createHorizontalStrut(25);
		hSpace1.add(component_4);

		openPdfReportCheckBox_ = new JCheckBox();
		openPdfReportCheckBox_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				updateGui();
			}
		});
		openPdfReportCheckBox_.setSelected(true);
		openPdfReportCheckBox_.setBackground(Color.WHITE);
		openPdfReportCheckBox_.setText("Open PDF report");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridwidth = 5;
		gridBagConstraints_1.anchor = GridBagConstraints.WEST;
		gridBagConstraints_1.gridy = 10;
		gridBagConstraints_1.gridx = 1;
		panel.add(openPdfReportCheckBox_, gridBagConstraints_1);

		downloadFigureArchiveCheckBox_ = new JCheckBox();
		downloadFigureArchiveCheckBox_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				updateGui();
			}
		});
		downloadFigureArchiveCheckBox_.setSelected(false);
		downloadFigureArchiveCheckBox_.setBackground(Color.WHITE);
		downloadFigureArchiveCheckBox_.setText("Download figures");
		final GridBagConstraints gridBagConstraints_22 = new GridBagConstraints();
		gridBagConstraints_22.gridwidth = 4;
		gridBagConstraints_22.anchor = GridBagConstraints.WEST;
		gridBagConstraints_22.gridy = 11;
		gridBagConstraints_22.gridx = 1;
		panel.add(downloadFigureArchiveCheckBox_, gridBagConstraints_22);

		final JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.setOpaque(false);
		add(northPanel, BorderLayout.NORTH);

		// FOCUS
		Component[] compList = { networkMotifAnalysisCheckBox_, maxDivergenceSpinner_, significanceLevelSpinner_, useBonferroni_, receiverOperatingCharacteristicCheckBox_,
				precisionAndRecallCheckBox_, generatePdfReport_, plotPrCurvesCheckBox_, openPdfReportCheckBox_, downloadFigureArchiveCheckBox_,
				evalAuthor_, processIdEdit_, note_, runButton_, cancelButton_, generateFromFileButton_, openLastReportButton_};
		panel.setFocusCycleRoot(true);
		panel.setFocusTraversalPolicy(new ICFocusPolicy(compList));

		networkMotifAnalysisCheckBox_.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				updateGui();
			}	
		});
		
		// Empty panel
		emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.X_AXIS));
		final JLabel notabLabel = new JLabel("Create a new tab by clicking on the \"Add button\" to add a new inference method");
		emptyPanel.add(notabLabel);
		// TODO to connect
		//significanceLabel_.setVisible(true);
		//significanceLevelSpinner_.setVisible(true);
		//useBonferroni_.setVisible(true);
	}

	// ----------------------------------------------------------------------------

	public void updateGui()
	{
		maxDivergenceOfLabel_.setEnabled(networkMotifAnalysisCheckBox_.isSelected());
		maxDivergenceSpinner_.setEnabled(networkMotifAnalysisCheckBox_.isSelected());
		significanceLabel_.setEnabled(networkMotifAnalysisCheckBox_.isSelected());
		significanceLevelSpinner_.setEnabled(networkMotifAnalysisCheckBox_.isSelected());
		useBonferroni_.setEnabled(networkMotifAnalysisCheckBox_.isSelected());
		plotPrCurvesCheckBox_.setEnabled(generatePdfReport_.isSelected() && (receiverOperatingCharacteristicCheckBox_.isSelected() || precisionAndRecallCheckBox_.isSelected()));
		generatePdfReport_.setEnabled(networkMotifAnalysisCheckBox_.isSelected() || receiverOperatingCharacteristicCheckBox_.isSelected() || precisionAndRecallCheckBox_.isSelected());
		openPdfReportCheckBox_.setEnabled(generatePdfReport_.isSelected() && generatePdfReport_.isEnabled());
		downloadFigureArchiveCheckBox_.setEnabled(generatePdfReport_.isSelected() && generatePdfReport_.isEnabled());
		runButton_.setEnabled(networkMotifAnalysisCheckBox_.isSelected() || receiverOperatingCharacteristicCheckBox_.isSelected() || precisionAndRecallCheckBox_.isSelected());
	}

	// ============================================================================
	// INNER CLASSES

	protected class PredictionTab extends JPanel {

		private static final long serialVersionUID = 1L;

		protected JTextField evalPredictionsFolderTextField_;
		protected JButton evalPredictionsFolderBrowse_;	
		protected JButton evalAddPrediction_;
		protected JPanel predictionsListPanel_;
		protected JButton evalReload_;
		protected JButton evalMatch_;

		// ============================================================================
		// PUBLIC

		public PredictionTab() {
			super();
			setBorder(new EmptyBorder(0, 0, 0, 0));

			final BorderLayout borderLayout_1 = new BorderLayout();
			borderLayout_1.setVgap(5);
			borderLayout_1.setHgap(5);
			this.setLayout(borderLayout_1);
			this.setBackground(Color.WHITE);

			final JPanel evalNorthPanel = new JPanel();
			final BorderLayout borderLayout = new BorderLayout();
			borderLayout.setHgap(4);
			evalNorthPanel.setLayout(borderLayout);
			evalNorthPanel.setOpaque(false);
			evalNorthPanel.setBackground(Color.WHITE);
			this.add(evalNorthPanel, BorderLayout.NORTH);

			final JLabel predictionsFolderLabel = new JLabel();
			evalNorthPanel.add(predictionsFolderLabel, BorderLayout.WEST);
			predictionsFolderLabel.setText("Predictions folder:");

			evalPredictionsFolderTextField_ = new JTextField();
			evalNorthPanel.add(evalPredictionsFolderTextField_);
			evalPredictionsFolderTextField_.setPreferredSize(new Dimension(0, 25));
			evalPredictionsFolderTextField_.setText(System.getProperty("user.home"));

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BorderLayout());
			((BorderLayout)buttonPanel.getLayout()).setHgap(5);
			buttonPanel.setOpaque(false);
			evalPredictionsFolderBrowse_ = new JButton();

			evalPredictionsFolderBrowse_.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
				}
			});
			evalPredictionsFolderBrowse_.setText("Browse");
			evalPredictionsFolderBrowse_.setToolTipText("<html>Select the folder containing the network prediction files</html>");

			evalReload_ = new JButton();
			evalReload_.setText("Scan");
			evalReload_.setToolTipText("<html>Reload the network prediction files</html>");
			evalMatch_ = new JButton();
			evalMatch_.setText("Match");
			evalMatch_.setToolTipText("<html>Automate the association between the gold standards and prediction files<br><i>The name of the current tab is determinant for a good match</i></html>");

			buttonPanel.add(evalPredictionsFolderBrowse_, BorderLayout.WEST);
			buttonPanel.add(evalReload_, BorderLayout.CENTER);
			buttonPanel.add(evalMatch_, BorderLayout.EAST);
			evalNorthPanel.add(buttonPanel, BorderLayout.EAST);
			final JPanel evalCenterPanel = new JPanel();
			evalCenterPanel.setLayout(new BorderLayout());
			evalCenterPanel.setBackground(Color.WHITE);
			this.add(evalCenterPanel);

			final JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
			scrollPane.setPreferredSize(new Dimension(0, 175));
			scrollPane.setBackground(Color.WHITE);
			scrollPane.setOpaque(false);
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);
			evalCenterPanel.add(scrollPane, BorderLayout.CENTER);

			final JPanel columnPanel = new JPanel();
			columnPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
			scrollPane.setColumnHeaderView(columnPanel);
			final BorderLayout borderLayout_3 = new BorderLayout();
			borderLayout_3.setHgap(5);
			columnPanel.setLayout(borderLayout_3);
			columnPanel.setMaximumSize(new Dimension(999999, 25));
			//columnPanel.setOpaque(false);
			columnPanel.setBackground(Color.WHITE);

			evalAddPrediction_ = new JButton();
			evalAddPrediction_.setMargin(new Insets(2, 0, 2, 0));
			columnPanel.add(evalAddPrediction_, BorderLayout.WEST);
			evalAddPrediction_.setPreferredSize(new Dimension(25, 25));
			evalAddPrediction_.setMaximumSize(new Dimension(99999, 25));
			evalAddPrediction_.setText("<html><center>+</center></html>");
			evalAddPrediction_.setToolTipText("<html>Add an additional gold standard and network prediction to be evaluated</html>");

			final JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(1, 0));
			panel.setBackground(Color.WHITE);
			columnPanel.add(panel);


			final JLabel GoldStandardsLabel = new JLabel();
			panel.add(GoldStandardsLabel);
			GoldStandardsLabel.setPreferredSize(new Dimension(200, 20));
			GoldStandardsLabel.setText("Gold standards");

			final JLabel PredictionsPanel = new JLabel();
			panel.add(PredictionsPanel);
			PredictionsPanel.setPreferredSize(new Dimension(400, 0));
			PredictionsPanel.setText("Network Predictions");


			predictionsListPanel_ = new JPanel();
			predictionsListPanel_.setBorder(new EmptyBorder(0, 0, 0, 0));
			predictionsListPanel_.setLayout(new BoxLayout(predictionsListPanel_, BoxLayout.Y_AXIS));
			//predictionsListPanel_.setOpaque(false);
			predictionsListPanel_.setBackground(Color.WHITE);

			scrollPane.setViewportView(predictionsListPanel_);
			
			
			
		}

		public JTextField getPredictionsFolderTextField() { return evalPredictionsFolderTextField_;}
		public JButton getAddPrediction() { return evalAddPrediction_;}
		public JPanel getListPanel() { return predictionsListPanel_;}

	}


	// ============================================================================

	/**  JPanel class with 1 remove button and two ComboBox to assign goldstandards and predictions
	 * 
	 * @author Gilles Roulet (firstname.name@gmail.com)
	 */
	protected class GoldStandardPredictionPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		protected JButton removeButton_;
		protected JComboBox<Object> goldStandardsComboBox_;
		protected JComboBox<Object> predictionsComboBox_;

		// ============================================================================
		// PUBLIC

		public GoldStandardPredictionPanel() {

			final BorderLayout borderLayout = new BorderLayout();
			borderLayout.setVgap(3);
			borderLayout.setHgap(5);

			this.setOpaque(false);
			this.setLayout(borderLayout);
			this.setMaximumSize(new Dimension(999999, 25));
			//this.setBackground(Color.WHITE);

			removeButton_ = new JButton();
			removeButton_.setPreferredSize(new Dimension(25, 25));
			removeButton_.setMaximumSize(new Dimension(99999, 25));
			removeButton_.setText("-");
			removeButton_.setToolTipText("<html>Remove this line</html>");
			this.add(removeButton_, BorderLayout.WEST);

			final JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(1, 2));
			add(panel);

			goldStandardsComboBox_ = new JComboBox<Object>();
			panel.add(goldStandardsComboBox_);

			predictionsComboBox_ = new JComboBox<Object>();
			panel.add(predictionsComboBox_);
		}
	}

	// ============================================================================
	// GETTERS AND SETTERS

	public JTabbedPane getTabbedPane() { return tabbedPane_;}

	protected boolean getEvalPR() { return precisionAndRecallCheckBox_.isSelected(); }
	protected boolean getEvalROC() { return receiverOperatingCharacteristicCheckBox_.isSelected(); }
	protected boolean getEvalPlots() { return plotPrCurvesCheckBox_.isSelected(); }
	protected boolean getEvalMotifAnalysis() { return networkMotifAnalysisCheckBox_.isSelected(); }
	protected boolean getEvalBars() { return includeROCPRBarPlotsOverview_.isSelected(); }
	protected boolean getEvalOpen() { return openPdfReportCheckBox_.isSelected(); }
	protected boolean getEvalDownloadFigures() { return downloadFigureArchiveCheckBox_.isSelected(); }
	protected boolean getEvalPDF() { return generatePdfReport_.isSelected(); }
	
	protected String getEvalAuthor() { return evalAuthor_.getText(); }
	protected String getEvalProcessID() { return processIdEdit_.getText(); }
	protected String getEvalNote() { return note_.getText(); }

	protected float getMaxDivergence() { return ((SpinnerNumberModel)maxDivergenceSpinner_.getModel()).getNumber().floatValue(); }
	protected float getSignificanceLevel() { return ((SpinnerNumberModel)significanceLevelSpinner_.getModel()).getNumber().floatValue(); }
	
	protected boolean getUseBonferroni() { return useBonferroni_.isSelected(); }
}