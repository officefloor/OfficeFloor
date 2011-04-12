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
import net.officefloor.eclipse.skin.woof.TemplateOutputFigure;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigureContext;
import net.officefloor.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofTemplateModel;

import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link TemplateOutputFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardTemplateOutputFigure extends AbstractOfficeFloorFigure
		implements TemplateOutputFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link TemplateOutputFigureContext}.
	 */
	public StandardTemplateOutputFigure(TemplateOutputFigureContext context) {

		// Create figure
		LabelConnectorFigure connector = new LabelConnectorFigure(
				context.getTemplateOutputName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(
				WoofTemplateOutputToWoofTemplateModel.class, anchor);
		this.registerConnectionAnchor(
				WoofTemplateOutputToWoofSectionInputModel.class, anchor);
		this.registerConnectionAnchor(
				WoofTemplateOutputToWoofResourceModel.class, anchor);

		this.setFigure(connector);
	}

}