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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.Timer;

/**
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class MessageBar extends MessageBarGui implements ActionListener
{
	private Timer mainTimer_;
	private Timer restoreTimer_;
	private Timer hideAfterFirstMessageTimer_;
	
	private ArrayList<String> messages_;
	private int currentMessageIndex_;
	
	private long normalDuration_;
	private long extendedDuration_;
	
	private static final long serialVersionUID = 1L;
	
	// ============================================================================
	// PUBLIC FUNCTIONS

	public MessageBar()
	{
		super();
		initialize();
	}
	
	// ----------------------------------------------------------------------------
	
	private void initialize()
	{
		try
		{
			nextButton_.addActionListener(this);
			backButton_.addActionListener(this);
			downButton_.addActionListener(this);
			upButton_.addActionListener(this);
			
			setNormalDuration(3000);
			setExtendedDuration(15000);
			setHideAfterFirstMessageDuration(2000);
			
			messages_ = new ArrayList<String>();
		}
		catch (Exception e)
		{
			Main.error(e, "Initialization of the message bar failed.");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void nextMessage()
	{	
		if (currentMessageIndex_ == messages_.size() - 1)
			currentMessageIndex_ = 0;
		else
			currentMessageIndex_++;
		
		setMessage(currentMessageIndex_);
	}
	
	// ----------------------------------------------------------------------------
	
	public void previousMessage()
	{
		if (currentMessageIndex_ == 0)
			currentMessageIndex_ = messages_.size() - 1;
		else
			currentMessageIndex_--;
		
		setMessage(currentMessageIndex_);
	}
	
	// ----------------------------------------------------------------------------
	
	public void setMessage(int index)
	{
		currentMessageIndex_ = index;
		messageLabel_.setText(messages_.get(index));
		
		repaint();
	}
	
	// ----------------------------------------------------------------------------
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == backButton_)
		{
			mainTimer_.stop();
			previousMessage();
			restoreTimer_.start();
			hideAfterFirstMessageTimer_.stop();
		}
		else if (e.getSource() == nextButton_)
		{
			mainTimer_.stop();
			nextMessage();
			restoreTimer_.start();
			hideAfterFirstMessageTimer_.stop();
		}
		else if (e.getSource() == restoreTimer_)
		{
			mainTimer_.restart();
		}
		else if (e.getSource() == mainTimer_)
		{
			nextMessage();
		}
		else if (e.getSource() == hideAfterFirstMessageTimer_)
		{
			hideAfterFirstMessage();
		}
		else if (e.getSource() == downButton_)
		{
			hideMessageBar();
			mainTimer_.stop();
			restoreTimer_.stop();
			hideAfterFirstMessageTimer_.stop();
		}
		else if (e.getSource() == upButton_)
		{
			showMessageBar();
			mainTimer_.restart();
			hideAfterFirstMessageTimer_.stop();
		}
    }
	
	// ----------------------------------------------------------------------------
	
	public void hideMessageBar()
	{
		backButton_.setVisible(false);
		messageLabel_.setVisible(false);
		nextButtonCardLayout_.show(nextButtonPanel_, "blank");
		
		cardLayout_.show(cardPanel_, "up");
	}
	
	// ----------------------------------------------------------------------------
	
	public void showMessageBar()
	{
		backButton_.setVisible(true);
		messageLabel_.setVisible(true);
		nextButtonCardLayout_.show(nextButtonPanel_, "next");
		
		cardLayout_.show(cardPanel_, "down");
	}
	
	// ----------------------------------------------------------------------------
	
	public void hideAfterFirstMessage()
	{	
		// fire an action from the downwards button
		downButton_.actionListener.actionPerformed(new ActionEvent(downButton_, 1, "hide bar"));
	}
	
	// ----------------------------------------------------------------------------
	
	public void start()
	{
		setMessage(0);
		mainTimer_.start();
		hideAfterFirstMessageTimer_.start(); // this timer only starts here
	}
	
	// ----------------------------------------------------------------------------
	
	public void stop()
	{
		mainTimer_.stop();
		restoreTimer_.stop();
		hideAfterFirstMessageTimer_.stop();
	}
	
	// ----------------------------------------------------------------------------
	
	public void addHtmlMessage(String text)
	{
		messages_.add("<html>" + text + "</html>");
	}
	
	// ----------------------------------------------------------------------------
	
	public void loadHtmlMessages(URL filename)
	{
		BufferedReader reader = null;
		
		try
		{
			String text = null;
			
			URLConnection con = filename.openConnection();
			con.connect();
			InputStream urlfs = con.getInputStream();
			reader = new BufferedReader(new InputStreamReader(urlfs));
			
			// repeat until all lines is read
			while ((text = reader.readLine()) != null)
			{
				if (!text.equals("") && text.charAt(0) != '#')
					addHtmlMessage(text);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public ArrayList<String> getMessages() { return messages_; }
	
	public void setNormalDuration(long duration)
	{
		normalDuration_ = duration;
		mainTimer_ = new javax.swing.Timer((int)normalDuration_, this);
		mainTimer_.setRepeats(true); // keep firing 
		mainTimer_.setInitialDelay((int)normalDuration_);
	}
	
	public void setExtendedDuration(long duration)
	{
		extendedDuration_ = duration;
		restoreTimer_ = new javax.swing.Timer(100, this); // value 100 doesn't matter
		restoreTimer_.setRepeats(false); // fire once after initial delay
		restoreTimer_.setInitialDelay((int)extendedDuration_);
	}
	
	public void setHideAfterFirstMessageDuration(int duration)
	{
		hideAfterFirstMessageTimer_ = new javax.swing.Timer(100, this);
		hideAfterFirstMessageTimer_.setRepeats(false);
		hideAfterFirstMessageTimer_.setInitialDelay(duration);
	}
}
