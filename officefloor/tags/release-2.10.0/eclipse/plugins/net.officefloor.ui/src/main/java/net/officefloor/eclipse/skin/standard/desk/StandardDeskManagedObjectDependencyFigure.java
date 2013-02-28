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

import net.officefloor.eclipse.skin.desk.DeskManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToExternalManagedObjectModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link DeskManagedObjectDependencyFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardDeskManagedObjectDependencyFigure extends
		AbstractOfficeFloorFigure implements DeskManagedObjectDependencyFigure {

	/**
	 * Name of the dependency.
	 */
	private final Label dependencyName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DeskManagedObjectDependencyFigureContext}.
	 */
	public StandardDeskManagedObjectDependencyFigure(
			DeskManagedObjectDependencyFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getDeskManagedObjectDependencyName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.dependencyName = figure.getLabel();

		// Register connections
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				DeskManagedObjectDependencyToDeskManagedObjectModel.class,
				anchor);
		this.registerConnectionAnchor(
				DeskManagedObjectDependencyToExternalManagedObjectModel.class,
				anchor);

		this.setFigure(figure);
	}

	/*
	 * =============== DeskManagedObjectDependencyFigure ======================
	 */

	@Override
	public void setDeskManagedObjectDependencyName(
			String deskManagedObjectDependencyName) {
		this.dependencyName.setText(deskManagedObjectDependencyName);
	}

}