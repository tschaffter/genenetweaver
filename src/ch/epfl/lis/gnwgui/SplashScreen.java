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

package ch.epfl.lis.gnwgui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

/**
 * Splashscreen
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class SplashScreen extends JWindow {

	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Parent frame */
	private Frame parent_;
	/** Progress bar */
	private JProgressBar progressBar_;
	/** Display a short text to indicates which task is currently done. */
	private JLabel taskId_;
	/** Contains the image of the splash screen. */
	private JPanel splash_;
	/** Splash screen image. */
	private Image splashImage_;
	/** If true, the user can hide the splash screen by a simple click over it. */
	private boolean hiddenAfterClick_;
	
	/** Normal mode, progress bar functional. */
	public static Byte NORMAL = 0;
	/** In this mode, the time taken by all the tasks during a normal load is computed. */
	public static Byte TIME_EVALUATION = 1;
	/** Mode used. */
	private Byte mode_;
	
	/** Values that the progress bar must take. */
	private int[] progressValues_ = {4, 4, 10, 39, 51, 73, 81, 84, 100};
	/** The state is the index of the task processed during the loading. */
	private int progressIndex_ = 0;

	/** Used in TIME_EVALUATION mode to compute the fraction of each tasks run. */
	private ArrayList<Long> time = new ArrayList<Long>();
	
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(SplashScreen.class.getName());

    
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Constructor
	 * @param f Frame
	 * @param filename URL to the image displayed as background of the splash screen.
	 * @param mode Mode
	 * @param displayTaskId Set to true to display some information about the tasks.
	 * @param displayBar Set to true to display the progress bar.
	 */
	@SuppressWarnings("serial")
	public SplashScreen(final Frame f, URL filename, Byte mode, 
			boolean displayTaskId, boolean displayBar, boolean hiddenAfterClick){
	
		super(f);
		parent_ = f;
		mode_ = mode;
		hiddenAfterClick_ = hiddenAfterClick;
		
		// The problem with trying to use getToolkit.getImage is that it returns even before
		// the image is loaded and therefore you get -1 as width and height, using the
		// MediaTracker to load our image.
		splashImage_ = getImageFile(filename);
		splashImage_.flush();
		splash_ = new JPanel() {
			// Paint the given image as background of the panel.
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Antialiazing
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2.drawImage(splashImage_, 0, 0, this);
		    }
		};
		splash_.setLayout(new GridBagLayout());
		int splashWidth = splashImage_.getWidth(null);
		int splashHeight = splashImage_.getHeight(null);
		splash_.setPreferredSize(new Dimension(splashWidth, splashHeight));
		
		// Progress bar
		progressBar_ = new JProgressBar();
		progressBar_.setBorderPainted(true);
		progressBar_.setMaximum(100);
		
		// Information about the tasks
		taskId_ = new JLabel();
		taskId_.setFont(new Font("Sans", Font.BOLD, 12));
		taskId_.setForeground(Color.WHITE);
		taskId_.setText("Loading");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints.insets = new Insets(290, 0, 0, 0);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		
		if (displayTaskId) {
			splash_.add(taskId_, gridBagConstraints);
		}
		getContentPane().add(splash_);
		if (displayBar) {
			getContentPane().add(progressBar_, BorderLayout.SOUTH);
		}
		pack();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width/2 -  (splashWidth/2),
				screenSize.height/2 - (splashHeight/2));
		
		// In TIME_EVALUATION mode, the chronometer starts now
		if (mode.equals(TIME_EVALUATION))
			time.add(System.currentTimeMillis());
		
		// Allow the user to hide the splash screen by clicking on it
		addMouseListener( new MouseAdapter() {
			public void mousePressed( MouseEvent e ) {
				if(SplashScreen.this.hiddenAfterClick_ && SplashScreen.this.isVisible()) {
					setVisible(false);
					dispose();
				}
			}
		});
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set the text to display (task information)
	 * @param id
	 */
	public void setTaskInfo(String id) {
		taskId_.setText(id);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Call this function at the end of each task done during the loading.
	 */
	public void stepDone() {
		if (mode_.equals(NORMAL)) {
			// Go to the next state of the progress bar.
			progressBar_.setValue(progressValues_[progressIndex_]);
			progressIndex_++;
			
			if (progressIndex_ == progressValues_.length)
				try {
					// Wait 0.2s to let the time to the user to see the progress bar
					// filled at 100%.
					Thread.sleep(200);
				} catch (InterruptedException e) {
					
				}
		}
		else if (mode_.equals(TIME_EVALUATION)) {
			// Save the time taken to do the last task.
			time.add(System.currentTimeMillis());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return an Image object from the given path to this image.
	 * @param filename
	 * @return Output image
	 */
	public Image getImageFile(URL filename) {
	   MediaTracker tracker = new MediaTracker(this);
	   Image openedImage = getToolkit().getImage(filename);
	   tracker.addImage(openedImage,0);
	   try {
		   tracker.waitForID(0);
	   } catch (InterruptedException ie) {
		   log_.log(Level.WARNING, "Media Tracker unable to wait.", ie);
	   }
	   return openedImage;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Show/hide the splash screen
	 */
	public void setVisible(boolean b) {
		super.setVisible(b);
		if(!b)
			dispose();
		
		if (!b && mode_.equals(TIME_EVALUATION)) {
			finalizeTimeEvaluation();
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Print the value of the variable progressValues_. There is just to
	 * copy/past it above and replace its actual value.
	 */
	public void finalizeTimeEvaluation() {
		
		long totalTime = time.get(time.size()-1) - time.get(0);
		
		// Compute the time taken by each task.
		for (int i=0; i < time.size()-1; i++)
			time.set(i, time.get(i+1) - time.get(i));
		// Remove the last value of the table
		time.remove(time.size()-1);
		
		int[] steps = new int[time.size()];
		
		// Compute the fraction of time necessary for each task.
		for (int i=0; i < time.size(); i++) {
			steps[i] = (int) (100 * ((double) time.get(i))/totalTime);
			if (i > 0)
				steps[i] += steps[i-1];
		}
		// Set to 100% the last value of the progress bar.
		steps[steps.length-1] = 100;
		
		String variableStr = "private int[] progressValues_ = {";
		for (int i=0; i < time.size(); i++) {
			variableStr += steps[i];
			if (i != time.size()-1)
				variableStr += ", ";
		}
		variableStr += "};";
		
		log_.log(Level.INFO, variableStr);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Display the splash screen for a fixed time and then disappear.
	 * @param waitTime
	 */
	public void showSplash(int waitTime){
		
		final int pause = waitTime;
		
		final Runnable closerRunner = new Runnable(){
			public void run(){
				setVisible(false);
				dispose();
				parent_.setVisible(true);
			}
		};
		
		Runnable waitRunner = new Runnable(){
			public void run(){
				try{
					for (int i=1; i<=10; i++){
						progressBar_.setValue(i*10);
						progressBar_.setString(i*10 + " %");
						Thread.sleep(pause);
					}
					SwingUtilities.invokeAndWait(closerRunner);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		
		setVisible(true);
		Thread splashThread = new Thread(waitRunner, "SplashThread");
		splashThread.start();
	}
}
