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

import net.officefloor.eclipse.skin.officefloor.ManagedObjectHandlerLinkProcessFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectHandlerLinkProcessFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.LinkProcessToOfficeTaskModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link ManagedObjectHandlerLinkProcessFigure}.
 * 
 * @author Daniel
 */
public class StandardManagedObjectHandlerLinkProcessFigure extends
		AbstractOfficeFloorFigure implements
		ManagedObjectHandlerLinkProcessFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ManagedObjectHandlerLinkProcessFigureContext}.
	 */
	public StandardManagedObjectHandlerLinkProcessFigure(
			ManagedObjectHandlerLinkProcessFigureContext context) {

		// Determine if linked by managed object source to task
		String linkTask = context.getTaskName();
		if ((linkTask != null) && (linkTask.length() > 0)) {
			// Linked to a task by managed object source
			linkTask = " (" + context.getWorkName() + "." + linkTask + ")";
		} else {
			// Not linked
			linkTask = null;
		}

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(2);
		figureLayout.horizontalSpacing = 2;
		figure.setLayoutManager(figureLayout);

		// Create the connector
		ConnectorFigure connector = new ConnectorFigure(
				ConnectorDirection.WEST, ColorConstants.black);
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(LinkProcessToOfficeTaskModel.class,
				anchor);
		figure.add(connector);

		// Specify state of connector
		connector.setVisible(linkTask == null);

		// Provide name of link process
		Label name = new Label(context.getLinkProcessName()
				+ (linkTask == null ? "" : linkTask));
		name.setLayoutManager(new NoSpacingToolbarLayout(true));
		figure.add(name);

		// Specify figure
		this.setFigure(figure);
	}
}
