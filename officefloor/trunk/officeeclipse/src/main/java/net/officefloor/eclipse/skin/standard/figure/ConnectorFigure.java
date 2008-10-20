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
package net.officefloor.eclipse.skin.standard.figure;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.graphics.Color;

/**
 * {@link IFigure} providing a connector.
 * 
 * @author Daniel
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
	 * Initiate with defaults of {@link ColorConstants#black} and
	 * {@link ConnectorDirection#EAST}.
	 */
	public ConnectorFigure() {
		this(ConnectorDirection.EAST, ColorConstants.black);
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
		RectangleFigure rectangle = new RectangleFigure();
		rectangle.setBackgroundColor(colour);
		rectangle.setForegroundColor(colour);
		rectangle.setOpaque(true);
		switch (direction) {
		case WEST:
		case EAST:
			rectangle.setSize(5, 1);
			break;
		case NORTH:
		case SOUTH:
			rectangle.setSize(1, 5);
			break;
		}

		// Provide connector
		Ellipse point = new Ellipse();
		point.setBackgroundColor(colour);
		point.setForegroundColor(colour);
		point.setOpaque(true);
		point.setSize(4, 4);

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
