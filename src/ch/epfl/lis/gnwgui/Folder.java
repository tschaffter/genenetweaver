package ch.epfl.lis.gnwgui;

import java.awt.Image;

import javax.swing.ImageIcon;

import ch.epfl.lis.gnwgui.GnwGuiSettings;
import ch.epfl.lis.gnwgui.idesktop.IDesktop;
import ch.epfl.lis.gnwgui.idesktop.IFolder;

public class Folder extends IFolder
{
	/** Default serialization */
	private static final long serialVersionUID = 1L;
	
	// ----------------------------------------------------------------------------
	// PUBLIC METHODS

	public Folder(String label, IDesktop desk)
	{
		super(label, desk);
		initialize();
	}
	
	// ----------------------------------------------------------------------------
	
	public void initialize()
	{		
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		setItemLabelSelecColor(global.getItemSelectedBgcolor());
		Image icon = new ImageIcon(global.getFolderIcon()).getImage();
		setItemIcon(icon);
	}
	
	// ----------------------------------------------------------------------------
	
	protected void leftMouseButtonInvocationSimple()
	{
		GnwGuiSettings.getInstance().getNetworkDesktop().displayChildrenOf(this);
	}
	
	// ----------------------------------------------------------------------------
	
	@Override
	protected void leftMouseButtonInvocationDouble()
	{
		if (desktop_.getNumberOfSelectedElements() == 1)
		{
			GnwGuiSettings global = GnwGuiSettings.getInstance();
			Options od = new Options(global.getGnwGui().getFrame(), this);
			od.setMenu(Options.FOLDER_MENU);
			this.mouseOverIcon_ = false;
			od.setVisible(true);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	@Override
	protected void rightMouseButtonInvocation()
	{
		if (desktop_.getNumberOfSelectedElements() == 1)
		{
			GnwGuiSettings global = GnwGuiSettings.getInstance();
			Options od = new Options(global.getGnwGui().getFrame(), this);
			od.setMenu(Options.FOLDER_MENU);
			this.mouseOverIcon_ = false;
			od.setVisible(true);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	@Override
	protected void wheelMouseButtonInvocation() {}
}
