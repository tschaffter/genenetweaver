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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.GnwConsoleHandler;

/** Window for the GNW console.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class GnwConsoleWindow extends JDialog {

	/** Serialization */
	private static final long serialVersionUID = 1L;

	/** Text editor */
	private JTextPane textPane_;
	/** StyledDocument for the content of the console */
	private StyledDocument doc_ = null;
	/** Style for the content of the console */
	private Style style_ = null;
	
	/** Universal separator, sensible to the OS used. */
	private final static String lineSep = System.getProperty("line.separator");
	
	/** Display current level of message printed */
	private JLabel verboseLevelLabel_;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(GnwConsoleWindow.class.getName());

    
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor
	 */
	public GnwConsoleWindow(Frame aFrame) {
		
		super(aFrame, false);
		setTitle("Console");
		setBounds(0, 0, 580, 460);

		final JPanel southPanel = new JPanel();
		southPanel.setPreferredSize(new Dimension(0, 20));
		southPanel.setLayout(new BorderLayout(0, 0));
		southPanel.setBackground(Color.WHITE);
		getContentPane().add(southPanel, BorderLayout.SOUTH);

		final JLabel helpInfo = new JLabel();
		helpInfo.setText(" Type 'h' for help");
		southPanel.add(helpInfo, BorderLayout.WEST);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.PINK);
		getContentPane().add(scrollPane);

		textPane_ = new JTextPane();
		textPane_.setEditable(false);
		//textPane_.setBackground(Color.BLACK);
		//textPane_.setForeground(Color.WHITE);
		scrollPane.setViewportView(textPane_);
		
		doc_ = (StyledDocument)textPane_.getDocument();
		style_ = doc_.addStyle("consoleStyle", null);
		//textPane_.setContentType("text/html");
		addHelpAction(helpInfo);
		addClearAction(helpInfo);
		printVersionAction(helpInfo);
		quitConsole(helpInfo);

		final JLabel ctrlcToCopyLabel = new JLabel();
		ctrlcToCopyLabel.setBackground(Color.WHITE);
		ctrlcToCopyLabel.setText("Ctrl+C to copy ");
		southPanel.add(ctrlcToCopyLabel, BorderLayout.EAST);

		// TODO: eventually allow the user to change the level of the messages printed in the console
		//final JComponent separator = DefaultComponentFactory.getInstance().createSeparator("|");
		//southPanel.add(separator);

		//verboseLevelLabel_ = DefaultComponentFactory.getInstance().createLabel("Verbose");
		//southPanel.add(verboseLevelLabel_);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Change the level of the messages printed in the console.
	 */
	public void setVerboseLevel(Level l) {
		l = (l == null ? Level.INFO : l);
		GnwConsoleHandler.getInstance().setLevel(l);
		verboseLevelLabel_.setText("Level " + l.toString());
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Shortcut to print the help inside the console.
	 * @param jp JComponent that will respond to the action
	 */
	@SuppressWarnings("serial")
	public void addHelpAction(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke("H");
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "help");
	   jp.getActionMap().put("help", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   printHelp();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Shortcut to clear the content of the console.
	 * @param jp JComponent that will respond to the action
	 */
	@SuppressWarnings("serial")
	public void addClearAction(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke("C");
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "clear");
	   jp.getActionMap().put("clear", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   clearConsole();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Shortcut to print the version of the software
	 * @param jp JComponent that will respond to the action
	 */
	@SuppressWarnings("serial")
	public void printVersionAction(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke("V");
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "versions");
	   jp.getActionMap().put("versions", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   printVersions();
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Shortcut to print the version of the software
	 * @param jp JComponent that will respond to the action
	 */
	@SuppressWarnings("serial")
	public void quitConsole(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke("Q");
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "quit");
	   jp.getActionMap().put("quit", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
			   GnwConsoleHandler.getInstance().displayConsoleWindow(false);
		   }
	   });
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Print the version of the software.
	 */
	public void printVersions() {
		String text = "GeneNetWeaver " + GnwSettings.getInstance().getGnwVersion() + lineSep;
		text += "Java version " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + lineSep;
		text += lineSep;
		printMsg(text);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Print the help.
	 */
	public void printHelp() {

		String text = "Commands:" + lineSep;
		text += "---------------------------" + lineSep;
		text += "c\tClear" + lineSep;
		text += "h\tHelp" + lineSep;
		text += "v\tVersions" + lineSep;
		text += "q\tQuit (hide) console" + lineSep;
		text += lineSep;
		
		printMsg(text);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Clear the content of the console.
	 */
	public void clearConsole() {
		try {
			doc_.remove(0, doc_.getLength());
		} catch (BadLocationException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Use this function to print something inside the console.
	 * @param data Text
	 */
	public void printMsg(String data) {
		
		try {
			doc_.insertString(doc_.getLength(), data, style_);
		} catch (BadLocationException e) {
			System.out.println(e.getMessage());
		}
		textPane_.setCaretPosition(doc_.getLength());
	}
}
