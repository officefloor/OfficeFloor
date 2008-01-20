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

import java.util.Map;

import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.FlowMetaDataImpl;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data of the {@link net.officefloor.frame.api.manage.Office}.
 * 
 * @author Daniel
 */
public class RawOfficeMetaData {

	/**
	 * Creates the {@link Office} for the input {@link OfficeBuilder}.
	 * 
	 * @param officeConfiguration
	 *            {@link OfficeConfiguration}.
	 * @param teamRegistry
	 *            Registry of the {@link Team} instances.
	 * @param rawMosRegistry
	 *            {@link RawManagedObjectRegistry}.
	 * @param rawAssetRegistry
	 *            {@link RawAssetManagerRegistry}.
	 * @param officeFloorEscalationProcedure
	 *            {@link OfficeFloor} {@link EscalationProcedure}.
	 * @return {@link RawOfficeMetaData}.
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	protected static RawOfficeMetaData createOffice(
			OfficeConfiguration officeConfiguration,
			Map<String, Team> teamRegistry,
			RawManagedObjectRegistry rawMosRegistry,
			RawAssetManagerRegistry rawAssetRegistry,
			EscalationProcedure officeFloorEscalationProcedure)
			throws Exception {

		// Obtain the Office Name
		String officeName = officeConfiguration.getOfficeName();

		// Create the resources for the Office
		RawOfficeResourceRegistry officeResources = RawOfficeResourceRegistry
				.createRawOfficeResourceRegistry(officeConfiguration,
						rawMosRegistry, teamRegistry);

		// Create the registry of raw Administrator meta-data
		RawAdministratorRegistry rawAdminRegistry = RawAdministratorRegistry
				.createRawAdministratorMetaDataRegistry(officeConfiguration);

		// Create process state Administrator meta-data
		AdministratorMetaData[] processStateAdministratorMetaData = rawAdminRegistry
				.createProcessStateAdministratorListing();

		// Create the registry of Work
		RawWorkRegistry workRegistry = RawWorkRegistry.createWorkRegistry(
				officeConfiguration, officeResources, rawAdminRegistry,
				rawAssetRegistry, officeFloorEscalationProcedure);

		// Create the listing of start up flows
		TaskNodeReference[] startupRefs = officeConfiguration.getStartupTasks();
		FlowMetaData[] startupFlows = new FlowMetaData[startupRefs.length];
		for (int i = 0; i < startupFlows.length; i++) {
			startupFlows[i] = new FlowMetaDataImpl(
					FlowInstigationStrategyEnum.ASYNCHRONOUS, workRegistry
							.getTaskMetaData(startupRefs[i]), null);
		}

		// Create the Office
		OfficeImpl office = new OfficeImpl(workRegistry
				.createWorkMetaDataRegistry(), rawMosRegistry
				.createManagedObjectSourceRegistry(), officeResources
				.getRawProcessManagedObjectRegistry()
				.getManagedObjectMetaData(), processStateAdministratorMetaData,
				startupFlows);

		// Link the Administrator with Tasks
		rawAdminRegistry.loadRemainingAdministratorState(workRegistry);

		// Return the Raw Office meta-data
		return new RawOfficeMetaData(officeName, office, officeConfiguration,
				officeResources, workRegistry);
	}

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link Office}.
	 */
	private final OfficeImpl office;

	/**
	 * {@link OfficeConfiguration}.
	 */
	private final OfficeConfiguration officeConfiguration;

	/**
	 * {@link RawProcessManagedObjectRegistry}.
	 */
	private final RawOfficeResourceRegistry officeResources;

	/**
	 * {@link RawWorkRegistry}.
	 */
	private final RawWorkRegistry workRegistry;

	/**
	 * Initiate.
	 * 
	 * @param office
	 *            {@link Office}.
	 */
	private RawOfficeMetaData(String officeName, OfficeImpl office,
			OfficeConfiguration officeConfiguration,
			RawOfficeResourceRegistry officeResources,
			RawWorkRegistry workRegistry) {
		this.officeName = officeName;
		this.office = office;
		this.officeConfiguration = officeConfiguration;
		this.officeResources = officeResources;
		this.workRegistry = workRegistry;
	}

	/**
	 * Obtains the name of the {@link Office}.
	 * 
	 * @return Name of the {@link Office}.
	 */
	public String getOfficeName() {
		return this.officeName;
	}

	/**
	 * Obtains the {@link Office}.
	 * 
	 * @return {@link Office}.
	 */
	public OfficeImpl getOffice() {
		return this.office;
	}

	/**
	 * Obtains the {@link OfficeConfiguration}.
	 * 
	 * @return {@link OfficeConfiguration}.
	 */
	public OfficeConfiguration getOfficeConfiguration() {
		return this.officeConfiguration;
	}

	/**
	 * Obtains the {@link RawOfficeResourceRegistry}.
	 * 
	 * @return {@link RawOfficeResourceRegistry}.
	 */
	public RawOfficeResourceRegistry getRawOfficeResourceRegistry() {
		return this.officeResources;
	}

	/**
	 * Obtains the {@link RawWorkRegistry}.
	 * 
	 * @return {@link RawWorkRegistry}.
	 */
	public RawWorkRegistry getRawWorkRegistry() {
		return this.workRegistry;
	}

}
