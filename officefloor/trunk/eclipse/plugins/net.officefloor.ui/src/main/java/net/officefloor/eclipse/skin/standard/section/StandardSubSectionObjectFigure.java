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

import net.officefloor.eclipse.skin.section.SubSectionObjectFigure;
import net.officefloor.eclipse.skin.section.SubSectionObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.SubSectionItemFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;

import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link SubSectionObjectFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSubSectionObjectFigure extends AbstractOfficeFloorFigure
		implements SubSectionObjectFigure {

	/**
	 * {@link SubSectionItemFigure}.
	 */
	private final SubSectionItemFigure figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SubSectionObjectFigureContext}.
	 */
	public StandardSubSectionObjectFigure(SubSectionObjectFigureContext context) {
		this.figure = new SubSectionItemFigure(
				context.getSubSectionObjectName(), false,
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		ConnectionAnchor anchor = this.figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				SubSectionObjectToExternalManagedObjectModel.class, anchor);
		this.registerConnectionAnchor(
				SubSectionObjectToSectionManagedObjectModel.class, anchor);
		this.setFigure(this.figure);
	}

	/*
	 * ==================== SubSectionObjectFigure ====================
	 */

	@Override
	public void setSubSectionObjectName(String subSectionObjectName) {
		this.figure.setItemName(subSectionObjectName);
	}

}