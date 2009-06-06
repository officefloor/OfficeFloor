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
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.OfficeEscalationFigure;
import net.officefloor.eclipse.skin.office.OfficeEscalationFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;

/**
 * {@link OfficeEscalationFigure} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeEscalationFigure extends AbstractOfficeFloorFigure
		implements OfficeEscalationFigure {

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link OfficeEscalationFigureContext}.
	 */
	public StandardOfficeEscalationFigure(OfficeEscalationFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(context
				.getOfficeEscalationTypeName(), ConnectorDirection.WEST,
				StandardOfficeFloorColours.BLACK());

		this.setFigure(figure);
	}

}