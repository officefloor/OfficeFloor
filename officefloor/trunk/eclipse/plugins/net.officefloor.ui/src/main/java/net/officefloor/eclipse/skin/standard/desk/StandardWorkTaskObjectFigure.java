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

import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigure;
import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.model.desk.WorkTaskObjectToDeskManagedObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;

/**
 * {@link WorkTaskObjectFigure} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardWorkTaskObjectFigure extends AbstractOfficeFloorFigure
		implements WorkTaskObjectFigure {

	/**
	 * {@link Figure} allowing to specify if a parameter.
	 */
	private final LabelConnectorFigure parameterFigure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link WorkTaskObjectFigureContext}.
	 */
	public StandardWorkTaskObjectFigure(WorkTaskObjectFigureContext context) {

		// Obtain the name
		String objectName = this.getWorkTaskObjectName(context);

		// Create the figure
		this.parameterFigure = new LabelConnectorFigure(objectName,
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		ConnectionAnchor anchor = this.parameterFigure.getConnectionAnchor();
		this.registerConnectionAnchor(
				WorkTaskObjectToExternalManagedObjectModel.class, anchor);
		this.registerConnectionAnchor(
				WorkTaskObjectToDeskManagedObjectModel.class, anchor);

		// Specify initial state
		this.setIsParameter(context.isParameter());

		// Specify the figure
		this.setFigure(this.parameterFigure);
	}

	/**
	 * Obtains the short type name from the input type.
	 * 
	 * @param typeName
	 *            Type name.
	 * @return Short type name.
	 */
	private String getWorkTaskObjectName(WorkTaskObjectFigureContext context) {

		// Determine if have name (not a number)
		String name = context.getWorkTaskObjectName();
		if ((name != null) && (name.trim().length() > 0)) {
			try {
				Integer.parseInt(name);
			} catch (NumberFormatException ex) {
				// Use the name as not a number
				return name;
			}
		}

		// Obtain index of '.'
		String typeName = context.getObjectType();
		int dotIndex = typeName.lastIndexOf('.');
		if (dotIndex < 0) {
			// Already short name
			return typeName;
		} else {
			// Return calculated short name (+1 to ignore '.')
			return typeName.substring((dotIndex + 1));
		}
	}

	/*
	 * ===================== WorkTaskObjectFigure =======================
	 */

	@Override
	public void setWorkTaskObjectName(WorkTaskObjectFigureContext context) {
		String objectName = this.getWorkTaskObjectName(context);
		this.parameterFigure.getLabel().setText(objectName);
	}

	@Override
	public void setIsParameter(boolean isParameter) {
		this.parameterFigure.setConnectorVisible(!isParameter);
	}

}