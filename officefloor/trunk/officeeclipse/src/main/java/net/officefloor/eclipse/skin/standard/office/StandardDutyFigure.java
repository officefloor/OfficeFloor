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

import net.officefloor.eclipse.skin.office.DutyFigure;
import net.officefloor.eclipse.skin.office.DutyFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.OfficeTaskToPostDutyModel;
import net.officefloor.model.office.OfficeTaskToPreDutyModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;

/**
 * Standard {@link DutyFigure}.
 * 
 * @author Daniel
 */
public class StandardDutyFigure extends AbstractOfficeFloorFigure implements
		DutyFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DutyFigureContext}.
	 */
	public StandardDutyFigure(DutyFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		figure.setLayoutManager(new NoSpacingGridLayout(1));

		// Create the duty figure
		LabelConnectorFigure duty = new LabelConnectorFigure(context
				.getDutyName(), ConnectorDirection.WEST, ColorConstants.black);
		figure.add(duty);

		// Register anchor to office tasks
		ConnectionAnchor anchor = duty.getConnectionAnchor();
		this.registerConnectionAnchor(OfficeTaskToPreDutyModel.class, anchor);
		this.registerConnectionAnchor(OfficeTaskToPostDutyModel.class, anchor);

		// Create the content pane
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		figure.add(contentPane);

		// Specify figure and content pane
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

}