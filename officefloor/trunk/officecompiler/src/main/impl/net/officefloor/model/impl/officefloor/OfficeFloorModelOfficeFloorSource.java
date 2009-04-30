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
package net.officefloor.model.impl.officefloor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOperations;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeFloorModel} {@link OfficeFloorSource}.
 * 
 * @author Daniel
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

		// Add the offices, keeping registry of them
		Map<String, DeployedOffice> offices = new HashMap<String, DeployedOffice>();
		for (DeployedOfficeModel officeModel : officeFloor.getDeployedOffices()) {

			// Add the office
			String officeName = officeModel.getDeployedOfficeName();
			DeployedOffice office = deployer.addDeployedOffice(officeName,
					officeModel.getOfficeSourceClassName(), officeModel
							.getOfficeLocation());
			offices.put(officeName, office);
			for (PropertyModel property : officeModel.getProperties()) {
				office.addProperty(property.getName(), property.getValue());
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

		// Manage the office floor managed object sources
		for (OfficeFloorManagedObjectSourceModel managedObjectSourceModel : officeFloor
				.getOfficeFloorManagedObjectSources()) {

			// Obtain the managed object
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
						managingOffice);
			}

		}
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
		if (OfficeFloorOperations.PROCESS_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (OfficeFloorOperations.THREAD_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (OfficeFloorOperations.WORK_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.WORK;
		}

		// Unknown scope if at this point
		deployer.addIssue("Unknown managed object scope " + managedObjectScope,
				AssetType.MANAGED_OBJECT, managedObjectName);
		return null;
	}
}