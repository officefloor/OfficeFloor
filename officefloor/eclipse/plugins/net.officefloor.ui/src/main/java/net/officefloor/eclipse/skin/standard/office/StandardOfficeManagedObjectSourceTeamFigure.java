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
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceTeamFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceTeamFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeManagedObjectSourceTeamFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeManagedObjectSourceTeamFigure extends
		AbstractOfficeFloorFigure implements
		OfficeManagedObjectSourceTeamFigure {

	/**
	 * Team name.
	 */
	private final Label teamName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeManagedObjectSourceTeamFigure}.
	 */
	public StandardOfficeManagedObjectSourceTeamFigure(
			OfficeManagedObjectSourceTeamFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getOfficeManagedObjectSourceTeamName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.teamName = figure.getLabel();
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeManagedObjectSourceTeamToOfficeTeamModel.class, anchor);
		this.setFigure(figure);
	}

	/*
	 * ============= OfficeManagedObjectSourceTeamFigure =============
	 */

	@Override
	public void setOfficeManagedObjectSourceTeamName(
			String officeManagedObjectSourceTeamName) {
		this.teamName.setText(officeManagedObjectSourceTeamName);
	}

}