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

import net.officefloor.eclipse.skin.officefloor.OfficeFloorInputManagedObjectFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorInputManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.SWT;

/**
 * Standard {@link OfficeFloorInputManagedObjectFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorInputManagedObjectFigure extends
		AbstractOfficeFloorFigure implements
		OfficeFloorInputManagedObjectFigure {

	/**
	 * {@link IFigure} containing the name.
	 */
	private Label name;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeFloorInputManagedObjectFigureContext}.
	 */
	public StandardOfficeFloorInputManagedObjectFigure(
			OfficeFloorInputManagedObjectFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(1);
		figure.setLayoutManager(figureLayout);

		// Figure to contain office object, container, managed object source
		Figure objectContainerMos = new Figure();
		NoSpacingGridLayout objectContainerMosLayout = new NoSpacingGridLayout(
				3);
		objectContainerMos.setLayoutManager(objectContainerMosLayout);
		figure.add(objectContainerMos);

		// Add the office object anchor
		ConnectorFigure dependency = new ConnectorFigure(
				ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
		dependency.setBorder(new MarginBorder(5, 0, 0, 0));
		objectContainerMosLayout.setConstraint(dependency, new GridData(
				SWT.BEGINNING, SWT.BEGINNING, false, false));
		objectContainerMos.add(dependency);

		// Register connection to office object
		this.registerConnectionAnchor(
				DeployedOfficeObjectToOfficeFloorInputManagedObjectModel.class,
				dependency.getConnectionAnchor());
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel.class,
				dependency.getConnectionAnchor());

		// Add the container for input managed object name
		RectangleFigure container = new RectangleFigure();
		container.setLayoutManager(new NoSpacingGridLayout(1));
		container.setBackgroundColor(StandardOfficeFloorColours
				.INPUT_MANAGED_OBJECT());
		this.name = new Label(context.getOfficeFloorInputManagedObjectName());
		this.name.setBorder(new MarginBorder(3, 3, 3, 3));
		container.add(this.name);
		objectContainerMos.add(container);

		// Add the managed object source anchor
		ConnectorFigure mos = new ConnectorFigure(ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());
		mos.setBorder(new MarginBorder(5, 0, 0, 0));
		objectContainerMosLayout.setConstraint(mos, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));
		objectContainerMos.add(mos);

		// Register connection to managed object source
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel.class,
				mos.getConnectionAnchor());

		// Add the bound managed object source anchor
		ConnectorFigure boundMos = new ConnectorFigure(
				ConnectorDirection.SOUTH, StandardOfficeFloorColours.BLACK());
		figureLayout.setConstraint(boundMos, new GridData(SWT.CENTER,
				SWT.BEGINNING, true, false));
		figure.add(boundMos);

		// Register connection to bound managed object source
		this.registerConnectionAnchor(
				OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel.class,
				boundMos.getConnectionAnchor());

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(container);
	}

	/*
	 * ============= OfficeFloorInputManagedObjectFigure ==================
	 */

	@Override
	public void setOfficeFloorInputManagedObjectName(
			String officeFloorInputManagedObjectName) {
		this.name.setText(officeFloorInputManagedObjectName);
	}

	@Override
	public IFigure getOfficeFloorInputManagedObjectNameFigure() {
		return this.name;
	}

}