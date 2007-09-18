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
package net.officefloor.frame.impl;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.spi.team.Team;

/**
 * Registry of resources for a particular
 * {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class RawOfficeResourceRegistry {

	/**
	 * Creates the registry of resources for the
	 * {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @param officeConfiguration
	 *            {@link OfficeConfiguration}.
	 * @param rawMoRegistry
	 *            {@link RawManagedObjectRegistry}.
	 * @param teamRegistry
	 *            Registry of {@link Team} instances for the
	 *            {@link net.officefloor.frame.api.manage.OfficeFloor}.
	 * @return {@link RawOfficeResourceRegistry}.
	 */
	public static RawOfficeResourceRegistry createRawOfficeResourceRegistry(
			OfficeConfiguration officeConfiguration,
			RawManagedObjectRegistry rawMoRegistry,
			Map<String, Team> teamRegistry) throws ConfigurationException {

		// Create the registry of managed objects
		Map<String, RawManagedObjectMetaData> managedObjects = new HashMap<String, RawManagedObjectMetaData>();
		for (LinkedManagedObjectConfiguration moConfig : officeConfiguration
				.getRegisteredManagedObjects()) {

			// Obtain the managed object Id
			String moId = moConfig.getManagedObjectId();

			// Obtain the raw managed object meta-data for the Id
			RawManagedObjectMetaData rawMoMetaData = rawMoRegistry
					.getRawManagedObjectMetaData(moId);

			// Register under the office local name
			managedObjects.put(moConfig.getManagedObjectName(), rawMoMetaData);
		}

		// Create the process managed object registry
		RawProcessManagedObjectRegistry processMoRegistry = RawProcessManagedObjectRegistry
				.createProcessStateManagedObjectRegistry(officeConfiguration,
						managedObjects);

		// Create the registry of teams
		Map<String, Team> teams = new HashMap<String, Team>();
		for (LinkedTeamConfiguration teamConfig : officeConfiguration
				.getRegisteredTeams()) {

			// Obtain the team Id
			String teamId = teamConfig.getTeamId();

			// Obtain the team
			Team team = teamRegistry.get(teamId);
			if (team == null) {
				throw new ConfigurationException("Unknown team '" + teamId
						+ "' on Office Floor");
			}

			// Register under the office local name
			teams.put(teamConfig.getTeamName(), team);
		}

		// Return the office resource registry
		return new RawOfficeResourceRegistry(managedObjects, teams,
				processMoRegistry);
	}

	/**
	 * {@link RawManagedObjectMetaData} for the
	 * {@link net.officefloor.frame.api.manage.Office}.
	 */
	private final Map<String, RawManagedObjectMetaData> managedObjects;

	/**
	 * {@link Team} registry for the
	 * {@link net.officefloor.frame.api.manage.Office}.
	 */
	private final Map<String, Team> teams;

	/**
	 * {@link RawProcessManagedObjectRegistry}.
	 */
	private final RawProcessManagedObjectRegistry processMoRegistry;

	/**
	 * Initiate.
	 * 
	 * @param officeConfiguration
	 *            {@link OfficeConfiguration}.
	 * @param managedObjects
	 *            {@link RawManagedObjectMetaData} for the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @param teams
	 *            {@link Team} registry for the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @param processMoRegistry
	 *            {@link RawProcessManagedObjectRegistry}.
	 */
	private RawOfficeResourceRegistry(
			Map<String, RawManagedObjectMetaData> managedObjects,
			Map<String, Team> teams,
			RawProcessManagedObjectRegistry processMoRegistry) {
		this.managedObjects = managedObjects;
		this.teams = teams;
		this.processMoRegistry = processMoRegistry;
	}

	/**
	 * Obtains the {@link RawManagedObjectMetaData} for the name local to the
	 * {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @param managedObjectName
	 *            Name local to the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @return {@link RawManagedObjectMetaData}.
	 */
	public RawManagedObjectMetaData getRawManagedObjectMetaData(
			String managedObjectName) {
		return this.managedObjects.get(managedObjectName);
	}

	/**
	 * Obtains the {@link RawProcessManagedObjectRegistry} for the
	 * {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @return {@link RawProcessManagedObjectRegistry} for the
	 *         {@link net.officefloor.frame.api.manage.Office}.
	 */
	public RawProcessManagedObjectRegistry getRawProcessManagedObjectRegistry() {
		return this.processMoRegistry;
	}

	/**
	 * Obtains the {@link Team} by the name local to the
	 * {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @param teamName
	 *            Name local to the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @return {@link Team}.
	 */
	public Team getTeam(String teamName) {
		return this.teams.get(teamName);
	}

}
