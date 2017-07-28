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
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;

import net.officefloor.eclipse.skin.office.AdministrationFigure;
import net.officefloor.eclipse.skin.office.AdministrationFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
import net.officefloor.model.office.OfficeManagedObjectToAdministrationModel;

/**
 * Standard {@link AdministrationFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardAdministratorFigure extends AbstractOfficeFloorFigure implements AdministrationFigure {

	/**
	 * {@link Label} containing the {@link AdministrationModel} name.
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

		// Managed Object / Function connector
		ConnectorFigure moOrFunction = new ConnectorFigure(ConnectorDirection.WEST, StandardOfficeFloorColours.BLACK());
		moOrFunction.setBorder(new MarginBorder(10, 0, 0, 0));
		figure.add(moOrFunction);
		layout.setConstraint(moOrFunction, new GridData(0, SWT.BEGINNING, false, false));

		// Register the anchor to external and office managed objects
		ConnectionAnchor moOrFunctionAnchor = moOrFunction.getConnectionAnchor();
		this.registerConnectionAnchor(ExternalManagedObjectToAdministrationModel.class, moOrFunctionAnchor);
		this.registerConnectionAnchor(OfficeManagedObjectToAdministrationModel.class, moOrFunctionAnchor);

		// Register anchor to office functions
		this.registerConnectionAnchor(OfficeFunctionToPreAdministrationModel.class, moOrFunctionAnchor);
		this.registerConnectionAnchor(OfficeFunctionToPostAdministrationModel.class, moOrFunctionAnchor);

		// Create the administration container
		RoundedContainerFigure administrator = new RoundedContainerFigure(context.getAdministrationName(),
				StandardOfficeFloorColours.ADMINISTRATOR(), 20, false);
		figure.add(administrator);
		this.administratorName = administrator.getContainerName();

		// Team connector
		ConnectorFigure team = new ConnectorFigure(ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		team.setBorder(new MarginBorder(10, 0, 0, 0));
		figure.add(team);
		layout.setConstraint(team, new GridData(0, SWT.BEGINNING, false, false));

		// Register the anchor to team
		this.registerConnectionAnchor(AdministrationToOfficeTeamModel.class, team.getConnectionAnchor());

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(administrator.getContentPane());
	}

	/*
	 * ===================== AdministrationFigure =========================
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