package ch.epfl.lis.utilities.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Vector;
import java.util.Arrays;

/**
 * From jboehmer9
 * http://forums.sun.com/thread.jspa?threadID=234532
 * Link active on September 10, 2010
 * Adapted by Thomas Schaffter (firstname.name@gmail.com)
 * 
 * Usage:
 * Component[] compList = {jtfField1, jtfField2, jtfField3 jtfField4};
 * this.setFocusTraversalPolicy(new ICFocusPolicy(compList));
 */
public class ICFocusPolicy extends FocusTraversalPolicy
{
	private Vector<Component> components_;
	private int size_ = 0;
	
	// ============================================================================
	// PUBLIC METHODS
	
	public ICFocusPolicy(Component[] objs)
	{
		components_ = new Vector<Component>(Arrays.asList(objs));
		size_ = objs.length;
	}
	
	// ----------------------------------------------------------------------------
	
	public Component getComponentAfter(Container focusCycleRoot, Component aComponent)
	{
		int loc = components_.indexOf(aComponent);
		return (loc ==-1)?null:(Component)components_.elementAt((loc==(size_-1))?0:loc+1);
	}
	
	// ----------------------------------------------------------------------------
	
	public Component getLastComponent(Container focusCycleRoot)
	{
		return (size_==0)?null:(Component)components_.elementAt(size_-1);
	}
	
	// ----------------------------------------------------------------------------
	
	public Component getFirstComponent(Container focusCycleRoot)
	{
		return (size_==0)?null:(Component)components_.elementAt(0);
	}
	
	// ----------------------------------------------------------------------------
	
	public Component getDefaultComponent(Container focusCycleRoot)
	{
		return getFirstComponent(focusCycleRoot);
	}
	
	// ----------------------------------------------------------------------------
	
	public Component getComponentBefore(Container focusCycleRoot, Component aComponent)
	{
		int loc = components_.indexOf(aComponent);
		return (loc ==-1)?null:(Component)components_.elementAt((loc==0)?size_-1:loc-1);
	}
}