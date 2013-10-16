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

/**
 * Adaptation of the following code(s)
 * - Java desktop drag and drop
 *   by Gregg Wonderly
 *   June 1, 2006
 */

package ch.epfl.lis.gnwgui.idesktop;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import ch.epfl.lis.gnwgui.Delete;
import ch.epfl.lis.gnwgui.GnwGuiSettings;

/** 
 * Interactive desktop iDesktop
 * 
 * Implement a Mac-inspired files-explorer. Elements on the desktop are displayed
 * column by column. When clicking on an element, its children are displayed in the next
 * column. The spaces between the columns are adaptative, i.e. if the label of an
 * element changes (new label, bold font), if it is deleted or if a new element is added,
 * the locations of the next columns will be automatically adapted.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
abstract public class IDesktop
{
	/** Instance of JDesktopPanel. */
	protected JDesktopPane desktopPanel_ = null;
	/** Height of one item. [px] */
	protected int elementHeight_ = 36;

	/** Initial gap (top) [px] */
	protected int vGap0_ = 0; // was 30
	/** Initial gap (left) [px] */
	protected int hGap0_ = 10;
	/** Horizontal gap between 2 items. [px] */
	protected int hGap_ = 15;
	/** Vertical gap between 2 items. [px] */
	protected int vGap_ = 20;

	/** Mouse state variables */
	protected boolean dragged_ = false;
	
	/** Location where the user clicks the mouse */
	protected Point pointClicked_ = new Point(0,0);
	/** Location where the user releases the mouse */
	protected Point pointReleased_ = new Point(0,0);
	
	/** If an item is close enough of a new location, it is attracted and place there. */
	protected double elementGravity_ = 0.5;
	
	/** Content of the desktop */
	protected ArrayList< ArrayList<IElement>> content_ = new ArrayList< ArrayList<IElement> >();
	
	/** Last IElement added */
	protected IElement lastElementAdded_;
	
	/** Different width is used for each column. */
	protected ArrayList<Integer> columnWidths_ = new ArrayList<Integer>();

	/** Location and extents of selection rectangle.   */
	private Rectangle rectangleSelection_;
	/** Stroke-defined outline of selection rectangle. */
	private BasicStroke rectangleSelectionBasicStroke_;
	/**  A gradient paint is used to create a distinctive-looking selection rectangle outline. */
	private GradientPaint rectangleSelectionGradientPaint_;   
	/** Selection rectangle location for drawing */
	private int srcx_ = 0, srcy_ = 0, destx_ = 0, desty_ = 0;
	/** Border color of the rectangle selection */
	private Color rectangleBorderSelectionColor_ = new Color(200,200,200);//new Color(4, 86, 142);
	/** Plain color of the rectangle selection */
	private Color rectanglePlainSelectionColor_ = new Color(0,0,0);//new Color(0, 108, 170); // 160,206,242
	/** Alpha of the rectangle selection */
	private float rectanglePlainSelectionAlpha_ = 0.05f;
	
	/** Element which children are being displayed */
	private IElement displayChildrenOf_ = null;
	
	/** Total number of elements displayed */
	private int numElementsDisplayed_ = 0;
	
	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(IDesktop.class.getName());
	

	// ============================================================================
	// PRIVATE

	/** Activates all the mouse listening activities */
	private void mouseEntry()
	{
		desktopPanel_.addMouseListener( new MyMouseAdapter());
		desktopPanel_.addMouseMotionListener(new MyMouseMotionListener());
	}
	

	// ============================================================================
	// ABSTRACT

	/** Defines what to do when an item is dragged and then released on the desktop. */
	abstract public void itemReleased(IElement element);
	/** Multiple elements selection popup window */
	abstract public void multipleElementsSelectionMenu();
	

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor */
	public IDesktop(String name)
	{
		rectangleSelection_ = new Rectangle();
		
		// Define the gradient paint for coloring selection rectangle outline.
		rectangleSelectionBasicStroke_ = new BasicStroke (1.0f,						// Width of stroke
														  BasicStroke.CAP_ROUND,	// End cap style
														  BasicStroke.JOIN_MITER);	// Join style
		rectangleSelectionGradientPaint_ = new GradientPaint (0.0f,
															  0.0f,
															  rectangleBorderSelectionColor_,
															  0.0f,
															  0.0f,
															  Color.white,
															  true);

		desktopPanel_ = new JDesktopPane()
		{
			/** Default serial */
			private static final long serialVersionUID = 1L;
			
			// ============================================================================
			// PUBLIC METHODS

			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				if (srcx_ != destx_ || srcy_ != desty_)
				{
					// Compute upper-left and lower-right coordinates for selection
					// rectangle corners.

					int x1 = (srcx_ < destx_) ? srcx_ : destx_;
					int y1 = (srcy_ < desty_) ? srcy_ : desty_;

					int x2 = (srcx_ > destx_) ? srcx_ : destx_;
					int y2 = (srcy_ > desty_) ? srcy_ : desty_;

					// Establish selection rectangle origin.

					rectangleSelection_.x = x1;
					rectangleSelection_.y = y1;

					// Establish selection rectangle extents.

					rectangleSelection_.width = (x2-x1)+1;
					rectangleSelection_.height = (y2-y1)+1;
					
					GradientPaint redtowhite = new GradientPaint(x1, y1, rectanglePlainSelectionColor_, rectangleSelection_.x + rectangleSelection_.width, 
							rectangleSelection_.y + rectangleSelection_.height , rectanglePlainSelectionColor_);

					// Draw selection rectangle.

					Graphics2D g2d = (Graphics2D) g;
					g2d.setStroke (rectangleSelectionBasicStroke_);
					g2d.setPaint (rectangleSelectionGradientPaint_);
					g2d.draw (rectangleSelection_);
					g2d.setPaint(redtowhite);
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rectanglePlainSelectionAlpha_));
					g2d.fill(new Rectangle2D.Double(x1, y1, rectangleSelection_.width, rectangleSelection_.height));
					
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				}
			}
		};

		initialize();
		desktopPanel_.setAutoscrolls(true);
		desktopPanel_.setName(name);
		desktopPanel_.setBackground(Color.WHITE);
		
		// Item must be opaque after being placed
		desktopPanel_.setOpaque(true);
	}

	// ----------------------------------------------------------------------------

	/** Initialize the iDesktop. */
	public void initialize()
	{
		content_.add(new ArrayList<IElement>());
		columnWidths_.add(0);
		mouseEntry(); // Active the mouse listener
		deleteSelectedElement(desktopPanel_);
	}

	// ----------------------------------------------------------------------------

	/** Add the given element in the first column of this desktop. */
	public void addItemOnDesktop(IElement element)
	{	
		element.setDesktop(this); // associate desktop to item
		content_.get(0).add(element);
		recalculateColumnWidths(element);
		lastElementAdded_ = element;
		refreshDesktop();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Add the given element in the first column of this desktop. */
	public void addItemOnDesktop(IElement element, IFolder folder)
	{	
		element.setDesktop(this); // associate desktop to item
		folder.addChild(element);
		recalculateColumnWidths(element);
		lastElementAdded_ = element;
		refreshDesktop();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Add the given element in the first column of this desktop. */
	public void addItemOnDesktop(IElement element, IElement elementParent_)
	{	
		element.setDesktop(this); // associate desktop to item
		elementParent_.addChild(element);
		recalculateColumnWidths(element);
		lastElementAdded_ = element;
		refreshDesktop();
	}

	// ----------------------------------------------------------------------------

	/**
	 * Add the given element on the desktop at the position specified. If an
	 * element already exists at this position, it is simply replaced.
	 */
	public void addItemOnDesktop(IElement element, Point position)
	{
		int c = (int) position.getX();
		int index = (int) position.getY();
		content_.get(c).add(index, element);
		lastElementAdded_ = element;
		refreshDesktop();
	}	
	
	// ----------------------------------------------------------------------------

	/**
	 * Recalculates the column width of the one that contains the given element.
	 * The desktop should be then repaint using repaintDesktop().
	 */
	public void recalculateColumnWidths(IElement element)
	{
		Point pos = getElementPosition(element);
		
		if (pos == null)
			return;
		
		int c = (int) pos.getX();
		recalculateColumnWidths(c);
	}

	// ----------------------------------------------------------------------------

	/** Recalculates the column width specified. */
	public void recalculateColumnWidths(int c)
	{
		columnWidths_.set(c, 0);
		int tempSize = 0;
		ArrayList<IElement> subContent = content_.get(c);
		
		for (int i = 0; i < subContent.size(); i++)
		{
			tempSize = (int) subContent.get(i).getPreferredWidth();
			
			if (tempSize > columnWidths_.get(c))
				columnWidths_.set(c, tempSize);
		}
	}

	// ----------------------------------------------------------------------------

	/** Remove the given element on the desktop. If its children are displayed, they are also remove. */
	public void removeItemFromDesktop(IElement element)
	{
		Point pos = getElementPosition(element);

		if (pos == null)
			return;

		int c = (int) pos.getX();
		
		// Remove item
		content_.get(c).remove(element);
		
		// If this item displays its children, remove them from the desktop
		if ((c + 1) < content_.size() && content_.get(c + 1) != null && !content_.get(c + 1).equals(""))
			if (content_.get(c + 1).size() > 0 && content_.get(c + 1).get(0).getFather().equals(element))
				for (int j = (c + 1); j < content_.size(); j++)
					content_.get(j).clear();
		
		// Remove this item as child from its eventual father
		if (element.getFather() != null)
			element.getFather().getChildren().remove(element);
		
		// Call garbage collector to immediately free memory
		System.gc();

		refreshDesktop();
	}

	// ----------------------------------------------------------------------------

	/** Replace the element on the desktop by element2. */
	public void replaceItem(IElement element, IElement element2)
	{
		Point pos = getElementPosition(element);
		int c = (int) pos.getX();
		int index = (int) pos.getY();
		content_.get(c).set(index, element2);
		refreshDesktop();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Return an array with all selected element and their recursive children items */
	public ArrayList<IElement> getTopSelectedItems()
	{
		ArrayList<IElement> selected = getSelectedElements();
		
		//Get all elements we want
		ArrayList<IElement> topElements = new ArrayList<IElement>();
		boolean add = false;
		
		for(IElement itemIE : selected)
		{						
			if( topElements.size() == 0 )
				topElements.add(itemIE);
			else
			{
				for(int i = 0; i < topElements.size(); i++)
				{
					add = false;
					
					if(!getAllChildren(topElements.get(i)).contains(itemIE) && !topElements.contains(itemIE))
						add = true;		
					else
						add = false;	
				}
				
				if(add)
					topElements.add(itemIE);
			}
		}
		
		// Detect the left-most column
		int min = 100;
		for (IElement e : topElements)
		{
			Point x = getElementPosition(e);
			if (x.x < min)
				min = x.x;
		}
		// Only keep the elements in the left-most column
		ArrayList<IElement> result = new ArrayList<IElement>();
		for (IElement e : topElements)
		{
			if (getElementPosition(e).x == min)
				result.add(e);
		}
		
		return result;
	}

	// ----------------------------------------------------------------------------

	/** Repaint the elements on the desktop accordingly to content_. */
	public void repaintDesktop()
	{
		int newNumElements = 0;
		ArrayList<IElement> subContent = null;
		
		// Remove all the elements present on the desktop
		desktopPanel_.removeAll();
		
		for (int i = 0; i < content_.size(); i++)
		{
			subContent = content_.get(i);
			newNumElements += content_.get(i).size();
			
			for (int j = 0; j < subContent.size(); j++)
			{
				subContent.get(j).setLocation( getElementLocationFromPosition(i, j) );
				desktopPanel_.add(subContent.get(j));
			}
		}

		if ( newNumElements != numElementsDisplayed_)
		{
			numElementsDisplayed_ = newNumElements;
			resizeDesktop();
		}
		
		desktopPanel_.repaint();
	}

	// ----------------------------------------------------------------------------

	/** Recalculate desktop size for scroll bars */
	public void resizeDesktop()
	{
		int maxSubContent = 0;
		ArrayList<IElement> subContent = null;
		
		for (int i = 0; i < content_.size(); i++)
		{
			subContent = content_.get(i);
			
			if (subContent.size() > maxSubContent)
				maxSubContent = subContent.size();
		}

		Dimension d = new Dimension(getElementLocationFromPosition(content_.size(), maxSubContent+1).x,getElementLocationFromPosition(content_.size(), maxSubContent).y);
		Dimension size = d.getSize();
		Dimension m = desktopPanel_.getMinimumSize();

		if (m.width > d.width)
		{
			d.setSize(m.width - Integer.parseInt(UIManager.get("ScrollBar.width").toString()), d.height);
			size.setSize(m.width, size.height);
		}
		if (m.height > d.height)
		{
			d.setSize(d.width, m.height - Integer.parseInt(UIManager.get("ScrollBar.width").toString()));
			size.setSize(size.width, m.height);
		}
		desktopPanel_.setSize(size);
		desktopPanel_.setPreferredSize(d);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Refreshing the desktop consists to recalculate the widths of the columns and repainting the desktop. */
	public void refreshDesktop()
	{
		for (int i = 0; i < content_.size(); i++)
			recalculateColumnWidths(i);

		repaintDesktop();
	}

	// ----------------------------------------------------------------------------

	/**
	 * Return the position of the given element as (column,index).
	 * If the element is not present on the desktop, the returned value is null.
	 */
	public Point getElementPosition(IElement element)
	{
		ArrayList<IElement> subContent = null;
		
		for (int i = 0; i < content_.size(); i++)
		{
			subContent = content_.get(i);
			for (int j = 0; j < subContent.size(); j++)
				if (subContent.get(j).equals(element))
					return new Point(i, j);
		}
		return null;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Return the location of an element on the desktop from its position determined
	 * by the index of its column (c) and its index in this column (index).
	 */
	public Point getElementLocationFromPosition(int c, int index)
	{
		int x = hGap0_;
		int y = vGap0_;

		for (int i = 0; i < c; i++)
			x += columnWidths_.get(i) + hGap_;
		
		y += index * elementHeight_;

		return new Point(x, y);
	}

	// ----------------------------------------------------------------------------

	/** Display the eventual children of the element given in parameter. */
	public void displayChildrenOf(IElement element)
	{
		displayChildrenOf_ = element;
		int numChildren = 0 ;
		
		if (element != null)
		{
			if (element.hasChildren())
				numChildren = element.getChildren().size();
		
			Point pt = getElementPosition(element);
			
			if (pt == null)
				return;
			
			int c1 = (int) pt.getX() + 1;
			
			// If the column that will be used to display the children does not
			// already exist -> creation
			if (content_.size() - 1 < c1)
			{
				columnWidths_.add(0);
				content_.add(new ArrayList<IElement>());
			}
			
			// Remove the content of all the columns next to the one of the given item.
			for (int i = c1; i < content_.size(); i++)
				content_.get(i).clear();
			
			// Fill the next column with the eventual children of the given item.
			for (int i = 0; i < numChildren; i++)
				content_.get(c1).add(element.getChildren().get(i));
		}
		
		refreshDesktop();
	}
	
	// ----------------------------------------------------------------------------

	/** Return if yes or no the item1 is on the item2. */
	public boolean isItemOnAnother(IElement item1, IElement item2)
	{
		double itemCoordX = item1.getLocation().getX();
		double itemCoordY = item1.getLocation().getY();
		double binCoordXLeft = item2.getLocation().getX() - elementGravity_*item2.getSize().width;
		double binCoordXRight = item2.getLocation().getX() + elementGravity_*item2.getSize().width;
		double binCoordYTop = item2.getLocation().getY() + elementGravity_*item2.getSize().height;
		double binCoordYDown = item2.getLocation().getY() - elementGravity_*item2.getSize().height;

		if (itemCoordX >= binCoordXLeft && itemCoordX <= binCoordXRight)
		{
			if (itemCoordY <= binCoordYTop && itemCoordY >= binCoordYDown)
				return true;
		}
		
		return false;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Return true if the given IElement is currently on top of another present
	 * on the desktop.
	 */
	public boolean isItemOnAnother(IElement item)
	{
		for(int i = 0; i < content_.size(); i++)
		{
			for(int j = 0; j < content_.get(i).size(); j++)
			{
				if ( !content_.get(i).get(j).equals(item) && item.getBounds().intersects(content_.get(i).get(j).getBounds()) ) 
					return true;
			}
		}
		return false;
	}
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Return the index of the column where is currently the given IElement.
	 * Otherwise return -1.
	 */
	public int isItemOnColumn(IElement item)
	{
		int prev = 0;
		for(int i = 0; i < columnWidths_.size(); i++)
		{
			if (prev < item.getX() && item.getX() < prev + columnWidths_.get(i))
				return i;
			
			prev += columnWidths_.get(i);
		}
		return -1;
	}
	// ----------------------------------------------------------------------------

	/** Get a element from its label */
	public IElement getIElementFromLabel(String label)
	{
		IElement element = null;

		for(int i = 0; i < getAllNetworks().size(); i++)
		{
			element = getAllNetworks().get(i);

			if(element.getLabel().equals(label))
				return element;
		}
		
		return null;
	}

	// ----------------------------------------------------------------------------

	/** Delete selected element */
	@SuppressWarnings("serial")
	public void deleteSelectedElement(JComponent jp)
	{
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DELETE_ELEMENT");
		k = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DELETE_ELEMENT");
		
		jp.getActionMap().put("DELETE_ELEMENT", new AbstractAction()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				deleteSelectedElement();
			}
		});
	}
	
	// ----------------------------------------------------------------------------

	/** Delete selected element */
	public void deleteSelectedElement()
	{
		IElement element = IElement.curItem;
		
		if (getNumberOfSelectedElements() == 1 && element.getChildren().size() == 0)
		{
			if (element != null)
			{
				if (element.isDestroyable())
				{
					removeItemFromDesktop(element);
					log_.log(Level.INFO, "Removing " + element.getLabel() + " (and its content if any)");
				}
			}
		}
		else
		{
			GnwGuiSettings global = GnwGuiSettings.getInstance();
			Delete dd = new Delete(global.getGnwGui().getFrame());
			dd.setVisible(true);
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Clear the entire selection. */
	public void clearSelection()
	{
		ArrayList<IElement> subContent = null;
		
		for (int i = 0; i < content_.size(); i++)
		{
			subContent = content_.get(i);
			for (int j = 0; j < subContent.size(); j++)
			{
				subContent.get(j).exit();
			}
		}
	}

	// ----------------------------------------------------------------------------
	
	/** Returns the number of element(s) selected */
	public int getNumberOfSelectedElements()
	{
		int n = 0;
		ArrayList<IElement> subContent = null;
		
		for (int i = 0; i < content_.size(); i++)
		{
			subContent = content_.get(i);
			for (int j = 0; j < subContent.size(); j++)
			{
				if (subContent.get(j).selected_ && subContent.get(j).destroyable_)
					n++;
			}
		}
		
		return n;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Delete all destroyable elements from the desktop */
	public void emptyDesktop()
	{
		for (int i = content_.get(0).size() - 1; i > 0 ; i--)
		{
			IElement element = content_.get(0).get(i);
			
			if (element.isDestroyable())
			{
				removeItemFromDesktop(element);
				refreshDesktop();	
			}
		}
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public IElement getLastElementAdded() { return lastElementAdded_; }
	public IElement getDisplayChildrenOf() { return displayChildrenOf_; }

	public JDesktopPane getDesktopPane() { return desktopPanel_; }
	public ArrayList< ArrayList<IElement>> getContent() {return content_; }
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Return the IFolder associated to the first folder found in column
	 * level which has the given name.
	 */
	public IFolder getFolder(String name, int level)
	{
		IElement element = null;
	
		ArrayList<IElement> list = content_.get(level);
		
		for (int i = 0; i < list.size(); i++)
		{
			element = list.get(i);
			
			if (element instanceof IFolder && element.getName() == name)
				return (IFolder) element;
		}
		
		return null;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Return a list containing all the item on the desktop. Only items that can
	 * be destroyed are returned.
	 */
	public ArrayList<IElement> getAllNetworks()
	{
		ArrayList<IElement> list = new ArrayList<IElement>();

		for (int i  =0; i < content_.get(0).size(); i++)
		{
			if ( content_.get(0).get(i).destroyable_ )
			{
				list.add(content_.get(0).get(i));
				
				if (getAllChildren(content_.get(0).get(i)) != null )
				{
					for(int j = 0; j < getAllChildren(content_.get(0).get(i)).size(); j++)
						list.add(getAllChildren(content_.get(0).get(i)).get(j));
				}
			}
		}
		return list;
	}
	
	// ----------------------------------------------------------------------------

	/** Return a list containing all the children elements of the given IElement. */
	@SuppressWarnings("unchecked")
	public ArrayList<IElement> getAllChildren(IElement e)
	{
		ArrayList<IElement> list = new ArrayList<IElement>();
		ArrayList<IElement> children = e.getChildren();
		
		for (int i = 0; i < children.size(); i++)
		{
			list.add(children.get(i));
			
			if (getAllChildren(children.get(i)) != null ) {
				for(int j = 0;j<getAllChildren(children.get(i)).size();j++)
					list.add(getAllChildren(children.get(i)).get(j));
			}
		}
		
		return (ArrayList<IElement>) list.clone();
	}
	
	// ----------------------------------------------------------------------------

	/** Return a list containing all selected elements. */
	public ArrayList<IElement> getSelectedElements()
	{
		ArrayList<IElement> selected = new ArrayList<IElement>();
		ArrayList<IElement> subContent = null;
		
		for (int i = 0; i < content_.size(); i++) 
		{
			subContent = content_.get(i);
			
			for (int j=0; j < subContent.size(); j++)
			{
				if (subContent.get(j).selected_ )
					if ( subContent.get(j).destroyable_)
						selected.add(subContent.get(j));
			}
		}
		
		return selected;
	}
	
	
	// ============================================================================
	// PRIVATE CLASSES

	/**
	 * The MouseAdapter that handles click events to establish the MouseMotionListener.
	 * 
	 * @author Thomas Schaffter (firstname.name@gmail.com)
	 */
	private class MyMouseAdapter extends MouseAdapter
	{	
		// ============================================================================
		// PUBLIC
		
		/** Creates an instance. */
		public MyMouseAdapter() {}

		
		// ============================================================================

		/** Called when the user clicks on the IElement. A click means "pressed-and-released" */
		public void mouseClicked(MouseEvent ev)
		{
			if (ev.getButton() == java.awt.event.MouseEvent.BUTTON1 && !(ev.isShiftDown() && ev.isControlDown()))
			{
				IElement.clearSelection(); 
				ArrayList<IElement> subContent = null;
				
				for (int i = 0; i < content_.size(); i++)
				{
					subContent = content_.get(i);
					
					for (int j=0; j < subContent.size(); j++)
						subContent.get(j).exit();
				}
			} 
			else if (ev.getButton() == java.awt.event.MouseEvent.BUTTON2)
			{
				//
			} 
			else if (ev.getButton() == java.awt.event.MouseEvent.BUTTON3)
			{
				//
			}
		}

		// ----------------------------------------------------------------------------

		/** Called when the mouse is pressed down. */
		public void mousePressed(MouseEvent ev) 
		{
			if (!ev.isShiftDown() || !ev.isControlDown())
				IElement.clearSelection();

			pointClicked_ = ev.getPoint();
			srcx_ = pointClicked_.x;
			srcy_ = pointClicked_.y;
			destx_= pointClicked_.x;
			desty_= pointClicked_.y;
		}

		// ----------------------------------------------------------------------------

		/** Called when the mouse button is released. */
		public void mouseReleased(MouseEvent ev)
		{
			srcx_ = 0;
			srcy_ = 0;
			destx_ = 0;
			desty_ = 0;

			// To avoid items being selected in several columns, all items are unselected
			// but the ones in the top column.
			ArrayList<IElement> top = getTopSelectedItems();
			
			if (top.size() == 0)
				return;
			
			clearSelection();
			IElement.curItem = top.get(0);
			for (IElement e : top)
				e.selected_ = true;
			
			repaintDesktop();
		}
	}
	
	// ----------------------------------------------------------------------------

	private class MyMouseMotionListener extends MouseMotionAdapter
	{
		// ============================================================================
		// PUBLIC

		/** Called when the mouse is moved without a button down. */
		public void mouseMoved(MouseEvent ev)
		{

		}

		// ----------------------------------------------------------------------------

		/** Called when the mouse is moved with button one down. */
		public void mouseDragged(MouseEvent ev)
		{
			dragged_ = true;
			int signX = 1,signY = 1;
			pointReleased_ = ev.getPoint();
			destx_ = pointReleased_.x;
			desty_ = pointReleased_.y;

			Dimension d;
			Point selectionP;
			int selectionX = pointClicked_.x,selectionY=pointClicked_.y;

			if (pointClicked_.x > pointReleased_.x)
			{
				signX = -1;
				selectionX = pointReleased_.x;
			}
			if (pointClicked_.y > pointReleased_.y)
			{
				signY = -1;
				selectionY = pointReleased_.y;
			}
			
			d = new Dimension(signX * (pointReleased_.x - pointClicked_.x), signY * (pointReleased_.y - pointClicked_.y));
			selectionP = new Point(selectionX,selectionY);
			Rectangle selection = new Rectangle(selectionP, d);

			ArrayList<IElement> subContent = null;
			
			for (int i = 0; i < content_.size(); i++)
			{
				subContent = content_.get(i);
				for (int j=0; j < subContent.size(); j++)
				{
					if ( selection.intersects(subContent.get(j).getBounds()))
						subContent.get(j).enter();
					else
					{
						if (( (ev.isShiftDown() || ev.isControlDown()) && !subContent.get(j).selected_) || !(ev.isShiftDown() || ev.isControlDown()))
							subContent.get(j).exit();
					}
				}
			}
			
			repaintDesktop();
		}
	}
}