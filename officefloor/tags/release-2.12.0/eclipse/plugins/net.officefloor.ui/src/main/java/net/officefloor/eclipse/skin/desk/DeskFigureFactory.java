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
package net.officefloor.eclipse.skin.desk;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.desk.DeskManagedObjectDependencyModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToTaskModel;
import net.officefloor.model.desk.DeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;
import net.officefloor.model.desk.TaskToNextTaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link DeskModel}.
 *
 * @author Daniel Sagenschneider
 */
public interface DeskFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link WorkModel}.
	 *
	 * @param context
	 *            {@link WorkFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	WorkFigure createWorkFigure(WorkFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link WorkTaskModel}.
	 *
	 * @param context
	 *            {@link WorkTaskFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	WorkTaskFigure createWorkTaskFigure(WorkTaskFigureContext context);

	/**
	 * Creates {@link OfficeFloorFigure} for the {@link WorkTaskObjectModel}.
	 *
	 * @param context
	 *            {@link WorkTaskObjectFigureContext}.
	 * @return {@link WorkTaskObjectFigure}.
	 */
	WorkTaskObjectFigure createWorkTaskObjectFigure(
			WorkTaskObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link ExternalFlowModel}.
	 *
	 * @param context
	 *            {@link ExternalFlowModel}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalFlowFigure createExternalFlowFigure(
			ExternalFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalManagedObjectModel}.
	 *
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link TaskModel}.
	 *
	 * @param context
	 *            {@link TaskFigureContext}.
	 * @return {@link TaskFigure}.
	 */
	TaskFigure createTaskFigure(TaskFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link TaskFlowModel}.
	 *
	 * @param context
	 *            {@link TaskFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	TaskFlowFigure createTaskFlowFigure(TaskFlowFigureContext context);

	/**
	 * Creates {@link OfficeFloorFigure} for the {@link TaskEscalationModel}.
	 *
	 * @param context
	 *            {@link TaskEscalationFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	TaskEscalationFigure createTaskEscalationFigure(
			TaskEscalationFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link DeskManagedObjectSourceModel}.
	 *
	 * @param context
	 *            {@link DeskManagedObjectSourceFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DeskManagedObjectSourceFigure createDeskManagedObjectSourceFigure(
			DeskManagedObjectSourceFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link DeskManagedObjectSourceFlowModel}.
	 *
	 * @param context
	 *            {@link DeskManagedObjectSourceFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DeskManagedObjectSourceFlowFigure createDeskManagedObjectSourceFlowFigure(
			DeskManagedObjectSourceFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link DeskManagedObjectModel}.
	 *
	 * @param context
	 *            {@link DeskManagedObjectFigureContext}.
	 * @return {@link DeskManagedObjectFigure}.
	 */
	DeskManagedObjectFigure createDeskManagedObjectFigure(
			DeskManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link DeskManagedObjectDependencyModel}.
	 *
	 * @param context
	 *            {@link DeskManagedObjectDependencyFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DeskManagedObjectDependencyFigure createDeskManagedObjectDependencyFigure(
			DeskManagedObjectDependencyFigureContext context);

	/**
	 * Decorates the {@link WorkTaskToTaskModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 */
	void decorateWorkTaskToTaskFigure(PolylineConnection figure,
			WorkTaskToTaskFigureContext context);

	/**
	 * Decorates the {@link WorkTaskObjectToExternalManagedObjectModel}
	 * connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link WorkTaskObjectToExternalManagedObjectModel}.
	 */
	void decorateWorkTaskObjectToExternalManagedObjectFigure(
			PolylineConnection figure,
			WorkTaskObjectToExternalManagedObjectFigureContext context);

	/**
	 * Decorates the {@link WorkTaskObjectToDeskManagedObjectModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link WorkTaskObjectToDeskManagedObjectModel}.
	 */
	void decorateWorkTaskObjectToDeskManagedObjectFigure(
			PolylineConnection figure,
			WorkTaskObjectToDeskManagedObjectFigureContext context);

	/**
	 * Decorates the {@link TaskFlowToTaskModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TaskFlowToTaskFigureContext}.
	 */
	void decorateTaskFlowToTaskFigure(PolylineConnection figure,
			TaskFlowToTaskFigureContext context);

	/**
	 * Decorates the {@link TaskFlowToExternalFlowModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TaskFlowToExternalFlowFigureContext}.
	 */
	void decorateTaskFlowToExternalFlowFigure(PolylineConnection figure,
			TaskFlowToExternalFlowFigureContext context);

	/**
	 * Decorates the {@link TaskToNextTaskModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TaskToNextTaskFigureContext}.
	 */
	void decorateTaskToNextTaskFigure(PolylineConnection figure,
			TaskToNextTaskFigureContext context);

	/**
	 * Decorates the {@link TaskToNextExternalFlowModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TaskToNextExternalFlowModel}.
	 */
	void decorateTaskToNextExternalFlowFigure(PolylineConnection figure,
			TaskToNextExternalFlowFigureContext context);

	/**
	 * Decorates the {@link TaskEscalationToTaskModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TaskEscalationToTaskFigureContext}.
	 */
	void decorateTaskEscalationToTaskFigure(PolylineConnection figure,
			TaskEscalationToTaskFigureContext context);

	/**
	 * Decorates the {@link TaskEscalationToExternalFlowModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link TaskEscalationToExternalFlowModel}.
	 */
	void decorateTaskEscalationToExternalFlowFigure(PolylineConnection figure,
			TaskEscalationToExternalFlowFigureContext context);

	/**
	 * Decorates the {@link WorkToInitialTaskModel} connection.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param workToInitialTaskEditPart
	 */
	void decorateWorkToInitialTaskFigure(PolylineConnection figure,
			WorkToInitialTaskFigureContext context);

	/**
	 * Decorates the {@link DeskManagedObjectToDeskManagedObjectSourceModel}
	 * figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link DeskManagedObjectToDeskManagedObjectSourceModel}
	 */
	void decorateDeskManagedObjectToDeskManagedObjectSourceFigure(
			PolylineConnection figure,
			DeskManagedObjectToDeskManagedObjectSourceFigureContext context);

	/**
	 * Decorates the {@link DeskManagedObjectSourceFlowToTaskModel} figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link DeskManagedObjectSourceFlowToTaskFigureContext}
	 */
	void decorateDeskManagedObjectSourceFlowToTaskFigure(
			PolylineConnection figure,
			DeskManagedObjectSourceFlowToTaskFigureContext context);

	/**
	 * Decorates the {@link DeskManagedObjectSourceFlowToExternalFlowModel}
	 * figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link DeskManagedObjectSourceFlowToExternalFlowFigureContext}
	 */
	void decorateDeskManagedObjectSourceFlowToExternalFlowFigure(
			PolylineConnection figure,
			DeskManagedObjectSourceFlowToExternalFlowFigureContext context);

	/**
	 * Decorates the {@link DeskManagedObjectDependencyToDeskManagedObjectModel}
	 * figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link DeskManagedObjectDependencyToDeskManagedObjectFigureContext}
	 */
	void decorateDeskManagedObjectDependencyToDeskManagedObjectFigure(
			PolylineConnection figure,
			DeskManagedObjectDependencyToDeskManagedObjectFigureContext context);

	/**
	 * Decorates the
	 * {@link DeskManagedObjectDependencyToExternalManagedObjectModel} figure.
	 *
	 * @param figure
	 *            {@link IFigure} to decorate.
	 * @param context
	 *            {@link DeskManagedObjectDependencyToExternalManagedObjectFigureContext}
	 */
	void decorateDeskManagedObjectDependencyToExternalManagedObjectFigure(
			PolylineConnection figure,
			DeskManagedObjectDependencyToExternalManagedObjectFigureContext context);

}