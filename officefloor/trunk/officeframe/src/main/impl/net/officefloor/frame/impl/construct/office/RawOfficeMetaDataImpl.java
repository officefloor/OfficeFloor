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
package net.officefloor.frame.impl.construct.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.administrator.RawBoundAdministratorMetaData;
import net.officefloor.frame.impl.construct.administrator.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaData;
import net.officefloor.frame.impl.construct.team.RawTeamMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link RawOfficeMetaData} implementation.
 * 
 * @author Daniel
 */
public class RawOfficeMetaDataImpl implements RawOfficeMetaDataFactory,
		RawOfficeMetaData {

	/**
	 * Obtains the {@link RawOfficeMetaDataFactory}.
	 * 
	 * @return {@link RawOfficeMetaDataFactory}.
	 */
	public static RawOfficeMetaDataFactory getFactory() {
		return new RawOfficeMetaDataImpl(null, null, null, null, null, null,
				null, null, null, null);
	}

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link RawOfficeFloorMetaData} containing this {@link Office}.
	 */
	private final RawOfficeFloorMetaData rawOfficeFloorMetaData;

	/**
	 * {@link Team} instances by their {@link Office} registered names.
	 */
	private final Map<String, Team> teams;

	/**
	 * {@link RawManagedObjectMetaData} instances by their {@link Office}
	 * registered names.
	 */
	private final Map<String, RawManagedObjectMetaData<?, ?>> managedObjectMetaData;

	/**
	 * {@link ProcessState} {@link RawBoundManagedObjectMetaData}.
	 */
	private final RawBoundManagedObjectMetaData<?>[] processBoundManagedObjects;

	/**
	 * {@link ThreadState} {@link RawBoundManagedObjectMetaData}.
	 */
	private final RawBoundManagedObjectMetaData<?>[] threadBoundManagedObjects;

	/**
	 * Scope {@link RawBoundManagedObjectMetaData} of the {@link Office} by the
	 * {@link ProcessState} and {@link ThreadState} bound names.
	 */
	private final Map<String, RawBoundManagedObjectMetaData<?>> scopeMo;

	/**
	 * {@link ProcessState} {@link RawBoundAdministratorMetaData}.
	 */
	private final RawBoundAdministratorMetaData<?, ?>[] processBoundAdministrators;

	/**
	 * {@link ThreadState} {@link RawBoundAdministratorMetaData}.
	 */
	private final RawBoundAdministratorMetaData<?, ?>[] threadBoundAdministrators;

	/**
	 * Scope {@link RawBoundAdministratorMetaData} of the {@link Office} by the
	 * {@link ProcessState} and {@link ThreadState} bound names.
	 */
	private final Map<String, RawBoundAdministratorMetaData<?, ?>> scopeAdmins;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            {@link Office} names.
	 * @param rawOfficeFloorMetaData
	 *            {@link RawOfficeFloorMetaData} containing this {@link Office}.
	 * @param teams
	 *            {@link Team} instances by their {@link Office} registered
	 *            names.
	 * @param managedObjectMetaData
	 *            {@link RawManagedObjectMetaData} instances by their
	 *            {@link Office} registered names.
	 * @param processBoundManagedObjects
	 *            {@link ProcessState} {@link RawBoundManagedObjectMetaData}
	 *            instances.
	 * @param threadBoundManagedObjects
	 *            {@link ThreadState} {@link RawBoundManagedObjectMetaData}
	 *            instances.
	 * @param scopeMo
	 *            Scope {@link RawBoundManagedObjectMetaData} of the
	 *            {@link Office} by the {@link ProcessState} and
	 *            {@link ThreadState} bound names.
	 * @param processBoundAdministrators
	 *            {@link ProcessState} {@link RawBoundAdministratorMetaData}
	 *            instances.
	 * @param threadBoundAdministrators
	 *            {@link ThreadState} {@link RawBoundAdministratorMetaData}
	 *            instances.
	 * @param scopeAdmins
	 *            Scope {@link RawBoundAdministratorMetaData} of the
	 *            {@link Office} by the {@link ProcessState} and
	 *            {@link ThreadState} bound names.
	 */
	private RawOfficeMetaDataImpl(String officeName,
			RawOfficeFloorMetaData rawOfficeFloorMetaData,
			Map<String, Team> teams,
			Map<String, RawManagedObjectMetaData<?, ?>> managedObjectMetaData,
			RawBoundManagedObjectMetaData<?>[] processBoundManagedObjects,
			RawBoundManagedObjectMetaData<?>[] threadBoundManagedObjects,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeMo,
			RawBoundAdministratorMetaData<?, ?>[] processBoundAdministrators,
			RawBoundAdministratorMetaData<?, ?>[] threadBoundAdministrators,
			Map<String, RawBoundAdministratorMetaData<?, ?>> scopeAdmins) {
		this.officeName = officeName;
		this.rawOfficeFloorMetaData = rawOfficeFloorMetaData;
		this.teams = teams;
		this.managedObjectMetaData = managedObjectMetaData;
		this.processBoundManagedObjects = processBoundManagedObjects;
		this.threadBoundManagedObjects = threadBoundManagedObjects;
		this.scopeMo = scopeMo;
		this.processBoundAdministrators = processBoundAdministrators;
		this.threadBoundAdministrators = threadBoundAdministrators;
		this.scopeAdmins = scopeAdmins;
	}

	/*
	 * ============= RawOfficeMetaDataFactory ===========================
	 */

	@Override
	public RawOfficeMetaData constructRawOfficeMetaData(
			OfficeConfiguration configuration, OfficeFloorIssues issues,
			RawOfficeFloorMetaData rawOfficeFloorMetaData,
			RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory) {

		// Obtain the name of the office
		String officeName = configuration.getOfficeName();
		if (ConstructUtil.isBlank(officeName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, OfficeFloor.class
					.getSimpleName(), "Office registered without name");
			return null; // can not continue
		}

		// Enhance the office
		OfficeEnhancerContextImpl.enhanceOffice(officeName, configuration,
				issues, rawOfficeFloorMetaData);

		// Register the teams to office
		Map<String, Team> officeTeams = new HashMap<String, Team>();
		for (LinkedTeamConfiguration teamConfig : configuration
				.getRegisteredTeams()) {

			// Ensure have office name for team
			String officeTeamName = teamConfig.getOfficeTeamName();
			if (ConstructUtil.isBlank(officeTeamName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Team registered to Office without name");
				continue; // can not register team
			}

			// Ensure have office floor name for team
			String officeFloorTeamName = teamConfig.getOfficeFloorTeamName();
			if (ConstructUtil.isBlank(officeFloorTeamName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"No Office Floor Team name for Office Team '"
								+ officeTeamName + "'");
				continue; // can not register team
			}

			// Obtain the team
			RawTeamMetaData rawTeamMetaData = rawOfficeFloorMetaData
					.getRawTeamMetaData(officeFloorTeamName);
			if (rawTeamMetaData == null) {
				issues.addIssue(AssetType.OFFICE, officeName, "Unknown Team '"
						+ officeFloorTeamName
						+ "' not available to register to Office");
				continue; // can not register team
			}

			// Register the team
			officeTeams.put(officeTeamName, rawTeamMetaData.getTeam());
		}

		// Register the managed object sources to office
		Map<String, RawManagedObjectMetaData<?, ?>> registeredMo = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		for (LinkedManagedObjectSourceConfiguration mos : configuration
				.getRegisteredManagedObjectSources()) {

			// Ensure have office name for managed object
			String moName = mos.getOfficeManagedObjectName();
			if (ConstructUtil.isBlank(moName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Managed Object registered to Office without name");
				continue; // can not register managed object
			}

			// Ensure have name of managed object source
			String mosName = mos.getOfficeFloorManagedObjectSourceName();
			if (ConstructUtil.isBlank(mosName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"No Managed Object Source name for Office Managed Object '"
								+ moName + "'");
				continue; // can not register managed object
			}

			// Obtain the raw managed object source meta-data
			RawManagedObjectMetaData<?, ?> rawMoMetaData = rawOfficeFloorMetaData
					.getRawManagedObjectMetaData(mosName);
			if (rawMoMetaData == null) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Unknown Managed Object Source '" + mosName
								+ "' not available to register to Office");
				continue; // can not register managed object
			}

			// Register the managed object
			registeredMo.put(moName, rawMoMetaData);
		}

		// Obtain the process bound managed object instances
		ManagedObjectConfiguration<?>[] processManagedObjectConfiguration = configuration
				.getProcessManagedObjectConfiguration();
		final RawBoundManagedObjectMetaData<?>[] processBoundManagedObjects;
		if ((processManagedObjectConfiguration == null)
				|| (processManagedObjectConfiguration.length == 0)) {
			processBoundManagedObjects = new RawBoundManagedObjectMetaData[0];
		} else {
			processBoundManagedObjects = rawBoundManagedObjectFactory
					.constructBoundManagedObjectMetaData(
							processManagedObjectConfiguration, issues,
							ManagedObjectScope.PROCESS, AssetType.OFFICE,
							officeName, registeredMo, null);
		}

		// Create the map of process bound managed objects by name
		Map<String, RawBoundManagedObjectMetaData<?>> scopeMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		for (RawBoundManagedObjectMetaData<?> mo : processBoundManagedObjects) {
			scopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Obtain the process bound administrator instances
		AdministratorSourceConfiguration<?, ?>[] processAdministratorConfiguration = configuration
				.getProcessAdministratorSourceConfiguration();
		final RawBoundAdministratorMetaData<?, ?>[] processBoundAdministrators;
		if ((processAdministratorConfiguration == null)
				|| (processAdministratorConfiguration.length == 0)) {
			processBoundAdministrators = new RawBoundAdministratorMetaData[0];
		} else {
			processBoundAdministrators = rawBoundAdministratorFactory
					.constructRawBoundAdministratorMetaData(
							processAdministratorConfiguration, issues,
							AdministratorScope.PROCESS, AssetType.OFFICE,
							officeName, officeTeams, scopeMo);
		}

		// Create the map of process bound administrators by name
		Map<String, RawBoundAdministratorMetaData<?, ?>> scopeAdmins = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();
		for (RawBoundAdministratorMetaData<?, ?> admin : processBoundAdministrators) {
			scopeAdmins.put(admin.getAdministratorName(), admin);
		}

		// Obtain the thread bound managed object instances
		ManagedObjectConfiguration<?>[] threadManagedObjectConfiguration = configuration
				.getThreadManagedObjectConfiguration();
		final RawBoundManagedObjectMetaData<?>[] threadBoundManagedObjects;
		if ((threadManagedObjectConfiguration == null)
				|| (threadManagedObjectConfiguration.length == 0)) {
			threadBoundManagedObjects = new RawBoundManagedObjectMetaData[0];
		} else {
			threadBoundManagedObjects = rawBoundManagedObjectFactory
					.constructBoundManagedObjectMetaData(
							threadManagedObjectConfiguration, issues,
							ManagedObjectScope.PROCESS, AssetType.OFFICE,
							officeName, registeredMo, scopeMo);
		}

		// Load the thread bound managed objects to scope managed objects
		for (RawBoundManagedObjectMetaData<?> mo : threadBoundManagedObjects) {
			scopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Obtain the thread bound administrator instances
		AdministratorSourceConfiguration<?, ?>[] threadAdministratorConfiguration = configuration
				.getThreadAdministratorSourceConfiguration();
		final RawBoundAdministratorMetaData<?, ?>[] threadBoundAdministrators;
		if ((threadAdministratorConfiguration == null)
				|| (threadAdministratorConfiguration.length == 0)) {
			threadBoundAdministrators = new RawBoundAdministratorMetaData[0];
		} else {
			threadBoundAdministrators = rawBoundAdministratorFactory
					.constructRawBoundAdministratorMetaData(
							threadAdministratorConfiguration, issues,
							AdministratorScope.THREAD, AssetType.OFFICE,
							officeName, officeTeams, scopeMo);
		}

		// Load the thread bound administrators to scope administrators
		for (RawBoundAdministratorMetaData<?, ?> admin : threadBoundAdministrators) {
			scopeAdmins.put(admin.getAdministratorName(), admin);
		}

		// Return the office meta data
		return new RawOfficeMetaDataImpl(officeName, rawOfficeFloorMetaData,
				officeTeams, registeredMo, processBoundManagedObjects,
				threadBoundManagedObjects, scopeMo, processBoundAdministrators,
				threadBoundAdministrators, scopeAdmins);
	}

	/*
	 * ============= RawOfficeMetaData =======================================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public RawOfficeFloorMetaData getRawOfficeFloorMetaData() {
		return this.rawOfficeFloorMetaData;
	}

	@Override
	public Map<String, Team> getTeams() {
		return this.teams;
	}

	@Override
	public Map<String, RawManagedObjectMetaData<?, ?>> getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public RawBoundManagedObjectMetaData<?>[] getProcessBoundManagedObjects() {
		return this.processBoundManagedObjects;
	}

	@Override
	public RawBoundManagedObjectMetaData<?>[] getThreadBoundManagedObjects() {
		return this.threadBoundManagedObjects;
	}

	@Override
	public Map<String, RawBoundManagedObjectMetaData<?>> getOfficeScopeManagedObject() {
		return this.scopeMo;
	}

	@Override
	public RawBoundAdministratorMetaData<?, ?>[] getProcessBoundAdministrators() {
		return this.processBoundAdministrators;
	}

	@Override
	public RawBoundAdministratorMetaData<?, ?>[] getThreadBoundAdministrators() {
		return this.threadBoundAdministrators;
	}

	@Override
	public Map<String, RawBoundAdministratorMetaData<?, ?>> getOfficeScopeAdministrator() {
		return this.scopeAdmins;
	}

}