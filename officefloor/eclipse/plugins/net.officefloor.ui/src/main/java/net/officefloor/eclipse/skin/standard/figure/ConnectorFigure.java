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

import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.graphics.Color;

/**
 * {@link IFigure} providing a connector.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectorFigure extends Figure {

	/**
	 * Direction of the connector.
	 */
	public static enum ConnectorDirection {
		NORTH, WEST, EAST, SOUTH
	};

	/**
	 * {@link ConnectionAnchor}.
	 */
	private final ConnectionAnchor connectionAnchor;

	/**
	 * Initiate with defaults of {@link StandardOfficeFloorColours#BLACK()} and
	 * {@link ConnectorDirection#EAST}.
	 */
	public ConnectorFigure() {
		this(ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
	}

	/**
	 * Initiate.
	 * 
	 * @param direction
	 *            {@link ConnectorDirection}.
	 * @param colour
	 *            {@link Color} of connector.
	 */
	public ConnectorFigure(ConnectorDirection direction, Color colour) {

		final int SIZE = 4;
		final int ALIGNMENT = -1;

		// Specify layout
		this.setLayoutManager(new NoSpacingToolbarLayout(true));

		// Create the figure
		Figure figure = new Figure();

		// Specify figure layout
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
		figure.setLayoutManager(figureLayout);

		// Create line
		Figure rectangle = new Figure();
		rectangle.setLayoutManager(new NoSpacingToolbarLayout(true));
		RectangleFigure rectangleShape = new RectangleFigure();
		rectangleShape.setBackgroundColor(colour);
		rectangleShape.setForegroundColor(colour);
		rectangleShape.setAlpha(50);
		rectangleShape.setOpaque(true);
		int topInset = 0;
		int leftInset = 0;
		switch (direction) {
		case WEST:
		case EAST:
			rectangleShape.setSize(SIZE + 1, 1);
			topInset = (SIZE / 2) + ALIGNMENT;
			break;
		case NORTH:
		case SOUTH:
			rectangleShape.setSize(1, SIZE + 1);
			leftInset = (SIZE / 2) + ALIGNMENT;
			break;
		}
		rectangle.add(rectangleShape);
		rectangle.setBorder(new MarginBorder(topInset, leftInset, 0, 0));

		// Provide connector
		Ellipse point = new Ellipse();
		point.setBackgroundColor(colour);
		point.setForegroundColor(colour);
		point.setAlpha(100);
		point.setOpaque(true);
		point.setSize(SIZE, SIZE);

		// Provide connection anchor
		this.connectionAnchor = new ChopboxAnchor(point);

		// Load in appropriate order
		switch (direction) {
		case WEST:
		case NORTH:
			figure.add(point);
			figure.add(rectangle);
			break;
		case EAST:
		case SOUTH:
			figure.add(rectangle);
			figure.add(point);
			break;
		}

		// Add the figure
		this.add(figure);
	}

	/**
	 * Obtains the {@link ConnectionAnchor}.
	 * 
	 * @return {@link ConnectionAnchor}.
	 */
	public ConnectionAnchor getConnectionAnchor() {
		return this.connectionAnchor;
	}
}
