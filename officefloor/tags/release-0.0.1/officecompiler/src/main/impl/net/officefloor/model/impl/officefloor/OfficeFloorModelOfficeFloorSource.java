/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.model.impl.officefloor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.TripleKeyMap;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
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
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeFloorModel} {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorModelOfficeFloorSource extends
		AbstractOfficeFloorSource {

	/*
	 * =================== AbstractOfficeFloorSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void specifyConfigurationProperties(
			RequiredProperties requiredProperties,
			OfficeFloorSourceContext context) throws Exception {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorSource.specifyConfigurationProperties");
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Obtain the configuration to the section
		ConfigurationItem configuration = context.getConfiguration(context
				.getOfficeFloorLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find office floor '"
					+ context.getOfficeFloorLocation() + "'");
		}

		// Retrieve the office floor model
		OfficeFloorModel officeFloor = new OfficeFloorRepositoryImpl(
				new ModelRepositoryImpl()).retrieveOfficeFloor(configuration);

		// Add the office floor managed object sources, keeping registry of them
		Map<String, OfficeFloorManagedObjectSource> officeFloorManagedObjectSources = new HashMap<String, OfficeFloorManagedObjectSource>();
		for (OfficeFloorManagedObjectSourceModel managedObjectSourceModel : officeFloor
				.getOfficeFloorManagedObjectSources()) {

			// Add the office floor managed object source
			String managedObjectSourceName = managedObjectSourceModel
					.getOfficeFloorManagedObjectSourceName();
			OfficeFloorManagedObjectSource managedObjectSource = deployer
					.addManagedObjectSource(managedObjectSourceName,
							managedObjectSourceModel
									.getManagedObjectSourceClassName());
			for (PropertyModel property : managedObjectSourceModel
					.getProperties()) {
				managedObjectSource.addProperty(property.getName(), property
						.getValue());
			}

			// Register the managed object source
			officeFloorManagedObjectSources.put(managedObjectSourceName,
					managedObjectSource);
		}

		// Add the office floor managed objects, keeping registry of them
		Map<String, OfficeFloorManagedObject> officeFloorManagedObjects = new HashMap<String, OfficeFloorManagedObject>();
		for (OfficeFloorManagedObjectModel managedObjectModel : officeFloor
				.getOfficeFloorManagedObjects()) {

			// Obtain the managed object details
			String managedObjectName = managedObjectModel
					.getOfficeFloorManagedObjectName();
			ManagedObjectScope managedObjectScope = this.getManagedObjectScope(
					managedObjectModel.getManagedObjectScope(), deployer,
					managedObjectName);

			// Obtain the managed object source for the managed object
			OfficeFloorManagedObjectSource moSource = null;
			OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel moToSource = managedObjectModel
					.getOfficeFloorManagedObjectSource();
			if (moToSource != null) {
				OfficeFloorManagedObjectSourceModel moSourceModel = moToSource
						.getOfficeFloorManagedObjectSource();
				if (moSourceModel != null) {
					moSource = officeFloorManagedObjectSources
							.get(moSourceModel
									.getOfficeFloorManagedObjectSourceName());
				}
			}
			if (moSource == null) {
				continue; // must have managed object source
			}

			// Add the managed object and also register it
			OfficeFloorManagedObject managedObject = moSource
					.addOfficeFloorManagedObject(managedObjectName,
							managedObjectScope);
			officeFloorManagedObjects.put(managedObjectName, managedObject);
		}

		// Link the dependencies for the managed objects
		for (OfficeFloorManagedObjectModel managedObjectModel : officeFloor
				.getOfficeFloorManagedObjects()) {

			// Obtain the managed object
			OfficeFloorManagedObject managedObject = officeFloorManagedObjects
					.get(managedObjectModel.getOfficeFloorManagedObjectName());

			// Link each dependency for the managed object
			for (OfficeFloorManagedObjectDependencyModel dependencyModel : managedObjectModel
					.getOfficeFloorManagedObjectDependencies()) {

				// Add the dependency
				String dependencyName = dependencyModel
						.getOfficeFloorManagedObjectDependencyName();
				ManagedObjectDependency dependency = managedObject
						.getManagedObjectDependency(dependencyName);

				// Obtain the dependent managed object
				OfficeFloorManagedObject dependentManagedObject = null;
				OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel dependencyToMo = dependencyModel
						.getOfficeFloorManagedObject();
				if (dependencyToMo != null) {
					OfficeFloorManagedObjectModel dependentMoModel = dependencyToMo
							.getOfficeFloorManagedObject();
					if (dependentMoModel != null) {
						dependentManagedObject = officeFloorManagedObjects
								.get(dependentMoModel
										.getOfficeFloorManagedObjectName());
					}
				}
				if (dependentManagedObject == null) {
					continue; // must have dependent managed object
				}

				// Link the dependency to the managed object
				deployer.link(dependency, dependentManagedObject);
			}
		}

		// Add the office floor teams, keeping registry of teams
		Map<String, OfficeFloorTeam> officeFloorTeams = new HashMap<String, OfficeFloorTeam>();
		for (OfficeFloorTeamModel teamModel : officeFloor.getOfficeFloorTeams()) {

			// Add the office floor team
			String teamName = teamModel.getOfficeFloorTeamName();
			OfficeFloorTeam team = deployer.addTeam(teamName, teamModel
					.getTeamSourceClassName());
			for (PropertyModel property : teamModel.getProperties()) {
				team.addProperty(property.getName(), property.getValue());
			}

			// Register the team
			officeFloorTeams.put(teamName, team);
		}

		// Add the offices, keeping registry of the offices and their inputs
		Map<String, DeployedOffice> offices = new HashMap<String, DeployedOffice>();
		TripleKeyMap<String, String, String, DeployedOfficeInput> officeInputs = new TripleKeyMap<String, String, String, DeployedOfficeInput>();
		for (DeployedOfficeModel officeModel : officeFloor.getDeployedOffices()) {

			// Add the office, registering them
			String officeName = officeModel.getDeployedOfficeName();
			DeployedOffice office = deployer.addDeployedOffice(officeName,
					officeModel.getOfficeSourceClassName(), officeModel
							.getOfficeLocation());
			offices.put(officeName, office);
			for (PropertyModel property : officeModel.getProperties()) {
				office.addProperty(property.getName(), property.getValue());
			}

			// Add the office inputs, registering them
			for (DeployedOfficeInputModel inputModel : officeModel
					.getDeployedOfficeInputs()) {
				String sectionName = inputModel.getSectionName();
				String sectionInputName = inputModel.getSectionInputName();
				DeployedOfficeInput officeInput = office
						.getDeployedOfficeInput(sectionName, sectionInputName);
				officeInputs.put(officeName, sectionName, sectionInputName,
						officeInput);
			}

			// Add the office objects
			for (DeployedOfficeObjectModel objectModel : officeModel
					.getDeployedOfficeObjects()) {

				// Add the office object
				OfficeObject officeObject = office
						.getDeployedOfficeObject(objectModel
								.getDeployedOfficeObjectName());

				// Obtain the office floor managed object
				OfficeFloorManagedObject managedObject = null;
				DeployedOfficeObjectToOfficeFloorManagedObjectModel conn = objectModel
						.getOfficeFloorManagedObject();
				if (conn != null) {
					OfficeFloorManagedObjectModel managedObjectModel = conn
							.getOfficeFloorManagedObject();
					if (managedObjectModel != null) {
						managedObject = officeFloorManagedObjects
								.get(managedObjectModel
										.getOfficeFloorManagedObjectName());
					}
				}
				if (managedObject == null) {
					continue; // must have managed object for office object
				}

				// Have the office object be the managed object
				deployer.link(officeObject, managedObject);
			}

			// Add the office teams
			for (DeployedOfficeTeamModel teamModel : officeModel
					.getDeployedOfficeTeams()) {

				// Add the office team
				OfficeTeam officeTeam = office.getDeployedOfficeTeam(teamModel
						.getDeployedOfficeTeamName());

				// Obtain the office floor team
				OfficeFloorTeam officeFloorTeam = null;
				DeployedOfficeTeamToOfficeFloorTeamModel conn = teamModel
						.getOfficeFloorTeam();
				if (conn != null) {
					OfficeFloorTeamModel officeFloorTeamModel = conn
							.getOfficeFloorTeam();
					if (officeFloorTeamModel != null) {
						officeFloorTeam = officeFloorTeams
								.get(officeFloorTeamModel
										.getOfficeFloorTeamName());
					}
				}
				if (officeFloorTeam == null) {
					continue; // must have undertaking office floor team
				}

				// Have the office team be the office floor team
				deployer.link(officeTeam, officeFloorTeam);
			}
		}

		// Link details for the managed object sources
		for (OfficeFloorManagedObjectSourceModel managedObjectSourceModel : officeFloor
				.getOfficeFloorManagedObjectSources()) {

			// Obtain the managed object source
			OfficeFloorManagedObjectSource managedObjectSource = officeFloorManagedObjectSources
					.get(managedObjectSourceModel
							.getOfficeFloorManagedObjectSourceName());
			if (managedObjectSource == null) {
				continue; // must have managed object source
			}

			// Obtain the managing office
			DeployedOffice managingOffice = null;
			OfficeFloorManagedObjectSourceToDeployedOfficeModel moToOffice = managedObjectSourceModel
					.getManagingOffice();
			if (moToOffice != null) {
				DeployedOfficeModel officeModel = moToOffice
						.getManagingOffice();
				if (officeModel != null) {
					managingOffice = offices.get(officeModel
							.getDeployedOfficeName());
				}
			}
			if (managingOffice != null) {
				// Have the office manage the managed object
				deployer.link(managedObjectSource.getManagingOffice(),
						managingOffice, moToOffice
								.getProcessBoundManagedObjectName());
			}

			// Add the office floor managed object source flows
			for (OfficeFloorManagedObjectSourceFlowModel flowModel : managedObjectSourceModel
					.getOfficeFloorManagedObjectSourceFlows()) {

				// Add the office floor managed object source flow
				String flowName = flowModel
						.getOfficeFloorManagedObjectSourceFlowName();
				ManagedObjectFlow flow = managedObjectSource
						.getManagedObjectFlow(flowName);

				// Obtain the office input
				DeployedOfficeInput officeInput = null;
				OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel flowToInput = flowModel
						.getDeployedOfficeInput();
				if (flowToInput != null) {
					DeployedOfficeInputModel officeInputModel = flowToInput
							.getDeployedOfficeInput();
					if (officeInputModel != null) {
						DeployedOfficeModel officeModel = this
								.getOfficeForInput(officeInputModel,
										officeFloor);
						officeInput = officeInputs.get(officeModel
								.getDeployedOfficeName(), officeInputModel
								.getSectionName(), officeInputModel
								.getSectionInputName());
					}
				}
				if (officeInput != null) {
					// Have the office input for the flow
					deployer.link(flow, officeInput);
				}
			}

			// Add the office floor managed object source teams
			for (OfficeFloorManagedObjectSourceTeamModel mosTeamModel : managedObjectSourceModel
					.getOfficeFloorManagedObjectSourceTeams()) {

				// Add the office floor managed object source team
				String mosTeamName = mosTeamModel
						.getOfficeFloorManagedObjectSourceTeamName();
				ManagedObjectTeam mosTeam = managedObjectSource
						.getManagedObjectTeam(mosTeamName);

				// Obtain the office floor team
				OfficeFloorTeam team = null;
				OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel mosTeamToTeam = mosTeamModel
						.getOfficeFloorTeam();
				if (mosTeamToTeam != null) {
					OfficeFloorTeamModel teamModel = mosTeamToTeam
							.getOfficeFloorTeam();
					if (teamModel != null) {
						team = officeFloorTeams.get(teamModel
								.getOfficeFloorTeamName());
					}
				}
				if (team != null) {
					// Have the team for the managed object source team
					deployer.link(mosTeam, team);
				}
			}
		}
	}

	/**
	 * Obtains {@link DeployedOfficeModel} for the
	 * {@link DeployedOfficeInputModel}.
	 * 
	 * @param officeInputModel
	 *            {@link DeployedOfficeInputModel}.
	 * @param officeFloor
	 *            {@link OfficeFloorModel}.
	 * @return {@link DeployedOfficeModel}.
	 */
	private DeployedOfficeModel getOfficeForInput(
			DeployedOfficeInputModel officeInputModel,
			OfficeFloorModel officeFloor) {

		// Find the office for the input
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			for (DeployedOfficeInputModel input : office
					.getDeployedOfficeInputs()) {
				if (input == officeInputModel) {
					// Found input, return containing office
					return office;
				}
			}
		}

		// No office if at this point
		return null;
	}

	/**
	 * Obtains the {@link ManagedObjectScope} from the managed object scope
	 * name.
	 * 
	 * @param managedObjectScope
	 *            Name of the {@link ManagedObjectScope}.
	 * @param deployer
	 *            {@link OfficeFloorDeployer}.
	 * @param managedObjectName
	 *            Name of the {@link OfficeFloorManagedObjectModel}.
	 * @return {@link ManagedObjectScope} or <code>null</code> with issue
	 *         reported to the {@link OfficeFloorDeployer}.
	 */
	private ManagedObjectScope getManagedObjectScope(String managedObjectScope,
			OfficeFloorDeployer deployer, String managedObjectName) {

		// Obtain the managed object scope
		if (OfficeFloorChanges.PROCESS_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (OfficeFloorChanges.THREAD_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (OfficeFloorChanges.WORK_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.WORK;
		}

		// Unknown scope if at this point
		deployer.addIssue("Unknown managed object scope " + managedObjectScope,
				AssetType.MANAGED_OBJECT, managedObjectName);
		return null;
	}
}