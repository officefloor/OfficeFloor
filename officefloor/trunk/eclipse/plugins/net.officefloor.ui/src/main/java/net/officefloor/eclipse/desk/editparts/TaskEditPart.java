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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.worksource.TaskDocumentationContext;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.skin.desk.TaskFigure;
import net.officefloor.eclipse.skin.desk.TaskFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.TaskModel.TaskEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskEditPart extends
		AbstractOfficeFloorEditPart<TaskModel, TaskEvent, TaskFigure> implements
		TaskFigureContext {

	/**
	 * Obtains the {@link WorkTaskModel} for the {@link TaskModel}.
	 * 
	 * @param task
	 *            {@link TaskModel}.
	 * @return {@link WorkTaskModel} or <code>null</code> if not attached.
	 */
	public static WorkTaskModel getWorkTask(TaskModel task) {
		// Ensure have task
		if (task == null) {
			return null;
		}

		// Obtain the work task
		WorkTaskToTaskModel conn = task.getWorkTask();
		if (conn != null) {
			// Return the work task
			return conn.getWorkTask();
		}

		// If here then no work task attached
		return null;
	}

	/**
	 * Obtains the {@link DeskModel} for the {@link TaskModel}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @return {@link DeskModel} or <code>null</code> if {@link Task} not
	 *         contained in a {@link DeskModel}.
	 */
	public static DeskModel getDesk(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart) {
		// Obtain the desk (root model), ensuring is correct type
		Object rootModel = editPart.getEditor().getCastedModel();
		return (rootModel instanceof DeskModel ? (DeskModel) rootModel : null);
	}

	@Override
	protected TaskFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createTaskFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTaskFlows());
		childModels.addAll(this.getCastedModel().getTaskEscalations());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getNextTask());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getNextExternalFlow());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Add work that this is initial task
		EclipseUtil.addToList(models, this.getCastedModel()
				.getInitialTaskForWork());

		// Add work task
		EclipseUtil.addToList(models, this.getCastedModel().getWorkTask());

		// Add task inputs, handled escalations, previous tasks
		models.addAll(this.getCastedModel().getTaskFlowInputs());
		models.addAll(this.getCastedModel().getTaskEscalationInputs());
		models.addAll(this.getCastedModel().getPreviousTasks());

		// Add managed object source flows
		models.addAll(this.getCastedModel().getDeskManagedObjectSourceFlows());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<TaskModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<DeskChanges, TaskModel>() {
			@Override
			public String getInitialValue() {
				return TaskEditPart.this.getCastedModel().getTaskName();
			}

			@Override
			public IFigure getLocationFigure() {
				return TaskEditPart.this.getOfficeFloorFigure()
						.getTaskNameFigure();
			}

			@Override
			public Change<TaskModel> createChange(DeskChanges changes,
					TaskModel target, String newValue) {
				return changes.renameTask(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<TaskModel> policy) {
		policy.allowOpening(new OpenHandler<TaskModel>() {
			@Override
			public void doOpen(OpenHandlerContext<TaskModel> context) {

				// Obtain the desk
				DeskModel desk = TaskEditPart.getDesk(context.getEditPart());

				// Obtain the work
				WorkTaskModel workTask = TaskEditPart.getWorkTask(context
						.getModel());
				WorkModel work = WorkTaskEditPart.getWork(workTask, desk);

				// Ensure have the work
				if (work == null) {
					// Must have connected work
					context.getEditPart()
							.messageError(
									"Can not open task.\n"
											+ "\nPlease ensure the task is connected to a work source.");
					return; // can not open
				}

				// Open the work
				WorkEditPart.openWorkSource(work, context);
			}
		});
	}

	@Override
	protected Class<TaskEvent> getPropertyChangeEventType() {
		return TaskEvent.class;
	}

	@Override
	protected void handlePropertyChange(TaskEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_TASK_NAME:
			this.getOfficeFloorFigure().setTaskName(
					this.getCastedModel().getTaskName());
			break;

		case CHANGE_IS_PUBLIC:
			// Ensure display is public
			this.getOfficeFloorFigure().setIsPublic(
					this.getCastedModel().getIsPublic());
			break;

		case CHANGE_NEXT_TASK:
		case CHANGE_NEXT_EXTERNAL_FLOW:
			this.refreshSourceConnections();
			break;

		case CHANGE_INITIAL_TASK_FOR_WORK:
		case CHANGE_WORK_TASK:
		case ADD_TASK_FLOW_INPUT:
		case REMOVE_TASK_FLOW_INPUT:
		case ADD_TASK_ESCALATION_INPUT:
		case REMOVE_TASK_ESCALATION_INPUT:
		case ADD_PREVIOUS_TASK:
		case REMOVE_PREVIOUS_TASK:
		case ADD_DESK_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_DESK_MANAGED_OBJECT_SOURCE_FLOW:
			this.refreshTargetConnections();
			break;

		case ADD_TASK_FLOW:
		case REMOVE_TASK_FLOW:
		case ADD_TASK_ESCALATION:
		case REMOVE_TASK_ESCALATION:
			this.refreshChildren();
			break;

		case CHANGE_RETURN_TYPE:
		case CHANGE_WORK_NAME:
		case CHANGE_WORK_TASK_NAME:
			// Non visual change
			break;
		}
	}

	/*
	 * ======================= FlowItemFigureContext ========================
	 */

	@Override
	public String getTaskName() {
		return this.getCastedModel().getTaskName();
	}

	@Override
	public boolean isPublic() {
		return this.getCastedModel().getIsPublic();
	}

	@Override
	public void setIsPublic(final boolean isPublic) {

		// Store current state
		final boolean currentIsPublic = this.getCastedModel().getIsPublic();

		// Make change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				TaskEditPart.this.getCastedModel().setIsPublic(isPublic);
			}

			@Override
			protected void undoCommand() {
				TaskEditPart.this.getCastedModel().setIsPublic(currentIsPublic);
			}
		});
	}

	@Override
	public String getParameterTypeName() {

		// Obtain the work task for the task
		WorkTaskModel workTask = TaskEditPart
				.getWorkTask(this.getCastedModel());
		if (workTask != null) {
			// Have work task, so find first parameter
			for (WorkTaskObjectModel object : workTask.getTaskObjects()) {
				if (object.getIsParameter()) {
					// Have parameter, so return its type
					return object.getObjectType();
				}
			}
		}

		// Did not find parameter
		return null;
	}

	@Override
	public String getReturnTypeName() {
		return this.getCastedModel().getReturnType();
	}

	@Override
	public String getTaskDocumentation() {

		// Obtain the desk
		DeskModel desk = TaskEditPart.getDesk(this);

		// Return task documentation
		return TaskDocumentationContextImpl.getTaskDocumentation(desk,
				this.getCastedModel(), this);
	}

	/**
	 * {@link TaskDocumentationContext} implementation.
	 */
	private static class TaskDocumentationContextImpl implements
			TaskDocumentationContext {

		/**
		 * Obtains the {@link Task} documentation for a {@link TaskModel}.
		 * 
		 * @param desk
		 *            {@link DeskModel}.
		 * @param task
		 *            {@link TaskModel}.
		 * @param editPart
		 *            {@link AbstractOfficeFloorEditPart}.
		 * @return Documentation for the {@link Task}.
		 */
		@SuppressWarnings("rawtypes")
		public static String getTaskDocumentation(DeskModel desk,
				TaskModel task, AbstractOfficeFloorEditPart<?, ?, ?> editPart) {

			// Obtain the work and work task for the task
			WorkTaskModel workTask = TaskEditPart.getWorkTask(task);
			WorkModel work = WorkTaskEditPart.getWork(workTask, desk);
			if (work == null) {
				// Can not obtain work, so provide available documentation
				return "Task "
						+ task.getTaskName()
						+ " is not associated with any Work.\n\nPlease ensure it is associated to Work.";

			}

			// Obtain the work source details
			String workSourceClassName = work.getWorkSourceClassName();
			if (EclipseUtil.isBlank(workSourceClassName)) {
				// Must have work source class name
				return "Task " + task.getTaskName() + " runs task "
						+ workTask.getWorkTaskName() + " on work "
						+ work.getWorkName()
						+ "\n\nThe work however does not specify a "
						+ WorkSource.class.getSimpleName();
			}

			// Determine if there is a work source extension for the work
			Map<String, WorkSourceExtension> extensions = ExtensionUtil
					.createWorkSourceExtensionMap();
			WorkSourceExtension<?, ?> extension = extensions
					.get(workSourceClassName);

			// Obtain the task documentation
			String taskDocumentation = null;
			if (extension != null) {
				try {

					// Obtain the work task name
					String workTaskName = workTask.getWorkTaskName();

					// Obtain the property list
					PropertyList properties = OfficeFloorCompiler
							.newPropertyList();
					for (PropertyModel property : work.getProperties()) {
						properties.addProperty(property.getName()).setValue(
								property.getValue());
					}

					// Obtain the class loader
					ClassLoader classLoader = ProjectClassLoader
							.create(editPart.getEditor());

					// Create the context
					TaskDocumentationContext context = new TaskDocumentationContextImpl(
							workTaskName, properties, classLoader);

					// Obtain documentation from extension
					taskDocumentation = extension.getTaskDocumentation(context);

				} catch (Throwable ex) {
					// No documentation for extension
					taskDocumentation = null;
				}
			}
			if (EclipseUtil.isBlank(taskDocumentation)) {
				// No extension, no documentation or failure from extension
				taskDocumentation = "Task " + workTask.getWorkTaskName()
						+ " of source " + workSourceClassName;

			}

			// Return the task documentation
			return taskDocumentation;
		}

		/**
		 * {@link Task} name.
		 */
		private final String taskName;

		/**
		 * {@link PropertyList}.
		 */
		private final PropertyList propertyList;

		/**
		 * {@link ClassLoader}.
		 */
		private final ClassLoader classLoader;

		/**
		 * Initiate.
		 * 
		 * @param taskName
		 *            {@link Task} name.
		 * @param propertyList
		 *            {@link PropertyList}.
		 * @param classLoader
		 *            {@link ClassLoader}.
		 */
		public TaskDocumentationContextImpl(String taskName,
				PropertyList propertyList, ClassLoader classLoader) {
			this.taskName = taskName;
			this.propertyList = propertyList;
			this.classLoader = classLoader;
		}

		/*
		 * ==================== TaskDocumentationContext ==================
		 */

		@Override
		public String getTaskName() {
			return this.taskName;
		}

		@Override
		public PropertyList getPropertyList() {
			return this.propertyList;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}

}