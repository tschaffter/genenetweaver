/*
Copyright (c) 2010 Thomas Schaffter

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

package ch.epfl.lis.msgbar;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import javax.swing.Box;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class MessageBarGui extends JPanel
{
	private JPanel center_;
	protected JLabel messageLabel_;
	
	protected JPanel nextButtonPanel_;
	protected CardLayout nextButtonCardLayout_;
	
	protected JPanel cardPanel_;
	protected CardLayout cardLayout_;
	
	private URL backImgUrl_;
	private URL nextImgUrl_;
	private URL upImgUrl_;
	private URL downImgUrl_;
	
	private URL backClickedImgUrl_;
	private URL nextClickedImgUrl_;
	private URL upClickedImgUrl_;
	private URL downClickedImgUrl_;
	
	protected ImageButton backButton_;
	protected ImageButton nextButton_;
	protected ImageButton upButton_;
	protected ImageButton downButton_;
	
	private static final long serialVersionUID = 1L;

	// ============================================================================
	// PUBLIC FUNCTIONS
	
	public MessageBarGui()
	{
		super();
		setBackground(Color.WHITE);
		
		setLayout(new BorderLayout());

		center_ = new JPanel();
		center_.setLayout(new BorderLayout());
		center_.setBackground(Color.WHITE);
		add(center_);

		messageLabel_ = new JLabel();
		messageLabel_.setForeground(Color.DARK_GRAY);
		messageLabel_.setText("message");
		messageLabel_.setVerticalTextPosition(SwingConstants.BOTTOM);
		messageLabel_.setHorizontalTextPosition(SwingConstants.CENTER);
		messageLabel_.setHorizontalAlignment(SwingConstants.CENTER);
		center_.add(messageLabel_);
		messageLabel_.setBackground(Color.WHITE);

		final JPanel left_ = new JPanel();
		left_.setLayout(new BorderLayout());
		left_.setPreferredSize(new Dimension(100, 0));
		left_.setBackground(Color.WHITE);
		add(left_, BorderLayout.WEST);

		final JPanel right_ = new JPanel();
		right_.setLayout(new BorderLayout());
		right_.setPreferredSize(new Dimension(100, 0));
		right_.setBackground(Color.WHITE);
		add(right_, BorderLayout.EAST);
		
		backImgUrl_ = getClass().getResource("rsc/back-bw-opa60-20px.png");
		nextImgUrl_ = getClass().getResource("rsc/next-bw-opa60-20px.png");
		upImgUrl_ = getClass().getResource("rsc/up-bw-opa60-20px.png");
		downImgUrl_ = getClass().getResource("rsc/down-bw-opa60-20px.png");
		
		backClickedImgUrl_ = getClass().getResource("rsc/back-bw-opa30-20px.png");
		nextClickedImgUrl_ = getClass().getResource("rsc/next-bw-opa30-20px.png");
		upClickedImgUrl_ = getClass().getResource("rsc/up-bw-opa30-20px.png");
		downClickedImgUrl_ = getClass().getResource("rsc/down-bw-opa30-20px.png");

		final JPanel leftSupportPanel = new JPanel();
		leftSupportPanel.setLayout(new BorderLayout());
		leftSupportPanel.setBackground(Color.WHITE);
		left_.add(leftSupportPanel);

		backButton_ = new ImageButton(backImgUrl_, backClickedImgUrl_);
		leftSupportPanel.add(backButton_);
		backButton_.setBackground(Color.WHITE);

		final JPanel rightSupportPanel = new JPanel();
		rightSupportPanel.setLayout(new GridBagLayout());
		rightSupportPanel.setBackground(Color.WHITE);
		right_.add(rightSupportPanel);

		nextButtonPanel_ = new JPanel();
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(0, 5, 0, 5);
		rightSupportPanel.add(nextButtonPanel_, gridBagConstraints);
		nextButtonPanel_.setBackground(Color.WHITE);
		nextButtonCardLayout_ = new CardLayout();
		nextButtonPanel_.setLayout(nextButtonCardLayout_);
		nextButton_ = new ImageButton(nextImgUrl_, nextClickedImgUrl_);
		nextButton_.setName("next");
		nextButtonPanel_.add(nextButton_, nextButton_.getName());
		nextButton_.setBackground(Color.WHITE);

		final Component component = Box.createRigidArea(nextButton_.getPreferredSize());
		component.setName("blank");
		nextButtonPanel_.add(component, component.getName());
		
		cardPanel_ = new JPanel();
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.fill = GridBagConstraints.BOTH;
		gridBagConstraints_1.gridx = 1;
		gridBagConstraints_1.gridy = 0;
		gridBagConstraints_1.insets = new Insets(0, 5, 0, 5);
		rightSupportPanel.add(cardPanel_, gridBagConstraints_1);
		cardPanel_.setBackground(Color.WHITE);
		cardLayout_ = new CardLayout();
		cardPanel_.setLayout(cardLayout_);
		
		downButton_ = new ImageButton(downImgUrl_, downClickedImgUrl_);
		downButton_.setName("down");
		cardPanel_.add(downButton_, downButton_.getName());
		downButton_.setBackground(Color.WHITE);
		
		upButton_ = new ImageButton(upImgUrl_, upClickedImgUrl_);
		upButton_.setName("up");
		cardPanel_.add(upButton_, upButton_.getName());
		upButton_.setBackground(Color.WHITE);
		
		cardLayout_.show(cardPanel_, "down");
	}
}
