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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.NoChange;
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
import net.officefloor.model.office.PropertyModel;

/**
 * {@link OfficeChanges} implementation.
 * 
 * @author Daniel
 */
public class OfficeChangesImpl implements OfficeChanges {

	/**
	 * {@link OfficeModel} to change.
	 */
	private final OfficeModel office;

	/**
	 * Initiate.
	 * 
	 * @param office
	 *            {@link OfficeModel} to change.
	 */
	public OfficeChangesImpl(OfficeModel office) {
		this.office = office;
	}

	/**
	 * Loads the hierarchy of {@link OfficeSubSection} instances from the top
	 * {@link OfficeSubSection} down to the {@link OfficeSubSection} containing
	 * the {@link OfficeTask}.
	 * 
	 * @param hierarchy
	 *            Hierarchy to be loaded with the {@link OfficeSubSection}
	 *            instances containing the {@link OfficeTask}.
	 * @param task
	 *            {@link OfficeTask} to load the hierarchy for.
	 * @param subSection
	 *            Current {@link OfficeSubSection} being searched for the
	 *            {@link OfficeTask}.
	 * @return <code>true</code> if the input {@link OfficeSubSection} contains
	 *         the {@link OfficeTask}.
	 */
	private boolean loadHierarchy(Deque<OfficeSubSection> hierarchy,
			OfficeTask task, OfficeSubSection subSection) {

		// Search the sub section for the task
		boolean isContainsTask = false;
		for (OfficeTask checkTask : subSection.getOfficeTasks()) {
			if (checkTask == task) {
				isContainsTask = true;
			}
		}

		// Determine if contains the task
		if (isContainsTask) {
			// Contains the task, so add the sub section to bottom of hierarchy
			hierarchy.push(subSection);

			// Return indicating sub section contains the task
			return true;
		}

		// Not in this sub section, search the sub sub sections
		for (OfficeSubSection subSubSection : subSection.getOfficeSubSections()) {

			// Search the sub sub section
			boolean isWithinSubSubSection = this.loadHierarchy(hierarchy, task,
					subSubSection);
			if (isWithinSubSubSection) {
				// Task within sub sub section, so add sub section to hierarchy
				hierarchy.push(subSection);

				// Return indicating this sub section contains the task
				return true;
			}
		}

		// No sub sub sections contains task, not contained in this sub section
		return false;
	}

	/**
	 * Adds the {@link OfficeSubSection} hierarchy to the
	 * {@link OfficeSectionModel}.
	 * 
	 * @param hierarchy
	 *            {@link OfficeSubSection} hierarchy.
	 * @param section
	 *            {@link OfficeSectionModel} to have the
	 *            {@link OfficeSubSection} hierarchy added.
	 * @return Bottom {@link OfficeSubSectionModel} of the added
	 *         {@link OfficeSubSection} hierarchy.
	 */
	private OfficeSubSectionModel addHierarchy(
			Deque<OfficeSubSection> hierarchy, OfficeSectionModel section) {

		// Top of hierarchy is always the office section
		hierarchy.pop();

		// Ensure the top level sub section is on the section
		OfficeSubSectionModel subSection = section.getOfficeSubSection();
		if (subSection == null) {
			// Create and add the top level sub section
			subSection = new OfficeSubSectionModel();
			section.setOfficeSubSection(subSection);
		}

		// Determine if further hierarchy to add
		if (hierarchy.size() == 0) {
			// No further hierarchy, so return sub section as bottom
			return subSection;
		}

		// Add the remaining hierarchy of sub sections
		return this.addHierarchy(hierarchy, subSection);
	}

	/**
	 * Adds the hierarchy to the {@link OfficeSubSectionModel}.
	 * 
	 * @param hierarchy
	 *            Hierarchy of {@link OfficeSubSection} instances to add to the
	 *            {@link OfficeSubSectionModel}.
	 * @param subSection
	 *            {@link OfficeSubSectionModel} to have the
	 *            {@link OfficeSubSection} hierarchy added.
	 * @return Bottom {@link OfficeSubSectionModel} of the added
	 *         {@link OfficeSubSection} hierarchy.
	 */
	private OfficeSubSectionModel addHierarchy(
			Deque<OfficeSubSection> hierarchy, OfficeSubSectionModel subSection) {

		// Obtain the name of the sub sub section to add
		OfficeSubSection addSubSubSection = hierarchy.pop();
		String subSubSectionName = addSubSubSection.getOfficeSectionName();

		// Determine if sub section already added
		OfficeSubSectionModel subSubSection = null;
		for (OfficeSubSectionModel checkSubSubSection : subSection
				.getOfficeSubSections()) {
			if (subSubSectionName.equals(checkSubSubSection
					.getOfficeSubSectionName())) {
				subSubSection = checkSubSubSection;
			}
		}
		if (subSubSection == null) {
			// Sub sub section not added, so add the sub sub section
			subSubSection = new OfficeSubSectionModel(subSubSectionName);
			subSection.addOfficeSubSection(subSubSection);

			// Keep sub sub sections ordered for the sub section
			Collections.sort(subSection.getOfficeSubSections(),
					new Comparator<OfficeSubSectionModel>() {
						@Override
						public int compare(OfficeSubSectionModel a,
								OfficeSubSectionModel b) {
							return a.getOfficeSubSectionName().compareTo(
									b.getOfficeSubSectionName());
						}
					});
		}

		// Determine if further hierarchy to add
		if (hierarchy.size() == 0) {
			// No further hierarchy, so return sub sub section as bottom
			return subSubSection;
		}

		// Add the remaining hierarchy of sub sections
		return this.addHierarchy(hierarchy, subSubSection);
	}

	/**
	 * Adds (or retrieves if already added) the {@link OfficeTask} to the
	 * {@link OfficeSubSectionModel}.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSectionModel} to add the {@link OfficeTask}.
	 * @param officeTask
	 *            {@link OfficeTask} to add to {@link OfficeSubSectionModel}.
	 * @return Resulting {@link OfficeTaskModel} that either existed already for
	 *         the {@link OfficeTask} in the {@link OfficeSubSectionModel} or
	 *         the newly added {@link OfficeTaskModel} for the
	 *         {@link OfficeTask}.
	 */
	private OfficeTaskModel addOfficeTask(OfficeSubSectionModel subSection,
			OfficeTask officeTask) {

		// Determine if office task already in sub section
		String taskName = officeTask.getOfficeTaskName();
		for (OfficeTaskModel task : subSection.getOfficeTasks()) {
			if (taskName.equals(task.getOfficeTaskName())) {
				// Task already exists, so return it
				return task;
			}
		}

		// As at this point, task does not exist so add it
		OfficeTaskModel task = new OfficeTaskModel(taskName);
		subSection.addOfficeTask(task);

		// Keep the tasks ordered for the sub section
		Collections.sort(subSection.getOfficeTasks(),
				new Comparator<OfficeTaskModel>() {
					@Override
					public int compare(OfficeTaskModel a, OfficeTaskModel b) {
						return a.getOfficeTaskName().compareTo(
								b.getOfficeTaskName());
					}
				});

		// Return the added task
		return task;
	}

	/**
	 * Cleans leaf {@link OfficeSubSectionModel} instances that do not contain
	 * connections.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSectionModel} to clean.
	 * @param changes
	 *            List to be populated with the {@link Change} instances that
	 *            occurred to clean up the {@link OfficeSubSectionModel}.
	 * @return <code>true</code> if {@link OfficeSubSectionModel} may be
	 *         cleaned.
	 */
	private boolean cleanSubSection(final OfficeSubSectionModel subSection,
			List<Change<?>> changes) {

		// Ensure have sub section
		if (subSection == null) {
			return true; // no sub section, so clean
		}

		// Determine if sub sub sections require cleaning
		for (final OfficeSubSectionModel subSubSection : new ArrayList<OfficeSubSectionModel>(
				subSection.getOfficeSubSections())) {
			// Determine if can clean the sub sub section
			if (this.cleanSubSection(subSubSection, changes)) {
				// Create change to remove the sub sub section as can clean it
				Change<?> change = new AbstractChange<OfficeSubSectionModel>(
						subSubSection, "Clean") {
					@Override
					public void apply() {
						subSection.removeOfficeSubSection(subSubSection);
					}

					@Override
					public void revert() {
						subSection.addOfficeSubSection(subSubSection);
					}
				};

				// Apply change to remove sub sub section and register it
				change.apply();
				changes.add(change);
			}
		}

		// Clean any tasks that do not have connections
		for (final OfficeTaskModel task : new ArrayList<OfficeTaskModel>(
				subSection.getOfficeTasks())) {
			int preDutyCount = task.getPreDuties().size();
			int postDutyCount = task.getPostDuties().size();
			if ((preDutyCount == 0) && (postDutyCount == 0)) {
				// Task not connected to duty, so remove (clean) it
				// Create change to remove the task
				Change<?> change = new AbstractChange<OfficeTaskModel>(task,
						"Clean") {
					@Override
					public void apply() {
						subSection.removeOfficeTask(task);
					}

					@Override
					public void revert() {
						subSection.addOfficeTask(task);
					}
				};

				// Apply change to remove task and register it
				change.apply();
				changes.add(change);
			}
		}

		// Return whether this sub section has become a leaf and may be cleaned
		int subSubSectionCount = subSection.getOfficeSubSections().size();
		int taskCount = subSection.getOfficeTasks().size();
		boolean canClean = ((subSubSectionCount == 0) && (taskCount == 0));
		return canClean;
	}

	/**
	 * Obtains the {@link OfficeSectionModel} containing the
	 * {@link OfficeTaskModel}.
	 * 
	 * @param task
	 *            {@link OfficeTaskModel}.
	 * @return {@link OfficeSectionModel} containing the {@link OfficeTaskModel}
	 *         or <code>null</code> if {@link OfficeTaskModel} not found in
	 *         {@link OfficeModel}.
	 */
	private OfficeSectionModel getContainingOfficeSection(OfficeTaskModel task) {

		// Find the the section containing the office task
		for (OfficeSectionModel section : this.office.getOfficeSections()) {
			if (this.containsTask(section.getOfficeSubSection(), task)) {
				// Found the section containing the office task
				return section;
			}
		}

		// If at this point, the office task is not found in the office
		return null;
	}

	/**
	 * Determines if the {@link OfficeSubSectionModel} contains the
	 * {@link OfficeTaskModel} on itself or any of its
	 * {@link OfficeSubSectionModel} instances.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSectionModel} to check if contains
	 *            {@link OfficeTaskModel}.
	 * @param task
	 *            {@link OfficeTaskModel}.
	 * @return <code>true</code> if {@link OfficeSubSectionModel} or any of its
	 *         {@link OfficeSubSectionModel} instances contains the
	 *         {@link OfficeTaskModel}.
	 */
	private boolean containsTask(OfficeSubSectionModel subSection,
			OfficeTaskModel task) {

		// Determine if sub section contains the task
		for (OfficeTaskModel checkTask : subSection.getOfficeTasks()) {
			if (task == checkTask) {
				// Sub section contains the task
				return true;
			}
		}

		// Determine if contained in sub sub section
		for (OfficeSubSectionModel subSubSection : subSection
				.getOfficeSubSections()) {
			if (this.containsTask(subSubSection, task)) {
				// Sub sub section contains the task
				return true;
			}
		}

		// Not in this sub section or any descendant sub sections
		return false;
	}

	/*
	 * =================== OfficeChanges =========================
	 */

	@Override
	public Change<OfficeSectionModel> addOfficeSection(
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, OfficeSection officeSection) {

		// TODO test this method (addOfficeSection)

		// Create the office section model
		String sectionName = officeSection.getOfficeSectionName();
		final OfficeSectionModel section = new OfficeSectionModel(sectionName,
				sectionSourceClassName, sectionLocation);
		for (Property property : properties) {
			section.addProperty(new PropertyModel(property.getName(), property
					.getValue()));
		}

		// Add the inputs
		for (OfficeSectionInput input : officeSection.getOfficeSectionInputs()) {
			section.addOfficeSectionInput(new OfficeSectionInputModel(input
					.getOfficeSectionInputName(), input.getParameterType()));
		}

		// Add the outputs
		for (OfficeSectionOutput output : officeSection
				.getOfficeSectionOutputs()) {
			section.addOfficeSectionOutput(new OfficeSectionOutputModel(output
					.getOfficeSectionOutputName(), output.getArgumentType(),
					output.isEscalationOnly()));
		}

		// Add the objects
		for (OfficeSectionObject object : officeSection
				.getOfficeSectionObjects()) {
			section.addOfficeSectionObject(new OfficeSectionObjectModel(object
					.getOfficeSectionObjectName(), object.getObjectType()));
		}

		// Add a responsibility for convenience
		section
				.addOfficeSectionResponsibility(new OfficeSectionResponsibilityModel(
						"Responsibility"));

		// Return the change to add the section
		return new AbstractChange<OfficeSectionModel>(section, "Add section") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addOfficeSection(section);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.removeOfficeSection(section);
			}
		};
	}

	@Override
	public Change<OfficeSectionModel> removeOfficeSection(
			final OfficeSectionModel officeSection) {

		// TODO test this method (removeOfficeSection)

		return new AbstractChange<OfficeSectionModel>(officeSection,
				"Remove section") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeOfficeSection(officeSection);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.addOfficeSection(officeSection);
			}
		};
	}

	@Override
	public Change<OfficeSectionModel> renameOfficeSection(
			final OfficeSectionModel officeSection,
			final String newOfficeSectionName) {

		// TODO test this method (renameOfficeSection)

		// Obtain the old name
		final String oldOfficeSectionName = officeSection
				.getOfficeSectionName();

		// Return the change to rename the office section
		return new AbstractChange<OfficeSectionModel>(officeSection,
				"Rename office section to " + newOfficeSectionName) {
			@Override
			public void apply() {
				officeSection.setOfficeSectionName(newOfficeSectionName);
			}

			@Override
			public void revert() {
				officeSection.setOfficeSectionName(oldOfficeSectionName);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String objectType) {

		// TODO test this method (addExternalManagedObject)

		// Create the external managed object
		final ExternalManagedObjectModel mo = new ExternalManagedObjectModel(
				externalManagedObjectName, objectType);

		// Return change to add external managed object
		return new AbstractChange<ExternalManagedObjectModel>(mo,
				"Add external object") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addExternalManagedObject(mo);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.removeExternalManagedObject(mo);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> removeExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject) {

		// TODO test this method (removeExternalManagedObject)

		// Return change to remove external managed object
		return new AbstractChange<ExternalManagedObjectModel>(
				externalManagedObject, "Remove external object") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeExternalManagedObject(externalManagedObject);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.addExternalManagedObject(externalManagedObject);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> renameExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject,
			final String newExternalManagedObjectName) {

		// TODO test this method (renameExternalManagedObject)

		// Obtain the old name for the external managed object
		final String oldExternalManagedObjectName = externalManagedObject
				.getExternalManagedObjectName();

		// Return change to rename external managed object
		return new AbstractChange<ExternalManagedObjectModel>(
				externalManagedObject, "Rename external object to "
						+ newExternalManagedObjectName) {
			@Override
			public void apply() {
				externalManagedObject
						.setExternalManagedObjectName(newExternalManagedObjectName);
			}

			@Override
			public void revert() {
				externalManagedObject
						.setExternalManagedObjectName(oldExternalManagedObjectName);
			}
		};
	}

	@Override
	public Change<OfficeTeamModel> addOfficeTeam(String teamName) {

		// TODO test this method (addOfficeTeam)

		// Create the office team
		final OfficeTeamModel team = new OfficeTeamModel(teamName);

		// Return change to add team
		return new AbstractChange<OfficeTeamModel>(team, "Add team") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addOfficeTeam(team);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.removeOfficeTeam(team);
			}
		};
	}

	@Override
	public Change<OfficeTeamModel> removeOfficeTeam(
			final OfficeTeamModel officeTeam) {

		// TODO test this method (removeOfficeTeam)

		// Return change to remove team
		return new AbstractChange<OfficeTeamModel>(officeTeam, "Remove team") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.removeOfficeTeam(officeTeam);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.addOfficeTeam(officeTeam);
			}
		};
	}

	@Override
	public Change<OfficeTeamModel> renameOfficeTeam(
			final OfficeTeamModel officeTeam, final String newOfficeTeamName) {

		// TODO test this method (renameOfficeTeam)

		// Obtain the old team name
		final String oldOfficeTeamName = officeTeam.getOfficeTeamName();

		// Return change to rename the office team
		return new AbstractChange<OfficeTeamModel>(officeTeam,
				"Rename team to " + newOfficeTeamName) {
			@Override
			public void apply() {
				officeTeam.setOfficeTeamName(newOfficeTeamName);
			}

			@Override
			public void revert() {
				officeTeam.setOfficeTeamName(oldOfficeTeamName);
			}
		};
	}

	@Override
	public Change<AdministratorModel> addAdministrator(
			String administratorName, String administratorSourceClassName,
			PropertyList properties, AdministratorScope administratorScope,
			AdministratorType<?, ?> administratorType) {

		// TODO test this method (addAdministrator)

		// Obtain the administrator scope text
		String administratorScopeText;
		switch (administratorScope) {
		case PROCESS:
			administratorScopeText = PROCESS_ADMINISTRATOR_SCOPE;
			break;
		case THREAD:
			administratorScopeText = THREAD_ADMINISTRATOR_SCOPE;
			break;
		case WORK:
			administratorScopeText = WORK_ADMINISTRATOR_SCOPE;
			break;
		default:
			throw new IllegalStateException("Unknown administrator scope "
					+ administratorScope);
		}

		// Create the administrator
		final AdministratorModel administrator = new AdministratorModel(
				administratorName, administratorSourceClassName,
				administratorScopeText);
		for (Property property : properties) {
			administrator.addProperty(new PropertyModel(property.getName(),
					property.getValue()));
		}

		// Add the duties
		for (DutyType<?, ?> duty : administratorType.getDutyTypes()) {
			administrator.addDuty(new DutyModel(duty.getDutyName()));
		}

		// Return change to add the administrator
		return new AbstractChange<AdministratorModel>(administrator,
				"Add administrator") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.addOfficeAdministrator(administrator);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.removeOfficeAdministrator(administrator);
			}
		};
	}

	@Override
	public Change<AdministratorModel> removeAdministrator(
			final AdministratorModel administrator) {

		// TODO test this method (removeAdministrator)

		// Return change to remove administrator
		return new AbstractChange<AdministratorModel>(administrator,
				"Remove administrator") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeOfficeAdministrator(administrator);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.addOfficeAdministrator(administrator);
			}
		};
	}

	@Override
	public Change<AdministratorModel> renameAdministrator(
			final AdministratorModel administrator,
			final String newAdministratorName) {

		// TODO test this method (renameAdministrator)

		// Obtain the old name
		final String oldAdministratorName = administrator
				.getAdministratorName();

		// Return change to rename the administrator
		return new AbstractChange<AdministratorModel>(administrator,
				"Rename administrator to " + newAdministratorName) {
			@Override
			public void apply() {
				administrator.setAdministratorName(newAdministratorName);
			}

			@Override
			public void revert() {
				administrator.setAdministratorName(oldAdministratorName);
			}
		};
	}

	@Override
	public Change<OfficeEscalationModel> addOfficeEscalation(
			String escalationType) {

		// TODO test this method (addOfficeEscalation)

		// Create the office escalation
		final OfficeEscalationModel escalation = new OfficeEscalationModel(
				escalationType);

		// Return change to add office escalation
		return new AbstractChange<OfficeEscalationModel>(escalation,
				"Add escalation") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addOfficeEscalation(escalation);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.removeOfficeEscalation(escalation);
			}
		};
	}

	@Override
	public Change<OfficeEscalationModel> removeOfficeEscalation(
			final OfficeEscalationModel officeEscalation) {

		// TODO test this method (removeOfficeEscalation)

		// Return change to remove office escalation
		return new AbstractChange<OfficeEscalationModel>(officeEscalation,
				"Remove escalation") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeOfficeEscalation(officeEscalation);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.addOfficeEscalation(officeEscalation);
			}
		};
	}

	@Override
	public Change<OfficeSectionResponsibilityModel> addOfficeSectionResponsibility(
			final OfficeSectionModel section,
			String officeSectionResponsibilityName) {

		// TODO test this method (addOfficeSectionResponsibility)

		// Create the office section responsibility
		final OfficeSectionResponsibilityModel responsibility = new OfficeSectionResponsibilityModel(
				officeSectionResponsibilityName);

		// Return change to add office section responsibility
		return new AbstractChange<OfficeSectionResponsibilityModel>(
				responsibility, "Add responsibility") {
			@Override
			public void apply() {
				section.addOfficeSectionResponsibility(responsibility);
			}

			@Override
			public void revert() {
				section.removeOfficeSectionResponsibility(responsibility);
			}
		};
	}

	@Override
	public Change<OfficeSectionResponsibilityModel> removeOfficeSectionResponsibility(
			final OfficeSectionResponsibilityModel officeSectionResponsibility) {

		// Find the office section containing the responsibility
		OfficeSectionModel officeSection = null;
		for (OfficeSectionModel section : this.office.getOfficeSections()) {
			for (OfficeSectionResponsibilityModel check : section
					.getOfficeSectionResponsibilities()) {
				if (check == officeSectionResponsibility) {
					officeSection = section;
				}
			}
		}
		if (officeSection == null) {
			// Must find section containing responsibility
			return new NoChange<OfficeSectionResponsibilityModel>(
					officeSectionResponsibility, "Remove responsibility",
					"Responsibility not in office");
		}

		// Return change to remove the responsibility
		final OfficeSectionModel section = officeSection;
		return new AbstractChange<OfficeSectionResponsibilityModel>(
				officeSectionResponsibility, "Remove responsibility") {
			@Override
			public void apply() {
				section
						.removeOfficeSectionResponsibility(officeSectionResponsibility);
			}

			@Override
			public void revert() {
				section
						.addOfficeSectionResponsibility(officeSectionResponsibility);
			}
		};
	}

	@Override
	public Change<OfficeSectionResponsibilityModel> renameOfficeSectionResponsibility(
			final OfficeSectionResponsibilityModel officeSectionResponsibility,
			final String newOfficeSectionResponsibilityName) {

		// TODO test this method (renameOfficeSectionResponsibility)

		// Obtain the old name
		final String oldOfficeSectionResponsibilityName = officeSectionResponsibility
				.getOfficeSectionResponsibilityName();

		// Return change to rename responsibility
		return new AbstractChange<OfficeSectionResponsibilityModel>(
				officeSectionResponsibility, "Rename responsibility to "
						+ newOfficeSectionResponsibilityName) {
			@Override
			public void apply() {
				officeSectionResponsibility
						.setOfficeSectionResponsibilityName(newOfficeSectionResponsibilityName);
			}

			@Override
			public void revert() {
				officeSectionResponsibility
						.setOfficeSectionResponsibilityName(oldOfficeSectionResponsibilityName);
			}
		};
	}

	@Override
	public Change<OfficeSectionObjectToExternalManagedObjectModel> linkOfficeSectionObjectToExternalManagedObject(
			OfficeSectionObjectModel officeSectionObject,
			ExternalManagedObjectModel externalManagedObject) {

		// TODO test this method
		// (linkOfficeSectionObjectToExternalManagedObject)

		// Create the connection
		final OfficeSectionObjectToExternalManagedObjectModel conn = new OfficeSectionObjectToExternalManagedObjectModel();
		conn.setOfficeSectionObject(officeSectionObject);
		conn.setExternalManagedObject(externalManagedObject);

		// Return change to add connection
		return new AbstractChange<OfficeSectionObjectToExternalManagedObjectModel>(
				conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<OfficeSectionObjectToExternalManagedObjectModel> removeOfficeSectionObjectToExternalManagedObject(
			final OfficeSectionObjectToExternalManagedObjectModel officeSectionObjectToExternalManagedObject) {

		// TODO test (removeOfficeSectionObjectToExternalManagedObject)

		// Return change to remove connection
		return new AbstractChange<OfficeSectionObjectToExternalManagedObjectModel>(
				officeSectionObjectToExternalManagedObject, "Remove") {
			@Override
			public void apply() {
				officeSectionObjectToExternalManagedObject.remove();
			}

			@Override
			public void revert() {
				officeSectionObjectToExternalManagedObject.connect();
			}
		};
	}

	@Override
	public Change<OfficeSectionOutputToOfficeSectionInputModel> linkOfficeSectionOutputToOfficeSectionInput(
			OfficeSectionOutputModel officeSectionOutput,
			OfficeSectionInputModel officeSectionInput) {

		// TODO test this method (linkOfficeSectionOutputToOfficeSectionInput)

		// Create the connection
		final OfficeSectionOutputToOfficeSectionInputModel conn = new OfficeSectionOutputToOfficeSectionInputModel();
		conn.setOfficeSectionOutput(officeSectionOutput);
		conn.setOfficeSectionInput(officeSectionInput);

		// Return change to add connection
		return new AbstractChange<OfficeSectionOutputToOfficeSectionInputModel>(
				conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<OfficeSectionOutputToOfficeSectionInputModel> removeOfficeSectionOutputToOfficeSectionInput(
			final OfficeSectionOutputToOfficeSectionInputModel officeSectionOutputToOfficeSectionInput) {

		// TODO test this method (removeOfficeSectionOutputToOfficeSectionInput)

		// Return change to remove the connection
		return new AbstractChange<OfficeSectionOutputToOfficeSectionInputModel>(
				officeSectionOutputToOfficeSectionInput, "Remove") {
			@Override
			public void apply() {
				officeSectionOutputToOfficeSectionInput.remove();
			}

			@Override
			public void revert() {
				officeSectionOutputToOfficeSectionInput.connect();
			}
		};
	}

	@Override
	public Change<OfficeSectionResponsibilityToOfficeTeamModel> linkOfficeSectionResponsibilityToOfficeTeam(
			OfficeSectionResponsibilityModel officeSectionResponsibility,
			OfficeTeamModel officeTeam) {

		// TODO test this method (linkOfficeSectionResponsibilityToOfficeTeam)

		// Create the connection
		final OfficeSectionResponsibilityToOfficeTeamModel conn = new OfficeSectionResponsibilityToOfficeTeamModel();
		conn.setOfficeSectionResponsibility(officeSectionResponsibility);
		conn.setOfficeTeam(officeTeam);

		// Return change to add the connection
		return new AbstractChange<OfficeSectionResponsibilityToOfficeTeamModel>(
				conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<OfficeSectionResponsibilityToOfficeTeamModel> removeOfficeSectionResponsibilityToOfficeTeam(
			final OfficeSectionResponsibilityToOfficeTeamModel officeSectionResponsibilityToOfficeTeam) {

		// TODO test this method (removeOfficeSectionResponsibilityToOfficeTeam)

		// Return change to remove connection
		return new AbstractChange<OfficeSectionResponsibilityToOfficeTeamModel>(
				officeSectionResponsibilityToOfficeTeam, "Remove") {
			@Override
			public void apply() {
				officeSectionResponsibilityToOfficeTeam.remove();
			}

			@Override
			public void revert() {
				officeSectionResponsibilityToOfficeTeam.connect();
			}
		};
	}

	@Override
	public Change<AdministratorToOfficeTeamModel> linkAdministratorToOfficeTeam(
			AdministratorModel administrator, OfficeTeamModel officeTeam) {

		// TODO test this method (linkAdministratorToOfficeTeam)

		// Create the connection
		final AdministratorToOfficeTeamModel conn = new AdministratorToOfficeTeamModel();
		conn.setAdministrator(administrator);
		conn.setOfficeTeam(officeTeam);

		// Return change to add connection
		return new AbstractChange<AdministratorToOfficeTeamModel>(conn,
				"Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<AdministratorToOfficeTeamModel> removeAdministratorToOfficeTeam(
			final AdministratorToOfficeTeamModel administratorToOfficeTeam) {

		// TODO test this method (removeAdministratorToOfficeTeam)

		// Return change to remove the connection
		return new AbstractChange<AdministratorToOfficeTeamModel>(
				administratorToOfficeTeam, "Remove") {
			@Override
			public void apply() {
				administratorToOfficeTeam.remove();
			}

			@Override
			public void revert() {
				administratorToOfficeTeam.connect();
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectToAdministratorModel> linkExternalManagedObjectToAdministrator(
			ExternalManagedObjectModel externalManagedObject,
			AdministratorModel administrator) {

		// TODO test this method (linkExternalManagedObjectToAdministrator)

		// Create the connection
		final ExternalManagedObjectToAdministratorModel conn = new ExternalManagedObjectToAdministratorModel();
		conn.setExternalManagedObject(externalManagedObject);
		conn.setAdministrator(administrator);

		// Return change to add the connection
		return new AbstractChange<ExternalManagedObjectToAdministratorModel>(
				conn, "Conect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectToAdministratorModel> removeExternalManagedObjectToAdministrator(
			final ExternalManagedObjectToAdministratorModel externalManagedObjectToAdministrator) {

		// TODO test this method (removeExternalManagedObjectToAdministrator)

		// Return change to remove the connection
		return new AbstractChange<ExternalManagedObjectToAdministratorModel>(
				externalManagedObjectToAdministrator, "Remove") {
			@Override
			public void apply() {
				externalManagedObjectToAdministrator.remove();
			}

			@Override
			public void revert() {
				externalManagedObjectToAdministrator.connect();
			}
		};
	}

	@Override
	public Change<OfficeTaskToPreDutyModel> linkOfficeTaskToPreDuty(
			final OfficeTask officeTask, final DutyModel duty,
			final OfficeSectionModel officeSectionModel,
			OfficeSection officeSection) {

		// TODO test this method (linkOfficeTaskToPreDuty)

		// Load the sub section hierarchy for the task
		final Deque<OfficeSubSection> hierarchy = new LinkedList<OfficeSubSection>();
		if (!(this.loadHierarchy(hierarchy, officeTask, officeSection))) {
			// Task not found in office section
			return new NoChange<OfficeTaskToPreDutyModel>(
					new OfficeTaskToPreDutyModel(), "Add pre-duty to task",
					"Can not find task '" + officeTask.getOfficeTaskName()
							+ "' in office section '"
							+ officeSection.getOfficeSectionName() + "'");
		}

		// Create the connection
		final OfficeTaskToPreDutyModel conn = new OfficeTaskToPreDutyModel();

		// Return change to link office task to pre duty
		return new AbstractChange<OfficeTaskToPreDutyModel>(conn, "Connect") {

			/**
			 * Clean {@link Change} instances.
			 */
			private List<Change<?>> cleanChanges = null;

			@Override
			public void apply() {

				// Determine if applying first time
				if (this.cleanChanges == null) {
					// Applying first time so add the hierarchy of sub sections
					OfficeSubSectionModel subSection = OfficeChangesImpl.this
							.addHierarchy(hierarchy, officeSectionModel);

					// Add the task to the sub section
					OfficeTaskModel task = OfficeChangesImpl.this
							.addOfficeTask(subSection, officeTask);

					// Add ends to the connection
					conn.setOfficeTask(task);
					conn.setDuty(duty);

				} else {
					// Re-applying so revert changes (adds back hierarchy)
					for (Change<?> change : this.cleanChanges) {
						change.revert();
					}
				}

				// Connect the connection
				conn.connect();
			}

			@Override
			public void revert() {
				// Remove the connection
				conn.remove();

				// Clean up sub section
				this.cleanChanges = new LinkedList<Change<?>>();
				OfficeChangesImpl.this.cleanSubSection(officeSectionModel
						.getOfficeSubSection(), this.cleanChanges);
			}
		};
	}

	@Override
	public Change<OfficeTaskToPreDutyModel> removeOfficeTaskToPreDuty(
			final OfficeTaskToPreDutyModel officeTaskToPreDuty) {

		// TODO test this method (removeOfficeTaskToPreDuty)

		// Obtain the office section containing the task
		final OfficeSectionModel officeSection = this
				.getContainingOfficeSection(officeTaskToPreDuty.getOfficeTask());

		// Return change to remove the connection
		return new AbstractChange<OfficeTaskToPreDutyModel>(
				officeTaskToPreDuty, "Remove") {

			/**
			 * Clean {@link Change} instances.
			 */
			private List<Change<?>> cleanChanges;

			@Override
			public void apply() {
				// Remove the connection
				officeTaskToPreDuty.remove();

				// Clean up sub section
				this.cleanChanges = new LinkedList<Change<?>>();
				OfficeChangesImpl.this.cleanSubSection(officeSection
						.getOfficeSubSection(), this.cleanChanges);
			}

			@Override
			public void revert() {
				// Revert the clean up of the sub section
				for (Change<?> change : this.cleanChanges) {
					change.revert();
				}

				// Re-add the connection
				officeTaskToPreDuty.connect();
			}
		};
	}

	@Override
	public Change<OfficeTaskToPostDutyModel> linkOfficeTaskToPostDuty(
			final OfficeTask officeTask, final DutyModel duty,
			final OfficeSectionModel officeSectionModel,
			OfficeSection officeSection) {

		// TODO test this method (linkOfficeTaskToPostDuty)

		// Load the sub section hierarchy for the task
		final Deque<OfficeSubSection> hierarchy = new LinkedList<OfficeSubSection>();
		if (!(this.loadHierarchy(hierarchy, officeTask, officeSection))) {
			// Task not found in office section
			return new NoChange<OfficeTaskToPostDutyModel>(
					new OfficeTaskToPostDutyModel(), "Add post-duty to task",
					"Can not find task '" + officeTask.getOfficeTaskName()
							+ "' in office section '"
							+ officeSection.getOfficeSectionName() + "'");
		}

		// Create the connection
		final OfficeTaskToPostDutyModel conn = new OfficeTaskToPostDutyModel();

		// Return change to link office task to post duty
		return new AbstractChange<OfficeTaskToPostDutyModel>(conn, "Connect") {

			/**
			 * Clean {@link Change} instances.
			 */
			private List<Change<?>> cleanChanges = null;

			@Override
			public void apply() {

				// Determine if applying first time
				if (this.cleanChanges == null) {
					// Applying first time so add the hierarchy of sub sections
					OfficeSubSectionModel subSection = OfficeChangesImpl.this
							.addHierarchy(hierarchy, officeSectionModel);

					// Add the task to the sub section
					OfficeTaskModel task = OfficeChangesImpl.this
							.addOfficeTask(subSection, officeTask);

					// Add ends to the connection
					conn.setOfficeTask(task);
					conn.setDuty(duty);

				} else {
					// Re-applying so revert changes (adds back hierarchy)
					for (Change<?> change : this.cleanChanges) {
						change.revert();
					}
				}

				// Connect the connection
				conn.connect();
			}

			@Override
			public void revert() {
				// Remove the connection
				conn.remove();

				// Clean up sub section
				this.cleanChanges = new LinkedList<Change<?>>();
				OfficeChangesImpl.this.cleanSubSection(officeSectionModel
						.getOfficeSubSection(), this.cleanChanges);
			}
		};
	}

	@Override
	public Change<OfficeTaskToPostDutyModel> removeOfficeTaskToPostDuty(
			final OfficeTaskToPostDutyModel officeTaskToPostDuty) {

		// TODO test this method (removeOfficeTaskToPostDuty)

		// Obtain the office section containing the task
		final OfficeSectionModel officeSection = this
				.getContainingOfficeSection(officeTaskToPostDuty
						.getOfficeTask());

		// Return change to remove the connection
		return new AbstractChange<OfficeTaskToPostDutyModel>(
				officeTaskToPostDuty, "Remove") {

			/**
			 * Clean {@link Change} instances.
			 */
			private List<Change<?>> cleanChanges;

			@Override
			public void apply() {
				// Remove the connection
				officeTaskToPostDuty.remove();

				// Clean up sub section
				this.cleanChanges = new LinkedList<Change<?>>();
				OfficeChangesImpl.this.cleanSubSection(officeSection
						.getOfficeSubSection(), this.cleanChanges);
			}

			@Override
			public void revert() {
				// Revert the clean up of the sub section
				for (Change<?> change : this.cleanChanges) {
					change.revert();
				}

				// Re-add the connection
				officeTaskToPostDuty.connect();
			}
		};
	}

}