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
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
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

		// Add the office floor managed objects, keeping registry of them
		Map<String, OfficeFloorManagedObject> officeFloorManagedObjects = new HashMap<String, OfficeFloorManagedObject>();
		for (OfficeFloorManagedObjectModel managedObjectModel : officeFloor
				.getOfficeFloorManagedObjects()) {

			// Add the office floor managed object
			String managedObjectName = managedObjectModel
					.getOfficeFloorManagedObjectName();
			OfficeFloorManagedObject managedObject = deployer.addManagedObject(
					managedObjectName, managedObjectModel
							.getManagedObjectSourceClassName());
			for (PropertyModel property : managedObjectModel.getProperties()) {
				managedObject.addProperty(property.getName(), property
						.getValue());
			}

			// Register the managed object
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

		// Manage the office floor managed objects
		for (OfficeFloorManagedObjectModel managedObjectModel : officeFloor
				.getOfficeFloorManagedObjects()) {

			// Obtain the managed object
			OfficeFloorManagedObject managedObject = officeFloorManagedObjects
					.get(managedObjectModel.getOfficeFloorManagedObjectName());
			if (managedObject == null) {
				continue; // must have managed object
			}

			// Obtain the managing office
			DeployedOffice managingOffice = null;
			OfficeFloorManagedObjectToDeployedOfficeModel moToOffice = managedObjectModel
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
				deployer
						.link(managedObject.getManagingOffice(), managingOffice);
			}

		}
	}

}