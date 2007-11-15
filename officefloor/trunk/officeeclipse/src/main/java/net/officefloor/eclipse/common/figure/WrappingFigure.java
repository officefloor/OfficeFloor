/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * <p>
 * {@link org.eclipse.draw2d.Figure} that specifies another figure to add child
 * {@link org.eclipse.draw2d.Figure} instances.
 * <p>
 * This allows sub {@link org.eclipse.draw2d.Figure} instances of this type to
 * decorate themselves with {@link org.eclipse.draw2d.Figure} instances and
 * still be able to have children {@link org.eclipse.draw2d.Figure} instances
 * added (most likely for child models).
 * 
 * @author Daniel
 */
public class WrappingFigure extends Figure {

	/**
	 * {@link Figure} that is to contain the added child {@link Figure}
	 * instances.
	 */
	private final Figure childContainer;

	/**
	 * <p>
	 * Initiate.
	 * <p>
	 * Note that implementing {@link Figure} constructor must call
	 * {@link #addContainerFigure()} to add the <code>childContainer</code>
	 * {@link Figure}.
	 * 
	 * @param childContainer
	 *            {@link Figure} that is to contain the added child
	 *            {@link Figure} instances.
	 */
	public WrappingFigure(Figure childContainer) {
		this.childContainer = childContainer;

		// Defaultly use toolbar layout
		this.setLayoutManager(new ToolbarLayout());
	}

	/**
	 * Adds the {@link #childContainer} {@link Figure}.
	 */
	public final void addChildContainerFigure() {
		// Add to child container
		this.addDecorate(this.childContainer);
	}

	/**
	 * Decorates this {@link Figure} with the input {@link Figure}.
	 * 
	 * @param figure
	 *            {@link Figure} to decorate this {@link Figure}.
	 */
	public final void addDecorate(IFigure figure) {
		// super to avoid override
		super.add(figure, null, -1);
	}

	/**
	 * Obtains the child container {@link Figure}.
	 * 
	 * @return Child container {@link Figure}.
	 */
	protected final Figure getChildContainerFigure() {
		return this.childContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.IFigure#add(org.eclipse.draw2d.IFigure,
	 *      java.lang.Object, int)
	 */
	@Override
	public void add(IFigure figure, Object constraint, int index) {
		// Add to child container figure
		this.childContainer.add(figure, constraint, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Figure#remove(org.eclipse.draw2d.IFigure)
	 */
	@Override
	public void remove(IFigure figure) {
		// Remove from child container
		this.childContainer.remove(figure);
	}

}
