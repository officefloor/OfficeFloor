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

import net.officefloor.eclipse.skin.section.SubSectionInputFigure;
import net.officefloor.eclipse.skin.section.SubSectionInputFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.SubSectionItemFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;

/**
 * Standard {@link SubSectionInputFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSubSectionInputFigure extends AbstractOfficeFloorFigure
		implements SubSectionInputFigure {

	/**
	 * {@link Figure}.
	 */
	private final SubSectionItemFigure figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SubSectionInputFigureContext}.
	 */
	public StandardSubSectionInputFigure(SubSectionInputFigureContext context) {
		this.figure = new SubSectionItemFigure(
				context.getSubSectionInputName(), context.isPublic(),
				ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
		ConnectionAnchor anchor = this.figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				SubSectionOutputToSubSectionInputModel.class, anchor);
		this.registerConnectionAnchor(
				SectionManagedObjectSourceFlowToSubSectionInputModel.class,
				anchor);
		this.setFigure(figure);
	}

	/*
	 * =================== SubSectionInputFigure ======================
	 */

	@Override
	public void setSubSectionInputName(String subSectionInputName) {
		this.figure.setItemName(subSectionInputName);
	}

	@Override
	public void setIsPublic(boolean isPublic) {
		this.figure.setIsPublic(isPublic);
	}

}