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
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.graphics.Color;

/**
 * {@link Figure} for an item of the {@link SubRoomModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionItemFigure extends Figure {

	/**
	 * {@link Figure} indicating if public.
	 */
	private final Ellipse isPublic;

	/**
	 * {@link ConnectorFigure} to obtain the {@link ConnectionAnchor}.
	 */
	private final ConnectorFigure connector;

	/**
	 * Name of the {@link SubRoomModel} item.
	 */
	private final Label itemName;

	/**
	 * Initiate.
	 * 
	 * @param itemName
	 *            Name of the {@link SubRoomModel} item.
	 * @param isPublic
	 *            Indicates if public.
	 * @param connectorDirection
	 *            {@link ConnectorDirection} that is either
	 *            {@link ConnectorDirection#WEST} or
	 *            {@link ConnectorDirection#EAST}.
	 * @param colour
	 *            {@link Color} of this {@link Figure}.
	 */
	public SubSectionItemFigure(String itemName, boolean isPublic,
			ConnectorDirection connectorDirection, Color colour) {

		// Specify layout
		this.setLayoutManager(new NoSpacingToolbarLayout(true));

		// Create the container
		Figure container = new Figure();
		NoSpacingGridLayout containerLayout = new NoSpacingGridLayout(5);
		containerLayout.horizontalSpacing = 3;
		container.setLayoutManager(containerLayout);
		this.add(container);

		// Add the is public figure
		this.isPublic = new Ellipse();
		this.isPublic.setBackgroundColor(colour);
		this.isPublic.setForegroundColor(colour);
		this.isPublic.setOpaque(true);
		this.isPublic.setSize(6, 6);
		container.add(this.isPublic);

		// Indicate is public state
		this.setIsPublic(isPublic);

		// Add spacing figure between public and west connector
		Figure spacing = new Figure();
		spacing.setSize(5, 1);
		container.add(spacing);

		// Add the west connector
		ConnectorFigure westConnector = new ConnectorFigure(
				ConnectorDirection.WEST, colour);
		container.add(westConnector);

		// Add the item name
		this.itemName = new Label(itemName);
		this.itemName.setLayoutManager(new NoSpacingToolbarLayout(true));
		this.itemName.setForegroundColor(colour);
		container.add(this.itemName);

		// Add the east connector
		ConnectorFigure eastConnector = new ConnectorFigure(
				ConnectorDirection.EAST, colour);
		container.add(eastConnector);

		// Specify connector direction
		switch (connectorDirection) {
		case WEST:
			// West, so hide east
			this.connector = westConnector;
			eastConnector.setVisible(false);
			break;
		case EAST:
			// East, so hide west
			this.connector = eastConnector;
			westConnector.setVisible(false);
			break;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName()
					+ " may not have direction "
					+ connectorDirection.toString());
		}
	}

	/**
	 * Flags whether this {@link SubRoomModel} item is public.
	 * 
	 * @param isPublic
	 *            <code>true</code> if public.
	 */
	public void setIsPublic(boolean isPublic) {
		this.isPublic.setVisible(isPublic);
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
	 * Specifies the name of the {@link SubRoomModel} item.
	 * 
	 * @param itemName
	 *            Name of the {@link SubRoomModel} item.
	 */
	public void setItemName(String itemName) {
		this.itemName.setText(itemName);
	}
}
