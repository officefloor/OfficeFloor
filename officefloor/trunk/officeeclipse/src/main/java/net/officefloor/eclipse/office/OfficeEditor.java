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
import net.officefloor.eclipse.office.editparts.DeskEditPart;
import net.officefloor.eclipse.office.editparts.DutyEditPart;
import net.officefloor.eclipse.office.editparts.DutyFlowEditPart;
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
import net.officefloor.eclipse.office.operations.ManageOfficeRoomOperation;
import net.officefloor.eclipse.office.operations.RefreshOfficeRoomOperation;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToManagedObjectModel;
import net.officefloor.model.office.AdministratorToTeamModel;
import net.officefloor.model.office.DutyFlowModel;
import net.officefloor.model.office.DutyFlowToFlowItemModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.FlowItemToPostAdministratorDutyModel;
import net.officefloor.model.office.FlowItemToPreAdministratorDutyModel;
import net.officefloor.model.office.FlowItemToTeamModel;
import net.officefloor.model.office.OfficeDeskModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.office.OfficeLoader;
import net.officefloor.repository.ConfigurationItem;

import org.eclipse.gef.EditPart;

/**
 * Editor for the {@link net.officefloor.model.office.OfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeEditor extends
		AbstractOfficeFloorEditor<OfficeModel, OfficeEditPart> {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.AbstractOfficeFloorEditor#
	 * populateEditPartTypes(java.util.Map)
	 */
	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(OfficeModel.class, OfficeEditPart.class);
		map.put(ExternalTeamModel.class, ExternalTeamEditPart.class);
		map.put(ExternalManagedObjectModel.class,
				ExternalManagedObjectEditPart.class);
		map.put(OfficeRoomModel.class, RoomEditPart.class);
		map.put(OfficeDeskModel.class, DeskEditPart.class);
		map.put(FlowItemModel.class, FlowItemEditPart.class);
		map.put(AdministratorModel.class, AdministratorEditPart.class);
		map.put(DutyModel.class, DutyEditPart.class);
		map.put(DutyFlowModel.class, DutyFlowEditPart.class);
		map.put(PreFlowItemAdministrationJointPointModel.class,
				FlowItemAdministrationJoinPointEditPart.class);
		map.put(PostFlowItemAdministrationJointPointModel.class,
				FlowItemAdministrationJoinPointEditPart.class);

		// Connections
		map.put(FlowItemToTeamModel.class, OfficeFloorConnectionEditPart.class);
		map.put(AdministratorToManagedObjectModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(AdministratorToTeamModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(FlowItemToPreAdministratorDutyModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(FlowItemToPostAdministratorDutyModel.class,
				OfficeFloorConnectionEditPart.class);
		map.put(DutyFlowToFlowItemModel.class,
				OfficeFloorConnectionEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.AbstractOfficeFloorEditor#isDragTarget()
	 */
	@Override
	protected boolean isDragTarget() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.AbstractOfficeFloorEditor#retrieveModel
	 * (net.officefloor.repository.ConfigurationItem)
	 */
	@Override
	protected OfficeModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		return this.getOfficeLoader().loadOffice(configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.AbstractOfficeFloorEditor#storeModel(T,
	 * net.officefloor.repository.ConfigurationItem)
	 */
	@Override
	protected void storeModel(OfficeModel model, ConfigurationItem configuration)
			throws Exception {
		this.getOfficeLoader().storeOffice(model, configuration);
	}

	/**
	 * Obtains the {@link OfficeLoader}.
	 * 
	 * @return {@link OfficeLoader}.
	 */
	private OfficeLoader getOfficeLoader() {
		return new OfficeLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.AbstractOfficeFloorEditor#populateOperations
	 * (java.util.List)
	 */
	@Override
	protected void populateOperations(List<Operation> list) {

		// Add model operations
		list.add(new AddAdministratorOperation());
		list.add(new AddExternalTeamOperation());
		list.add(new ManageOfficeRoomOperation(this.getRootEditPart()));
		list.add(new CycleManagedObjectScopeOperation());

		// Refresh model operations
		list.add(new RefreshOfficeRoomOperation());
	}
}
