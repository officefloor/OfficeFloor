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
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToTaskModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link DeskManagedObjectSourceFlowFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardDeskManagedObjectSourceFlowFigure extends
		AbstractOfficeFloorFigure implements DeskManagedObjectSourceFlowFigure {

	/**
	 * Name of the flow.
	 */
	private final Label flowName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DeskManagedObjectSourceFlowFigureContext}.
	 */
	public StandardDeskManagedObjectSourceFlowFigure(
			DeskManagedObjectSourceFlowFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getDeskManagedObjectSourceFlowName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.flowName = figure.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				DeskManagedObjectSourceFlowToTaskModel.class, anchor);
		this.registerConnectionAnchor(
				DeskManagedObjectSourceFlowToExternalFlowModel.class, anchor);

		// Specify the figure
		this.setFigure(figure);
	}

	/*
	 * ===================== DeskManagedObjectSourceFlowFigure ===============
	 */

	@Override
	public void setDeskManagedObjectSourceFlowName(
			String deskManagedObjectSourceFlowName) {
		this.flowName.setText(deskManagedObjectSourceFlowName);
	}

}