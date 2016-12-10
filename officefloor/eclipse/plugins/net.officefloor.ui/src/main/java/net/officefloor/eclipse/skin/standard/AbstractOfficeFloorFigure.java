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
package net.officefloor.eclipse.skin.standard;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.ConnectionModel;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;

/**
 * Abstract {@link OfficeFloorFigure}.
 *
 * @author Daniel Sagenschneider
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
	 * Map of source {@link ConnectionModel} type to {@link ConnectionAnchor}.
	 */
	private final Map<Class<?>, ConnectionAnchor> sourceConnectionAnchors = new HashMap<Class<?>, ConnectionAnchor>(
			1);

	/**
	 * Map of target {@link ConnectionModel} type to {@link ConnectionAnchor}.
	 */
	private final Map<Class<?>, ConnectionAnchor> targetConnectionAnchors = new HashMap<Class<?>, ConnectionAnchor>(
			1);

	/**
	 * Map of {@link ConnectionModel} type to {@link ConnectionAnchor}.
	 */
	private final Map<Class<? extends ConnectionModel>, ConnectionAnchor> connectionAnchors = new HashMap<Class<? extends ConnectionModel>, ConnectionAnchor>(
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
	 *            {@link ConnectionModel} type for the connection.
	 * @param connectionAnchor
	 *            {@link ConnectionAnchor} for the connection.
	 */
	protected void registerConnectionAnchor(
			Class<? extends ConnectionModel> connectionModelType,
			ConnectionAnchor connectionAnchor) {
		this.connectionAnchors.put(connectionModelType, connectionAnchor);
	}

	/**
	 * Registers a source {@link ConnectionAnchor}.
	 *
	 * @param connectionModelType
	 *            {@link ConnectionModel} type for the connection.
	 * @param connectionAnchor
	 *            Source {@link ConnectionAnchor} for the connection.
	 */
	protected void registerSourceConnectionAnchor(
			Class<? extends ConnectionModel> connectionModelType,
			ConnectionAnchor connectionAnchor) {
		this.sourceConnectionAnchors.put(connectionModelType, connectionAnchor);
	}

	/**
	 * Registers a target {@link ConnectionAnchor}.
	 *
	 * @param connectionModelType
	 *            {@link ConnectionModel} type for the connection.
	 * @param connectionAnchor
	 *            Target {@link ConnectionAnchor} for the connection.
	 */
	protected void registerTargetConnectionAnchor(
			Class<? extends ConnectionModel> connectionModelType,
			ConnectionAnchor connectionAnchor) {
		this.targetConnectionAnchors.put(connectionModelType, connectionAnchor);
	}

	/*
	 * ====================== AbstractOfficeFloorFigure =====================
	 */

	@Override
	public IFigure getFigure() {

		// Ensure a figure available
		if (this.figure == null) {
			// Provide no figure (if one not configured)
			Label noFigure = new Label("ERROR: no figure provided");
			noFigure.setBackgroundColor(StandardOfficeFloorColours.ERROR());
			noFigure.setOpaque(true);
			noFigure.setLayoutManager(new FlowLayout());
			this.figure = noFigure;
		}

		// Return the figure
		return this.figure;
	}

	@Override
	public IFigure getContentPane() {
		// Return figure if no content pane
		return (this.contentPane == null ? this.getFigure() : this.contentPane);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
			Class<?> connectionModelType) {
		return this.getConnectionAnchor(this.sourceConnectionAnchors,
				connectionModelType);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
			Class<?> connectionModelType) {
		return this.getConnectionAnchor(this.targetConnectionAnchors,
				connectionModelType);
	}

	/**
	 * Obtains the {@link ConnectionAnchor}.
	 *
	 * @param specificConnectionAnchors
	 *            Specific {@link ConnectionAnchor} map.
	 * @param connectionModelType
	 *            {@link ConnectionModel} type.
	 * @return {@link ConnectionAnchor}.
	 */
	private ConnectionAnchor getConnectionAnchor(
			Map<Class<?>, ConnectionAnchor> specificConnectionAnchors,
			Class<?> connectionModelType) {

		// Obtain the specific connection anchor
		ConnectionAnchor connectionAnchor = specificConnectionAnchors
				.get(connectionModelType);
		if (connectionAnchor != null) {
			return connectionAnchor;
		}

		// Obtain default connection anchor
		connectionAnchor = this.connectionAnchors.get(connectionModelType);
		if (connectionAnchor != null) {
			return connectionAnchor;
		}

		// Return anchor around the figure
		return new ChopboxAnchor(this.getFigure());
	}

}