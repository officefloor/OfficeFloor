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

import net.officefloor.eclipse.skin.desk.DeskManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.desk.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.desk.WorkTaskObjectToDeskManagedObjectModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;

/**
 * {@link DeskManagedObjectFigure} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class StandardDeskManagedObjectFigure extends AbstractOfficeFloorFigure
		implements DeskManagedObjectFigure {

	/**
	 * {@link Label} containing the {@link DeskManagedObjectModel} name.
	 */
	private final Label deskManagedObjectName;

	/**
	 * {@link DeskManagedObjectFigureContext}.
	 */
	private final DeskManagedObjectFigureContext context;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link DeskManagedObjectFigureContext}.
	 */
	public StandardDeskManagedObjectFigure(
			DeskManagedObjectFigureContext context) {
		this.context = context;

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(1);
		figure.setLayoutManager(figureLayout);

		// Figure to contain work task object and managed object container
		Figure objectAndMo = new Figure();
		NoSpacingGridLayout objectAndMoLayout = new NoSpacingGridLayout(2);
		objectAndMo.setLayoutManager(objectAndMoLayout);
		figure.add(objectAndMo);

		// Add the work task object and managed object dependency connector
		ConnectorFigure dependency = new ConnectorFigure(
				ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
		dependency.setBorder(new MarginBorder(10, 0, 0, 0));
		objectAndMoLayout.setConstraint(dependency, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));
		objectAndMo.add(dependency);

		// Register connection to work task objects and dependencies
		ConnectionAnchor dependencyAnchor = dependency.getConnectionAnchor();
		this.registerConnectionAnchor(
				WorkTaskObjectToDeskManagedObjectModel.class, dependencyAnchor);
		this.registerConnectionAnchor(
				DeskManagedObjectDependencyToDeskManagedObjectModel.class,
				dependencyAnchor);

		// Create the managed object source
		RoundedContainerFigure mo = new RoundedContainerFigure(this
				.getDeskManagedObjectLabel(), StandardOfficeFloorColours
				.MANAGED_OBJECT(), 20, false);
		this.deskManagedObjectName = mo.getContainerName();
		objectAndMo.add(mo);

		// Add the managed object source connector
		ConnectorFigure mos = new ConnectorFigure(ConnectorDirection.SOUTH,
				StandardOfficeFloorColours.LINK_LINE());
		figureLayout.setConstraint(mos, new GridData(SWT.CENTER, SWT.BEGINNING,
				true, false));
		figure.add(mos);

		// Register the connections to managed object source
		this.registerConnectionAnchor(
				DeskManagedObjectToDeskManagedObjectSourceModel.class, mos
						.getConnectionAnchor());

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(mo.getContentPane());
	}

	/**
	 * Obtains the label text for the {@link DeskManagedObjectFigure}.
	 *
	 * @return Label text for the {@link DeskManagedObjectFigure}.
	 */
	private String getDeskManagedObjectLabel() {

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
		return this.context.getDeskManagedObjectName() + " [" + scopeName + "]";
	}

	/*
	 * ================ DeskManagedObjectFigure =====================
	 */

	@Override
	public void setDeskManagedObjectName(String DeskManagedObjectName) {
		this.deskManagedObjectName.setText(this.getDeskManagedObjectLabel());
	}

	@Override
	public void setManagedObjectScope(ManagedObjectScope managedObjectScope) {
		this.deskManagedObjectName.setText(this.getDeskManagedObjectLabel());
	}

	@Override
	public IFigure getDeskManagedObjectNameFigure() {
		return this.deskManagedObjectName;
	}

}