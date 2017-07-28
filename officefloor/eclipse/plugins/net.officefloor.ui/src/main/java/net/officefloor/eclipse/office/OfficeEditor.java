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
package net.officefloor.eclipse.office;

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
import net.officefloor.eclipse.office.editparts.AdministrationEditPart;
import net.officefloor.eclipse.office.editparts.AdministrationToOfficeTeamEditPart;
import net.officefloor.eclipse.office.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.ExternalManagedObjectToAdministrationEditPart;
import net.officefloor.eclipse.office.editparts.FunctionAdministrationJoinPointEditPart;
import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.eclipse.office.editparts.OfficeEscalationEditPart;
import net.officefloor.eclipse.office.editparts.OfficeEscalationToOfficeSectionInputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeFunctionEditPart;
import net.officefloor.eclipse.office.editparts.OfficeFunctionToOfficeTeamEditPart;
import net.officefloor.eclipse.office.editparts.OfficeFunctionToPostAdministrationEditPart;
import net.officefloor.eclipse.office.editparts.OfficeFuntionToPreAdministrationEditPart;
import net.officefloor.eclipse.office.editparts.OfficeInputManagedObjectDependencyEditPart;
import net.officefloor.eclipse.office.editparts.OfficeInputManagedObjectDependencyToExternalManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeInputManagedObjectDependencyToOfficeManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectDependencyEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectDependencyToExternalManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectDependencyToOfficeManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectSourceEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectSourceFlowEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectSourceFlowToOfficeSectionInputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectSourceTeamEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectSourceTeamToOfficeTeamEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectToAdministrationEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectToOfficeManagedObjectSourceEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionInputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionObjectToExternalManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionObjectToOfficeManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionOutputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionOutputToOfficeSectionInputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeStartEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSubSectionEditPart;
import net.officefloor.eclipse.office.editparts.OfficeTeamEditPart;
import net.officefloor.eclipse.office.models.PostFunctionAdministrationJointPointModel;
import net.officefloor.eclipse.office.models.PreFunctionAdministrationJointPointModel;
import net.officefloor.eclipse.office.operations.AddAdministratorOperation;
import net.officefloor.eclipse.office.operations.AddExternalManagedObjectOperation;
import net.officefloor.eclipse.office.operations.AddOfficeEscalationOperation;
import net.officefloor.eclipse.office.operations.AddOfficeManagedObjectOperation;
import net.officefloor.eclipse.office.operations.AddOfficeManagedObjectSourceOperation;
import net.officefloor.eclipse.office.operations.AddOfficeSectionOperation;
import net.officefloor.eclipse.office.operations.AddOfficeStartOperation;
import net.officefloor.eclipse.office.operations.AddOfficeTeamOperation;
import net.officefloor.eclipse.office.operations.RefactorOfficeSectionChangeOperation;
import net.officefloor.eclipse.wizard.officefunction.OfficeFunctionInstance;
import net.officefloor.eclipse.wizard.officefunction.OfficeFunctionWizard;
import net.officefloor.model.impl.office.OfficeChangesImpl;
import net.officefloor.model.impl.office.OfficeRepositoryImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministrationModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionToOfficeTeamModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectToAdministrationModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeTeamModel;

/**
 * Editor for the {@link OfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeEditor extends AbstractOfficeFloorEditor<OfficeModel, OfficeChanges> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.office";

	@Override
	protected OfficeModel retrieveModel(ConfigurationItem configuration) throws Exception {
		OfficeModel office = new OfficeModel();
		new OfficeRepositoryImpl(new ModelRepositoryImpl()).retrieveOffice(office, configuration);
		return office;
	}

	@Override
	protected void storeModel(OfficeModel model, WritableConfigurationItem configuration) throws Exception {
		new OfficeRepositoryImpl(new ModelRepositoryImpl()).storeOffice(model, configuration);
	}

	@Override
	protected OfficeChanges createModelChanges(OfficeModel model) {
		return new OfficeChangesImpl(model);
	}

	@Override
	protected void populateEditPartTypes(Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(OfficeModel.class, OfficeEditPart.class);
		map.put(AdministrationModel.class, AdministrationEditPart.class);
		map.put(OfficeTeamModel.class, OfficeTeamEditPart.class);
		map.put(OfficeEscalationModel.class, OfficeEscalationEditPart.class);
		map.put(ExternalManagedObjectModel.class, ExternalManagedObjectEditPart.class);
		map.put(OfficeStartModel.class, OfficeStartEditPart.class);
		map.put(OfficeSectionModel.class, OfficeSectionEditPart.class);
		map.put(OfficeSectionInputModel.class, OfficeSectionInputEditPart.class);
		map.put(OfficeSectionOutputModel.class, OfficeSectionOutputEditPart.class);
		map.put(OfficeSectionObjectModel.class, OfficeSectionObjectEditPart.class);
		map.put(OfficeSubSectionModel.class, OfficeSubSectionEditPart.class);
		map.put(OfficeFunctionModel.class, OfficeFunctionEditPart.class);
		map.put(PreFunctionAdministrationJointPointModel.class, FunctionAdministrationJoinPointEditPart.class);
		map.put(PostFunctionAdministrationJointPointModel.class, FunctionAdministrationJoinPointEditPart.class);
		map.put(OfficeManagedObjectSourceModel.class, OfficeManagedObjectSourceEditPart.class);
		map.put(OfficeManagedObjectSourceFlowModel.class, OfficeManagedObjectSourceFlowEditPart.class);
		map.put(OfficeManagedObjectSourceTeamModel.class, OfficeManagedObjectSourceTeamEditPart.class);
		map.put(OfficeInputManagedObjectDependencyModel.class, OfficeInputManagedObjectDependencyEditPart.class);
		map.put(OfficeManagedObjectModel.class, OfficeManagedObjectEditPart.class);
		map.put(OfficeManagedObjectDependencyModel.class, OfficeManagedObjectDependencyEditPart.class);

		// Connections
		map.put(OfficeSectionObjectToExternalManagedObjectModel.class,
				OfficeSectionObjectToExternalManagedObjectEditPart.class);
		map.put(OfficeSectionObjectToOfficeManagedObjectModel.class,
				OfficeSectionObjectToOfficeManagedObjectEditPart.class);
		map.put(OfficeSectionOutputToOfficeSectionInputModel.class,
				OfficeSectionOutputToOfficeSectionInputEditPart.class);
		map.put(OfficeFunctionToOfficeTeamModel.class, OfficeFunctionToOfficeTeamEditPart.class);
		map.put(AdministrationToOfficeTeamModel.class, AdministrationToOfficeTeamEditPart.class);
		map.put(ExternalManagedObjectToAdministrationModel.class, ExternalManagedObjectToAdministrationEditPart.class);
		map.put(OfficeManagedObjectToAdministrationModel.class, OfficeManagedObjectToAdministrationEditPart.class);
		map.put(OfficeFunctionToPreAdministrationModel.class, OfficeFuntionToPreAdministrationEditPart.class);
		map.put(OfficeFunctionToPostAdministrationModel.class, OfficeFunctionToPostAdministrationEditPart.class);
		map.put(OfficeManagedObjectToOfficeManagedObjectSourceModel.class,
				OfficeManagedObjectToOfficeManagedObjectSourceEditPart.class);
		map.put(OfficeManagedObjectDependencyToOfficeManagedObjectModel.class,
				OfficeManagedObjectDependencyToOfficeManagedObjectEditPart.class);
		map.put(OfficeManagedObjectDependencyToExternalManagedObjectModel.class,
				OfficeManagedObjectDependencyToExternalManagedObjectEditPart.class);
		map.put(OfficeInputManagedObjectDependencyToExternalManagedObjectModel.class,
				OfficeInputManagedObjectDependencyToExternalManagedObjectEditPart.class);
		map.put(OfficeInputManagedObjectDependencyToOfficeManagedObjectModel.class,
				OfficeInputManagedObjectDependencyToOfficeManagedObjectEditPart.class);
		map.put(OfficeManagedObjectSourceFlowToOfficeSectionInputModel.class,
				OfficeManagedObjectSourceFlowToOfficeSectionInputEditPart.class);
		map.put(OfficeManagedObjectSourceTeamToOfficeTeamModel.class,
				OfficeManagedObjectSourceTeamToOfficeTeamEditPart.class);
		map.put(OfficeEscalationToOfficeSectionInputModel.class, OfficeEscalationToOfficeSectionInputEditPart.class);
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the office changes
		OfficeChanges officeChanges = this.getModelChanges();

		// Add model operations
		list.add(new AddOfficeSectionOperation(officeChanges));
		list.add(new AddOfficeTeamOperation(officeChanges));
		list.add(new AddExternalManagedObjectOperation(officeChanges));
		list.add(new AddOfficeManagedObjectSourceOperation(officeChanges));
		list.add(new AddAdministratorOperation(officeChanges));
		list.add(new AddOfficeEscalationOperation(officeChanges));
		list.add(new AddOfficeStartOperation(officeChanges));

		// Add managed object from managed object source
		list.add(new AddOfficeManagedObjectOperation(officeChanges));

		// Office section operations
		list.add(new RefactorOfficeSectionChangeOperation(officeChanges));
	}

	@Override
	protected void populateGraphicalEditPolicy(OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect office section object to external managed object
		policy.addConnection(OfficeSectionObjectModel.class, ExternalManagedObjectModel.class, (source, target,
				request) -> this.getModelChanges().linkOfficeSectionObjectToExternalManagedObject(source, target));

		// Connect office section object to managed object
		policy.addConnection(OfficeSectionObjectModel.class, OfficeManagedObjectModel.class, (source, target,
				request) -> this.getModelChanges().linkOfficeSectionObjectToOfficeManagedObject(source, target));

		// Connect managed object dependency to managed object
		policy.addConnection(OfficeManagedObjectDependencyModel.class, OfficeManagedObjectModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeManagedObjectDependencyToOfficeManagedObject(source, target));

		// Connect managed object dependency to external managed object
		policy.addConnection(OfficeManagedObjectDependencyModel.class, ExternalManagedObjectModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeManagedObjectDependencyToExternalManagedObject(source, target));

		// Connect managed object source flow to office section input
		policy.addConnection(OfficeManagedObjectSourceFlowModel.class, OfficeSectionInputModel.class,
				(source, target, request) -> this.getModelChanges()
						.linkOfficeManagedObjectSourceFlowToOfficeSectionInput(source, target));

		// Connect office section output to office section input
		policy.addConnection(OfficeSectionOutputModel.class, OfficeSectionInputModel.class, (source, target,
				request) -> this.getModelChanges().linkOfficeSectionOutputToOfficeSectionInput(source, target));

		// Connect office section function to office team
		policy.addConnection(OfficeFunctionModel.class, OfficeTeamModel.class,
				(source, target, request) -> this.getModelChanges().linkOfficeFunctionToOfficeTeam(source, target));

		// Connect office managed object source team to office team
		policy.addConnection(OfficeManagedObjectSourceTeamModel.class, OfficeTeamModel.class, (source, target,
				request) -> this.getModelChanges().linkOfficeManagedObjectSourceTeamToOfficeTeam(source, target));

		// Connect administration to office team
		policy.addConnection(AdministrationModel.class, OfficeTeamModel.class,
				(source, target, request) -> this.getModelChanges().linkAdministrationToOfficeTeam(source, target));

		// Connect external managed object to administration
		policy.addConnection(ExternalManagedObjectModel.class, AdministrationModel.class, (source, target,
				request) -> this.getModelChanges().linkExternalManagedObjectToAdministration(source, target));

		// Connect managed object to administration
		policy.addConnection(OfficeManagedObjectModel.class, AdministrationModel.class, (source, target,
				request) -> this.getModelChanges().linkOfficeManagedObjectToAdministration(source, target));

		// Connect function to pre/post administration
		policy.addConnection(AdministrationModel.class, OfficeSectionModel.class, (source, target, request) -> {

			// Obtain the selected function
			OfficeFunctionInstance function = OfficeFunctionWizard.getOfficeFunction(target, OfficeEditor.this);
			if (function == null) {
				return null; // no function, no change
			}

			// Return change to add pre/post administration to function
			if (function.isPreRatherThanPostAdministration()) {
				// Return change to add pre administration
				return OfficeEditor.this.getModelChanges().linkOfficeFunctionToPreAdministration(target,
						function.getOfficeFunctionType(), source);
			} else {
				// Return change to add post administration
				return OfficeEditor.this.getModelChanges().linkOfficeFunctionToPostAdministration(target,
						function.getOfficeFunctionType(), source);
			}

		});

		// Connect escalation to input
		policy.addConnection(OfficeEscalationModel.class, OfficeSectionInputModel.class, (source, target,
				request) -> this.getModelChanges().linkOfficeEscalationToOfficeSectionInput(source, target));

		// Connect start to input
		policy.addConnection(OfficeStartModel.class, OfficeSectionInputModel.class, (source, target, request) -> this
				.getModelChanges().linkOfficeStartToOfficeSectionInput(source, target));
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {

		// Allow deleting the office section
		policy.addDelete(OfficeSectionModel.class, (target) -> this.getModelChanges().removeOfficeSection(target));

		// Allow deleting the office team
		policy.addDelete(OfficeTeamModel.class, (target) -> this.getModelChanges().removeOfficeTeam(target));

		// Allow deleting the office start
		policy.addDelete(OfficeStartModel.class, (target) -> this.getModelChanges().removeOfficeStart(target));

		// Allow deleting the external managed object
		policy.addDelete(ExternalManagedObjectModel.class,
				(target) -> this.getModelChanges().removeExternalManagedObject(target));

		// Allow deleting the managed object source
		policy.addDelete(OfficeManagedObjectSourceModel.class,
				(target) -> this.getModelChanges().removeOfficeManagedObjectSource(target));

		// Allow deleting the managed object
		policy.addDelete(OfficeManagedObjectModel.class,
				(target) -> this.getModelChanges().removeOfficeManagedObject(target));

		// Allow deleting the administrator
		policy.addDelete(AdministrationModel.class, (target) -> this.getModelChanges().removeAdministration(target));

		// Allow deleting the office escalation
		policy.addDelete(OfficeEscalationModel.class,
				(target) -> this.getModelChanges().removeOfficeEscalation(target));

		// Allow deleting office section object to external managed object
		policy.addDelete(OfficeSectionObjectToExternalManagedObjectModel.class,
				(target) -> this.getModelChanges().removeOfficeSectionObjectToExternalManagedObject(target));

		// Allow deleting office section object to managed object
		policy.addDelete(OfficeSectionObjectToOfficeManagedObjectModel.class,
				(target) -> this.getModelChanges().removeOfficeSectionObjectToOfficeManagedObject(target));

		// Allow deleting dependency to managed object
		policy.addDelete(OfficeManagedObjectDependencyToOfficeManagedObjectModel.class,
				(target) -> this.getModelChanges().removeOfficeManagedObjectDependencyToOfficeManagedObject(target));

		// Allow deleting dependency to external managed object
		policy.addDelete(OfficeManagedObjectDependencyToExternalManagedObjectModel.class,
				(target) -> this.getModelChanges().removeOfficeManagedObjectDependencyToExternalManagedObject(target));

		// Allow deleting managed object source flow to office section input
		policy.addDelete(OfficeManagedObjectSourceFlowToOfficeSectionInputModel.class,
				(target) -> this.getModelChanges().removeOfficeManagedObjectSourceFlowToOfficeSectionInput(target));

		// Allow deleting office section output to office section input
		policy.addDelete(OfficeSectionOutputToOfficeSectionInputModel.class,
				(target) -> this.getModelChanges().removeOfficeSectionOutputToOfficeSectionInput(target));

		// Allow deleting office section function to office team
		policy.addDelete(OfficeFunctionToOfficeTeamModel.class,
				(target) -> this.getModelChanges().removeOfficeFunctionToOfficeTeam(target));

		// Allow deleting office managed object source team to office team
		policy.addDelete(OfficeManagedObjectSourceTeamToOfficeTeamModel.class,
				(target) -> this.getModelChanges().removeOfficeManagedObjectSourceTeamToOfficeTeam(target));

		// Allow deleting administrator to team
		policy.addDelete(AdministrationToOfficeTeamModel.class,
				(target) -> this.getModelChanges().removeAdministrationToOfficeTeam(target));

		// Allow deleting external managed object to administrator
		policy.addDelete(ExternalManagedObjectToAdministrationModel.class,
				(target) -> this.getModelChanges().removeExternalManagedObjectToAdministration(target));

		// Allow deleting managed object to administration
		policy.addDelete(OfficeManagedObjectToAdministrationModel.class,
				(target) -> this.getModelChanges().removeOfficeManagedObjectToAdministration(target));

		// Allow deleting function to pre administration
		policy.addDelete(OfficeFunctionToPreAdministrationModel.class,
				(target) -> this.getModelChanges().removeOfficeFunctionToPreAdministration(target));

		// Allow deleting function to post adminitration
		policy.addDelete(OfficeFunctionToPostAdministrationModel.class,
				(target) -> this.getModelChanges().removeOfficeFunctionToPostAdministration(target));

		// Allow deleting escalation to input
		policy.addDelete(OfficeEscalationToOfficeSectionInputModel.class,
				(target) -> this.getModelChanges().removeOfficeEscalationToOfficeSectionInput(target));

		// Allow deleting start to input
		policy.addDelete(OfficeStartToOfficeSectionInputModel.class,
				(target) -> this.getModelChanges().removeOfficeStartToOfficeSectionInput(target));
	}

}