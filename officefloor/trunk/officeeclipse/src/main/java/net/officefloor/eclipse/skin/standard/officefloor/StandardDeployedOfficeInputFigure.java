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

import net.officefloor.eclipse.skin.officefloor.DeployedOfficeInputFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeInputFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.OfficeItemFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link DeployedOfficeInputFigure}.
 * 
 * @author Daniel
 */
public class StandardDeployedOfficeInputFigure extends AbstractOfficeFloorFigure
		implements DeployedOfficeInputFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DeployedOfficeInputFigureContext}.
	 */
	public StandardDeployedOfficeInputFigure(DeployedOfficeInputFigureContext context) {
		OfficeItemFigure figure = new OfficeItemFigure(context.getTaskName(),
				ConnectorDirection.EAST, ColorConstants.black);
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this
				.registerConnectionAnchor(
						OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class,
						anchor);
		this.setFigure(figure);
	}

}