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

import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import ch.epfl.lis.gnwgui.windows.GnwConsoleWindow;


/** Handler of the GNW console loggers.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class GnwConsoleHandler extends Handler {

	/** the window to which the logging is done */
	private GnwConsoleWindow window = null;

	/** Singleton instance */
	private static GnwConsoleHandler handler = null;
	
    /** Logger for this class */
    private static Logger log_ = Logger.getLogger(GnwConsoleHandler.class.getName());
	
	
	// ============================================================================
	// PUBLIC METHODS
    
	/**
	 * If the 'display' is true, displays the console window
	 * @param display
	 */
	public void displayConsoleWindow(boolean display) {
		window.setVisible(display);
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public GnwConsoleHandler() {
		if (window == null) {
			JFrame f = new JFrame();
			try {
				f.setIconImage(new ImageIcon(GnwGuiSettings.getInstance().getGnwIcon()).getImage());
			} catch (NoSuchMethodError e) {
				// setIconImage() doesn't exist in Mac OS Java implementation.
			}
			window = new GnwConsoleWindow(f);
		}
		configure();
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * The getInstance method returns the singleton instance of the
	 * WindowHandler object It is synchronized to prevent two threads trying to
	 * create an instance simultaneously. @ return WindowHandler object
	 */
	public static synchronized GnwConsoleHandler getInstance() {

		if (handler == null) {
			handler = new GnwConsoleHandler();
		}
		return handler;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * This method loads the configuration properties from the JDK level
	 * configuration file with the help of the LogManager class. It then sets
	 * its level, filter and formatter properties.
	 */
	private void configure() {
		LogManager manager = LogManager.getLogManager();
	    String className = this.getClass().getName();
	    String level = manager.getProperty(className + ".level");
	    //String filter = manager.getProperty(className + ".filter");
	    String formatter = manager.getProperty(className + ".formatter");

	    //accessing super class methods to set the parameters
	    setLevel(level != null ? Level.parse(level) : Level.INFO);
	    //setFilter(makeFilter(filter));
	    setFormatter(makeFormatter(formatter));
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * private method constructing a Filter object with the filter name.
	 * 
	 * @param filterName
	 *            the name of the filter
	 * @return the Filter object
	 */
	@SuppressWarnings("unused")
	private Filter makeFilter(String filterName) {
		 @SuppressWarnings("rawtypes")
		Class c = null;
		 Filter f = null;
		 try {
			 c = Class.forName(filterName);
			 f = (Filter) c.newInstance();
		 } catch (Exception e) {
			 log_.log(Level.WARNING, "There was a problem to load the filter class: " + filterName, e);
		 }
		 return f;
	 }
	 
	 
	// ----------------------------------------------------------------------------

	/**
	 * private method creating a Formatter object with the formatter name. If no
	 * name is specified, it returns a SimpleFormatter object
	 * 
	 * @param formatterName
	 *            the name of the formatter
	 * @return Formatter object
	 */
	private Formatter makeFormatter(String formatterName) {
		 @SuppressWarnings("rawtypes")
		Class c = null;
		 Formatter f = null;

		 try {
			 c = Class.forName(formatterName);
			 f = (Formatter) c.newInstance();
		 } catch (Exception e) {
			 f = new GnwConsoleSimpleFormatter();
		 }
		 return f;
	}
	 
	 
	// ----------------------------------------------------------------------------

	/**
	 * This is the overridden publish method of the abstract super class
	 * Handler. This method writes the logging information to the associated
	 * Java window. This method is synchronized to make it thread-safe. In case
	 * there is a problem, it reports the problem with the ErrorManager, only
	 * once and silently ignores the others.
	 * 
	 * @param record LogRecord object
	 */
	public synchronized void publish(LogRecord record) {
		String message = null;
		//check if the record is loggable
	    if (!isLoggable(record))
	    	return;
	    try {
	    	message = getFormatter().format(record);
	    } catch (Exception e) {
	    	reportError(null, e, ErrorManager.FORMAT_FAILURE);
	    }

	    try {
	    	window.printMsg(message);
	    } catch (Exception ex) {
	    	reportError(null, ex, ErrorManager.WRITE_FAILURE);
	    }
	}
	
	
	// ----------------------------------------------------------------------------

	public void close() {}
	public void flush() {}
	
	public GnwConsoleWindow getConsoleWindow() { return window; }
}
