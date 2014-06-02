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

import net.officefloor.eclipse.skin.section.SectionManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link SectionManagedObjectDependencyFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSectionManagedObjectDependencyFigure extends
		AbstractOfficeFloorFigure implements
		SectionManagedObjectDependencyFigure {

	/**
	 * {@link SectionManagedObjectDependencyModel} name.
	 */
	private final Label dependencyName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SectionManagedObjectDependencyFigureContext}.
	 */
	public StandardSectionManagedObjectDependencyFigure(
			SectionManagedObjectDependencyFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getSectionManagedObjectDependencyName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.dependencyName = figure.getLabel();

		// Register connections
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				SectionManagedObjectDependencyToSectionManagedObjectModel.class,
				anchor);
		this.registerConnectionAnchor(
				SectionManagedObjectDependencyToExternalManagedObjectModel.class,
				anchor);

		this.setFigure(figure);
	}

	/*
	 * ==================== SectionManagedObjectDependencyFigure ===============
	 */

	@Override
	public void setSectionManagedObjectDependencyName(
			String sectionManagedObjectDependencyName) {
		this.dependencyName.setText(sectionManagedObjectDependencyName);
	}

}