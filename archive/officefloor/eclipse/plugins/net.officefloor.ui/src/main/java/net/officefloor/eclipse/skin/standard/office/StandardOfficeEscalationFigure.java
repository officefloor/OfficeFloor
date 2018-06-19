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

import org.eclipse.draw2d.Label;

import net.officefloor.eclipse.skin.office.OfficeEscalationFigure;
import net.officefloor.eclipse.skin.office.OfficeEscalationFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;

/**
 * {@link OfficeEscalationFigure} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeEscalationFigure extends AbstractOfficeFloorFigure
		implements OfficeEscalationFigure {

	/**
	 * Escalation type name.
	 */
	private final Label escalationTypeName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeEscalationFigureContext}.
	 */
	public StandardOfficeEscalationFigure(OfficeEscalationFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getOfficeEscalationTypeName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());
		this.escalationTypeName = figure.getLabel();

		this.registerConnectionAnchor(
				OfficeEscalationToOfficeSectionInputModel.class,
				figure.getConnectionAnchor());

		this.setFigure(figure);
	}

	/*
	 * ======================= OfficeEscalationFigure ===================
	 */

	@Override
	public void setOfficeEscalationTypeName(String officeEscalationTypeName) {
		this.escalationTypeName.setText(officeEscalationTypeName);
	}

}