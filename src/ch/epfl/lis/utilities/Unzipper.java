package ch.epfl.lis.utilities;

import java.io.BufferedInputStream;  
import java.io.BufferedOutputStream;  
import java.io.File;  
import java.io.FileOutputStream;  
import java.io.FileInputStream;  
import java.io.InputStream;  
import java.io.OutputStream;  
import java.util.Stack;  
import java.util.zip.ZipEntry;  
import java.util.zip.ZipInputStream;  
  
/**
 * Zip uncompression functions
 * 
 * http://wingcomputer.blogspot.com/2009/04/recursive-unzipper-developed-in-java.html
 * Active on September 18, 2010
 */
public class Unzipper
{  
	private final static int BUFFER = 1024;  

	// ----------------------------------------------------------------------------
	// STATIC METHODS
    
    public static void main(String[] args)
    {  
        Unzipper unzip = new Unzipper();  
        String unZipFile = "/home/tschaffter/gnw_desktop.zip";  
        String unZipOutFolder = "/home/tschaffter/";  
        unzip.recursiveUnzip(new File(unZipFile), new File(unZipOutFolder));  
//        unzip.removeAllZipFiles(new File(unZipOutFolder));  
//        System.out.println("Finished!!");  
    }  
    
	// ----------------------------------------------------------------------------

    public File recursiveUnzip(File inFile, File outFolder)  
    {
    	try  
    	{
    		this.createFolder(outFolder, true);  
    		BufferedOutputStream out = null;  
    		ZipInputStream  in = new ZipInputStream(new BufferedInputStream(new FileInputStream(inFile)));  
    		ZipEntry entry;
    		
    		while((entry = in.getNextEntry()) != null)  
    		{
    			int count;  
    			byte data[] = new byte[BUFFER];  
                     
    			// write the files to the disk  
    			File newFile = new File(outFolder.getPath() + "/" + entry.getName());  
    			Stack<File> pathStack = new Stack<File>();  
    			File newNevigate = newFile.getParentFile();  
    			while(newNevigate != null)
    			{  
    				pathStack.push(newNevigate);  
    				newNevigate = newNevigate.getParentFile();  
    			}  
    			
    			//create all the path directories  
    			//while(!pathStack.isEmpty())
    			while(pathStack.size() != 0)
    			{  
    				File createFile = pathStack.pop();  
    				this.createFolder(createFile, true);  
    			} 
    			
    			if(!entry.isDirectory())
    			{  
    				out = new BufferedOutputStream(new FileOutputStream(newFile),BUFFER);  
    				
    				while ((count = in.read(data,0,BUFFER)) != -1)
    				{  
    					out.write(data,0,count);  
    				}  
    				
    				this.cleanUp(out);  
    				//recursively unzip files  
    				if(entry.getName().toLowerCase().endsWith(".zip"))
    				{  
    					String zipFilePath = outFolder.getPath() + "/" + entry.getName();  
    					this.recursiveUnzip(new File(zipFilePath), new File(zipFilePath.substring(0, zipFilePath.length()-4)));  
    				}  
    			}
    			else
    			{  
    				this.createFolder(new File(entry.getName()), true);  
    			}  
    		} 
    		
    		this.cleanUp(in);  
    		return outFolder;  
    		
    	}
    	catch(Exception e)
    	{  
    		e.printStackTrace();  
    		return inFile;  
    	}  
    }  
    
	// ----------------------------------------------------------------------------
      
    public void removeAllZipFiles(File folder)
    {  
    	String[] files = folder.list();
    	
        for(String file: files)
        {  
        	File item = new File(folder.getPath() + "/" + file); 
        	
            if(item.exists() && item.isDirectory())
            {  
            	this.removeAllZipFiles(item);  
            }
            else if(item.exists() && item.getName().toLowerCase().endsWith(".zip"))
            {  
                item.delete();  
                System.out.println(item.getName() + " Removed!!");  
            }  
        }  
    }  
      
	// ----------------------------------------------------------------------------
      
    private void createFolder(File folder, boolean isDriectory)
    {  
        if(isDriectory)
        {  
            folder.mkdir();  
        }  
    }  
    
	// ----------------------------------------------------------------------------
      
    private void cleanUp(InputStream in) throws Exception
    {  
         in.close();  
    }  
    
    // ----------------------------------------------------------------------------
      
    private void cleanUp(OutputStream out) throws Exception
    {  
         out.flush();  
         out.close();  
    }  
}  