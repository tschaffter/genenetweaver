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

/**
 * Implements many file filters.
 */
package ch.epfl.lis.utilities.filefilters;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/** Utilities related to filenames.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class FilenameUtilities {

	/** Logger of this class */
	private static Logger log_ = Logger.getLogger(FilenameUtilities.class.getName());
    
	
	// ============================================================================
	// STATIC METHODS
    
	/**
	 * Get the extension of a file.
	 * @param f Input File
	 * @return Extension
	 */
    public static String getExtension(File f) {
    	return getExtension(f.getName());
    }
    
    
    // ----------------------------------------------------------------------------
    
    /**
     * Get the extension of a filename.
     * @param s Input filename
     * @return Extension
     */
    public static String getExtension(String s) {
    	String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
    
    // ----------------------------------------------------------------------------
    
	/***
	* Takes a filename with path and returns just the filename.
	* @param path Absolute path
	* @return Filename without the path prefix
	*/
	public static String getFilenameWithoutPath(String path) {
		File f = new File(path);
		return f.getName();
	}
	
	
	public static String getDirectory(String path) {
		File f = new File(path);
		return f.getParent();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Get a filename without its extension.
	 * @param fullPath Input filename
	 * @return Filename without extension
	 */
	public static String getFilenameWithoutExtension(String fullPath) {
		
		char extensionSeparator = '.';
		String pathSeparator = System.getProperty("file.separator");
		
		 int dot = fullPath.lastIndexOf(extensionSeparator);
		 if (dot == -1)
			 dot = fullPath.length();
		 
	     int sep = fullPath.lastIndexOf(pathSeparator);
	     
	     return fullPath.substring(sep + 1, dot);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Check is the input filename has already one of the specified extensions.
	 * Otherwise, the first extension of the set is added to the filename.
	 * @param filename Input filename
	 * @param extension List of possible extensions
	 * @return Filename with extension
	 */
	public static String addExtension(String filename, String[] extension) {
		
		String fileSeparator = System.getProperty("file.separator");
		// Get the eventuall position of a file separator
		int sep = filename.lastIndexOf(fileSeparator);
		String prefix = "";
		if (sep != -1) { // If path before filename, path saved in prefix and is removed from filename
			prefix = filename.substring(0, sep+1);
			filename = getFilenameWithoutPath(filename);
		}
		
		// Get the position of the last dot (if exist)
		int dot = filename.lastIndexOf('.');
		if (dot == -1) { // No point, so no given extension
			return prefix + filename + "." + extension[0];
		}
		
		String givenExt = filename.substring(dot+1, filename.length());
		for (int i=0; i < extension.length; i++) {
			if (givenExt.compareToIgnoreCase(extension[i]) == 0)
				return prefix + filename;
		}
		// If here, we should add an extension
		filename += "." + extension[0];
		
		return prefix + filename;		
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Check is the input filename has already one of the specified extensions.
	 * Otherwise, the first extension of the set is added to the filename.
	 * @param file file => filename
	 * @param extension List of possible extensions
	 * @return Filename with extension
	 */
	public static File addExtension(File file, String[] extension) {
		String filename = file.getAbsolutePath();
		filename = addExtension(filename, extension);
		return new File(filename);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Check is the input filename has already one of the specified extensions.
	 * Otherwise, the first extension of the set is added to the filename.
	 * @param url URL => filename
	 * @param extension List of possible extensions
	 * @return Filename with extension
	 */
	public static URL addExtension(URL url, String[] extension) {
		try {
			return new URL(addExtension(url.getPath(), extension));
		} catch (MalformedURLException e) {
			log_.warning(e.getMessage());
		}
		return null;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return if yes or not a file should be written.
	 * @param path Absolute path
	 * @param frame Frame source of the dialog box
	 * @return True if the user decided to erase an existing file.
	 */
	public static boolean writeOrAbort(String path, JFrame frame) {
		// Check if the file already exists
		if (fileAlreadyExist(path)) {
			int n = JOptionPane.showConfirmDialog(
//					GnwGuiSettings.getInstance().getGnwGui().getFrame(),
					frame,
				    path
				    + "\n\n"
				    + "The selected filename already exists. If you\n"
				    + "continue, the contents of the existing file will\n"
				    + "be replaced.\n"
				    + "\n"
				    + "Do you want to continue?",
				    "Replace file",
				    JOptionPane.YES_NO_OPTION);

			if (n == JOptionPane.YES_OPTION)
				return true; // If the user selected YES
			else
				return false;
		} else
			return true;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return true if the given path points on an already existing file.
	 * @param path
	 * @return Return true if there is already an existing file at the location defined by
	 * the input path.
	 */
	public static boolean fileAlreadyExist(String path) {
		File file = new File(path);
		return file.exists();
	}
	
	/** 
	 * Read a directory with the specified extension
	 * @param directory path
	 * @param extension of the files wanted
	 * @param browse recursively
	 * @return ArrayList<File> with all the files with the specified extension
	 */
	public static ArrayList<File> readDirectory(String directory,final String ext, boolean rec) {
		
		ArrayList<File> list = new ArrayList<File>();
		ArrayList<File> children = null;
		//Filter to read only files
		FileFilter filter = new FileFilter() {
			public boolean accept(File file){
				if (file.isDirectory()) {
					return !file.getName().startsWith(".");
				}
				else if(file.isFile()) {
					return file.getName().endsWith(ext);
				}
				
				return false;
			}
		};
		
		File dir = new File(directory);
	
		if ( !dir.isDirectory() && dir.exists())
			dir = new File(FilenameUtilities.getDirectory(directory));
		
		if ( !dir.exists() ) {
			return null;
		}
		
		File[] files = dir.listFiles(filter);
		for(int i=0;i<files.length;i++) {
			if ( files[i].isDirectory() && rec) {
				children = readDirectory(files[i].getAbsolutePath(), ext, rec);
				for (int j=0;j<children.size();j++)
					list.add(children.get(j));
			}
			if ( files[i].isFile())
				list.add(files[i]);
		}
		
		return list;
	}
	

}
