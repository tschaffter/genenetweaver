package ch.epfl.lis.gnwgui;

import java.awt.Graphics;

import javax.swing.ImageIcon;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/** This class add a signature to a JUNG2 viewer (container).
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class GraphSignature implements VisualizationViewer.Paintable {

	/** Container */
	@SuppressWarnings("rawtypes")
	private VisualizationViewer vv_ = null;
	/** Signature */
	private ImageIcon signature_ = null;
	/** Margin */
	private final int margin = 10;
	
	
    // =======================================================================================
    // PUBLIC FUNCTIONS
    //
	
	/**
	 * Constructor
	 * @param vv Viewer (container)
	 * @param signature Image
	 */
	public GraphSignature(@SuppressWarnings("rawtypes") VisualizationViewer vv, ImageIcon signature) {
		vv_ = vv;
		signature_ = signature;
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Paint method
	 */
	public void paint(Graphics g) {
		final int y0 = (int) vv_.getSize().getHeight() - signature_.getIconHeight() - margin;
		final int x0 = (int) vv_.getSize().getWidth() - signature_.getIconWidth() - margin;
        g.drawImage(signature_.getImage(),x0,y0,signature_.getIconWidth(),signature_.getIconHeight(),vv_);
	}
	
	
	// ----------------------------------------------------------------------------

	public boolean useTransform() {
		return false;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Add the signature to the container.
	 * @throws If the signature or container are null
	 */
	public void addSignatureToContainer() throws Exception {
		if (signature_ == null)
			throw new Exception("Signature is not defined (null)!");
		if (vv_ == null)
			throw new Exception("Container is not defined (null)!");
		
		vv_.addPostRenderPaintable(this);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Remove this signature from the container
	 * @throws If the signature or container are null
	 */
	public void removeSignatureFromContainer() throws Exception {
		if (signature_ == null)
			throw new Exception("Signature is not defined (null)!");
		if (vv_ == null)
			throw new Exception("Container is not defined (null)!");
		
		vv_.removePostRenderPaintable(this);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Change the ImageIcon of the signature. If signature if added to a container,
	 * the display on the container is automatically refreshed.
	 * @param newSignature New signature (container automatically refreshed)
	 */
	public void changeSignature(ImageIcon newSignature) {
		signature_ = newSignature;
	}
}

