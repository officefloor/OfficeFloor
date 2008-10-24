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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;

import net.officefloor.eclipse.skin.office.FlowItemFigure;
import net.officefloor.eclipse.skin.office.FlowItemFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.FlowItemToTeamModel;

/**
 * Standard {@link FlowItemFigure}.
 * 
 * @author Daniel
 */
public class StandardFlowItemFigure extends AbstractOfficeFloorFigure implements
		FlowItemFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link FlowItemFigureContext}.
	 */
	public StandardFlowItemFigure(FlowItemFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(2);
		figure.setLayoutManager(figureLayout);

		// Create the flow item and team connector
		LabelConnectorFigure flowItem = new LabelConnectorFigure(context
				.getFlowItemName(), ConnectorDirection.WEST,
				ColorConstants.black);
		ConnectionAnchor anchor = flowItem.getConnectionAnchor();
		this.registerConnectionAnchor(FlowItemToTeamModel.class, anchor);
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
