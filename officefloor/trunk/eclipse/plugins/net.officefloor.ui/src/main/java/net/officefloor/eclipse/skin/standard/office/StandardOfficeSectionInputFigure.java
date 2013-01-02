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

import net.officefloor.eclipse.skin.office.OfficeSectionInputFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * {@link OfficeSectionInputFigure} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeSectionInputFigure extends AbstractOfficeFloorFigure
		implements OfficeSectionInputFigure {

	/**
	 * {@link OfficeSectionInputModel} name.
	 */
	private final Label sectionInputName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeSectionInputFigureContext}.
	 */
	public StandardOfficeSectionInputFigure(
			OfficeSectionInputFigureContext context) {
		LabelConnectorFigure connector = new LabelConnectorFigure(
				context.getOfficeSectionInputName(), ConnectorDirection.WEST,
				StandardOfficeFloorColours.BLACK());
		this.sectionInputName = connector.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeSectionOutputToOfficeSectionInputModel.class, anchor);
		this.registerConnectionAnchor(
				OfficeManagedObjectSourceFlowToOfficeSectionInputModel.class,
				anchor);
		this.registerConnectionAnchor(
				OfficeEscalationToOfficeSectionInputModel.class, anchor);

		this.setFigure(connector);
	}

	/*
	 * =================== OfficeSectionInputFigure ======================
	 */

	@Override
	public void setOfficeSectionInputName(String officeSectionInputName) {
		this.sectionInputName.setText(officeSectionInputName);
	}

}