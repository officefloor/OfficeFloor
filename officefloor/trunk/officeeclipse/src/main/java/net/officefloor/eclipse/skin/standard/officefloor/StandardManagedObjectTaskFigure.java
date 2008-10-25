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
package net.officefloor.eclipse.skin.standard.officefloor;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;

import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;

/**
 * Standard {@link ManagedObjectTaskFigure}.
 * 
 * @author Daniel
 */
public class StandardManagedObjectTaskFigure extends AbstractOfficeFloorFigure
		implements ManagedObjectTaskFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ManagedObjectTaskFigureContext}.
	 */
	public StandardManagedObjectTaskFigure(
			ManagedObjectTaskFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		figure.setLayoutManager(new NoSpacingGridLayout(1));

		// Determine the task name
		String taskName = context.getWorkName() + "." + context.getTaskName();

		// Obtain the team name
		String teamName = context.getTeamName();
		if ((teamName == null) || (teamName.length() == 0)) {
			// Ensure no team name
			teamName = "";
		} else {
			// Specify the team name
			teamName = "  (" + teamName + ")";
		}

		// Create the task name
		Label taskLabel = new Label(taskName + teamName);
		taskLabel.setLayoutManager(new NoSpacingToolbarLayout(true));
		figure.add(taskLabel);

		// Add the content pane
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPane.setBorder(new MarginBorder(0, 20, 0, 0));
		figure.add(contentPane);

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}
}
