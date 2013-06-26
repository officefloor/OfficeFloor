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

import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RectangleContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.SWT;

/**
 * Standard {@link SectionManagedObjectSourceFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardSectionManagedObjectSourceFigure extends
		AbstractOfficeFloorFigure implements SectionManagedObjectSourceFigure {

	/**
	 * {@link Label} containing the {@link SectionManagedObjectSourceModel}
	 * name.
	 */
	private final Label sectionManagedObjectSourceName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link SectionManagedObjectSourceFigureContext}.
	 */
	public StandardSectionManagedObjectSourceFigure(
			SectionManagedObjectSourceFigureContext context) {

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
				SectionManagedObjectToSectionManagedObjectSourceModel.class,
				managedObjectAnchor);

		// Create the managed object source
		RectangleContainerFigure mos = new RectangleContainerFigure(context
				.getSectionManagedObjectSourceName(),
				StandardOfficeFloorColours.MANAGED_OBJECT_SOURCE(), 20, false);
		this.sectionManagedObjectSourceName = mos.getContainerName();
		figure.add(mos);

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(mos.getContentPane());
	}

	/*
	 * ================ SectionManagedObjectSourceFigure ===================
	 */

	@Override
	public void setSectionManagedObjectName(
			String sectionManagedObjectSourceName) {
		this.sectionManagedObjectSourceName
				.setText(sectionManagedObjectSourceName);
	}

	@Override
	public IFigure getSectionManagedObjectSourceNameFigure() {
		return this.sectionManagedObjectSourceName;
	}

}