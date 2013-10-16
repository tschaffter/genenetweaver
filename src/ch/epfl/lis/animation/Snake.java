/*
Copyright (c) 2008-2010 Thomas Schaffter

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

package ch.epfl.lis.animation;

import java.awt.*;

import javax.swing.JPanel;

/**  This object is a user waiting item representing a snake turning in circles.
 * 
 * The waiting item is composed of a given number of bullets that represent the road.
 * The snake is a chain of bullets (snake length < road length) with an opaque head color
 * and body's bullets set with the head color but with transparency (alpha) descreasing.
 * Radius of the road as well as radius of bullets or snake's speed are configurable.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class Snake extends JPanel implements Runnable
{
	/** Serial */
	private static final long serialVersionUID = 1L;
	
	/** Snake thread */
	private Thread myThread_;
	/** Sleep time between two paints */
	private int sleep_;
	/** Radius of the snake loop */
	private float R_;
	/** Radius of the bullets that compose the snake */
	private float r_;
	/** Number of bullets composing the loop */
	private int totalNumBullets_;
	/** Number of bullets composing the snake (< num loop bullets) */
	private int snakeNumBullets_;
	/** Current position of the snake relative to the loop */
	private int snakePosition_;
	
	/** The color of the snake head */
	private Color snakeHeadColor_;
	/** Transparency range between the snake's head and tail (0 to 100 percent) */
	private int deltaTransparencyFromHeadToTail_;
	/** Snake motion clockwise or anti-clockwise */
	private boolean clockwise_;
	
	/** List of transparency values for all the bullets of the loop */
	private int[] snakeBulletsTransparency_;

	// ============================================================================
	// PUBLIC METHODS

	public Snake()
	{	
		super();
		init();
	}
	
	// ----------------------------------------------------------------------------
	
	public void init()
	{	
		myThread_ = null;
		sleep_ = 90;
		R_ = 15f;
		r_ = 2.5f;
		totalNumBullets_ = 12;
		snakeNumBullets_ = 8;
		snakePosition_ = 0;
		// Snake's head: dark gray
		snakeHeadColor_ = new Color(80, 80, 80);
		// Snake's head: opaque
		// Snake's tail: alpha=20%
		deltaTransparencyFromHeadToTail_ = 80;
		clockwise_ = true;
		snakeBulletsTransparency_ = new int[totalNumBullets_];
		// Current transparency: alpha = 100%
		double currentTransp = 100;
		// The range of transparency defines by usedTransRange_ is used for all the
		// length_ bullets of the snake's body.
		double usedTransp = deltaTransparencyFromHeadToTail_ / snakeNumBullets_;
		
		for (int i=0; i < totalNumBullets_; i++)
		{	
			snakeBulletsTransparency_[i] = (int)((currentTransp * 255) / 100);
			// If we are always in the snake's body, we decrease alpha
			if (i < snakeNumBullets_)
				currentTransp -= usedTransp;
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void start()
	{	
		if (myThread_ == null)
		{
			myThread_ = new Thread(this);
			myThread_.start();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void stop()
	{	
		myThread_ = null;
	}
	
	// ----------------------------------------------------------------------------
	
	public void run()
	{	
		Thread thisThread = Thread.currentThread();
		while (myThread_ == thisThread)
		{	
			repaint();	
			try
			{
				Thread.sleep(sleep_);
			}
			catch (InterruptedException ie)
			{
				return;
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void update(Graphics g)
	{	
		paint(g);
	}
	
	// ----------------------------------------------------------------------------

	public void paint(Graphics g)
	{	
		paintComponent(g);
	}
	
	// ----------------------------------------------------------------------------
	
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		// Antialiazing
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Angle between two bullets of the loop
		double oneAngle = 2*Math.PI/totalNumBullets_;
		int x, y, alpha, size;
		
		// to center the snake in the middle of the panel
		int w = (int) (2 * (R_ + r_));
		int h = w;		
		int x0 = (getWidth() - w) / 2;
		int y0 = (getHeight() - h) / 2;

		// Draw each bullets of the loop
		for (int i=0; i < totalNumBullets_; i++) {

			// Get alpha of the current bullet
			alpha = snakeBulletsTransparency_[(i+snakePosition_) % totalNumBullets_];
			g2.setColor(new Color(snakeHeadColor_.getRed(), snakeHeadColor_.getGreen(), snakeHeadColor_.getBlue(), alpha));
			
			// Calculate the coordinates (x,y)
			if (clockwise_) {
				x = (int)(R_ * Math.cos(i * -oneAngle) + R_ + x0);
				y = (int)(R_ * Math.sin(i * -oneAngle) + R_ + y0);	
			}
			else {
				x = (int)(R_ * Math.cos(i * oneAngle) + R_ + x0);
				y = (int)(R_ * Math.sin(i * oneAngle) + R_ + y0);
			}
			
			// Diameter of the bullet
			size = (int)(2 * r_);
			
			g2.fillOval(x, y, size, size);
		}
		
		// Set the state of the snake relative to the loop
		snakePosition_ = (snakePosition_ + 1) % totalNumBullets_;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setSleep(int value) { sleep_ = value; }
	public int getSleep() { return sleep_; }
	public void setR(float value) { R_ = value; }
	public float getR() { return R_; }
	public void setr(float value) { r_ = value; }
	public float getr() { return r_;}
	public void setTotalNumBullets(int value) { totalNumBullets_ = value; }
	public int getTotalNumBullets() { return totalNumBullets_; }
	public void setSnakeNumBullets(int value) { snakeNumBullets_ = value; }
	public int getSnakeNumBullets() { return snakeNumBullets_; }
	public void setSnakePosition(int value) { snakePosition_ = value % totalNumBullets_; }
	public int getSnakePostion() { return snakePosition_; }
	public void setSnakeHeadColor(Color c) { snakeHeadColor_ = c; }
	public Color getSnakeHeadColor() { return snakeHeadColor_; }
	public void setClockwise(boolean sense) { clockwise_ = sense; }
	public boolean getClockwise() { return clockwise_; }
	public String getName() { return "snake"; }
}