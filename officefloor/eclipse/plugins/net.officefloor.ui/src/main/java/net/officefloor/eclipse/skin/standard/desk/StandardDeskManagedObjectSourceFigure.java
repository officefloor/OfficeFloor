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

import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RectangleContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.DeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskManagedObjectToDeskManagedObjectSourceModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.SWT;

/**
 * Standard {@link DeskManagedObjectSourceFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardDeskManagedObjectSourceFigure extends
		AbstractOfficeFloorFigure implements DeskManagedObjectSourceFigure {

	/**
	 * {@link Label} containing the {@link DeskManagedObjectSourceModel} name.
	 */
	private final Label deskManagedObjectSourceName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link DeskManagedObjectSourceFigureContext}.
	 */
	public StandardDeskManagedObjectSourceFigure(
			DeskManagedObjectSourceFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(1);
		figure.setLayoutManager(figureLayout);

		// Add the managed object connector
		ConnectorFigure managedObject = new ConnectorFigure(
				ConnectorDirection.NORTH, StandardOfficeFloorColours
						.LINK_LINE());
		figureLayout.setConstraint(managedObject, new GridData(SWT.CENTER,
				SWT.BEGINNING, true, false));
		figure.add(managedObject);

		// Register the connections to managed objects
		ConnectionAnchor managedObjectAnchor = managedObject
				.getConnectionAnchor();
		this.registerConnectionAnchor(
				DeskManagedObjectToDeskManagedObjectSourceModel.class,
				managedObjectAnchor);

		// Create the managed object source
		RectangleContainerFigure mos = new RectangleContainerFigure(context
				.getDeskManagedObjectSourceName(), StandardOfficeFloorColours
				.MANAGED_OBJECT_SOURCE(), 20, false);
		this.deskManagedObjectSourceName = mos.getContainerName();
		figure.add(mos);

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(mos.getContentPane());
	}

	/*
	 * ================ DeskManagedObjectSourceFigure ===================
	 */

	@Override
	public void setDeskManagedObjectName(String deskManagedObjectSourceName) {
		this.deskManagedObjectSourceName.setText(deskManagedObjectSourceName);
	}

	@Override
	public IFigure getDeskManagedObjectSourceNameFigure() {
		return this.deskManagedObjectSourceName;
	}

}