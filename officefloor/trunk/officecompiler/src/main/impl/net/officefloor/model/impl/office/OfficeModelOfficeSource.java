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
package net.officefloor.model.impl.office;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeSourceService;
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.AdministerableManagedObject;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
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
import net.officefloor.model.office.PropertyModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeModel} {@link OfficeSource}.
 * 
 * @author Daniel
 */
public class OfficeModelOfficeSource extends AbstractOfficeSource implements
		OfficeSourceService<OfficeModelOfficeSource> {

	/*
	 * ====================== OfficeSourceService ==============================
	 */

	@Override
	public String getOfficeSourceAlias() {
		return "OFFICE";
	}

	@Override
	public Class<OfficeModelOfficeSource> getOfficeSourceClass() {
		return OfficeModelOfficeSource.class;
	}

	/*
	 * ================= AbstractOfficeSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceOffice(OfficeArchitect architect,
			OfficeSourceContext context) throws Exception {

		// Obtain the configuration to the section
		ConfigurationItem configuration = context.getConfiguration(context
				.getOfficeLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find office '"
					+ context.getOfficeLocation() + "'");
		}

		// Retrieve the office model
		OfficeModel office = new OfficeRepositoryImpl(new ModelRepositoryImpl())
				.retrieveOffice(configuration);

		// Add the external managed objects, keeping registry of them
		Map<String, OfficeObject> officeObjects = new HashMap<String, OfficeObject>();
		for (ExternalManagedObjectModel object : office
				.getExternalManagedObjects()) {
			String officeObjectName = object.getExternalManagedObjectName();
			OfficeObject officeObject = architect.addOfficeObject(
					officeObjectName, object.getObjectType());
			officeObjects.put(officeObjectName, officeObject);
		}

		// Add the teams, keeping registry of the teams
		Map<String, OfficeTeam> teams = new HashMap<String, OfficeTeam>();
		for (OfficeTeamModel teamModel : office.getOfficeTeams()) {
			String teamName = teamModel.getOfficeTeamName();
			OfficeTeam team = architect.addOfficeTeam(teamName);
			teams.put(teamName, team);
		}

		// Add the sections, keeping registry of inputs/outputs/objects
		Map<String, OfficeSection> sections = new HashMap<String, OfficeSection>();
		DoubleKeyMap<String, String, OfficeSectionInput> inputs = new DoubleKeyMap<String, String, OfficeSectionInput>();
		DoubleKeyMap<String, String, OfficeSectionOutput> outputs = new DoubleKeyMap<String, String, OfficeSectionOutput>();
		DoubleKeyMap<String, String, OfficeSectionObject> objects = new DoubleKeyMap<String, String, OfficeSectionObject>();
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Create the property list to add the section
			PropertyList propertyList = architect.createPropertyList();
			for (PropertyModel property : sectionModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(
						property.getValue());
			}

			// Add the section (register for later)
			String sectionName = sectionModel.getOfficeSectionName();
			OfficeSection section = architect.addOfficeSection(sectionName,
					sectionModel.getSectionSourceClassName(), sectionModel
							.getSectionLocation(), propertyList);
			sections.put(sectionName, section);

			// Register the section inputs
			for (OfficeSectionInput input : section.getOfficeSectionInputs()) {
				inputs.put(sectionName, input.getOfficeSectionInputName(),
						input);
			}

			// Register the section outputs
			for (OfficeSectionOutput output : section.getOfficeSectionOutputs()) {
				outputs.put(sectionName, output.getOfficeSectionOutputName(),
						output);
			}

			// Register the section objects
			for (OfficeSectionObject object : section.getOfficeSectionObjects()) {
				objects.put(sectionName, object.getOfficeSectionObjectName(),
						object);
			}

			// Create the listing of responsibilities
			List<Responsibility> responsibilities = new LinkedList<Responsibility>();
			for (OfficeSectionResponsibilityModel responsibilityModel : sectionModel
					.getOfficeSectionResponsibilities()) {

				// Obtain the office team responsible
				OfficeTeam officeTeam = null;
				OfficeSectionResponsibilityToOfficeTeamModel conn = responsibilityModel
						.getOfficeTeam();
				if (conn != null) {
					OfficeTeamModel teamModel = conn.getOfficeTeam();
					if (teamModel != null) {
						String teamName = teamModel.getOfficeTeamName();
						officeTeam = teams.get(teamName);
					}
				}
				if (officeTeam == null) {
					continue; // must have team responsible
				}

				// Add the responsibility
				responsibilities.add(new Responsibility(officeTeam));
			}

			// Create the listing of all tasks
			List<OfficeTask> tasks = new LinkedList<OfficeTask>();
			this.loadOfficeTasks(section, tasks);

			// Assign teams their responsibilities
			for (Responsibility responsibility : responsibilities) {
				for (OfficeTask task : new ArrayList<OfficeTask>(tasks)) {
					if (responsibility.isResponsible(task)) {
						// Assign the team responsible for task
						architect.link(task.getTeamResponsible(),
								responsibility.officeTeam);

						// Remove task from listing as assigned its team
						tasks.remove(task);
					}
				}
			}
		}

		// Link the sections to other sections and external office
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Obtain the section name
			String sectionName = sectionModel.getOfficeSectionName();

			// Link the objects to office objects
			for (OfficeSectionObjectModel objectModel : sectionModel
					.getOfficeSectionObjects()) {

				// Obtain the object
				OfficeSectionObject object = objects.get(sectionName,
						objectModel.getOfficeSectionObjectName());
				if (object == null) {
					continue; // must have the object
				}

				// Determine if link object to office object
				OfficeObject officeObject = null;
				OfficeSectionObjectToExternalManagedObjectModel conn = objectModel
						.getExternalManagedObject();
				if (conn != null) {
					ExternalManagedObjectModel extMo = conn
							.getExternalManagedObject();
					if (extMo != null) {
						officeObject = officeObjects.get(extMo
								.getExternalManagedObjectName());
					}
				}
				if (officeObject != null) {
					// Link object to office object
					architect.link(object, officeObject);
				}

			}

			// Link the outputs to the inputs
			for (OfficeSectionOutputModel outputModel : sectionModel
					.getOfficeSectionOutputs()) {

				// Obtain the output
				OfficeSectionOutput output = outputs.get(sectionName,
						outputModel.getOfficeSectionOutputName());
				if (output == null) {
					continue; // must have the output
				}

				// Obtain the input
				OfficeSectionInput input = null;
				OfficeSectionOutputToOfficeSectionInputModel conn = outputModel
						.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel inputModel = conn
							.getOfficeSectionInput();
					if (inputModel != null) {
						OfficeSectionModel inputSection = this
								.getOfficeSectionForInput(office, inputModel);
						if (inputSection != null) {
							input = inputs.get(inputSection
									.getOfficeSectionName(), inputModel
									.getOfficeSectionInputName());
						}
					}
				}
				if (input == null) {
					continue; // must have the input
				}

				// Link output to the input
				architect.link(output, input);
			}
		}

		// Add the administrators (keeping registry of administrators, duties)
		Map<String, OfficeAdministrator> administrators = new HashMap<String, OfficeAdministrator>();
		Map<DutyModel, AdministratorModel> dutyAdmins = new HashMap<DutyModel, AdministratorModel>();
		DoubleKeyMap<String, String, OfficeDuty> duties = new DoubleKeyMap<String, String, OfficeDuty>();
		for (AdministratorModel adminModel : office.getOfficeAdministrators()) {

			// Add the administrator and register it
			String adminName = adminModel.getAdministratorName();
			OfficeAdministrator admin = architect.addOfficeAdministrator(
					adminName, adminModel.getAdministratorSourceClassName());
			for (PropertyModel property : adminModel.getProperties()) {
				admin.addProperty(property.getName(), property.getValue());
			}
			administrators.put(adminName, admin);

			// Obtain the office team responsible for this administration
			OfficeTeam officeTeam = null;
			AdministratorToOfficeTeamModel adminToTeam = adminModel
					.getOfficeTeam();
			if (adminToTeam != null) {
				OfficeTeamModel teamModel = adminToTeam.getOfficeTeam();
				if (teamModel != null) {
					officeTeam = teams.get(teamModel.getOfficeTeamName());
				}
			}
			if (officeTeam != null) {
				// Assign the team responsible for administration
				architect.link(admin, officeTeam);
			}

			// Add the duties for the administrator (register also)
			for (DutyModel dutyModel : adminModel.getDuties()) {

				// Add the duty and register it
				String dutyName = dutyModel.getDutyName();
				OfficeDuty duty = admin.getDuty(dutyName);
				duties.put(adminName, dutyName, duty);

				// Keep registry of duties to their administrators
				dutyAdmins.put(dutyModel, adminModel);
			}
		}

		// Link the office tasks to administration duties
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Obtain the top level office sub sections
			OfficeSection subSection = sections.get(sectionModel
					.getOfficeSectionName());
			OfficeSubSectionModel subSectionModel = sectionModel
					.getOfficeSubSection();

			// Recurse through the sub sections linking duties to tasks
			this.linkTasksToDuties(null, subSection, subSectionModel,
					sectionModel, dutyAdmins, duties, architect);
		}

		// Create the listing of objects to be administered
		Map<String, List<AdministeredManagedObject>> administration = new HashMap<String, List<AdministeredManagedObject>>();
		for (ExternalManagedObjectModel extMo : office
				.getExternalManagedObjects()) {

			// Obtain the office object
			OfficeObject officeObject = officeObjects.get(extMo
					.getExternalManagedObjectName());

			// Add the object for administration
			for (ExternalManagedObjectToAdministratorModel extMoToAdmin : extMo
					.getAdministrators()) {
				AdministratorModel adminModel = extMoToAdmin.getAdministrator();
				if (adminModel != null) {
					String administratorName = adminModel
							.getAdministratorName();
					List<AdministeredManagedObject> list = administration
							.get(administratorName);
					if (list == null) {
						list = new LinkedList<AdministeredManagedObject>();
						administration.put(administratorName, list);
					}
					list.add(new AdministeredManagedObject(extMoToAdmin
							.getOrder(), officeObject));
				}
			}
		}

		// Administer the managed objects
		for (AdministratorModel adminModel : office.getOfficeAdministrators()) {

			// Obtain the administrator
			String administratorName = adminModel.getAdministratorName();
			OfficeAdministrator admin = administrators.get(administratorName);

			// Obtain the objects to administer
			List<AdministeredManagedObject> managedObjects = administration
					.get(administratorName);
			if (managedObjects == null) {
				continue; // no managed objects to administer
			}

			// Order the managed objects
			Collections.sort(managedObjects);

			// Add managed objects for administration
			for (AdministeredManagedObject managedObject : managedObjects) {
				admin.administerManagedObject(managedObject.managedObject);
			}
		}

	}

	/**
	 * Recurses through the {@link OfficeSubSectionModel} instances linking the
	 * {@link OfficeTaskModel} instances to the {@link DutyModel} instances.
	 * 
	 * @param subSectionPath
	 *            Path from top level {@link OfficeSubSectionModel} to current
	 *            {@link OfficeSubSectionModel}.
	 * @param subSection
	 *            {@link OfficeSubSection}.
	 * @param subSectionModel
	 *            {@link OfficeSubSectionModel}.
	 * @param sectionModel
	 *            {@link OfficeSectionModel}.
	 * @param dutyAdministrators
	 *            Map of providing the {@link AdministratorModel} for a
	 *            {@link DutyModel}.
	 * @param duties
	 *            Map of {@link OfficeDuty} by {@link Administrator} then
	 *            {@link Duty} names.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void linkTasksToDuties(String subSectionPath,
			OfficeSubSection subSection, OfficeSubSectionModel subSectionModel,
			OfficeSectionModel sectionModel,
			Map<DutyModel, AdministratorModel> dutyAdministrators,
			DoubleKeyMap<String, String, OfficeDuty> duties,
			OfficeArchitect architect) {

		// Ensure have sub section model
		if (subSectionModel == null) {
			return;
		}

		// Determine the path for current sub section
		String subSectionName = subSectionModel.getOfficeSubSectionName();
		String subSectionLabel;
		if (subSectionPath == null) {
			subSectionPath = sectionModel.getOfficeSectionName();
			subSectionLabel = "(section=" + subSectionPath + ")";
		} else {
			subSectionPath = subSectionPath + "/" + subSectionName;
			subSectionLabel = "(sub section=" + subSectionPath + ")";
		}

		// Link the tasks for the current sub section
		for (OfficeTaskModel taskModel : subSectionModel.getOfficeTasks()) {

			// Determine if task is linked to duties
			List<OfficeTaskToPreDutyModel> preDuties = taskModel.getPreDuties();
			List<OfficeTaskToPostDutyModel> postDuties = taskModel
					.getPostDuties();
			if ((preDuties.size() == 0) && (postDuties.size() == 0)) {
				continue; // no duties to link for task
			}

			// Obtain the corresponding office task
			String taskName = taskModel.getOfficeTaskName();
			OfficeTask task = null;
			for (OfficeTask checkTask : subSection.getOfficeTasks()) {
				if (taskName.equals(checkTask.getOfficeTaskName())) {
					task = checkTask;
				}
			}
			if (task == null) {
				architect.addIssue(
						"Office model is out of sync with sections. Can not find task '"
								+ taskName + "' " + subSectionLabel,
						AssetType.TASK, taskName);
				continue; // must have task
			}

			// Link the pre task duties
			for (int i = 0; i < preDuties.size(); i++) {
				OfficeTaskToPreDutyModel conn = preDuties.get(i);

				// Obtain the pre task duty
				OfficeDuty preTaskDuty = null;
				DutyModel dutyModel = conn.getDuty();
				if (dutyModel != null) {
					AdministratorModel adminModel = dutyAdministrators
							.get(dutyModel);
					if (adminModel != null) {
						preTaskDuty = duties.get(adminModel
								.getAdministratorName(), dutyModel
								.getDutyName());
					}
				}
				if (preTaskDuty == null) {
					architect.addIssue(
							"Can not find pre duty " + i + " for task '"
									+ taskName + "' " + subSectionLabel,
							AssetType.TASK, taskName);
					continue; // must have duty
				}

				// Add the pre task duty
				task.addPreTaskDuty(preTaskDuty);
			}

			// Link the post task duties
			for (int i = 0; i < postDuties.size(); i++) {
				OfficeTaskToPostDutyModel conn = postDuties.get(i);

				// Obtain the post task duty
				OfficeDuty postTaskDuty = null;
				DutyModel dutyModel = conn.getDuty();
				if (dutyModel != null) {
					AdministratorModel adminModel = dutyAdministrators
							.get(dutyModel);
					if (adminModel != null) {
						postTaskDuty = duties.get(adminModel
								.getAdministratorName(), dutyModel
								.getDutyName());
					}
				}
				if (postTaskDuty == null) {
					architect.addIssue(
							"Can not find post duty " + i + " for task '"
									+ taskName + "' " + subSectionLabel,
							AssetType.TASK, taskName);
					continue; // must have duty
				}

				// Add the post task duty
				task.addPostTaskDuty(postTaskDuty);
			}
		}

		// Recurse into the sub sections
		for (OfficeSubSectionModel subSubSectionModel : subSectionModel
				.getOfficeSubSections()) {

			// Obtain the corresponding sub section
			String subSubSectionName = subSubSectionModel
					.getOfficeSubSectionName();
			OfficeSubSection subSubSection = null;
			for (OfficeSubSection checkSection : subSection
					.getOfficeSubSections()) {
				if (subSubSectionName.equals(checkSection
						.getOfficeSectionName())) {
					subSubSection = checkSection;
				}
			}
			if (subSubSection == null) {
				architect.addIssue(
						"Office model is out of sync with sections. Can not find sub section '"
								+ subSubSectionName + "' " + subSectionLabel,
						null, null);
				continue; // must have office sub section
			}

			// Recursively link the sub section tasks
			this.linkTasksToDuties(subSectionPath, subSubSection,
					subSubSectionModel, sectionModel, dutyAdministrators,
					duties, architect);
		}
	}

	/**
	 * Obtains the {@link OfficeSectionModel} containing the
	 * {@link OfficeSectionInputModel}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param input
	 *            {@link OfficeSectionInput}.
	 * @return {@link OfficeSectionModel} containing the
	 *         {@link OfficeSectionInput}.
	 */
	private OfficeSectionModel getOfficeSectionForInput(OfficeModel office,
			OfficeSectionInputModel input) {

		// Find and return the office section for the input
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel check : section
					.getOfficeSectionInputs()) {
				if (check == input) {
					// Found the input so subsequently return section
					return section;
				}
			}
		}

		// As here did not find the section
		return null;
	}

	/**
	 * Loads the {@link OfficeTask} instances for the {@link OfficeSubSection}
	 * and its {@link OfficeSubSection} instances.
	 * 
	 * @param section
	 *            {@link OfficeSubSection}.
	 * @param tasks
	 *            Listing to be populated with the {@link OfficeSubSection}
	 *            {@link OfficeTask} instances.
	 */
	private void loadOfficeTasks(OfficeSubSection section,
			List<OfficeTask> tasks) {

		// Ensure have section
		if (section == null) {
			return;
		}

		// Add the section office tasks
		for (OfficeTask task : section.getOfficeTasks()) {
			tasks.add(task);
		}

		// Recursively add the sub section office tasks
		for (OfficeSubSection subSection : section.getOfficeSubSections()) {
			this.loadOfficeTasks(subSection, tasks);
		}
	}

	/**
	 * Responsibility.
	 */
	private static class Responsibility {

		/**
		 * {@link OfficeTeam} responsible for this responsibility.
		 */
		public final OfficeTeam officeTeam;

		/**
		 * Initiate.
		 * 
		 * @param officeTeam
		 *            {@link OfficeTeam} responsible for this responsibility.
		 */
		public Responsibility(OfficeTeam officeTeam) {
			this.officeTeam = officeTeam;
		}

		/**
		 * Indicates if {@link OfficeTask} is within this responsibility.
		 * 
		 * @param task
		 *            {@link OfficeTask}.
		 * @return <code>true</code> if {@link OfficeTask} is within this
		 *         responsibility.
		 */
		public boolean isResponsible(OfficeTask task) {
			// TODO handle managed object matching for responsibility
			return true; // TODO for now always responsible
		}
	}

	/**
	 * {@link ManagedObject} to be administered.
	 */
	private static class AdministeredManagedObject implements
			Comparable<AdministeredManagedObject> {

		/**
		 * Position in the order that the objects are administered.
		 */
		public final String order;

		/**
		 * {@link AdministerableManagedObject}.
		 */
		public final AdministerableManagedObject managedObject;

		/**
		 * Initiate.
		 * 
		 * @param order
		 *            Position in the order that the objects are administered.
		 * @param managedObject
		 *            {@link AdministerableManagedObject}.
		 */
		public AdministeredManagedObject(String order,
				AdministerableManagedObject managedObject) {
			this.order = order;
			this.managedObject = managedObject;
		}

		/*
		 * ================== Comparable ==========================
		 */

		@Override
		public int compareTo(AdministeredManagedObject that) {
			return this.getOrder(this.order) - this.getOrder(that.order);
		}

		/**
		 * Obtains the order as an {@link Integer}.
		 * 
		 * @param order
		 *            Text order value.
		 * @return Numeric order value.
		 */
		private int getOrder(String order) {
			try {
				return Integer.parseInt(order);
			} catch (NumberFormatException ex) {
				return Integer.MAX_VALUE; // invalid number so make last
			}
		}
	}

}