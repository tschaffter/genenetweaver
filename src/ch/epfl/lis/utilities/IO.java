package ch.epfl.lis.utilities;

import java.io.File;
import java.util.logging.Logger;

public class IO
{
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(IO.class.getName());
	
	// ----------------------------------------------------------------------------
	// STATIC METHODS
	
	/** Deletes all files and subdirectories under dir. */
	public static boolean deleteFolder(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			
			for (int i=0; i<children.length; i++)
			{
				boolean success = deleteFolder(new File(dir, children[i]));
				
				if (!success)
					return false;
			}
		}
	
		// The directory is now empty so delete it
		return dir.delete();
	}
}