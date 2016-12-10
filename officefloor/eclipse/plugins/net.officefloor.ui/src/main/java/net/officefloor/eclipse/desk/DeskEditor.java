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
package net.officefloor.eclipse.desk;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionChangeFactory;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.DeleteChangeFactory;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectDependencyEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectDependencyToDeskManagedObjectEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectDependencyToExternalManagedObjectEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectSourceEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectSourceFlowEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectSourceFlowToExternalFlowEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectSourceFlowToTaskEditPart;
import net.officefloor.eclipse.desk.editparts.DeskManagedObjectToDeskManagedObjectSourceEditPart;
import net.officefloor.eclipse.desk.editparts.ExternalFlowEditPart;
import net.officefloor.eclipse.desk.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.desk.editparts.TaskEditPart;
import net.officefloor.eclipse.desk.editparts.TaskEscalationEditPart;
import net.officefloor.eclipse.desk.editparts.TaskEscalationToExternalFlowEditPart;
import net.officefloor.eclipse.desk.editparts.TaskEscalationToTaskEditPart;
import net.officefloor.eclipse.desk.editparts.TaskFlowEditPart;
import net.officefloor.eclipse.desk.editparts.TaskFlowToExternalFlowEditPart;
import net.officefloor.eclipse.desk.editparts.TaskFlowToTaskEditPart;
import net.officefloor.eclipse.desk.editparts.TaskToNextExternalFlowEditPart;
import net.officefloor.eclipse.desk.editparts.TaskToNextTaskEditPart;
import net.officefloor.eclipse.desk.editparts.WorkEditPart;
import net.officefloor.eclipse.desk.editparts.WorkTaskEditPart;
import net.officefloor.eclipse.desk.editparts.WorkTaskObjectEditPart;
import net.officefloor.eclipse.desk.editparts.WorkTaskObjectToDeskManagedObjectEditPart;
import net.officefloor.eclipse.desk.editparts.WorkTaskObjectToExternalManagedObjectEditPart;
import net.officefloor.eclipse.desk.editparts.WorkTaskToTaskEditPart;
import net.officefloor.eclipse.desk.editparts.WorkToInitialTaskEditPart;
import net.officefloor.eclipse.desk.operations.AddDeskManagedObjectOperation;
import net.officefloor.eclipse.desk.operations.AddDeskManagedObjectSourceOperation;
import net.officefloor.eclipse.desk.operations.AddExternalFlowOperation;
import net.officefloor.eclipse.desk.operations.AddExternalManagedObjectOperation;
import net.officefloor.eclipse.desk.operations.AddWorkOperation;
import net.officefloor.eclipse.desk.operations.CreateTaskFromWorkTaskOperation;
import net.officefloor.eclipse.desk.operations.DeleteExternalFlowOperation;
import net.officefloor.eclipse.desk.operations.DeleteExternalManagedObjectOperation;
import net.officefloor.eclipse.desk.operations.DeleteTaskOperation;
import net.officefloor.eclipse.desk.operations.DeleteWorkOperation;
import net.officefloor.eclipse.desk.operations.RefactorWorkOperation;
import net.officefloor.eclipse.desk.operations.ToggleTaskPublicOperation;
import net.officefloor.eclipse.desk.operations.ToggleTaskObjectParameterOperation;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
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
import net.officefloor.model.desk.WorkTaskObjectToDeskManagedObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.impl.desk.DeskChangesImpl;
import net.officefloor.model.impl.desk.DeskRepositoryImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link DeskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeskEditor extends
		AbstractOfficeFloorEditor<DeskModel, DeskChanges> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.desk";

	@Override
	protected DeskChanges createModelChanges(DeskModel model) {
		return new DeskChangesImpl(model);
	}

	@Override
	protected DeskModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		return new DeskRepositoryImpl(new ModelRepositoryImpl())
				.retrieveDesk(configuration);
	}

	@Override
	protected void storeModel(DeskModel model, ConfigurationItem configuration)
			throws Exception {
		new DeskRepositoryImpl(new ModelRepositoryImpl()).storeDesk(model,
				configuration);
	}

	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(DeskModel.class, DeskEditPart.class);
		map.put(ExternalManagedObjectModel.class,
				ExternalManagedObjectEditPart.class);
		map.put(WorkModel.class, WorkEditPart.class);
		map.put(WorkTaskModel.class, WorkTaskEditPart.class);
		map.put(WorkTaskObjectModel.class, WorkTaskObjectEditPart.class);
		map.put(TaskModel.class, TaskEditPart.class);
		map.put(TaskFlowModel.class, TaskFlowEditPart.class);
		map.put(TaskEscalationModel.class, TaskEscalationEditPart.class);
		map.put(ExternalFlowModel.class, ExternalFlowEditPart.class);
		map.put(DeskManagedObjectSourceModel.class,
				DeskManagedObjectSourceEditPart.class);
		map.put(DeskManagedObjectSourceFlowModel.class,
				DeskManagedObjectSourceFlowEditPart.class);
		map.put(DeskManagedObjectModel.class, DeskManagedObjectEditPart.class);
		map.put(DeskManagedObjectDependencyModel.class,
				DeskManagedObjectDependencyEditPart.class);

		// Connections
		map.put(WorkTaskToTaskModel.class, WorkTaskToTaskEditPart.class);
		map.put(WorkTaskObjectToExternalManagedObjectModel.class,
				WorkTaskObjectToExternalManagedObjectEditPart.class);
		map.put(WorkTaskObjectToDeskManagedObjectModel.class,
				WorkTaskObjectToDeskManagedObjectEditPart.class);
		map.put(TaskFlowToTaskModel.class, TaskFlowToTaskEditPart.class);
		map.put(TaskFlowToExternalFlowModel.class,
				TaskFlowToExternalFlowEditPart.class);
		map.put(TaskToNextTaskModel.class, TaskToNextTaskEditPart.class);
		map.put(TaskToNextExternalFlowModel.class,
				TaskToNextExternalFlowEditPart.class);
		map.put(TaskEscalationToTaskModel.class,
				TaskEscalationToTaskEditPart.class);
		map.put(TaskEscalationToExternalFlowModel.class,
				TaskEscalationToExternalFlowEditPart.class);
		map.put(WorkToInitialTaskModel.class, WorkToInitialTaskEditPart.class);
		map.put(DeskManagedObjectToDeskManagedObjectSourceModel.class,
				DeskManagedObjectToDeskManagedObjectSourceEditPart.class);
		map.put(DeskManagedObjectSourceFlowToExternalFlowModel.class,
				DeskManagedObjectSourceFlowToExternalFlowEditPart.class);
		map.put(DeskManagedObjectSourceFlowToTaskModel.class,
				DeskManagedObjectSourceFlowToTaskEditPart.class);
		map.put(DeskManagedObjectDependencyToDeskManagedObjectModel.class,
				DeskManagedObjectDependencyToDeskManagedObjectEditPart.class);
		map.put(DeskManagedObjectDependencyToExternalManagedObjectModel.class,
				DeskManagedObjectDependencyToExternalManagedObjectEditPart.class);
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {

		// Allow deleting work
		policy.addDelete(WorkModel.class, new DeleteChangeFactory<WorkModel>() {
			@Override
			public Change<WorkModel> createChange(WorkModel target) {
				return DeskEditor.this.getModelChanges().removeWork(target);
			}
		});

		// Allow deleting task
		policy.addDelete(TaskModel.class, new DeleteChangeFactory<TaskModel>() {
			@Override
			public Change<TaskModel> createChange(TaskModel target) {
				return DeskEditor.this.getModelChanges().removeTask(target);
			}
		});

		// Allow deleting external managed object
		policy.addDelete(ExternalManagedObjectModel.class,
				new DeleteChangeFactory<ExternalManagedObjectModel>() {
					@Override
					public Change<ExternalManagedObjectModel> createChange(
							ExternalManagedObjectModel target) {
						return DeskEditor.this.getModelChanges()
								.removeExternalManagedObject(target);
					}
				});

		// Allow deleting managed object source
		policy.addDelete(DeskManagedObjectSourceModel.class,
				new DeleteChangeFactory<DeskManagedObjectSourceModel>() {
					@Override
					public Change<DeskManagedObjectSourceModel> createChange(
							DeskManagedObjectSourceModel target) {
						return DeskEditor.this.getModelChanges()
								.removeDeskManagedObjectSource(target);
					}
				});

		// Allow deleting managed object
		policy.addDelete(DeskManagedObjectModel.class,
				new DeleteChangeFactory<DeskManagedObjectModel>() {
					@Override
					public Change<DeskManagedObjectModel> createChange(
							DeskManagedObjectModel target) {
						return DeskEditor.this.getModelChanges()
								.removeDeskManagedObject(target);
					}
				});

		// Allow deleting external flow
		policy.addDelete(ExternalFlowModel.class,
				new DeleteChangeFactory<ExternalFlowModel>() {
					@Override
					public Change<ExternalFlowModel> createChange(
							ExternalFlowModel target) {
						return DeskEditor.this.getModelChanges()
								.removeExternalFlow(target);
					}
				});

		// Allow deleting task flow to task
		policy.addDelete(TaskFlowToTaskModel.class,
				new DeleteChangeFactory<TaskFlowToTaskModel>() {
					@Override
					public Change<TaskFlowToTaskModel> createChange(
							TaskFlowToTaskModel target) {
						return DeskEditor.this.getModelChanges()
								.removeTaskFlowToTask(target);
					}
				});

		// Allow deleting task flow to external flow
		policy.addDelete(TaskFlowToExternalFlowModel.class,
				new DeleteChangeFactory<TaskFlowToExternalFlowModel>() {
					@Override
					public Change<TaskFlowToExternalFlowModel> createChange(
							TaskFlowToExternalFlowModel target) {
						return DeskEditor.this.getModelChanges()
								.removeTaskFlowToExternalFlow(target);
					}
				});

		// Allow deleting task to next task
		policy.addDelete(TaskToNextTaskModel.class,
				new DeleteChangeFactory<TaskToNextTaskModel>() {
					@Override
					public Change<TaskToNextTaskModel> createChange(
							TaskToNextTaskModel target) {
						return DeskEditor.this.getModelChanges()
								.removeTaskToNextTask(target);
					}
				});

		// Allow deleting task to next external flow
		policy.addDelete(TaskToNextExternalFlowModel.class,
				new DeleteChangeFactory<TaskToNextExternalFlowModel>() {
					@Override
					public Change<TaskToNextExternalFlowModel> createChange(
							TaskToNextExternalFlowModel target) {
						return DeskEditor.this.getModelChanges()
								.removeTaskToNextExternalFlow(target);
					}
				});

		// Allow deleting task escalation to task
		policy.addDelete(TaskEscalationToTaskModel.class,
				new DeleteChangeFactory<TaskEscalationToTaskModel>() {
					@Override
					public Change<TaskEscalationToTaskModel> createChange(
							TaskEscalationToTaskModel target) {
						return DeskEditor.this.getModelChanges()
								.removeTaskEscalationToTask(target);
					}
				});

		// Allow deleting task escalation to external flow
		policy.addDelete(TaskEscalationToExternalFlowModel.class,
				new DeleteChangeFactory<TaskEscalationToExternalFlowModel>() {
					@Override
					public Change<TaskEscalationToExternalFlowModel> createChange(
							TaskEscalationToExternalFlowModel target) {
						return DeskEditor.this.getModelChanges()
								.removeTaskEscalationToExternalFlow(target);
					}
				});

		// Allow deleting work task object to external managed object
		policy.addDelete(
				WorkTaskObjectToExternalManagedObjectModel.class,
				new DeleteChangeFactory<WorkTaskObjectToExternalManagedObjectModel>() {
					@Override
					public Change<WorkTaskObjectToExternalManagedObjectModel> createChange(
							WorkTaskObjectToExternalManagedObjectModel target) {
						return DeskEditor.this.getModelChanges()
								.removeWorkTaskObjectToExternalManagedObject(
										target);
					}
				});

		// Allow deleting work task object to managed object
		policy.addDelete(
				WorkTaskObjectToDeskManagedObjectModel.class,
				new DeleteChangeFactory<WorkTaskObjectToDeskManagedObjectModel>() {
					@Override
					public Change<WorkTaskObjectToDeskManagedObjectModel> createChange(
							WorkTaskObjectToDeskManagedObjectModel target) {
						return DeskEditor.this
								.getModelChanges()
								.removeWorkTaskObjectToDeskManagedObject(target);
					}
				});

		// Allow deleting work to initial task
		policy.addDelete(WorkToInitialTaskModel.class,
				new DeleteChangeFactory<WorkToInitialTaskModel>() {
					@Override
					public Change<WorkToInitialTaskModel> createChange(
							WorkToInitialTaskModel target) {
						return DeskEditor.this.getModelChanges()
								.removeWorkToInitialTask(target);
					}
				});

		// Allow deleting managed object source flow to task
		policy.addDelete(
				DeskManagedObjectSourceFlowToTaskModel.class,
				new DeleteChangeFactory<DeskManagedObjectSourceFlowToTaskModel>() {
					@Override
					public Change<DeskManagedObjectSourceFlowToTaskModel> createChange(
							DeskManagedObjectSourceFlowToTaskModel target) {
						return DeskEditor.this
								.getModelChanges()
								.removeDeskManagedObjectSourceFlowToTask(target);
					}
				});

		// Allow deleting managed object source flow to external flow
		policy.addDelete(
				DeskManagedObjectSourceFlowToExternalFlowModel.class,
				new DeleteChangeFactory<DeskManagedObjectSourceFlowToExternalFlowModel>() {
					@Override
					public Change<DeskManagedObjectSourceFlowToExternalFlowModel> createChange(
							DeskManagedObjectSourceFlowToExternalFlowModel target) {
						return DeskEditor.this
								.getModelChanges()
								.removeDeskManagedObjectSourceFlowToExternalFlow(
										target);
					}
				});

		// Allow deleting managed object dependency to managed object
		policy.addDelete(
				DeskManagedObjectDependencyToDeskManagedObjectModel.class,
				new DeleteChangeFactory<DeskManagedObjectDependencyToDeskManagedObjectModel>() {
					@Override
					public Change<DeskManagedObjectDependencyToDeskManagedObjectModel> createChange(
							DeskManagedObjectDependencyToDeskManagedObjectModel target) {
						return DeskEditor.this
								.getModelChanges()
								.removeDeskManagedObjectDependencyToDeskManagedObject(
										target);
					}
				});

		// Allow deleting managed object dependency to external managed object
		policy.addDelete(
				DeskManagedObjectDependencyToExternalManagedObjectModel.class,
				new DeleteChangeFactory<DeskManagedObjectDependencyToExternalManagedObjectModel>() {
					@Override
					public Change<DeskManagedObjectDependencyToExternalManagedObjectModel> createChange(
							DeskManagedObjectDependencyToExternalManagedObjectModel target) {
						return DeskEditor.this
								.getModelChanges()
								.removeDeskManagedObjectDependencyToExternalManagedObject(
										target);
					}
				});
	}

	@Override
	protected void populateGraphicalEditPolicy(
			OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect work task object to external managed object
		policy.addConnection(
				WorkTaskObjectModel.class,
				ExternalManagedObjectModel.class,
				new ConnectionChangeFactory<WorkTaskObjectModel, ExternalManagedObjectModel>() {
					@Override
					public Change<?> createChange(WorkTaskObjectModel source,
							ExternalManagedObjectModel target,
							CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkWorkTaskObjectToExternalManagedObject(
										source, target);
					}
				});

		// Connect work task object to managed object
		policy.addConnection(
				WorkTaskObjectModel.class,
				DeskManagedObjectModel.class,
				new ConnectionChangeFactory<WorkTaskObjectModel, DeskManagedObjectModel>() {
					@Override
					public Change<?> createChange(WorkTaskObjectModel source,
							DeskManagedObjectModel target,
							CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkWorkTaskObjectToDeskManagedObject(source,
										target);
					}
				});

		// Connect task flow to task
		policy.addConnection(TaskFlowModel.class, TaskModel.class,
				new ConnectionChangeFactory<TaskFlowModel, TaskModel>() {
					@Override
					public Change<?> createChange(TaskFlowModel source,
							TaskModel target, CreateConnectionRequest request) {

						// Obtain the instigation strategy
						FlowInstigationStrategyEnum strategy = DeskEditor.this
								.getFlowInstigationStrategy(request
										.getNewObject());
						if (strategy == null) {
							return null; // must have instigation strategy
						}

						// Return the change to the link
						return DeskEditor.this.getModelChanges()
								.linkTaskFlowToTask(source, target, strategy);
					}
				});

		// Connect task flow to external flow
		policy.addConnection(
				TaskFlowModel.class,
				ExternalFlowModel.class,
				new ConnectionChangeFactory<TaskFlowModel, ExternalFlowModel>() {
					@Override
					public Change<?> createChange(TaskFlowModel source,
							ExternalFlowModel target,
							CreateConnectionRequest request) {

						// Obtain the instigation strategy
						FlowInstigationStrategyEnum strategy = DeskEditor.this
								.getFlowInstigationStrategy(request
										.getNewObject());
						if (strategy == null) {
							return null; // must have instigation
							// strategy
						}

						// Return the change to the link
						return DeskEditor.this.getModelChanges()
								.linkTaskFlowToExternalFlow(source, target,
										strategy);
					}
				});

		// Connect task to next task
		policy.addConnection(TaskModel.class, TaskModel.class,
				new ConnectionChangeFactory<TaskModel, TaskModel>() {
					@Override
					public Change<?> createChange(TaskModel source,
							TaskModel target, CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkTaskToNextTask(source, target);
					}
				});

		// Connect task to next external flow
		policy.addConnection(TaskModel.class, ExternalFlowModel.class,
				new ConnectionChangeFactory<TaskModel, ExternalFlowModel>() {
					@Override
					public Change<?> createChange(TaskModel source,
							ExternalFlowModel target,
							CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkTaskToNextExternalFlow(source, target);
					}
				});

		// Connect task escalation to task
		policy.addConnection(TaskEscalationModel.class, TaskModel.class,
				new ConnectionChangeFactory<TaskEscalationModel, TaskModel>() {
					@Override
					public Change<?> createChange(TaskEscalationModel source,
							TaskModel target, CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkTaskEscalationToTask(source, target);
					}
				});

		// Connect task escalation to external flow
		policy.addConnection(
				TaskEscalationModel.class,
				ExternalFlowModel.class,
				new ConnectionChangeFactory<TaskEscalationModel, ExternalFlowModel>() {
					@Override
					public Change<?> createChange(TaskEscalationModel source,
							ExternalFlowModel target,
							CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkTaskEscalationToExternalFlow(source,
										target);
					}
				});

		// Connect work to initial task
		policy.addConnection(WorkModel.class, TaskModel.class,
				new ConnectionChangeFactory<WorkModel, TaskModel>() {
					@Override
					public Change<?> createChange(WorkModel source,
							TaskModel target, CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkWorkToInitialTask(source, target);
					}
				});

		// Connect managed object source flow to task
		policy.addConnection(
				DeskManagedObjectSourceFlowModel.class,
				TaskModel.class,
				new ConnectionChangeFactory<DeskManagedObjectSourceFlowModel, TaskModel>() {
					@Override
					public Change<?> createChange(
							DeskManagedObjectSourceFlowModel source,
							TaskModel target, CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkDeskManagedObjectSourceFlowToTask(source,
										target);
					}
				});

		// Connect managed object source flow to external flow
		policy.addConnection(
				DeskManagedObjectSourceFlowModel.class,
				ExternalFlowModel.class,
				new ConnectionChangeFactory<DeskManagedObjectSourceFlowModel, ExternalFlowModel>() {
					@Override
					public Change<?> createChange(
							DeskManagedObjectSourceFlowModel source,
							ExternalFlowModel target,
							CreateConnectionRequest request) {
						return DeskEditor.this.getModelChanges()
								.linkDeskManagedObjectSourceFlowToExternalFlow(
										source, target);
					}
				});

		// Connect managed object dependency to managed object
		policy.addConnection(
				DeskManagedObjectDependencyModel.class,
				DeskManagedObjectModel.class,
				new ConnectionChangeFactory<DeskManagedObjectDependencyModel, DeskManagedObjectModel>() {
					@Override
					public Change<?> createChange(
							DeskManagedObjectDependencyModel source,
							DeskManagedObjectModel target,
							CreateConnectionRequest request) {
						return DeskEditor.this
								.getModelChanges()
								.linkDeskManagedObjectDependencyToDeskManagedObject(
										source, target);
					}
				});

		// Connect managed object dependency to external managed object
		policy.addConnection(
				DeskManagedObjectDependencyModel.class,
				ExternalManagedObjectModel.class,
				new ConnectionChangeFactory<DeskManagedObjectDependencyModel, ExternalManagedObjectModel>() {
					@Override
					public Change<?> createChange(
							DeskManagedObjectDependencyModel source,
							ExternalManagedObjectModel target,
							CreateConnectionRequest request) {
						return DeskEditor.this
								.getModelChanges()
								.linkDeskManagedObjectDependencyToExternalManagedObject(
										source, target);
					}
				});
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the desk model and create changes for it
		DeskChanges deskChanges = this.getModelChanges();

		// Add actions
		list.add(new AddWorkOperation(deskChanges));
		list.add(new CreateTaskFromWorkTaskOperation(deskChanges));
		list.add(new AddExternalFlowOperation(deskChanges));
		list.add(new AddExternalManagedObjectOperation(deskChanges));
		list.add(new AddDeskManagedObjectSourceOperation(deskChanges));
		list.add(new AddDeskManagedObjectOperation(deskChanges));

		// Delete actions
		list.add(new DeleteWorkOperation(deskChanges));
		list.add(new DeleteTaskOperation(deskChanges));
		list.add(new DeleteExternalFlowOperation(deskChanges));
		list.add(new DeleteExternalManagedObjectOperation(deskChanges));

		// Work actions
		list.add(new RefactorWorkOperation(deskChanges));

		// Task actions
		list.add(new ToggleTaskPublicOperation(deskChanges));

		// Work Task actions
		list.add(new ToggleTaskObjectParameterOperation(deskChanges));
	}

	@Override
	protected void initialisePaletteRoot() {
		// Add the link group
		PaletteGroup linkGroup = new PaletteGroup("Links");
		linkGroup.add(new ConnectionCreationToolEntry("Sequential",
				"sequential", new FlowInstigationTagFactory(
						DeskChanges.SEQUENTIAL_LINK), null, null));
		linkGroup.add(new ConnectionCreationToolEntry("Parallel", "parallel",
				new FlowInstigationTagFactory(DeskChanges.PARALLEL_LINK), null,
				null));
		linkGroup.add(new ConnectionCreationToolEntry("Asynchronous",
				"asynchronous", new FlowInstigationTagFactory(
						DeskChanges.ASYNCHRONOUS_LINK), null, null));
		this.paletteRoot.add(linkGroup);
	}

	/**
	 * Obtains the {@link FlowInstigationStrategyEnum}.
	 * 
	 * @param instigationStrategy
	 *            Instigation type.
	 * @return {@link FlowInstigationStrategyEnum} or <code>null</code> if
	 *         unknown instigation strategy.
	 */
	public FlowInstigationStrategyEnum getFlowInstigationStrategy(
			Object instigationStrategy) {

		// Ensure have a instigation strategy
		if (instigationStrategy == null) {
			this.messageError("Must select instigation strategy");
			return null;
		}

		// Obtain the flow instigation strategy
		FlowInstigationStrategyEnum enumStrategy = getFlowInstigationStrategy(instigationStrategy
				.toString());
		if (enumStrategy == null) {
			this.messageError("Unknown instigation strategy "
					+ instigationStrategy);
			return null; // must have instigation strategy
		}

		// Return the instigation strategy
		return enumStrategy;
	}

	/**
	 * Obtains the {@link FlowInstigationStrategyEnum}.
	 * 
	 * @param instigationStrategy
	 *            Text name of the {@link FlowInstigationStrategyEnum}.
	 * @return {@link FlowInstigationStrategyEnum} or <code>null</code> if
	 *         unknown.
	 */
	public static FlowInstigationStrategyEnum getFlowInstigationStrategy(
			String instigationStrategy) {

		// Obtain the flow instigation strategy
		if (DeskChanges.SEQUENTIAL_LINK.equals(instigationStrategy)) {
			return FlowInstigationStrategyEnum.SEQUENTIAL;
		} else if (DeskChanges.PARALLEL_LINK.equals(instigationStrategy)) {
			return FlowInstigationStrategyEnum.PARALLEL;
		} else if (DeskChanges.ASYNCHRONOUS_LINK.equals(instigationStrategy)) {
			return FlowInstigationStrategyEnum.ASYNCHRONOUS;
		} else {
			// Unknown flow instigation strategy
			return null;
		}
	}

}