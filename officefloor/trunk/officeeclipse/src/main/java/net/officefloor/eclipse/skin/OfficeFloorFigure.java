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
package net.officefloor.eclipse.skin;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * Provides details of the {@link IFigure} for an {@link EditPart}.
 * 
 * @author Daniel
 */
public class OfficeFloorFigure {

	/**
	 * {@link IFigure} to represent the {@link EditPart}.
	 */
	private final IFigure figure;

	/**
	 * {@link IFigure} to add children of this {@link EditPart}.
	 */
	private final IFigure contentPane;

	/**
	 * Initiate.
	 * 
	 * @param figure
	 *            {@link IFigure} to represent the {@link EditPart}.
	 * @param contentPane
	 *            {@link IFigure} to add children of this {@link EditPart}.
	 */
	public OfficeFloorFigure(IFigure figure, IFigure contentPane) {
		this.figure = figure;
		this.contentPane = contentPane;
	}

	/**
	 * Initiate to add children to top level {@link IFigure}.
	 * 
	 * @param figure
	 *            {@link IFigure} to represent the {@link EditPart} and also
	 *            potentially have children added.
	 */
	public OfficeFloorFigure(IFigure figure) {
		this(figure, figure);
	}

	/**
	 * Obtains the {@link IFigure} to represent the {@link EditPart}.
	 * 
	 * @return {@link IFigure} to represent the {@link EditPart}.
	 */
	public IFigure getFigure() {
		return this.figure;
	}

	/**
	 * Obtains the {@link IFigure} to add children of this {@link EditPart}.
	 * 
	 * @return {@link IFigure} to add children of this {@link EditPart}.
	 */
	public IFigure getContentPane() {
		return this.contentPane;
	}
}
