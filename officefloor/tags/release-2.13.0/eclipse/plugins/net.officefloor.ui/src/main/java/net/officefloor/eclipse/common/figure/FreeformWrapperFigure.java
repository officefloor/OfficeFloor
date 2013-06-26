/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.common.figure;

import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;

/**
 * {@link WrappingFigure} for a {@link FreeformViewport}.
 * 
 * @author Daniel Sagenschneider
 */
public class FreeformWrapperFigure extends Figure {

	/**
	 * Wrapped {@link Figure}.
	 */
	private final Figure wrappedFigure;

	/**
	 * Initiate.
	 * 
	 * @param figure
	 *            {@link Figure} to be wrapped to be put on a
	 *            {@link FreeformViewport}.
	 */
	public FreeformWrapperFigure(Figure figure) {
		this.wrappedFigure = figure;
		this.setLayoutManager(new NoSpacingToolbarLayout(true));
		this.setOpaque(false);
		
		// Add to this (not wrapped figure)
		super.add(this.wrappedFigure, null, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.IFigure#validate()
	 */
	@Override
	public void validate() {
		// Ensure invalid
		if (this.isValid()) {
			return;
		}

		// Layout the wrapped figure
		this.wrappedFigure.setValid(false);
		this.wrappedFigure.validate();

		// Make this the size of the wrapped figure
		this.setSize(this.wrappedFigure.getPreferredSize());

		// Do the super validation
		super.validate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.IFigure#add(org.eclipse.draw2d.IFigure,
	 *      java.lang.Object, int)
	 */
	@Override
	public void add(IFigure figure, Object constraint, int index) {
		// Add to child to wrapped figure
		this.wrappedFigure.add(figure, constraint, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Figure#remove(org.eclipse.draw2d.IFigure)
	 */
	@Override
	public void remove(IFigure figure) {
		// Remove from wrapped figure
		this.wrappedFigure.remove(figure);
	}

}
