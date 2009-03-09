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
package net.officefloor.frame.impl.construct.officefloor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.administrator.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawOfficeManagingManagedObjectMetaData;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaDataFactory;
import net.officefloor.frame.impl.construct.task.RawTaskMetaDataFactory;
import net.officefloor.frame.impl.construct.team.RawTeamMetaData;
import net.officefloor.frame.impl.construct.team.RawTeamMetaDataFactory;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.construct.work.RawWorkMetaDataFactory;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.officefloor.ManagedObjectSourceInstanceImpl;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw {@link OfficeFloorMetaData} implementation.
 * 
 * @author Daniel
 */
public class RawOfficeFloorMetaDataImpl implements RawOfficeFloorMetaData,
		RawOfficeFloorMetaDataFactory {

	/**
	 * Obtains the {@link RawOfficeFloorMetaDataFactory}.
	 * 
	 * @return {@link RawOfficeFloorMetaDataFactory}.
	 */
	public static RawOfficeFloorMetaDataFactory getFactory() {
		return new RawOfficeFloorMetaDataImpl(null, null, null);
	}

	/**
	 * Registry of {@link RawTeamMetaData} by the {@link Team} name.
	 */
	private final Map<String, RawTeamMetaData> teamRegistry;

	/**
	 * Registry of {@link RawManagedObjectMetaData} by the
	 * {@link ManagedObjectSource} name.
	 */
	private final Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry;

	/**
	 * {@link EscalationProcedure}.
	 */
	private final EscalationProcedure escalationProcedure;

	/**
	 * {@link OfficeFloorMetaData}.
	 */
	private OfficeFloorMetaData officeFloorMetaData;

	/**
	 * Initiate.
	 * 
	 * @param teamRegistry
	 *            Registry of {@link RawTeamMetaData} by the {@link Team} name.
	 * @param mosRegistry
	 *            Registry of {@link RawManagedObjectMetaData} by the
	 *            {@link ManagedObjectSource} name.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure}.
	 */
	private RawOfficeFloorMetaDataImpl(
			Map<String, RawTeamMetaData> teamRegistry,
			Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry,
			EscalationProcedure escalationProcedure) {
		this.teamRegistry = teamRegistry;
		this.mosRegistry = mosRegistry;
		this.escalationProcedure = escalationProcedure;
	}

	/*
	 * ================ RawOfficeFloorMetaDataFactory ===================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public RawOfficeFloorMetaData constructRawOfficeFloorMetaData(
			OfficeFloorConfiguration configuration, OfficeFloorIssues issues,
			RawTeamMetaDataFactory rawTeamFactory,
			RawManagedObjectMetaDataFactory rawMosFactory,
			RawBoundManagedObjectMetaDataFactory rawBoundMoFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdminFactory,
			AssetManagerFactory assetManagerFactory,
			RawOfficeMetaDataFactory rawOfficeFactory,
			RawWorkMetaDataFactory rawWorkFactory,
			RawTaskMetaDataFactory rawTaskFactory) {

		// Name of office floor for reporting issues
		String officeFloorName = configuration.getOfficeFloorName();
		if (ConstructUtil.isBlank(officeFloorName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, "Unknown",
					"Name not provided for Office Floor");

			// Not that important to name the Office Floor, so provide default
			officeFloorName = OfficeFloor.class.getSimpleName();
		}

		// Construct the teams
		Map<String, RawTeamMetaData> teamRegistry = new HashMap<String, RawTeamMetaData>();
		for (TeamConfiguration<?> teamConfiguration : configuration
				.getTeamConfiguration()) {

			// Construct the team
			RawTeamMetaData teamMetaData = rawTeamFactory
					.constructRawTeamMetaData(teamConfiguration, issues);
			if (teamMetaData == null) {
				continue; // issue with team
			}

			// Obtain the team name
			String teamName = teamMetaData.getTeamName();
			if (teamRegistry.containsKey(teamName)) {
				issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName,
						"Teams registered with the same name '" + teamName
								+ "'");
				continue; // maintain only first team
			}

			// Register the team
			teamRegistry.put(teamName, teamMetaData);
		}

		// Construct the managed object sources
		Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		Map<String, List<RawOfficeManagingManagedObjectMetaData>> officeManagedObjects = new HashMap<String, List<RawOfficeManagingManagedObjectMetaData>>();
		for (ManagedObjectSourceConfiguration mosConfiguration : configuration
				.getManagedObjectSourceConfiguration()) {

			// Construct the managed object source
			RawManagedObjectMetaData<?, ?> mosMetaData = rawMosFactory
					.constructRawManagedObjectMetaData(mosConfiguration,
							issues, assetManagerFactory, configuration);
			if (mosMetaData == null) {
				continue; // issue with managed object source
			}

			// Obtain the managed object source name
			String managedObjectSourceName = mosMetaData.getManagedObjectName();
			if (mosRegistry.containsKey(managedObjectSourceName)) {
				issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName,
						"Managed object sources registered with the same name '"
								+ managedObjectSourceName + "'");
				continue; // maintain only first managed object source
			}

			// Obtain details for the office managing the managed object
			RawOfficeManagingManagedObjectMetaData managingOfficeMetaData = mosMetaData
					.getManagingOfficeMetaData();
			if (managingOfficeMetaData == null) {
				issues
						.addIssue(AssetType.MANAGED_OBJECT,
								managedObjectSourceName,
								"Managing Object Source did not specify managing office meta-data");
				continue; // must have a managing office
			}
			String managingOfficeName = managingOfficeMetaData
					.getManagingOfficeName();
			if (ConstructUtil.isBlank(managingOfficeName)) {
				issues
						.addIssue(AssetType.MANAGED_OBJECT,
								managedObjectSourceName,
								"Managed Object Source did not specify a managing Office");
				continue; // must have a managing office
			}

			// Register the managed object source
			mosRegistry.put(managedObjectSourceName, mosMetaData);

			// Register for being managed by the office
			List<RawOfficeManagingManagedObjectMetaData> officeManagingManagedObjects = officeManagedObjects
					.get(managingOfficeName);
			if (officeManagingManagedObjects == null) {
				officeManagingManagedObjects = new LinkedList<RawOfficeManagingManagedObjectMetaData>();
				officeManagedObjects.put(managingOfficeName,
						officeManagingManagedObjects);
			}
			officeManagingManagedObjects.add(managingOfficeMetaData);
		}

		// Obtain the escalation procedure
		EscalationProcedure escalationProcedure = configuration
				.getEscalationProcedure();
		if (escalationProcedure == null) {
			// Provide default escalation procedure
			escalationProcedure = new EscalationProcedureImpl();
		}

		// Create the raw office floor meta-data
		RawOfficeFloorMetaDataImpl rawMetaData = new RawOfficeFloorMetaDataImpl(
				teamRegistry, mosRegistry, escalationProcedure);

		// Construct the offices
		Map<String, RawOfficeMetaData> rawOfficeRegistry = new HashMap<String, RawOfficeMetaData>();
		List<OfficeMetaData> officeMetaDatas = new LinkedList<OfficeMetaData>();
		for (OfficeConfiguration officeConfiguration : configuration
				.getOfficeConfiguration()) {

			// Obtain the office name
			String officeName = officeConfiguration.getOfficeName();
			if (ConstructUtil.isBlank(officeName)) {
				issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName,
						"Office added without a name");
				continue; // office must have name
			}

			// Obtain the managed objects being managed by the office
			List<RawOfficeManagingManagedObjectMetaData> officeManagingManagedObjectList = officeManagedObjects
					.get(officeName);
			RawOfficeManagingManagedObjectMetaData[] officeManagingManagedObjects = (officeManagingManagedObjectList == null ? new RawOfficeManagingManagedObjectMetaData[0]
					: officeManagingManagedObjectList
							.toArray(new RawOfficeManagingManagedObjectMetaData[0]));

			// Unregister managed objects as check later all managed by offices
			officeManagedObjects.remove(officeName);

			// Construct the raw office meta-data
			RawOfficeMetaData rawOfficeMetaData = rawOfficeFactory
					.constructRawOfficeMetaData(officeConfiguration, issues,
							officeManagingManagedObjects, rawMetaData,
							rawBoundMoFactory, rawBoundAdminFactory,
							rawWorkFactory, rawTaskFactory);
			if (rawOfficeMetaData == null) {
				continue; // issue with office
			}

			// Register the raw office meta-data
			rawOfficeRegistry.put(officeName, rawOfficeMetaData);

			// Add the office meta-data to listing
			OfficeMetaData officeMetaData = rawOfficeMetaData
					.getOfficeMetaData();
			officeMetaDatas.add(officeMetaData);

			// Have the managed objects managed by the office
			for (RawOfficeManagingManagedObjectMetaData officeManagingManagedObject : officeManagingManagedObjects) {
				officeManagingManagedObject.getRawManagedObjectMetaData()
						.manageByOffice(officeMetaData, issues);
			}
		}

		// Issue if office not exist for the managed object
		if (officeManagedObjects.size() > 0) {
			for (String officeName : officeManagedObjects.keySet()) {
				for (RawOfficeManagingManagedObjectMetaData managingOfficeMetaData : officeManagedObjects
						.get(officeName)) {
					issues.addIssue(AssetType.MANAGED_OBJECT, officeName,
							"No Office by name '"
									+ officeName
									+ "' to manage Managed Object Source "
									+ managingOfficeMetaData
											.getRawManagedObjectMetaData()
											.getManagedObjectName());
				}
			}
		}

		// Obtain the listing of teams
		Team[] teams = this.teamRegistry.values().toArray(new Team[0]);

		// Obtain the listing of managed object source instances
		List<ManagedObjectSourceInstance<?>> mosInstances = new LinkedList<ManagedObjectSourceInstance<?>>();
		for (RawManagedObjectMetaData<?, ?> rawMoMetaData : this.mosRegistry
				.values()) {
			ManagedObjectSourceInstance<?> mosInstance = this
					.createManagedObjectSourceInstance(rawMoMetaData);
			mosInstances.add(mosInstance);
		}

		// Create the office floor meta-data
		rawMetaData.officeFloorMetaData = new OfficeFloorMetaDataImpl(teams,
				mosInstances.toArray(new ManagedObjectSourceInstance[0]),
				officeMetaDatas.toArray(new OfficeMetaData[0]));

		// Return the raw meta-data
		return rawMetaData;
	}

	/**
	 * Creates the {@link ManagedObjectSourceInstance} for the
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @return {@link ManagedObjectSourceInstance}.
	 */
	private <H extends Enum<H>> ManagedObjectSourceInstance<H> createManagedObjectSourceInstance(
			RawManagedObjectMetaData<?, H> rawMoMetaData) {
		return new ManagedObjectSourceInstanceImpl<H>(rawMoMetaData
				.getManagedObjectSource(), rawMoMetaData.getHandlers(),
				rawMoMetaData.getManagedObjectPool());
	}

	/*
	 * =================== RawOfficeFloorMetaData ========================
	 */

	@Override
	public RawTeamMetaData getRawTeamMetaData(String teamName) {
		return this.teamRegistry.get(teamName);
	}

	@Override
	public RawManagedObjectMetaData<?, ?> getRawManagedObjectMetaData(
			String managedObjectSourceName) {
		return this.mosRegistry.get(managedObjectSourceName);
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public OfficeFloorMetaData getOfficeFloorMetaData() {
		return this.officeFloorMetaData;
	}

}