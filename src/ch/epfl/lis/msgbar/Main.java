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
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

/**
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Main
{
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.getContentPane().setFocusCycleRoot(true);
		frame.setSize(new Dimension(0, 0));
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setTitle("MessageBar");
		frame.setForeground(Color.BLACK);
		frame.setBounds(100, 100, 600, 1);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.setLocationRelativeTo(null);
		
		MessageBar msgBar = new MessageBar();
		frame.getContentPane().add(msgBar, BorderLayout.CENTER);
		frame.setVisible(true);

//		ArrayList<String> messages = msgBar.getMessages();
//		messages.add("Message 1");
//		messages.add("Message 2");
//		messages.add("Message 3");
//		messages.add("Message 4");
//		messages.add("Message 5");
		
		msgBar.loadHtmlMessages(MessageBar.class.getResource("rsc/html-messages.txt"));

		msgBar.start();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Print the error message, the stack trace of the exception, and exit */
	public static void error(Exception e, String msg)
	{
		System.err.println("EXCEPTION: " + msg);
		error(e);
	}
	
	// ----------------------------------------------------------------------------

	/** Print the stack trace of the exception and exit */
	public static void error(Exception e)
	{
		e.printStackTrace();
		System.exit(-1);
	}
}
