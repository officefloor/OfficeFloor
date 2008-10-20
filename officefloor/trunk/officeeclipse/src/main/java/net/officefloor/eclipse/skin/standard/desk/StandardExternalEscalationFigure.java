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
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.ExternalEscalationFigure;
import net.officefloor.eclipse.skin.desk.ExternalEscalationFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.FlowItemEscalationToExternalEscalationModel;

import org.eclipse.draw2d.ColorConstants;

/**
 * Standard {@link ExternalEscalationFigure}.
 * 
 * @author Daniel
 */
public class StandardExternalEscalationFigure extends AbstractOfficeFloorFigure
		implements ExternalEscalationFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ExternalEscalationFigureContext}.
	 */
	public StandardExternalEscalationFigure(
			ExternalEscalationFigureContext context) {

		// Create the figure
		LabelConnectorFigure figure = new LabelConnectorFigure(context
				.getExternalEscalationName(), ConnectorDirection.WEST,
				ColorConstants.red);
		this.registerConnectionAnchor(
				FlowItemEscalationToExternalEscalationModel.class, figure
						.getConnectionAnchor());

		// Specify the figure
		this.setFigure(figure);
	}
}
