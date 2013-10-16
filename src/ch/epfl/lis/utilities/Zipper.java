package ch.epfl.lis.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
 
/**
 * Zip compression functions
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Zipper
{
	// ----------------------------------------------------------------------------
	// STATIC METHODS
	
	public static void main(String[] args) throws Exception
	{
		BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
		System.out.println("Enter the source directory/file name : ");
		String source = in.readLine();    
		File src = new File (source);
	 
		if (src.isDirectory())
		{ 
			String zipFile = source + ".zip";
			zipFolder (source, zipFile);
		}
		else
		{
			int lastDot = source.lastIndexOf(".");
			String zipFile;
			
			if (lastDot != -1)
				zipFile = source.substring(0, lastDot) + ".zip";  
			else
	          zipFile = source + ".zip";

			zipFolder (source, zipFile);    
		}
	}

	// ----------------------------------------------------------------------------
	 
	static public void zipFolder(String srcFolder, String destZipFile) throws Exception
	{
		ZipOutputStream zip = null;
	    FileOutputStream fileWriter = null;
	 
	    fileWriter = new FileOutputStream(destZipFile);
	    zip = new ZipOutputStream(fileWriter);
	 
	    addFileToZip("", srcFolder, zip);
	    
	    zip.flush();
	    zip.close();
	}
	
	// ----------------------------------------------------------------------------
	 
	static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception
	{
		File folder = new File(srcFile);
	 
	    if (folder.isDirectory())
	        addFolderToZip(path, srcFile, zip);
	    else
	    {
	    	byte[] buf = new byte[1024];
	    	int len;
	    	FileInputStream in = new FileInputStream(srcFile);
	    	zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
	 
	    	while ((len = in.read(buf)) > 0)
	    		zip.write(buf, 0, len);
	    	
	    	in.close();
	    }
	}
	
	// ----------------------------------------------------------------------------
	 
	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception
	{
		File folder = new File(srcFolder);
		String[] fileName = folder.list(); 
	 
		for (int i = 0; i < fileName.length; i++)
		{
			if (path.equals(""))
				addFileToZip(folder.getName(), srcFolder + "/" + fileName[i], zip);
			else 
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName[i], zip);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public static String appendZipExtensionIfRequired(String zipFilename)
	{
		int lastDot = zipFilename.lastIndexOf(".");
		
		if (lastDot != -1)
			return zipFilename.substring(0, lastDot) + ".zip";
		else
			return zipFilename + ".zip";
	}
}