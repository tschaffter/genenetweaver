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

import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import ch.epfl.lis.gnwgui.GnwGuiSettings;

/** This dialog allows the user to choice one of the several process available for the networks.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class InternetConnectionDialog {
	
	private boolean accepted_ = false;
	
	// ============================================================================
	// PUBLIC METHODS
	
	public InternetConnectionDialog(Frame frame)
	{	
		ImageIcon icon = new ImageIcon(GnwGuiSettings.getInstance().getConnectionImage());
		
		Object[] options = {"Accept", "Refuse"};

		int n = JOptionPane.showOptionDialog(
				frame,
				"Connecting to the secured GNW server for PDF report generation.\n\n" +
				"Network predictions are evaluated locally and are not being uploaded,\n" +
				"evaluation statistics are uploaded to the secured GNW server for the\n" +
				"sole purpose of generating the PDF report and are immediately deleted\n" +
				"at the end of the process.\n\n" +
				"If you refuse or don't have internet connection, evaluation results will\n" +
				"be saved in a text file (XML format) instead of a PDF report.\n\n",
				"Connection to Internet",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				icon,
			    options,
			    options[0]);

		accepted_ = (n == JOptionPane.YES_OPTION);
	}
	
	public boolean isAccepted() { return accepted_; }
}
