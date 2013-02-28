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

import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;

/**
 * {@link OfficeFloorManagedObjectFigure} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorManagedObjectFigure extends
		AbstractOfficeFloorFigure implements OfficeFloorManagedObjectFigure {

	/**
	 * {@link Label} containing the {@link OfficeFloorManagedObjectModel} name.
	 */
	private final Label officeFloorManagedObjectName;

	/**
	 * {@link OfficeFloorManagedObjectFigureContext}.
	 */
	private final OfficeFloorManagedObjectFigureContext context;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link OfficeFloorManagedObjectFigureContext}.
	 */
	public StandardOfficeFloorManagedObjectFigure(
			OfficeFloorManagedObjectFigureContext context) {
		this.context = context;

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
				ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
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
		RoundedContainerFigure mo = new RoundedContainerFigure(this
				.getOfficeFloorManagedObjectLabel(), StandardOfficeFloorColours
				.MANAGED_OBJECT(), 20, false);
		this.officeFloorManagedObjectName = mo.getContainerName();
		objectAndMo.add(mo);

		// Add the managed object source connector
		ConnectorFigure mos = new ConnectorFigure(ConnectorDirection.SOUTH,
				StandardOfficeFloorColours.BLACK());
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

	/**
	 * Obtains the label text for the {@link OfficeFloorManagedObjectFigure}.
	 *
	 * @return Label text for the {@link OfficeFloorManagedObjectFigure}.
	 */
	private String getOfficeFloorManagedObjectLabel() {

		// Determine the scope name
		String scopeName;
		ManagedObjectScope scope = this.context.getManagedObjectScope();
		if (scope == null) {
			scopeName = "Undefined";
		} else {
			switch (scope) {
			case PROCESS:
				scopeName = "process";
				break;
			case THREAD:
				scopeName = "thread";
				break;
			case WORK:
				scopeName = "work";
				break;
			default:
				scopeName = "Unknown";
				break;
			}
		}

		// Return the label text
		return this.context.getOfficeFloorManagedObjectName() + " ["
				+ scopeName + "]";
	}

	/*
	 * ================ OfficeFloorManagedObjectFigure =====================
	 */

	@Override
	public void setOfficeFloorManagedObjectName(
			String officeFloorManagedObjectName) {
		this.officeFloorManagedObjectName.setText(this
				.getOfficeFloorManagedObjectLabel());
	}

	@Override
	public void setManagedObjectScope(ManagedObjectScope managedObjectScope) {
		this.officeFloorManagedObjectName.setText(this
				.getOfficeFloorManagedObjectLabel());
	}

	@Override
	public IFigure getOfficeFloorManagedObjectNameFigure() {
		return this.officeFloorManagedObjectName;
	}

}