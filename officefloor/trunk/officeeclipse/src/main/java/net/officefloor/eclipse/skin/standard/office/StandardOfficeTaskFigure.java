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

import net.officefloor.eclipse.skin.office.OfficeTaskFigure;
import net.officefloor.eclipse.skin.office.OfficeTaskFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;

/**
 * Standard {@link OfficeTaskFigure}.
 * 
 * @author Daniel
 */
public class StandardOfficeTaskFigure extends AbstractOfficeFloorFigure implements
		OfficeTaskFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeTaskFigureContext}.
	 */
	public StandardOfficeTaskFigure(OfficeTaskFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(2);
		figure.setLayoutManager(figureLayout);

		// Create the flow item and team connector
		LabelConnectorFigure flowItem = new LabelConnectorFigure(context
				.getOfficeTaskName(), ConnectorDirection.WEST,
				ColorConstants.black);
		ConnectionAnchor anchor = flowItem.getConnectionAnchor();
//		this.registerConnectionAnchor(FlowItemToTeamModel.class, anchor);
		figure.add(flowItem);

		// Create the container for child connectors
		Figure contentPane = new Figure();
		NoSpacingGridLayout contentPaneLayout = new NoSpacingGridLayout(1);
		contentPaneLayout.verticalSpacing = 2;
		contentPane.setLayoutManager(contentPaneLayout);
		figure.add(contentPane);

		// Specify the figure
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}
}