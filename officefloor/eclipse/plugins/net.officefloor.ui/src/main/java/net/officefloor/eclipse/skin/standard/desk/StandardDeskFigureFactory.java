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

import net.officefloor.eclipse.skin.desk.DeskFigureFactory;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectDependencyToDeskManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectDependencyToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFlowFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFlowToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectToDeskManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigure;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.desk.WorkToInitialTaskFigureContext;
import net.officefloor.eclipse.skin.section.FunctionEscalationFigure;
import net.officefloor.eclipse.skin.section.FunctionEscalationFigureContext;
import net.officefloor.eclipse.skin.section.FunctionEscalationToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.FunctionEscalationToFunctionFigureContext;
import net.officefloor.eclipse.skin.section.FunctionFigure;
import net.officefloor.eclipse.skin.section.FunctionFigureContext;
import net.officefloor.eclipse.skin.section.FunctionFlowFigure;
import net.officefloor.eclipse.skin.section.FunctionFlowFigureContext;
import net.officefloor.eclipse.skin.section.FunctionFlowToExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.FunctionFlowToFunctionFigureContext;
import net.officefloor.eclipse.skin.section.FunctionNamespaceFigure;
import net.officefloor.eclipse.skin.section.FunctionNamespaceFigureContext;
import net.officefloor.eclipse.skin.section.FunctionToNextExternalFlowFigureContext;
import net.officefloor.eclipse.skin.section.FunctionToNextFunctionFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionFigure;
import net.officefloor.eclipse.skin.section.ManagedFunctionFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectFigure;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectToExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionObjectToSectionManagedObjectFigureContext;
import net.officefloor.eclipse.skin.section.ManagedFunctionToFunctionFigureContext;
import net.officefloor.eclipse.skin.section.SectionManagedObjectSourceFlowToFunctionFigureContext;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

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
			figure.setForegroundColor(StandardOfficeFloorColours.ERROR());
			return;
		}

		// Decorate based on flow instigation strategy
		switch (instigationStrategy) {
		case SEQUENTIAL:
			figure.setTargetDecoration(new PolylineDecoration());
			break;
		case PARALLEL:
			figure.setSourceDecoration(new PolylineDecoration());
			figure.setTargetDecoration(new PolylineDecoration());
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
	public FunctionNamespaceFigure createWorkFigure(FunctionNamespaceFigureContext context) {
		return new StandardWorkFigure(context);
	}

	@Override
	public ManagedFunctionFigure createWorkTaskFigure(
			final ManagedFunctionFigureContext context) {
		return new StandardWorkTaskFigure(context);
	}

	@Override
	public ManagedFunctionObjectFigure createWorkTaskObjectFigure(
			final ManagedFunctionObjectFigureContext context) {
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
	public FunctionFigure createTaskFigure(final FunctionFigureContext context) {
		return new StandardTaskFigure(context);
	}

	@Override
	public FunctionEscalationFigure createTaskEscalationFigure(
			FunctionEscalationFigureContext context) {
		return new StandardTaskEscalationFigure(context);
	}

	@Override
	public FunctionFlowFigure createTaskFlowFigure(FunctionFlowFigureContext context) {
		return new StandardTaskFlowFigure(context);
	}

	@Override
	public DeskManagedObjectSourceFigure createDeskManagedObjectSourceFigure(
			DeskManagedObjectSourceFigureContext context) {
		return new StandardDeskManagedObjectSourceFigure(context);
	}

	@Override
	public DeskManagedObjectSourceFlowFigure createDeskManagedObjectSourceFlowFigure(
			DeskManagedObjectSourceFlowFigureContext context) {
		return new StandardDeskManagedObjectSourceFlowFigure(context);
	}

	@Override
	public DeskManagedObjectFigure createDeskManagedObjectFigure(
			DeskManagedObjectFigureContext context) {
		return new StandardDeskManagedObjectFigure(context);
	}

	@Override
	public DeskManagedObjectDependencyFigure createDeskManagedObjectDependencyFigure(
			DeskManagedObjectDependencyFigureContext context) {
		return new StandardDeskManagedObjectDependencyFigure(context);
	}

	@Override
	public void decorateWorkTaskToTaskFigure(PolylineConnection figure,
			ManagedFunctionToFunctionFigureContext context) {
		figure.setForegroundColor(StandardOfficeFloorColours.LINK_LINE());
	}

	@Override
	public void decorateWorkTaskObjectToExternalManagedObjectFigure(
			PolylineConnection figure,
			ManagedFunctionObjectToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateWorkTaskObjectToDeskManagedObjectFigure(
			PolylineConnection figure,
			ManagedFunctionObjectToSectionManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateTaskFlowToTaskFigure(PolylineConnection figure,
			FunctionFlowToFunctionFigureContext context) {
		this.decorateInstigationStrategy(figure, context
				.getFlowInstigationStrategy());
	}

	@Override
	public void decorateTaskFlowToExternalFlowFigure(PolylineConnection figure,
			FunctionFlowToExternalFlowFigureContext context) {
		this.decorateInstigationStrategy(figure, context
				.getFlowInstigationStrategy());
	}

	@Override
	public void decorateTaskToNextTaskFigure(PolylineConnection figure,
			FunctionToNextFunctionFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateTaskToNextExternalFlowFigure(PolylineConnection figure,
			FunctionToNextExternalFlowFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateTaskEscalationToTaskFigure(PolylineConnection figure,
			FunctionEscalationToFunctionFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateTaskEscalationToExternalFlowFigure(
			PolylineConnection figure,
			FunctionEscalationToExternalFlowFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateWorkToInitialTaskFigure(PolylineConnection figure,
			WorkToInitialTaskFigureContext context) {
		figure.setForegroundColor(StandardOfficeFloorColours
				.INITIAL_TASK_LINE());
	}

	@Override
	public void decorateDeskManagedObjectToDeskManagedObjectSourceFigure(
			PolylineConnection figure,
			DeskManagedObjectToDeskManagedObjectSourceFigureContext context) {
		figure.setForegroundColor(StandardOfficeFloorColours.LINK_LINE());
	}

	@Override
	public void decorateDeskManagedObjectSourceFlowToExternalFlowFigure(
			PolylineConnection figure,
			DeskManagedObjectSourceFlowToExternalFlowFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateDeskManagedObjectSourceFlowToTaskFigure(
			PolylineConnection figure,
			SectionManagedObjectSourceFlowToFunctionFigureContext context) {
		figure.setTargetDecoration(new PolylineDecoration());
	}

	@Override
	public void decorateDeskManagedObjectDependencyToDeskManagedObjectFigure(
			PolylineConnection figure,
			DeskManagedObjectDependencyToDeskManagedObjectFigureContext context) {
		// Leave as default line
	}

	@Override
	public void decorateDeskManagedObjectDependencyToExternalManagedObjectFigure(
			PolylineConnection figure,
			DeskManagedObjectDependencyToExternalManagedObjectFigureContext context) {
		// Leave as default line
	}

}