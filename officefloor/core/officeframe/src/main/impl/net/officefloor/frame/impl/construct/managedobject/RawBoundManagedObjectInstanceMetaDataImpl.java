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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectGovernanceMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;

/**
 * {@link RawBoundManagedObjectInstanceMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectInstanceMetaDataImpl<D extends Enum<D>>
		implements RawBoundManagedObjectInstanceMetaData<D> {

	/**
	 * Name that the {@link ManagedObject} is bound under.
	 */
	private final String boundManagedObjectName;

	/**
	 * {@link RawBoundManagedObjectMetaData} containing this
	 * {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	private final RawBoundManagedObjectMetaData rawBoundMetaData;

	/**
	 * Index of this {@link RawBoundManagedObjectInstanceMetaData} within its
	 * containing {@link RawBoundManagedObjectMetaData}.
	 */
	private final int instanceIndex;

	/**
	 * {@link RawManagedObjectMetaData}.
	 */
	private final RawManagedObjectMetaData<D, ?> rawMoMetaData;

	/**
	 * Listing of the {@link ManagedObjectDependencyConfiguration} for the
	 * {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	private final ManagedObjectDependencyConfiguration<D>[] dependenciesConfiguration;

	/**
	 * Listing of the {@link ManagedObjectGovernanceConfiguration} for the
	 * {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	private final ManagedObjectGovernanceConfiguration[] governanceConfiguration;

	/**
	 * Dependencies.
	 */
	private RawBoundManagedObjectMetaData[] dependencies;

	/**
	 * {@link ManagedObjectGovernanceMetaData}.
	 */
	private ManagedObjectGovernanceMetaData<?>[] governanceMetaData;

	/**
	 * {@link ManagedObjectMetaData}.
	 */
	private ManagedObjectMetaData<D> managedObjectMetaData;

	/**
	 * Initiate.
	 * 
	 * @param boundManagedObjectName
	 *            Name that the {@link ManagedObject} is bound under.
	 * @param rawBoundMetaData
	 *            {@link RawBoundManagedObjectMetaData} containing this
	 *            {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param instanceIndex
	 *            Index of this {@link RawBoundManagedObjectInstanceMetaData}
	 *            within its containing {@link RawBoundManagedObjectMetaData}.
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @param dependenciesConfiguration
	 *            Listing of the {@link ManagedObjectDependencyConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param governanceConfiguration
	 *            Listing of the {@link ManagedObjectGovernanceConfiguration}
	 *            for the {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	public RawBoundManagedObjectInstanceMetaDataImpl(String boundManagedObjectName,
			RawBoundManagedObjectMetaData rawBoundMetaData, int instanceIndex,
			RawManagedObjectMetaData<D, ?> rawMoMetaData,
			ManagedObjectDependencyConfiguration<D>[] dependenciesConfiguration,
			ManagedObjectGovernanceConfiguration[] governanceConfiguration) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.rawBoundMetaData = rawBoundMetaData;
		this.instanceIndex = instanceIndex;
		this.rawMoMetaData = rawMoMetaData;
		this.dependenciesConfiguration = dependenciesConfiguration;
		this.governanceConfiguration = governanceConfiguration;
	}

	/**
	 * Loads the dependencies.
	 * 
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param boundMo
	 *            Mapping of {@link RawBoundManagedObjectMetaData} by its scope
	 *            bound name.
	 */
	public void loadDependencies(OfficeFloorIssues issues, Map<String, RawBoundManagedObjectMetaData> boundMo) {

		// Obtain the dependency meta-data
		ManagedObjectDependencyMetaData<D>[] dependencyMetaDatas = this.rawMoMetaData.getManagedObjectSourceMetaData()
				.getDependencyMetaData();

		// Determine if dependencies for managed object
		if ((dependencyMetaDatas == null) || (dependencyMetaDatas.length == 0)) {

			// No dependencies but issue if dependencies configured
			if ((this.dependenciesConfiguration != null) && (this.dependenciesConfiguration.length > 0)) {
				issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
						"No dependencies required but dependencies configured");
			}

			// No dependencies for managed object
			this.dependencies = new RawBoundManagedObjectMetaData[0];
			return;
		}

		// Create the dependency mappings for the configuration
		Map<Integer, ManagedObjectDependencyConfiguration<D>> dependencyMappings = new HashMap<Integer, ManagedObjectDependencyConfiguration<D>>();
		for (int i = 0; i < this.dependenciesConfiguration.length; i++) {
			ManagedObjectDependencyConfiguration<D> dependencyConfiguration = this.dependenciesConfiguration[i];

			// Ensure have dependency configuration
			if (dependencyConfiguration == null) {
				continue;
			}

			// Obtain the index to identify the dependency
			D dependencyKey = dependencyConfiguration.getDependencyKey();
			int index = (dependencyKey != null ? dependencyKey.ordinal() : i);

			// Load the dependency at its index
			dependencyMappings.put(new Integer(index), dependencyConfiguration);
		}

		// Load the dependencies
		Map<Integer, RawBoundManagedObjectMetaData> dependencies = new HashMap<Integer, RawBoundManagedObjectMetaData>();
		for (int i = 0; i < dependencyMetaDatas.length; i++) {
			ManagedObjectDependencyMetaData<D> dependencyMetaData = dependencyMetaDatas[i];

			// Obtain the index to identify the dependency
			D dependencyKey = dependencyMetaData.getKey();
			int index = (dependencyKey != null ? dependencyKey.ordinal() : i);

			// Create name to identify dependency
			String label = dependencyMetaData.getLabel();
			String dependencyLabel = "dependency " + index + " (key="
					+ (dependencyKey != null ? dependencyKey.toString() : "<indexed>") + ", label="
					+ (!ConstructUtil.isBlank(label) ? label : "<no label>") + ")";

			// Obtain the mapping for the dependency
			ManagedObjectDependencyConfiguration<D> dependencyMapping = dependencyMappings.get(new Integer(index));
			if (dependencyMapping == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
						"No mapping configured for " + dependencyLabel);
				return; // no dependency mapping
			}

			// Remove configuration for later check no extra configured
			dependencyMappings.remove(new Integer(index));

			// Obtain the dependent managed object
			String dependentMoName = dependencyMapping.getScopeManagedObjectName();
			if (ConstructUtil.isBlank(dependentMoName)) {
				issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
						"No dependency name configured for " + dependencyLabel);
				return; // no dependency specified
			}
			RawBoundManagedObjectMetaData dependency = boundMo.get(dependentMoName);
			if (dependency == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
						"No dependent " + ManagedObject.class.getSimpleName() + " by name '" + dependentMoName
								+ "' for " + dependencyLabel);
				return; // no dependency
			}

			// Obtain require type for dependencies instances
			Class<?> requiredType = dependencyMetaData.getType();

			// Ensure each dependency instance is of correct type
			boolean isDependencyIssue = false;
			for (RawBoundManagedObjectInstanceMetaData<?> dependencyInstanceMetaData : dependency
					.getRawBoundManagedObjectInstanceMetaData()) {
				RawManagedObjectMetaData<?, ?> rawDependencyMetaData = dependencyInstanceMetaData
						.getRawManagedObjectMetaData();
				Class<?> dependencyType = rawDependencyMetaData.getObjectType();
				if (!requiredType.isAssignableFrom(dependencyType)) {
					// incompatible dependency
					isDependencyIssue = true;
					issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
							"Incompatible dependency for " + dependencyLabel + " (required type="
									+ requiredType.getName() + ", dependency type=" + dependencyType.getName()
									+ ", ManagedObjectSource=" + rawDependencyMetaData.getManagedObjectName() + ")");
				}
			}
			if (isDependencyIssue) {
				return; // incompatible so can not load dependencies
			}

			// Load the dependency
			dependencies.put(new Integer(index), dependency);
		}

		// Ensure there are no additional dependencies configured
		if (dependencyMappings.size() > 0) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
					"Extra dependencies configured than required by ManagedObjectSourceMetaData");
			return; // additional dependencies configured
		}

		// Specify the dependencies on the bound managed object
		this.dependencies = ConstructUtil.toArray(dependencies, new RawBoundManagedObjectMetaData[0]);
	}

	/**
	 * Loads the {@link ManagedObjectGovernanceMetaData}.
	 * 
	 * @param rawGovernanceMetaDatas
	 *            {@link RawGovernanceMetaData} of the {@link Office} by its
	 *            {@link Office} registered name.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadGovernance(Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaDatas,
			OfficeFloorIssues issues) {

		// Determine if governance
		if ((this.governanceConfiguration == null) || (this.governanceConfiguration.length == 0)) {
			this.governanceMetaData = new ManagedObjectGovernanceMetaData<?>[0];
			return; // No governance
		}

		// Load the governance
		this.governanceMetaData = new ManagedObjectGovernanceMetaData<?>[this.governanceConfiguration.length];
		NEXT_GOVERNANCE: for (int i = 0; i < this.governanceConfiguration.length; i++) {
			ManagedObjectGovernanceConfiguration configuration = this.governanceConfiguration[i];

			// Obtain the governance
			String governanceName = configuration.getGovernanceName();
			RawGovernanceMetaData rawGovernanceMetaData = rawGovernanceMetaDatas.get(governanceName);

			// Ensure have the governance
			if (rawGovernanceMetaData == null) {
				// No governance
				issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
						"Unknown governance '" + governanceName + "'");
				continue NEXT_GOVERNANCE;
			}

			// Obtain the index of the governance
			int governanceIndex = rawGovernanceMetaData.getGovernanceIndex();

			// Obtain the Governance extension interface
			Class<?> governanceExtensionInterface = rawGovernanceMetaData.getExtensionInterfaceType();

			// Create the extension interface extractor
			ManagedObjectExtensionInterfaceMetaData<?>[] eiMetaDatas = this.rawMoMetaData
					.getManagedObjectSourceMetaData().getExtensionInterfacesMetaData();
			for (ManagedObjectExtensionInterfaceMetaData<?> eiMetaData : eiMetaDatas) {

				// Determine if extension interface to use
				Class<?> extensionInterfaceType = eiMetaData.getExtensionInterfaceType();
				if (governanceExtensionInterface.isAssignableFrom(extensionInterfaceType)) {

					// Found the extension interface, so obtain the factory
					ExtensionInterfaceFactory<?> factory = eiMetaData.getExtensionInterfaceFactory();

					// Load the governance meta-data
					this.governanceMetaData[i] = new ManagedObjectGovernanceMetaDataImpl(governanceIndex, factory);
				}
			}

			// Ensure have the governance
			if (this.governanceMetaData[i] == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
						"Extension interface of type " + governanceExtensionInterface.getName()
								+ " is not available from Managed Object for Governance '" + governanceName + "'");
			}
		}
	}

	/**
	 * Loads the {@link ManagedObjectMetaData} for the
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param assetType
	 *            {@link AssetType} requiring the {@link ManagedObject}.
	 * @param assetName
	 *            Name of the {@link Asset} requiring the {@link ManagedObject}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	public void loadManagedObjectMetaData(AssetType assetType, String assetName,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Determine if already loaded
		if (this.managedObjectMetaData != null) {
			return; // already loaded
		}

		// Obtain the dependency mappings
		ManagedObjectIndex[] dependencyMappings = null;
		if (this.dependencies != null) {
			// Have dependencies so load them
			dependencyMappings = new ManagedObjectIndex[this.dependencies.length];
			for (int i = 0; i < dependencyMappings.length; i++) {
				RawBoundManagedObjectMetaData dependency = this.dependencies[i];

				// Do not map if not have dependency
				if (dependency == null) {
					continue;
				}

				// Map in the dependency
				dependencyMappings[i] = dependency.getManagedObjectIndex();
			}
		}

		// Create and specify the managed object meta-data
		this.managedObjectMetaData = this.rawMoMetaData.createManagedObjectMetaData(assetType, assetName,
				this.rawBoundMetaData, this.instanceIndex, this, dependencyMappings, this.governanceMetaData,
				assetManagerFactory, issues);
	}

	/*
	 * ============= RawBoundManagedObjectInstanceMetaData ==================
	 */

	@Override
	public RawBoundManagedObjectMetaData[] getDependencies() {
		return this.dependencies;
	}

	@Override
	public RawManagedObjectMetaData<D, ?> getRawManagedObjectMetaData() {
		return this.rawMoMetaData;
	}

	@Override
	public ManagedObjectMetaData<D> getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

}