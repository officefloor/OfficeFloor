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
package net.officefloor.eclipse.skin.standard.section;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;

import net.officefloor.eclipse.skin.section.SectionManagedObjectFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;

/**
 * {@link SectionManagedObjectFigure} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class StandardSectionManagedObjectFigure extends AbstractOfficeFloorFigure
		implements SectionManagedObjectFigure {

	/**
	 * {@link Label} containing the {@link SectionManagedObjectModel} name.
	 */
	private final Label sectionManagedObjectName;

	/**
	 * {@link SectionManagedObjectFigureContext}.
	 */
	private final SectionManagedObjectFigureContext context;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link SectionManagedObjectFigureContext}.
	 */
	public StandardSectionManagedObjectFigure(SectionManagedObjectFigureContext context) {
		this.context = context;

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(1);
		figure.setLayoutManager(figureLayout);

		// Figure to contain section object and managed object container
		Figure objectAndMo = new Figure();
		NoSpacingGridLayout objectAndMoLayout = new NoSpacingGridLayout(2);
		objectAndMo.setLayoutManager(objectAndMoLayout);
		figure.add(objectAndMo);

		// Add the section object and managed object dependency connector
		ConnectorFigure dependency = new ConnectorFigure(ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
		dependency.setBorder(new MarginBorder(10, 0, 0, 0));
		objectAndMoLayout.setConstraint(dependency, new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		objectAndMo.add(dependency);

		// Register connection to section objects and dependencies
		ConnectionAnchor dependencyAnchor = dependency.getConnectionAnchor();
		this.registerConnectionAnchor(SubSectionObjectToSectionManagedObjectModel.class, dependencyAnchor);
		this.registerConnectionAnchor(SectionManagedObjectDependencyToSectionManagedObjectModel.class,
				dependencyAnchor);

		// Create the managed object source
		RoundedContainerFigure mo = new RoundedContainerFigure(this.getSectionManagedObjectLabel(),
				StandardOfficeFloorColours.MANAGED_OBJECT(), 20, false);
		this.sectionManagedObjectName = mo.getContainerName();
		objectAndMo.add(mo);

		// Add the managed object source connector
		ConnectorFigure mos = new ConnectorFigure(ConnectorDirection.SOUTH, StandardOfficeFloorColours.LINK_LINE());
		figureLayout.setConstraint(mos, new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
		figure.add(mos);

		// Register the connections to managed object source
		this.registerConnectionAnchor(SectionManagedObjectToSectionManagedObjectSourceModel.class,
				mos.getConnectionAnchor());

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(mo.getContentPane());
	}

	/**
	 * Obtains the label text for the {@link SectionManagedObjectFigure}.
	 *
	 * @return Label text for the {@link SectionManagedObjectFigure}.
	 */
	private String getSectionManagedObjectLabel() {

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
			case FUNCTION:
				scopeName = "function";
				break;
			default:
				scopeName = "Unknown";
				break;
			}
		}

		// Return the label text
		return this.context.getSectionManagedObjectName() + " [" + scopeName + "]";
	}

	/*
	 * ================ SectionManagedObjectFigure =====================
	 */

	@Override
	public void setSectionManagedObjectName(String sectionManagedObjectName) {
		this.sectionManagedObjectName.setText(this.getSectionManagedObjectLabel());
	}

	@Override
	public void setManagedObjectScope(ManagedObjectScope managedObjectScope) {
		this.sectionManagedObjectName.setText(this.getSectionManagedObjectLabel());
	}

	@Override
	public IFigure getSectionManagedObjectNameFigure() {
		return this.sectionManagedObjectName;
	}

}