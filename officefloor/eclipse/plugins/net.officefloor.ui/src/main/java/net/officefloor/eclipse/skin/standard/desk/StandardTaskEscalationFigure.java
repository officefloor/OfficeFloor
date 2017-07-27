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

import net.officefloor.eclipse.skin.section.FunctionEscalationFigure;
import net.officefloor.eclipse.skin.section.FunctionEscalationFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;

/**
 * Standard {@link FunctionEscalationFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardTaskEscalationFigure extends AbstractOfficeFloorFigure
		implements FunctionEscalationFigure {

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link FunctionEscalationFigureContext}.
	 */
	public StandardTaskEscalationFigure(
			FunctionEscalationFigureContext context) {

		// Obtain simple name of escalation
		String escalationType = context.getEscalationType();
		String simpleType = escalationType;
		if (simpleType.indexOf('.') > 0) {
			simpleType = simpleType.substring(simpleType.lastIndexOf('.') + 1);
		}

		LabelConnectorFigure figure = new LabelConnectorFigure(simpleType,
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.registerConnectionAnchor(TaskEscalationToTaskModel.class,
				figure.getConnectionAnchor());
		this.registerConnectionAnchor(
				TaskEscalationToExternalFlowModel.class, figure
						.getConnectionAnchor());
		this.setFigure(figure);
	}
}
