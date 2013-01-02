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
import org.eclipse.draw2d.Label;

import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;

/**
 * Standard {@link SectionManagedObjectSourceFlowFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSectionManagedObjectSourceFlowFigure extends
		AbstractOfficeFloorFigure implements
		SectionManagedObjectSourceFlowFigure {

	/**
	 * Flow name.
	 */
	private final Label flowName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SectionManagedObjectSourceFlowFigureContext}.
	 */
	public StandardSectionManagedObjectSourceFlowFigure(
			SectionManagedObjectSourceFlowFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getSectionManagedObjectSourceFlowName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.flowName = figure.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				SectionManagedObjectSourceFlowToSubSectionInputModel.class,
				anchor);
		this.registerConnectionAnchor(
				SectionManagedObjectSourceFlowToExternalFlowModel.class, anchor);

		// Specify the figure
		this.setFigure(figure);
	}

	/*
	 * =================== SectionManagedObjectSourceFlowFigure ===============
	 */

	@Override
	public void setSectionManagedObjectSourceFlowName(
			String sectionManagedObjectSourceFlowName) {
		this.flowName.setText(sectionManagedObjectSourceFlowName);
	}

}