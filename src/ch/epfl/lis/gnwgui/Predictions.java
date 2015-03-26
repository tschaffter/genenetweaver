package ch.epfl.lis.gnwgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnw.evaluation.BackgroundAnalysis;
import ch.epfl.lis.gnw.evaluation.BatchPerformanceEvaluator;
import ch.epfl.lis.gnw.evaluation.MotifAnalysis;
import ch.epfl.lis.gnw.evaluation.Score;
import ch.epfl.lis.gnw.evaluation.ScoreAnalysis;
import ch.epfl.lis.gnwgui.Predictions.Evaluation.goldstandPrediction;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.idesktop.IFolder;
import ch.epfl.lis.gnwgui.windows.InternetConnectionDialog;
import ch.epfl.lis.gnwgui.windows.PredictionsPanel;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.utilities.filefilters.FilterFileTXT;
import ch.epfl.lis.utilities.filefilters.FilterReportGNR;
import ch.epfl.lis.imod.ImodNetwork;

@SuppressWarnings("serial")
public class Predictions extends PredictionsPanel {

	/** User accepeted upload boolean */
	private boolean acceptUpload_ = false;
	/** Last report opened */
	private String lastReport_ = null;
	/** Global settings */
	GnwGuiSettings global = GnwGuiSettings.getInstance();
	/** Array of predictions */
	private ArrayList<Evaluation> predictionsList_;
	/** BatchPerformanceEval */
	BatchPerformanceEvaluator bpe;
	/** Analysis thread */
	private Analysis analysis_;
	/** Inet thread */
	Inet inet = new Inet();
	/** Data file */
	String datafile_ = null;
	String datafolder_ = null;
	String dataarchive_ = null;
	String datareport_ = null;
	
	/** Server specific settings */
	private static String serverAddress_ = "http://tschaffter.ddns.net/gnr/"; // was "http://lisythaq.epfl.ch/gnw/", http://128.178.131.231/gnw/, http://lissrv3.epfl.ch/gnw/
	private static String serverCheckAddress_ = "http://www.google.com";
	
	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(Predictions.class.getName());

	
	/**
	 * Constructor
	 */
	public Predictions() {

		analysis_ = new Analysis();
		predictionsList_ = new ArrayList<Evaluation>();
		this.addPrediction("InferenceMethod1");

		emptyPanel.setBackground(Color.white);

		runButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				analysis_.start();		
			}
		});

		cancelButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				analysis_.stop();
			}
		});

		generateFromFileButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				analysis_.start(true);

			}
		});

		openLastReportButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				File report = new File(lastReport_);
				if ( report.exists()) {
					try {
						Desktop.getDesktop().open(report);
						lastReport_ = datareport_;

					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
				}
				else {
					String message = "Unable to find the last PDF report opened.\n\n";

					JOptionPane.showMessageDialog(			
							GnwGuiSettings.getInstance().getGnwGui().getFrame(),
							message,
							"Open PDF report",
							JOptionPane.WARNING_MESSAGE);

					log_.log(Level.WARNING, message);
					return;
				}
			}
		});

		// Set tool tips for all elements of the window
		addTooltips();
	}

	/** Add a prediction */
	public void addPrediction() {
		addPrediction("Untitled");
	}

	public void addPrediction(String name) {
		Evaluation newEval = new Evaluation(this,name);
		addPrediction(newEval);
	}

	public void addPrediction(Evaluation newEval) {
		tabbedPane_.remove(emptyPanel);
		if( tabbedPane_.getTabCount() < 5 ) {
			boolean nameExists = false;
			int n = 2;
			String name = newEval.getName();

			do {
				nameExists = false;
				for (int i=0;i<predictionsList_.size();i++) {

					if ( predictionsList_.get(i).getName().equals(newEval.getName()) ) {
						nameExists = true;
						newEval.setName(name + "" + n++);
					}
				}
			}
			while(nameExists);

			predictionsList_.add(newEval);
			tabbedPane_.addTab(newEval.getName(), newEval);
			tabbedPane_.setTabComponentAt(tabbedPane_.getTabCount()-1, new ButtonTabComponent(tabbedPane_));
			tabbedPane_.setSelectedIndex(tabbedPane_.getTabCount()-1);


		}
		else {
			log_.log(Level.WARNING, "Maximum number of prediction reached");
		}
		displayAddPredictionButton();
	}
	/** Display the add prediction button tab */

	public void displayAddPredictionButton() {
		if ( tabbedPane_.getTabCount() < 5) {
			tabbedPane_.add(emptyPanel);
			tabbedPane_.setTabComponentAt(tabbedPane_.getTabCount()-1, new AddTabButton());
			tabbedPane_.setBackgroundAt(tabbedPane_.getTabCount()-1, Color.WHITE);
			tabbedPane_.setForegroundAt(tabbedPane_.getTabCount()-1,Color.WHITE);
			tabbedPane_.setEnabledAt(tabbedPane_.getTabCount()-1,false);
		}
	}

	/** Remove a prediction */
	public void removePrediction(int i) {
		tabbedPane_.remove(emptyPanel);
		predictionsList_.remove(i);
		if( predictionsList_.size() > 0) {
			if ( tabbedPane_.getSelectedIndex() == 0)
				tabbedPane_.setSelectedIndex(0);
			else 
				tabbedPane_.setSelectedIndex(i-1);

		}
		displayAddPredictionButton();

	}

	/** Refresh the models of all predictions */
	public void refreshAllModels() {
		for(int i=0;i<predictionsList_.size();i++)
			predictionsList_.get(i).updateComboBoxModel();
	}


	/** Add tooltips for (about all) elements of the window */
	private void addTooltips()
	{
		networkMotifAnalysisCheckBox_.setToolTipText("<html>Perform a network motif analysis, where the performance of<br>" +
				"inference methods is profiled on local connectivity patterns.<br>" +
				"The network motif analysis often reveals systematic prediction errors,<br>" +
				"thereby indicating potential ways of network reconstruction improvements.<br>" +
		"The method is described in: Marbach et al. 2010. <i>PNAS</i>, 107(14):6286-6291.</html>");

		maxDivergenceSpinner_.setToolTipText("<html>Maximum value on the color scale of the divergence of prediction confidence<br>" +
		"for motif edges (this value affects only the visualization of the motifs)</html>");

		receiverOperatingCharacteristicCheckBox_.setToolTipText("<html>Compute area under ROC curve (AUROC) for each network prediction</html>");

		precisionAndRecallCheckBox_.setToolTipText("<html>Compute area under PR curve (AUPR) for each network prediction</html>");

		generatePdfReport_.setToolTipText("<html>Generate a PDF report with the result of the<br>" +
		"evaluation (motif analysis, AUROC, AUPR, etc.)</html>");

		plotPrCurvesCheckBox_.setToolTipText("<html>Include ROC and PR curves in the PDF report</html>");

		openPdfReportCheckBox_.setToolTipText("<html>If checked, the PDF report is automatically opened in your<br>" +
		"PDF reader (in addition to being saved)</html>");

		downloadFigureArchiveCheckBox_.setToolTipText("<html>If checked, a ZIP archive containing all the figures of the PDF<br>" +
		"report is saved (vectorial images in .svg format)</html>");

		evalAuthor_.setToolTipText("<html>The specified username will be written on the PDF report</html>");

		processIdEdit_.setToolTipText("<html>The report ID helps to differentiate several reports. In addition,<br>" +
				"this ID is used as root name for all the files saved, e.g<br>" +
				"- <i>report_id</i>.xml<br>" +
				"- <i>report_id</i>.pdf<br>" +
				"- <i>report_id</i>-figures.zip<br>" +
		"etc.</html>");

		note_.setToolTipText("<html>These notes are printed at the end of the report</html>");

		runButton_.setToolTipText("<html>Run the evaluation. The output is saved in a text<br>" +
									"file (XML format) and the PDF report is generated.</html>");

		cancelButton_.setToolTipText("<html>Cancel the evaluation</html>");

		generateFromFileButton_.setToolTipText("<html>Generate a PDF report from the output of a previous evaluation (XML file)</html>");

		openLastReportButton_.setToolTipText("<html>Open the last PDF report that was generated</html>");

		// tooltips disappear only after 10s
		ToolTipManager.sharedInstance().setDismissDelay(10000);

	}



	//Internet related class (test inet and upload/download)
	protected class Inet implements Runnable {
		Thread T;
		private boolean serverReachable_;
		private boolean internetReachable_;

		private String uploadPage_ = "generate.php";
		private String downloadPage_ = "getreport.php";
		private String zipArchive_ = "getzip.php";

		public Inet() {
			T = new Thread(this);
			T.start();
			serverReachable_ = false;


		}

		public boolean isServerReachable()      {
			try {
				//make a URL to a known source
				URL url = new URL(serverAddress_);

				//open a connection to that source
				HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();

				//trying to retrieve data from the source. If there
				//is no connection, this line will fail
				urlConnect.getContent();
			}
			catch (Exception e) {
				//e.printStackTrace();
				//log_.log(Level.WARNING,"Unable to contact the report server");
				return false;
			}
			return true;
		}

		public boolean isInternetReachable()      {

			try {
				//make a URL to a known source
				URL url = new URL(serverCheckAddress_);

				//open a connection to that source
				HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();

				//trying to retrieve data from the source. If there
				//is no connection, this line will fail
				urlConnect.getContent();
			}
			catch (Exception e) {
				//e.printStackTrace();
				//log_.log(Level.WARNING,"Unable to connect to internet");
				return false;
			}
			return true;
		}

		public void run() {
			internetReachable_ = isInternetReachable();
			if ( internetReachable_)
				serverReachable_ = isServerReachable();
		}

		public String convertStreamToString(InputStream is) throws IOException {
			/*
			 * To convert the InputStream to String we use the BufferedReader.readLine()
			 * method. We iterate until the BufferedReader return null which means
			 * there's no more data to read. Each line will appended to a StringBuilder
			 * and returned as String.
			 */
			if (is != null) {
				StringBuilder sb = new StringBuilder();
				String line;

				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					while ((line = reader.readLine()) != null) {
						sb.append(line).append("\n");
					}
				} finally {
					is.close();
				}
				return sb.toString();
			} else {        
				return "";
			}
		}

		public String upload(String filePath) throws Exception{
			//Default HTTP client
			HttpClient httpclient = new DefaultHttpClient();
			//Post method
			HttpPost httppost = new HttpPost(serverAddress_ + uploadPage_);
			//Form multipart (send file)
			MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE);

			File file = new File(filePath);

			entity.addPart("file", new FileBody(file));

			httppost.setEntity(entity);

			//Execute post
			log_.log(Level.INFO,"Uploading data file to server and generating the report. This operation may take sometime.");
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();

			String id = null;

			if (resEntity != null) {
				int status = response.getStatusLine().getStatusCode();
				if ( status != 200) 
					throw new Exception("Unable to upload data file to GNR server (status code: " + status + ")!");
				id = convertStreamToString(resEntity.getContent()).trim();
				resEntity.consumeContent();

			}
			httpclient.getConnectionManager().shutdown();
			log_.log(Level.INFO,"Succesfully uploaded data file to server (id: " + id + ")");

			datafile_ = datafolder_ + "/" + FilenameUtilities.getFilenameWithoutExtension(filePath);
			datafile_ += "-" + String.format("%05d", Integer.parseInt(id)) + ".xml";
			file.renameTo(new File(datafile_));
			log_.log(Level.INFO,"Successfully renamed data file to:");
			log_.log(Level.INFO,datafile_);
			return id;
		}

		public boolean download(String id) throws Exception {
			//Get the report
			//Default HTTP client
			HttpClient httpclient = new DefaultHttpClient();

			HttpGet httpget = new HttpGet(serverAddress_ + downloadPage_ + "?id=" + id);
			log_.log(Level.INFO,"Downloading PDF report");
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity resEntity = response.getEntity();

			if (resEntity != null ) {
				//Check status code
				if ( response.getStatusLine().getStatusCode() != 200)
					throw new Exception("Unable to download PDF report");

				if ( datafolder_ != null ) {
					datareport_ += "-" + String.format("%05d", Integer.parseInt(id)) + ".pdf";
					File outfile = new File(datareport_);
					InputStream in = resEntity.getContent();
					OutputStream out=new FileOutputStream(outfile);
					byte buf[]= new byte[1024];
					int len;
					while((len=in.read(buf))>0) {
						out.write(buf,0,len);
					}	    	
					out.close();
					log_.log(Level.INFO,"Successfully saved the PDF report:");
					log_.log(Level.INFO,datareport_);
				}
				resEntity.consumeContent();
			}
			else
				throw new Exception("Error while downloading PDF report");


			//Get the zip
			if ( getEvalDownloadFigures() ) {
				httpget = new HttpGet(serverAddress_ + zipArchive_ + "?id=" + id);
				log_.log(Level.INFO,"Downloading figures ZIP");
				response = httpclient.execute(httpget);
				resEntity = response.getEntity();

				if (resEntity != null) {
					if ( response.getStatusLine().getStatusCode() != 200)
						throw new Exception("Unable to download ZIP archive");

					if ( datafolder_ != null ) {

						dataarchive_ += "-" + String.format("%05d", Integer.parseInt(id)) + "-figures.zip";
						File outfile = new File(dataarchive_);
						InputStream in = resEntity.getContent();
						OutputStream out=new FileOutputStream(outfile);
						byte buf[]= new byte[1024];
						int len;
						while((len=in.read(buf))>0) {
							out.write(buf,0,len);
						}	    	
						out.close();
						log_.log(Level.INFO,"Successfully saved the figures:");
						log_.log(Level.INFO,dataarchive_);
					}
					resEntity.consumeContent();
				}
				else {
					throw new Exception("Error while downloading figures!");
				}
			}

			httpclient.getConnectionManager().shutdown();

			return true;
		}

		public boolean getInetStatus() { return internetReachable_; }
		public boolean getServerStatus() { return serverReachable_; }

	}

	protected class Analysis implements Runnable {

		private Thread myThread_;		/** Main Thread */
		private boolean onlyUpload_ = false;

		public Analysis() {
			super();
			myThread_ = null;
		}
		public void start(boolean onlyUpload) {
			onlyUpload_ = onlyUpload;
			start();
		}
		public void start() {
			
			if ( !onlyUpload_ ) {
				for(int i=0;i<predictionsList_.size();i++) {
					ArrayList<goldstandPrediction> gp = predictionsList_.get(i).getGoldstandardPrediction();
					for(int j=0;j<gp.size();j++) {
						if ( gp.get(j).getGoldStandard() == null || gp.get(j).getPredictionFile() == null) {
							String message = "One prediction does not have a valid goldstandard or prediction file.\n\n";

							JOptionPane.showMessageDialog(			
									global.getGnwGui().getFrame(),
									message,
									"Invalid goldstandard/prediction",
									JOptionPane.WARNING_MESSAGE);

							log_.log(Level.WARNING, message);
							return;
						}
					}
				}
			}
			if (myThread_ == null) {
				runButton_.setEnabled(false);
				snake_.setVisible(true);
				snake_.start();
				runCardLayout_.show(runCardPanel_, snakePanel_.getName());
				myThread_ = new Thread(this);
				myThread_.start();
			}
		}

		public void stop() {
			onlyUpload_ = false;
			snake_.setVisible(false);
			snake_.stop();
			runCardLayout_.show(runCardPanel_, runPanel_.getName());
			runButton_.setEnabled(true);
			myThread_ = null;
		}

		public void run() {

			

			String id = null;

			//Allow upload to internet
			if ( !acceptUpload_ && getEvalPDF()) {

				InternetConnectionDialog auw = new InternetConnectionDialog(GnwGuiSettings.getInstance().getGnwGui().getFrame());
				acceptUpload_ = auw.isAccepted();

				if ( !acceptUpload_) {
					stop();
					return;
				}
			}
			if ( getEvalPDF() ) {
				//check inet status
				inet.run();

				if ( inet.getInetStatus() && getEvalPDF()) {
					if ( inet.getServerStatus()) {
						log_.log(Level.INFO,"Connected to internet, proceeding.");
					}
					else {

						String message = "Unable to contact the GNW server (but internet connection was detected).\n\n" +
						"Please contact Thomas Schaffter (firstname.name@gmail.com).\n\n";

						JOptionPane.showMessageDialog(			
								global.getGnwGui().getFrame(),
								message,
								"GNW Server Unreachable",
								JOptionPane.WARNING_MESSAGE);

						log_.log(Level.WARNING, message);
						stop();
						return;
					}
				}
				else {

					String message = "Please check your internet connection.";

					JOptionPane.showMessageDialog(			
							global.getGnwGui().getFrame(),
							message,
							"Internet Unreachable",
							JOptionPane.WARNING_MESSAGE);

					log_.log(Level.WARNING, message);
					stop();
					return;
				}
			}
			//Select folder / file

			if ( onlyUpload_ ) {

				if ( !getEvalPDF() ) {
					String message = "You did not select \"Generate PDF report\". Nothing will be done.";

					JOptionPane.showMessageDialog(			
							global.getGnwGui().getFrame(),
							message,
							"Nothing to do.",
							JOptionPane.WARNING_MESSAGE);

					log_.log(Level.WARNING, message);
					stop();
					return;

				}

				IODialog dialog = new IODialog(global.getGnwGui().getFrame(), "Select XML",
						GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);

				dialog.addFilter(new FilterReportGNR());
				dialog.display();

				if ( dialog.getSelection() != null) {
					log_.log(Level.INFO,"All evaluation files will be saved in " + dialog.getDirectory());
					datafolder_ = dialog.getDirectory();
					GnwSettings.getInstance().setOutputDirectory(datafolder_);
					datafile_ = dialog.getSelection();
					dataarchive_ = datafolder_ + "/" + FilenameUtilities.getFilenameWithoutExtension(datafile_) ;
					datareport_ = datafolder_ + "/" + FilenameUtilities.getFilenameWithoutExtension(datafile_) ;


				}
				else {
					stop();
					return;
				}

			}
			else if (!onlyUpload_) {
				String report_id = getEvalProcessID().replaceAll(" ", "_");

				IODialog dialog = new IODialog(global.getGnwGui().getFrame(), "Select folder",
						GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);

				dialog.selectOnlyFolder(true);
				dialog.display();

				if ( dialog.getSelection() != null ) {
					log_.log(Level.INFO,"All evaluation files will be saved in " + dialog.getSelection());
					datafolder_ = dialog.getSelection();
					GnwSettings.getInstance().setOutputDirectory(datafolder_);
					datafile_ = datafolder_ + "/" + report_id + ".xml";
					dataarchive_ = datafolder_ + "/" + report_id ;
					datareport_ = datafolder_ + "/" + report_id ;

				}
				else {
					stop();
					return;
				}
				//Proceed to analysis
				log_.log(Level.INFO,"Evaluating predictions. This operation may take some time");
				analysis();

			}

			if ( myThread_ != null) {

				if ( getEvalPDF() ) {
					//check inet status
					inet.run();

					if ( inet.getInetStatus() && getEvalPDF()) {
						if ( inet.getServerStatus()) {
							//Upload/Download files
							try {
								//upload
								id = inet.upload(datafile_);
								if ( myThread_ == null)
									return;

								if( id != null) {
									//download
									inet.download(id);

									lastReport_ = datareport_;
									openLastReportButton_.setEnabled(true);
									if ( myThread_ == null)
										return;
									//Open pdf
									if ( Desktop.isDesktopSupported() && getEvalPDF() && getEvalOpen()) {
										File report = new File(datareport_);
										try {
											Desktop.getDesktop().open(report);

										} catch (IOException e) {
											// TODO Auto-generated catch block
											log_.log(Level.WARNING,e.getMessage(),e);
											
										}
									}
								}
								else {
									stop();
									log_.log(Level.WARNING,"Error while uploading data file!");
									return;
								}

							} catch (Exception e) {
								log_.log(Level.WARNING,e.getMessage(),e);
								stop();
								return;
							}
						}
						else {

							String message = "Unable to contact the GNW server (but internet connection was detected).\n\n" +
							"Please contact Thomas Schaffter (firstname.name@gmail.com).\n\n";

							JOptionPane.showMessageDialog(			
									global.getGnwGui().getFrame(),
									message,
									"GNW Server Unreachable",
									JOptionPane.WARNING_MESSAGE);

							log_.log(Level.WARNING, message);
						}
					}
					else {

						String message = "Please check your internet connection.";

						JOptionPane.showMessageDialog(			
								global.getGnwGui().getFrame(),
								message,
								"Internet Unreachable",
								JOptionPane.WARNING_MESSAGE);

						log_.log(Level.WARNING, message);	
					}
				}
			}
			stop();
		}

		/** Perform analysis */
		public void analysis() {


			/** List of all GoldStandard networks for analysis */
			ArrayList<ImodNetwork> goldstandardList = new ArrayList<ImodNetwork>();
			/** List of all the prediction files */
			ArrayList<URL> predictionURLList = new ArrayList<URL>();
			/** Output string with processed data */
			String output = "";
			/** motifs array */
			ArrayList<ArrayList<Double>> motifsData;
			/** Current MotifAnalysis */
			MotifAnalysis ma;
			/** Background Analysis */
			BackgroundAnalysis ba;
			/** Score Analysis */
			ScoreAnalysis sa;
			/** Scores array */
			ArrayList<Score> scores_;
			/** Date */
			Date today = new Date();
			output = output.concat("<?xml version=\"1.0\"?>\n");
			
			output = output.concat("<GNW_analysis>\n");
			
			output = output.concat("<param name=\"version\">" + GnwSettings.getInstance().getGnwVersion() + "</param>\n");
			output = output.concat("<param name=\"author\">" + getEvalAuthor() + "</param>\n");
			output = output.concat("<param name=\"maxDivergence\">" + getMaxDivergence() + "</param>\n");
			output = output.concat("<param name=\"date\">" + today.toString() + "</param>\n");
			output = output.concat("<param name=\"processid\">" + getEvalProcessID() + "</param>\n");
			output = output.concat("<param name=\"note\">" + getEvalNote() + "</param>\n");
			output = output.concat("<param name=\"plots\">" + getEvalPlots() + "</param>\n");
			output = output.concat("<param name=\"pr\">" + getEvalPR() + "</param>\n");
			output = output.concat("<param name=\"roc\">" + getEvalROC() + "</param>\n");
			output = output.concat("<param name=\"motifAnalysis\">" + getEvalMotifAnalysis() + "</param>\n");
			output = output.concat("<param name=\"numTabs\">" + predictionsList_.size() + "</param>\n");
			output = output.concat("<param name=\"openReport\">" + getEvalOpen() + "</param>\n");
			output = output.concat("<param name=\"figuresArchive\">" + getEvalDownloadFigures() + "</param>\n");
			output = output.concat("<param name=\"significanceLevel\">" + getSignificanceLevel() + "</param>\n");
			output = output.concat("<param name=\"useBonferroni\">" + getUseBonferroni() + "</param>");
			
			String num_predictions_per_tab = "";
			
			for(int k=0;k<predictionsList_.size();k++) {
				num_predictions_per_tab += "" + predictionsList_.get(k).getGoldstandardPrediction().size() + " ";
			}
			
			output = output.concat("<param name=\"num_predictions_per_tab\">" + num_predictions_per_tab + "</param>\n");

			if(myThread_ == null)
				return;

			for(int k=0;k<predictionsList_.size();k++) {
				goldstandardList = predictionsList_.get(k).getGoldStandards();
				predictionURLList = predictionsList_.get(k).getPredictions();
				if ( goldstandardList.size() != predictionURLList.size() ) {
					log_.log(Level.INFO,predictionsList_.get(k).getName() + ": Number of goldstandards not egal to the number of predictions");
				}
				else {
					bpe = new BatchPerformanceEvaluator(goldstandardList);
					bpe.setPlotROC(getEvalROC());
					bpe.setAUROC(getEvalROC());
					bpe.setPlotPR(getEvalPR());
					bpe.setAUPR(getEvalPR());
					bpe.setNetworkMotifAnalysis(getEvalMotifAnalysis());
					log_.log(Level.INFO,"Evaluating " + predictionsList_.get(k).getName());
					log_.log(Level.INFO,"========================================");
					try {
						bpe.loadPredictions(predictionURLList, false);
						bpe.run();
						if(myThread_ == null)
							return;
					}
					catch(RuntimeException exception) {		
						String msg = "Error while evaluating " + predictionsList_.get(k).getName() + "\n" + exception.getMessage();
						JOptionPane.showMessageDialog(			
								global.getGnwGui().getFrame(),
								msg,
								"Evaluation error",
								JOptionPane.WARNING_MESSAGE);
						stop();
						log_.log(Level.WARNING,msg, exception);
						return;
					}

					/** Motif Analysis Data */
					if ( getEvalMotifAnalysis() && bpe.getMotifAnalyzer() != null ) {	
						ma = bpe.getMotifAnalyzer();
						output = output.concat("\t <MotifAnalysis name=\""+ predictionsList_.get(k).getName() + "\">\n");

						
						
						/** Number of motif instances
						 * 
						 */
						int[] motifInstances = ma.getNumMotifInstances();
						output = output.concat("\t\t <NumMotifInstances>\n");
						for (int i=0;i<motifInstances.length;i++) {
							output = output.concat("\t\t\t <motif id=\"" + i + "\">"+motifInstances[i]+"</motif>\n");
						}
						
						output = output.concat("\t\t </NumMotifInstances>\n");
						
						/** Median Rank 
						 * 
						 */
						motifsData = ma.getMedianRanks();
						output = output.concat("\t\t <MedianRank>\n");
						for(int i=0;i<motifsData.size();i++) {
							

								output = output.concat("\t\t\t <motif id=\"" + i + "\">\n");
								for(int j=0;j<motifsData.get(i).size();j++) {
									output = output.concat("\t\t\t\t <edge id=\"" + j + "\">"+motifsData.get(i).get(j)+"</edge>\n");
								}
								output = output.concat("\t\t\t </motif>\n");
							
						}
						output = output.concat("\t\t </MedianRank>\n");
						if(myThread_ == null)
							return;
						
						/** DivMedianRank 
						 * 
						 */
						
						motifsData = ma.getDivMedianRanks();

						output = output.concat("\t\t <DivMedianRank>\n");
						for(int i=0;i<motifsData.size();i++) {
							
								output = output.concat("\t\t\t <motif id=\"" + i + "\">\n");
								for(int j=0;j<motifsData.get(i).size();j++) {
									output = output.concat("\t\t\t\t <edge id=\"" + j + "\">"+motifsData.get(i).get(j)+"</edge>\n");
								}
								output = output.concat("\t\t\t </motif>\n");
							
						}
						output = output.concat("\t\t </DivMedianRank>\n");
						if(myThread_ == null)
							return;
						
						/** pvals 
						 * 
						 */
						
						motifsData = ma.getPvals();
						output = output.concat("\t\t <Pvals>\n");
						for(int i=0;i<motifsData.size();i++) {
							
								output = output.concat("\t\t\t <motif id=\"" + i + "\">\n");
								for(int j=0;j<motifsData.get(i).size();j++)
								{
									output = output.concat("\t\t\t\t <edge id=\"" + j + "\">" + motifsData.get(i).get(j) + "</edge>\n");
								}
								output = output.concat("\t\t\t </motif>\n");
							
						}
						output = output.concat("\t\t </Pvals>\n");

						/** Background Analysis */
						output = output.concat("\t\t <Background>\n");
						ba = bpe.getBackgroundAnalyzer();
						if ( ba != null ) {
							output = output.concat("\t\t\t <motif id=\"0\">\n");
							output = output.concat("\t\t\t\t <edge id=\"0\">" + ba.getMedianTrueEdges() + "</edge>\n");
							output = output.concat("\t\t\t\t <edge id=\"1\">" + ba.getMedianAbsentEdges() + "</edge>\n");
							output = output.concat("\t\t\t\t <edge id=\"2\">" + ba.getMedianBackEdges() + "</edge>\n");
							output = output.concat("\t\t\t </motif>\n");
						}
						output = output.concat("\t\t </Background>\n");
						/** End of Motif Analysis */
						output = output.concat("\t </MotifAnalysis>\n");
					}
					if(myThread_ == null)
						return;

					if( getEvalPR() || getEvalROC()) {
						sa = bpe.getScoreAnalyzer();
						if(myThread_ == null)
							return;
						Score s;
						if ( sa != null) {
							scores_ = sa.getAllScores();
							if( getEvalPR()) {

								output = output.concat("\t <PR name=\""+ predictionsList_.get(k).getName() + "\" >\n");
								for(int i=0;i<scores_.size();i++) {
									s = scores_.get(i);

									output = output.concat("\t\t <goldstandard name=\""+s.getGoldStandard() + "\" prediction=\"" + s.getPredictionName() + "\" AUPR=\"" + s.getAUPR() + "\">\n");
									output = output.concat(s.getPRString()+"\n");
									output = output.concat("\t\t </goldstandard>\n");
								}

								output = output.concat("\t </PR>\n");
							}

							if( getEvalROC()) {
								output = output.concat("\t <ROC name=\""+ predictionsList_.get(k).getName() + "\">\n");
								for(int i=0;i<scores_.size();i++) {
									s = scores_.get(i);
									output = output.concat("\t\t <goldstandard name=\""+s.getGoldStandard() + "\" prediction=\"" + s.getPredictionName() + "\" AUROC=\"" + s.getAUROC() + "\">\n");
									output = output.concat(s.getROCString()+"\n");
									output = output.concat("\t\t </goldstandard>\n");
								}
								output = output.concat("\t </ROC>\n");
							}
						}
					}
					
					//Add new content here to xml
				}
			}
			if(myThread_ == null)
				return;
			/** End of Analysis */
			output = output.concat("</GNW_analysis>\n");

			Charset cs = Charset.forName("UTF-8");
			
			if ( datafile_ != null ) {
				File outfile = new File(datafile_);
				try {
					FileOutputStream fos = new FileOutputStream(outfile);
					FileChannel channel = fos.getChannel();
					channel.write(cs.encode(output));
					channel.close();
					fos.close();
					log_.log(Level.INFO,"\nSuccessfully saved XML data file!");
				} catch (IOException e) {
					log_.log(Level.WARNING,"Unable to save data file to " + datafile_);
					stop();
				}
			}

		}

		double roundTwoDecimals(double d) {
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			return Double.valueOf(twoDForm.format(d));
		}
	}

	protected class Evaluation extends PredictionTab {

		/** List of GoldStandards/Predictions for this evaluation*/
		private ArrayList<goldstandPrediction> list_;
		/** Name of this Evaluation */
		private String name_;
		/** Directory listing Thread */
		private runner r;
		private Predictions parent_;
		
		public void init() {
			r = new runner();
			r.setDirectory(getPredictionsFolderTextField().getText());
			//r.start();
			
			evalAddPrediction_.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					addPrediction();
				}
			});

			evalPredictionsFolderTextField_.getDocument().addDocumentListener(new DocumentListener() {

				public void removeUpdate(DocumentEvent e) {
					r.setDirectory(getPredictionsFolderTextField().getText());
					//r.start();
				}

				public void insertUpdate(DocumentEvent e) {
					r.setDirectory(getPredictionsFolderTextField().getText());
					//r.start();
				}

				public void changedUpdate(DocumentEvent e) {
					r.setDirectory(getPredictionsFolderTextField().getText());
					//r.start();
				}
			});

			evalPredictionsFolderBrowse_.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					browseFolder();
				}
			});
			
			evalReload_.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					r.setDirectory(getPredictionsFolderTextField().getText());
					r.start();
				}
			});
			
			evalMatch_.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					 updateComboBoxModel();
					
				}
			});
			
		}
		public Evaluation(Predictions parent, String name) {
			
			parent_ = parent;
			name_ = name;
			list_ = new ArrayList<Predictions.Evaluation.goldstandPrediction>();
			init();		

			this.addPrediction();
		}


		public Evaluation(Evaluation model) {
			parent_ = model.parent_;
			name_ = "Copy of " + model.name_;
			list_ = new ArrayList<Predictions.Evaluation.goldstandPrediction>();
			
			evalPredictionsFolderTextField_.setText(model.evalPredictionsFolderTextField_.getText());
			
			ArrayList<goldstandPrediction> modelList = (ArrayList<goldstandPrediction>) model.getGoldstandardPrediction();
			init();
			if ( model.r != null)
				r.setFileList(model.r.getFileList());
			
			for (int i=0;i<modelList.size() ;i++) {
				addPrediction();
				if ( modelList.get(i).getGoldStandard() != null )
					list_.get(i).goldStandardsModel_.setSelectedItem(modelList.get(i).getGoldStandard());
				if ( modelList.get(i).getPredictionFile() != null )		
					list_.get(i).predictionsModel_.setSelectedItem(modelList.get(i).getPredictionFile());
			}
			
		}

		public void browseFolder() {
			IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Select folder",
					GnwSettings.getInstance().getOutputDirectory(), IODialog.LOAD);
			dialog.addFilter(new FilterFileTXT());
			dialog.setAcceptAllFileFilterUsed(false);
			dialog.selectOnlyFolder(true);
			dialog.setSelection(evalPredictionsFolderTextField_.getText());
			dialog.display();

			try {
				if (dialog.getSelection() != null) {
					evalPredictionsFolderTextField_.setText(dialog.getSelection());
					r.setDirectory(getPredictionsFolderTextField().getText());
					r.start();
				}
			}
			catch (Exception e) {}
		}

		public void setName(String name) { name_ = name;}
		public String getName() { return name_;}

		public void setPredictionComboBoxMessage(String message) {
			if ( list_ != null)
				for(int i=0;i<list_.size();i++)
					list_.get(i).setPredictionComboBoxMessage(message);
		}
		public void updateComboBoxModel() {
			if ( list_ != null)
				for(int i=0;i<list_.size();i++)
					list_.get(i).updateComboBoxModel();
		}

		public ArrayList<URL> getPredictions() {
			/** a prediction file */
			File prediction = null;
			/** List of all the prediction files */
			ArrayList<URL> predictions = new ArrayList<URL>();
			//check prediction file
			for ( int i=0;i<list_.size();i++) {
				prediction = list_.get(i).getPredictionFile();
				if (prediction != null) {

					try {
						predictions.add(prediction.getAbsoluteFile().toURI().toURL());
					} catch (MalformedURLException e1) {
						log_.log(Level.WARNING,"@" + name_ + " Malformated URL for prediction " + (i+1));
					}
				}
			}
			return predictions;
		}

		public ArrayList<ImodNetwork> getGoldStandards() {
			/** a goldstandard network */
			Object goldstandard = null;
			/** List of all GoldStandards */
			ArrayList<ImodNetwork> goldstandards = new ArrayList<ImodNetwork>();

			for (int i=0;i<list_.size();i++) {
				goldstandard = list_.get(i).getGoldStandard();
				//Check goldstandard
				if (goldstandard != null) {
					if ( goldstandard.getClass() == StructureElement.class )
						goldstandards.add(((StructureElement)goldstandard).getNetwork());
					if ( goldstandard.getClass() == DynamicalModelElement.class )
						goldstandards.add(((DynamicalModelElement)goldstandard).getGeneNetwork());						
				}
				else {
					log_.log(Level.WARNING,"@" + name_ + " No GoldStandard selected for prediction " + (i+1));
				}
			}
			return goldstandards;
		}

		/** Add a prediction */
		public void addPrediction() {
			list_.add(new goldstandPrediction(this));
		}

		/** Get goldstandardPrecition list */
		public ArrayList<goldstandPrediction> getGoldstandardPrediction() {
			return (ArrayList<goldstandPrediction>) list_;
		}

		protected class goldstandPrediction extends GoldStandardPredictionPanel {

			protected IElementComboBoxModel goldStandardsModel_;
			protected DefaultComboBoxModel<Object> predictionsModel_;
			protected Evaluation parent_;

			public goldstandPrediction(Evaluation parent) {

				parent_ = parent;

				goldStandardsComboBox_.setRenderer(new ComboBoxRender("Select a network"));
				goldStandardsComboBox_.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {

						 if (goldStandardsComboBox_.getSelectedItem() == null) {
							 	//Keep popup opened if it is not a valid selection
					            SwingUtilities.invokeLater(new Runnable() {
					              public void run() {
					                goldStandardsComboBox_.showPopup();
					              }
					            });
					          } else {
					        	  matchGoldstandPrediction();
					          }
					}
				});
				
				goldStandardsModel_ = new IElementComboBoxModel();
				predictionsComboBox_.setRenderer(new ComboBoxRender("Select a prediction file"));
				predictionsModel_ = new DefaultComboBoxModel<Object>();
				predictionsComboBox_.setSelectedItem(null);

				goldStandardsComboBox_.setModel(goldStandardsModel_);
				predictionsComboBox_.setModel(predictionsModel_);

				parent_.getListPanel().add(this);
				parent_.getListPanel().revalidate();
				parent_.getListPanel().repaint();

				removeButton_.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						remove();
					}
				});
				
				updateComboBoxModel();
				matchGoldstandPrediction();
				
				if ( r != null && r.getStatus() == runner.LISTING)
					setPredictionComboBoxMessage("Listing directory...");
			}

			/** Remove this prediction */
			private void remove() {
				list_.remove(this);
				parent_.getListPanel().remove(this);
				parent_.getListPanel().revalidate();
				parent_.getListPanel().repaint();
			}

			/** Return the goldstandard network or null */
			public Object getGoldStandard() {
				return goldStandardsComboBox_.getSelectedItem();
			}

			/** Return the prediction file selected */
			public File getPredictionFile() {
				if ( predictionsComboBox_.getSelectedItem() != null)
				if ( predictionsComboBox_.getSelectedItem().getClass() == File.class)
					return (File)predictionsComboBox_.getSelectedItem();
				return null;
			}

			public void setPredictionComboBoxMessage(String message) {
				predictionsModel_.removeAllElements();
				predictionsModel_.addElement(new JLabel(message));
			}

			/** Update combobox model */
			@SuppressWarnings("static-access")
			public void updateComboBoxModel() {
				//Update GoldStandards list
				goldStandardsModel_.updateList(GnwGuiSettings.getInstance().getNetworkDesktop().getAllNetworks());
				//Update Predictions list
				//ArrayList<File> predictionFiles = FilenameUtilities.readDirectory(parent_.getPredictionsFolderTextField().getText(),".txt", true);
				if ( r != null ) {
				ArrayList<File> predictionFiles = r.getFileList();
				predictionsModel_.removeAllElements();
				if (predictionFiles != null) {
					Collections.sort(predictionFiles);
					for (int i=0;i<predictionFiles.size();i++)
						predictionsModel_.addElement(predictionFiles.get(i));
				}
				if ( r.getStatus() == r.FINISHED)
					matchGoldstandPrediction();
				}
				
			}
			
			public void matchGoldstandPrediction() {
				//Try to match goldstandard with prediction files
				int match = Integer.MAX_VALUE;
				String s1,s2;
				File bestmatch = null;
				if (goldStandardsComboBox_.getSelectedItem() != null && predictionsModel_.getSize() > 0 && r.getFileList() != null) {
					for ( File file : r.getFileList()) {
						s1 = name_.toLowerCase() +  ((IElement)goldStandardsComboBox_.getSelectedItem()).getLabel().toLowerCase();
						s2 = FilenameUtilities.getFilenameWithoutExtension(file.getName()).toLowerCase();
						if ( LevenshteinDistance(s1, s2) < match ) {
							match = LevenshteinDistance(s1, s2);
							bestmatch = file;
						}
						//Check for DREAM4
						if ( LevenshteinDistance("dream4_" + s1, s2) < match ) {
							match = LevenshteinDistance("dream4_" + s1, s2);
							bestmatch = file;
						}

					}
					predictionsComboBox_.setSelectedItem(bestmatch);
				}
			}

			/**
			 * Levenshtein Distance, implemention from wikipedia
			 * @param s1 
			 * @param s2
			 * @return the Levenshtein distance betwen s1 and s2
			 */
			public int LevenshteinDistance(String s1, String s2) {
				int m = s1.length();
				int n = s2.length();
				int[][] d = new int[m][n];

				for (int i = 0;i<m;i++) 
					d[i][0] = i;
				for (int j = 0;j<n;j++) 
					d[0][j] = j;

				for( int j = 1;j<n;j++) {
					for( int i = 1;i<m;i++) {
						if ( s1.charAt(i) == s2.charAt(j) ) 
							d[i][j] = d[i-1][j-1];
						else
							d[i][j] = Math.min(d[i-1][j]+1, Math.min(d[i][j-1]+1, d[i-1][j-1]+1));
					}
				}
				return d[m-1][n-1];
			}
		}
		protected class runner implements Runnable {
			static final int LISTING = 0;
			static final int FINISHED = 1;
			public Thread T;
			boolean go_ = false;
			boolean finished_ = true;
			boolean restart_ = true;
			private FileFilter filter_;
			private String directory_ = "";
			private ArrayList<File> filelist_ = null;
			private int status = runner.FINISHED;
			public  runner() {
				T = new Thread(this);

				
				filter_ = new FileFilter() {
					public boolean accept(File file){
						if (file.isDirectory()) {
							return !file.getName().startsWith(".");
						}
						else if(file.isFile()) {
							return file.getName().endsWith(".txt");
						}

						return false;
					}
				};

				
				
			}

			synchronized void start() {
				if (!T.isAlive())
					T.start();
				
				go_ = false;
				restart_ = true;
				finished_ = false;
				notify();
			}

			synchronized void stop() {
				//log_.log(Level.INFO,"stop()");
				go_ = false;
				finished_ = true;
				notify();
			}

			public ArrayList<File> readDirectory(String directory, boolean rec) {
				//log_.log(Level.INFO,"Reading directed " + directory);
				if ( !go_) {
					finished_ = true;
					return null;
				}

				ArrayList<File> list = new ArrayList<File>();
				ArrayList<File> children = null;
				//Filter to read only files

				File dir = new File(directory);

				if ( !dir.isDirectory() && dir.exists())
					dir = new File(FilenameUtilities.getDirectory(directory));

				if ( !dir.exists() ) 
					return null;


				File[] files = dir.listFiles(filter_);

				if ( !go_) {
					stop();
					return null;
				}
				
				if ( files == null) {
					stop();
					return null;
				}
				for(int i=0;i<files.length;i++) {
					if ( !go_) {
						stop();
						return null;
					}

					if ( files[i].isDirectory() && rec) {
						if ( !go_) {
							stop();
							return null;
						}
						children = readDirectory(files[i].getAbsolutePath(), rec);
						if ( !go_) {
							stop();
							return null;
						}
						if ( children != null) {
							list.addAll(children);
						}
					}
					if ( files[i].isFile()) 
						list.add(files[i]);

					if ( !go_) {
						stop();
						return null;
					}
				}

				return list;
			}
			synchronized void waiter() throws InterruptedException {
				//log_.log(Level.INFO,"Waiting....");
				wait();
			}
			public void run() {

				while(true) {
					//log_.log(Level.INFO,"run()" + go_);
					if (go_) {
						restart_ = false;
						//log_.log(Level.INFO,"go");
						runButton_.setEnabled(false);
						status = runner.LISTING;
						setPredictionComboBoxMessage("Listing directory...");
						filelist_ = readDirectory(directory_, true);
						status = runner.FINISHED;
						if ( filelist_ != null) {
							//log_.log(Level.INFO,"Finished reading...(" + filelist_.size() +" file(s) found)");
							updateComboBoxModel();
						}
						else {
							filelist_ = new ArrayList<File>();
							updateComboBoxModel();
						}
						
						runButton_.setEnabled(true);
						stop();
					}
					try {
						if (!restart_)
							
							waiter();
						else
							go_ = true;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			public void setDirectory(String dir) {
				directory_ = dir;
			}

			public ArrayList<File> getFileList() {return filelist_;}
			public void setFileList(final ArrayList<File> array) {filelist_ = array;}
			public int getStatus() {return status;};
		}

	}


	/**
	 * Render for ComboBox
	 * @author gilles
	 *
	 */

	protected class ComboBoxRender extends JLabel implements ListCellRenderer<Object> {

		/** Network icon */
		ImageIcon iconStructure = new ImageIcon(GnwGuiSettings.getInstance().getStructureIcon24());
		ImageIcon iconDynamical = new ImageIcon(GnwGuiSettings.getInstance().getGrnIcon24());
		/** Prediction icon */
		ImageIcon iconPrediction = new ImageIcon(GnwGuiSettings.getInstance().getFileIcon());
		/** Folder icon */
		ImageIcon iconFolder = new ImageIcon(GnwGuiSettings.getInstance().getFolderIcon24());
		/** Type of cell renderer */
		String selectMessage;

		public ComboBoxRender(String msg) {
			selectMessage = msg;
			setOpaque(true);
		}

		public ComboBoxRender() {
			selectMessage = "Select an item";
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String name = null;
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			if (value != null) {
				setFont(list.getFont());
				if ( value.getClass() == DynamicalModelElement.class || value.getClass() == StructureElement.class) {
					setText(printNSpace(((IElement)value).getNumberOfFather(0)) + ((IElement)value).getLabel());
					if ( value.getClass() == StructureElement.class )
						setIcon(iconStructure);
					if ( value.getClass() == DynamicalModelElement.class )
						setIcon(iconDynamical);			
				}
				if (value.getClass() == File.class ) {
					name = ((File)value).getName();
					if ( name.length() > 55)
						setText(name.substring(0,55));
					else
						setText(name);
					setIcon(iconPrediction);
				}
				if( value.getClass() == Folder.class) {
					setText(printNSpace(((IElement)value).getNumberOfFather(0)) + ((IFolder)value).getLabel());
					setIcon(iconFolder);
					
				}
				if( value.getClass() == JLabel.class) {
					setText(((JLabel)value).getText());
				}
			}
			else {
				setText(selectMessage);
			}
			if ( list.getModel().getSize() <= 0 ) {
				setText("No prediction file found");
			}
			


			return this;
		}

		private String printNSpace(int n) {
			String s="";
			for (int i=0;i<n;i++)
				s+= "  ";

			return s;
		}

	}

	/**
	 * ComboBox Model
	 * @author gilles
	 *
	 */

	protected class IElementComboBoxModel extends AbstractListModel<Object> implements MutableComboBoxModel<Object> {

		private Object selectedItem;
		private ArrayList<IElement> list;


		public IElementComboBoxModel() {
			list = new ArrayList<IElement>();

		}

		// ListModel
		public int getSize() { 
			// Return the size of the ArrayList
			return list.size();
		}

		public Object getElementAt(int i) {
			// Return the element at the specified position
			return list.get(i);
		}

		// ComboBoxModel

		public Object getSelectedItem() {
			return selectedItem;
		}

		public void setSelectedItem(Object newValue) {
			selectedItem = newValue;
			
			if (selectedItem != null && selectedItem.getClass() == Folder.class)
				setSelectedItem(null);
		}

		// MutableComboBoxModel
		public void addElement(Object element) {
			// Insert the element
			list.add((IElement)element);

			// Added at end, notify ListDataListener objects
			int length = getSize();
			fireIntervalAdded(this, length-1, length-1);
		}

		public void insertElementAt(Object element, int index) {
			// Insert the element at the specified position
			list.add(index, (IElement)element);

			// Added in middle, notify ListDataListener objects
			fireIntervalAdded(this, index, index);
		}

		public void removeElement(Object element) {
			// Find out position
			int index = list.indexOf(element);

			if (index != -1) {

				// Remove an element
				list.remove(element);

				// Removed from middle, notify ListDataListener objects
				fireIntervalRemoved(this, index, index);
			}
		}

		public void removeElementAt(int index) {
			if (getSize() >= index) {

				// Remove an element at the specified position
				list.remove(index);
				// Removed from index, notify ListDataListener objects
				fireIntervalRemoved(this, index, index);
			}
		}

		public void updateList(ArrayList<IElement> newList) {
			list.clear();
			for (int i=0;i<newList.size();i++) {
				addElement(newList.get(i));
			}
		}


	}

	public class AddTabButton extends JButton implements ActionListener {
		public AddTabButton() {
			int size = 22;
			setPreferredSize(new Dimension(size, size));
			//Make the button looks the same for all Laf's
			setUI(new BasicButtonUI());
			//Make it transparent
			setContentAreaFilled(false);
			//No need to be focusable
			setFocusable(false);
			setBorderPainted(false);
			//Making nice rollover effect
			//we use the same listener for all buttons
			addMouseListener(addTabButtonMouseListener);
			setRolloverEnabled(true);
			//Close the proper tab by clicking the button
			addActionListener(this);

			setIcon(new ImageIcon(GnwGuiSettings.getInstance().getNewIcon()));

			// Set tool tips for all elements of the window
			addTooltips();
		}

		private void addTooltips()
		{
			this.setToolTipText("<html>Click to create a new tab for an additional inference method to be evaluated</html>");

			// tooltips disappear only after 10s
			ToolTipManager.sharedInstance().setDismissDelay(10000);
		}

		public void actionPerformed(ActionEvent e) {
			addPrediction();
		}

		//we don't want to update UI for this button
		public void updateUI() {
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
		}

	}

	private final MouseListener addTabButtonMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
				button.setOpaque(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
				button.setOpaque(false);
			}
		}
	};

	public class ButtonTabComponent extends JPanel {

		private final JTabbedPane pane;
		private JLabel label;
		private JTextField name;
		JButton removeButton;
		JButton copyButton;

		public ButtonTabComponent(final JTabbedPane pane) {


			//unset default FlowLayout' gaps
			super(new FlowLayout(FlowLayout.LEFT, 0, 0));
			if (pane == null) {
				throw new NullPointerException("TabbedPane is null");
			}
			this.pane = pane;
			setOpaque(false);
			setFocusable(false);
			removeButton = new RemoveButton();
			copyButton = new CopyButton();
			//make JLabel read titles from JTabbedPane
			label = new JLabel() {
				public String getText() {
					int i = pane.indexOfTabComponent(ButtonTabComponent.this);
					if (i != -1) {
						return pane.getTitleAt(i);
					}
					return null;
				}
			};
			label.setMinimumSize(new Dimension(0,22));
			name = new JTextField();
			name.setText(label.getText());
			name.setEditable(true);
			name.setVisible(false);

			add(name);
			add(label);

			label.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					pane.setSelectedIndex(pane.indexOfTabComponent(ButtonTabComponent.this));

					if(e.getClickCount() >= 2) {
						name.setSize(label.getSize());
						label.setVisible(false);
						name.setText(label.getText());
						name.setVisible(true);
					}

				}

			});

			name.addMouseListener(new MouseAdapter() {
				public void mouseExited(MouseEvent e) {
					updateTitle();
				}
			});

			name.addKeyListener( new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						updateTitle();
					}
				}
			});


			//add more space between the label and the button
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			name.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			//tab button

			add(copyButton);
			add(removeButton);

			//add more space to the top of the component
			setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			// Add tool tips
			addTooltips();
		}

		private void addTooltips()
		{
			label.setToolTipText("<html>Create one tab for each inference method to be evaluated.<br>" +
			"Double-click on the tab to rename it.<html>");

			// tooltips disappear only after 10s
			ToolTipManager.sharedInstance().setDismissDelay(10000);
		}

		private void updateTitle() {
			boolean nameExists = false;
			int n=1;

			//if (name.getText().isEmpty()) {
			if (name.getText().compareTo("") == 0)
				name.setText("Untitled");

			name.setVisible(false);

			String newName = name.getText();

			do {
				nameExists = false;
				for (int i=0;i<predictionsList_.size();i++) {

					if ( predictionsList_.get(i).getName().equals(newName) && i != pane.getSelectedIndex() ) {
						nameExists = true;
						newName = name.getText() + "" + n++;
					}
				}
			}
			while(nameExists);

			pane.setTitleAt(pane.getSelectedIndex(),newName);
			predictionsList_.get(pane.getSelectedIndex()).setName(newName);
			label.setVisible(true);
		}




		private class RemoveButton extends JButton implements ActionListener {

			public RemoveButton() {
				int size = 22;
				setPreferredSize(new Dimension(size, size));
				setIcon(new ImageIcon(GnwGuiSettings.getInstance().getRemoveIcon()));
				//Make the button looks the same for all Laf's
				setUI(new BasicButtonUI());
				//Make it transparent
				setContentAreaFilled(false);
				//No need to be focusable
				setFocusable(false);
				setBorderPainted(false);
				setEnabled(false);
				//Making nice rollover effect
				//we use the same listener for all buttons
				addMouseListener(buttonMouseListener);
				setRolloverEnabled(true);
				//Close the proper tab by clicking the button
				addActionListener(this);

				// Add tooltips
				addTooltips();
			}

			private void addTooltips()
			{
				this.setToolTipText("<html>Click to remove this tab (cannot be undone)<html>");

				// tooltips disappear only after 10s
				ToolTipManager.sharedInstance().setDismissDelay(10000);
			}

			public void actionPerformed(ActionEvent e) {
				int i = pane.indexOfTabComponent(ButtonTabComponent.this);
				if (i != -1) {
					pane.remove(i);
					removePrediction(i);
				}
			}

			//we don't want to update UI for this button
			public void updateUI() {
			}

			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
			}

		}

		private class CopyButton extends JButton implements ActionListener {
			public CopyButton() {
				int size = 22;
				setPreferredSize(new Dimension(size, size));
				setIcon(new ImageIcon(GnwGuiSettings.getInstance().getCopyIcon()));
				//Make the button looks the same for all Laf's
				setUI(new BasicButtonUI());
				//Make it transparent
				setContentAreaFilled(false);
				//No need to be focusable
				setFocusable(false);
				setBorderPainted(false);
				//Making nice rollover effect
				//we use the same listener for all buttons
				addMouseListener(buttonMouseListener);
				setRolloverEnabled(true);
				//Close the proper tab by clicking the button
				addActionListener(this);

				// Add tooltips
				addTooltips();
			}

			private void addTooltips()
			{
				this.setToolTipText("<html>Click to duplicate this tab<html>");

				// tooltips disappear only after 10s
				ToolTipManager.sharedInstance().setDismissDelay(10000);
			}

			public void actionPerformed(ActionEvent e) {
				int i = pane.indexOfTabComponent(ButtonTabComponent.this);
				if (i != -1) {

					addPrediction(new Evaluation((Evaluation)pane.getComponentAt(i)));

				}
			}

			//we don't want to update UI for this button
			public void updateUI() {
			}

			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
			}
		}

		private final MouseListener buttonMouseListener = new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				Component component = e.getComponent();
				if (component instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) component;
					button.setBorderPainted(true);
					button.setOpaque(true);
					button.setEnabled(true);
				}
			}

			public void mouseExited(MouseEvent e) {
				Component component = e.getComponent();
				if (component instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) component;
					button.setBorderPainted(false);
					button.setOpaque(false);
					button.setEnabled(false);
				}
			}
		};
	}
}
