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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * 
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class OptionsWindow extends GenericWindow
{	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	protected JPanel topPanel_;
	protected FlowLayout topPanelLayout_;
	
	protected JPanel bottomPanel_;
	protected FlowLayout bottomPanelLayout_;
	
	protected JButton bExtract_;
	protected JButton bRename_;
	protected JButton bView_;
	protected JButton bExport_;
	protected JButton bDelete_;
	protected JButton bKinetic_;
	protected JButton bDatasets_;
	protected JButton bAnonymize_;

	/** This panel is displayed in the main and contains neither dynamicNetDisplay_ or staticNetDisplay_. */
	protected JPanel mainDisplay_;
	/** Layout of mainDisplay. */
	protected CardLayout mainDisplayLayout_ = new CardLayout();
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(OptionsWindow.class.getName());
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS
	
	public OptionsWindow(Frame aFrame)
	{	
		super(aFrame, true);
		setBounds(100, 100, 500, 340);

		mainDisplay_ = new JPanel();
		mainDisplay_.setLayout(mainDisplayLayout_);
		mainDisplay_.setBackground(Color.WHITE);
		getContentPane().add(mainDisplay_, BorderLayout.CENTER);

		final JPanel oneCard_ = new JPanel();
		oneCard_.setLayout(new BorderLayout());
		oneCard_.setName("oneCard");
		mainDisplay_.add(oneCard_, oneCard_.getName());

		final JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridBagLayout());
		oneCard_.add(panel);

		topPanel_ = new JPanel();
		topPanel_.setBackground(Color.WHITE);
		topPanelLayout_ = new FlowLayout();
		topPanelLayout_.setHgap(10);
		topPanel_.setLayout(topPanelLayout_);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		panel.add(topPanel_, gridBagConstraints);

		bRename_ = new JButton();
		bRename_.setMargin(new Insets(2, 0, 2, 0));
		bRename_.setPreferredSize(new Dimension(100, 80));
		topPanel_.add(bRename_);
		bRename_.setText("<html><center><u>R</u>ename</center></html>");

		bAnonymize_ = new JButton();
		bAnonymize_.setText("<html><center><u>A</u>nonymize<br>gene names</center></html>");
		bAnonymize_.setPreferredSize(new Dimension(100, 80));
		bAnonymize_.setMargin(new Insets(2, 0, 2, 0));
		topPanel_.add(bAnonymize_);
		
		bView_ = new JButton();
		bView_.setMargin(new Insets(2, 0, 2, 0));
		bView_.setPreferredSize(new Dimension(100, 80));
		bView_.setText("<html><u>V</u>iew</html>");
		topPanel_.add(bView_);

		bExtract_ = new JButton();
		bExtract_.setMargin(new Insets(2, 0, 2, 0));
		bExtract_.setPreferredSize(new Dimension(100, 80));
		bExtract_.setText("<html><center><u>E</u>xtract<br>Subnetworks</center></html>");
		topPanel_.add(bExtract_);

		bottomPanel_ = new JPanel();
		bottomPanel_.setBackground(Color.WHITE);
		final FlowLayout flowLayout = new FlowLayout();
		flowLayout.setHgap(10);
		bottomPanel_.setLayout(flowLayout);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridy = 1;
		gridBagConstraints_1.gridx = 0;
		panel.add(bottomPanel_, gridBagConstraints_1);

		bKinetic_ = new JButton();
		bKinetic_.setPreferredSize(new Dimension(100, 80));
		bKinetic_.setMargin(new Insets(2, 0, 2, 0));
		bKinetic_.setText("<html><center>Generate<br><u>K</u>inetic<br>Model</center></html>");
		bottomPanel_.add(bKinetic_);

		bDatasets_ = new JButton();
		bDatasets_.setPreferredSize(new Dimension(100, 80));
		bDatasets_.setMargin(new Insets(2, 0, 2, 0));
		bDatasets_.setText("<html><center>Generate<br><u>D</u>atasets</center></html>");
		bottomPanel_.add(bDatasets_);

		bDelete_ = new JButton();
		bDelete_.setPreferredSize(new Dimension(100, 80));
		bDelete_.setMargin(new Insets(2, 0, 2, 0));
		bDelete_.setText("Delete");
		bottomPanel_.add(bDelete_);

		bExport_ = new JButton();
		bExport_.setMargin(new Insets(2, 0, 2, 0));
		bExport_.setPreferredSize(new Dimension(100, 80));
		bExport_.setText("<html><u>S</u>ave As</html>");
		bottomPanel_.add(bExport_);
		
		mainDisplayLayout_.show(mainDisplay_, oneCard_.getName());
		setLocationRelativeTo(aFrame);
	}
}
