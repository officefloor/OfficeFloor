/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.impl.officefloor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.DisconnectChange;
import net.officefloor.model.impl.change.NoChange;
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
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TypeQualificationModel;

/**
 * {@link OfficeFloorChanges} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorChangesImpl implements OfficeFloorChanges {

	/**
	 * {@link OfficeFloorModel}.
	 */
	private final OfficeFloorModel officeFloor;

	/**
	 * Initiate.
	 * 
	 * @param officeFloor {@link OfficeFloorModel}.
	 */
	public OfficeFloorChangesImpl(OfficeFloorModel officeFloor) {
		this.officeFloor = officeFloor;
	}

	/**
	 * Obtains the text name identifying the {@link ManagedObjectScope}.
	 * 
	 * @param scope {@link ManagedObjectScope}.
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
		case FUNCTION:
			return FUNCTION_MANAGED_OBJECT_SCOPE;
		default:
			throw new IllegalStateException("Unknown scope " + scope);
		}
	}

	/*
	 * ================= OfficeFloorChanges ==============================
	 */

	@Override
	public Change<DeployedOfficeModel> addDeployedOffice(String officeName, String officeSourceClassName,
			String officeLocation, PropertyList propertyList, OfficeType officeType) {

		// TODO test this method (addDeployedOffice)

		// Create the deployed office
		final DeployedOfficeModel office = new DeployedOfficeModel(officeName, officeSourceClassName, officeLocation);
		for (Property property : propertyList) {
			office.addProperty(new PropertyModel(property.getName(), property.getValue()));
		}

		// Add the inputs for the office
		for (OfficeAvailableSectionInputType input : officeType.getOfficeSectionInputTypes()) {
			office.addDeployedOfficeInput(new DeployedOfficeInputModel(input.getOfficeSectionName(),
					input.getOfficeSectionInputName(), input.getParameterType()));
		}

		// Add the teams for the office
		for (OfficeTeamType team : officeType.getOfficeTeamTypes()) {
			office.addDeployedOfficeTeam(new DeployedOfficeTeamModel(team.getOfficeTeamName()));
		}

		// Add the objects for the office
		for (OfficeManagedObjectType managedObject : officeType.getOfficeManagedObjectTypes()) {
			office.addDeployedOfficeObject(new DeployedOfficeObjectModel(managedObject.getOfficeManagedObjectName(),
					managedObject.getObjectType()));
		}

		// Return the change to add the office
		return new AbstractChange<DeployedOfficeModel>(office, "Add office") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.addDeployedOffice(office);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.removeDeployedOffice(office);
			}
		};
	}

	@Override
	public Change<DeployedOfficeModel> removeDeployedOffice(final DeployedOfficeModel deployedOffice) {

		// TODO test this method (removeDeployedOffice)

		// Return change to remove the office
		return new AbstractChange<DeployedOfficeModel>(deployedOffice, "Remove office") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.removeDeployedOffice(deployedOffice);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.addDeployedOffice(deployedOffice);
			}
		};
	}

	@Override
	public Change<DeployedOfficeModel> renameDeployedOffice(final DeployedOfficeModel deployedOffice,
			final String newDeployedOfficeName) {

		// TODO test this method (renameDeployedOffice)

		// Obtain the old name
		final String oldDeployedOfficeName = deployedOffice.getDeployedOfficeName();

		// Return change to rename the office
		return new AbstractChange<DeployedOfficeModel>(deployedOffice, "Rename office to " + newDeployedOfficeName) {
			@Override
			public void apply() {
				deployedOffice.setDeployedOfficeName(newDeployedOfficeName);
			}

			@Override
			public void revert() {
				deployedOffice.setDeployedOfficeName(oldDeployedOfficeName);
			}
		};
	}

	@Override
	public Change<DeployedOfficeModel> refactorDeployedOffice(final DeployedOfficeModel office, final String officeName,
			final String officeSourceClassName, final String officeLocation, PropertyList properties,
			OfficeType officeType, Map<String, String> objectNameMapping, Map<String, String> inputNameMapping,
			Map<String, String> teamNameMapping) {

		// Create the list to contain all refactor changes
		final List<Change<?>> refactor = new LinkedList<Change<?>>();

		// ------------ Details of DeployedOfficeModel -----------------

		// Add change for office details
		final String existingOfficeName = office.getDeployedOfficeName();
		final String existingOfficeSourceClassName = office.getOfficeSourceClassName();
		final String existingOfficeLocation = office.getOfficeLocation();
		refactor.add(new AbstractChange<DeployedOfficeModel>(office, "Change office details") {
			@Override
			public void apply() {
				office.setDeployedOfficeName(officeName);
				office.setOfficeSourceClassName(officeSourceClassName);
				office.setOfficeLocation(officeLocation);
			}

			@Override
			public void revert() {
				office.setDeployedOfficeName(existingOfficeName);
				office.setOfficeSourceClassName(existingOfficeSourceClassName);
				office.setOfficeLocation(existingOfficeLocation);
			}
		});

		// Add change to the properties
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(office.getProperties());
		final List<PropertyModel> newProperties = new LinkedList<PropertyModel>();
		for (Property property : properties) {
			newProperties.add(new PropertyModel(property.getName(), property.getValue()));
		}
		refactor.add(new AbstractChange<DeployedOfficeModel>(office, "Change office properties") {
			@Override
			public void apply() {
				for (PropertyModel property : existingProperties) {
					office.removeProperty(property);
				}
				for (PropertyModel property : newProperties) {
					office.addProperty(property);
				}
			}

			@Override
			public void revert() {
				for (PropertyModel property : newProperties) {
					office.removeProperty(property);
				}
				for (PropertyModel property : existingProperties) {
					office.addProperty(property);
				}
			}
		});

		// ------------------- OfficeSectionObjects ---------------------------

		// Create the map of existing objects to their names
		Map<String, DeployedOfficeObjectModel> existingObjectMapping = new HashMap<String, DeployedOfficeObjectModel>();
		for (DeployedOfficeObjectModel object : office.getDeployedOfficeObjects()) {
			existingObjectMapping.put(object.getDeployedOfficeObjectName(), object);
		}

		// Create the listing of target objects
		OfficeManagedObjectType[] objectTypes = officeType.getOfficeManagedObjectTypes();
		final DeployedOfficeObjectModel[] targetObjects = new DeployedOfficeObjectModel[objectTypes.length];
		for (int o = 0; o < targetObjects.length; o++) {
			OfficeManagedObjectType objectType = objectTypes[o];

			// Obtain the details of the object
			final String objectName = objectType.getOfficeManagedObjectName();
			final String objectTypeName = objectType.getObjectType();
			// TODO refactor extension interfaces

			// Obtain the object for object type (may need to create)
			DeployedOfficeObjectModel findObject = this.getExistingItem(objectName, objectNameMapping,
					existingObjectMapping);
			final DeployedOfficeObjectModel object = (findObject != null ? findObject
					: new DeployedOfficeObjectModel(objectName, objectTypeName));
			targetObjects[o] = object;

			// Refactor details of object
			final String existingObjectName = object.getDeployedOfficeObjectName();
			final String existingObjectTypeName = object.getObjectType();
			refactor.add(new AbstractChange<DeployedOfficeObjectModel>(object, "Refactor office object") {
				@Override
				public void apply() {
					object.setDeployedOfficeObjectName(objectName);
					object.setObjectType(objectTypeName);
				}

				@Override
				public void revert() {
					object.setDeployedOfficeObjectName(existingObjectName);
					object.setObjectType(existingObjectTypeName);
				}
			});
		}

		// Ensure target objects sorted by name
		Arrays.sort(targetObjects, new Comparator<DeployedOfficeObjectModel>() {
			@Override
			public int compare(DeployedOfficeObjectModel a, DeployedOfficeObjectModel b) {
				return a.getDeployedOfficeObjectName().compareTo(b.getDeployedOfficeObjectName());
			}
		});

		// Obtain the existing objects
		final DeployedOfficeObjectModel[] existingObjects = office.getDeployedOfficeObjects()
				.toArray(new DeployedOfficeObjectModel[0]);

		// Add changes to disconnect existing objects to be removed
		Set<DeployedOfficeObjectModel> targetObjectSet = new HashSet<DeployedOfficeObjectModel>(
				Arrays.asList(targetObjects));
		for (DeployedOfficeObjectModel existingObject : existingObjects) {
			if (!(targetObjectSet.contains(existingObject))) {
				// Add change to disconnect object
				final DeployedOfficeObjectModel object = existingObject;
				refactor.add(new DisconnectChange<DeployedOfficeObjectModel>(existingObject) {
					@Override
					protected void populateRemovedConnections(List<ConnectionModel> connList) {
						DeployedOfficeObjectToOfficeFloorManagedObjectModel conn = object.getOfficeFloorManagedObject();
						if (conn != null) {
							conn.remove();
							connList.add(conn);
						}
					}
				});
			}
		}

		// Add change to refactor objects
		refactor.add(new AbstractChange<DeployedOfficeModel>(office, "Refactor objects of office") {
			@Override
			public void apply() {
				// Remove existing objects, add target objects
				for (DeployedOfficeObjectModel object : existingObjects) {
					office.removeDeployedOfficeObject(object);
				}
				for (DeployedOfficeObjectModel object : targetObjects) {
					office.addDeployedOfficeObject(object);
				}
			}

			@Override
			public void revert() {
				// Remove the target objects, add back existing
				for (DeployedOfficeObjectModel object : targetObjects) {
					office.removeDeployedOfficeObject(object);
				}
				for (DeployedOfficeObjectModel object : existingObjects) {
					office.addDeployedOfficeObject(object);
				}
			}
		});

		// ------------------- OfficeSectionInputs ---------------------------

		// Create the map of existing inputs to their names
		Map<String, DeployedOfficeInputModel> existingInputMapping = new HashMap<String, DeployedOfficeInputModel>();
		for (DeployedOfficeInputModel input : office.getDeployedOfficeInputs()) {
			String sectionInputName = input.getSectionName() + SECTION_INPUT_SEPARATOR + input.getSectionInputName();
			existingInputMapping.put(sectionInputName, input);
		}

		// Create the listing of target inputs
		OfficeAvailableSectionInputType[] inputTypes = officeType.getOfficeSectionInputTypes();
		final DeployedOfficeInputModel[] targetInputs = new DeployedOfficeInputModel[inputTypes.length];
		for (int i = 0; i < targetInputs.length; i++) {
			OfficeAvailableSectionInputType inputType = inputTypes[i];

			// Obtain the details of the input
			final String sectionName = inputType.getOfficeSectionName();
			final String inputName = inputType.getOfficeSectionInputName();
			final String sectionInputName = sectionName + SECTION_INPUT_SEPARATOR + inputName;
			final String parameterTypeName = inputType.getParameterType();

			// Obtain the input for input type (may need to create)
			DeployedOfficeInputModel findInput = this.getExistingItem(sectionInputName, inputNameMapping,
					existingInputMapping);
			final DeployedOfficeInputModel input = (findInput != null ? findInput
					: new DeployedOfficeInputModel(sectionName, inputName, parameterTypeName));
			targetInputs[i] = input;

			// Refactor details of input
			final String existingSectionName = input.getSectionName();
			final String existingInputName = input.getSectionInputName();
			final String existingParmeterTypeName = input.getParameterType();
			refactor.add(new AbstractChange<DeployedOfficeInputModel>(input, "Refactor office input") {
				@Override
				public void apply() {
					input.setSectionName(sectionName);
					input.setSectionInputName(inputName);
					input.setParameterType(parameterTypeName);
				}

				@Override
				public void revert() {
					input.setSectionName(existingSectionName);
					input.setSectionInputName(existingInputName);
					input.setParameterType(existingParmeterTypeName);
				}
			});
		}

		// Ensure target inputs sorted by name
		Arrays.sort(targetInputs, new Comparator<DeployedOfficeInputModel>() {
			@Override
			public int compare(DeployedOfficeInputModel a, DeployedOfficeInputModel b) {
				return (a.getSectionName() + SECTION_INPUT_SEPARATOR + a.getSectionInputName())
						.compareTo(b.getSectionName() + SECTION_INPUT_SEPARATOR + b.getSectionInputName());
			}
		});

		// Obtain the existing inputs
		final DeployedOfficeInputModel[] existingInputs = office.getDeployedOfficeInputs()
				.toArray(new DeployedOfficeInputModel[0]);

		// Add changes to disconnect existing inputs to be removed
		Set<DeployedOfficeInputModel> targetInputSet = new HashSet<DeployedOfficeInputModel>(
				Arrays.asList(targetInputs));
		for (DeployedOfficeInputModel existingInput : existingInputs) {
			if (!(targetInputSet.contains(existingInput))) {
				// Add change to disconnect input
				final DeployedOfficeInputModel input = existingInput;
				refactor.add(new DisconnectChange<DeployedOfficeInputModel>(existingInput) {
					@Override
					protected void populateRemovedConnections(List<ConnectionModel> connList) {
						for (OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel conn : new ArrayList<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel>(
								input.getOfficeFloorManagedObjectSourceFlows())) {
							conn.remove();
							connList.add(conn);
						}
					}
				});
			}
		}

		// Add change to refactor inputs
		refactor.add(new AbstractChange<DeployedOfficeModel>(office, "Refactor inputs of office") {
			@Override
			public void apply() {
				// Remove existing inputs, add target inputs
				for (DeployedOfficeInputModel input : existingInputs) {
					office.removeDeployedOfficeInput(input);
				}
				for (DeployedOfficeInputModel input : targetInputs) {
					office.addDeployedOfficeInput(input);
				}
			}

			@Override
			public void revert() {
				// Remove the target inputs, add back existing
				for (DeployedOfficeInputModel input : targetInputs) {
					office.removeDeployedOfficeInput(input);
				}
				for (DeployedOfficeInputModel input : existingInputs) {
					office.addDeployedOfficeInput(input);
				}
			}
		});

		// ------------------- OfficeSectionTeams ---------------------------

		// Create the map of existing teams to their names
		Map<String, DeployedOfficeTeamModel> existingTeamMapping = new HashMap<String, DeployedOfficeTeamModel>();
		for (DeployedOfficeTeamModel team : office.getDeployedOfficeTeams()) {
			existingTeamMapping.put(team.getDeployedOfficeTeamName(), team);
		}

		// Create the listing of target teams
		OfficeTeamType[] teamTypes = officeType.getOfficeTeamTypes();
		final DeployedOfficeTeamModel[] targetTeams = new DeployedOfficeTeamModel[teamTypes.length];
		for (int i = 0; i < targetTeams.length; i++) {
			OfficeTeamType teamType = teamTypes[i];

			// Obtain the details of the team
			final String teamName = teamType.getOfficeTeamName();

			// Obtain the team for team type (may need to create)
			DeployedOfficeTeamModel findTeam = this.getExistingItem(teamName, teamNameMapping, existingTeamMapping);
			final DeployedOfficeTeamModel team = (findTeam != null ? findTeam : new DeployedOfficeTeamModel(teamName));
			targetTeams[i] = team;

			// Refactor details of team
			final String existingTeamName = team.getDeployedOfficeTeamName();
			refactor.add(new AbstractChange<DeployedOfficeTeamModel>(team, "Refactor office team") {
				@Override
				public void apply() {
					team.setDeployedOfficeTeamName(teamName);
				}

				@Override
				public void revert() {
					team.setDeployedOfficeTeamName(existingTeamName);
				}
			});
		}

		// Ensure target teams sorted by name
		Arrays.sort(targetTeams, new Comparator<DeployedOfficeTeamModel>() {
			@Override
			public int compare(DeployedOfficeTeamModel a, DeployedOfficeTeamModel b) {
				return a.getDeployedOfficeTeamName().compareTo(b.getDeployedOfficeTeamName());
			}
		});

		// Obtain the existing teams
		final DeployedOfficeTeamModel[] existingTeams = office.getDeployedOfficeTeams()
				.toArray(new DeployedOfficeTeamModel[0]);

		// Add changes to disconnect existing teams to be removed
		Set<DeployedOfficeTeamModel> targetTeamSet = new HashSet<DeployedOfficeTeamModel>(Arrays.asList(targetTeams));
		for (DeployedOfficeTeamModel existingTeam : existingTeams) {
			if (!(targetTeamSet.contains(existingTeam))) {
				// Add change to disconnect team
				final DeployedOfficeTeamModel team = existingTeam;
				refactor.add(new DisconnectChange<DeployedOfficeTeamModel>(existingTeam) {
					@Override
					protected void populateRemovedConnections(List<ConnectionModel> connList) {
						DeployedOfficeTeamToOfficeFloorTeamModel conn = team.getOfficeFloorTeam();
						if (conn != null) {
							conn.remove();
							connList.add(conn);
						}
					}
				});
			}
		}

		// Add change to refactor teams
		refactor.add(new AbstractChange<DeployedOfficeModel>(office, "Refactor teams of office") {
			@Override
			public void apply() {
				// Remove existing teams, add target teams
				for (DeployedOfficeTeamModel team : existingTeams) {
					office.removeDeployedOfficeTeam(team);
				}
				for (DeployedOfficeTeamModel team : targetTeams) {
					office.addDeployedOfficeTeam(team);
				}
			}

			@Override
			public void revert() {
				// Remove the target teams, add back existing
				for (DeployedOfficeTeamModel team : targetTeams) {
					office.removeDeployedOfficeTeam(team);
				}
				for (DeployedOfficeTeamModel team : existingTeams) {
					office.addDeployedOfficeTeam(team);
				}
			}
		});

		// ----------------- Refactoring -------------------------------

		// Return change to do all the refactoring
		return new AbstractChange<DeployedOfficeModel>(office, "Refactor deployed office") {
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
	 * @param targetItemName       Target item name.
	 * @param targetToExistingName Mapping of target item name to existing item
	 *                             name.
	 * @param existingNameToItem   Mapping of existing item name to the existing
	 *                             item.
	 */
	private <T> T getExistingItem(String targetItemName, Map<String, String> targetToExistingName,
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
	public Change<OfficeFloorManagedObjectModel> addOfficeFloorManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope, OfficeFloorManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addOfficeFloorManagedObject)

		// Create the managed object
		final OfficeFloorManagedObjectModel managedObject = new OfficeFloorManagedObjectModel(managedObjectName,
				getManagedObjectScope(managedObjectScope));

		// Add the dependencies for the managed object
		for (ManagedObjectDependencyType<?> dependency : managedObjectType.getDependencyTypes()) {
			managedObject.addOfficeFloorManagedObjectDependency(new OfficeFloorManagedObjectDependencyModel(
					dependency.getDependencyName(), dependency.getDependencyType().getName()));
		}

		// Create connection to the managed object source
		final OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel conn = new OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel();
		conn.setOfficeFloorManagedObject(managedObject);
		conn.setOfficeFloorManagedObjectSource(managedObjectSource);

		// Return change to add the managed object
		return new AbstractChange<OfficeFloorManagedObjectModel>(managedObject, "Add managed object") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.addOfficeFloorManagedObject(managedObject);
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
				OfficeFloorChangesImpl.this.officeFloor.removeOfficeFloorManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectModel> removeOfficeFloorManagedObject(
			final OfficeFloorManagedObjectModel managedObject) {

		// TODO test this method (removeOfficeFloorManagedObject)

		// Obtain the connection to managed object source
		final OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel conn = managedObject
				.getOfficeFloorManagedObjectSource();

		// Return change to remove the managed object
		return new AbstractChange<OfficeFloorManagedObjectModel>(managedObject, "Remove managed object") {
			@Override
			public void apply() {
				if (conn != null) {
					conn.remove();
				}
				OfficeFloorChangesImpl.this.officeFloor.removeOfficeFloorManagedObject(managedObject);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.addOfficeFloorManagedObject(managedObject);
				if (conn != null) {
					conn.connect();
				}
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectModel> renameOfficeFloorManagedObject(
			final OfficeFloorManagedObjectModel managedObject, final String newManagedObjectName) {

		// TODO test this method (renameOfficeFloorManagedObject)

		// Obtain the old managed object name
		final String oldManagedObjectName = managedObject.getOfficeFloorManagedObjectName();

		// Return change to rename the managed object
		return new AbstractChange<OfficeFloorManagedObjectModel>(managedObject,
				"Rename managed object to " + newManagedObjectName) {
			@Override
			public void apply() {
				managedObject.setOfficeFloorManagedObjectName(newManagedObjectName);
			}

			@Override
			public void revert() {
				managedObject.setOfficeFloorManagedObjectName(oldManagedObjectName);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectModel> rescopeOfficeFloorManagedObject(
			final OfficeFloorManagedObjectModel managedObject, ManagedObjectScope newManagedObjectScope) {

		// TODO test this method (scopeOfficeFloorManagedObject)

		// Obtain the new scope text
		final String newScope = getManagedObjectScope(newManagedObjectScope);

		// OBtain the old managed object scope
		final String oldScope = managedObject.getManagedObjectScope();

		// Return change to re-scope the managed object
		return new AbstractChange<OfficeFloorManagedObjectModel>(managedObject,
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
	public Change<TypeQualificationModel> addOfficeFloorManagedObjectTypeQualification(
			OfficeFloorManagedObjectModel officeFloorManagedObject, String qualifier, String type) {

		// Create the type qualification
		final TypeQualificationModel typeQualification = new TypeQualificationModel(qualifier, type);

		// Return change to add type qualification
		return new AbstractChange<TypeQualificationModel>(typeQualification, "Add Managed Object Type Qualification") {
			@Override
			public void apply() {
				officeFloorManagedObject.addTypeQualification(typeQualification);
			}

			@Override
			public void revert() {
				officeFloorManagedObject.removeTypeQualification(typeQualification);
			}
		};
	}

	@Override
	public Change<TypeQualificationModel> removeOfficeFloorManagedObjectTypeQualification(
			TypeQualificationModel typeQualification) {

		// Find the managed object containing the type qualification
		OfficeFloorManagedObjectModel containingOfficeManagedObject = null;
		for (OfficeFloorManagedObjectModel mo : this.officeFloor.getOfficeFloorManagedObjects()) {
			for (TypeQualificationModel check : mo.getTypeQualifications()) {
				if (check == typeQualification) {
					containingOfficeManagedObject = mo;
				}
			}
		}
		if (containingOfficeManagedObject == null) {
			// Must find team containing type qualification
			return new NoChange<TypeQualificationModel>(typeQualification, "Remove Managed Object Type Qualification",
					"Type Qualification not on Managed Object in Office");
		}

		// Return change to remove type qualification
		final OfficeFloorManagedObjectModel officeManagedObject = containingOfficeManagedObject;
		return new AbstractChange<TypeQualificationModel>(typeQualification,
				"Remove Managed Object Type Qualification") {
			@Override
			public void apply() {
				officeManagedObject.removeTypeQualification(typeQualification);
			}

			@Override
			public void revert() {
				officeManagedObject.addTypeQualification(typeQualification);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceModel> addOfficeFloorManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties, long timeout,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addOfficeFloorManagedObjectSource)

		// Create the managed object source
		final OfficeFloorManagedObjectSourceModel managedObjectSource = new OfficeFloorManagedObjectSourceModel(
				managedObjectSourceName, managedObjectSourceClassName, managedObjectType.getObjectType().getName(),
				String.valueOf(timeout));
		for (Property property : properties) {
			managedObjectSource.addProperty(new PropertyModel(property.getName(), property.getValue()));
		}

		// Add the flows for the managed object source
		for (ManagedObjectFlowType<?> flow : managedObjectType.getFlowTypes()) {
			managedObjectSource.addOfficeFloorManagedObjectSourceFlow(
					new OfficeFloorManagedObjectSourceFlowModel(flow.getFlowName(), flow.getArgumentType().getName()));
		}

		// Add the teams for the managed object source
		for (ManagedObjectTeamType team : managedObjectType.getTeamTypes()) {
			managedObjectSource.addOfficeFloorManagedObjectSourceTeam(
					new OfficeFloorManagedObjectSourceTeamModel(team.getTeamName()));
		}

		// Return the change to add the managed object source
		return new AbstractChange<OfficeFloorManagedObjectSourceModel>(managedObjectSource,
				"Add managed object source") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.addOfficeFloorManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.removeOfficeFloorManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceModel> removeOfficeFloorManagedObjectSource(
			final OfficeFloorManagedObjectSourceModel managedObjectSource) {

		// TODO test this method (removeOfficeFloorManagedObjectSource)

		// Return change to remove the managed object source
		return new AbstractChange<OfficeFloorManagedObjectSourceModel>(managedObjectSource,
				"Remove managed object source") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.removeOfficeFloorManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.addOfficeFloorManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceModel> renameOfficeFloorManagedObjectSource(
			final OfficeFloorManagedObjectSourceModel managedObjectSource, final String newManagedObjectSourceName) {

		// TODO test this method (renameOfficeFloorManagedObjectSource)

		// Obtain the old managed object source name
		final String oldManagedObjectSourceName = managedObjectSource.getOfficeFloorManagedObjectSourceName();

		// Return change to rename the managed object source
		return new AbstractChange<OfficeFloorManagedObjectSourceModel>(managedObjectSource,
				"Rename managed object source to " + newManagedObjectSourceName) {
			@Override
			public void apply() {
				managedObjectSource.setOfficeFloorManagedObjectSourceName(newManagedObjectSourceName);
			}

			@Override
			public void revert() {
				managedObjectSource.setOfficeFloorManagedObjectSourceName(oldManagedObjectSourceName);
			}
		};
	}

	@Override
	public Change<OfficeFloorTeamModel> addOfficeFloorTeam(String teamName, int teamSize,
			boolean isRequestNoTeamOversight, String teamSourceClassName, PropertyList properties, TeamType teamType) {

		// TODO test this method (addOfficeFloorTeam)

		// Create the office floor team
		final OfficeFloorTeamModel team = new OfficeFloorTeamModel(teamName, teamSize, teamSourceClassName,
				isRequestNoTeamOversight);
		for (Property property : properties) {
			team.addProperty(new PropertyModel(property.getName(), property.getValue()));
		}

		// Return change to add the office floor team
		return new AbstractChange<OfficeFloorTeamModel>(team, "Add team") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.addOfficeFloorTeam(team);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.removeOfficeFloorTeam(team);
			}
		};
	}

	@Override
	public Change<OfficeFloorTeamModel> removeOfficeFloorTeam(final OfficeFloorTeamModel officeFloorTeam) {

		// TODO test this method (removeOfficeFloorTeam)

		// Return change to remove the office floor team
		return new AbstractChange<OfficeFloorTeamModel>(officeFloorTeam, "Remove team") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.removeOfficeFloorTeam(officeFloorTeam);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.addOfficeFloorTeam(officeFloorTeam);
			}
		};
	}

	@Override
	public Change<OfficeFloorTeamModel> renameOfficeFloorTeam(final OfficeFloorTeamModel officeFloorTeam,
			final String newOfficeFloorTeamName) {

		// TODO test this method (renameOfficeFloorTeam)

		// Obtain the old office floor team name
		final String oldOfficeFloorTeamName = officeFloorTeam.getOfficeFloorTeamName();

		// Return the change to rename the office floor team
		return new AbstractChange<OfficeFloorTeamModel>(officeFloorTeam, "Rename team to " + newOfficeFloorTeamName) {
			@Override
			public void apply() {
				officeFloorTeam.setOfficeFloorTeamName(newOfficeFloorTeamName);
			}

			@Override
			public void revert() {
				officeFloorTeam.setOfficeFloorTeamName(oldOfficeFloorTeamName);
			}
		};
	}

	@Override
	public Change<TypeQualificationModel> addOfficeFloorTeamTypeQualification(OfficeFloorTeamModel officeFloorTeam,
			String qualifier, String type) {

		// Create the type qualification
		final TypeQualificationModel typeQualification = new TypeQualificationModel(qualifier, type);

		// Return change to add type qualification
		return new AbstractChange<TypeQualificationModel>(typeQualification, "Add Team Type Qualification") {
			@Override
			public void apply() {
				officeFloorTeam.addTypeQualification(typeQualification);
			}

			@Override
			public void revert() {
				officeFloorTeam.removeTypeQualification(typeQualification);
			}
		};
	}

	@Override
	public Change<TypeQualificationModel> removeOfficeFloorTeamTypeQualification(
			TypeQualificationModel typeQualification) {

		// Find the team containing the type qualification
		OfficeFloorTeamModel containingOfficeTeam = null;
		for (OfficeFloorTeamModel team : this.officeFloor.getOfficeFloorTeams()) {
			for (TypeQualificationModel check : team.getTypeQualifications()) {
				if (check == typeQualification) {
					containingOfficeTeam = team;
				}
			}
		}
		if (containingOfficeTeam == null) {
			// Must find team containing type qualification
			return new NoChange<TypeQualificationModel>(typeQualification, "Remove Team Type Qualification",
					"Type Qualification not on Team in Office");
		}

		// Return change to remove type qualification
		final OfficeFloorTeamModel officeTeam = containingOfficeTeam;
		return new AbstractChange<TypeQualificationModel>(typeQualification, "Remove Team Type Qualification") {
			@Override
			public void apply() {
				officeTeam.removeTypeQualification(typeQualification);
			}

			@Override
			public void revert() {
				officeTeam.addTypeQualification(typeQualification);
			}
		};
	}

	@Override
	public Change<DeployedOfficeObjectToOfficeFloorManagedObjectModel> linkDeployedOfficeObjectToOfficeFloorManagedObject(
			DeployedOfficeObjectModel deployedOfficeObject, OfficeFloorManagedObjectModel officeFloorManagedObject) {

		// TODO test (linkDeployedOfficeObjectToOfficeFloorManagedObject)

		// Create the connection
		final DeployedOfficeObjectToOfficeFloorManagedObjectModel conn = new DeployedOfficeObjectToOfficeFloorManagedObjectModel();
		conn.setDeployedOfficeObject(deployedOfficeObject);
		conn.setOfficeFloorManagedObject(officeFloorManagedObject);

		// Create change to add the connection
		return new AbstractChange<DeployedOfficeObjectToOfficeFloorManagedObjectModel>(conn, "Connect") {
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
	public Change<DeployedOfficeObjectToOfficeFloorManagedObjectModel> removeDeployedOfficeObjectToOfficeFloorManagedObject(
			final DeployedOfficeObjectToOfficeFloorManagedObjectModel deployedOfficeObjectToOfficeFloorManagedObject) {

		// TODO test (removeDeployedOfficeObjectToOfficeFloorManagedObject)

		// Return change to remove the connection
		return new AbstractChange<DeployedOfficeObjectToOfficeFloorManagedObjectModel>(
				deployedOfficeObjectToOfficeFloorManagedObject, "Remove") {
			@Override
			public void apply() {
				deployedOfficeObjectToOfficeFloorManagedObject.remove();
			}

			@Override
			public void revert() {
				deployedOfficeObjectToOfficeFloorManagedObject.connect();
			}
		};
	}

	@Override
	public Change<DeployedOfficeObjectToOfficeFloorInputManagedObjectModel> linkDeployedOfficeObjectToOfficeFloorInputManagedObject(
			DeployedOfficeObjectModel deployedOfficeObject, OfficeFloorInputManagedObjectModel inputManagedObject) {

		// TODO test (linkDeployedOfficeObjectToOfficeFloorInputManagedObject)

		// Create the connection
		final DeployedOfficeObjectToOfficeFloorInputManagedObjectModel conn = new DeployedOfficeObjectToOfficeFloorInputManagedObjectModel();
		conn.setDeployedOfficeObject(deployedOfficeObject);
		conn.setOfficeFloorInputManagedObject(inputManagedObject);

		// Return change to add the connection
		return new AbstractChange<DeployedOfficeObjectToOfficeFloorInputManagedObjectModel>(conn, "Connect") {
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
	public Change<DeployedOfficeObjectToOfficeFloorInputManagedObjectModel> removeDeployedOfficeObjectToOfficeFloorInputManagedObject(
			final DeployedOfficeObjectToOfficeFloorInputManagedObjectModel deployedOfficeObjectToInputManagedObject) {

		// TODO test (removeDeployedOfficeObjectToOfficeFloorInputManagedObject)

		// Return change to remove the connection
		return new AbstractChange<DeployedOfficeObjectToOfficeFloorInputManagedObjectModel>(
				deployedOfficeObjectToInputManagedObject, "Remove") {
			@Override
			public void apply() {
				deployedOfficeObjectToInputManagedObject.remove();
			}

			@Override
			public void revert() {
				deployedOfficeObjectToInputManagedObject.connect();
			}
		};
	}

	@Override
	public Change<DeployedOfficeTeamToOfficeFloorTeamModel> linkDeployedOfficeTeamToOfficeFloorTeam(
			DeployedOfficeTeamModel deployedOfficeTeam, OfficeFloorTeamModel officeFloorTeam) {

		// TODO test (linkDeployedOfficeTeamToOfficeFloorTeam)

		// Create the connection
		final DeployedOfficeTeamToOfficeFloorTeamModel conn = new DeployedOfficeTeamToOfficeFloorTeamModel();
		conn.setDeployedOfficeTeam(deployedOfficeTeam);
		conn.setOfficeFloorTeam(officeFloorTeam);

		// Return the change to add connection
		return new AbstractChange<DeployedOfficeTeamToOfficeFloorTeamModel>(conn, "Connect") {
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
	public Change<DeployedOfficeTeamToOfficeFloorTeamModel> removeDeployedOfficeTeamToOfficeFloorTeam(
			final DeployedOfficeTeamToOfficeFloorTeamModel deployedOfficeTeamToOfficeFloorTeam) {

		// TODO test (removeDeployedOfficeTeamToOfficeFloorTeam)

		// Return the change to remove the connection
		return new AbstractChange<DeployedOfficeTeamToOfficeFloorTeamModel>(deployedOfficeTeamToOfficeFloorTeam,
				"Remove") {
			@Override
			public void apply() {
				deployedOfficeTeamToOfficeFloorTeam.remove();
			}

			@Override
			public void revert() {
				deployedOfficeTeamToOfficeFloorTeam.connect();
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel> linkOfficeFloorManagedObjectDependencyToOfficeFloorManagedObject(
			OfficeFloorManagedObjectDependencyModel officeFloorManagedObjectDependency,
			OfficeFloorManagedObjectModel officeFloorManagedObject) {

		// TODO test (linkOfficeFloorManagedObjectDependencyToOffice...)

		// Create the connection
		final OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel conn = new OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel();
		conn.setOfficeFloorManagedObjectDependency(officeFloorManagedObjectDependency);
		conn.setOfficeFloorManagedObject(officeFloorManagedObject);

		// Return change to add the connection
		return new AbstractChange<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel>(conn, "Connect") {
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
	public Change<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel> removeOfficeFloorManagedObjectDependencyToOfficeFloorManagedObject(
			final OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel officeFloorManagedObjectDependencyToOfficeFloorManagedObject) {

		// TODO test (removeOfficeFloorManagedObjectDependencyToOffice...)

		// Return change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel>(
				officeFloorManagedObjectDependencyToOfficeFloorManagedObject, "Remove") {
			@Override
			public void apply() {
				officeFloorManagedObjectDependencyToOfficeFloorManagedObject.remove();
			}

			@Override
			public void revert() {
				officeFloorManagedObjectDependencyToOfficeFloorManagedObject.connect();
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel> linkOfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObject(
			OfficeFloorManagedObjectDependencyModel officeFloorManagedObjectDependency,
			OfficeFloorInputManagedObjectModel officeFloorInputManagedObject) {

		// TODO test (linkOfficeFloorManagedObjectDependencyToOffice...)

		// Create the connection
		final OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel conn = new OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel();
		conn.setOfficeFloorManagedObjectDependency(officeFloorManagedObjectDependency);
		conn.setOfficeFloorInputManagedObject(officeFloorInputManagedObject);

		// Return change to add the connection
		return new AbstractChange<OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel>(conn,
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
	public Change<OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel> removeOfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObject(
			final OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel officeFloorManagedObjectDependencyToOfficeFloorInputManagedObject) {

		// TODO test (removeOfficeFloorManagedObjectDependencyToOffice...)

		// Return change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel>(
				officeFloorManagedObjectDependencyToOfficeFloorInputManagedObject, "Remove") {
			@Override
			public void apply() {
				officeFloorManagedObjectDependencyToOfficeFloorInputManagedObject.remove();
			}

			@Override
			public void revert() {
				officeFloorManagedObjectDependencyToOfficeFloorInputManagedObject.connect();
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel> linkOfficeFloorManagedObjectSourceFlowToDeployedOfficeInput(
			OfficeFloorManagedObjectSourceFlowModel officeFloorManagedObjectSourceFlow,
			DeployedOfficeInputModel deployedOfficeInput) {

		// TODO test (linkOfficeFloorManagedObjectSourceFlowToDeployed...)

		// Create the connection
		final OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel conn = new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel();
		conn.setOfficeFloorManagedObjectSoruceFlow(officeFloorManagedObjectSourceFlow);
		conn.setDeployedOfficeInput(deployedOfficeInput);

		// Return the change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel>(conn, "Connect") {
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
	public Change<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel> removeOfficeFloorManagedObjectSourceFlowToDeployedOfficeInput(
			final OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel officeFloorManagedObjectSourceFlowToDeployedOfficeInput) {

		// TODO test (removeOfficeFloorManagedObjectSourceFlowToDeployed...)

		// Return the change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel>(
				officeFloorManagedObjectSourceFlowToDeployedOfficeInput, "Remove") {
			@Override
			public void apply() {
				officeFloorManagedObjectSourceFlowToDeployedOfficeInput.remove();
			}

			@Override
			public void revert() {
				officeFloorManagedObjectSourceFlowToDeployedOfficeInput.connect();
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel> linkOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam(
			OfficeFloorManagedObjectSourceTeamModel officeFloorManagedObjectSourceTeam,
			OfficeFloorTeamModel officeFloorTeam) {

		// TODO test (linkOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam)

		// Create the connection
		final OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel conn = new OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel();
		conn.setOfficeFloorManagedObjectSourceTeam(officeFloorManagedObjectSourceTeam);
		conn.setOfficeFloorTeam(officeFloorTeam);

		// Return change to add the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel>(conn, "Connect") {
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
	public Change<OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel> removeOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam(
			final OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel officeFloorManagedObjectSourceTeamToOfficeFloorTeam) {

		// TODO test (removeOfficeFloorManagedObjectSourceTeamToOfficeFloorTeam)

		// Return change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel>(
				officeFloorManagedObjectSourceTeamToOfficeFloorTeam, "Remove") {
			@Override
			public void apply() {
				officeFloorManagedObjectSourceTeamToOfficeFloorTeam.remove();
			}

			@Override
			public void revert() {
				officeFloorManagedObjectSourceTeamToOfficeFloorTeam.connect();
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceToDeployedOfficeModel> linkOfficeFloorManagedObjectSourceToDeployedOffice(
			OfficeFloorManagedObjectSourceModel officeFloorManagedObjectSource, DeployedOfficeModel deployedOffice) {

		// TODO test (OfficeFloorManagedObjectSourceToDeployedOfficeModel)

		// Create the connection
		final OfficeFloorManagedObjectSourceToDeployedOfficeModel conn = new OfficeFloorManagedObjectSourceToDeployedOfficeModel();
		conn.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		conn.setManagingOffice(deployedOffice);

		// Return change to add the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceToDeployedOfficeModel>(conn, "Connect") {
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
	public Change<OfficeFloorManagedObjectSourceToDeployedOfficeModel> removeOfficeFloorManagedObjectSourceToDeployedOffice(
			final OfficeFloorManagedObjectSourceToDeployedOfficeModel officeFloorManagedObjectSourceToDeployedOffice) {

		// TODO test (OfficeFloorManagedObjectSourceToDeployedOfficeModel)

		// Returns the change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceToDeployedOfficeModel>(
				officeFloorManagedObjectSourceToDeployedOffice, "Remove") {
			@Override
			public void apply() {
				officeFloorManagedObjectSourceToDeployedOffice.remove();
			}

			@Override
			public void revert() {
				officeFloorManagedObjectSourceToDeployedOffice.connect();
			}
		};
	}

	@Override
	public Change<OfficeFloorInputManagedObjectModel> addOfficeFloorInputManagedObject(String inputManagedObjectName,
			String objectType) {

		// TODO test (addOfficeFloorInputManagedObject)

		// Create the input managed object
		final OfficeFloorInputManagedObjectModel inputManagedObject = new OfficeFloorInputManagedObjectModel(
				inputManagedObjectName, objectType);

		// Return change to add the input managed object
		return new AbstractChange<OfficeFloorInputManagedObjectModel>(inputManagedObject, "Add Input Managed Object") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.addOfficeFloorInputManagedObject(inputManagedObject);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.removeOfficeFloorInputManagedObject(inputManagedObject);
			}
		};
	}

	@Override
	public Change<OfficeFloorInputManagedObjectModel> renameOfficeFloorInputManagedObject(
			final OfficeFloorInputManagedObjectModel inputManagedObject, final String newInputManagedObjectName) {

		// TODO test (renameOfficeFloorInputManagedObject)

		// Obtain the existing input managed object name for reverting
		final String existingInputManagedObjectName = inputManagedObject.getOfficeFloorInputManagedObjectName();

		// Return change to rename
		return new AbstractChange<OfficeFloorInputManagedObjectModel>(inputManagedObject, "Rename") {
			@Override
			public void apply() {
				inputManagedObject.setOfficeFloorInputManagedObjectName(newInputManagedObjectName);
			}

			@Override
			public void revert() {
				inputManagedObject.setOfficeFloorInputManagedObjectName(existingInputManagedObjectName);
			}
		};
	}

	@Override
	public Change<OfficeFloorInputManagedObjectModel> removeOfficeFloorInputManagedObject(
			final OfficeFloorInputManagedObjectModel inputManagedObject) {

		// TODO test (removeOfficeFloorInputManagedObject)

		// Return change to remove the input managed object
		return new AbstractChange<OfficeFloorInputManagedObjectModel>(inputManagedObject, "Remove") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor.removeOfficeFloorInputManagedObject(inputManagedObject);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor.addOfficeFloorInputManagedObject(inputManagedObject);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel> linkOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObject(
			OfficeFloorManagedObjectSourceModel managedObjectSource,
			OfficeFloorInputManagedObjectModel inputManagedObject) {

		// TODO test (linkOfficeFloorManagedObjectSourceToOfficeFloorInput...)

		// Create the link
		final OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel conn = new OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel();
		conn.setOfficeFloorManagedObjectSource(managedObjectSource);
		conn.setOfficeFloorInputManagedObject(inputManagedObject);

		// Return change to add the link
		return new AbstractChange<OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel>(conn, "Connect") {
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
	public Change<OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel> removeOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObject(
			final OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel managedObjectSourceToInputManagedObject) {

		// TODO test (removeOfficeFloorManagedObjectSourceToOfficeFloorInput...)

		// Return change to remove the link
		return new AbstractChange<OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel>(
				managedObjectSourceToInputManagedObject, "Remove") {
			@Override
			public void apply() {
				managedObjectSourceToInputManagedObject.remove();
			}

			@Override
			public void revert() {
				managedObjectSourceToInputManagedObject.connect();
			}
		};
	}

	@Override
	public Change<OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel> linkOfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSource(
			OfficeFloorInputManagedObjectModel inputManagedObject,
			OfficeFloorManagedObjectSourceModel boundManagedObjectSource) {

		// TODO test (link...InputManagedObjectToBound..ManagedObjectSource)

		// Create the connection
		final OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel conn = new OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel();
		conn.setBoundOfficeFloorInputManagedObject(inputManagedObject);
		conn.setBoundOfficeFloorManagedObjectSource(boundManagedObjectSource);

		// Return change to add the connection
		return new AbstractChange<OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel>(conn,
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
	public Change<OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel> removeOfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSource(
			final OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel inputManagedObjectToBoundManagedObjectSource) {

		// TODO test (remove...InputManagedObjectToBound...ManagedObjectSource)

		// Return change to remove the connection
		return new AbstractChange<OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel>(
				inputManagedObjectToBoundManagedObjectSource, "Remove") {
			@Override
			public void apply() {
				inputManagedObjectToBoundManagedObjectSource.remove();
			}

			@Override
			public void revert() {
				inputManagedObjectToBoundManagedObjectSource.connect();
			}
		};
	}

}
