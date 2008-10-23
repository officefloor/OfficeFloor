/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.AdministratorFigure;
import net.officefloor.eclipse.skin.office.AdministratorFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.AdministratorToManagedObjectModel;
import net.officefloor.model.office.AdministratorToTeamModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Standard {@link AdministratorFigure}.
 * 
 * @author Daniel
 */
public class StandardAdministratorFigure extends AbstractOfficeFloorFigure
		implements AdministratorFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link AdministratorFigureContext}.
	 */
	public StandardAdministratorFigure(AdministratorFigureContext context) {

		Color administratorColour = new Color(null, 100, 255, 200);

		// Figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(3);
		figure.setLayoutManager(layout);

		// Team connector
		ConnectorFigure team = new ConnectorFigure(ConnectorDirection.WEST,
				ColorConstants.black);
		team.setBorder(new MarginBorder(10, 0, 0, 0));
		this.registerConnectionAnchor(AdministratorToTeamModel.class, team
				.getConnectionAnchor());
		figure.add(team);
		layout
				.setConstraint(team, new GridData(0, SWT.BEGINNING, false,
						false));

		// Create the administrator container
		ContainerFigure administrator = new ContainerFigure(context
				.getAdministratorName(), administratorColour, 20, false);
		figure.add(administrator);

		// Managed Object connector
		ConnectorFigure mo = new ConnectorFigure(ConnectorDirection.EAST,
				ColorConstants.black);
		mo.setBorder(new MarginBorder(10, 0, 0, 0));
		this.registerConnectionAnchor(AdministratorToManagedObjectModel.class,
				mo.getConnectionAnchor());
		figure.add(mo);
		layout.setConstraint(mo, new GridData(0, SWT.BEGINNING, false, false));

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(administrator.getContentPane());
	}

}
