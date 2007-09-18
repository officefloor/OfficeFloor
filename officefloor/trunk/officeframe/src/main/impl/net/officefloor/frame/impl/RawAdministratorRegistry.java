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

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeScope;
import net.officefloor.frame.impl.execute.AdministratorMetaDataImpl;
import net.officefloor.frame.impl.execute.DutyMetaDataImpl;
import net.officefloor.frame.impl.execute.FlowMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.DutyConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * Registry of the {@link net.officefloor.frame.impl.RawAdministratorMetaData}.
 * 
 * @author Daniel
 */
public class RawAdministratorRegistry {

	/**
	 * Creates the registry of {@link RawAdministratorMetaData} instances by
	 * thier respective names.
	 * 
	 * @param officeConfiguration
	 *            Configuration of the Office.
	 * @return Registry of {@link RawAdministratorMetaData} instances by thier
	 *         respective names.
	 * @throws Exception
	 *             If fails.
	 */
	public static RawAdministratorRegistry createRawAdministratorMetaDataRegistry(
			OfficeConfiguration officeConfiguration) throws Exception {

		// Create the registry of raw Administrator meta-data
		Map<String, RawAdministratorMetaData> rasRegistry = new HashMap<String, RawAdministratorMetaData>();
		for (AdministratorSourceConfiguration asConfig : officeConfiguration
				.getAdministratorSourceConfiguration()) {
			rasRegistry.put(asConfig.getAdministratorName(),
					RawAdministratorMetaData
							.createRawAdministratorMetaData(asConfig));
		}

		// Return the registry
		return new RawAdministratorRegistry(rasRegistry);
	}

	/**
	 * Registry of the {@link RawAdministratorMetaData}.
	 */
	protected final Map<String, RawAdministratorMetaData> rawAdminRegistry;

	/**
	 * Initiate.
	 * 
	 * @param rawAdminRegistry
	 *            Registry of the {@link RawAdministratorMetaData}.
	 */
	private RawAdministratorRegistry(
			Map<String, RawAdministratorMetaData> rawAdminRegistry) {
		this.rawAdminRegistry = rawAdminRegistry;
	}

	/**
	 * Loads the remaining state for the {@link AdministratorMetaData}.
	 * 
	 * @param workRegistry
	 *            {@link RawWorkRegistry}.
	 * @throws ConfigurationException
	 *             If fails configuration.
	 */
	@SuppressWarnings("unchecked")
	public <A extends Enum<A>> void loadRemainingAdministratorState(
			RawWorkRegistry workRegistry) throws ConfigurationException {

		// Iterate over the raw administrator objects
		for (RawAdministratorMetaData radmin : this.rawAdminRegistry.values()) {

			// Obtain the administrator configuration
			AdministratorSourceConfiguration sourceConfig = radmin
					.getAdministratorSourceConfiguration();

			// Obtain the administrator source
			AdministratorSource source = radmin.getAdministratorSource();

			// Obtain the duty keys
			Class<A> dutyKeys = source.getMetaData().getAministratorDutyKeys();

			// Create the map of duties
			Map<A, DutyMetaData> duties;
			if (dutyKeys == null) {
				// No duties
				duties = Collections.EMPTY_MAP;
			} else {

				// Create the list of duties
				duties = new EnumMap<A, DutyMetaData>(dutyKeys);

				// Create the registry of duty configuration
				EnumMap<A, DutyConfiguration<?>> dutyConfigs = new EnumMap<A, DutyConfiguration<?>>(
						dutyKeys);
				for (DutyConfiguration<?> dutyConfig : sourceConfig
						.getDutyConfiguration()) {
					dutyConfigs.put((A) dutyConfig.getDutyKey(), dutyConfig);
				}

				// Load the registry of duties
				for (A key : dutyKeys.getEnumConstants()) {

					// Obtain the duty configuration
					DutyConfiguration<?> dutyConfig = dutyConfigs.get(key);

					// Create teh flow links
					FlowMetaData<?>[] flowLinks;
					if (dutyConfig == null) {
						// No configuration, therefore default to link no flows
						flowLinks = new FlowMetaData[0];
					} else {

						// Create the flow links from configuration
						TaskNodeReference[] flowLinkConfig = dutyConfig
								.getLinkedProcessConfiguration();
						flowLinks = new FlowMetaData[flowLinkConfig.length];
						for (int i = 0; i < flowLinks.length; i++) {

							// No flow manager required as all parallel
							// instigated

							// Create the current flow meta-data
							FlowMetaData<?> flowLink = new FlowMetaDataImpl(
									FlowInstigationStrategyEnum.PARALLEL,
									workRegistry
											.getTaskMetaData(flowLinkConfig[i]),
									null);

							// Load the process link
							flowLinks[i] = flowLink;
						}
					}

					// Create the duty meta-data
					DutyMetaData dutyMetaData = new DutyMetaDataImpl(flowLinks);

					// Register the duty
					duties.put(key, dutyMetaData);
				}
			}

			// Iterate over the meta-data of the Administrators
			for (AdministratorMetaDataImpl adminMetaData : radmin
					.getAdministratorMetaData()) {
				// Load the remaining state to the meta-data
				adminMetaData.loadRemainingState(duties);
			}
		}
	}

	/**
	 * Obtains the {@link RawAdministratorMetaData} for the input name.
	 * 
	 * @param administratorId
	 *            Name of the {@link Administrator}.
	 * @return {@link RawAdministratorMetaData} for the input name.
	 */
	public RawAdministratorMetaData getRawAdministratorMetaData(
			String administratorId) {
		return this.rawAdminRegistry.get(administratorId);
	}

	/**
	 * Obtains the {@link AdministratorMetaData} for the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}. Also
	 * specifies the index within the {@link RawAdministratorMetaData} of the
	 * {@link Administrator} within the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * 
	 * @return {@link AdministratorMetaData} for the
	 *         {@link net.officefloor.frame.internal.structure.ProcessState}.
	 */
	@SuppressWarnings("unchecked")
	public AdministratorMetaData[] createProcessStateAdministratorListing()
			throws ConfigurationException {

		// Create the listing of process managed objects
		int currentIndex = 0;
		List<AdministratorMetaData> processAdministratorList = new LinkedList<AdministratorMetaData>();
		for (RawAdministratorMetaData metaData : this.rawAdminRegistry.values()) {
			if (metaData.getAdministratorScope() == OfficeScope.PROCESS) {

				// Add the meta-data to the listing
				processAdministratorList.add(metaData
						.createAdministratorMetaData());

				// Specify the index
				metaData.setProcessStateIndex(currentIndex++);
			}
		}

		// Return the process administrator meta-data
		return processAdministratorList.toArray(new AdministratorMetaData[0]);
	}

}
