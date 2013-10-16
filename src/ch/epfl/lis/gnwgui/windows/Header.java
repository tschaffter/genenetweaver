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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/** Header of the GNW windows.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class Header extends JPanel {
	
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Title label */
	private JLabel title_ = null;
	/** Information label display below the title label. */
	private JLabel info_ = null;
	/** Upper color of the header background. */
	private Color top_ = null;
	/** Lower color of the header background. */
	private Color bottom_ = null;
	/** List of colors for top_ */
	private ArrayList<Color> topList_ = null;
	/** List of colors for bottom_ */
	private ArrayList<Color> bottomList_ = null;
	
	/** Blue background gradient mode */
	public static final int BLUE   = 0;
	/** Orange background gradient mode */
	public static final int ORANGE = 1;
	/** Green background gradient mode */
	public static final int GREEN  = 2;
	/** Thesis theme */
	public static final int THESIS = 3;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(Header.class.getName());
	
    // =======================================================================================
    // PUBLIC METHODS
	//
	
	/**
	 * Default constructor
	 */
	public Header() {
		
		topList_ = new ArrayList<Color>();
		bottomList_ = new ArrayList<Color>();
		
		topList_.add(new Color(0, 108, 170)); // Light blue
		topList_.add(new Color(252, 236, 0)); // Light orange
		topList_.add(new Color(148, 234, 49)); // Light green
//		topList_.add(new Color(52, 96, 155)); // Thesis theme: Dark blue
		topList_.add(new Color(54, 98, 157)); // Thesis theme: Dark blue
		
		bottomList_.add(new Color(8, 64, 115)); // Dark blue
		bottomList_.add(new Color(255, 157, 0)); // Dark orange
		bottomList_.add(new Color(66, 137, 8)); // Dark green
//		bottomList_.add(new Color(69, 116, 180)); // Thesis theme: Light blue
		bottomList_.add(new Color(73, 120, 178)); // Thesis theme: Light blue
		
		setBackgroundGetup(THESIS);
		
		title_ = new JLabel("Title");
		info_ = new JLabel("Information");
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(0, 70));
		
		title_.setBorder(new EmptyBorder(10, 15, 0, 0));
		title_.setForeground(Color.WHITE);
		title_.setBackground(UIManager.getColor("Button.background"));
		title_.setFont(new Font("Sans", Font.BOLD, 20));
		add(title_, BorderLayout.NORTH);

		info_.setBorder(new EmptyBorder(0, 15, 0, 0));
		info_.setForeground(Color.WHITE);
		info_.setFont(new Font("Sans", Font.BOLD, 14));
		add(info_, BorderLayout.WEST);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This method paints the content of the header.
	 * @param g Graphics instance
	 */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		Paint pt = g2.getPaint();
		g2.setPaint( new GradientPaint(this.getSize().width, 0, top_, this.getSize().width, this.getSize().height, bottom_));
		g2.fillRoundRect(0, 0, this.getSize().width, this.getSize().height, 0, 0);
        g2.setPaint(pt);
	}
    
    
	// ----------------------------------------------------------------------------
    
    /**
     * Set the background color by selection one of the saved color gradient.
     * @param index
     */
    public void setBackgroundGetup(int index) {
    	top_ = topList_.get(index);
    	bottom_ = bottomList_.get(index);
    }
    
    
    // =======================================================================================
    // GETTERS AND SETTERS
    //
    
    public void setTopColor(Color color) { top_ = color; }
    public void setBottomColor(Color color) { bottom_ = color; }
    
    public void setTitle(String title) { title_.setText(title); }
    public void setInfo(String info) { info_.setText(info); }
    
    public JLabel getTitle() { return title_; }
    public JLabel getInfo() { return info_; }
}
