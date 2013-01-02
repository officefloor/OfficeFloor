/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionChangeFactory;
import net.officefloor.eclipse.common.editpolicies.connection.OfficeFloorGraphicalNodeEditPolicy;
import net.officefloor.eclipse.common.editpolicies.layout.DeleteChangeFactory;
import net.officefloor.eclipse.common.editpolicies.layout.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.office.editparts.AdministratorEditPart;
import net.officefloor.eclipse.office.editparts.AdministratorToOfficeTeamEditPart;
import net.officefloor.eclipse.office.editparts.DutyEditPart;
import net.officefloor.eclipse.office.editparts.ExternalManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.ExternalManagedObjectToAdministratorEditPart;
import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.eclipse.office.editparts.OfficeEscalationEditPart;
import net.officefloor.eclipse.office.editparts.OfficeEscalationToOfficeSectionInputEditPart;
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
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectToAdministratorEditPart;
import net.officefloor.eclipse.office.editparts.OfficeManagedObjectToOfficeManagedObjectSourceEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionInputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionObjectToExternalManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionObjectToOfficeManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionOutputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionOutputToOfficeSectionInputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionResponsibilityEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionResponsibilityToOfficeTeamEditPart;
import net.officefloor.eclipse.office.editparts.OfficeStartEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSubSectionEditPart;
import net.officefloor.eclipse.office.editparts.OfficeTaskEditPart;
import net.officefloor.eclipse.office.editparts.OfficeTaskToPostDutyEditPart;
import net.officefloor.eclipse.office.editparts.OfficeTaskToPreDutyEditPart;
import net.officefloor.eclipse.office.editparts.OfficeTeamEditPart;
import net.officefloor.eclipse.office.editparts.TaskAdministrationJoinPointEditPart;
import net.officefloor.eclipse.office.models.PostTaskAdministrationJointPointModel;
import net.officefloor.eclipse.office.models.PreTaskAdministrationJointPointModel;
import net.officefloor.eclipse.office.operations.AddAdministratorOperation;
import net.officefloor.eclipse.office.operations.AddExternalManagedObjectOperation;
import net.officefloor.eclipse.office.operations.AddOfficeEscalationOperation;
import net.officefloor.eclipse.office.operations.AddOfficeManagedObjectOperation;
import net.officefloor.eclipse.office.operations.AddOfficeManagedObjectSourceOperation;
import net.officefloor.eclipse.office.operations.AddOfficeSectionOperation;
import net.officefloor.eclipse.office.operations.AddOfficeSectionResponsibilityOperation;
import net.officefloor.eclipse.office.operations.AddOfficeStartOperation;
import net.officefloor.eclipse.office.operations.AddOfficeTeamOperation;
import net.officefloor.eclipse.office.operations.RefactorOfficeSectionChangeOperation;
import net.officefloor.eclipse.wizard.officetask.OfficeTaskInstance;
import net.officefloor.eclipse.wizard.officetask.OfficeTaskWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.office.OfficeChangesImpl;
import net.officefloor.model.impl.office.OfficeRepositoryImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
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
import net.officefloor.model.office.OfficeManagedObjectToAdministratorModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTaskToPostDutyModel;
import net.officefloor.model.office.OfficeTaskToPreDutyModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.repository.ConfigurationItem;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.ui.IEditorPart;

/**
 * Editor for the {@link OfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeEditor extends
		AbstractOfficeFloorEditor<OfficeModel, OfficeChanges> {

	/**
	 * ID for this {@link IEditorPart}.
	 */
	public static final String EDITOR_ID = "net.officefloor.editors.office";

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
	protected OfficeChanges createModelChanges(OfficeModel model) {
		return new OfficeChangesImpl(model);
	}

	@Override
	protected void populateEditPartTypes(
			Map<Class<?>, Class<? extends EditPart>> map) {

		// Entities
		map.put(OfficeModel.class, OfficeEditPart.class);
		map.put(AdministratorModel.class, AdministratorEditPart.class);
		map.put(DutyModel.class, DutyEditPart.class);
		map.put(OfficeTeamModel.class, OfficeTeamEditPart.class);
		map.put(OfficeEscalationModel.class, OfficeEscalationEditPart.class);
		map.put(ExternalManagedObjectModel.class,
				ExternalManagedObjectEditPart.class);
		map.put(OfficeStartModel.class, OfficeStartEditPart.class);
		map.put(OfficeSectionModel.class, OfficeSectionEditPart.class);
		map.put(OfficeSectionInputModel.class, OfficeSectionInputEditPart.class);
		map.put(OfficeSectionOutputModel.class,
				OfficeSectionOutputEditPart.class);
		map.put(OfficeSectionObjectModel.class,
				OfficeSectionObjectEditPart.class);
		map.put(OfficeSectionResponsibilityModel.class,
				OfficeSectionResponsibilityEditPart.class);
		map.put(OfficeSubSectionModel.class, OfficeSubSectionEditPart.class);
		map.put(OfficeTaskModel.class, OfficeTaskEditPart.class);
		map.put(PreTaskAdministrationJointPointModel.class,
				TaskAdministrationJoinPointEditPart.class);
		map.put(PostTaskAdministrationJointPointModel.class,
				TaskAdministrationJoinPointEditPart.class);
		map.put(OfficeManagedObjectSourceModel.class,
				OfficeManagedObjectSourceEditPart.class);
		map.put(OfficeManagedObjectSourceFlowModel.class,
				OfficeManagedObjectSourceFlowEditPart.class);
		map.put(OfficeManagedObjectSourceTeamModel.class,
				OfficeManagedObjectSourceTeamEditPart.class);
		map.put(OfficeInputManagedObjectDependencyModel.class,
				OfficeInputManagedObjectDependencyEditPart.class);
		map.put(OfficeManagedObjectModel.class,
				OfficeManagedObjectEditPart.class);
		map.put(OfficeManagedObjectDependencyModel.class,
				OfficeManagedObjectDependencyEditPart.class);

		// Connections
		map.put(OfficeSectionObjectToExternalManagedObjectModel.class,
				OfficeSectionObjectToExternalManagedObjectEditPart.class);
		map.put(OfficeSectionObjectToOfficeManagedObjectModel.class,
				OfficeSectionObjectToOfficeManagedObjectEditPart.class);
		map.put(OfficeSectionOutputToOfficeSectionInputModel.class,
				OfficeSectionOutputToOfficeSectionInputEditPart.class);
		map.put(OfficeSectionResponsibilityToOfficeTeamModel.class,
				OfficeSectionResponsibilityToOfficeTeamEditPart.class);
		map.put(AdministratorToOfficeTeamModel.class,
				AdministratorToOfficeTeamEditPart.class);
		map.put(ExternalManagedObjectToAdministratorModel.class,
				ExternalManagedObjectToAdministratorEditPart.class);
		map.put(OfficeManagedObjectToAdministratorModel.class,
				OfficeManagedObjectToAdministratorEditPart.class);
		map.put(OfficeTaskToPreDutyModel.class,
				OfficeTaskToPreDutyEditPart.class);
		map.put(OfficeTaskToPostDutyModel.class,
				OfficeTaskToPostDutyEditPart.class);
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
		map.put(OfficeEscalationToOfficeSectionInputModel.class,
				OfficeEscalationToOfficeSectionInputEditPart.class);
	}

	@Override
	protected void populateLayoutEditPolicy(OfficeFloorLayoutEditPolicy policy) {

		// Allow deleting the office section
		policy.addDelete(OfficeSectionModel.class,
				new DeleteChangeFactory<OfficeSectionModel>() {
					@Override
					public Change<OfficeSectionModel> createChange(
							OfficeSectionModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeSection(target);
					}
				});

		// Allow deleting the office team
		policy.addDelete(OfficeTeamModel.class,
				new DeleteChangeFactory<OfficeTeamModel>() {
					@Override
					public Change<OfficeTeamModel> createChange(
							OfficeTeamModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeTeam(target);
					}
				});

		// Allow deleting the office start
		policy.addDelete(OfficeStartModel.class,
				new DeleteChangeFactory<OfficeStartModel>() {
					@Override
					public Change<OfficeStartModel> createChange(
							OfficeStartModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeStart(target);
					}
				});

		// Allow deleting the external managed object
		policy.addDelete(ExternalManagedObjectModel.class,
				new DeleteChangeFactory<ExternalManagedObjectModel>() {
					@Override
					public Change<ExternalManagedObjectModel> createChange(
							ExternalManagedObjectModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeExternalManagedObject(target);
					}
				});

		// Allow deleting the managed object source
		policy.addDelete(OfficeManagedObjectSourceModel.class,
				new DeleteChangeFactory<OfficeManagedObjectSourceModel>() {
					@Override
					public Change<OfficeManagedObjectSourceModel> createChange(
							OfficeManagedObjectSourceModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeManagedObjectSource(target);
					}
				});

		// Allow deleting the managed object
		policy.addDelete(OfficeManagedObjectModel.class,
				new DeleteChangeFactory<OfficeManagedObjectModel>() {
					@Override
					public Change<OfficeManagedObjectModel> createChange(
							OfficeManagedObjectModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeManagedObject(target);
					}
				});

		// Allow deleting the administrator
		policy.addDelete(AdministratorModel.class,
				new DeleteChangeFactory<AdministratorModel>() {
					@Override
					public Change<AdministratorModel> createChange(
							AdministratorModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeAdministrator(target);
					}
				});

		// Allow deleting the office escalation
		policy.addDelete(OfficeEscalationModel.class,
				new DeleteChangeFactory<OfficeEscalationModel>() {
					@Override
					public Change<OfficeEscalationModel> createChange(
							OfficeEscalationModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeEscalation(target);
					}
				});

		// Allow deleting the office section responsibility
		policy.addDelete(OfficeSectionResponsibilityModel.class,
				new DeleteChangeFactory<OfficeSectionResponsibilityModel>() {
					@Override
					public Change<OfficeSectionResponsibilityModel> createChange(
							OfficeSectionResponsibilityModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeSectionResponsibility(target);
					}
				});

		// Allow deleting office section object to external managed object
		policy.addDelete(
				OfficeSectionObjectToExternalManagedObjectModel.class,
				new DeleteChangeFactory<OfficeSectionObjectToExternalManagedObjectModel>() {
					@Override
					public Change<OfficeSectionObjectToExternalManagedObjectModel> createChange(
							OfficeSectionObjectToExternalManagedObjectModel target) {
						return OfficeEditor.this
								.getModelChanges()
								.removeOfficeSectionObjectToExternalManagedObject(
										target);
					}
				});

		// Allow deleting office section object to managed object
		policy.addDelete(
				OfficeSectionObjectToOfficeManagedObjectModel.class,
				new DeleteChangeFactory<OfficeSectionObjectToOfficeManagedObjectModel>() {
					@Override
					public Change<OfficeSectionObjectToOfficeManagedObjectModel> createChange(
							OfficeSectionObjectToOfficeManagedObjectModel target) {
						return OfficeEditor.this
								.getModelChanges()
								.removeOfficeSectionObjectToOfficeManagedObject(
										target);
					}
				});

		// Allow deleting dependency to managed object
		policy.addDelete(
				OfficeManagedObjectDependencyToOfficeManagedObjectModel.class,
				new DeleteChangeFactory<OfficeManagedObjectDependencyToOfficeManagedObjectModel>() {
					@Override
					public Change<OfficeManagedObjectDependencyToOfficeManagedObjectModel> createChange(
							OfficeManagedObjectDependencyToOfficeManagedObjectModel target) {
						return OfficeEditor.this
								.getModelChanges()
								.removeOfficeManagedObjectDependencyToOfficeManagedObject(
										target);
					}
				});

		// Allow deleting dependency to external managed object
		policy.addDelete(
				OfficeManagedObjectDependencyToExternalManagedObjectModel.class,
				new DeleteChangeFactory<OfficeManagedObjectDependencyToExternalManagedObjectModel>() {
					@Override
					public Change<OfficeManagedObjectDependencyToExternalManagedObjectModel> createChange(
							OfficeManagedObjectDependencyToExternalManagedObjectModel target) {
						return OfficeEditor.this
								.getModelChanges()
								.removeOfficeManagedObjectDependencyToExternalManagedObject(
										target);
					}
				});

		// Allow deleting managed object source flow to office section input
		policy.addDelete(
				OfficeManagedObjectSourceFlowToOfficeSectionInputModel.class,
				new DeleteChangeFactory<OfficeManagedObjectSourceFlowToOfficeSectionInputModel>() {
					@Override
					public Change<OfficeManagedObjectSourceFlowToOfficeSectionInputModel> createChange(
							OfficeManagedObjectSourceFlowToOfficeSectionInputModel target) {
						return OfficeEditor.this
								.getModelChanges()
								.removeOfficeManagedObjectSourceFlowToOfficeSectionInput(
										target);
					}
				});

		// Allow deleting office section output to office section input
		policy.addDelete(
				OfficeSectionOutputToOfficeSectionInputModel.class,
				new DeleteChangeFactory<OfficeSectionOutputToOfficeSectionInputModel>() {
					@Override
					public Change<OfficeSectionOutputToOfficeSectionInputModel> createChange(
							OfficeSectionOutputToOfficeSectionInputModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeSectionOutputToOfficeSectionInput(
										target);
					}
				});

		// Allow deleting office section responsibility to office team
		policy.addDelete(
				OfficeSectionResponsibilityToOfficeTeamModel.class,
				new DeleteChangeFactory<OfficeSectionResponsibilityToOfficeTeamModel>() {
					@Override
					public Change<OfficeSectionResponsibilityToOfficeTeamModel> createChange(
							OfficeSectionResponsibilityToOfficeTeamModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeSectionResponsibilityToOfficeTeam(
										target);
					}
				});

		// Allow deleting office managed object source team to office team
		policy.addDelete(
				OfficeManagedObjectSourceTeamToOfficeTeamModel.class,
				new DeleteChangeFactory<OfficeManagedObjectSourceTeamToOfficeTeamModel>() {
					@Override
					public Change<OfficeManagedObjectSourceTeamToOfficeTeamModel> createChange(
							OfficeManagedObjectSourceTeamToOfficeTeamModel target) {
						return OfficeEditor.this
								.getModelChanges()
								.removeOfficeManagedObjectSourceTeamToOfficeTeam(
										target);
					}
				});

		// Allow deleting administrator to team
		policy.addDelete(AdministratorToOfficeTeamModel.class,
				new DeleteChangeFactory<AdministratorToOfficeTeamModel>() {
					@Override
					public Change<AdministratorToOfficeTeamModel> createChange(
							AdministratorToOfficeTeamModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeAdministratorToOfficeTeam(target);
					}
				});

		// Allow deleting external managed object to administrator
		policy.addDelete(
				ExternalManagedObjectToAdministratorModel.class,
				new DeleteChangeFactory<ExternalManagedObjectToAdministratorModel>() {
					@Override
					public Change<ExternalManagedObjectToAdministratorModel> createChange(
							ExternalManagedObjectToAdministratorModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeExternalManagedObjectToAdministrator(
										target);
					}
				});

		// Allow deleting managed object to administrator
		policy.addDelete(
				OfficeManagedObjectToAdministratorModel.class,
				new DeleteChangeFactory<OfficeManagedObjectToAdministratorModel>() {
					@Override
					public Change<OfficeManagedObjectToAdministratorModel> createChange(
							OfficeManagedObjectToAdministratorModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeManagedObjectToAdministrator(
										target);
					}
				});

		// Allow deleting task to pre duty
		policy.addDelete(OfficeTaskToPreDutyModel.class,
				new DeleteChangeFactory<OfficeTaskToPreDutyModel>() {
					@Override
					public Change<OfficeTaskToPreDutyModel> createChange(
							OfficeTaskToPreDutyModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeTaskToPreDuty(target);
					}
				});

		// Allow deleting task to post duty
		policy.addDelete(OfficeTaskToPostDutyModel.class,
				new DeleteChangeFactory<OfficeTaskToPostDutyModel>() {
					@Override
					public Change<OfficeTaskToPostDutyModel> createChange(
							OfficeTaskToPostDutyModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeTaskToPostDuty(target);
					}
				});

		// Allow deleting escalation to input
		policy.addDelete(
				OfficeEscalationToOfficeSectionInputModel.class,
				new DeleteChangeFactory<OfficeEscalationToOfficeSectionInputModel>() {
					@Override
					public Change<OfficeEscalationToOfficeSectionInputModel> createChange(
							OfficeEscalationToOfficeSectionInputModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeEscalationToOfficeSectionInput(
										target);
					}
				});

		// Allow deleting start to input
		policy.addDelete(
				OfficeStartToOfficeSectionInputModel.class,
				new DeleteChangeFactory<OfficeStartToOfficeSectionInputModel>() {
					@Override
					public Change<OfficeStartToOfficeSectionInputModel> createChange(
							OfficeStartToOfficeSectionInputModel target) {
						return OfficeEditor.this.getModelChanges()
								.removeOfficeStartToOfficeSectionInput(target);
					}
				});
	}

	@Override
	protected void populateGraphicalEditPolicy(
			OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect office section object to external managed object
		policy.addConnection(
				OfficeSectionObjectModel.class,
				ExternalManagedObjectModel.class,
				new ConnectionChangeFactory<OfficeSectionObjectModel, ExternalManagedObjectModel>() {
					@Override
					public Change<?> createChange(
							OfficeSectionObjectModel source,
							ExternalManagedObjectModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this
								.getModelChanges()
								.linkOfficeSectionObjectToExternalManagedObject(
										source, target);
					}
				});

		// Connect office section object to managed object
		policy.addConnection(
				OfficeSectionObjectModel.class,
				OfficeManagedObjectModel.class,
				new ConnectionChangeFactory<OfficeSectionObjectModel, OfficeManagedObjectModel>() {
					@Override
					public Change<?> createChange(
							OfficeSectionObjectModel source,
							OfficeManagedObjectModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkOfficeSectionObjectToOfficeManagedObject(
										source, target);
					}
				});

		// Connect managed object dependency to managed object
		policy.addConnection(
				OfficeManagedObjectDependencyModel.class,
				OfficeManagedObjectModel.class,
				new ConnectionChangeFactory<OfficeManagedObjectDependencyModel, OfficeManagedObjectModel>() {
					@Override
					public Change<?> createChange(
							OfficeManagedObjectDependencyModel source,
							OfficeManagedObjectModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this
								.getModelChanges()
								.linkOfficeManagedObjectDependencyToOfficeManagedObject(
										source, target);
					}
				});

		// Connect managed object dependency to external managed object
		policy.addConnection(
				OfficeManagedObjectDependencyModel.class,
				ExternalManagedObjectModel.class,
				new ConnectionChangeFactory<OfficeManagedObjectDependencyModel, ExternalManagedObjectModel>() {
					@Override
					public Change<?> createChange(
							OfficeManagedObjectDependencyModel source,
							ExternalManagedObjectModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this
								.getModelChanges()
								.linkOfficeManagedObjectDependencyToExternalManagedObject(
										source, target);
					}
				});

		// Connect managed object source flow to office section input
		policy.addConnection(
				OfficeManagedObjectSourceFlowModel.class,
				OfficeSectionInputModel.class,
				new ConnectionChangeFactory<OfficeManagedObjectSourceFlowModel, OfficeSectionInputModel>() {
					@Override
					public Change<?> createChange(
							OfficeManagedObjectSourceFlowModel source,
							OfficeSectionInputModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this
								.getModelChanges()
								.linkOfficeManagedObjectSourceFlowToOfficeSectionInput(
										source, target);
					}
				});

		// Connect office section output to office section input
		policy.addConnection(
				OfficeSectionOutputModel.class,
				OfficeSectionInputModel.class,
				new ConnectionChangeFactory<OfficeSectionOutputModel, OfficeSectionInputModel>() {
					@Override
					public Change<?> createChange(
							OfficeSectionOutputModel source,
							OfficeSectionInputModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkOfficeSectionOutputToOfficeSectionInput(
										source, target);
					}
				});

		// Connect office section responsibility to office team
		policy.addConnection(
				OfficeSectionResponsibilityModel.class,
				OfficeTeamModel.class,
				new ConnectionChangeFactory<OfficeSectionResponsibilityModel, OfficeTeamModel>() {
					@Override
					public Change<?> createChange(
							OfficeSectionResponsibilityModel source,
							OfficeTeamModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkOfficeSectionResponsibilityToOfficeTeam(
										source, target);
					}
				});

		// Connect office managed object source team to office team
		policy.addConnection(
				OfficeManagedObjectSourceTeamModel.class,
				OfficeTeamModel.class,
				new ConnectionChangeFactory<OfficeManagedObjectSourceTeamModel, OfficeTeamModel>() {
					@Override
					public Change<?> createChange(
							OfficeManagedObjectSourceTeamModel source,
							OfficeTeamModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkOfficeManagedObjectSourceTeamToOfficeTeam(
										source, target);
					}
				});

		// Connect administrator to office team
		policy.addConnection(
				AdministratorModel.class,
				OfficeTeamModel.class,
				new ConnectionChangeFactory<AdministratorModel, OfficeTeamModel>() {
					@Override
					public Change<?> createChange(AdministratorModel source,
							OfficeTeamModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkAdministratorToOfficeTeam(source, target);
					}
				});

		// Connect external managed object to administrator
		policy.addConnection(
				ExternalManagedObjectModel.class,
				AdministratorModel.class,
				new ConnectionChangeFactory<ExternalManagedObjectModel, AdministratorModel>() {
					@Override
					public Change<?> createChange(
							ExternalManagedObjectModel source,
							AdministratorModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkExternalManagedObjectToAdministrator(
										source, target);
					}
				});

		// Connect managed object to administrator
		policy.addConnection(
				OfficeManagedObjectModel.class,
				AdministratorModel.class,
				new ConnectionChangeFactory<OfficeManagedObjectModel, AdministratorModel>() {
					@Override
					public Change<?> createChange(
							OfficeManagedObjectModel source,
							AdministratorModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkOfficeManagedObjectToAdministrator(source,
										target);
					}
				});

		// Connect task to pre/post duty
		policy.addConnection(DutyModel.class, OfficeSectionModel.class,
				new ConnectionChangeFactory<DutyModel, OfficeSectionModel>() {
					@Override
					public Change<?> createChange(DutyModel source,
							OfficeSectionModel target,
							CreateConnectionRequest request) {

						// Obtain the selected task
						OfficeTaskInstance task = OfficeTaskWizard
								.getOfficeTask(target, OfficeEditor.this);
						if (task == null) {
							return null; // no task, no change
						}

						// Return change to add pre/post duty to task
						if (task.isPreRatherThanPostDuty()) {
							// Return change to add pre duty
							return OfficeEditor.this.getModelChanges()
									.linkOfficeTaskToPreDuty(
											task.getOfficeTask(), source,
											target, task.getOfficeSection());
						} else {
							// Return change to add post duty
							return OfficeEditor.this.getModelChanges()
									.linkOfficeTaskToPostDuty(
											task.getOfficeTask(), source,
											target, task.getOfficeSection());
						}
					}
				});

		// Connect escalation to input
		policy.addConnection(
				OfficeEscalationModel.class,
				OfficeSectionInputModel.class,
				new ConnectionChangeFactory<OfficeEscalationModel, OfficeSectionInputModel>() {
					@Override
					public Change<?> createChange(OfficeEscalationModel source,
							OfficeSectionInputModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkOfficeEscalationToOfficeSectionInput(
										source, target);
					}
				});

		// Connect start to input
		policy.addConnection(
				OfficeStartModel.class,
				OfficeSectionInputModel.class,
				new ConnectionChangeFactory<OfficeStartModel, OfficeSectionInputModel>() {
					@Override
					public Change<?> createChange(OfficeStartModel source,
							OfficeSectionInputModel target,
							CreateConnectionRequest request) {
						return OfficeEditor.this.getModelChanges()
								.linkOfficeStartToOfficeSectionInput(source,
										target);
					}
				});
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
		list.add(new AddOfficeSectionResponsibilityOperation(officeChanges));
	}

}