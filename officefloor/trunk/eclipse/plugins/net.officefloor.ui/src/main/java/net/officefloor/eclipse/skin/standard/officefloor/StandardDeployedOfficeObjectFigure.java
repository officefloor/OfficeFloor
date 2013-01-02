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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.DeployedOfficeObjectFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.OfficeItemFigure;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;

/**
 * Standard {@link DeployedOfficeObjectFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardDeployedOfficeObjectFigure extends
		AbstractOfficeFloorFigure implements DeployedOfficeObjectFigure {

	/**
	 * {@link DeployedOfficeObjectModel} {@link Figure}.
	 */
	private final OfficeItemFigure figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DeployedOfficeObjectFigureContext}.
	 */
	public StandardDeployedOfficeObjectFigure(
			DeployedOfficeObjectFigureContext context) {
		this.figure = new OfficeItemFigure(
				context.getDeployedOfficeObjectName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());
		ConnectionAnchor anchor = this.figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				DeployedOfficeObjectToOfficeFloorManagedObjectModel.class,
				anchor);
		this.registerConnectionAnchor(
				DeployedOfficeObjectToOfficeFloorInputManagedObjectModel.class,
				anchor);
		this.setFigure(this.figure);
	}

	/*
	 * ===================== DeployedOfficeObjectFigure ==================
	 */

	@Override
	public void setDeployedOfficeObjectName(String deployedOfficeObjectName) {
		this.figure.setItemName(deployedOfficeObjectName);
	}

}