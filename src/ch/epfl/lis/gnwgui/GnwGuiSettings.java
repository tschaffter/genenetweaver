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

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import ch.epfl.lis.gnw.GnwSettings;

/**  Offers global parameters (settings) and functions used by all classes of gnwgui package.
 * 
 * Global makes use of the Singleton design pattern: There's at most one
 * instance present, which can only be accessed through getInstance().
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class GnwGuiSettings {
	
	/** Global instance */
	private static GnwGuiSettings instance_ = null;
	
	private static boolean isMac_;
	private static boolean isWin_;
	
	/** Use Swing native look & feel */
	private boolean useSwingNativeLookAndFeel_;
	
	/** Icon of the static network items. */
	private URL structureIcon_ = null;
	private URL structureIcon24_ = null;
	/** Icon of the dynamic network items. */
	private URL grnIcon_ = null;
	private URL grnIcon24_ = null;
	/** Item label background color when selected. */
	private static Color itemSelectedBgcolor_ = new Color(56, 117, 215);
	/** Icon of the bin (emtpy state). */
	private URL binEmptyIcon_ = null;
	/** Icon of the bin (filled state). */
	private URL binFullIcon_ = null;
	/** Icon of folder element */
	private URL folderIcon_ = null;
	private URL folderIcon24_ = null;
	/** IFile icon */
	private URL fileIcon_ = null;
	/** Copy icon */
	private URL copyIcon_ = null;
	/** Copy icon */
	private URL newIcon_ = null;
	/** Remove icon */
	private URL removeIcon_ = null;
	/** Icon for import. */
	private URL importNetworkIcon_ = null;
	/** Splash screen */
	private URL splashScreenImage_ = null;
	/** About image */
	private URL aboutImage_ = null;
	/** Interaction labels */
	private URL geneInteractionLabels_ = null;
	/** Path to the left-icon image of the application */
	private URL gnwIcon_ = null;
	/** Path to the EPFL logo */
	private URL epflImage_ = null;
	/** Path to the MIT logo */
	private URL mitImage_ = null;
	/** Path to the LIS logo */
	private URL lisImage_ = null;
	/** Graph signature */
	private URL gnwWatermarkImage_ = null;
	/** Graph signature with background since EPS doesn't support the transparency. */
	private URL gnwWatermarkNoTransparencyImage_ = null;
	/** Connection icon */
	private URL connectionImage_ = null;
	/** Export network desktop content */
	private URL exportNetworkDesktopImage_ = null;
	
	/** About image */
	private URL menuAboutImage_ = null;
	/** Network image */
	private URL menuNetworkImage_ = null;
	/** Evaluation image */
	private URL menuEvaluationImage_ = null;
	/** Settings image */
	private URL menuSettingsImage_ = null;
	/** Tutorial image */
	private URL menuTutorialImage_ = null;
	/** Documentation image */
	private URL menuHelpImage_ = null;
	/** Konsole image */
	private URL menuConsoleImage_ = null;
	/** Exit image */
	private URL menuExitImage_ = null;
	private URL dialogExitImage_ = null;

	/** Icon for snapshots */
	private URL snapshotImage_ = null;
	
	/** URL to the HTML tutorial */
	private URL htmlTutorial_ = null;
	/** URL to the HTML help */
	private URL htmlHelp_ = null;
	
	/** Reference to the interactive desktop used for the Network Manager. */
	private NetworkDesktop networkDesktop_ = null;
	
	/** Reference to the main instance of the application */
	private GnwGui gnwgui_ = null;
	
	/**
	 * Each entry in this list corresponds to a network (structure or dynamical model) to load
	 * when the application is launch. The key (string) corresponds to the label under with the
	 * element will appear on the iDesktop (Network Manager). If the first letter of the label
	 * is '#', the element will be name accordingly to its filename without extension. URL
	 * represent the path to the network file to load.
	 */
	private SortedMap<String, URL> initialNetworksToLoad_ = new TreeMap<String, URL>();
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(GnwGuiSettings.class.getName());
	
    static {
    	String osname = System.getProperty("os.name").toLowerCase();
    	isWin_ = osname.startsWith("windows");
    	isMac_ = !isWin_ && osname.startsWith("mac os x"); // official way
    }


    // =======================================================================================
    // PUBLIC FUNCTIONS
    //
	
	/**
	 * Default constructor
	 */
	public GnwGuiSettings() {
		initialize();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Get Universal instance
	 */
	static public GnwGuiSettings getInstance() {
		if (instance_ == null) {
			instance_ = new GnwGuiSettings();
		}
		return instance_;
	}

	
	// ----------------------------------------------------------------------------
	
	/**
	 * Initialization
	 */
	public void initialize()
	{
		useSwingNativeLookAndFeel_ = false;

		// Get the different resources
		structureIcon_ = getClass().getResource("rsc/structureIcon_mini.png");
		structureIcon24_ = getClass().getResource("rsc/structureIcon24.png");
		grnIcon_ = getClass().getResource("rsc/grnIcon_mini.png");
		grnIcon24_ = getClass().getResource("rsc/grnIcon24.png");
		binEmptyIcon_ = getClass().getResource("rsc/trashcan_empty_mini.png");
		binFullIcon_ = getClass().getResource("rsc/trashcan_full_mini.png");
		folderIcon_ = getClass().getResource("rsc/folder_blue.png");
		folderIcon24_ = getClass().getResource("rsc/folder_blue24.png");
		fileIcon_ = getClass().getResource("rsc/file24.png");
		removeIcon_ = getClass().getResource("rsc/process-stop.png");
		importNetworkIcon_ = getClass().getResource("rsc/folder_green.png");
		splashScreenImage_ = getClass().getResource("rsc/splash-screen.png");
		aboutImage_ = getClass().getResource("rsc/about-tab.png");
		geneInteractionLabels_ = getClass().getResource("rsc/interaction-labels.png");
		htmlTutorial_ = getClass().getResource("rsc/tutorial.html");
		htmlHelp_ = getClass().getResource("rsc/help.html");
		gnwIcon_ = getClass().getResource("rsc/GNW-icon.png");
		epflImage_ = getClass().getResource("rsc/EPFL_logo.png");
		mitImage_ = getClass().getResource("rsc/MIT_logo.png");
		lisImage_ = getClass().getResource("rsc/LIS_logo2.png");
		gnwWatermarkImage_ = getClass().getResource("rsc/signature.png"); // rsc/signature.png
		gnwWatermarkNoTransparencyImage_ = getClass().getResource("rsc/signature-white.png");
		snapshotImage_ = getClass().getResource("rsc/ksnapshot.png");
		copyIcon_ = getClass().getResource("rsc/copy.png");
		newIcon_ = getClass().getResource("rsc/new.png");
		menuAboutImage_ = getClass().getResource("rsc/gnw32x32.png");
		menuNetworkImage_ = getClass().getResource("rsc/desktop.png");
		menuEvaluationImage_ = getClass().getResource("rsc/kchart.png");
		menuSettingsImage_ = getClass().getResource("rsc/settings2.png");
		menuTutorialImage_ = getClass().getResource("rsc/easymoblog.png");
		menuHelpImage_ = getClass().getResource("rsc/help2.png");
		menuConsoleImage_ = getClass().getResource("rsc/konsole.png");
		menuExitImage_ = getClass().getResource("rsc/shutdown.png");
		dialogExitImage_ = getClass().getResource("rsc/shutdown64.png");
		connectionImage_ = getClass().getResource("rsc/network_local.png");
		exportNetworkDesktopImage_ = getClass().getResource("rsc/folder_yellow.png");
	
		// Specify the networks to load at t0
		// IMPORTANT: Start with '/' for Java Web Start, start with '//' for standalone version
		// If the key starts with "#", the filename is used as network id, otherwise id = key
//		initialNetworksToLoad_.put("Ecoli", getClass().getResource("rsc/net/ecoli_transcriptional_network_regulonDB_6_7.tsv"));
		initialNetworksToLoad_.put("Ecoli", getClass().getResource("/ch/epfl/lis/networks/ecoli_transcriptional_network_regulonDB_6_7.tsv"));
		initialNetworksToLoad_.put("Yeast", getClass().getResource("/ch/epfl/lis/networks/yeast_transcriptional_network_Balaji2006.tsv"));
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return a random color in the RGB format.
	 * @return Random RGB color
	 */
	final public Color randomColor() {
		GnwSettings uni = GnwSettings.getInstance();
		int R = uni.getUniformDistribution().nextIntFromTo(0, 255);
		int G = uni.getUniformDistribution().nextIntFromTo(0, 255);
		int B = uni.getUniformDistribution().nextIntFromTo(0, 255);
		
		return new Color(R, G, B);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Convert an instance of Color into hexa format.
	 * @param c Input color
	 * @return Contains the hexadecimal value of the input color
	 */
	public static String RGB2HexaColor(Color c) {
		return Integer.toHexString( c.getRGB() & 0x00ffffff );
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Convert a DoubleMatric1D vector to an ArrayList<Integer> vector.
	 * @param list Input vector
	 * @return Output vector
	 */
	public static ArrayList<Integer> doubleMatrix1D2ArrayListInteger(DoubleMatrix1D list) {
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i=0; i < list.size(); i++)
			result.add((int)list.get(i));
		
		return result;
	}

	
    // =======================================================================================
    // GETTERS AND SETTERS
    //
	
	public void isMac(boolean mac) { isMac_ = mac; }
	public boolean isMac() { return isMac_; }
	
	public void isWin(boolean win) { isWin_ = win; }
	public boolean isWin() { return isWin_; }
	
	public void useSwingNativeLookAndFeel(boolean b) { useSwingNativeLookAndFeel_ = b; }
	public boolean useSwingNativeLookAndFeel() { return useSwingNativeLookAndFeel_; }
	
	public void setStructureIcon(URL path) { structureIcon_ = path; }
	public URL getStructureIcon() { return structureIcon_; }
	
	public void setStructureIcon24(URL path) { structureIcon24_ = path; }
	public URL getStructureIcon24() { return structureIcon24_; }
	
	public void setGrnIcon(URL path) { grnIcon_ = path; }
	public URL getGrnIcon() { return grnIcon_; }
	
	public void setGrnIcon24(URL path) { grnIcon24_ = path; }
	public URL getGrnIcon24() { return grnIcon24_; }
	
	public void setItemSelectedBgcolor(Color c) { itemSelectedBgcolor_ = c; }
	public Color getItemSelectedBgcolor() { return itemSelectedBgcolor_; }
	
	public void setBinEmptyIcon(URL path) { binEmptyIcon_ = path; }
	public URL getBinEmptyIcon() { return binEmptyIcon_; }
	
	public void setBinFullIcon(URL path) { binFullIcon_ = path; }
	public URL getBinFullIcon() { return binFullIcon_; }
	
	public void setFolderIcon(URL path) { folderIcon_ = path; }
	public URL getFolderIcon() { return folderIcon_; }
	
	public void setFolderIcon24(URL path) { folderIcon24_ = path; }
	public URL getFolderIcon24() { return folderIcon24_; }
	
	public void setFileIcon(URL path) { fileIcon_ = path; }
	public URL getFileIcon() { return fileIcon_; }
	
	public void setCopyIcon(URL path) { copyIcon_ = path; }
	public URL getCopyIcon() { return copyIcon_; }
	
	public void setNewIcon(URL path) { newIcon_ = path; }
	public URL getNewIcon() { return newIcon_; }
	
	public void setRemoveIcon(URL path) { removeIcon_ = path; }
	public URL getRemoveIcon() { return removeIcon_; }
	
	public void setNetworkDesktop(NetworkDesktop desktop) { networkDesktop_= desktop; }
	public NetworkDesktop getNetworkDesktop() { return networkDesktop_; }
	
	public void setGnwGui(GnwGui app) { gnwgui_ = app; }
	public GnwGui getGnwGui() { return gnwgui_; }
	
	public void setSnapshotImage(URL path) { snapshotImage_ = path; }
	public URL getSnapshotImage() { return snapshotImage_; }
	
	public Map<String, URL> getInitialNetworksToLoad() { return initialNetworksToLoad_; }
	
	public void setImportNetworkIcon(URL path) { importNetworkIcon_ = path; }
	public URL getImportNetworkIcon() { return importNetworkIcon_; }
	
	public void setSplashScreenImage(URL path) { splashScreenImage_ = path; }
	public URL getSplashScreenImage() { return splashScreenImage_; }
	
	public void setAboutImage(URL path) { aboutImage_ = path; }
	public URL getAboutImagePath() { return aboutImage_; }
	
	public void setInteractionLabels(URL path) { geneInteractionLabels_ = path; }
	public URL getInteractionLabels() { return geneInteractionLabels_; }
	
	public void setHtmlTutorial(URL path) { htmlTutorial_ = path; }
	public URL getHtmlTutorial() { return htmlTutorial_; }
	
	public void setHtmlHelp(URL path) { htmlHelp_ = path; }
	public URL getHtmlHelp() { return htmlHelp_; }
	
	public void setGnwIcon(URL path) { gnwIcon_ = path; }
	public URL getGnwIcon() { return gnwIcon_; }
	
	public void setEpflImage(URL path) { epflImage_ = path; }
	public URL getEpflImage() { return epflImage_; }
	
	public void setMitImage(URL path) { mitImage_ = path; }
	public URL getMitImage() { return mitImage_; }
	
	public void setLisImage(URL path) { lisImage_ = path; }
	public URL getLisImage() { return lisImage_; }
	
	public void setMenuAboutImage(URL path) { menuAboutImage_ = path; }
	public URL getMenuAboutImage() { return menuAboutImage_; }
	
	public void setMenuNetworkImage(URL path) { menuNetworkImage_ = path; }
	public URL getMenuNetworkImage() { return menuNetworkImage_; }
	
	public void setMenuEvaluationImage(URL path) { menuEvaluationImage_ = path; }
	public URL getMenuEvaluationImage() { return menuEvaluationImage_; }
	
	public void setMenuSettingsImage(URL path) { menuSettingsImage_ = path; }
	public URL getMenuSettingsImage() { return menuSettingsImage_; }
	
	public void setMenuTutorialImage(URL path) { menuTutorialImage_ = path; } 
	public URL getMenuTutorialImage() { return menuTutorialImage_; }
	
	public void setMenuHelpImage(URL path) { menuHelpImage_ = path; } 
	public URL getMenuHelpImage() { return menuHelpImage_; }
	
	public void setMenuConsoleImage(URL path) { menuConsoleImage_ = path; }
	public URL getMenuConsoleImage() { return menuConsoleImage_; }
	
	public void setMenuExitImage(URL path) { menuExitImage_ = path; }
	public URL getMenuExitImage() { return menuExitImage_; }
	
	public void setDialogExitImage(URL path) { dialogExitImage_ = path; }
	public URL getDialogExitImage() { return dialogExitImage_; }
	
	public void setGnwWatermarkImage(URL path) { gnwWatermarkImage_ = path; }
	public URL getGnwWatermarkImage() { return gnwWatermarkImage_; }
	
	public void setConnectionImage(URL path) { connectionImage_ = path; }
	public URL getConnectionImage() { return connectionImage_; }
	
	public void setGnwWatermarkNoTransparencyImage(URL path) { gnwWatermarkNoTransparencyImage_ = path; }
	public URL getGnwWatermarkNoTransparencyImage() { return gnwWatermarkNoTransparencyImage_; }
	
	public void setExportNetworkDesktopImage(URL path) { exportNetworkDesktopImage_ = path; }
	public URL getExportNetworkDesktopImage() { return exportNetworkDesktopImage_; }
}
