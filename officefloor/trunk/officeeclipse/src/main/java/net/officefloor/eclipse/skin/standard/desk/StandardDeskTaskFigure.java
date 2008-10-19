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
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.DeskTaskFigure;
import net.officefloor.eclipse.skin.desk.DeskTaskFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Insets;

/**
 * Standard {@link DeskTaskFigure}.
 * 
 * @author Daniel
 */
public class StandardDeskTaskFigure extends AbstractOfficeFloorFigure implements
		DeskTaskFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DeskTaskFigureContext}.
	 */
	public StandardDeskTaskFigure(DeskTaskFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		figure.setLayoutManager(new NoSpacingToolbarLayout(false));

		// Task name
		LabelConnectorFigure nameFigure = new LabelConnectorFigure(context
				.getTaskName(), ConnectorDirection.EAST, ColorConstants.black);
		this.registerConnectionAnchor(DeskTaskToFlowItemModel.class, nameFigure
				.getConnectionAnchor());
		figure.add(nameFigure);

		// Content Pane
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPane.setBorder(new MarginBorder(new Insets(0, 20, 0, 0)));
		figure.add(contentPane);

		// Specify figures
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}
}
