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
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.section.FunctionFlowFigure;
import net.officefloor.eclipse.skin.section.FunctionFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link FunctionFlowFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardTaskFlowFigure extends AbstractOfficeFloorFigure implements
		FunctionFlowFigure {

	/**
	 * {@link TaskFlowModel} name.
	 */
	private final Label taskFlowName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link FunctionFlowFigureContext}.
	 */
	public StandardTaskFlowFigure(FunctionFlowFigureContext context) {
		LabelConnectorFigure connector = new LabelConnectorFigure(
				context.getFunctionFlowName(), ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());
		this.taskFlowName = connector.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(TaskFlowToTaskModel.class, anchor);
		this.registerConnectionAnchor(TaskFlowToExternalFlowModel.class, anchor);

		this.setFigure(connector);
	}

	/*
	 * ================= TaskFlowFigure ============================
	 */

	@Override
	public void setFunctionFlowName(String taskFlowName) {
		this.taskFlowName.setText(taskFlowName);
	}

}