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

import net.officefloor.eclipse.skin.desk.DeskFigureFactory;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigure;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.TaskEscalationFigure;
import net.officefloor.eclipse.skin.desk.TaskEscalationFigureContext;
import net.officefloor.eclipse.skin.desk.TaskEscalationToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.TaskEscalationToTaskFigureContext;
import net.officefloor.eclipse.skin.desk.TaskFigure;
import net.officefloor.eclipse.skin.desk.TaskFigureContext;
import net.officefloor.eclipse.skin.desk.TaskFlowFigure;
import net.officefloor.eclipse.skin.desk.TaskFlowFigureContext;
import net.officefloor.eclipse.skin.desk.TaskFlowToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.TaskFlowToTaskFigureContext;
import net.officefloor.eclipse.skin.desk.TaskToNextExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.TaskToNextTaskFigureContext;
import net.officefloor.eclipse.skin.desk.WorkFigure;
import net.officefloor.eclipse.skin.desk.WorkFigureContext;
import net.officefloor.eclipse.skin.desk.WorkTaskFigure;
import net.officefloor.eclipse.skin.desk.WorkTaskFigureContext;
import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigure;
import net.officefloor.eclipse.skin.desk.WorkTaskObjectFigureContext;
import net.officefloor.eclipse.skin.desk.WorkTaskObjectToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.WorkTaskToTaskFigureContext;
import net.officefloor.eclipse.skin.desk.WorkToInitialTaskFigureContext;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

/**
 * Standard {@link DeskFigureFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardDeskFigureFactory implements DeskFigureFactory {

	/**
	 * Decorates the {@link Figure} based on the
	 * {@link FlowInstigationStrategyEnum}.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}. May be <code>null</code>.
	 */
	private void decorateInstigationStrategy(PolylineConnection figure,
			FlowInstigationStrategyEnum instigationStrategy) {

		// Ensure have flow instigation strategy
		if (instigationStrategy == null) {
			figure.setForegroundColor(ColorConstants.red);
			return;
		}

		// Decorate based on flow instigation strategy
		switch (instigationStrategy) {
		case SEQUENTIAL:
			PolylineDecoration sequentialArrow = new PolylineDecoration();
			sequentialArrow.setLineWidth(2);
			figure.setTargetDecoration(sequentialArrow);
			figure.setLineWidth(2);
			break;
		case PARALLEL:
			PolylineDecoration parallelSourceArrow = new PolylineDecoration();
			parallelSourceArrow.setLineWidth(2);
			figure.setSourceDecoration(parallelSourceArrow);
			PolylineDecoration parallelTargetArrow = new PolylineDecoration();
			parallelTargetArrow.setLineWidth(2);
			figure.setTargetDecoration(parallelTargetArrow);
			figure.setLineWidth(2);
			break;
		case ASYNCHRONOUS:
			figure.setTargetDecoration(new PolylineDecoration());
			figure.setLineStyle(Graphics.LINE_DASH);
			break;
		default:
			throw new IllegalStateException("Unknown instigation strategy "
					+ instigationStrategy);
		}
	}

	/*
	 * ===================== DeskFigureFactory ============================
	 */

	@Override
	public WorkFigure createWorkFigure(WorkFigureContext context) {
		return new StandardWorkFigure(context);
	}

	@Override
	public WorkTaskFigure createWorkTaskFigure(
			final WorkTaskFigureContext context) {
		return new StandardWorkTaskFigure(context);
	}

	@Override
	public WorkTaskObjectFigure createWorkTaskObjectFigure(
			final WorkTaskObjectFigureContext context) {
		return new StandardWorkTaskObjectFigure(context);
	}

	@Override
	public ExternalFlowFigure createExternalFlowFigure(
			ExternalFlowFigureContext context) {
		return new StandardExternalFlowFigure(context);
	}

	@Override
	public ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
		return new StandardExternalManagedObjectFigure(context);
	}

	@Override
	public TaskFigure createTaskFigure(final TaskFigureContext context) {
		return new StandardTaskFigure(context);
	}

	@Override
	public TaskEscalationFigure createTaskEscalationFigure(
			TaskEscalationFigureContext context) {
		return new StandardTaskEscalationFigure(context);
	}

	@Override
	public TaskFlowFigure createTaskFlowFigure(TaskFlowFigureContext context) {
		return new StandardTaskFlowFigure(context);
	}

	@Override
	public void decorateWorkTaskToTaskFigure(PolylineConnection figure,
			WorkTaskToTaskFigureContext context) {
		figure.setForegroundColor(ColorConstants.lightGray);
	}

	@Override
	public void decorateWorkTaskObjectToExternalManagedObjectFigure(
			PolylineConnection figure,
			WorkTaskObjectToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateTaskFlowToTaskFigure(PolylineConnection figure,
			TaskFlowToTaskFigureContext context) {
		this.decorateInstigationStrategy(figure, context
				.getFlowInstigationStrategy());
	}

	@Override
	public void decorateTaskFlowToExternalFlowFigure(PolylineConnection figure,
			TaskFlowToExternalFlowFigureContext context) {
		this.decorateInstigationStrategy(figure, context
				.getFlowInstigationStrategy());
	}

	@Override
	public void decorateTaskToNextTaskFigure(PolylineConnection figure,
			TaskToNextTaskFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateTaskToNextExternalFlowFigure(PolylineConnection figure,
			TaskToNextExternalFlowFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateTaskEscalationToTaskFigure(PolylineConnection figure,
			TaskEscalationToTaskFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateTaskEscalationToExternalFlowFigure(
			PolylineConnection figure,
			TaskEscalationToExternalFlowFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateWorkToInitialTaskFigure(PolylineConnection figure,
			WorkToInitialTaskFigureContext context) {
		figure.setForegroundColor(ColorConstants.lightBlue);
	}

}