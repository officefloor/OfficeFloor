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

import net.officefloor.eclipse.skin.officefloor.DeployedOfficeTeamFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeTeamFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.OfficeItemFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;

import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link DeployedOfficeTeamFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardDeployedOfficeTeamFigure extends AbstractOfficeFloorFigure
		implements DeployedOfficeTeamFigure {

	/**
	 * {@link OfficeItemFigure}.
	 */
	private final OfficeItemFigure figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DeployedOfficeTeamFigureContext}.
	 */
	public StandardDeployedOfficeTeamFigure(
			DeployedOfficeTeamFigureContext context) {
		this.figure = new OfficeItemFigure(context.getDeployedOfficeTeamName(),
				ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
		ConnectionAnchor anchor = this.figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				DeployedOfficeTeamToOfficeFloorTeamModel.class, anchor);
		this.setFigure(this.figure);
	}

	/*
	 * ===================== DeployedOfficeTeamFigure ========================
	 */

	@Override
	public void setDeployedOfficeTeamName(String deployedOfficeTeamName) {
		this.figure.setItemName(deployedOfficeTeamName);
	}

}