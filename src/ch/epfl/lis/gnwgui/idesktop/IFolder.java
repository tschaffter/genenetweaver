package ch.epfl.lis.gnwgui.idesktop;

import java.util.ArrayList;

/**
 * Folder
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class IFolder extends IElement
{	
	/** Default serial */
	private static final long serialVersionUID = 1L;
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS	

	public IFolder(String label, IDesktop desk)
	{
		super(label,desk);
		initialize();
	}
	
	// ----------------------------------------------------------------------------
	
	public IFolder(IFolder item)
	{
		super("");
		initialize();
		
		this.draggable_ = item.draggable_;
		this.mvtItemStep_ = item.mvtItemStep_;
		this.desktop_ = item.desktop_;
		this.pane_ = item.pane_;
		this.iconWidth_ = item.iconWidth_;
		this.iconHeight_ = item.iconHeight_;
		this.icon_ = item.icon_;
		this.usedIconHighlighted_ = item.usedIconHighlighted_;
		this.setLabel(item.getLabel());
		this.label_ = "Copy of " + item.label_;
		this.itemLabelSelecColor_ = item.itemLabelSelecColor_;
		this.father_ = item.father_;
		//this.children_ = item.copyChildren();
		this.setToolTipText(item.getToolTipText());
	}
	
	// ----------------------------------------------------------------------------
	
	public void initialize()
	{
		destroyable_ = true;
		repaint();
	}
	
	// ----------------------------------------------------------------------------

	@Override
	public IElement copyElement()
	{
		return null;
	}
	
	// ----------------------------------------------------------------------------

	@Override
	protected void leftMouseButtonInvocationDouble() {}

	@Override
	protected void leftMouseButtonInvocationSimple() {}

	@Override
	protected void rightMouseButtonInvocation() {}

	@Override
	protected void wheelMouseButtonInvocation() {}
	
	// ----------------------------------------------------------------------------
	
	static public int getNumAllChildren(IElement element)
	{
		int count = 0;
		
		ArrayList<IElement> list = element.getChildren();		
		count += list.size();
		
		for (IElement e : list)
			count += getNumAllChildren(e);
			
		return count;
	}

//	@Override
//	public IElement copyElement()
//	{
//		IFolder copy = new IFolder(this);
//		IElement item = null;
//		
//		copy.setLabel("Copy of " + this.getLabel());
//		
//		if (hasChildren())
//		{
//			for(int i=0;i<children_.size();i++)
//			{
//				item = children_.get(i);
//				
//				if ( item.getClass() == DynamicalModelElement.class)
//					copy.addChild(((DynamicalModelElement)item).copyElement());
//				
//				if ( item.getClass() == StructureElement.class)
//					copy.addChild(((StructureElement)item).copyElement());
//				
//				if ( item.getClass() == IFolder.class)
//					copy.addChild(((IFolder)item).copyElement());
//			}
//		}
//		
//		return copy;
//	}
	
	// ----------------------------------------------------------------------------

//	@Override
//	protected void leftMouseButtonInvocationSimple() {
//		GnwGuiSettings.getInstance().getNetworkDesktop().displayChildrenOf(this);
//	}
//	
//	@Override
//	protected void leftMouseButtonInvocationDouble() {
//		if ( desktop_.getNumberOfSelectedElements() == 1 ) {
//			GnwGuiSettings global = GnwGuiSettings.getInstance();
//			Folder f = new Folder(global.getGnwGui().getFrame(),this);
//			f.setVisible(true);
//		}
//	}
//
//	@Override
//	protected void wheelMouseButtonInvocation() {}
//	
//	@Override
//	protected void rightMouseButtonInvocation() {
//		if ( desktop_.getNumberOfSelectedElements() == 1 ) {
//			GnwGuiSettings global = GnwGuiSettings.getInstance();
//			Folder f = new Folder(global.getGnwGui().getFrame(),this);
//			f.setVisible(true);
//		}
//	}
}
