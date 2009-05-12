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
package net.officefloor.eclipse.officefloor;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectDependencyEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceFlowEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceTeamEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeInputEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeTeamEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorTeamEditPart;
import net.officefloor.eclipse.officefloor.operations.AddOfficeFloorManagedObjectSourceOperation;
import net.officefloor.eclipse.officefloor.operations.AddDeployedOfficeOperation;
import net.officefloor.eclipse.officefloor.operations.AddOfficeFloorTeamOperation;
import net.officefloor.model.impl.officefloor.OfficeFloorChangesImpl;
import net.officefloor.model.impl.officefloor.OfficeFloorRepositoryImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.repository.ConfigurationItem;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorEditor extends
		AbstractOfficeFloorEditor<OfficeFloorModel, OfficeFloorChanges> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.officefloor";

	@Override
	protected boolean isDragTarget() {
		return false;
	}

	@Override
	protected OfficeFloorModel retrieveModel(ConfigurationItem configuration)
			throws Exception {
		return new OfficeFloorRepositoryImpl(new ModelRepositoryImpl())
				.retrieveOfficeFloor(configuration);
	}

	@Override
	protected void storeModel(OfficeFloorModel model,
			ConfigurationItem configuration) throws Exception {
		new OfficeFloorRepositoryImpl(new ModelRepositoryImpl())
				.storeOfficeFloor(model, configuration);
	}

	@Override
	protected OfficeFloorChanges createModelChanges(OfficeFloorModel model) {
		return new OfficeFloorChangesImpl(model);
	}

	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(OfficeFloorModel.class, OfficeFloorEditPart.class);
		map.put(OfficeFloorManagedObjectSourceModel.class,
				OfficeFloorManagedObjectSourceEditPart.class);
		map.put(OfficeFloorTeamModel.class, OfficeFloorTeamEditPart.class);
		map.put(DeployedOfficeModel.class, DeployedOfficeEditPart.class);
		map
				.put(DeployedOfficeTeamModel.class,
						DeployedOfficeTeamEditPart.class);
		map.put(DeployedOfficeInputModel.class,
				DeployedOfficeInputEditPart.class);
		map.put(DeployedOfficeObjectModel.class,
				DeployedOfficeObjectEditPart.class);
		map.put(OfficeFloorManagedObjectDependencyModel.class,
				OfficeFloorManagedObjectDependencyEditPart.class);
		map.put(OfficeFloorManagedObjectSourceFlowModel.class,
				OfficeFloorManagedObjectSourceFlowEditPart.class);
		map.put(OfficeFloorManagedObjectSourceTeamModel.class,
				OfficeFloorManagedObjectSourceTeamEditPart.class);

		// Connections
		map.put(OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				AbstractOfficeFloorConnectionEditPart.class);
		map
				.put(
						OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class,
						AbstractOfficeFloorConnectionEditPart.class);
		map.put(OfficeFloorManagedObjectSourceToDeployedOfficeModel.class,
				AbstractOfficeFloorConnectionEditPart.class);
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {
		// TODO populate layout edit policty for the OfficeFloor
	}

	@Override
	protected void populateGraphicalEditPolicy(
			OfficeFloorGraphicalNodeEditPolicy policy) {
		// TODO populate the connection policy for the OfficeFloor
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the office floor changes
		OfficeFloorChanges officeFloorChanges = this.getModelChanges();

		// Add model operations
		list.add(new AddDeployedOfficeOperation(officeFloorChanges));
		list.add(new AddOfficeFloorTeamOperation(officeFloorChanges));
		list.add(new AddOfficeFloorManagedObjectSourceOperation(
				officeFloorChanges));
	}

}