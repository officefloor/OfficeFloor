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
package net.officefloor.eclipse.room;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.OfficeFloorConnectionEditPart;
import net.officefloor.eclipse.room.editparts.ExternalFlowEditPart;
import net.officefloor.eclipse.room.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.room.editparts.RoomEditPart;
import net.officefloor.eclipse.room.editparts.SubRoomEditPart;
import net.officefloor.eclipse.room.editparts.SubRoomInputFlowEditPart;
import net.officefloor.eclipse.room.editparts.SubRoomManagedObjectEditPart;
import net.officefloor.eclipse.room.editparts.SubRoomOutputFlowEditPart;
import net.officefloor.eclipse.room.operations.AddExternalFlowOperation;
import net.officefloor.eclipse.room.operations.AddExternalManagedObjectOperation;
import net.officefloor.eclipse.room.operations.AddSubRoomOperation;
import net.officefloor.eclipse.room.operations.RefreshSubRoomOperation;
import net.officefloor.eclipse.room.operations.ToggleSubRoomInputFlowPublicOperation;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.section.SectionRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link SectionModel}.
 * 
 * @author Daniel
 */
// TODO rename to SectionEditor
public class RoomEditor extends
		AbstractOfficeFloorEditor<SectionModel, RoomEditPart> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.section";

	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {
		// Entities
		map.put(SectionModel.class, RoomEditPart.class);
		map.put(ExternalManagedObjectModel.class,
				ExternalManagedObjectEditPart.class);
		map.put(ExternalFlowModel.class, ExternalFlowEditPart.class);
		map.put(SubSectionModel.class, SubRoomEditPart.class);
		map.put(SubSectionInputModel.class, SubRoomInputFlowEditPart.class);
		map.put(SubSectionOutputModel.class, SubRoomOutputFlowEditPart.class);
		map
				.put(SubSectionObjectModel.class,
						SubRoomManagedObjectEditPart.class);

		// Connections
		map.put(SubSectionObjectToExternalManagedObjectModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(SubSectionOutputToExternalFlowModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(SubSectionOutputToSubSectionInputModel.class,
				OfficeFloorConnectionEditPart.class);
	}

	@Override
	protected boolean isDragTarget() {
		// Disallow as drag target
		return false;
	}

	@Override
	protected SectionModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		return new SectionRepositoryImpl(new ModelRepositoryImpl())
				.retrieveSection(configuration);
	}

	@Override
	protected void storeModel(SectionModel model,
			ConfigurationItem configuration) throws Exception {
		new SectionRepositoryImpl(new ModelRepositoryImpl()).storeSection(
				model, configuration);
	}

	@Override
	protected void populateOperations(List<Operation> list) {
		// Add model add operations
		list.add(new AddSubRoomOperation());
		list.add(new AddExternalManagedObjectOperation());
		list.add(new AddExternalFlowOperation());

		// Add refresh operations
		list.add(new RefreshSubRoomOperation());

		// Sub room input flow operations
		list.add(new ToggleSubRoomInputFlowPublicOperation());
	}

}