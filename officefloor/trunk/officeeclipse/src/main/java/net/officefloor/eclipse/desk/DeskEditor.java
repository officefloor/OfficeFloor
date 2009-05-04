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
package net.officefloor.eclipse.desk;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.commands.TagFactory;
import net.officefloor.eclipse.common.editparts.OfficeFloorConnectionEditPart;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.eclipse.desk.editparts.WorkTaskEditPart;
import net.officefloor.eclipse.desk.editparts.WorkTaskObjectEditPart;
import net.officefloor.eclipse.desk.editparts.WorkEditPart;
import net.officefloor.eclipse.desk.editparts.ExternalFlowEditPart;
import net.officefloor.eclipse.desk.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.desk.editparts.TaskEditPart;
import net.officefloor.eclipse.desk.editparts.TaskEscalationEditPart;
import net.officefloor.eclipse.desk.editparts.TaskFlowEditPart;
import net.officefloor.eclipse.desk.operations.AddExternalFlowOperation;
import net.officefloor.eclipse.desk.operations.AddExternalManagedObjectOperation;
import net.officefloor.eclipse.desk.operations.AddWorkOperation;
import net.officefloor.eclipse.desk.operations.CreateFlowItemFromDeskTaskOperation;
import net.officefloor.eclipse.desk.operations.RefreshWorkOperation;
import net.officefloor.eclipse.desk.operations.ToggleFlowItemPublicOperation;
import net.officefloor.eclipse.desk.operations.ToggleTaskObjectParameterOperation;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskChanges;
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
import net.officefloor.model.impl.desk.DeskRepositoryImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link DeskModel}.
 * 
 * @author Daniel
 */
public class DeskEditor extends
		AbstractOfficeFloorEditor<DeskModel, DeskEditPart> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.desk";

	@Override
	protected boolean isDragTarget() {
		// Disallow as drag target
		return false;
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

		// Connections
		map.put(WorkTaskObjectToExternalManagedObjectModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(TaskFlowToTaskModel.class, OfficeFloorConnectionEditPart.class);
		map.put(TaskFlowToExternalFlowModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(WorkTaskToTaskModel.class, OfficeFloorConnectionEditPart.class);
		map.put(WorkToInitialTaskModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(TaskToNextTaskModel.class, OfficeFloorConnectionEditPart.class);
		map.put(TaskToNextExternalFlowModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(TaskEscalationToTaskModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(TaskEscalationToExternalFlowModel.class,
				OfficeFloorConnectionEditPart.class);
	}

	@Override
	protected void initialisePaletteRoot() {
		// Add the link group
		PaletteGroup linkGroup = new PaletteGroup("Links");
		linkGroup.add(new ConnectionCreationToolEntry("Sequential",
				"sequential", new TagFactory(DeskChanges.SEQUENTIAL_LINK),
				null, null));
		linkGroup.add(new ConnectionCreationToolEntry("Parallel", "parallel",
				new TagFactory(DeskChanges.PARALLEL_LINK), null, null));
		linkGroup.add(new ConnectionCreationToolEntry("Asynchronous",
				"asynchronous",
				new TagFactory(DeskChanges.ASYNCHRONOUS_LINK), null, null));
		this.paletteRoot.add(linkGroup);
	}

	@Override
	protected void populateOperations(List<Operation> list) {
		// Add model actions
		list.add(new AddExternalManagedObjectOperation());
		list.add(new AddWorkOperation());
		list.add(new AddExternalFlowOperation());

		// Refresh work action
		list.add(new RefreshWorkOperation());

		// Flow item operations
		list.add(new CreateFlowItemFromDeskTaskOperation());
		list.add(new ToggleFlowItemPublicOperation());

		// Toggle as parameter
		list.add(new ToggleTaskObjectParameterOperation());
	}

}