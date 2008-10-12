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
package net.officefloor.eclipse.skin.standard;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.Model;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * Abstract {@link OfficeFloorFigure}.
 * 
 * @author Daniel
 */
public class OfficeFloorFigureImpl implements OfficeFloorFigure {

	/**
	 * {@link IFigure} to represent the {@link EditPart}.
	 */
	private final IFigure figure;

	/**
	 * {@link IFigure} to add children of this {@link EditPart}.
	 */
	private final IFigure contentPane;

	/**
	 * Map of connection {@link Model} type to {@link ConnectionAnchor}.
	 */
	private final Map<Class<?>, ConnectionAnchor> connectionAnchors = new HashMap<Class<?>, ConnectionAnchor>();

	/**
	 * Initiate.
	 * 
	 * @param figure
	 *            {@link IFigure} to represent the {@link EditPart}.
	 * @param contentPane
	 *            {@link IFigure} to add children of this {@link EditPart}.
	 */
	public OfficeFloorFigureImpl(IFigure figure, IFigure contentPane) {
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
	public OfficeFloorFigureImpl(IFigure figure) {
		this(figure, figure);
	}

	/**
	 * Override to register the {@link ConnectionAnchor}.
	 * 
	 * @param connectionModelType
	 *            Connection {@link Model} type of the connection.
	 * @param connectionAnchor
	 *            {@link ConnectionAnchor} for the connection.
	 */
	protected void registerConnectionAnchor(Class<?> connectionModelType,
			ConnectionAnchor connectionAnchor) {
		// By default load no connection anchors
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.OfficeFloorFigure#getFigure()
	 */
	@Override
	public IFigure getFigure() {
		return this.figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.OfficeFloorFigure#getContentPane()
	 */
	@Override
	public IFigure getContentPane() {
		return this.contentPane;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.OfficeFloorFigure#getConnectionAnchor(java
	 * .lang.Class)
	 */
	@Override
	public ConnectionAnchor getConnectionAnchor(Class<?> connectionModelType) {
		// Determine if have connection anchor
		ConnectionAnchor connectionAnchor = this.connectionAnchors
				.get(connectionModelType);
		if (connectionAnchor == null) {
			// Provide wrapping figure
			connectionAnchor = new ChopboxAnchor(this.getFigure());
		}

		// Return the anchor
		return connectionAnchor;
	}

}
