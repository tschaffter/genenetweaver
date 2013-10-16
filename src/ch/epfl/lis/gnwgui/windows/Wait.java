package ch.epfl.lis.gnwgui.windows;

import java.awt.Color;
import java.awt.Frame;

import ch.epfl.lis.animation.Snake;
import ch.epfl.lis.gnwgui.GnwGuiSettings;

public class Wait extends GenericWindow
{
	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Snake */
	protected Snake snake_;
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS

	public Wait(Frame frame, boolean modal)
	{
		super(frame, modal);
		
		setSize(250, 250);
		
		header_.setTitle("Please wait");
		setHeaderInfo("See console for details");
		
		snake_ = new Snake();
		snake_.setR(2 * snake_.getR());
		snake_.setr(2 * snake_.getr());
		
		snake_.setBackground(Color.WHITE);
		getContentPane().add(snake_);
		
		setAlwaysOnTop(true);
        setResizable(false);
        setLocationRelativeTo(GnwGuiSettings.getInstance().getGnwGui().getFrame());
	}
	
	// ----------------------------------------------------------------------------
	
	public void start()
	{
		snake_.start();
		setVisible(true);
	}
	
	// ----------------------------------------------------------------------------
	
	public void stop()
	{
		snake_.stop();
		setVisible(false);
	}
}
