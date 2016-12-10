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
package net.officefloor.model.impl.office;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.spi.office.OfficeSectionTask;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.DisconnectChange;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
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
import net.officefloor.model.office.PropertyModel;

/**
 * {@link OfficeChanges} implementation.
 * 
 * @author Daniel Sagenschneider
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
	 * Obtains the text name identifying the {@link ManagedObjectScope}.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 * @return Text name for the {@link ManagedObjectScope}.
	 */
	public static String getManagedObjectScope(ManagedObjectScope scope) {

		// Ensure have scope
		if (scope == null) {
			return null;
		}

		// Return the text of the scope
		switch (scope) {
		case PROCESS:
			return PROCESS_MANAGED_OBJECT_SCOPE;
		case THREAD:
			return THREAD_MANAGED_OBJECT_SCOPE;
		case WORK:
			return WORK_MANAGED_OBJECT_SCOPE;
		default:
			throw new IllegalStateException("Unknown scope " + scope);
		}
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

	/**
	 * Links the {@link OfficeTaskModel} to the {@link DutyModel}.
	 * 
	 * @param officeSectionModel
	 *            {@link OfficeSectionModel} containing the {@link OfficeSectionTask}.
	 * @param officeTaskType
	 *            {@link OfficeTaskType} of {@link OfficeSectionTask} to link to
	 *            {@link DutyModel}.
	 * @param duty
	 *            {@link DutyModel}.
	 * @param connectionModel
	 *            Specific {@link ConnectionModel}.
	 * @param connectConnection
	 *            Configures the {@link ConnectionModel} with end points.
	 * @return {@link Change} to link the {@link OfficeTaskModel} to the
	 *         {@link DutyModel}.
	 */
	private <C extends ConnectionModel> Change<C> linkOfficeTaskToDuty(
			OfficeSectionModel officeSectionModel,
			OfficeTaskType officeTaskType, DutyModel duty, C connectionModel,
			Consumer<OfficeTaskModel> connectConnection) {

		// Create hierarchy of sub section names (not include top level section)
		final Deque<String> subSectionNames = new LinkedList<String>();
		OfficeSubSectionType subSectionType = officeTaskType
				.getOfficeSubSectionType();
		while (subSectionType.getParentOfficeSubSectionType() != null) {
			subSectionNames.push(subSectionType.getOfficeSectionName());
			subSectionType = subSectionType.getParentOfficeSubSectionType();
		}

		// Find the lowest sub section in hierarchy
		OfficeSubSectionModel subSectionModel = officeSectionModel
				.getOfficeSubSection();
		while (!subSectionNames.isEmpty()) {
			String sectionName = subSectionNames.pop();
			OfficeSubSectionModel[] matches = subSectionModel
					.getOfficeSubSections()
					.stream()
					.filter((subSubSectionModel) -> (sectionName
							.equals(subSubSectionModel
									.getOfficeSubSectionName())))
					.toArray(OfficeSubSectionModel[]::new);
			if (matches.length > 0) {
				// Sub section already exists (continue to next level)
				subSectionModel = matches[0];

			} else {
				// New branch off sub section
				subSectionNames.push(sectionName);
			}
		}

		// Construct branch of sub sections
		OfficeSubSectionModel branchHead = null;
		OfficeSubSectionModel branchTail = null;
		while (!subSectionNames.isEmpty()) {
			String sectionName = subSectionNames.pop();
			OfficeSubSectionModel subSubSection = new OfficeSubSectionModel(
					sectionName);
			if (branchHead == null) {
				branchHead = subSubSection;
				branchTail = branchHead;
			} else {
				branchTail.addOfficeSubSection(subSubSection);
				branchTail = subSubSection;
			}
		}

		// Obtain the task
		String taskName = officeTaskType.getOfficeTaskName();
		OfficeTaskModel foundTask = null;
		if (branchHead == null) {
			OfficeTaskModel[] tasks = subSectionModel
					.getOfficeTasks()
					.stream()
					.filter((task) -> (taskName.equals(task.getOfficeTaskName())))
					.toArray(OfficeTaskModel[]::new);
			if (tasks.length > 0) {
				foundTask = tasks[0];
			}
		}

		// Return change to link office task to duty
		final OfficeSubSectionModel existingLeaf = subSectionModel;
		final OfficeSubSectionModel newHead = branchHead;
		final OfficeSubSectionModel newTail = branchTail;
		final OfficeTaskModel existingTask = foundTask;
		final OfficeTaskModel newTask = (foundTask != null ? null
				: new OfficeTaskModel(taskName));
		return new AbstractChange<C>(connectionModel, "Connect") {

			@Override
			public void apply() {

				// Add the branch
				OfficeSubSectionModel tail = existingLeaf;
				if (newHead != null) {
					existingLeaf.addOfficeSubSection(newHead);
					tail = newTail;
				}

				// Add the task
				OfficeTaskModel taskModel = existingTask;
				if (newTask != null) {
					tail.addOfficeTask(newTask);
					taskModel = newTask;

					// Keep the tasks ordered for the sub section
					Collections.sort(tail.getOfficeTasks(),
							new Comparator<OfficeTaskModel>() {
								@Override
								public int compare(OfficeTaskModel a,
										OfficeTaskModel b) {
									return a.getOfficeTaskName().compareTo(
											b.getOfficeTaskName());
								}
							});
				}

				// Connect the link
				connectConnection.accept(taskModel);

				// Connect the connection
				connectionModel.connect();
			}

			@Override
			public void revert() {
				// Remove the connection
				connectionModel.remove();

				// Remove the possible new task
				if (newTask != null) {
					OfficeSubSectionModel tail = existingLeaf;
					if (newHead != null) {
						tail = newTail;
					}
					tail.removeOfficeTask(newTask);
				}

				// Remove the possible branch
				if (newHead != null) {
					existingLeaf.removeOfficeSubSection(newHead);
				}
			}
		};
	}

	/*
	 * =================== OfficeChanges =========================
	 */

	@Override
	public Change<OfficeSectionModel> addOfficeSection(
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, OfficeSectionType officeSectionType) {

		// TODO test this method (addOfficeSection)

		// Create the office section model
		String sectionName = officeSectionType.getOfficeSectionName();
		final OfficeSectionModel section = new OfficeSectionModel(sectionName,
				sectionSourceClassName, sectionLocation);
		for (Property property : properties) {
			section.addProperty(new PropertyModel(property.getName(), property
					.getValue()));
		}

		// Add the inputs
		for (OfficeSectionInputType inputType : officeSectionType
				.getOfficeSectionInputTypes()) {
			section.addOfficeSectionInput(new OfficeSectionInputModel(inputType
					.getOfficeSectionInputName(), inputType.getParameterType()));
		}

		// Add the outputs
		for (OfficeSectionOutputType outputType : officeSectionType
				.getOfficeSectionOutputTypes()) {
			section.addOfficeSectionOutput(new OfficeSectionOutputModel(
					outputType.getOfficeSectionOutputName(), outputType
							.getArgumentType(), outputType.isEscalationOnly()));
		}

		// Add the objects
		for (OfficeSectionObjectType objectType : officeSectionType
				.getOfficeSectionObjectTypes()) {
			section.addOfficeSectionObject(new OfficeSectionObjectModel(
					objectType.getOfficeSectionObjectName(), objectType
							.getObjectType()));
		}

		// Add a responsibility for convenience
		section.addOfficeSectionResponsibility(new OfficeSectionResponsibilityModel(
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
	public Change<OfficeSectionModel> refactorOfficeSection(
			final OfficeSectionModel sectionModel, final String sectionName,
			final String sectionSourceClassName, final String sectionLocation,
			PropertyList properties, OfficeSectionType officeSectionType,
			Map<String, String> inputNameMapping,
			Map<String, String> outputNameMapping,
			Map<String, String> objectNameMapping) {

		// Create the list to contain all refactor changes
		final List<Change<?>> refactor = new LinkedList<Change<?>>();

		// ------------ Details of OfficeSectionModel -----------------

		// Add change to details of office section
		final String existingSectionName = sectionModel.getOfficeSectionName();
		final String existingSectionSourceClassName = sectionModel
				.getSectionSourceClassName();
		final String existingSectionLocation = sectionModel
				.getSectionLocation();
		refactor.add(new AbstractChange<OfficeSectionModel>(sectionModel,
				"Change office section details") {
			@Override
			public void apply() {
				sectionModel.setOfficeSectionName(sectionName);
				sectionModel.setSectionSourceClassName(sectionSourceClassName);
				sectionModel.setSectionLocation(sectionLocation);
			}

			@Override
			public void revert() {
				sectionModel.setOfficeSectionName(existingSectionName);
				sectionModel
						.setSectionSourceClassName(existingSectionSourceClassName);
				sectionModel.setSectionLocation(existingSectionLocation);
			}
		});

		// Add change to the properties
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(
				sectionModel.getProperties());
		final List<PropertyModel> newProperties = new LinkedList<PropertyModel>();
		for (Property property : properties) {
			newProperties.add(new PropertyModel(property.getName(), property
					.getValue()));
		}
		refactor.add(new AbstractChange<OfficeSectionModel>(sectionModel,
				"Change section properties") {
			@Override
			public void apply() {
				for (PropertyModel property : existingProperties) {
					sectionModel.removeProperty(property);
				}
				for (PropertyModel property : newProperties) {
					sectionModel.addProperty(property);
				}
			}

			@Override
			public void revert() {
				for (PropertyModel property : newProperties) {
					sectionModel.removeProperty(property);
				}
				for (PropertyModel property : existingProperties) {
					sectionModel.addProperty(property);
				}
			}
		});

		// ------------------- OfficeSectionObjects ---------------------------

		// Create the map of existing objects to their names
		Map<String, OfficeSectionObjectModel> existingObjectMapping = new HashMap<String, OfficeSectionObjectModel>();
		for (OfficeSectionObjectModel object : sectionModel
				.getOfficeSectionObjects()) {
			existingObjectMapping.put(object.getOfficeSectionObjectName(),
					object);
		}

		// Create the listing of target objects
		OfficeSectionObjectType[] objectTypes = officeSectionType
				.getOfficeSectionObjectTypes();
		final OfficeSectionObjectModel[] targetObjects = new OfficeSectionObjectModel[objectTypes.length];
		for (int o = 0; o < targetObjects.length; o++) {
			OfficeSectionObjectType objectType = objectTypes[o];

			// Obtain the details of the object
			final String objectName = objectType.getOfficeSectionObjectName();
			final String objectTypeName = objectType.getObjectType();

			// Obtain the object for object type (may need to create)
			OfficeSectionObjectModel findObject = this.getExistingItem(
					objectName, objectNameMapping, existingObjectMapping);
			final OfficeSectionObjectModel object = (findObject != null ? findObject
					: new OfficeSectionObjectModel(objectName, objectTypeName));
			targetObjects[o] = object;

			// Refactor details of object
			final String existingObjectName = object
					.getOfficeSectionObjectName();
			final String existingObjectTypeName = object.getObjectType();
			refactor.add(new AbstractChange<OfficeSectionObjectModel>(object,
					"Refactor office section object") {
				@Override
				public void apply() {
					object.setOfficeSectionObjectName(objectName);
					object.setObjectType(objectTypeName);
				}

				@Override
				public void revert() {
					object.setOfficeSectionObjectName(existingObjectName);
					object.setObjectType(existingObjectTypeName);
				}
			});
		}

		// Ensure target objects sorted by name
		Arrays.sort(targetObjects, new Comparator<OfficeSectionObjectModel>() {
			@Override
			public int compare(OfficeSectionObjectModel a,
					OfficeSectionObjectModel b) {
				return a.getOfficeSectionObjectName().compareTo(
						b.getOfficeSectionObjectName());
			}
		});

		// Obtain the existing objects
		final OfficeSectionObjectModel[] existingObjects = sectionModel
				.getOfficeSectionObjects().toArray(
						new OfficeSectionObjectModel[0]);

		// Add changes to disconnect existing objects to be removed
		Set<OfficeSectionObjectModel> targetObjectSet = new HashSet<OfficeSectionObjectModel>(
				Arrays.asList(targetObjects));
		for (OfficeSectionObjectModel existingObject : existingObjects) {
			if (!(targetObjectSet.contains(existingObject))) {
				// Add change to disconnect object
				final OfficeSectionObjectModel object = existingObject;
				refactor.add(new DisconnectChange<OfficeSectionObjectModel>(
						existingObject) {
					@Override
					protected void populateRemovedConnections(
							List<ConnectionModel> connList) {
						OfficeSectionObjectToExternalManagedObjectModel conn = object
								.getExternalManagedObject();
						if (conn != null) {
							conn.remove();
							connList.add(conn);
						}
					}
				});
			}
		}

		// Add change to refactor objects
		refactor.add(new AbstractChange<OfficeSectionModel>(sectionModel,
				"Refactor objects of office section") {
			@Override
			public void apply() {
				// Remove existing objects, add target objects
				for (OfficeSectionObjectModel object : existingObjects) {
					sectionModel.removeOfficeSectionObject(object);
				}
				for (OfficeSectionObjectModel object : targetObjects) {
					sectionModel.addOfficeSectionObject(object);
				}
			}

			@Override
			public void revert() {
				// Remove the target objects, add back existing
				for (OfficeSectionObjectModel object : targetObjects) {
					sectionModel.removeOfficeSectionObject(object);
				}
				for (OfficeSectionObjectModel object : existingObjects) {
					sectionModel.addOfficeSectionObject(object);
				}
			}
		});

		// ------------------- OfficeSectionInputs ---------------------------

		// Create the map of existing inputs to their names
		Map<String, OfficeSectionInputModel> existingInputMapping = new HashMap<String, OfficeSectionInputModel>();
		for (OfficeSectionInputModel object : sectionModel
				.getOfficeSectionInputs()) {
			existingInputMapping
					.put(object.getOfficeSectionInputName(), object);
		}

		// Create the listing of target inputs
		OfficeSectionInputType[] inputTypes = officeSectionType
				.getOfficeSectionInputTypes();
		final OfficeSectionInputModel[] targetInputs = new OfficeSectionInputModel[inputTypes.length];
		for (int i = 0; i < targetInputs.length; i++) {
			OfficeSectionInputType inputType = inputTypes[i];

			// Obtain the details of the input
			final String inputName = inputType.getOfficeSectionInputName();
			final String parameterTypeName = inputType.getParameterType();

			// Obtain the input for input type (may need to create)
			OfficeSectionInputModel findInput = this.getExistingItem(inputName,
					inputNameMapping, existingInputMapping);
			final OfficeSectionInputModel input = (findInput != null ? findInput
					: new OfficeSectionInputModel(inputName, parameterTypeName));
			targetInputs[i] = input;

			// Refactor details of input
			final String existingInputName = input.getOfficeSectionInputName();
			final String existingInputParameterTypeName = input
					.getParameterType();
			refactor.add(new AbstractChange<OfficeSectionInputModel>(input,
					"Refactor office section input") {
				@Override
				public void apply() {
					input.setOfficeSectionInputName(inputName);
					input.setParameterType(parameterTypeName);
				}

				@Override
				public void revert() {
					input.setOfficeSectionInputName(existingInputName);
					input.setParameterType(existingInputParameterTypeName);
				}
			});
		}

		// Ensure target inputs sorted by name
		Arrays.sort(targetInputs, new Comparator<OfficeSectionInputModel>() {
			@Override
			public int compare(OfficeSectionInputModel a,
					OfficeSectionInputModel b) {
				return a.getOfficeSectionInputName().compareTo(
						b.getOfficeSectionInputName());
			}
		});

		// Obtain the existing inputs
		final OfficeSectionInputModel[] existingInputs = sectionModel
				.getOfficeSectionInputs().toArray(
						new OfficeSectionInputModel[0]);

		// Add changes to disconnect existing inputs to be removed
		Set<OfficeSectionInputModel> targetInputSet = new HashSet<OfficeSectionInputModel>(
				Arrays.asList(targetInputs));
		for (OfficeSectionInputModel existingInput : existingInputs) {
			if (!(targetInputSet.contains(existingInput))) {
				// Add change to disconnect input
				final OfficeSectionInputModel input = existingInput;
				refactor.add(new DisconnectChange<OfficeSectionInputModel>(
						existingInput) {
					@Override
					protected void populateRemovedConnections(
							List<ConnectionModel> connList) {
						for (OfficeSectionOutputToOfficeSectionInputModel conn : new ArrayList<OfficeSectionOutputToOfficeSectionInputModel>(
								input.getOfficeSectionOutputs())) {
							conn.remove();
							connList.add(conn);
						}
					}
				});
			}
		}

		// Add change to refactor inputs
		refactor.add(new AbstractChange<OfficeSectionModel>(sectionModel,
				"Refactor inputs of office section") {
			@Override
			public void apply() {
				// Remove existing inputs, add target inputs
				for (OfficeSectionInputModel input : existingInputs) {
					sectionModel.removeOfficeSectionInput(input);
				}
				for (OfficeSectionInputModel input : targetInputs) {
					sectionModel.addOfficeSectionInput(input);
				}
			}

			@Override
			public void revert() {
				// Remove the target inputs, add back existing
				for (OfficeSectionInputModel input : targetInputs) {
					sectionModel.removeOfficeSectionInput(input);
				}
				for (OfficeSectionInputModel input : existingInputs) {
					sectionModel.addOfficeSectionInput(input);
				}
			}
		});

		// ------------------- OfficeSectionOutputs ---------------------------

		// Create the map of existing outputs to their names
		Map<String, OfficeSectionOutputModel> existingOutputMapping = new HashMap<String, OfficeSectionOutputModel>();
		for (OfficeSectionOutputModel object : sectionModel
				.getOfficeSectionOutputs()) {
			existingOutputMapping.put(object.getOfficeSectionOutputName(),
					object);
		}

		// Create the listing of target outputs
		OfficeSectionOutputType[] outputTypes = officeSectionType
				.getOfficeSectionOutputTypes();
		final OfficeSectionOutputModel[] targetOutputs = new OfficeSectionOutputModel[outputTypes.length];
		for (int o = 0; o < targetOutputs.length; o++) {
			OfficeSectionOutputType outputType = outputTypes[o];

			// Obtain the details of the output
			final String outputName = outputType.getOfficeSectionOutputName();
			final String argumentTypeName = outputType.getArgumentType();
			final boolean isEscalationOnly = outputType.isEscalationOnly();

			// Obtain the output for output type (may need to create)
			OfficeSectionOutputModel findOutput = this.getExistingItem(
					outputName, outputNameMapping, existingOutputMapping);
			final OfficeSectionOutputModel output = (findOutput != null ? findOutput
					: new OfficeSectionOutputModel(outputName,
							argumentTypeName, isEscalationOnly));
			targetOutputs[o] = output;

			// Refactor details of output
			final String existingOutputName = output
					.getOfficeSectionOutputName();
			final String existingOutputArgumentTypeName = output
					.getArgumentType();
			final boolean existingIsEscalationOnly = output.getEscalationOnly();
			refactor.add(new AbstractChange<OfficeSectionOutputModel>(output,
					"Refactor office section output") {
				@Override
				public void apply() {
					output.setOfficeSectionOutputName(outputName);
					output.setArgumentType(argumentTypeName);
					output.setEscalationOnly(isEscalationOnly);
				}

				@Override
				public void revert() {
					output.setOfficeSectionOutputName(existingOutputName);
					output.setArgumentType(existingOutputArgumentTypeName);
					output.setEscalationOnly(existingIsEscalationOnly);
				}
			});
		}

		// Ensure target outputs sorted by name
		Arrays.sort(targetOutputs, new Comparator<OfficeSectionOutputModel>() {
			@Override
			public int compare(OfficeSectionOutputModel a,
					OfficeSectionOutputModel b) {
				return a.getOfficeSectionOutputName().compareTo(
						b.getOfficeSectionOutputName());
			}
		});

		// Obtain the existing outputs
		final OfficeSectionOutputModel[] existingOutputs = sectionModel
				.getOfficeSectionOutputs().toArray(
						new OfficeSectionOutputModel[0]);

		// Add changes to disconnect existing outputs to be removed
		Set<OfficeSectionOutputModel> targetOutputSet = new HashSet<OfficeSectionOutputModel>(
				Arrays.asList(targetOutputs));
		for (OfficeSectionOutputModel existingOutput : existingOutputs) {
			if (!(targetOutputSet.contains(existingOutput))) {
				// Add change to disconnect output
				final OfficeSectionOutputModel output = existingOutput;
				refactor.add(new DisconnectChange<OfficeSectionOutputModel>(
						existingOutput) {
					@Override
					protected void populateRemovedConnections(
							List<ConnectionModel> connList) {
						OfficeSectionOutputToOfficeSectionInputModel conn = output
								.getOfficeSectionInput();
						if (conn != null) {
							conn.remove();
							connList.add(conn);
						}
					}
				});
			}
		}

		// Add change to refactor outputs
		refactor.add(new AbstractChange<OfficeSectionModel>(sectionModel,
				"Refactor outputs of office section") {
			@Override
			public void apply() {
				// Remove existing outputs, add target outputs
				for (OfficeSectionOutputModel output : existingOutputs) {
					sectionModel.removeOfficeSectionOutput(output);
				}
				for (OfficeSectionOutputModel output : targetOutputs) {
					sectionModel.addOfficeSectionOutput(output);
				}
			}

			@Override
			public void revert() {
				// Remove the target outputs, add back existing
				for (OfficeSectionOutputModel output : targetOutputs) {
					sectionModel.removeOfficeSectionOutput(output);
				}
				for (OfficeSectionOutputModel output : existingOutputs) {
					sectionModel.addOfficeSectionOutput(output);
				}
			}
		});

		// ----------------- Refactoring -------------------------------

		// Return change to do all the refactoring
		return new AbstractChange<OfficeSectionModel>(sectionModel,
				"Refactor office section") {
			@Override
			public void apply() {
				for (Change<?> change : refactor) {
					change.apply();
				}
			}

			@Override
			public void revert() {
				// Revert changes in reverse order as applied
				for (int i = (refactor.size() - 1); i >= 0; i--) {
					Change<?> change = refactor.get(i);
					change.revert();
				}
			}
		};
	}

	/**
	 * Obtains the existing item for the target name.
	 * 
	 * @param targetItemName
	 *            Target item name.
	 * @param targetToExistingName
	 *            Mapping of target item name to existing item name.
	 * @param existingNameToItem
	 *            Mapping of existing item name to the existing item.
	 */
	private <T> T getExistingItem(String targetItemName,
			Map<String, String> targetToExistingName,
			Map<String, T> existingNameToItem) {

		// Obtain the existing item name
		String existingItemName = targetToExistingName.get(targetItemName);
		if (existingItemName != null) {
			// Have existing name, so return existing item by name
			return existingNameToItem.get(existingItemName);
		} else {
			// No existing name, so no existing item
			return null;
		}
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
	public Change<OfficeManagedObjectSourceModel> addOfficeManagedObjectSource(
			String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties,
			long timeout, ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addOfficeManagedObjectSource)

		// Create the managed object source
		final OfficeManagedObjectSourceModel managedObjectSource = new OfficeManagedObjectSourceModel(
				managedObjectSourceName, managedObjectSourceClassName,
				managedObjectType.getObjectClass().getName(),
				String.valueOf(timeout));
		for (Property property : properties) {
			managedObjectSource.addProperty(new PropertyModel(property
					.getName(), property.getValue()));
		}

		// Add the flows for the managed object source
		for (ManagedObjectFlowType<?> flow : managedObjectType.getFlowTypes()) {
			managedObjectSource
					.addOfficeManagedObjectSourceFlow(new OfficeManagedObjectSourceFlowModel(
							flow.getFlowName(), flow.getArgumentType()
									.getName()));
		}

		// Add the teams for the managed object source
		for (ManagedObjectTeamType team : managedObjectType.getTeamTypes()) {
			managedObjectSource
					.addOfficeManagedObjectSourceTeam(new OfficeManagedObjectSourceTeamModel(
							team.getTeamName()));
		}

		// Return the change to add the managed object source
		return new AbstractChange<OfficeManagedObjectSourceModel>(
				managedObjectSource, "Add managed object source") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.addOfficeManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.removeOfficeManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectSourceModel> removeOfficeManagedObjectSource(
			final OfficeManagedObjectSourceModel managedObjectSource) {

		// TODO this this method (removeOfficeManagedObjectSource)

		// Return change to remove the managed object source
		return new AbstractChange<OfficeManagedObjectSourceModel>(
				managedObjectSource, "Remove managed object source") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeOfficeManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.addOfficeManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectSourceModel> renameOfficeManagedObjectSource(
			final OfficeManagedObjectSourceModel managedObjectSource,
			final String newManagedObjectSourceName) {

		// TODO test this method (renameOfficeManagedObjectSource)

		// Obtain the old managed object source name
		final String oldManagedObjectSourceName = managedObjectSource
				.getOfficeManagedObjectSourceName();

		// Return change to rename the managed object source
		return new AbstractChange<OfficeManagedObjectSourceModel>(
				managedObjectSource, "Rename managed object source to "
						+ newManagedObjectSourceName) {
			@Override
			public void apply() {
				managedObjectSource
						.setOfficeManagedObjectSourceName(newManagedObjectSourceName);
			}

			@Override
			public void revert() {
				managedObjectSource
						.setOfficeManagedObjectSourceName(oldManagedObjectSourceName);
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectModel> addOfficeManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope,
			OfficeManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addOfficeManagedObject)

		// Create the managed object
		final OfficeManagedObjectModel managedObject = new OfficeManagedObjectModel(
				managedObjectName, getManagedObjectScope(managedObjectScope));

		// Add the dependencies for the managed object
		for (ManagedObjectDependencyType<?> dependency : managedObjectType
				.getDependencyTypes()) {
			managedObject
					.addOfficeManagedObjectDependency(new OfficeManagedObjectDependencyModel(
							dependency.getDependencyName(), dependency
									.getDependencyType().getName()));
		}

		// Create connection to the managed object source
		final OfficeManagedObjectToOfficeManagedObjectSourceModel conn = new OfficeManagedObjectToOfficeManagedObjectSourceModel();
		conn.setOfficeManagedObject(managedObject);
		conn.setOfficeManagedObjectSource(managedObjectSource);

		// Return change to add the managed object
		return new AbstractChange<OfficeManagedObjectModel>(managedObject,
				"Add managed object") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.addOfficeManagedObject(managedObject);
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
				OfficeChangesImpl.this.office
						.removeOfficeManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectModel> removeOfficeManagedObject(
			final OfficeManagedObjectModel managedObject) {

		// TODO test this method (removeFloorManagedObject)

		// Return change to remove the managed object
		return new AbstractChange<OfficeManagedObjectModel>(managedObject,
				"Remove managed object") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office
						.removeOfficeManagedObject(managedObject);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office
						.addOfficeManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectModel> renameOfficeManagedObject(
			final OfficeManagedObjectModel managedObject,
			final String newManagedObjectName) {

		// TODO test this method (renameOfficeManagedObject)

		// Obtain the old managed object name
		final String oldManagedObjectName = managedObject
				.getOfficeManagedObjectName();

		// Return change to rename the managed object
		return new AbstractChange<OfficeManagedObjectModel>(managedObject,
				"Rename managed object to " + newManagedObjectName) {
			@Override
			public void apply() {
				managedObject.setOfficeManagedObjectName(newManagedObjectName);
			}

			@Override
			public void revert() {
				managedObject.setOfficeManagedObjectName(oldManagedObjectName);
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectModel> rescopeOfficeManagedObject(
			final OfficeManagedObjectModel managedObject,
			final ManagedObjectScope newManagedObjectScope) {

		// TODO test this method (rescopeOfficeManagedObject)

		// Obtain the new scope text
		final String newScope = getManagedObjectScope(newManagedObjectScope);

		// OBtain the old managed object scope
		final String oldScope = managedObject.getManagedObjectScope();

		// Return change to re-scope the managed object
		return new AbstractChange<OfficeManagedObjectModel>(managedObject,
				"Rescope managed object to " + newScope) {
			@Override
			public void apply() {
				managedObject.setManagedObjectScope(newScope);
			}

			@Override
			public void revert() {
				managedObject.setManagedObjectScope(oldScope);
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
	public Change<OfficeStartModel> addOfficeStart() {

		// TODO test this method (addOfficeStart)

		// Determine the next name for start
		final String startPrefix = "Start";
		int startIndex = 1;
		String startName;
		boolean isUnique;
		do {
			isUnique = true;

			// Determine if start name is unique
			startName = startPrefix + String.valueOf(startIndex++);
			for (OfficeStartModel start : this.office.getOfficeStarts()) {
				if (startName.equals(start.getStartName())) {
					isUnique = false;
				}
			}

		} while (!isUnique);

		// Create the Office with the unique name
		final OfficeStartModel start = new OfficeStartModel(startName);

		// Return change to add team
		return new AbstractChange<OfficeStartModel>(start, "Add start") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.addOfficeStart(start);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.removeOfficeStart(start);
			}
		};
	}

	@Override
	public Change<OfficeStartModel> removeOfficeStart(
			final OfficeStartModel officeStart) {

		// TODO test this method (removeOfficeStart)

		// Return change to remove start
		return new AbstractChange<OfficeStartModel>(officeStart, "Remove start") {
			@Override
			public void apply() {
				OfficeChangesImpl.this.office.removeOfficeStart(officeStart);
			}

			@Override
			public void revert() {
				OfficeChangesImpl.this.office.addOfficeStart(officeStart);
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
				section.removeOfficeSectionResponsibility(officeSectionResponsibility);
			}

			@Override
			public void revert() {
				section.addOfficeSectionResponsibility(officeSectionResponsibility);
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

		// TODO test (linkOfficeSectionObjectToExternalManagedObject)

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
	public Change<OfficeSectionObjectToOfficeManagedObjectModel> linkOfficeSectionObjectToOfficeManagedObject(
			OfficeSectionObjectModel officeSectionObject,
			OfficeManagedObjectModel officeManagedObject) {

		// TODO test this method (linkOfficeSectionObjectToOfficeManagedObject)

		// Create the connection
		final OfficeSectionObjectToOfficeManagedObjectModel conn = new OfficeSectionObjectToOfficeManagedObjectModel();
		conn.setOfficeSectionObject(officeSectionObject);
		conn.setOfficeManagedObject(officeManagedObject);

		// Return change to add connection
		return new AbstractChange<OfficeSectionObjectToOfficeManagedObjectModel>(
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
	public Change<OfficeSectionObjectToOfficeManagedObjectModel> removeOfficeSectionObjectToOfficeManagedObject(
			final OfficeSectionObjectToOfficeManagedObjectModel officeSectionObjectToOfficeManagedObject) {

		// TODO test (removeOfficeSectionObjectToOfficeManagedObject)

		// Return change to remove connection
		return new AbstractChange<OfficeSectionObjectToOfficeManagedObjectModel>(
				officeSectionObjectToOfficeManagedObject, "Remove") {
			@Override
			public void apply() {
				officeSectionObjectToOfficeManagedObject.remove();
			}

			@Override
			public void revert() {
				officeSectionObjectToOfficeManagedObject.connect();
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectDependencyToOfficeManagedObjectModel> linkOfficeManagedObjectDependencyToOfficeManagedObject(
			OfficeManagedObjectDependencyModel dependency,
			OfficeManagedObjectModel managedObject) {

		// TODO test (linkOfficeManagedObjectDependencyToOfficeManagedObject)

		// Create the connection
		final OfficeManagedObjectDependencyToOfficeManagedObjectModel conn = new OfficeManagedObjectDependencyToOfficeManagedObjectModel();
		conn.setOfficeManagedObjectDependency(dependency);
		conn.setOfficeManagedObject(managedObject);

		// Return change to add connection
		return new AbstractChange<OfficeManagedObjectDependencyToOfficeManagedObjectModel>(
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
	public Change<OfficeManagedObjectDependencyToOfficeManagedObjectModel> removeOfficeManagedObjectDependencyToOfficeManagedObject(
			final OfficeManagedObjectDependencyToOfficeManagedObjectModel officeManagedObjectDependencyToOfficeManagedObject) {

		// TODO test (removeOfficeManagedObjectDependencyToOfficeManagedObject)

		// Return change to remove connection
		return new AbstractChange<OfficeManagedObjectDependencyToOfficeManagedObjectModel>(
				officeManagedObjectDependencyToOfficeManagedObject, "Remove") {
			@Override
			public void apply() {
				officeManagedObjectDependencyToOfficeManagedObject.remove();
			}

			@Override
			public void revert() {
				officeManagedObjectDependencyToOfficeManagedObject.connect();
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectDependencyToExternalManagedObjectModel> linkOfficeManagedObjectDependencyToExternalManagedObject(
			OfficeManagedObjectDependencyModel dependency,
			ExternalManagedObjectModel externalManagedObject) {

		// TODO test (linkOfficeManagedObjectDependencyToExternalManagedObject)

		// Create the connection
		final OfficeManagedObjectDependencyToExternalManagedObjectModel conn = new OfficeManagedObjectDependencyToExternalManagedObjectModel();
		conn.setOfficeManagedObjectDependency(dependency);
		conn.setExternalManagedObject(externalManagedObject);

		// Return the change to add the connection
		return new AbstractChange<OfficeManagedObjectDependencyToExternalManagedObjectModel>(
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
	public Change<OfficeManagedObjectDependencyToExternalManagedObjectModel> removeOfficeManagedObjectDependencyToExternalManagedObject(
			final OfficeManagedObjectDependencyToExternalManagedObjectModel officeManagedObjectDependencyToExternalManagedObject) {

		// TODO test
		// (removeOfficeManagedObjectDependencyToExternalManagedObject)

		// Return change to remove the connection
		return new AbstractChange<OfficeManagedObjectDependencyToExternalManagedObjectModel>(
				officeManagedObjectDependencyToExternalManagedObject, "Remove") {
			@Override
			public void apply() {
				officeManagedObjectDependencyToExternalManagedObject.remove();
			}

			@Override
			public void revert() {
				officeManagedObjectDependencyToExternalManagedObject.connect();
			}
		};
	}

	@Override
	public Change<OfficeManagedObjectSourceFlowToOfficeSectionInputModel> linkOfficeManagedObjectSourceFlowToOfficeSectionInput(
			OfficeManagedObjectSourceFlowModel managedObjectSourceFlow,
			OfficeSectionInputModel officeSectionInput) {

		// TODO test (linkOfficeManagedObjectSourceFlowToOfficeSectionInput)

		// Create the connection
		final OfficeManagedObjectSourceFlowToOfficeSectionInputModel conn = new OfficeManagedObjectSourceFlowToOfficeSectionInputModel();
		conn.setOfficeManagedObjectSourceFlow(managedObjectSourceFlow);
		conn.setOfficeSectionInput(officeSectionInput);

		// Return change to add the connection
		return new AbstractChange<OfficeManagedObjectSourceFlowToOfficeSectionInputModel>(
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
	public Change<OfficeManagedObjectSourceFlowToOfficeSectionInputModel> removeOfficeManagedObjectSourceFlowToOfficeSectionInput(
			final OfficeManagedObjectSourceFlowToOfficeSectionInputModel managedObjectSourceFlowToOfficeSectionInput) {

		// TODO test (removeOfficeManagedObjectSourceFlowToOfficeSectionInput)

		// Return change to remove the connection
		return new AbstractChange<OfficeManagedObjectSourceFlowToOfficeSectionInputModel>(
				managedObjectSourceFlowToOfficeSectionInput, "Remove") {
			@Override
			public void apply() {
				managedObjectSourceFlowToOfficeSectionInput.remove();
			}

			@Override
			public void revert() {
				managedObjectSourceFlowToOfficeSectionInput.connect();
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
	public Change<OfficeManagedObjectSourceTeamToOfficeTeamModel> linkOfficeManagedObjectSourceTeamToOfficeTeam(
			OfficeManagedObjectSourceTeamModel mosTeam,
			OfficeTeamModel officeTeam) {

		// TODO test this method (linkOfficeManagedObjectSourceTeamToOfficeTeam)

		// Create the connection
		final OfficeManagedObjectSourceTeamToOfficeTeamModel conn = new OfficeManagedObjectSourceTeamToOfficeTeamModel();
		conn.setOfficeManagedObjectSourceTeam(mosTeam);
		conn.setOfficeTeam(officeTeam);

		// Return change to add the connection
		return new AbstractChange<OfficeManagedObjectSourceTeamToOfficeTeamModel>(
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
	public Change<OfficeManagedObjectSourceTeamToOfficeTeamModel> removeOfficeManagedObjectSourceTeamToOfficeTeam(
			final OfficeManagedObjectSourceTeamToOfficeTeamModel officeManagedObjectSourceTeamToOfficeTeam) {

		// TODO test (removeOfficeManagedObjectSourceTeamToOfficeTeam)

		// Return change to remove the connection
		return new AbstractChange<OfficeManagedObjectSourceTeamToOfficeTeamModel>(
				officeManagedObjectSourceTeamToOfficeTeam, "Remove") {
			@Override
			public void apply() {
				officeManagedObjectSourceTeamToOfficeTeam.remove();
			}

			@Override
			public void revert() {
				officeManagedObjectSourceTeamToOfficeTeam.connect();
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
	public Change<OfficeManagedObjectToAdministratorModel> linkOfficeManagedObjectToAdministrator(
			OfficeManagedObjectModel managedObject,
			AdministratorModel administrator) {

		// TODO test this method (linkOfficeManagedObjectToAdministrator)

		// Create the connection
		final OfficeManagedObjectToAdministratorModel conn = new OfficeManagedObjectToAdministratorModel();
		conn.setOfficeManagedObject(managedObject);
		conn.setAdministrator(administrator);

		// Return change to add the connection
		return new AbstractChange<OfficeManagedObjectToAdministratorModel>(
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
	public Change<OfficeManagedObjectToAdministratorModel> removeOfficeManagedObjectToAdministrator(
			final OfficeManagedObjectToAdministratorModel managedObjectToAdministrator) {

		// TODO test this method (removeOfficeManagedObjectToAdministrator)

		// Return change to remove the connection
		return new AbstractChange<OfficeManagedObjectToAdministratorModel>(
				managedObjectToAdministrator, "Remove") {
			@Override
			public void apply() {
				managedObjectToAdministrator.remove();
			}

			@Override
			public void revert() {
				managedObjectToAdministrator.connect();
			}
		};
	}

	@Override
	public Change<OfficeTaskToPreDutyModel> linkOfficeTaskToPreDuty(
			OfficeSectionModel officeSectionModel,
			final OfficeTaskType officeTaskType, final DutyModel duty) {

		// TODO test this method (linkOfficeTaskToPreDuty)

		// Create the connection
		final OfficeTaskToPreDutyModel connectionModel = new OfficeTaskToPreDutyModel();

		// Return change to link office task to post duty
		return this.linkOfficeTaskToDuty(officeSectionModel, officeTaskType,
				duty, connectionModel, (officeTaskModel) -> {
					connectionModel.setOfficeTask(officeTaskModel);
					connectionModel.setDuty(duty);
				});
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
				OfficeChangesImpl.this.cleanSubSection(
						officeSection.getOfficeSubSection(), this.cleanChanges);
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
			OfficeSectionModel officeSectionModel,
			final OfficeTaskType officeTaskType, final DutyModel duty) {

		// TODO test this method (linkOfficeTaskToPostDuty)

		// Create the connection
		final OfficeTaskToPostDutyModel connectionModel = new OfficeTaskToPostDutyModel();

		// Return change to link office task to post duty
		return this.linkOfficeTaskToDuty(officeSectionModel, officeTaskType,
				duty, connectionModel, (officeTaskModel) -> {
					connectionModel.setOfficeTask(officeTaskModel);
					connectionModel.setDuty(duty);
				});
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
				OfficeChangesImpl.this.cleanSubSection(
						officeSection.getOfficeSubSection(), this.cleanChanges);
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

	@Override
	public Change<OfficeEscalationToOfficeSectionInputModel> linkOfficeEscalationToOfficeSectionInput(
			OfficeEscalationModel escalation,
			OfficeSectionInputModel sectionInput) {

		// TODO test this method (linkOfficeEscalationToOfficeSectionInput)

		// Create the connection
		final OfficeEscalationToOfficeSectionInputModel conn = new OfficeEscalationToOfficeSectionInputModel();
		conn.setOfficeEscalation(escalation);
		conn.setOfficeSectionInput(sectionInput);

		// Return change to add the connection
		return new AbstractChange<OfficeEscalationToOfficeSectionInputModel>(
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
	public Change<OfficeEscalationToOfficeSectionInputModel> removeOfficeEscalationToOfficeSectionInput(
			final OfficeEscalationToOfficeSectionInputModel escalationToSectionInput) {

		// TODO test this method (removeOfficeEscalationToOfficeSectionInput)

		// Return change to remove the connection
		return new AbstractChange<OfficeEscalationToOfficeSectionInputModel>(
				escalationToSectionInput, "Remove") {
			@Override
			public void apply() {
				escalationToSectionInput.remove();
			}

			@Override
			public void revert() {
				escalationToSectionInput.connect();
			}
		};
	}

	@Override
	public Change<OfficeStartToOfficeSectionInputModel> linkOfficeStartToOfficeSectionInput(
			OfficeStartModel start, OfficeSectionInputModel sectionInput) {

		// TODO test this method (linkOfficeStartToOfficeSectionInput)

		// Create the connection
		final OfficeStartToOfficeSectionInputModel conn = new OfficeStartToOfficeSectionInputModel();
		conn.setOfficeStart(start);
		conn.setOfficeSectionInput(sectionInput);

		// Return change to add the connection
		return new AbstractChange<OfficeStartToOfficeSectionInputModel>(conn,
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
	public Change<OfficeStartToOfficeSectionInputModel> removeOfficeStartToOfficeSectionInput(
			final OfficeStartToOfficeSectionInputModel startToSectionInput) {

		// TODO test this method (removeOfficeStartToOfficeSectionInput)

		// Return change to remove the connection
		return new AbstractChange<OfficeStartToOfficeSectionInputModel>(
				startToSectionInput, "Remove") {
			@Override
			public void apply() {
				startToSectionInput.remove();
			}

			@Override
			public void revert() {
				startToSectionInput.connect();
			}
		};
	}

}