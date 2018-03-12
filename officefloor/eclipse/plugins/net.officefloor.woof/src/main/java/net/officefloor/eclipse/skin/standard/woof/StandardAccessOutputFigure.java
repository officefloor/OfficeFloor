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
package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.woof.SecurityOutputFigure;
import net.officefloor.eclipse.skin.woof.SecurityOutputFigureContext;
import net.officefloor.model.woof.WoofAccessOutputToWoofResourceModel;
import net.officefloor.model.woof.WoofAccessOutputToWoofSectionInputModel;
import net.officefloor.model.woof.WoofAccessOutputToWoofTemplateModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link SecurityOutputFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardAccessOutputFigure extends AbstractOfficeFloorFigure
		implements SecurityOutputFigure {

	/**
	 * Name.
	 */
	private final Label name;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SecurityOutputFigureContext}.
	 */
	public StandardAccessOutputFigure(SecurityOutputFigureContext context) {

		// Create figure
		LabelConnectorFigure connector = new LabelConnectorFigure(
				context.getSecurityOutputName(), ConnectorDirection.EAST,
				CommonWoofColours.CONNECTIONS());
		this.name = connector.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(
				WoofAccessOutputToWoofTemplateModel.class, anchor);
		this.registerConnectionAnchor(
				WoofAccessOutputToWoofSectionInputModel.class, anchor);
		this.registerConnectionAnchor(
				WoofAccessOutputToWoofResourceModel.class, anchor);

		this.setFigure(connector);
	}

	/*
	 * ====================== AccessOutputFigure =============================
	 */

	@Override
	public void setAccessOutputName(String accessOutputName) {
		this.name.setText(accessOutputName);
	}

}