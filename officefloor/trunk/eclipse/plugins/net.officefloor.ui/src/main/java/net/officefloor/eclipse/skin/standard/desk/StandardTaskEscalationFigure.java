/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.TaskEscalationFigure;
import net.officefloor.eclipse.skin.desk.TaskEscalationFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;

import org.eclipse.draw2d.ColorConstants;

/**
 * Standard {@link TaskEscalationFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardTaskEscalationFigure extends AbstractOfficeFloorFigure
		implements TaskEscalationFigure {

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link TaskEscalationFigureContext}.
	 */
	public StandardTaskEscalationFigure(
			TaskEscalationFigureContext context) {

		// Obtain simple name of escalation
		String escalationType = context.getEscalationType();
		String simpleType = escalationType;
		if (simpleType.indexOf('.') > 0) {
			simpleType = simpleType.substring(simpleType.lastIndexOf('.') + 1);
		}

		LabelConnectorFigure figure = new LabelConnectorFigure(simpleType,
				ConnectorDirection.EAST, ColorConstants.black);
		this.registerConnectionAnchor(TaskEscalationToTaskModel.class,
				figure.getConnectionAnchor());
		this.registerConnectionAnchor(
				TaskEscalationToExternalFlowModel.class, figure
						.getConnectionAnchor());
		this.setFigure(figure);
	}
}
