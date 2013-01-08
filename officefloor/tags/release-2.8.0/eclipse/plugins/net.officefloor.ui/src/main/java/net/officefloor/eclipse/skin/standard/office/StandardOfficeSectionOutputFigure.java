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
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.OfficeSectionOutputFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * {@link OfficeSectionOutputFigure} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeSectionOutputFigure extends
		AbstractOfficeFloorFigure implements OfficeSectionOutputFigure {

	/**
	 * {@link OfficeSectionOutputModel} name.
	 */
	private final Label sectionOutputName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeSectionOutputFigureContext}.
	 */
	public StandardOfficeSectionOutputFigure(
			OfficeSectionOutputFigureContext context) {
		LabelConnectorFigure connector = new LabelConnectorFigure(
				context.getOfficeSectionOutputName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());
		this.sectionOutputName = connector.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeSectionOutputToOfficeSectionInputModel.class, anchor);

		this.setFigure(connector);
	}

	/*
	 * ==================== OfficeSectionOutputFigure ==================
	 */

	@Override
	public void setOfficeSectionOutputName(String officeSectionOutputName) {
		this.sectionOutputName.setText(officeSectionOutputName);
	}

}