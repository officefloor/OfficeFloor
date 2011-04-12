/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.woof.SectionOutputFigure;
import net.officefloor.eclipse.skin.woof.SectionOutputFigureContext;
import net.officefloor.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofTemplateModel;

import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link SectionOutputFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSectionOutputFigure extends AbstractOfficeFloorFigure
		implements SectionOutputFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SectionOutputFigureContext}.
	 */
	public StandardSectionOutputFigure(SectionOutputFigureContext context) {

		// Create figure
		LabelConnectorFigure connector = new LabelConnectorFigure(
				context.getSectionOutputName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(
				WoofSectionOutputToWoofTemplateModel.class, anchor);
		this.registerConnectionAnchor(
				WoofSectionOutputToWoofSectionInputModel.class, anchor);
		this.registerConnectionAnchor(
				WoofSectionOutputToWoofResourceModel.class, anchor);

		this.setFigure(connector);
	}

}