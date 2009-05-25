/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RectangleContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Standard {@link OfficeFloorManagedObjectSourceFigure}.
 * 
 * @author Daniel
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

		Color moColor = ColorConstants.lightGray;

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(1);
		figure.setLayoutManager(figureLayout);

		// Add the managed object connector
		ConnectorFigure managedObject = new ConnectorFigure(
				ConnectorDirection.NORTH, ColorConstants.lightBlue);
		figureLayout.setConstraint(managedObject, new GridData(SWT.CENTER,
				SWT.BEGINNING, true, false));
		figure.add(managedObject);

		// Register the connections to managed objects
		this
				.registerConnectionAnchor(
						OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel.class,
						managedObject.getConnectionAnchor());

		// Figure to contain office connector and source container
		Figure officeAndMos = new Figure();
		NoSpacingGridLayout officeAndMosLayout = new NoSpacingGridLayout(2);
		officeAndMos.setLayoutManager(officeAndMosLayout);
		figure.add(officeAndMos);

		// Add the office connector
		ConnectorFigure office = new ConnectorFigure(ConnectorDirection.WEST,
				ColorConstants.black);
		office.setBorder(new MarginBorder(10, 0, 0, 0));
		officeAndMos.add(office);
		officeAndMosLayout.setConstraint(office, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));

		// Register the connection to managing office
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectSourceToDeployedOfficeModel.class,
				office.getConnectionAnchor());

		// Create the managed object source
		RectangleContainerFigure mos = new RectangleContainerFigure(context
				.getOfficeFloorManagedObjectSourceName(), moColor, 20, false);
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