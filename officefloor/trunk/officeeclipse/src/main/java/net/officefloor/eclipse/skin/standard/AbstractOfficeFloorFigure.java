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

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.Model;

/**
 * Abstract {@link OfficeFloorFigure}.
 * 
 * @author Daniel
 */
public abstract class AbstractOfficeFloorFigure implements OfficeFloorFigure {

	/**
	 * {@link IFigure} to represent the {@link EditPart}.
	 */
	private IFigure figure;

	/**
	 * {@link IFigure} to add children of this {@link EditPart}.
	 */
	private IFigure contentPane = null;

	/**
	 * Map of connection {@link Model} type to {@link ConnectionAnchor}.
	 */
	private final Map<Class<? extends Model>, ConnectionAnchor> connectionAnchors = new HashMap<Class<? extends Model>, ConnectionAnchor>(
			1);

	/**
	 * Initiate.
	 * 
	 * @param figure
	 *            {@link IFigure} to represent the {@link EditPart}.
	 * @param contentPane
	 *            {@link IFigure} to add children of this {@link EditPart}.
	 */
	public AbstractOfficeFloorFigure(IFigure figure, IFigure contentPane) {
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
	public AbstractOfficeFloorFigure(IFigure figure) {
		this.figure = figure;
	}

	/**
	 * <p>
	 * Allow complex creation of {@link IFigure} within the constructor.
	 * <p>
	 * Using this constructor you must ensure {@link #setFigure(IFigure)} is
	 * called with the {@link IFigure}.
	 * 
	 * @see #setFigure(IFigure)
	 * @see #setContentPane(IFigure)
	 */
	public AbstractOfficeFloorFigure() {
	}

	/**
	 * Specifies the {@link IFigure}.
	 * 
	 * @param figure
	 *            {@link IFigure}.
	 */
	protected void setFigure(IFigure figure) {
		this.figure = figure;
	}

	/**
	 * Specifies the content pane.
	 * 
	 * @param contentPane
	 *            Content pane {@link IFigure}.
	 */
	protected void setContentPane(IFigure contentPane) {
		this.contentPane = contentPane;
	}

	/**
	 * Registers a {@link ConnectionAnchor}.
	 * 
	 * @param connectionModelType
	 *            Connection {@link Model} type of the connection.
	 * @param connectionAnchor
	 *            {@link ConnectionAnchor} for the connection.
	 */
	protected void registerConnectionAnchor(
			Class<? extends Model> connectionModelType,
			ConnectionAnchor connectionAnchor) {
		this.connectionAnchors.put(connectionModelType, connectionAnchor);
	}

	/*
	 * ====================== AbstractOfficeFloorFigure =====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.OfficeFloorFigure#getFigure()
	 */
	@Override
	public IFigure getFigure() {

		// Ensure a figure available
		if (this.figure == null) {
			// Provide no figure (if one not configured)
			Label noFigure = new Label("ERROR: no figure provided");
			noFigure.setBackgroundColor(ColorConstants.red);
			noFigure.setOpaque(true);
			noFigure.setLayoutManager(new FlowLayout());
			this.figure = noFigure;
		}

		// Return the figure
		return this.figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.OfficeFloorFigure#getContentPane()
	 */
	@Override
	public IFigure getContentPane() {
		// Return figure if no content pane
		return (this.contentPane == null ? this.getFigure() : this.contentPane);
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
			// No configured anchor so anchor on figure
			connectionAnchor = new ChopboxAnchor(this.getFigure());
		}

		// Return the anchor
		return connectionAnchor;
	}

}
