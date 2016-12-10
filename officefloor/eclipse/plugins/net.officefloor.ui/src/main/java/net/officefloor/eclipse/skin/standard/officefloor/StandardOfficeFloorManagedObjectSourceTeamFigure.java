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

import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceTeamFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceTeamFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeFloorManagedObjectSourceTeamFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorManagedObjectSourceTeamFigure extends
		AbstractOfficeFloorFigure implements
		OfficeFloorManagedObjectSourceTeamFigure {

	/**
	 * Team name.
	 */
	private final Label teamName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceTeamFigure}.
	 */
	public StandardOfficeFloorManagedObjectSourceTeamFigure(
			OfficeFloorManagedObjectSourceTeamFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getOfficeFloorManagedObjectSourceTeamName(),
				ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
		this.teamName = figure.getLabel();
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				anchor);
		this.setFigure(figure);
	}

	/*
	 * ============= OfficeFloorManagedObjectSourceTeamFigure ============
	 */

	@Override
	public void setOfficeFloorManagedObjectSourceTeamName(
			String officeFloorManagedObjectSourceTeamName) {
		this.teamName.setText(officeFloorManagedObjectSourceTeamName);
	}

}