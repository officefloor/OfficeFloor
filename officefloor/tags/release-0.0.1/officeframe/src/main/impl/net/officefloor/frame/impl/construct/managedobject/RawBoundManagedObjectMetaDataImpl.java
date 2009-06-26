/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;

/**
 * Raw meta-data for a bound {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectMetaDataImpl<D extends Enum<D>> implements
		RawBoundManagedObjectMetaDataFactory, RawBoundManagedObjectMetaData<D> {

	/**
	 * Obtains the {@link RawBoundManagedObjectMetaDataFactory}.
	 * 
	 * @return {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	@SuppressWarnings("unchecked")
	public static RawBoundManagedObjectMetaDataFactory getFactory() {
		return new RawBoundManagedObjectMetaDataImpl(null, null, null, null);
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
	 * {@link ManagedObjectConfiguration}.
	 */
	private final ManagedObjectConfiguration<D> managedObjectConfiguration;

	/**
	 * {@link RawManagedObjectMetaData}.
	 */
	private final RawManagedObjectMetaData<D, ?> rawMoMetaData;

	/**
	 * Dependencies.
	 */
	private RawBoundManagedObjectMetaData<?>[] dependencies;

	/**
	 * {@link ManagedObjectMetaData}.
	 */
	private ManagedObjectMetaData<D> managedObjectMetaData;

	/**
	 * Initiate.
	 * 
	 * @param boundManagedObjectName
	 *            Name that the {@link ManagedObject} is bound under.
	 * @param index
	 *            {@link ManagedObjectIndex}.
	 * @param managedObjectConfiguration
	 *            {@link ManagedObjectConfiguration}.
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 */
	private RawBoundManagedObjectMetaDataImpl(String boundManagedObjectName,
			ManagedObjectIndex index,
			ManagedObjectConfiguration<D> managedObjectConfiguration,
			RawManagedObjectMetaData<D, ?> rawMoMetaData) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.index = index;
		this.managedObjectConfiguration = managedObjectConfiguration;
		this.rawMoMetaData = rawMoMetaData;
	}

	/*
	 * ============== RawBoundManagedObjectMetaDataFactory ====================
	 */

	@Override
	public RawBoundManagedObjectMetaData<?>[] constructBoundManagedObjectMetaData(
			ManagedObjectConfiguration<?>[] boundManagedObjectConfiguration,
			OfficeFloorIssues issues,
			ManagedObjectScope scope,
			AssetType assetType,
			String assetName,
			AssetManagerFactory assetManagerFactory,
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeManagedObjects) {

		// Handle if null scope managed objects
		if (scopeManagedObjects == null) {
			scopeManagedObjects = Collections.emptyMap();
		}

		// Obtain the bound managed object instances
		Map<String, RawBoundManagedObjectMetaData<?>> boundMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		List<RawBoundManagedObjectMetaDataImpl<?>> boundMoList = new LinkedList<RawBoundManagedObjectMetaDataImpl<?>>();
		int boundMoIndex = 0;
		for (ManagedObjectConfiguration<?> mo : boundManagedObjectConfiguration) {

			// Obtain the bound managed object name
			String boundMoName = mo.getBoundManagedObjectName();
			if (ConstructUtil.isBlank(boundMoName)) {
				issues.addIssue(assetType, assetName,
						"No bound name for managed object");
				continue; // no bound managed object
			}

			// Obtain the registered office managed object name
			String officeMoName = mo.getOfficeManagedObjectName();
			if (ConstructUtil.isBlank(officeMoName)) {
				issues.addIssue(assetType, assetName,
						"No office name for bound managed object of name '"
								+ boundMoName + "'");
				continue; // no managed object
			}

			// Obtain the raw managed object meta-data
			RawManagedObjectMetaData<?, ?> rawMoMetaData = registeredManagedObjects
					.get(officeMoName);
			if (rawMoMetaData == null) {
				issues.addIssue(assetType, assetName,
						"No managed object by name '" + officeMoName
								+ "' registered with the Office");
				continue; // no managed object
			}

			// Create the index of this managed object
			ManagedObjectIndex index = new ManagedObjectIndexImpl(scope,
					boundMoIndex++);

			// Register the bound managed object
			RawBoundManagedObjectMetaDataImpl<?> rawBoundMoMetaData = this
					.newRawBoundManagedObjectMetaDataImpl(boundMoName, index,
							mo, rawMoMetaData);
			boundMo.put(boundMoName, rawBoundMoMetaData);
			boundMoList.add(rawBoundMoMetaData);
		}

		// Load dependencies and meta-data of bound managed objects
		for (RawBoundManagedObjectMetaDataImpl<?> moMetaData : boundMoList) {

			// Create the mapping of scope managed objects
			Map<String, RawBoundManagedObjectMetaData<?>> dependencyMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
			dependencyMo.putAll(scopeManagedObjects); // scope first
			dependencyMo.putAll(boundMo); // bound possibly overwrite scope

			// Load the dependencies
			this.loadDependencies(moMetaData, issues, dependencyMo);

			// Load the meta-data
			moMetaData.loadManagedObjectMetaData(assetManagerFactory, issues);
		}

		// Return the bound managed object meta-data
		return boundMoList.toArray(new RawBoundManagedObjectMetaData[0]);
	}

	@Override
	public RawBoundManagedObjectMetaData<?>[] affixOfficeManagingManagedObjects(
			String officeName,
			RawBoundManagedObjectMetaData<?>[] processBoundManagedObjectMetaData,
			RawManagingOfficeMetaData<?>[] officeManagingManagedObjects,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Create the map of process bound managed objects.
		// At same time also determine the maximum process bound index.
		Map<String, RawBoundManagedObjectMetaData<?>> processBoundMos = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		int nextProcessBoundIndex = 0;
		for (RawBoundManagedObjectMetaData<?> processBoundMo : processBoundManagedObjectMetaData) {

			// Register the process bound managed object
			processBoundMos.put(processBoundMo.getBoundManagedObjectName(),
					processBoundMo);

			// Determine if update the index
			ManagedObjectIndex index = processBoundMo.getManagedObjectIndex();
			if (!ManagedObjectScope.PROCESS.equals(index
					.getManagedObjectScope())) {
				issues
						.addIssue(
								AssetType.OFFICE,
								officeName,
								"Attempting to affix managed objects to listing of managed objects that are not all process bound");
				return processBoundManagedObjectMetaData; // only process bound
			}
			int moIndex = index.getIndexOfManagedObjectWithinScope();
			if (moIndex >= nextProcessBoundIndex) {
				nextProcessBoundIndex = moIndex + 1; // +1 as next index
			}
		}

		// Create the list to append additional managed objects
		List<RawBoundManagedObjectMetaData<?>> managedObjects = new LinkedList<RawBoundManagedObjectMetaData<?>>();
		managedObjects.addAll(Arrays.asList(processBoundManagedObjectMetaData));

		// Append additional process bound managed objects
		for (RawManagingOfficeMetaData<?> officeMo : officeManagingManagedObjects) {

			// Only bind to process state if require flows
			if (!officeMo.isRequireFlows()) {
				continue; // no flows required so do not bind to process state
			}

			// Obtain the raw managed object meta-data
			RawManagedObjectMetaData<?, ?> rawMoMetaData = officeMo
					.getRawManagedObjectMetaData();
			String managedObjectSourceName = rawMoMetaData
					.getManagedObjectName();

			// Obtain the process bound name
			String processBoundName = officeMo.getProcessBoundName();
			if (ConstructUtil.isBlank(processBoundName)) {
				issues
						.addIssue(AssetType.MANAGED_OBJECT,
								managedObjectSourceName,
								"Must provide process bound name as requires managing by an office");
				continue; // Must have process bound name
			}

			// Determine if managed by the office already
			RawBoundManagedObjectMetaData<?> rawBoundMo = processBoundMos
					.get(processBoundName);
			if (rawBoundMo != null) {
				// Already bound, so confirm the same managed object
				RawManagedObjectMetaData<?, ?> rawMo = rawBoundMo
						.getRawManagedObjectMetaData();
				if (rawMoMetaData != rawMo) {
					issues
							.addIssue(
									AssetType.MANAGED_OBJECT,
									managedObjectSourceName,
									"Process bound ManagedObject "
											+ processBoundName
											+ " is different (bound managed object source="
											+ rawMo.getManagedObjectName()
											+ ")");
				}

			} else {
				// Not bound, so create process bound managed object meta-data

				// Must not have dependencies. If requires dependencies should
				// have been added to ProcessState of the Office with
				// dependencies configured.
				ManagedObjectDependencyMetaData<?>[] dependencyMetaData = rawMoMetaData
						.getManagedObjectSourceMetaData()
						.getDependencyMetaData();
				if ((dependencyMetaData != null)
						&& (dependencyMetaData.length > 0)) {
					issues
							.addIssue(
									AssetType.MANAGED_OBJECT,
									managedObjectSourceName,
									"Must map dependencies for affixed ManagedObjectSource. Please add to process and map dependencies.");
					continue; // can not have dependencies
				}

				// Create the process bound index for the managed object
				ManagedObjectIndex moIndex = new ManagedObjectIndexImpl(
						ManagedObjectScope.PROCESS, nextProcessBoundIndex++);

				// Create process bound managed object meta-data
				RawBoundManagedObjectMetaDataImpl<?> rawBoundMoImpl = this
						.newRawBoundManagedObjectMetaDataImpl(processBoundName,
								moIndex, null, rawMoMetaData);
				rawBoundMoImpl.loadManagedObjectMetaData(assetManagerFactory,
						issues);
				rawBoundMo = rawBoundMoImpl;

				// Append to listing and map to make next office aware
				managedObjects.add(rawBoundMo);
				processBoundMos.put(processBoundName, rawBoundMo);
			}
		}

		// Return the new listing of raw bound managed object meta-data
		return managedObjects.toArray(new RawBoundManagedObjectMetaData[0]);
	}

	/**
	 * Wraps construction of {@link RawBoundManagedObjectMetaDataImpl} to avoid
	 * generic type safe issues.
	 * 
	 * @param boundManagedObjectName
	 *            Bound {@link ManagedObject} name.
	 * @param index
	 *            {@link ManagedObjectIndex}.
	 * @param mo
	 *            {@link ManagedObjectConfiguration}.
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @return {@link RawBoundManagedObjectMetaDataImpl}.
	 */
	@SuppressWarnings("unchecked")
	private <d extends Enum<d>> RawBoundManagedObjectMetaDataImpl<d> newRawBoundManagedObjectMetaDataImpl(
			String boundManagedObjectName, ManagedObjectIndex index,
			ManagedObjectConfiguration<?> mo,
			RawManagedObjectMetaData<d, ?> rawMoMetaData) {
		return new RawBoundManagedObjectMetaDataImpl(boundManagedObjectName,
				index, mo, rawMoMetaData);
	}

	/**
	 * Loads the dependencies onto {@link RawBoundManagedObjectMetaDataImpl}.
	 * 
	 * @param moMetaData
	 *            {@link RawBoundManagedObjectMetaDataImpl}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of {@link Asset}.
	 * @param boundMo
	 *            Mapping of {@link RawBoundManagedObjectMetaData} by its scope
	 *            bound name.
	 */
	private <d extends Enum<d>> void loadDependencies(
			RawBoundManagedObjectMetaDataImpl<d> moMetaData,
			OfficeFloorIssues issues,
			Map<String, RawBoundManagedObjectMetaData<?>> boundMo) {

		// Obtain the dependency meta-data
		ManagedObjectDependencyMetaData<d>[] dependencyMetaDatas = moMetaData
				.getRawManagedObjectMetaData().getManagedObjectSourceMetaData()
				.getDependencyMetaData();

		// Obtain the dependency configuration
		ManagedObjectDependencyConfiguration<d>[] dependencyConfigurations = moMetaData.managedObjectConfiguration
				.getDependencyConfiguration();

		// Determine if dependencies for managed object
		if ((dependencyMetaDatas == null) || (dependencyMetaDatas.length == 0)) {

			// No dependencies but issue if dependencies configured
			if ((dependencyConfigurations != null)
					&& (dependencyConfigurations.length > 0)) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						moMetaData.boundManagedObjectName,
						"No dependencies required but dependencies configured");
			}

			// No dependencies for managed object
			moMetaData.dependencies = new RawBoundManagedObjectMetaData[0];
			return;
		}

		// Create the dependency mappings for the configuration
		Map<Integer, ManagedObjectDependencyConfiguration<d>> dependencyMappings = new HashMap<Integer, ManagedObjectDependencyConfiguration<d>>();
		for (int i = 0; i < dependencyConfigurations.length; i++) {
			ManagedObjectDependencyConfiguration<d> dependencyConfiguration = dependencyConfigurations[i];

			// Obtain the index to identify the dependency
			d dependencyKey = dependencyConfiguration.getDependencyKey();
			int index = (dependencyKey != null ? dependencyKey.ordinal() : i);

			// Load the dependency at its index
			dependencyMappings.put(new Integer(index), dependencyConfiguration);
		}

		// Load the dependencies
		Map<Integer, RawBoundManagedObjectMetaData<?>> dependencies = new HashMap<Integer, RawBoundManagedObjectMetaData<?>>();
		for (int i = 0; i < dependencyMetaDatas.length; i++) {
			ManagedObjectDependencyMetaData<d> dependencyMetaData = dependencyMetaDatas[i];

			// Obtain the index to identify the dependency
			d dependencyKey = dependencyMetaData.getKey();
			int index = (dependencyKey != null ? dependencyKey.ordinal() : i);

			// Create name to identify dependency
			String label = dependencyMetaData.getLabel();
			String dependencyLabel = "dependency "
					+ index
					+ " (key="
					+ (dependencyKey != null ? dependencyKey.toString()
							: "<indexed>") + ", label="
					+ (!ConstructUtil.isBlank(label) ? label : "<no label>")
					+ ")";

			// Obtain the mapping for the dependency
			ManagedObjectDependencyConfiguration<d> dependencyMapping = dependencyMappings
					.get(new Integer(index));
			if (dependencyMapping == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						moMetaData.boundManagedObjectName,
						"No mapping configured for " + dependencyLabel);
				return; // no dependency mapping
			}

			// Remove configuration for later check no extra configured
			dependencyMappings.remove(new Integer(index));

			// Obtain the dependent managed object
			String dependentMoName = dependencyMapping
					.getScopeManagedObjectName();
			if (ConstructUtil.isBlank(dependentMoName)) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						moMetaData.boundManagedObjectName,
						"No dependency name configured for " + dependencyLabel);
				return; // no dependency specified
			}
			RawBoundManagedObjectMetaData<?> dependency = boundMo
					.get(dependentMoName);
			if (dependency == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						moMetaData.boundManagedObjectName, "No dependent "
								+ ManagedObject.class.getSimpleName()
								+ " by name '" + dependentMoName + "' for "
								+ dependencyLabel);
				return; // no dependency
			}

			// Ensure the dependency object is of correct type
			Class<?> requiredType = dependencyMetaData.getType();
			Class<?> dependencyType = dependency.getRawManagedObjectMetaData()
					.getObjectType();
			if (!requiredType.isAssignableFrom(dependencyType)) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						moMetaData.boundManagedObjectName,
						"Incompatible dependency for " + dependencyLabel
								+ " (required type=" + requiredType.getName()
								+ ", dependency type="
								+ dependencyType.getName() + ")");
				return; // incompatible dependency
			}

			// Load the dependency
			dependencies.put(new Integer(index), dependency);
		}

		// Ensure there are no additional dependencies configured
		if (dependencyMappings.size() > 0) {
			issues
					.addIssue(AssetType.MANAGED_OBJECT,
							moMetaData.boundManagedObjectName,
							"Extra dependencies configured than required by ManagedObjectSourceMetaData");
			return; // additional dependencies configured
		}

		// Specify the dependencies on the bound managed object
		moMetaData.dependencies = ConstructUtil.toArray(dependencies,
				new RawBoundManagedObjectMetaData[0]);
	}

	/**
	 * Loads the {@link ManagedObjectMetaData} for the
	 * {@link RawBoundManagedObjectMetaData}.
	 */
	private void loadManagedObjectMetaData(
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
				RawBoundManagedObjectMetaData<?> dependency = this.dependencies[i];

				// Do not map if not have dependency
				if (dependency == null) {
					continue;
				}

				// Map in the dependency
				dependencyMappings[i] = dependency.getManagedObjectIndex();
			}
		}

		// Create and specify the managed object meta-data
		this.managedObjectMetaData = this.rawMoMetaData
				.createManagedObjectMetaData(this, dependencyMappings,
						assetManagerFactory, issues);
	}

	/*
	 * ============== RawBoundManagedObjectMetaData ====================
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
	public RawManagedObjectMetaData<D, ?> getRawManagedObjectMetaData() {
		return this.rawMoMetaData;
	}

	@Override
	public RawBoundManagedObjectMetaData<?>[] getDependencies() {
		return this.dependencies;
	}

	@Override
	public ManagedObjectMetaData<D> getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

}