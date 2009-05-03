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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.TeamFigure;
import net.officefloor.eclipse.skin.officefloor.TeamFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link TeamFigure}.
 * 
 * @author Daniel
 */
public class StandardTeamFigure extends AbstractOfficeFloorFigure implements
		TeamFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link TeamFigureContext}.
	 */
	public StandardTeamFigure(TeamFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(context
				.getTeamName(), ConnectorDirection.EAST, ColorConstants.black);
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				DeployedOfficeTeamToOfficeFloorTeamModel.class, anchor);
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				anchor);
		this.setFigure(figure);
	}

}