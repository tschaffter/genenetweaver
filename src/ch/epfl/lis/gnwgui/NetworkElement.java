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

import java.util.logging.Logger;

import ch.epfl.lis.gnwgui.idesktop.IDesktop;
import ch.epfl.lis.gnwgui.idesktop.IElement;

/** Extends the interactive element iElement to represent network structures or
 * dynamical models.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class NetworkElement extends IElement {

	/** Serialization */
	private static final long serialVersionUID = 1L;

	/** Reference to a graph representation. */
	protected NetworkGraph networkViewer_;

	/** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(NetworkElement.class.getName());

	// ----------------------------------------------------------------------------
	// PUBLIC METHODS

	/**
	 * Initialization
	 */
	private void initialize() {
		networkViewer_ = null;
	}


	// ----------------------------------------------------------------------------

	/**
	 * Constructor
	 * @param label Label of the element.
	 */
	public NetworkElement(String label) {
		super(label);
		initialize();
	}


	// ----------------------------------------------------------------------------

	/**
	 * Constructor
	 * @param label Label of the element.
	 * @param desk IDesktop on which the item is added.
	 */
	public NetworkElement(String label, IDesktop desk) {
		super(label, desk);
		initialize();
	}


	// ----------------------------------------------------------------------------

	/**
	 * Constructor
	 * @param item An NetworkItem to copy.
	 */
	public NetworkElement(NetworkElement item) {
		super(item);
		initialize();
		if (this.networkViewer_ != null)
			this.networkViewer_ = item.networkViewer_.copy();
	}


	// ----------------------------------------------------------------------------
	/*
	/**
	 * Copy function.
	 * @return A copy of this element
	 */
	public NetworkElement copy() {
		return new NetworkElement(this);
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to execute when the left mouse button is clicked once.
	 * Display the network children in the next column of the iDestop.
	 */
	protected void leftMouseButtonInvocationSimple() {
		GnwGuiSettings.getInstance().getNetworkDesktop().displayChildrenOf(this);
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to execute when the left mouse button is clicked twice.
	 * Open the options dialog.
	 */
	protected void leftMouseButtonInvocationDouble() {
		rightMouseButtonInvocation();
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to execute when the wheel of the mouse is clicked.
	 * Open the network viewer
	 */
	protected void wheelMouseButtonInvocation() {
		Options.viewNetwork(this);
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to execute when the right mouse button is clicked once.
	 * Open the option dialog
	 */
	protected void rightMouseButtonInvocation() {
		if ( desktop_.getNumberOfSelectedElements() == 1 ) {
			GnwGuiSettings global = GnwGuiSettings.getInstance();
			Options od = new Options(global.getGnwGui().getFrame(), this);
			this.mouseOverIcon_ = false;
			od.setVisible(true);
		}
	}


	// ============================================================================
	// GETTERS AND SETTERS

	public NetworkGraph getNetworkViewer() { return networkViewer_; }
	public void setNetworkViewer(NetworkGraph viewer) { this.networkViewer_ = viewer; }


	@Override
	public IElement copyElement() {
		// TODO Auto-generated method stub
		return null;
	}
}
