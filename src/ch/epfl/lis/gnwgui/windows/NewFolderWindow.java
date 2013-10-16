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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.epfl.lis.gnwgui.windows.GenericWindow;

/** This dialog is used to rename a network.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class NewFolderWindow extends GenericWindow {

	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Cancel button */
	protected JButton cancelButton;
	/** Apply button */
	protected JButton applyButton;
	
	/** This text field must contain the new name of the network. */
	protected JTextField newName_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(NewFolderWindow.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
    /**
     * Constructor
     * @param aFrame
     */
	public NewFolderWindow(Frame aFrame) {
		
		super(aFrame, true);
		setResizable(false);
		setHeaderTitle("Add a new folder");
		setTitle("New Folder");
		setHeaderInfo("Enter a name for the new folder");
		setBounds(100, 100, 416, 275);

		final JPanel centerPanel = new JPanel();
		centerPanel.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {7,7,7};
		centerPanel.setLayout(gridBagLayout);
		getContentPane().add(centerPanel, BorderLayout.CENTER);

		newName_ = new JTextField();
		newName_.setRequestFocusEnabled(false);
		newName_.setColumns(26);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		centerPanel.add(newName_, gridBagConstraints);

		setLocationRelativeTo(aFrame);

		final Component component = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.gridy = 1;
		gridBagConstraints_2.gridx = 0;
		centerPanel.add(component, gridBagConstraints_2);

		final JPanel actionPanel = new JPanel();
		final GridLayout gridLayout = new GridLayout(1, 0);
		gridLayout.setHgap(10);
		actionPanel.setLayout(gridLayout);
		actionPanel.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_1.gridy = 2;
		gridBagConstraints_1.gridx = 0;
		centerPanel.add(actionPanel, gridBagConstraints_1);

		applyButton = new JButton();
		applyButton.setMargin(new Insets(5, 14, 5, 14));
		actionPanel.add(applyButton);
		applyButton.setMnemonic(KeyEvent.VK_A);
		applyButton.setSelected(true);
		applyButton.setText("Apply");

		cancelButton = new JButton();
		cancelButton.setMargin(new Insets(5, 14, 5, 14));
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.setText("Cancel");
		actionPanel.add(cancelButton);
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS

	public void setNewName(String text) { newName_.setText(text); }
	public String getNewName() { return newName_.getText(); }
}
