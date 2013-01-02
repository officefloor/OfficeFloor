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
package net.officefloor.eclipse.conform.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.conform.ExistingItemModel;
import net.officefloor.model.conform.ExistingItemToTargetItemModel;
import net.officefloor.model.conform.TargetItemModel;

/**
 * {@link OfficeFloorFigure} for the {@link ExistingItemModel} or
 * {@link TargetItemModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConformModelItemFigure extends AbstractOfficeFloorFigure {

	/**
	 * {@link Label} displaying the item name.
	 */
	private final Label itemName;

	/**
	 * Initiate.
	 * 
	 * @param itemName
	 *            Name of the {@link ExistingItemModel} or
	 *            {@link TargetItemModel}.
	 * @param isExistingNotTarget
	 *            Flag indicating if {@link ExistingItemModel} rather than
	 *            {@link TargetItemModel}.
	 */
	public ConformModelItemFigure(String itemName, boolean isExistingNotTarget) {
		// Create the connector figure
		LabelConnectorFigure figure = new LabelConnectorFigure(itemName,
				(isExistingNotTarget ? ConnectorDirection.EAST
						: ConnectorDirection.WEST), ColorConstants.black);
		this.itemName = figure.getLabel();

		// Register the anchor for the connection
		this.registerConnectionAnchor(ExistingItemToTargetItemModel.class,
				figure.getConnectionAnchor());

		// Specify the figure
		this.setFigure(figure);
	}

	/**
	 * Specifies the new item name to display.
	 * 
	 * @param itemName
	 *            New item name to display.
	 */
	public void setItemName(String itemName) {
		this.itemName.setText(itemName);
	}

}