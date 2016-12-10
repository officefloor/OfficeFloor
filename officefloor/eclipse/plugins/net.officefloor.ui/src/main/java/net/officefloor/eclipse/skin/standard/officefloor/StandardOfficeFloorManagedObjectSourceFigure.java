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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RectangleContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;

/**
 * Standard {@link OfficeFloorManagedObjectSourceFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorManagedObjectSourceFigure extends
		AbstractOfficeFloorFigure implements
		OfficeFloorManagedObjectSourceFigure {

	/**
	 * {@link Label} containing the {@link OfficeFloorManagedObjectSourceModel}
	 * name.
	 */
	private final Label officeFloorManagedObjectSourceName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceFigureContext}.
	 */
	public StandardOfficeFloorManagedObjectSourceFigure(
			OfficeFloorManagedObjectSourceFigureContext context) {

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
		this
				.registerConnectionAnchor(
						OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel.class,
						managedObjectAnchor);
		this
				.registerConnectionAnchor(
						OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel.class,
						managedObjectAnchor);

		// Figure to contain office connector and source container
		Figure officeAndMos = new Figure();
		NoSpacingGridLayout officeAndMosLayout = new NoSpacingGridLayout(2);
		officeAndMos.setLayoutManager(officeAndMosLayout);
		figure.add(officeAndMos);

		// Add the office connector
		ConnectorFigure office = new ConnectorFigure(ConnectorDirection.WEST,
				StandardOfficeFloorColours.BLACK());
		office.setBorder(new MarginBorder(10, 0, 0, 0));
		officeAndMos.add(office);
		officeAndMosLayout.setConstraint(office, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));

		// Register the connection to managing office and input managed object
		ConnectionAnchor officeInputMoAnchor = office.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectSourceToDeployedOfficeModel.class,
				officeInputMoAnchor);
		this
				.registerConnectionAnchor(
						OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel.class,
						officeInputMoAnchor);

		// Create the managed object source
		RectangleContainerFigure mos = new RectangleContainerFigure(context
				.getOfficeFloorManagedObjectSourceName(),
				StandardOfficeFloorColours.MANAGED_OBJECT_SOURCE(), 20, false);
		this.officeFloorManagedObjectSourceName = mos.getContainerName();
		officeAndMos.add(mos);

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(mos.getContentPane());
	}

	/*
	 * ================ OfficeFloorManagedObjectSourceFigure ===================
	 */

	@Override
	public void setOfficeFloorManagedObjectName(
			String officeFloorManagedObjectSourceName) {
		this.officeFloorManagedObjectSourceName
				.setText(officeFloorManagedObjectSourceName);
	}

	@Override
	public IFigure getOfficeFloorManagedObjectSourceNameFigure() {
		return this.officeFloorManagedObjectSourceName;
	}

}