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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import ch.epfl.lis.gnwgui.GnwGuiSettings;


/**
 * Framework for the GeneNetWeaver dialogs.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class GenericWindow extends JDialog
{
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Header instance */
	protected Header header_;
	/** Can be used to display important informations. */
	protected JPanel infoPanel_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(GenericWindow.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor */
	public GenericWindow(Frame aFrame, boolean modal)
	{	
		super(aFrame, modal);
		
		initialize();
		header_ = new Header();
		header_.setPreferredSize(new Dimension(0, 70));
		getContentPane().add(header_, BorderLayout.NORTH);
		
		// doesn't work with Daniel (Mac OS X), it gives an unresolved compilation problem exception...
		try
		{
			setIconImage(new ImageIcon(GnwGuiSettings.getInstance().getGnwIcon()).getImage());
		}
		catch (NoSuchMethodError e)
		{
			// setIconImage() doesn't exist in Mac OS Java implementation.
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void setHeaderTitle(String title)
	{
		header_.setTitle(title);
	}
	
	// ----------------------------------------------------------------------------
	
	public void setHeaderInfo(String info)
	{
		header_.setInfo(info);
	}

	
	// ============================================================================
	// PRIVATE METHODS
	
	/**
	 * Initialization function. Add keybord shortcuts (ESCAPE, ENTER).
	 * By default, ESCAPE is used to close the window (=cancel) and ENTER closes the
	 * window too (=OK).
	 */
	private void initialize()
	{	
		JRootPane rootPane = this.getRootPane();
		InputMap iMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
		iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");

		ActionMap aMap = rootPane.getActionMap();
		aMap.put("ESCAPE", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				escapeAction();
			}
	 	});
		
		aMap.put("ENTER", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				enterAction();
			}
	 	});
		
		this.addWindowListener(new WindowListener()
		{
			public void windowActivated(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0)
			{
				escapeAction();
			}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}		
		});
	}
	
	
	// ============================================================================
	// PROTECTED METHODS
    
    /** Function called when ESCAPE is pressed. */
	protected void escapeAction()
	{
		dispose();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Function called when ENTER is pressed. */
	protected void enterAction()
	{
		dispose();
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public Header getHeader() { return header_; }
	public JPanel getInfoPanel() { return infoPanel_; }
}
