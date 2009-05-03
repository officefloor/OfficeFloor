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
package net.officefloor.eclipse.office;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.OfficeFloorConnectionEditPart;
import net.officefloor.eclipse.office.editparts.AdministratorEditPart;
import net.officefloor.eclipse.office.editparts.DutyEditPart;
import net.officefloor.eclipse.office.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.ExternalTeamEditPart;
import net.officefloor.eclipse.office.editparts.FlowItemAdministrationJoinPointEditPart;
import net.officefloor.eclipse.office.editparts.FlowItemEditPart;
import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.eclipse.office.editparts.RoomEditPart;
import net.officefloor.eclipse.office.models.PostFlowItemAdministrationJointPointModel;
import net.officefloor.eclipse.office.models.PreFlowItemAdministrationJointPointModel;
import net.officefloor.eclipse.office.operations.AddAdministratorOperation;
import net.officefloor.eclipse.office.operations.AddExternalTeamOperation;
import net.officefloor.eclipse.office.operations.CycleManagedObjectScopeOperation;
import net.officefloor.eclipse.office.operations.RefreshOfficeRoomOperation;
import net.officefloor.model.impl.office.OfficeRepositoryImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.repository.ConfigurationItem;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeEditor extends
		AbstractOfficeFloorEditor<OfficeModel, OfficeEditPart> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.office";

	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(OfficeModel.class, OfficeEditPart.class);
		map.put(OfficeTeamModel.class, ExternalTeamEditPart.class);
		map.put(ExternalManagedObjectModel.class,
				ExternalManagedObjectEditPart.class);
		map.put(OfficeSectionModel.class, RoomEditPart.class);
		map.put(OfficeTaskModel.class, FlowItemEditPart.class);
		map.put(AdministratorModel.class, AdministratorEditPart.class);
		map.put(DutyModel.class, DutyEditPart.class);
		map.put(PreFlowItemAdministrationJointPointModel.class,
				FlowItemAdministrationJoinPointEditPart.class);
		map.put(PostFlowItemAdministrationJointPointModel.class,
				FlowItemAdministrationJoinPointEditPart.class);

		// Connections
		map.put(OfficeSectionOutputToOfficeSectionInputModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(OfficeSectionObjectToExternalManagedObjectModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(OfficeSectionResponsibilityToOfficeTeamModel.class,
				OfficeFloorConnectionEditPart.class);
	}

	@Override
	protected boolean isDragTarget() {
		return false;
	}

	@Override
	protected OfficeModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		return new OfficeRepositoryImpl(new ModelRepositoryImpl())
				.retrieveOffice(configuration);
	}

	@Override
	protected void storeModel(OfficeModel model, ConfigurationItem configuration)
			throws Exception {
		new OfficeRepositoryImpl(new ModelRepositoryImpl()).storeOffice(model,
				configuration);
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Add model operations
		list.add(new AddAdministratorOperation());
		list.add(new AddExternalTeamOperation());
		list.add(new CycleManagedObjectScopeOperation());

		// Refresh model operations
		list.add(new RefreshOfficeRoomOperation());
	}
}