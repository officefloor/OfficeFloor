/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.managedobject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Raw meta-data for a bound {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectMetaDataImpl implements
		RawBoundManagedObjectMetaDataFactory, RawBoundManagedObjectMetaData {

	/**
	 * Obtains the {@link RawBoundManagedObjectMetaDataFactory}.
	 * 
	 * @return {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	public static RawBoundManagedObjectMetaDataFactory getFactory() {
		return new RawBoundManagedObjectMetaDataImpl(null, null, false);
	}

	/**
	 * Name that the {@link ManagedObject} is bound under.
	 */
	private final String boundManagedObjectName;

	/**
	 * Bound {@link ManagedObjectIndex} for the {@link ManagedObject}.
	 */
	private final ManagedObjectIndex index;

	/**
	 * Indicates if an Input {@link ManagedObject}.
	 */
	private final boolean isInput;

	/**
	 * Default instance index.
	 */
	private int defaultInstanceIndex = -1;

	/**
	 * Listing of {@link RawBoundManagedObjectInstanceMetaData} instances for
	 * this {@link RawBoundManagedObjectMetaData}.
	 */
	private final List<RawBoundManagedObjectInstanceMetaDataImpl<?>> instancesMetaData = new LinkedList<RawBoundManagedObjectInstanceMetaDataImpl<?>>();

	/**
	 * Initiate.
	 * 
	 * @param boundManagedObjectName
	 *            Name that the {@link ManagedObject} is bound under.
	 * @param index
	 *            {@link ManagedObjectIndex}.
	 * @param isInput
	 *            Indicates if an Input {@link ManagedObject}.
	 */
	private RawBoundManagedObjectMetaDataImpl(String boundManagedObjectName,
			ManagedObjectIndex index, boolean isInput) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.index = index;
		this.isInput = isInput;
	}

	/**
	 * Adds a {@link RawBoundManagedObjectInstanceMetaData} to this
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundManagedObjectName
	 *            Name that the {@link ManagedObject} is bound under.
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @param dependenciesConfiguration
	 *            Listing of the {@link ManagedObjectDependencyConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param governanceConfiguration
	 *            Listing of the {@link ManagedObjectGovernanceConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addInstance(String boundManagedObjectName,
			RawManagedObjectMetaData rawMoMetaData,
			ManagedObjectDependencyConfiguration[] dependenciesConfiguration,
			ManagedObjectGovernanceConfiguration[] governanceConfiguration) {

		// Obtain the index for the instance
		int instanceIndex = this.instancesMetaData.size();

		// Add the instance meta-data
		this.instancesMetaData
				.add(new RawBoundManagedObjectInstanceMetaDataImpl(
						boundManagedObjectName, this, instanceIndex,
						rawMoMetaData, dependenciesConfiguration,
						governanceConfiguration));
	}

	/*
	 * ============== RawBoundManagedObjectMetaDataFactory ====================
	 */

	@Override
	public RawBoundManagedObjectMetaData[] constructBoundManagedObjectMetaData(
			ManagedObjectConfiguration<?>[] boundManagedObjectConfiguration,
			OfficeFloorIssues issues,
			ManagedObjectScope scope,
			AssetType assetType,
			String assetName,
			AssetManagerFactory assetManagerFactory,
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects,
			Map<String, RawBoundManagedObjectMetaData> scopeManagedObjects,
			RawManagingOfficeMetaData<?>[] inputManagedObjects,
			Map<String, String> boundInputManagedObjects,
			Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData) {

		// Handle if null scope managed objects
		if (scopeManagedObjects == null) {
			scopeManagedObjects = Collections.emptyMap();
		}

		// Create details for obtaining the managed object instances
		Map<String, RawBoundManagedObjectMetaDataImpl> boundMo = new HashMap<String, RawBoundManagedObjectMetaDataImpl>();
		List<RawBoundManagedObjectMetaDataImpl> boundMoList = new LinkedList<RawBoundManagedObjectMetaDataImpl>();
		int boundMoIndex = 0;

		// Obtain the bound managed object instances
		if (boundManagedObjectConfiguration != null) {
			NEXT_MO: for (ManagedObjectConfiguration<?> mo : boundManagedObjectConfiguration) {

				// Obtain the bound managed object name
				String boundMoName = mo.getBoundManagedObjectName();
				if (ConstructUtil.isBlank(boundMoName)) {
					issues.addIssue(assetType, assetName,
							"No bound name for managed object");
					continue NEXT_MO; // no bound managed object
				}

				// Ensure no name clash with another bound ManagedObject
				if (boundMo.containsKey(boundMoName)) {
					issues.addIssue(AssetType.MANAGED_OBJECT, boundMoName,
							"Name clash between bound Managed Objects (name="
									+ boundMoName + ")");
					continue NEXT_MO; // name clash
				}

				// Obtain the registered office managed object name
				String officeMoName = mo.getOfficeManagedObjectName();
				if (ConstructUtil.isBlank(officeMoName)) {
					issues.addIssue(assetType, assetName,
							"No office name for bound managed object of name '"
									+ boundMoName + "'");
					continue NEXT_MO; // no managed object
				}

				// Obtain the raw managed object meta-data
				RawManagedObjectMetaData<?, ?> rawMoMetaData = registeredManagedObjects
						.get(officeMoName);
				if (rawMoMetaData == null) {
					issues.addIssue(assetType, assetName,
							"No managed object by name '" + officeMoName
									+ "' registered with the Office");
					continue NEXT_MO; // no managed object
				}

				// Create the index of this managed object
				ManagedObjectIndex index = new ManagedObjectIndexImpl(scope,
						boundMoIndex++);

				// Obtain the dependencies configuration
				ManagedObjectDependencyConfiguration<?>[] dependenciesConfiguration = mo
						.getDependencyConfiguration();

				// Obtain the governance configuration
				ManagedObjectGovernanceConfiguration[] governanceConfiguration = mo
						.getGovernanceConfiguration();

				// Create the bound ManagedObject meta-data (with instance)
				RawBoundManagedObjectMetaDataImpl rawBoundMoMetaData = new RawBoundManagedObjectMetaDataImpl(
						boundMoName, index, false);
				rawBoundMoMetaData.addInstance(boundMoName, rawMoMetaData,
						dependenciesConfiguration, governanceConfiguration);

				// Register the bound managed object
				boundMo.put(boundMoName, rawBoundMoMetaData);
				boundMoList.add(rawBoundMoMetaData);
			}
		}

		// Bind the input ManagedObjects
		if (inputManagedObjects != null) {
			NEXT_MO: for (RawManagingOfficeMetaData<?> inputManagedObject : inputManagedObjects) {

				// Obtain the input ManagedObject configuration
				InputManagedObjectConfiguration<?> inputConfiguration = inputManagedObject
						.getInputManagedObjectConfiguration();
				if (inputConfiguration == null) {
					// No issue, Managed Object not Input (only office managing)
					continue NEXT_MO;
				}

				// Obtain the bound managed object name
				String boundMoName = inputConfiguration
						.getBoundManagedObjectName();
				if (ConstructUtil.isBlank(boundMoName)) {
					issues.addIssue(assetType, assetName,
							"No bound name for input managed object");
					continue NEXT_MO; // no bound managed object
				}

				// Ensure no name clash with bound ManagedObject
				RawBoundManagedObjectMetaDataImpl possibleClash = boundMo
						.get(boundMoName);
				if ((possibleClash != null) && (!possibleClash.isInput)) {

					// Only clash if not same managed object
					RawManagedObjectMetaData<?, ?> inputRawMetaData = inputManagedObject
							.getRawManagedObjectMetaData();
					for (RawBoundManagedObjectInstanceMetaData<?> possibleClashRawInstanceMetaData : possibleClash
							.getRawBoundManagedObjectInstanceMetaData()) {
						RawManagedObjectMetaData<?, ?> possibleClashRawMetaData = possibleClashRawInstanceMetaData
								.getRawManagedObjectMetaData();
						if (inputRawMetaData == possibleClashRawMetaData) {
							// Same object so no clash
							continue NEXT_MO; // already bound
						}
					}

					// Clash of names for different managed objects
					issues.addIssue(AssetType.MANAGED_OBJECT, boundMoName,
							"Name clash between bound and input Managed Objects (name="
									+ boundMoName + ")");
					continue NEXT_MO; // name clash
				}

				// Obtain the input ManagedObject meta-data
				RawManagedObjectMetaData<?, ?> rawMoMetaData = inputManagedObject
						.getRawManagedObjectMetaData();

				// Obtain the dependencies configuration
				ManagedObjectDependencyConfiguration<?>[] dependenciesConfiguration = inputConfiguration
						.getDependencyConfiguration();

				// Obtain the governance configuration
				ManagedObjectGovernanceConfiguration[] governanceConfiguration = inputConfiguration
						.getGovernanceConfiguration();

				// Obtain the bound ManagedObject meta-data
				RawBoundManagedObjectMetaDataImpl rawBoundMoMetaData;
				if (possibleClash != null) {
					// Inputs bound to same name
					rawBoundMoMetaData = possibleClash;
				} else {
					// Create the bound ManagedObject meta-data
					ManagedObjectIndex index = new ManagedObjectIndexImpl(
							scope, boundMoIndex++);
					rawBoundMoMetaData = new RawBoundManagedObjectMetaDataImpl(
							boundMoName, index, true);

					// Register the input managed object
					boundMo.put(boundMoName, rawBoundMoMetaData);
					boundMoList.add(rawBoundMoMetaData);
				}

				// Add the Input ManagedObject instance
				rawBoundMoMetaData.addInstance(boundMoName, rawMoMetaData,
						dependenciesConfiguration, governanceConfiguration);
			}
		}

		// Load default instance indexes, dependencies, meta-data
		NEXT_MO: for (RawBoundManagedObjectMetaDataImpl moMetaData : boundMoList) {

			// If only one instance than is the default instance
			if (moMetaData.instancesMetaData.size() == 1) {
				// Only the single instance
				moMetaData.defaultInstanceIndex = 0;
			} else {
				// Multiple instances, obtain default managed object source name
				String boundMoName = moMetaData.boundManagedObjectName;
				String defaultManagedObjectSourceName = null;
				if (boundInputManagedObjects != null) {
					defaultManagedObjectSourceName = boundInputManagedObjects
							.get(boundMoName);
				}
				if (ConstructUtil.isBlank(defaultManagedObjectSourceName)) {
					// Must have the name of the bound Managed Object Source
					issues.addIssue(AssetType.MANAGED_OBJECT, boundMoName,
							"Bound Managed Object Source must be specified for Input Managed Object '"
									+ boundMoName + "'");
					continue NEXT_MO; // must have bound name

				} else {
					// Search for the instance containing managed object source
					NEXT_MOS: for (int i = 0; i < moMetaData.instancesMetaData
							.size(); i++) {
						RawBoundManagedObjectInstanceMetaDataImpl<?> instanceMetaData = moMetaData.instancesMetaData
								.get(i);

						// Determine if instance
						String managedObjectSourceName = instanceMetaData
								.getRawManagedObjectMetaData()
								.getManagedObjectName();
						if (defaultManagedObjectSourceName
								.equals(managedObjectSourceName)) {
							// Have the instance, so specify it as default
							moMetaData.defaultInstanceIndex = i;
							break NEXT_MOS; // default instance found
						}
					}
					if (moMetaData.defaultInstanceIndex < 0) {
						issues.addIssue(
								AssetType.MANAGED_OBJECT,
								boundMoName,
								"Managed Object Source '"
										+ defaultManagedObjectSourceName
										+ "' not linked to Input Managed Object '"
										+ boundMoName
										+ "' for being the bound instance");
						continue NEXT_MO; // must have bound instance index
					}
				}
			}

			// Load dependencies and meta-data for each instance
			for (RawBoundManagedObjectInstanceMetaDataImpl<?> instanceMetaData : moMetaData.instancesMetaData) {

				// Create the mapping of scope managed objects
				Map<String, RawBoundManagedObjectMetaData> dependencyMo = new HashMap<String, RawBoundManagedObjectMetaData>();
				dependencyMo.putAll(scopeManagedObjects); // scope first
				dependencyMo.putAll(boundMo); // bound possibly overwrite scope

				// Load the dependencies
				instanceMetaData.loadDependencies(issues, dependencyMo);

				// Load the governance
				instanceMetaData.loadGovernance(rawGovernanceMetaData, issues);

				// Load the meta-data
				instanceMetaData.loadManagedObjectMetaData(assetManagerFactory,
						issues);
			}
		}

		// Return the bound managed object meta-data
		return boundMoList.toArray(new RawBoundManagedObjectMetaData[0]);
	}

	/*
	 * ================= RawBoundManagedObjectMetaData =======================
	 */

	@Override
	public String getBoundManagedObjectName() {
		return this.boundManagedObjectName;
	}

	@Override
	public ManagedObjectIndex getManagedObjectIndex() {
		return this.index;
	}

	@Override
	public int getDefaultInstanceIndex() {
		return this.defaultInstanceIndex;
	}

	@Override
	public RawBoundManagedObjectInstanceMetaData<?>[] getRawBoundManagedObjectInstanceMetaData() {
		// Provide the instances in order of their indexes
		return this.instancesMetaData
				.toArray(new RawBoundManagedObjectInstanceMetaData[0]);
	}

}