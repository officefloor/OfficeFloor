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
package net.officefloor.eclipse.skin.standard.figure;

import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.graphics.Color;

/**
 * {@link Figure} providing named connector.
 * 
 * @author Daniel Sagenschneider
 */
public class LabelConnectorFigure extends Figure {

	/**
	 * {@link ConnectorFigure}.
	 */
	private final ConnectorFigure connector;

	/**
	 * {@link Label}.
	 */
	private final Label label;

	/**
	 * Initiate.
	 * 
	 * @param label
	 *            Text of connector {@link Label}.
	 * @param direction
	 *            {@link ConnectorDirection}.
	 * @param colour
	 *            Colour of connector.
	 */
	public LabelConnectorFigure(String label, ConnectorDirection direction,
			Color colour) {
		this(new Label(label), direction, colour);
	}

	/**
	 * Initiate.
	 * 
	 * @param label
	 *            {@link Label} for the connector.
	 * @param direction
	 *            {@link ConnectorDirection}.
	 * @param colour
	 *            Colour of connector.
	 */
	public LabelConnectorFigure(Label label, ConnectorDirection direction,
			Color colour) {
		this.label = label;

		// Ensure render
		this.setLayoutManager(new NoSpacingToolbarLayout(true));

		// Create figure
		Figure figure = new Figure();

		// Specify layout
		NoSpacingGridLayout figureLayout = null;
		switch (direction) {
		case WEST:
		case EAST:
			figureLayout = new NoSpacingGridLayout(2);
			break;
		case NORTH:
		case SOUTH:
			figureLayout = new NoSpacingGridLayout(1);
			break;
		}
		figureLayout.horizontalSpacing = 2;
		figureLayout.verticalSpacing = 2;
		figure.setLayoutManager(figureLayout);

		// Provide name
		this.label.setLayoutManager(new NoSpacingToolbarLayout(true));
		this.label.setForegroundColor(colour);

		// Provide connector
		this.connector = new ConnectorFigure(direction, colour);

		// Add in appropriate order
		switch (direction) {
		case NORTH:
		case WEST:
			figure.add(this.connector);
			figure.add(label);
			break;
		case SOUTH:
		case EAST:
			figure.add(label);
			figure.add(this.connector);
			break;
		}

		// Add the figure
		this.add(figure);
	}

	/**
	 * Obtains the {@link Label}.
	 * 
	 * @return {@link Label}.
	 */
	public Label getLabel() {
		return this.label;
	}

	/**
	 * Obtains the {@link ConnectionAnchor}.
	 * 
	 * @return {@link ConnectionAnchor}.
	 */
	public ConnectionAnchor getConnectionAnchor() {
		return this.connector.getConnectionAnchor();
	}

	/**
	 * Specifies whether the connector is visible.
	 * 
	 * @param isVisible
	 *            <code>true</code> is visible.
	 */
	public void setConnectorVisible(boolean isVisible) {
		this.connector.setVisible(isVisible);
	}

}