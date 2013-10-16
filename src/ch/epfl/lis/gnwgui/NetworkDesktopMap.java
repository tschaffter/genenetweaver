package ch.epfl.lis.gnwgui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.idesktop.IDesktop;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class NetworkDesktopMap
{
	/** Desktop */
	IDesktop desktop_;
	
	/** XML Document */
	Document xmldoc_;

	/** Absolute path where are is the files to load */
	private String absPath_;
	
	/** Root filenames */
	private String rootFilename_;
	
	/** Counter for filenames */
	private int counter_;
	
	/** List of all filenames */
	private ArrayList<String> filenames_;
	
	/** Types of the elements */
	public static final String TYPE_STRUCTURE = "structure";
	public static final String TYPE_DYNAMICAL_NETWORK = "dynamical_network";
	public static final String TYPE_FOLDER = "folder";

	// ----------------------------------------------------------------------------
	// PUBLIC METHODS
	
	public NetworkDesktopMap(IDesktop desktop)
	{
		desktop_ = desktop;
		xmldoc_ = null;
		absPath_ = "";
		rootFilename_ = "element_";
		counter_ = 0;
		filenames_ = null;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Create a XML file describing the content of an IDesktop */
	public void encode(String xmlFileAbsPath) throws FileNotFoundException, IOException, Exception
	{
		counter_ = 0;
		
		// Document (Xerces implementation only).
		xmldoc_ = new DocumentImpl();
		Element root = xmldoc_.createElement("gnw_desktop");
		
		ArrayList<IElement> list = desktop_.getContent().get(0);
		
		// save recursively all the elements on the desktop
		for (IElement element : list)
			saveElementRecursively(root, element);
		  
		xmldoc_.appendChild(root);
		
		FileOutputStream fos = new FileOutputStream(xmlFileAbsPath);
		OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
		of.setIndent(1);
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(fos, of);
		serializer.asDOMSerializer();
		serializer.serialize(xmldoc_.getDocumentElement());
		fos.close();
		
		// build a list of filenames
		filenames_ = new ArrayList<String>(counter_);
		
		for (int i = 0; i < counter_; i++)
			filenames_.add(rootFilename_ + i);
		
		counter_ = 0;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Read a XML file to extract the description of the content of an IDesktop */
	public void decode(String xmlFileAbsPath) throws ParserConfigurationException, IOException, Exception
	{
		absPath_ = FilenameUtilities.getDirectory(xmlFileAbsPath);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		xmldoc_ = db.parse(xmlFileAbsPath);
		xmldoc_.getDocumentElement().normalize();
		
		Element root = xmldoc_.getDocumentElement();
		ArrayList<Element> list = getChildrenByTagName(root, "element");
		
		for (Element i : list)
			loadElementRecursively(null, i);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Used by encode() */
	public void saveElementRecursively(Element parent, IElement ie) throws Exception
	{
		// no need to save elements that are always present on the desktop
		if (!ie.isDestroyable())
			return;
		
		Element e = IElement2Element(ie);
		Node n = null;
		
		if (ie instanceof StructureElement || ie instanceof DynamicalModelElement)
			n = xmldoc_.createTextNode(rootFilename_ + counter_++);
		else if (ie instanceof Folder)
			n = xmldoc_.createTextNode("");
		
		e.appendChild(n);
		parent.appendChild(e);
		
		if (ie.hasChildren())
		{
			ArrayList<IElement> list = ie.getChildren();
			
			for (IElement ie2 : list)
				saveElementRecursively(e, ie2);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Used by decode() */
	public void loadElementRecursively(IElement parent, Element e) throws Exception
	{
		IElement decodedElement = null;
		String name = e.getAttribute("name");
		
		if (e.getAttribute("type").compareTo(TYPE_STRUCTURE) == 0)
		{
			URL url = GnwSettings.getInstance().getURL(absPath_ + "/" + e.getFirstChild().getNodeValue());
			StructureElement element = IONetwork.loadStructureItem(name, url, Structure.TSV);
			decodedElement = element;
			IONetwork.printOpeningInfo(element);
			
			if (parent == null)
				desktop_.addItemOnDesktop(element);
			else
				desktop_.addItemOnDesktop(element, parent);
		}
		else if (e.getAttribute("type").compareTo(TYPE_DYNAMICAL_NETWORK) == 0)
		{
			URL url = GnwSettings.getInstance().getURL(absPath_ + "/" + e.getFirstChild().getNodeValue());
			DynamicalModelElement element = IONetwork.loadDynamicNetworkItem(name, url, GeneNetwork.SBML);
			decodedElement = element;
			IONetwork.printOpeningInfo(element);
			
			if (parent == null)
				desktop_.addItemOnDesktop(element);
			else
				desktop_.addItemOnDesktop(element, parent);
		}
		else if (e.getAttribute("type").compareTo(TYPE_FOLDER) == 0)
		{
			Folder folder = new Folder(name, desktop_);
			decodedElement = folder;
			
			if (parent == null)
				desktop_.addItemOnDesktop(folder);
			else
				parent.addChild(folder);
		}
		
		ArrayList<Element> list = getChildrenByTagName(e, "element");
		
		for (Element i : list)
			loadElementRecursively(decodedElement, i);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Create a XML Element from an IElement. WARNING: the element is not added to the XML file. */
	public Element IElement2Element(IElement ie) throws Exception
	{
		Element e = xmldoc_.createElementNS(null, "element");
		e.setAttributeNS(null, "name", ie.getLabel());
		
		if (ie instanceof StructureElement)
			e.setAttributeNS(null, "type", TYPE_STRUCTURE);
		else if (ie instanceof DynamicalModelElement)
			e.setAttributeNS(null, "type", TYPE_DYNAMICAL_NETWORK);
		else if (ie instanceof Folder)
			e.setAttributeNS(null, "type", TYPE_FOLDER);
		else
			throw new Exception("Unknown element type of " + ie.getLabel());
		
		return e;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Helper function to get all the level-1 children of an Element */
	 public static ArrayList<Element> getChildrenByTagName(Element parent, String name)
	 {
		 ArrayList<Element> nodeList = new ArrayList<Element>();
		 
		 for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
		 {
			 if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName()))
				 nodeList.add((Element) child);
		 }

		 return nodeList;
	 }
	
	// ----------------------------------------------------------------------------
	// GETTERS AND SETTERS
	
	public String getRootFilename() { return rootFilename_; }
	
	public String getNextFilename() { return filenames_.get(counter_++); }
}
