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

import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * {@link OfficeSectionResponsibilityFigure} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeSectionResponsibilityFigure extends
		AbstractOfficeFloorFigure implements OfficeSectionResponsibilityFigure {

	/**
	 * {@link Label} containing the {@link OfficeSectionResponsibilityModel}
	 * name.
	 */
	private final Label officeSectionResponsibilityName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link OfficeSectionResponsibilityFigureContext}.
	 */
	public StandardOfficeSectionResponsibilityFigure(
			OfficeSectionResponsibilityFigureContext context) {

		Figure container = new Figure();
		container.setLayoutManager(new NoSpacingGridLayout(2));

		// Create connector for managed objects
		ConnectorFigure moConnector = new ConnectorFigure(
				ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
		container.add(moConnector);

		// Create label and connector for teams
		LabelConnectorFigure teamConnector = new LabelConnectorFigure(context
				.getOfficeSectionResponsibilityName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());
		container.add(teamConnector);
		this.officeSectionResponsibilityName = teamConnector.getLabel();

		// Register the anchors
		ConnectionAnchor teamAnchor = teamConnector.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeSectionResponsibilityToOfficeTeamModel.class, teamAnchor);

		this.setFigure(container);
	}

	/*
	 * ================== OfficeSectionResponsibilityFigure ==================
	 */

	@Override
	public void setOfficeSectionResponsibilityName(
			String officeSectionResponsibilityName) {
		this.officeSectionResponsibilityName
				.setText(officeSectionResponsibilityName);
	}

	@Override
	public IFigure getOfficeSectionResponsibilityNameFigure() {
		return this.officeSectionResponsibilityName;
	}

}