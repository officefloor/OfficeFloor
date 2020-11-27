/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.managedobject;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaData;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectGovernanceMetaDataImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectStartupFunction;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * {@link RawBoundManagedObjectInstanceMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectInstanceMetaData<O extends Enum<O>> {

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
	private final RawManagedObjectMetaData<O, ?> rawMoMetaData;

	/**
	 * Listing of the {@link ManagedObjectDependencyConfiguration} for the
	 * {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	private final ManagedObjectDependencyConfiguration<?>[] dependenciesConfiguration;

	/**
	 * Listing of the {@link ManagedObjectGovernanceConfiguration} for the
	 * {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	private final ManagedObjectGovernanceConfiguration[] governanceConfiguration;

	/**
	 * Listing of the {@link AdministrationConfiguration} for the pre-load
	 * {@link RawAdministrationMetaData}.
	 */
	private final AdministrationConfiguration<?, ?, ?>[] preLoadAdministrationConfiguration;

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
	private ManagedObjectMetaDataImpl<O> managedObjectMetaData;

	/**
	 * Initiate.
	 * 
	 * @param boundManagedObjectName             Name that the {@link ManagedObject}
	 *                                           is bound under.
	 * @param rawBoundMetaData                   {@link RawBoundManagedObjectMetaData}
	 *                                           containing this
	 *                                           {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param instanceIndex                      Index of this
	 *                                           {@link RawBoundManagedObjectInstanceMetaData}
	 *                                           within its containing
	 *                                           {@link RawBoundManagedObjectMetaData}.
	 * @param rawMoMetaData                      {@link RawManagedObjectMetaData}.
	 * @param dependenciesConfiguration          Listing of the
	 *                                           {@link ManagedObjectDependencyConfiguration}
	 *                                           for the
	 *                                           {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param governanceConfiguration            Listing of the
	 *                                           {@link ManagedObjectGovernanceConfiguration}
	 *                                           for the
	 *                                           {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param preLoadAdministrationConfiguration Pre-load
	 *                                           {@link AdministrationConfiguration}.
	 */
	public RawBoundManagedObjectInstanceMetaData(String boundManagedObjectName,
			RawBoundManagedObjectMetaData rawBoundMetaData, int instanceIndex,
			RawManagedObjectMetaData<O, ?> rawMoMetaData,
			ManagedObjectDependencyConfiguration<?>[] dependenciesConfiguration,
			ManagedObjectGovernanceConfiguration[] governanceConfiguration,
			AdministrationConfiguration<?, ?, ?>[] preLoadAdministrationConfiguration) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.rawBoundMetaData = rawBoundMetaData;
		this.instanceIndex = instanceIndex;
		this.rawMoMetaData = rawMoMetaData;
		this.dependenciesConfiguration = dependenciesConfiguration;
		this.governanceConfiguration = governanceConfiguration;
		this.preLoadAdministrationConfiguration = preLoadAdministrationConfiguration;
	}

	/**
	 * Loads the dependencies.
	 * 
	 * @param issues  {@link OfficeFloorIssues}.
	 * @param boundMo Mapping of {@link RawBoundManagedObjectMetaData} by its scope
	 *                bound name.
	 */
	public void loadDependencies(OfficeFloorIssues issues, Map<String, RawBoundManagedObjectMetaData> boundMo) {

		// Obtain the dependency meta-data
		ManagedObjectDependencyMetaData<?>[] dependencyMetaDatas = this.rawMoMetaData.getManagedObjectSourceMetaData()
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
		Map<Integer, ManagedObjectDependencyConfiguration<?>> dependencyMappings = new HashMap<>();
		for (int i = 0; i < this.dependenciesConfiguration.length; i++) {
			ManagedObjectDependencyConfiguration<?> dependencyConfiguration = this.dependenciesConfiguration[i];

			// Ensure have dependency configuration
			if (dependencyConfiguration == null) {
				continue;
			}

			// Obtain the index to identify the dependency
			Enum<?> dependencyKey = dependencyConfiguration.getDependencyKey();
			int index = (dependencyKey != null ? dependencyKey.ordinal() : i);

			// Load the dependency at its index
			dependencyMappings.put(Integer.valueOf(index), dependencyConfiguration);
		}

		// Load the dependencies
		Map<Integer, RawBoundManagedObjectMetaData> dependencies = new HashMap<Integer, RawBoundManagedObjectMetaData>();
		for (int i = 0; i < dependencyMetaDatas.length; i++) {
			ManagedObjectDependencyMetaData<?> dependencyMetaData = dependencyMetaDatas[i];

			// Obtain the index to identify the dependency
			Enum<?> dependencyKey = dependencyMetaData.getKey();
			int index = (dependencyKey != null ? dependencyKey.ordinal() : i);

			// Create name to identify dependency
			String label = dependencyMetaData.getLabel();
			String dependencyLabel = "dependency " + index + " (key="
					+ (dependencyKey != null ? dependencyKey.toString() : "<indexed>") + ", label="
					+ (!ConstructUtil.isBlank(label) ? label : "<no label>") + ")";

			// Obtain the mapping for the dependency
			ManagedObjectDependencyConfiguration<?> dependencyMapping = dependencyMappings.get(Integer.valueOf(index));
			if (dependencyMapping == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
						"No mapping configured for " + dependencyLabel);
				return; // no dependency mapping
			}

			// Remove configuration for later check no extra configured
			dependencyMappings.remove(Integer.valueOf(index));

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
			dependencies.put(Integer.valueOf(index), dependency);
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
	 * @param rawGovernanceMetaDatas {@link RawGovernanceMetaData} of the
	 *                               {@link Office} by its {@link Office} registered
	 *                               name.
	 * @param issues                 {@link OfficeFloorIssues}.
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
			Class<?> governanceExtensionInterface = rawGovernanceMetaData.getExtensionType();

			// Create the extension interface extractor
			ManagedObjectExtensionMetaData<?>[] eiMetaDatas = this.rawMoMetaData.getManagedObjectSourceMetaData()
					.getExtensionInterfacesMetaData();
			for (ManagedObjectExtensionMetaData<?> eiMetaData : eiMetaDatas) {

				// Determine if extension interface to use
				Class<?> extensionInterfaceType = eiMetaData.getExtensionType();
				if (governanceExtensionInterface.isAssignableFrom(extensionInterfaceType)) {

					// Found the extension interface, so obtain the factory
					ExtensionFactory<?> factory = eiMetaData.getExtensionFactory();

					// Load the governance meta-data
					this.governanceMetaData[i] = new ManagedObjectGovernanceMetaDataImpl(governanceIndex, factory);
				}
			}

			// Ensure have the governance
			if (this.governanceMetaData[i] == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, this.boundManagedObjectName,
						"Extension of type " + governanceExtensionInterface.getName()
								+ " is not available from Managed Object for Governance '" + governanceName + "'");
			}
		}
	}

	/**
	 * Loads the {@link ManagedObjectMetaData} for the
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param assetType                      {@link AssetType} requiring the
	 *                                       {@link ManagedObject}.
	 * @param assetName                      Name of the {@link Asset} requiring the
	 *                                       {@link ManagedObject}.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 * @param issues                         {@link OfficeFloorIssues}.
	 */
	public void loadManagedObjectMetaData(AssetType assetType, String assetName,
			AssetManagerRegistry assetManagerRegistry, long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

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
				assetManagerRegistry, issues);

		// Manage by the office
		this.rawMoMetaData.getRawManagingOfficeMetaData().manageManagedObject(this, assetManagerRegistry,
				defaultAsynchronousFlowTimeout);
	}

	/**
	 * Loads the remaining state for the {@link ManagedObjectMetaData}.
	 * 
	 * @param officeMetaData                 {@link OfficeMetaData}.
	 * @param startupFunctions               {@link ManagedObjectStartupFunction}
	 *                                       instances.
	 * @param recycleFlowMetaData            Recycle {@link FlowMetaData}.
	 * @param managedObjectAdminFactory      {@link ManagedObjectAdministrationMetaDataFactory}.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 * @param issues                         {@link OfficeFloorIssues}.
	 */
	public void loadRemainingState(OfficeMetaData officeMetaData, ManagedObjectStartupFunction[] startupFunctions,
			FlowMetaData recycleFlowMetaData, ManagedObjectAdministrationMetaDataFactory managedObjectAdminFactory,
			AssetManagerRegistry assetManagerRegistry, long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Load the pre-load administration
		ManagedObjectAdministrationMetaData<?, ?, ?>[] preLoadAdmin = managedObjectAdminFactory
				.createManagedObjectAdministrationMetaData(this.boundManagedObjectName,
						this.preLoadAdministrationConfiguration, this.rawBoundMetaData, assetManagerRegistry,
						defaultAsynchronousFlowTimeout, issues);

		// Load the remaining state
		this.managedObjectMetaData.loadRemainingState(officeMetaData, startupFunctions, recycleFlowMetaData,
				preLoadAdmin);
	}

	/**
	 * Obtains the {@link RawBoundManagedObjectMetaData} instances of the
	 * dependencies of this {@link ManagedObject}.
	 *
	 * @return {@link RawBoundManagedObjectMetaData} instances of the dependencies
	 *         of this {@link ManagedObject}.
	 */
	public RawBoundManagedObjectMetaData[] getDependencies() {
		return this.dependencies;
	}

	/**
	 * Obtains the {@link RawManagedObjectMetaData}.
	 *
	 * @return {@link RawManagedObjectMetaData}.
	 */
	public RawManagedObjectMetaData<?, ?> getRawManagedObjectMetaData() {
		return this.rawMoMetaData;
	}

	/**
	 * Obtains the {@link ManagedObjectMetaData}.
	 *
	 * @return {@link ManagedObjectMetaData}.
	 */
	public ManagedObjectMetaData<?> getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

}
