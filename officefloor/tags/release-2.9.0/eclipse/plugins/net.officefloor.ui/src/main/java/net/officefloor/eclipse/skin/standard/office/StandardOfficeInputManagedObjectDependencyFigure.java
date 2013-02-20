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

import net.officefloor.eclipse.skin.office.OfficeInputManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.office.OfficeInputManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeInputManagedObjectDependencyFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeInputManagedObjectDependencyFigure extends
		AbstractOfficeFloorFigure implements
		OfficeInputManagedObjectDependencyFigure {

	/**
	 * {@link OfficeInputManagedObjectDependencyModel} name.
	 */
	private final Label dependencyName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeInputManagedObjectDependencyFigureContext}.
	 */
	public StandardOfficeInputManagedObjectDependencyFigure(
			OfficeInputManagedObjectDependencyFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getOfficeInputManagedObjectDependencyName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.dependencyName = figure.getLabel();

		// Register connections
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeManagedObjectDependencyToOfficeManagedObjectModel.class,
				anchor);
		this.registerConnectionAnchor(
				OfficeManagedObjectDependencyToExternalManagedObjectModel.class,
				anchor);

		this.setFigure(figure);
	}

	/*
	 * ================= OfficeInputManagedObjectDependencyFigure ============
	 */

	@Override
	public void setOfficeInputManagedObjectDependencyName(
			String officeInputManagedObjectDependencyName) {
		this.dependencyName.setText(officeInputManagedObjectDependencyName);
	}

}