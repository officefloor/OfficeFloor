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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.OfficeFloorTeamFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorTeamFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeFloorTeamFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorTeamFigure extends AbstractOfficeFloorFigure
		implements OfficeFloorTeamFigure {

	/**
	 * {@link Label} containing the {@link OfficeFloorTeamModel} name.
	 */
	private Label officeFloorTeamName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link OfficeFloorTeamFigureContext}.
	 */
	public StandardOfficeFloorTeamFigure(OfficeFloorTeamFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(context
				.getOfficeFloorTeamName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());
		this.officeFloorTeamName = figure.getLabel();

		// Register connections
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				DeployedOfficeTeamToOfficeFloorTeamModel.class, anchor);
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				anchor);

		this.setFigure(figure);
	}

	/*
	 * ================== OfficeFloorTeamFigure ========================
	 */

	@Override
	public void setOfficeFloorTeamName(String officeFloorTeamName) {
		this.officeFloorTeamName.setText(officeFloorTeamName);
	}

	@Override
	public IFigure getOfficeFloorTeamNameFigure() {
		return this.officeFloorTeamName;
	}

}