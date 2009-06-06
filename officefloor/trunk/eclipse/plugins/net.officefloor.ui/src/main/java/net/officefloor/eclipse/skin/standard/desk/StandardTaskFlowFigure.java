/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.TaskFlowFigure;
import net.officefloor.eclipse.skin.desk.TaskFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;

import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link TaskFlowFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardTaskFlowFigure extends AbstractOfficeFloorFigure implements
		TaskFlowFigure {

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link TaskFlowFigureContext}.
	 */
	public StandardTaskFlowFigure(TaskFlowFigureContext context) {
		LabelConnectorFigure connector = new LabelConnectorFigure(context
				.getTaskFlowName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(TaskFlowToTaskModel.class, anchor);
		this
				.registerConnectionAnchor(TaskFlowToExternalFlowModel.class,
						anchor);

		this.setFigure(connector);
	}
}
