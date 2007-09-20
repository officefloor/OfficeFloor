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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.impl.execute.ExtensionInterfaceMetaDataImpl;
import net.officefloor.frame.impl.execute.TaskDutyAssociationImpl;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.WorkAdministratorConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;

/**
 * Registry of the
 * {@link net.officefloor.frame.impl.RawWorkAministratorMetaData}.
 * 
 * @author Daniel
 */
public class RawWorkAdministratorRegistry {

	/**
	 * Obtains the {@link AdministratorMetaData} registry for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @param workConfig
	 *            Configuration of the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 * @param rawAdminRegistry
	 *            Registry of {@link RawAdministratorMetaData} instances for the
	 *            Office.
	 * @param processManagedObjects
	 *            {@link ManagedObjectMetaData} for the
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            bound
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances.
	 * @return {@link RawAdministratorMetaData} registry for the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 * @throws ConfigurationException
	 *             Indicates invalid configuration.
	 */
	@SuppressWarnings("unchecked")
	public static RawWorkAdministratorRegistry createWorkAdministratorRegistry(
			WorkConfiguration workConfig,
			RawOfficeResourceRegistry officeResources,
			RawAdministratorRegistry rawAdminRegistry,
			RawWorkManagedObjectRegistry workMoRegistry)
			throws ConfigurationException {

		// Create the listing of work bound administrators
		List<RawWorkAdministratorMetaData> workBoundRequiredListing = new LinkedList<RawWorkAdministratorMetaData>();

		// Create the listing of process bound administrators
		List<RawWorkAdministratorMetaData> processBoundRequiredListing = new LinkedList<RawWorkAdministratorMetaData>();

		// Create the registry of the Work Administrator meta-data
		for (WorkAdministratorConfiguration workAdminConfig : workConfig
				.getAdministratorConfiguration()) {

			// Obtain the name to register the administrator
			String workAdminName = workAdminConfig.getWorkAdministratorName();

			// Obtain the raw meta-data for the administrator
			RawAdministratorMetaData rawMetaData = rawAdminRegistry
					.getRawAdministratorMetaData(workAdminConfig
							.getAdministratorId());
			if (rawMetaData == null) {
				throw new ConfigurationException("Unknown raw administrator '"
						+ workAdminConfig.getAdministratorId() + "'");
			}

			// Determine meta-data based on scope
			AdministratorMetaData adminMetaData;
			switch (rawMetaData.getAdministratorScope()) {
			case PROCESS:
				// Process scoped meta-data
				adminMetaData = rawMetaData.createAdministratorMetaData();

				// Add to listing of process bound
				processBoundRequiredListing
						.add(new RawWorkAdministratorMetaData(workAdminName,
								adminMetaData, workAdminConfig));

				break;

			default: // Work scoped

				// Determine the extension interface type
				Class<?> eiType = rawMetaData.getAdministratorSource()
						.getMetaData().getExtensionInterface();

				// Create the listing managed objects to be administerred
				String[] workMoNames = workAdminConfig
						.getWorkManagedObjectNames();
				ExtensionInterfaceMetaData<?>[] eiMetaData = new ExtensionInterfaceMetaData[workMoNames.length];
				for (int i = 0; i < eiMetaData.length; i++) {

					// Obtain the name of managed object
					String workMoName = workMoNames[i];

					// Obtain the index of the managed object
					int moIndex = workMoRegistry
							.getIndexByWorkManagedObjectName(workMoName);

					// Obtain the managed object meta-data
					ManagedObjectMetaData moMetaData = workMoRegistry
							.getWorkManagedObjectListing()[moIndex];
					if (moMetaData.getProcessStateManagedObjectIndex() != ManagedObjectMetaData.NON_PROCESS_INDEX) {
						// Process bound managed object
						moMetaData = officeResources
								.getRawProcessManagedObjectRegistry()
								.getManagedObjectMetaData()[moMetaData
								.getProcessStateManagedObjectIndex()];
					}

					// Obtain the extension interface factory
					ExtensionInterfaceFactory eiFactory = null;
					for (ManagedObjectExtensionInterfaceMetaData moEiMetaData : moMetaData
							.getManagedObjectSource().getMetaData()
							.getExtensionInterfacesMetaData()) {
						// Check if extension interface
						if (eiType.isAssignableFrom(moEiMetaData
								.getExtensionInterfaceType())) {
							eiFactory = moEiMetaData
									.getExtensionInterfaceFactory();
						}
					}

					// Ensure managed object supports extension interface
					if (eiFactory == null) {
						throw new ConfigurationException("Managed Object '"
								+ workMoName + "' on work '"
								+ workConfig.getWorkName()
								+ "' does not support extension interface "
								+ eiType.getName());
					}

					// Create the extension interface meta-data
					eiMetaData[i] = new ExtensionInterfaceMetaDataImpl(moIndex,
							eiFactory);
				}

				// Work scoped meta-data
				adminMetaData = rawMetaData
						.createAdministratorMetaData(eiMetaData);

				// Add to listing of work bound
				workBoundRequiredListing.add(new RawWorkAdministratorMetaData(
						workAdminName, adminMetaData, workAdminConfig));

				break;
			}
		}

		// Create the listing of administrator meta-data and compile indexes
		AdministratorMetaData[] adminMetaData = new AdministratorMetaData[workBoundRequiredListing
				.size()
				+ processBoundRequiredListing.size()];
		Map<String, Integer> adminIndexes = new HashMap<String, Integer>();
		int index = 0;

		// Load the work bound administrators first
		for (RawWorkAdministratorMetaData rawMetaData : workBoundRequiredListing) {
			// Load administrator details
			adminMetaData[index] = rawMetaData.metaData;
			adminIndexes.put(rawMetaData.name, new Integer(index));

			// Increment index
			index++;
		}

		// Load process bound administrators second
		for (RawWorkAdministratorMetaData rawMetaData : processBoundRequiredListing) {
			// Load administrator details
			adminMetaData[index] = rawMetaData.metaData;
			adminIndexes.put(rawMetaData.name, new Integer(index));

			// Increment index
			index++;
		}

		// Create the Work Administrator meta-data
		RawWorkAdministratorRegistry workAdminRegistry = new RawWorkAdministratorRegistry(
				workConfig.getWorkName(), adminMetaData, adminIndexes);

		// Return the Work Administrator meta-data registry
		return workAdminRegistry;
	}

	/**
	 * Name of the {@link net.officefloor.frame.api.execute.Work} this
	 * represents the
	 * {@link net.officefloor.frame.spi.administration.Administrator} instances.
	 */
	protected final String workName;

	/**
	 * {@link AdministratorMetaData} for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 */
	protected final AdministratorMetaData<?, ?>[] adminMetaData;

	/**
	 * Indexes of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} instances
	 * for the {@link net.officefloor.frame.api.execute.Work}.
	 */
	protected final Map<String, Integer> adminIndexes;

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of the {@link net.officefloor.frame.api.execute.Work}.
	 * @param adminMetaData
	 *            {@link AdministratorMetaData} for the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 * @param adminIndexes
	 *            Indexes of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            instances by their names.
	 */
	private RawWorkAdministratorRegistry(String workName,
			AdministratorMetaData<?, ?>[] adminMetaData,
			Map<String, Integer> adminIndexes) {
		this.workName = workName;
		this.adminMetaData = adminMetaData;
		this.adminIndexes = adminIndexes;
	}

	/**
	 * Creates the listing of {@link AdministratorMetaData} for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @return Listing of {@link AdministratorMetaData} for the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 */
	public AdministratorMetaData<?, ?>[] getWorkAdministratorListing() {
		return this.adminMetaData;
	}

	/**
	 * Obtains the index of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} on the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @param workAdministratorName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}.
	 * @return Index of the
	 *         {@link net.officefloor.frame.spi.administration.Administrator} on
	 *         the {@link net.officefloor.frame.api.execute.Work}.
	 * @throws ConfigurationException
	 *             If unknown
	 *             {@link net.officefloor.frame.spi.administration.Administrator}
	 *             name.
	 */
	public int getWorkIndex(String workAdministratorName)
			throws ConfigurationException {

		// Obtain the index
		Integer index = this.adminIndexes.get(workAdministratorName);

		// Ensure known work administrator
		if (index == null) {
			throw new ConfigurationException("Unknown work administrator '"
					+ workAdministratorName + "' for work '" + this.workName
					+ "'");
		}

		// Return the index
		return index.intValue();
	}

	/**
	 * Creates the {@link TaskDutyAssociation} instances from the input
	 * configuration.
	 * 
	 * @param dutyConfiguration
	 *            Listing of {@link TaskDutyConfiguration}.
	 * @return {@link TaskDutyAssociation} for the input configuration.
	 * @throws ConfigurationException
	 *             If failure in configuration.
	 */
	@SuppressWarnings("unchecked")
	public TaskDutyAssociation<?>[] createTaskAdministration(
			TaskDutyConfiguration[] dutyConfiguration)
			throws ConfigurationException {
		// Create the task duty associations
		TaskDutyAssociation<?>[] taskDuties = new TaskDutyAssociation[dutyConfiguration.length];
		for (int i = 0; i < taskDuties.length; i++) {

			// Obtain the current duty configuration
			TaskDutyConfiguration dutyConfig = dutyConfiguration[i];

			// Ensure have administrator
			String adminName = dutyConfig.getAdministratorName();
			Integer adminIndex = this.adminIndexes.get(adminName);
			if (adminIndex == null) {
				throw new ConfigurationException("Administrator '" + adminName
						+ "' can not be found on work '" + this.workName + "'");
			}

			// Obtain the duty key
			Enum dutyKey = dutyConfig.getDuty();

			// Register the task duty association
			taskDuties[i] = new TaskDutyAssociationImpl(adminIndex.intValue(),
					dutyKey);
		}

		// Return the task duty associations
		return taskDuties;
	}

}

/**
 * Structure to link
 * {@link net.officefloor.frame.internal.structure.AdministratorMetaData} to its
 * name.
 */
class RawWorkAdministratorMetaData {

	/**
	 * Name of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 */
	protected final String name;

	/**
	 * {@link AdministratorMetaData}.
	 */
	protected final AdministratorMetaData<?, ?> metaData;

	/**
	 * {@link WorkAdministratorConfiguration}.
	 */
	protected final WorkAdministratorConfiguration adminConfig;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}.
	 * @param metaData
	 *            {@link AdministratorMetaData}.
	 * @param adminConfig
	 *            {@link WorkAdministratorConfiguration}.
	 */
	public RawWorkAdministratorMetaData(String name,
			AdministratorMetaData<?, ?> metaData,
			WorkAdministratorConfiguration adminConfig) {
		this.name = name;
		this.metaData = metaData;
		this.adminConfig = adminConfig;
	}

}