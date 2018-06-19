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
package net.officefloor.eclipse.officefloor;

import java.util.List;
import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.IEditorPart;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeInputEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeObjectToOfficeFloorInputManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeObjectToOfficeFloorManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeTeamEditPart;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeTeamToOfficeFloorTeamEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorInputManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectDependencyEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceFlowEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceInputDependencyEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceTeamEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceToDeployedOfficeEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceEditPart;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorTeamEditPart;
import net.officefloor.eclipse.officefloor.operations.AddDeployedOfficeOperation;
import net.officefloor.eclipse.officefloor.operations.AddOfficeFloorInputManagedObjectOperation;
import net.officefloor.eclipse.officefloor.operations.AddOfficeFloorManagedObjectOperation;
import net.officefloor.eclipse.officefloor.operations.AddOfficeFloorManagedObjectSourceOperation;
import net.officefloor.eclipse.officefloor.operations.AddOfficeFloorTeamOperation;
import net.officefloor.eclipse.officefloor.operations.RefactorDeployedOfficeFloorChangeOperation;
import net.officefloor.eclipse.officefloor.operations.RescopeOfficeFloorManagedObjectOperation;
import net.officefloor.model.impl.officefloor.OfficeFloorChangesImpl;
import net.officefloor.model.impl.officefloor.OfficeFloorRepositoryImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;

/**
 * Editor for the {@link OfficeFloorModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorEditor extends AbstractOfficeFloorEditor<OfficeFloorModel, OfficeFloorChanges> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.officefloor";

	@Override
	protected OfficeFloorModel retrieveModel(ConfigurationItem configuration) throws Exception {
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		new OfficeFloorRepositoryImpl(new ModelRepositoryImpl()).retrieveOfficeFloor(officeFloor, configuration);
		return officeFloor;
	}

	@Override
	protected void storeModel(OfficeFloorModel model, WritableConfigurationItem configuration) throws Exception {
		new OfficeFloorRepositoryImpl(new ModelRepositoryImpl()).storeOfficeFloor(model, configuration);
	}

	@Override
	protected OfficeFloorChanges createModelChanges(OfficeFloorModel model) {
		return new OfficeFloorChangesImpl(model);
	}

	@Override
	protected void populateEditPartTypes(Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(OfficeFloorModel.class, OfficeFloorEditPart.class);
		map.put(OfficeFloorManagedObjectSourceModel.class, OfficeFloorManagedObjectSourceEditPart.class);
		map.put(OfficeFloorManagedObjectSourceFlowModel.class, OfficeFloorManagedObjectSourceFlowEditPart.class);
		map.put(OfficeFloorManagedObjectSourceTeamModel.class, OfficeFloorManagedObjectSourceTeamEditPart.class);
		map.put(OfficeFloorManagedObjectSourceInputDependencyModel.class,
				OfficeFloorManagedObjectSourceInputDependencyEditPart.class);
		map.put(OfficeFloorInputManagedObjectModel.class, OfficeFloorInputManagedObjectEditPart.class);
		map.put(OfficeFloorManagedObjectModel.class, OfficeFloorManagedObjectEditPart.class);
		map.put(OfficeFloorManagedObjectDependencyModel.class, OfficeFloorManagedObjectDependencyEditPart.class);
		map.put(DeployedOfficeModel.class, DeployedOfficeEditPart.class);
		map.put(DeployedOfficeInputModel.class, DeployedOfficeInputEditPart.class);
		map.put(DeployedOfficeObjectModel.class, DeployedOfficeObjectEditPart.class);
		map.put(DeployedOfficeTeamModel.class, DeployedOfficeTeamEditPart.class);
		map.put(OfficeFloorTeamModel.class, OfficeFloorTeamEditPart.class);

		// Connections
		map.put(DeployedOfficeObjectToOfficeFloorManagedObjectModel.class,
				DeployedOfficeObjectToOfficeFloorManagedObjectEditPart.class);
		map.put(DeployedOfficeObjectToOfficeFloorInputManagedObjectModel.class,
				DeployedOfficeObjectToOfficeFloorInputManagedObjectEditPart.class);
		map.put(DeployedOfficeTeamToOfficeFloorTeamModel.class, DeployedOfficeTeamToOfficeFloorTeamEditPart.class);
		map.put(OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel.class,
				OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceEditPart.class);
		map.put(OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel.class,
				OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectEditPart.class);
		map.put(OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel.class,
				OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectEditPart.class);
		map.put(OfficeFloorManagedObjectSourceToDeployedOfficeModel.class,
				OfficeFloorManagedObjectSourceToDeployedOfficeEditPart.class);
		map.put(OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel.class,
				OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectEditPart.class);
		map.put(OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel.class,
				OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceEditPart.class);
		map.put(OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class,
				OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputEditPart.class);
		map.put(OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamEditPart.class);
		map.put(OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel.class,
				OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectEditPart.class);
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the OfficeFloor changes
		OfficeFloorChanges officeFloorChanges = this.getModelChanges();

		// Add model operations
		list.add(new AddDeployedOfficeOperation(officeFloorChanges));
		list.add(new AddOfficeFloorManagedObjectSourceOperation(officeFloorChanges));
		list.add(new AddOfficeFloorInputManagedObjectOperation(officeFloorChanges));
		list.add(new AddOfficeFloorTeamOperation(officeFloorChanges));

		// Further add managed object from managed object source
		list.add(new AddOfficeFloorManagedObjectOperation(officeFloorChanges));

		// Configure operations
		list.add(new RefactorDeployedOfficeFloorChangeOperation(officeFloorChanges));
		list.add(new RescopeOfficeFloorManagedObjectOperation(officeFloorChanges));
	}

	@Override
	protected void populateGraphicalEditPolicy(OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect deployed office object to managed object
		policy.addConnection(DeployedOfficeObjectModel.class, OfficeFloorManagedObjectModel.class, (source, target,
				request) -> this.getModelChanges().linkDeployedOfficeObjectToOfficeFloorManagedObject(source, target));

		// Connect deployed office object to input managed object
		policy.addConnection(DeployedOfficeObjectModel.class, OfficeFloorInputManagedObjectModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkDeployedOfficeObjectToOfficeFloorInputManagedObject(source, target));

		// Connect deployed office team to office floor team
		policy.addConnection(DeployedOfficeTeamModel.class, OfficeFloorTeamModel.class, (source, target,
				request) -> this.getModelChanges().linkDeployedOfficeTeamToOfficeFloorTeam(source, target));

		// Connect managed object dependency to managed object
		policy.addConnection(OfficeFloorManagedObjectDependencyModel.class, OfficeFloorManagedObjectModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeFloorManagedObjectDependencyToOfficeFloorManagedObject(source, target));

		// Connect managed object dependency to input managed object
		policy.addConnection(OfficeFloorManagedObjectDependencyModel.class, OfficeFloorInputManagedObjectModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObject(source, target));

		// Connect managed object source to deployed office
		policy.addConnection(OfficeFloorManagedObjectSourceModel.class, DeployedOfficeModel.class, (source, target,
				request) -> this.getModelChanges().linkOfficeFloorManagedObjectSourceToDeployedOffice(source, target));

		// Connect managed object source flow to office input
		policy.addConnection(OfficeFloorManagedObjectSourceFlowModel.class, DeployedOfficeInputModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeFloorManagedObjectSourceFlowToDeployedOfficeInput(source, target));

		// Connect managed object source team to office team
		policy.addConnection(OfficeFloorManagedObjectSourceTeamModel.class, OfficeFloorTeamModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam(source, target));

		// Connect managed object source to input managed object
		policy.addConnection(OfficeFloorManagedObjectSourceModel.class, OfficeFloorInputManagedObjectModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObject(source, target));

		// Connect input managed object to bound managed object source
		policy.addConnection(OfficeFloorInputManagedObjectModel.class, OfficeFloorManagedObjectSourceModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSource(source, target));
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {

		// Allow deleting the office floor team
		policy.addDelete(OfficeFloorTeamModel.class, (target) -> this.getModelChanges().removeOfficeFloorTeam(target));

		// Allow deleting the deployed office
		policy.addDelete(DeployedOfficeModel.class, (target) -> this.getModelChanges().removeDeployedOffice(target));

		// Allow deleting the managed object source
		policy.addDelete(OfficeFloorManagedObjectSourceModel.class,
				(target) -> this.getModelChanges().removeOfficeFloorManagedObjectSource(target));

		// Allow deleting the input managed object
		policy.addDelete(OfficeFloorInputManagedObjectModel.class,
				(target) -> this.getModelChanges().removeOfficeFloorInputManagedObject(target));

		// Allow deleting the managed object
		policy.addDelete(OfficeFloorManagedObjectModel.class,
				(target) -> this.getModelChanges().removeOfficeFloorManagedObject(target));

		// Allow deleting deployed office object to managed object
		policy.addDelete(DeployedOfficeObjectToOfficeFloorManagedObjectModel.class,
				(target) -> this.getModelChanges().removeDeployedOfficeObjectToOfficeFloorManagedObject(target));

		// All deleting deployed office object to input managed object
		policy.addDelete(DeployedOfficeObjectToOfficeFloorInputManagedObjectModel.class,
				(target) -> this.getModelChanges().removeDeployedOfficeObjectToOfficeFloorInputManagedObject(target));

		// Allow deleting deployed office team to office floor team
		policy.addDelete(DeployedOfficeTeamToOfficeFloorTeamModel.class,
				(target) -> this.getModelChanges().removeDeployedOfficeTeamToOfficeFloorTeam(target));

		// Allow deleting managed object dependency to managed object
		policy.addDelete(OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel.class, (target) -> this
				.getModelChanges().removeOfficeFloorManagedObjectDependencyToOfficeFloorManagedObject(target));

		// Allow deleting managed object dependency to input managed object
		policy.addDelete(OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel.class, (target) -> this
				.getModelChanges().removeOfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObject(target));

		// Allow deleting managed object source to deployed office
		policy.addDelete(OfficeFloorManagedObjectSourceToDeployedOfficeModel.class,
				(target) -> this.getModelChanges().removeOfficeFloorManagedObjectSourceToDeployedOffice(target));

		// Allow deleting managed object source flow to office input
		policy.addDelete(OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class, (target) -> this
				.getModelChanges().removeOfficeFloorManagedObjectSourceFlowToDeployedOfficeInput(target));

		// Allow deleting managed object source team to office team
		policy.addDelete(OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel.class,
				(target) -> this.getModelChanges().removeOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam(target));

		// Allow deleting managed object source to input managed object
		policy.addDelete(OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel.class, (target) -> this
				.getModelChanges().removeOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObject(target));

		// Allow deleting input managed object to bound managed object source
		policy.addDelete(OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel.class, (target) -> this
				.getModelChanges().removeOfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSource(target));
	}

}