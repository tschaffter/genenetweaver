package ch.epfl.lis.gnwgui;

import java.awt.Frame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/** This class is used to handle open/save dialogs.
 * 
 * Primarily, this class has been designed to be able to switch easily
 * between Swing and AWT dialogs. If the GUI of GNW makes use of Swing
 * in order to have the same GUI on all platforms, the possibility to
 * release GNW as a Mac application is studying because Mac offer nice
 * features to its applications such as drag&drop files on the application
 * icon to open them. What was intended with the implementation of this
 * class was to benefit from the nice, native open/save Mac dialogs.
 * 
 * EDIT: AWT not used anymore, only Swing.
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 *
 */
public class IODialog {
	
	private String title_;
	private Frame parent_;
	private String path_;
	private String selection_;
	
	public static final Byte LOAD = 0;
	public static final Byte SAVE = 1;
	
	private JFileChooser jfc_;
	
    /** Logger for this class */
	private static Logger log_ = Logger.getLogger(IODialog.class.getName());	
	
	// ============================================================================
	// PUBLIC FUNCTIONS
	
	public IODialog(Frame parent, String title, String path, Byte mode)
	{
		parent_ = parent;
		title_ = title;
		path_ = path;
		
		try
		{
			jfc_ = new JFileChooser(path_);
		}
		catch (IllegalArgumentException iae)
		{
			log_.log(Level.WARNING, "IODialog::IODialog(): " + iae.getMessage(), iae);
		}
		catch (Exception e)
		{
			log_.log(Level.WARNING, "IODialog::IODialog(): " + e.getMessage(), e);
		}
	}	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Display the dialog.
	 */
	public void display()
	{	
		selection_ = null;
		int returnVal = jfc_.showDialog(parent_, title_);
			
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			selection_ = jfc_.getSelectedFile().getAbsolutePath();
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Add one file filter. To use with Swing dialog.
	 */
	public void addFilter(final FileFilter filter)
	{
		jfc_.addChoosableFileFilter(filter);
		jfc_.setFileFilter(filter);
	}
	
	// ----------------------------------------------------------------------------
	
	public FileFilter getSelectedFilter()
	{
		return jfc_.getFileFilter();
	}
	
	// ----------------------------------------------------------------------------
	
	public String getDirectory()
	{
		return jfc_.getSelectedFile().getParentFile().getAbsolutePath();
		
	}

	// ----------------------------------------------------------------------------
	
	public void selectOnlyFolder(boolean b)
	{
		if (b)
			jfc_.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		else
			jfc_.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}
	
	// ----------------------------------------------------------------------------
	
	public void setAcceptAllFileFilterUsed(final boolean b)
	{
		jfc_.setAcceptAllFileFilterUsed(b);
	}
	
	// ----------------------------------------------------------------------------
	
	public void setSelection(String filename)
	{
		File f = new File(filename);
		jfc_.setSelectedFile(f);
	}

	// ----------------------------------------------------------------------------
	
	public String getSelection() { return selection_; }
}
