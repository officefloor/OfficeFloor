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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeFloorManagedObjectSourceFlowFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorManagedObjectSourceFlowFigure extends
		AbstractOfficeFloorFigure implements
		OfficeFloorManagedObjectSourceFlowFigure {

	/**
	 * {@link OfficeFloorManagedObjectSourceFlowFigureContext}.
	 */
	private final OfficeFloorManagedObjectSourceFlowFigureContext context;

	/**
	 * {@link ConnectorFigure}.
	 */
	private final ConnectorFigure connector;

	/**
	 * Name of the {@link ManagedObjectTaskFlowModel}.
	 */
	private final Label flowName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeFloorManagedObjectSourceFlowFigureContext}.
	 */
	public StandardOfficeFloorManagedObjectSourceFlowFigure(
			OfficeFloorManagedObjectSourceFlowFigureContext context) {
		this.context = context;

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(2);
		figureLayout.horizontalSpacing = 2;
		figure.setLayoutManager(figureLayout);

		// Create the connector
		this.connector = new ConnectorFigure(ConnectorDirection.WEST,
				StandardOfficeFloorColours.BLACK());
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class,
				this.connector.getConnectionAnchor());
		figure.add(this.connector);

		// Create the flow name
		this.flowName = new Label();
		this.flowName.setLayoutManager(new NoSpacingToolbarLayout(true));
		this.flowName.setForegroundColor(StandardOfficeFloorColours.BLACK());
		figure.add(this.flowName);

		// Initiate connection state
		this.connectionChanged();

		// Specify the figure
		this.setFigure(figure);
	}

	/**
	 * Flags that the connection has changed.
	 */
	public void connectionChanged() {

		// Obtain the flow name
		String flowName = this.context
				.getOfficeFloorManagedObjectSourceFlowName();

		// Obtains the task name
		String taskName = context.getInitialTaskName();
		if ((taskName == null) || (taskName.length() == 0)) {
			// No managed object task connected
			taskName = null;
		} else {
			// Linked to a task by managed object source
			taskName = " (" + context.getInitialWorkName() + "." + taskName
					+ ")";
		}

		// Specify state based on whether connected to task
		if (taskName == null) {
			// To be linked to office task
			this.connector.setVisible(true);
			this.flowName.setText(flowName);
		} else {
			// Linked to managed object task
			this.connector.setVisible(false);
			this.flowName.setText(flowName + taskName);
		}
	}

	/*
	 * ================== OfficeFloorManagedObjectSourceFlowFigure ============
	 */

	@Override
	public void setOfficeFloorManagedObjectSourceFlowName(
			String officeFloorManagedObjectSourceFlowName) {
		this.connectionChanged();
	}

}
