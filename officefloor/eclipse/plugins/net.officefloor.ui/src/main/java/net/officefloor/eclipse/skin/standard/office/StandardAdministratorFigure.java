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

import net.officefloor.eclipse.skin.office.AdministrationFigure;
import net.officefloor.eclipse.skin.office.AdministrationFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
import net.officefloor.model.office.OfficeManagedObjectToAdministratorModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;

/**
 * Standard {@link AdministrationFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardAdministratorFigure extends AbstractOfficeFloorFigure
		implements AdministrationFigure {

	/**
	 * {@link Label} containing the {@link AdministratorModel} name.
	 */
	private final Label administratorName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link AdministrationFigureContext}.
	 */
	public StandardAdministratorFigure(AdministrationFigureContext context) {

		// Figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(3);
		figure.setLayoutManager(layout);

		// Managed Object connector
		ConnectorFigure mo = new ConnectorFigure(ConnectorDirection.WEST,
				StandardOfficeFloorColours.BLACK());
		mo.setBorder(new MarginBorder(10, 0, 0, 0));
		figure.add(mo);
		layout.setConstraint(mo, new GridData(0, SWT.BEGINNING, false, false));

		// Register the anchor to external and office managed objects
		ConnectionAnchor moAnchor = mo.getConnectionAnchor();
		this.registerConnectionAnchor(
				ExternalManagedObjectToAdministratorModel.class, moAnchor);
		this.registerConnectionAnchor(
				OfficeManagedObjectToAdministratorModel.class, moAnchor);

		// Create the administrator container
		RoundedContainerFigure administrator = new RoundedContainerFigure(
				context.getAdministrationName(), StandardOfficeFloorColours
						.ADMINISTRATOR(), 20, false);
		figure.add(administrator);
		this.administratorName = administrator.getContainerName();

		// Team connector
		ConnectorFigure team = new ConnectorFigure(ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());
		team.setBorder(new MarginBorder(10, 0, 0, 0));
		figure.add(team);
		layout
				.setConstraint(team, new GridData(0, SWT.BEGINNING, false,
						false));

		// Register the anchor to team
		this.registerConnectionAnchor(AdministratorToOfficeTeamModel.class,
				team.getConnectionAnchor());

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(administrator.getContentPane());
	}

	/*
	 * ===================== AdministratorFigure =========================
	 */

	@Override
	public void setAdministrationName(String administratorName) {
		this.administratorName.setText(administratorName);
	}

	@Override
	public IFigure getAdministrationNameFigure() {
		return this.administratorName;
	}

}