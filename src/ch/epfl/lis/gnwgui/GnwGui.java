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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.gnwgui.Folder;
import ch.epfl.lis.gnwgui.windows.GnwGuiWindow;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.HierarchicalScaleFreeNetwork;
import ch.epfl.lis.networks.ios.ParseException;


/** Main class of the GeneNetWeaver GUI application.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class GnwGui extends GnwGuiWindow {

	/** Packages to set up for logging. TODO: TO DELETE AND REPLACE WITH LOG4J */
	private static String[] packages2log = {"ch.epfl.lis.gnw", 
		"ch.epfl.lis.gnwgui", 
		"ch.epfl.lis.gnwgui.filefilters", 
		"ch.epfl.lis.gnwgui.idesktop", 
		"ch.epfl.lis.gnwgui.jungtransformers", 
		"ch.epfl.lis.gnwgui.windows", 
		"ch.epfl.lis.imod", 
		"ch.epfl.lis.networks", 
		"ch.epfl.lis.networks.ios",
		"ch.epfl.lis.sde"};

	/** Splash Screen */
	private static SplashScreen splashScreen_;

	/** StyledDocument for the content of the console */
	private StyledDocument doc_ = null;
	/** Style for the content of the console */
	private Style style_ = null;

	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(GnwGui.class.getName());

	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor
	 */
	public GnwGui() {

	}


	// ----------------------------------------------------------------------------

	/**
	 * Build and initialize the GUI.
	 */
	public void run()
	{
		setGnwConsole();

		// display the splash
		splashScreen_ = new SplashScreen(new Frame(), GnwGuiSettings.getInstance().getSplashScreenImage(),
				SplashScreen.NORMAL, true, true, false);
		splashScreen_.setVisible(true);

		initialize();

		loadInitialNetworks();
		loadDREAMNetworks();
		

		setMessages();
		
		// hide the splash and display the loaded GUI interface
		splashScreen_.setVisible(false);
		this.getFrame().setVisible(true);
	}


	// ----------------------------------------------------------------------------

	/**
	 * Build the GUI interface of GNW
	 */
	private void initialize() {

		splashScreen_.setTaskInfo("Initialization");

		GnwGuiSettings settings = GnwGuiSettings.getInstance();
		settings.setGnwGui(this);

		// set settings tab
		doc_ = (StyledDocument)settingsTextPane_.getDocument();
		style_ = doc_.addStyle("settingsStyle", null);
		displaySettingsContent();

		// Console welcome
		String text = "Welcome to GeneNetWeaver " + GnwSettings.getInstance().getGnwVersion() + "\n";
		log_.info(text);

		// Define the actions of the components of the window
		// Add a listener for the close event
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				if (!shouldExit())
					return;
				//frame.setVisible(false);
				//frame.dispose();
				closeAction();
			}
		});

		about_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, aboutPanel_.getName());
				header_.setTitle("GeneNetWeaver");
				header_.setInfo(GnwSettings.getInstance().getGnwVersion());
			}
		});

		networkManager_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, networkPanel_.getName());
				header_.setTitle("Network Desktop");
				header_.setInfo("Double-click on networks for options (blue = network " +
						"structures / orange = dynamical " +
				"network models)");
			}
		});

		evaluation_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				evaluationContentPanel_.refreshAllModels();
				mainDisplayLayout_.show(displayPanel_, evaluationPanel_.getName());

				header_.setTitle("Evaluation of network predictions");
				header_.setInfo("Assign network predictions to gold standards for evaluation");
			}
		});

		settings_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, settingsPanel_.getName());
				header_.setTitle("Settings");
				header_.setInfo("Load/save and edit GNW settings");
			}
		});

		tutorial_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, tutorialPanel_.getName());
				header_.setTitle("Tutorial");
				header_.setInfo("Generating benchmarks and evaluating predictions with GNW");
			}
		});

		help_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, helpPanel_.getName());
				header_.setTitle("Help");
				header_.setInfo("GNW documentation");
			}
		});

		exit_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (shouldExit())
					closeAction();
			}
		});

		applySettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				applySettings();
			}
		});

		reloadSettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				reloadSettings();
			}
		});

		openSettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				openSettings();
			}
		});

		exportSettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				exportSettings();
			}
		});

		consoleToggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				GnwConsoleHandler.getInstance().displayConsoleWindow(true);
			}
		});

		networkScrollPanel_.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				networkDesktop_.getDesktopPane().repaint();
			}
		});

		networkScrollPanel_.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				networkDesktop_.getDesktopPane().repaint();
			}
		});

		networkScrollPanel_.addComponentListener(new ComponentListener() {

			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			public void componentResized(ComponentEvent e) {
				networkDesktop_.getDesktopPane().setMinimumSize(networkScrollPanel_.getSize());

				networkDesktop_.resizeDesktop();
			}

			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});

		// Link the escape action with anyone of the components that are always present in the main
		// application window.
		keyboardExit(header_);

		// Display the Network manager after launching
		networkManager_.doClick();
		splashScreen_.stepDone();
	}


	// ----------------------------------------------------------------------------

	/**
	 * Display a dialog that asks the user if he/she really want to leave the application.
	 * @return Return true if the user answer is "Yes" to the question "Exit GeneNetWeaver ?".
	 */
	public boolean shouldExit() {

		ImageIcon icon = new ImageIcon(GnwGuiSettings.getInstance().getMenuExitImage());

		int n = JOptionPane.showConfirmDialog(
				frame,
				"Exit GeneNetWeaver ?",
				"GNW message",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				icon);

		if (n == JOptionPane.YES_OPTION)
			return true; // If the user selected YES
		else
			return false;
	}


	// ----------------------------------------------------------------------------

	/**
	 * Associated the logger to the console handler.
	 */
	public void setGnwConsole() {
		Handler ch = GnwConsoleHandler.getInstance();
		for (int i=0; i < packages2log.length; i++)
			Logger.getLogger(packages2log[i]).addHandler(ch);
	}


	// ----------------------------------------------------------------------------


	/**
	 * @throws IOException, Exception 
	 * 
	 */
	public String loadSettingsContent() throws IOException, Exception {

		// Create a URL for the desired page
		URL url = GnwSettings.getInstance().getLastSettingsURL();
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String str = "";
		String tmp = in.readLine();

		while (tmp != null) {
			str += tmp + "\n";
			tmp = in.readLine();
		}
		in.close();

		return str;
	}


	// ----------------------------------------------------------------------------


	/**
	 * Display the content of the last settings file loaded.
	 */
	public void displaySettingsContent() {
		try {
			printSettingsContent(loadSettingsContent());
			settingsTextPane_.setCaretPosition(0); // set the cursor on the first line
		} catch (BadLocationException e) {
			printSettingsContent("Unable to display settings file content, see console for details.");
			log_.log(Level.WARNING, "Unable to display settings file content (BadLocationException): " + e.getMessage(), e);
		} catch (IOException e) {
			printSettingsContent("Unable to display settings file content, see console for details.");
			log_.log(Level.WARNING, "Unable to display settings file content (IOException): " + e.getMessage(), e);
		} catch (Exception e) {
			printSettingsContent("Unable to display settings file content, see console for details.");;
			log_.log(Level.WARNING, "Unable to display settings file content (Exception): " + e.getMessage(), e);
		}
	}


	// ----------------------------------------------------------------------------


	/**
	 * Apply the new settings that have been edited by the user inside GNW.
	 */
	public void applySettings() {

		try {
			// get the content of the settings pane
			String data = doc_.getText(0, doc_.getLength());
			InputStream is = new ByteArrayInputStream(data.getBytes("UTF-8"));
			GnwSettings.getInstance().loadSettingsFromStream(is);

			log_.info("The new settings are successfully applied!");

		} catch (BadLocationException e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to apply the new settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to apply the new settings (BadLocationException): " + e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to apply the new settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to apply the new settings (UnsupportedEncodingException): " + e.getMessage(), e);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to apply the new settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to apply the new settings (Exception): " + e.getMessage(), e);
		}
	}


	// ----------------------------------------------------------------------------


	public void reloadSettings() {

		try {
			GnwSettings.getInstance().loadLastSettingsOpened();
			displaySettingsContent();

			log_.info("The settings file " + FilenameUtilities.getFilenameWithoutPath(GnwSettings.getInstance().getLastSettingsURL().getPath()) + " is successfully loaded!");

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to reload the settings file, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to reload the settings file (IOException): " + e.getMessage(), e);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to reload the settings file, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to reload the settings file (Exception): " + e.getMessage(), e);
		}
	}


	// ----------------------------------------------------------------------------


	public void openSettings() {

		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Open Settings",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.LOAD);

		dialog.setAcceptAllFileFilterUsed(true);
		dialog.display();

		try {
			if (dialog.getSelection() != null) {
				GnwSettings.getInstance().loadSettingsFromURL(GnwSettings.getInstance().getURL(dialog.getSelection()));
				displaySettingsContent();
				log_.info("The settings file " + FilenameUtilities.getFilenameWithoutPath(dialog.getSelection()) + " is successfully loaded!");

				// Save the current directory as default path
				String dir = FilenameUtilities.getDirectory(dialog.getSelection());
				GnwSettings.getInstance().setOutputDirectory(dir);
			}

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to load the settings file, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to load the settings file (IOException): " + e.getMessage(), e);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to load the settings file, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to load the settings file (Exception): " + e.getMessage(), e);
		}
	}


	// ----------------------------------------------------------------------------


	public void exportSettings() {

		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Export Settings",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);

		dialog.setAcceptAllFileFilterUsed(true);
		dialog.display();

		try {
			if (dialog.getSelection() != null) {
				OutputStream output = new FileOutputStream(dialog.getSelection()); 
				output.write(doc_.getText(0, doc_.getLength()).getBytes("UTF-8"));
				output.close();
				log_.info("The settings file " + FilenameUtilities.getFilenameWithoutPath(dialog.getSelection()) + " is successfully saved!");

				// Save the current directory as default path
				String dir = FilenameUtilities.getDirectory(dialog.getSelection());
				GnwSettings.getInstance().setOutputDirectory(dir);
			}

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to save the settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to save the settings (IOException): " + e.getMessage(), e);
		} catch (BadLocationException e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to save the settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to save the settings (BadLocationException): " + e.getMessage(), e);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to save the settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log_.log(Level.WARNING, "Unable to save the settings (Exception): " + e.getMessage(), e);
		}
	}


	// ----------------------------------------------------------------------------


	/**
	 * Use this function to print the settings content
	 * @param data Text
	 * @throws BadLocationException 
	 */
	public void printSettingsContent(String data) {
		try {
			doc_.remove(0, doc_.getLength()); // clean the content of the settings window
			doc_.insertString(0, data, style_); // insert the content of the settings file loaded
		} catch (BadLocationException e) {
			log_.log(Level.WARNING, "Unable to print the settings file content: " + e.getMessage(), e);
		} 
	}


	// ----------------------------------------------------------------------------


	/**
	 * Load network and display them on the Network tab.
	 */
	public void loadInitialNetworks() {

		GnwGuiSettings settings = GnwGuiSettings.getInstance();

		// create an instance of hierarchical scale-free network (Ravasz and al.)
		splashScreen_.setTaskInfo("Loading Example");
		HierarchicalScaleFreeNetwork scaleFree = new HierarchicalScaleFreeNetwork(3, "G");
		scaleFree.setId("Example");
		StructureElement scaleFreeItem = new StructureElement(scaleFree.getId(), settings.getNetworkDesktop());
		scaleFreeItem.setNetwork(new ImodNetwork(scaleFree));
		settings.getNetworkDesktop().addItemOnDesktop(scaleFreeItem);
		IONetwork.printOpeningInfo(scaleFreeItem);
		splashScreen_.stepDone();

		// load network defined in GnwGuiSettings
		// TODO: specification of these networks must be done using command line arguments
		// NOTE: take into account network description !!
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		Map<String, URL> list = global.getInitialNetworksToLoad();
		Iterator<?> it = list.entrySet().iterator();

		while (it.hasNext()) {
			try {
				@SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
				splashScreen_.setTaskInfo("Loading " + (String)pairs.getKey());
				IONetwork.loadItem((String)pairs.getKey(), (URL) pairs.getValue(), null);
			} catch (FileNotFoundException e) {
				log_.log(Level.WARNING, "Unable to load initial networks (FileNotFoundException): " + e.getMessage(), e);
			} catch (ParseException e) {
				log_.log(Level.WARNING, "Unable to load initial networks (ParseException): " + e.getMessage(), e);
			} catch (Exception e) {
				log_.log(Level.WARNING, "Unable to load initial networks (Exception): " + e.getMessage(), e);
			}
			splashScreen_.stepDone();
		}

		// networks information
		
		scaleFreeItem.setToolTipText(
				"<html>Hierarchical scale-free network model: 64 nodes, 207 edges.<br>" +
				"Has a scale-free topology with embedded modularity similar to many<br>" +
		"biological networks (Ravasz et al. 2002. <i>Science</i>, 297:1551-55).</html>");
		if ( networkDesktop_.getIElementFromLabel("Ecoli") != null ) {
		networkDesktop_.getIElementFromLabel("Ecoli").setToolTipText(
				"<html>E.coli transcriptional regulatory network: 1565 nodes, 3758 edges.<br>" +
				"Corresponds to the TF-gene interactions of RegulonDB release 6.7 (May 2010).<br>" +
		"(Gama-Castro et al. 2008. <i>Nucleic Acids Res</i>, 36:D120-4).</html>");
		}
		if ( networkDesktop_.getIElementFromLabel("Yeast") != null ) {
		networkDesktop_.getIElementFromLabel("Yeast").setToolTipText(
				"<html>Yeast transcriptional regulatory network: 4441 nodes, 12873 edges.<br>" +
		"As described in: Balaji et al. 2006. <i>J Mol Biol</i>, 360:213-27.</html>");
		}


	}

	/**
	 * Load DREAM 3/4 networks
	 */
	public void loadDREAMNetworks() {

		GnwGuiSettings global = GnwGuiSettings.getInstance();
		NetworkDesktop nm = (NetworkDesktop)global.getNetworkDesktop();
		Map<String, Map<String, URL>> networks = new TreeMap<String, Map<String, URL>>();
		
		//DREAM3 Size10
		Map<String, URL> dream3size10 = new TreeMap<String, URL>();
		dream3size10.put("InSilicoSize10-Ecoli1",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize10-Ecoli1.xml"));
		dream3size10.put("InSilicoSize10-Ecoli2",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize10-Ecoli2.xml"));
		dream3size10.put("InSilicoSize10-Yeast1",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize10-Yeast1.xml"));
		dream3size10.put("InSilicoSize10-Yeast2",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize10-Yeast2.xml"));
		dream3size10.put("InSilicoSize10-Yeast3",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize10-Yeast3.xml"));
		networks.put("DREAM3_In-Silico_Size_10", dream3size10);
		//DREAM3 Size50
		Map<String, URL> dream3size50 = new TreeMap<String, URL>();
		dream3size50.put("InSilicoSize50-Ecoli1",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize50-Ecoli1.xml"));
		dream3size50.put("InSilicoSize50-Ecoli2",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize50-Ecoli2.xml"));
		dream3size50.put("InSilicoSize50-Yeast1",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize50-Yeast1.xml"));
		dream3size50.put("InSilicoSize50-Yeast2",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize50-Yeast2.xml"));
		dream3size50.put("InSilicoSize50-Yeast3",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize50-Yeast3.xml"));
		networks.put("DREAM3_In-Silico_Size_50", dream3size50);
		//DREAM3 Size50
		Map<String, URL> dream3size100 = new TreeMap<String, URL>();
		dream3size100.put("InSilicoSize100-Ecoli1",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize100-Ecoli1.xml"));
		dream3size100.put("InSilicoSize100-Ecoli2",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize100-Ecoli2.xml"));
		dream3size100.put("InSilicoSize100-Yeast1",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize100-Yeast1.xml"));
		dream3size100.put("InSilicoSize100-Yeast2",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize100-Yeast2.xml"));
		dream3size100.put("InSilicoSize100-Yeast3",getClass().getResource("/ch/epfl/lis/networks/dream3/InSilicoSize100-Yeast3.xml"));
		networks.put("DREAM3_In-Silico_Size_100", dream3size100);
		//DREAM4 Size10
		Map<String, URL> dream4size10 = new TreeMap<String, URL>();
		dream4size10.put("insilico_size10_1",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size10_1.xml"));
		dream4size10.put("insilico_size10_2",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size10_2.xml"));
		dream4size10.put("insilico_size10_3",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size10_3.xml"));
		dream4size10.put("insilico_size10_4",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size10_4.xml"));
		dream4size10.put("insilico_size10_5",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size10_5.xml"));
		networks.put("DREAM4_In-Silico_Size_10", dream4size10);
		//DREAM4 Size100
		Map<String, URL> dream4size100 = new TreeMap<String, URL>();
		dream4size100.put("insilico_size100_1",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size100_1.xml"));
		dream4size100.put("insilico_size100_2",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size100_2.xml"));
		dream4size100.put("insilico_size100_3",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size100_3.xml"));
		dream4size100.put("insilico_size100_4",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size100_4.xml"));
		dream4size100.put("insilico_size100_5",getClass().getResource("/ch/epfl/lis/networks/dream4/insilico_size100_5.xml"));
		networks.put("DREAM4_In-Silico_Size_100", dream4size100);
		
		nm.addItemOnDesktop(new Folder("DREAM_Challenges",nm));
		
		Iterator<?> it = networks.entrySet().iterator();
		Iterator<?> itChallenge = null;
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
			nm.addItemOnDesktop(new Folder((String)pairs.getKey(),nm));
			nm.moveItemToFolder(nm.getLastElementAdded(), (Folder)nm.getIElementFromLabel("DREAM_Challenges"));
			itChallenge = ((Map<?,?>)pairs.getValue()).entrySet().iterator();
			while (itChallenge.hasNext()) {
				try {
					@SuppressWarnings("rawtypes")
					Map.Entry net = (Map.Entry)itChallenge.next();
					splashScreen_.setTaskInfo("Loading " + (String)net.getKey());
					IONetwork.loadItem((String)net.getKey(), (URL) net.getValue(), null);
					nm.moveItemToFolder(nm.getLastElementAdded(), (Folder)nm.getIElementFromLabel((String)pairs.getKey()));
				} catch (FileNotFoundException e) {
					log_.log(Level.WARNING, "Unable to load initial networks (FileNotFoundException): " + e.getMessage(), e);
				} catch (ParseException e) {
					log_.log(Level.WARNING, "Unable to load initial networks (ParseException): " + e.getMessage(), e);
				} catch (Exception e) {
					log_.log(Level.WARNING, "Unable to load initial networks (Exception): " + e.getMessage(), e);
				}
				
			}
			splashScreen_.stepDone();
		}

		nm.refreshDesktop();
	}

	// ----------------------------------------------------------------------------

	public void setMessages()
	{
		msgBar_.loadHtmlMessages(GnwGui.class.getResource("rsc/html-messages.txt"));
		//Collections.shuffle(msgBar_.getMessages());

		msgBar_.setNormalDuration(4 * 60000);
		msgBar_.setExtendedDuration(4 * 60000);
		msgBar_.setHideAfterFirstMessageDuration(4 * 60000);
		
		msgBar_.setMessage(msgBar_.getMessages().size() - 1);
		msgBar_.start();
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Start the exit process when user press on ESCAPE.
	 */
	@SuppressWarnings("serial")
	public void keyboardExit(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "EXIT");
		jp.getActionMap().put("EXIT", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				if (shouldExit())
					closeAction();
			}
		});
	}


	// ----------------------------------------------------------------------------


	/**
	 * Called to close the application.
	 */
	public void closeAction()
	{
		log_.info(GnwSettings.getInstance().getSignature());
		System.exit(0);
	}
}
