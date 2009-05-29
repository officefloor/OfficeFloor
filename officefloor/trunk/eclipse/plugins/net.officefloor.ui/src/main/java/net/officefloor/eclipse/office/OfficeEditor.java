/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
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
import net.officefloor.eclipse.office.editparts.OfficeSectionEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionInputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionObjectToExternalManagedObjectEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionOutputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionOutputToOfficeSectionInputEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionResponsibilityEditPart;
import net.officefloor.eclipse.office.editparts.OfficeSectionResponsibilityToOfficeTeamEditPart;
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
import net.officefloor.eclipse.office.operations.AddOfficeSectionOperation;
import net.officefloor.eclipse.office.operations.AddOfficeSectionResponsibilityOperation;
import net.officefloor.eclipse.office.operations.AddOfficeTeamOperation;
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
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
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
		map.put(OfficeSectionModel.class, OfficeSectionEditPart.class);
		map
				.put(OfficeSectionInputModel.class,
						OfficeSectionInputEditPart.class);
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

		// Connections
		map.put(OfficeSectionObjectToExternalManagedObjectModel.class,
				OfficeSectionObjectToExternalManagedObjectEditPart.class);
		map.put(OfficeSectionOutputToOfficeSectionInputModel.class,
				OfficeSectionOutputToOfficeSectionInputEditPart.class);
		map.put(OfficeSectionResponsibilityToOfficeTeamModel.class,
				OfficeSectionResponsibilityToOfficeTeamEditPart.class);
		map.put(AdministratorToOfficeTeamModel.class,
				AdministratorToOfficeTeamEditPart.class);
		map.put(ExternalManagedObjectToAdministratorModel.class,
				ExternalManagedObjectToAdministratorEditPart.class);
		map.put(OfficeTaskToPreDutyModel.class,
				OfficeTaskToPreDutyEditPart.class);
		map.put(OfficeTaskToPostDutyModel.class,
				OfficeTaskToPostDutyEditPart.class);
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
		policy
				.addDelete(
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

		// Allow deleting office section output to office section input
		policy
				.addDelete(
						OfficeSectionOutputToOfficeSectionInputModel.class,
						new DeleteChangeFactory<OfficeSectionOutputToOfficeSectionInputModel>() {
							@Override
							public Change<OfficeSectionOutputToOfficeSectionInputModel> createChange(
									OfficeSectionOutputToOfficeSectionInputModel target) {
								return OfficeEditor.this
										.getModelChanges()
										.removeOfficeSectionOutputToOfficeSectionInput(
												target);
							}
						});

		// Allow deleting office section responsibility to office team
		policy
				.addDelete(
						OfficeSectionResponsibilityToOfficeTeamModel.class,
						new DeleteChangeFactory<OfficeSectionResponsibilityToOfficeTeamModel>() {
							@Override
							public Change<OfficeSectionResponsibilityToOfficeTeamModel> createChange(
									OfficeSectionResponsibilityToOfficeTeamModel target) {
								return OfficeEditor.this
										.getModelChanges()
										.removeOfficeSectionResponsibilityToOfficeTeam(
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
		policy
				.addDelete(
						ExternalManagedObjectToAdministratorModel.class,
						new DeleteChangeFactory<ExternalManagedObjectToAdministratorModel>() {
							@Override
							public Change<ExternalManagedObjectToAdministratorModel> createChange(
									ExternalManagedObjectToAdministratorModel target) {
								return OfficeEditor.this
										.getModelChanges()
										.removeExternalManagedObjectToAdministrator(
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
	}

	@Override
	protected void populateGraphicalEditPolicy(
			OfficeFloorGraphicalNodeEditPolicy policy) {

		// Connect office section object to external managed object
		policy
				.addConnection(
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

		// Connect office section output to office section input
		policy
				.addConnection(
						OfficeSectionOutputModel.class,
						OfficeSectionInputModel.class,
						new ConnectionChangeFactory<OfficeSectionOutputModel, OfficeSectionInputModel>() {
							@Override
							public Change<?> createChange(
									OfficeSectionOutputModel source,
									OfficeSectionInputModel target,
									CreateConnectionRequest request) {
								return OfficeEditor.this
										.getModelChanges()
										.linkOfficeSectionOutputToOfficeSectionInput(
												source, target);
							}
						});

		// Connect office section responsibility to office team
		policy
				.addConnection(
						OfficeSectionResponsibilityModel.class,
						OfficeTeamModel.class,
						new ConnectionChangeFactory<OfficeSectionResponsibilityModel, OfficeTeamModel>() {
							@Override
							public Change<?> createChange(
									OfficeSectionResponsibilityModel source,
									OfficeTeamModel target,
									CreateConnectionRequest request) {
								return OfficeEditor.this
										.getModelChanges()
										.linkOfficeSectionResponsibilityToOfficeTeam(
												source, target);
							}
						});

		// Connect administrator to office team
		policy
				.addConnection(
						AdministratorModel.class,
						OfficeTeamModel.class,
						new ConnectionChangeFactory<AdministratorModel, OfficeTeamModel>() {
							@Override
							public Change<?> createChange(
									AdministratorModel source,
									OfficeTeamModel target,
									CreateConnectionRequest request) {
								return OfficeEditor.this.getModelChanges()
										.linkAdministratorToOfficeTeam(source,
												target);
							}
						});

		// Connect external managed object to administrator
		policy
				.addConnection(
						ExternalManagedObjectModel.class,
						AdministratorModel.class,
						new ConnectionChangeFactory<ExternalManagedObjectModel, AdministratorModel>() {
							@Override
							public Change<?> createChange(
									ExternalManagedObjectModel source,
									AdministratorModel target,
									CreateConnectionRequest request) {
								return OfficeEditor.this
										.getModelChanges()
										.linkExternalManagedObjectToAdministrator(
												source, target);
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
	}

	@Override
	protected void populateOperations(List<Operation> list) {

		// Obtain the office changes
		OfficeChanges officeChanges = this.getModelChanges();

		// Add model operations
		list.add(new AddOfficeSectionOperation(officeChanges));
		list.add(new AddOfficeTeamOperation(officeChanges));
		list.add(new AddExternalManagedObjectOperation(officeChanges));
		list.add(new AddAdministratorOperation(officeChanges));
		list.add(new AddOfficeEscalationOperation(officeChanges));

		// Office section add model operations
		list.add(new AddOfficeSectionResponsibilityOperation(officeChanges));
	}

}