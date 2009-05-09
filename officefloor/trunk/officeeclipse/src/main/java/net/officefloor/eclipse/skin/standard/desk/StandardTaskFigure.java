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

import net.officefloor.eclipse.skin.desk.TaskFigure;
import net.officefloor.eclipse.skin.desk.TaskFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.TaskEscalationToTaskModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;
import net.officefloor.model.desk.TaskToNextTaskModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * {@link StandardTaskFigure} implementation.
 * 
 * @author Daniel
 */
public class StandardTaskFigure extends AbstractOfficeFloorFigure implements
		TaskFigure {

	/**
	 * Flow item {@link Figure}.
	 */
	private final ContainerFigure flowItem;

	/**
	 * Initiate.
	 */
	public StandardTaskFigure(final TaskFigureContext context) {

		Color flowColour = new Color(null, 130, 255, 150);

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(3);
		figure.setLayoutManager(layout);

		// Create the connector
		ConnectorFigure inputConnector = new ConnectorFigure(
				ConnectorDirection.WEST, ColorConstants.black);
		inputConnector.setBorder(new MarginBorder(10, 0, 0, 0));
		ConnectionAnchor inputAnchor = inputConnector.getConnectionAnchor();
		this.registerConnectionAnchor(TaskFlowToTaskModel.class, inputAnchor);
		this.registerConnectionAnchor(TaskEscalationToTaskModel.class,
				inputAnchor);
		this
				.registerConnectionAnchor(WorkToInitialTaskModel.class,
						inputAnchor);
		this.registerTargetConnectionAnchor(TaskToNextTaskModel.class,
				inputAnchor);
		figure.add(inputConnector);
		layout.setConstraint(inputConnector, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, true, false));

		// Create container of flow item and next flow connector
		Figure flowItemAndTaskLink = new Figure();
		NoSpacingGridLayout flowItemAndNextFlowLayout = new NoSpacingGridLayout(
				1);
		flowItemAndTaskLink.setLayoutManager(flowItemAndNextFlowLayout);
		figure.add(flowItemAndTaskLink);

		// Create the flow item container
		this.flowItem = new ContainerFigure(context.getTaskName(), flowColour,
				20, true);
		flowItemAndTaskLink.add(this.flowItem);

		// Initiate state of is public
		this.setIsPublic(context.isPublic());

		// Add the connector for task
		ConnectorFigure taskConnector = new ConnectorFigure(
				ConnectorDirection.SOUTH, ColorConstants.lightGray);
		taskConnector.setBorder(new MarginBorder(0, 20, 0, 0));
		this.registerConnectionAnchor(WorkTaskToTaskModel.class, taskConnector
				.getConnectionAnchor());
		flowItemAndTaskLink.add(taskConnector);

		// Add next flow connector
		ConnectorFigure nextFlow = new ConnectorFigure(ConnectorDirection.EAST,
				ColorConstants.black);
		nextFlow.setBorder(new MarginBorder(10, 0, 0, 0));
		ConnectionAnchor nextFlowAnchor = nextFlow.getConnectionAnchor();
		this.registerSourceConnectionAnchor(TaskToNextTaskModel.class,
				nextFlowAnchor);
		this.registerConnectionAnchor(TaskToNextExternalFlowModel.class,
				nextFlowAnchor);
		layout.setConstraint(nextFlow, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, true, false));
		figure.add(nextFlow);

		// Specify figures
		this.setFigure(figure);
		this.setContentPane(this.flowItem.getContentPane());
	}

	/*
	 * ================= FlowItemFigure =================================
	 */

	@Override
	public void setTaskName(String taskName) {
		this.flowItem.getContainerName().setText(taskName);
	}

	@Override
	public void setIsPublic(boolean isPublic) {
		this.flowItem.setIsPublic(isPublic);
	}

	@Override
	public IFigure getTaskNameFigure() {
		return this.flowItem.getContainerName();
	}

}