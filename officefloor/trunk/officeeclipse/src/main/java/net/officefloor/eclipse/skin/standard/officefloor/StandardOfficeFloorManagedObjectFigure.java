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

import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * {@link OfficeFloorManagedObjectFigure} implementation.
 * 
 * @author Daniel
 */
public class StandardOfficeFloorManagedObjectFigure extends
		AbstractOfficeFloorFigure implements OfficeFloorManagedObjectFigure {

	/**
	 * {@link Label} containing the {@link OfficeFloorManagedObjectModel} name.
	 */
	private final Label officeFloorManagedObjectName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectFigureContext}.
	 */
	public StandardOfficeFloorManagedObjectFigure(
			OfficeFloorManagedObjectFigureContext context) {

		Color moColor = ColorConstants.lightBlue;

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(1);
		figure.setLayoutManager(figureLayout);

		// Figure to contain office object and managed object container
		Figure objectAndMo = new Figure();
		NoSpacingGridLayout objectAndMoLayout = new NoSpacingGridLayout(2);
		objectAndMo.setLayoutManager(objectAndMoLayout);
		figure.add(objectAndMo);

		// Add the office object and managed object dependency connector
		ConnectorFigure dependency = new ConnectorFigure(
				ConnectorDirection.WEST, ColorConstants.black);
		dependency.setBorder(new MarginBorder(10, 0, 0, 0));
		objectAndMoLayout.setConstraint(dependency, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));
		objectAndMo.add(dependency);

		// Register connection to office objects and dependencies
		ConnectionAnchor dependencyAnchor = dependency.getConnectionAnchor();
		this.registerConnectionAnchor(
				DeployedOfficeObjectToOfficeFloorManagedObjectModel.class,
				dependencyAnchor);
		this
				.registerConnectionAnchor(
						OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel.class,
						dependencyAnchor);

		// Create the managed object source
		RoundedContainerFigure mo = new RoundedContainerFigure(context
				.getOfficeFloorManagedObjectName(), moColor, 20, false);
		this.officeFloorManagedObjectName = mo.getContainerName();
		objectAndMo.add(mo);

		// Add the managed object source connector
		ConnectorFigure mos = new ConnectorFigure(ConnectorDirection.SOUTH,
				ColorConstants.lightBlue);
		figureLayout.setConstraint(mos, new GridData(SWT.CENTER, SWT.BEGINNING,
				true, false));
		figure.add(mos);

		// Register the connections to managed object source
		this
				.registerConnectionAnchor(
						OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel.class,
						mos.getConnectionAnchor());

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(mo.getContentPane());
	}

	/*
	 * ================ OfficeFloorManagedObjectFigure =====================
	 */

	@Override
	public void setOfficeFloorManagedObjectName(
			String officeFloorManagedObjectName) {
		this.officeFloorManagedObjectName.setText(officeFloorManagedObjectName);
	}

	@Override
	public IFigure getOfficeFloorManagedObjectNameFigure() {
		return this.officeFloorManagedObjectName;
	}

}