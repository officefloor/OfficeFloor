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

import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigure;
import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;

import org.eclipse.draw2d.ColorConstants;
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

		// Obtain the short name
		String shortObjectTypeName = this.getShortTypeName(context
				.getObjectType());

		// Create the figure
		this.parameterFigure = new LabelConnectorFigure(shortObjectTypeName,
				ConnectorDirection.WEST, ColorConstants.black);
		this.registerConnectionAnchor(
				WorkTaskObjectToExternalManagedObjectModel.class,
				this.parameterFigure.getConnectionAnchor());

		// Specify initial state
		this.setIsParameter(context.isParameter());

		// Specify the figure
		this.setFigure(this.parameterFigure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.DeskTaskObjectFigure#setIsParameter
	 * (boolean)
	 */
	@Override
	public void setIsParameter(boolean isParameter) {
		this.parameterFigure.setConnectorVisible(!isParameter);
	}

	/**
	 * Obtains the short type name from the input type.
	 * 
	 * @param typeName
	 *            Type name.
	 * @return Short type name.
	 */
	private String getShortTypeName(String typeName) {
		// Obtain index of '.'
		int dotIndex = typeName.lastIndexOf('.');
		if (dotIndex < 0) {
			// Already short name
			return typeName;
		} else {
			// Return calculated short name (+1 to ignore '.')
			return typeName.substring((dotIndex + 1));
		}
	}

}
