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
package net.officefloor.model.impl.officefloor;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;

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
	 * @param officeFloor
	 *            {@link OfficeFloorModel}.
	 */
	public OfficeFloorChangesImpl(OfficeFloorModel officeFloor) {
		this.officeFloor = officeFloor;
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

	/*
	 * ================= OfficeFloorChanges ==============================
	 */

	@Override
	public Change<DeployedOfficeModel> addDeployedOffice(String officeName,
			String officeSourceClassName, String officeLocation,
			PropertyList propertyList, OfficeType officeType) {

		// TODO test this method (addDeployedOffice)

		// Create the deployed office
		final DeployedOfficeModel office = new DeployedOfficeModel(officeName,
				officeSourceClassName, officeLocation);
		for (Property property : propertyList) {
			office.addProperty(new PropertyModel(property.getName(), property
					.getValue()));
		}

		// Add the inputs for the office
		for (OfficeInputType input : officeType.getOfficeInputTypes()) {
			office.addDeployedOfficeInput(new DeployedOfficeInputModel(input
					.getOfficeSectionName(), input.getOfficeSectionInputName(),
					input.getParameterType()));
		}

		// Add the teams for the office
		for (OfficeTeamType team : officeType.getOfficeTeamTypes()) {
			office.addDeployedOfficeTeam(new DeployedOfficeTeamModel(team
					.getOfficeTeamName()));
		}

		// Add the objects for the office
		for (OfficeManagedObjectType managedObject : officeType
				.getOfficeManagedObjectTypes()) {
			office.addDeployedOfficeObject(new DeployedOfficeObjectModel(
					managedObject.getOfficeManagedObjectName(), managedObject
							.getObjectType()));
		}

		// Return the change to add the office
		return new AbstractChange<DeployedOfficeModel>(office, "Add office") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor
						.addDeployedOffice(office);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor
						.removeDeployedOffice(office);
			}
		};
	}

	@Override
	public Change<DeployedOfficeModel> removeDeployedOffice(
			final DeployedOfficeModel deployedOffice) {

		// TODO test this method (removeDeployedOffice)

		// Return change to remove the office
		return new AbstractChange<DeployedOfficeModel>(deployedOffice,
				"Remove office") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor
						.removeDeployedOffice(deployedOffice);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor
						.addDeployedOffice(deployedOffice);
			}
		};
	}

	@Override
	public Change<DeployedOfficeModel> renameDeployedOffice(
			final DeployedOfficeModel deployedOffice,
			final String newDeployedOfficeName) {

		// TODO test this method (renameDeployedOffice)

		// Obtain the old name
		final String oldDeployedOfficeName = deployedOffice
				.getDeployedOfficeName();

		// Return change to rename the office
		return new AbstractChange<DeployedOfficeModel>(deployedOffice,
				"Rename office to " + newDeployedOfficeName) {
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
	public Change<OfficeFloorManagedObjectModel> addOfficeFloorManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope,
			OfficeFloorManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addOfficeFloorManagedObject)

		// Create the managed object
		final OfficeFloorManagedObjectModel managedObject = new OfficeFloorManagedObjectModel(
				managedObjectName, getManagedObjectScope(managedObjectScope));

		// Add the dependencies for the managed object
		for (ManagedObjectDependencyType<?> dependency : managedObjectType
				.getDependencyTypes()) {
			managedObject
					.addOfficeFloorManagedObjectDependency(new OfficeFloorManagedObjectDependencyModel(
							dependency.getDependencyName(), dependency
									.getDependencyType().getName()));
		}

		// Create connection to the managed object source
		final OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel conn = new OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel();
		conn.setOfficeFloorManagedObject(managedObject);
		conn.setOfficeFloorManagedObjectSource(managedObjectSource);

		// Return change to add the managed object
		return new AbstractChange<OfficeFloorManagedObjectModel>(managedObject,
				"Add managed object") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor
						.addOfficeFloorManagedObject(managedObject);
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
				OfficeFloorChangesImpl.this.officeFloor
						.removeOfficeFloorManagedObject(managedObject);
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
		return new AbstractChange<OfficeFloorManagedObjectModel>(managedObject,
				"Remove managed object") {
			@Override
			public void apply() {
				if (conn != null) {
					conn.remove();
				}
				OfficeFloorChangesImpl.this.officeFloor
						.removeOfficeFloorManagedObject(managedObject);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor
						.addOfficeFloorManagedObject(managedObject);
				if (conn != null) {
					conn.connect();
				}
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectModel> renameOfficeFloorManagedObject(
			final OfficeFloorManagedObjectModel managedObject,
			final String newManagedObjectName) {

		// TODO test this method (renameOfficeFloorManagedObject)

		// Obtain the old managed object name
		final String oldManagedObjectName = managedObject
				.getOfficeFloorManagedObjectName();

		// Return change to rename the managed object
		return new AbstractChange<OfficeFloorManagedObjectModel>(managedObject,
				"Rename managed object to " + newManagedObjectName) {
			@Override
			public void apply() {
				managedObject
						.setOfficeFloorManagedObjectName(newManagedObjectName);
			}

			@Override
			public void revert() {
				managedObject
						.setOfficeFloorManagedObjectName(oldManagedObjectName);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceModel> addOfficeFloorManagedObjectSource(
			String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addOfficeFloorManagedObjectSource)

		// Create the managed object source
		final OfficeFloorManagedObjectSourceModel managedObjectSource = new OfficeFloorManagedObjectSourceModel(
				managedObjectSourceName, managedObjectSourceClassName,
				managedObjectType.getObjectClass().getName());
		for (Property property : properties) {
			managedObjectSource.addProperty(new PropertyModel(property
					.getName(), property.getValue()));
		}

		// Add the flows for the managed object source
		for (ManagedObjectFlowType<?> flow : managedObjectType.getFlowTypes()) {
			managedObjectSource
					.addOfficeFloorManagedObjectSourceFlow(new OfficeFloorManagedObjectSourceFlowModel(
							flow.getFlowName(), flow.getArgumentType()
									.getName()));
		}

		// Add the teams for the managed object source
		for (ManagedObjectTeamType team : managedObjectType.getTeamTypes()) {
			managedObjectSource
					.addOfficeFloorManagedObjectSourceTeam(new OfficeFloorManagedObjectSourceTeamModel(
							team.getTeamName()));
		}

		// Return the change to add the managed object source
		return new AbstractChange<OfficeFloorManagedObjectSourceModel>(
				managedObjectSource, "Add managed object source") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor
						.addOfficeFloorManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor
						.removeOfficeFloorManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceModel> removeOfficeFloorManagedObjectSource(
			final OfficeFloorManagedObjectSourceModel managedObjectSource) {

		// TODO test this method (removeOfficeFloorManagedObjectSource)

		// Return change to remove the managed object source
		return new AbstractChange<OfficeFloorManagedObjectSourceModel>(
				managedObjectSource, "Remove managed object source") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor
						.removeOfficeFloorManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor
						.addOfficeFloorManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<OfficeFloorManagedObjectSourceModel> renameOfficeFloorManagedObjectSource(
			final OfficeFloorManagedObjectSourceModel managedObjectSource,
			final String newManagedObjectSourceName) {

		// TODO test this method (renameOfficeFloorManagedObjectSource)

		// Obtain the old managed object source name
		final String oldManagedObjectSourceName = managedObjectSource
				.getOfficeFloorManagedObjectSourceName();

		// Return change to rename the managed object source
		return new AbstractChange<OfficeFloorManagedObjectSourceModel>(
				managedObjectSource, "Rename managed object source to "
						+ newManagedObjectSourceName) {
			@Override
			public void apply() {
				managedObjectSource
						.setOfficeFloorManagedObjectSourceName(newManagedObjectSourceName);
			}

			@Override
			public void revert() {
				managedObjectSource
						.setOfficeFloorManagedObjectSourceName(oldManagedObjectSourceName);
			}
		};
	}

	@Override
	public Change<OfficeFloorTeamModel> addOfficeFloorTeam(String teamName,
			String teamSourceClassName, PropertyList properties,
			TeamType teamType) {

		// TODO test this method (addOfficeFloorTeam)

		// Create the office floor team
		final OfficeFloorTeamModel team = new OfficeFloorTeamModel(teamName,
				teamSourceClassName);
		for (Property property : properties) {
			team.addProperty(new PropertyModel(property.getName(), property
					.getValue()));
		}

		// Return change to add the office floor team
		return new AbstractChange<OfficeFloorTeamModel>(team, "Add team") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor
						.addOfficeFloorTeam(team);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor
						.removeOfficeFloorTeam(team);
			}
		};
	}

	@Override
	public Change<OfficeFloorTeamModel> removeOfficeFloorTeam(
			final OfficeFloorTeamModel officeFloorTeam) {

		// TODO test this method (removeOfficeFloorTeam)

		// Return change to remove the office floor team
		return new AbstractChange<OfficeFloorTeamModel>(officeFloorTeam,
				"Remove team") {
			@Override
			public void apply() {
				OfficeFloorChangesImpl.this.officeFloor
						.removeOfficeFloorTeam(officeFloorTeam);
			}

			@Override
			public void revert() {
				OfficeFloorChangesImpl.this.officeFloor
						.addOfficeFloorTeam(officeFloorTeam);
			}
		};
	}

	@Override
	public Change<OfficeFloorTeamModel> renameOfficeFloorTeam(
			final OfficeFloorTeamModel officeFloorTeam,
			final String newOfficeFloorTeamName) {

		// TODO test this method (renameOfficeFloorTeam)

		// Obtain the old office floor team name
		final String oldOfficeFloorTeamName = officeFloorTeam
				.getOfficeFloorTeamName();

		// Return the change to rename the office floor team
		return new AbstractChange<OfficeFloorTeamModel>(officeFloorTeam,
				"Rename team to " + newOfficeFloorTeamName) {
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
	public Change<DeployedOfficeObjectToOfficeFloorManagedObjectModel> linkDeployedOfficeObjectToOfficeFloorManagedObject(
			DeployedOfficeObjectModel deployedOfficeObject,
			OfficeFloorManagedObjectModel officeFloorManagedObject) {

		// TODO test (linkDeployedOfficeObjectToOfficeFloorManagedObject)

		// Create the connection
		final DeployedOfficeObjectToOfficeFloorManagedObjectModel conn = new DeployedOfficeObjectToOfficeFloorManagedObjectModel();
		conn.setDeployedOfficeObject(deployedOfficeObject);
		conn.setOfficeFloorManagedObject(officeFloorManagedObject);

		// Create change to add the connection
		return new AbstractChange<DeployedOfficeObjectToOfficeFloorManagedObjectModel>(
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
	public Change<DeployedOfficeTeamToOfficeFloorTeamModel> linkDeployedOfficeTeamToOfficeFloorTeam(
			DeployedOfficeTeamModel deployedOfficeTeam,
			OfficeFloorTeamModel officeFloorTeam) {

		// TODO test (linkDeployedOfficeTeamToOfficeFloorTeam)

		// Create the connection
		final DeployedOfficeTeamToOfficeFloorTeamModel conn = new DeployedOfficeTeamToOfficeFloorTeamModel();
		conn.setDeployedOfficeTeam(deployedOfficeTeam);
		conn.setOfficeFloorTeam(officeFloorTeam);

		// Return the change to add connection
		return new AbstractChange<DeployedOfficeTeamToOfficeFloorTeamModel>(
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
	public Change<DeployedOfficeTeamToOfficeFloorTeamModel> removeDeployedOfficeTeamToOfficeFloorTeam(
			final DeployedOfficeTeamToOfficeFloorTeamModel deployedOfficeTeamToOfficeFloorTeam) {

		// TODO test (removeDeployedOfficeTeamToOfficeFloorTeam)

		// Return the change to remove the connection
		return new AbstractChange<DeployedOfficeTeamToOfficeFloorTeamModel>(
				deployedOfficeTeamToOfficeFloorTeam, "Remove") {
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
		conn
				.setOfficeFloorManagedObjectDependency(officeFloorManagedObjectDependency);
		conn.setOfficeFloorManagedObject(officeFloorManagedObject);

		// Return change to add the connection
		return new AbstractChange<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel>(
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
	public Change<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel> removeOfficeFloorManagedObjectDependencyToOfficeFloorManagedObject(
			final OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel officeFloorManagedObjectDependencyToOfficeFloorManagedObject) {

		// TODO test (removeOfficeFloorManagedObjectDependencyToOffice...)

		// Return change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel>(
				officeFloorManagedObjectDependencyToOfficeFloorManagedObject,
				"Remove") {
			@Override
			public void apply() {
				officeFloorManagedObjectDependencyToOfficeFloorManagedObject
						.remove();
			}

			@Override
			public void revert() {
				officeFloorManagedObjectDependencyToOfficeFloorManagedObject
						.connect();
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
		conn
				.setOfficeFloorManagedObjectSoruceFlow(officeFloorManagedObjectSourceFlow);
		conn.setDeployedOfficeInput(deployedOfficeInput);

		// Return the change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel>(
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
	public Change<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel> removeOfficeFloorManagedObjectSourceFlowToDeployedOfficeInput(
			final OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel officeFloorManagedObjectSourceFlowToDeployedOfficeInput) {

		// TODO test (removeOfficeFloorManagedObjectSourceFlowToDeployed...)

		// Return the change to remove the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel>(
				officeFloorManagedObjectSourceFlowToDeployedOfficeInput,
				"Remove") {
			@Override
			public void apply() {
				officeFloorManagedObjectSourceFlowToDeployedOfficeInput
						.remove();
			}

			@Override
			public void revert() {
				officeFloorManagedObjectSourceFlowToDeployedOfficeInput
						.connect();
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
		conn
				.setOfficeFloorManagedObjectSourceTeam(officeFloorManagedObjectSourceTeam);
		conn.setOfficeFloorTeam(officeFloorTeam);

		// Return change to add the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel>(
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
			OfficeFloorManagedObjectSourceModel officeFloorManagedObjectSource,
			DeployedOfficeModel deployedOffice) {

		// TODO test (OfficeFloorManagedObjectSourceToDeployedOfficeModel)

		// Create the connection
		final OfficeFloorManagedObjectSourceToDeployedOfficeModel conn = new OfficeFloorManagedObjectSourceToDeployedOfficeModel();
		conn.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		conn.setManagingOffice(deployedOffice);

		// Return change to add the connection
		return new AbstractChange<OfficeFloorManagedObjectSourceToDeployedOfficeModel>(
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
	public Change<OfficeFloorManagedObjectSourceToDeployedOfficeModel> setProcessBoundManagedObjectName(
			final OfficeFloorManagedObjectSourceToDeployedOfficeModel officeFloorManagedObjectSourceToDeployedOffice,
			final String newProcessBoundManagedObjectName) {

		// TODO test (setProcessBoundManagedObjectName)

		// Obtain the old process bound name
		final String oldProcessBoundManagedObjectName = officeFloorManagedObjectSourceToDeployedOffice
				.getProcessBoundManagedObjectName();

		// Returns the change of process bound name
		return new AbstractChange<OfficeFloorManagedObjectSourceToDeployedOfficeModel>(
				officeFloorManagedObjectSourceToDeployedOffice,
				"Rename process bound name to "
						+ newProcessBoundManagedObjectName) {
			@Override
			public void apply() {
				officeFloorManagedObjectSourceToDeployedOffice
						.setProcessBoundManagedObjectName(newProcessBoundManagedObjectName);
			}

			@Override
			public void revert() {
				officeFloorManagedObjectSourceToDeployedOffice
						.setProcessBoundManagedObjectName(oldProcessBoundManagedObjectName);
			}
		};
	}

}