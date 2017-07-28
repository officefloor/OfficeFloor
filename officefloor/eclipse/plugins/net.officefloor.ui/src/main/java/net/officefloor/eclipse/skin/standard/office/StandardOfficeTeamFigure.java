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

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

import net.officefloor.eclipse.skin.office.OfficeTeamFigure;
import net.officefloor.eclipse.skin.office.OfficeTeamFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.OfficeFunctionToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeTeamModel;

/**
 * Standard {@link OfficeTeamFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeTeamFigure extends AbstractOfficeFloorFigure implements OfficeTeamFigure {

	/**
	 * {@link Label} containing the {@link OfficeTeamModel} name.
	 */
	private final Label officeTeamName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link OfficeTeamFigureContext}.
	 */
	public StandardOfficeTeamFigure(OfficeTeamFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(context.getOfficeTeamName(), ConnectorDirection.WEST,
				StandardOfficeFloorColours.BLACK());
		this.officeTeamName = figure.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(OfficeFunctionToOfficeTeamModel.class, anchor);
		this.registerConnectionAnchor(AdministrationToOfficeTeamModel.class, anchor);
		this.registerConnectionAnchor(OfficeManagedObjectSourceTeamToOfficeTeamModel.class, anchor);

		this.setFigure(figure);
	}

	/*
	 * ================== OfficeTeamFigure ===================================
	 */

	@Override
	public void setOfficeTeamName(String officeTeamName) {
		this.officeTeamName.setText(officeTeamName);
	}

	@Override
	public IFigure getOfficeTeamNameFigure() {
		return this.officeTeamName;
	}

}