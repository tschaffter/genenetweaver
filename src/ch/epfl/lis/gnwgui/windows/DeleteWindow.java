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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;


/**
 * This dialog allows the user to delete multiple networks at once.
 * 
 * @author Gilles Roulet (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class DeleteWindow extends GenericWindow
{	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** This panel is displayed in the main */
	protected JPanel mainDisplay_;
	/** Panel containing the buttons */
	protected JPanel buttonsPanel_ = new JPanel();
	/** Delete button */
	protected JButton deleteButton_ = new JButton();
	/** Cancel button */
	protected JButton cancelButton_ = new JButton();
	/** Selected network(s) list */
	protected JList<String> networkList_;
	protected JScrollPane listScrollPane_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(DeleteWindow.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor */
	public DeleteWindow(Frame aFrame)
	{	
		super(aFrame, true);
		setHeaderInfo("The following network(s) will be deleted");
		setHeaderTitle("Delete network(s)");
		setBounds(100, 100, 420, 380);

		mainDisplay_ = new JPanel();
		final BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap(10);
		borderLayout.setHgap(10);
		mainDisplay_.setLayout(borderLayout);
		mainDisplay_.setBackground(Color.WHITE);
		getContentPane().add(mainDisplay_, BorderLayout.CENTER);

		buttonsPanel_ = new JPanel();
		final FlowLayout flowLayout = new FlowLayout();
		flowLayout.setVgap(10);
		flowLayout.setHgap(10);
		buttonsPanel_.setLayout(flowLayout);
		buttonsPanel_.setBackground(Color.WHITE);
		mainDisplay_.add(buttonsPanel_, BorderLayout.SOUTH);

		deleteButton_ = new JButton();
		deleteButton_.setMargin(new Insets(5, 30, 5, 30));
		deleteButton_.setText("Delete");
		buttonsPanel_.add(deleteButton_);

		cancelButton_ = new JButton();
		cancelButton_.setMargin(new Insets(5, 30, 5, 30));
		cancelButton_.setText("Cancel");
		buttonsPanel_.add(cancelButton_);

		listScrollPane_ = new JScrollPane();
		listScrollPane_.setFocusable(false);
		listScrollPane_.setBorder(new EmptyBorder(0, 0, 0, 0));
		listScrollPane_.setBackground(Color.WHITE);
		mainDisplay_.add(listScrollPane_);
		
		setLocationRelativeTo(aFrame);
	}
}
