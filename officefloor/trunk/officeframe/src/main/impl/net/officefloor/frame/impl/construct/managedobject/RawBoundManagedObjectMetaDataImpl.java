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
package net.officefloor.frame.impl.construct.managedobject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeManagingManagedObjectMetaData;
import net.officefloor.frame.internal.construct.TaskMetaDataLocator;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Raw meta-data for a bound {@link ManagedObject}.
 * 
 * @author Daniel
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
	 * Dependency keys.
	 */
	@SuppressWarnings("unchecked")
	private D[] dependencyKeys = (D[]) new Enum[0];

	/**
	 * Dependencies.
	 */
	private final Map<D, RawBoundManagedObjectMetaData<?>> dependencies = new HashMap<D, RawBoundManagedObjectMetaData<?>>();

	/**
	 * {@link ManagedObjectMetaData} of this {@link RawManagedObjectMetaData}.
	 */
	private ManagedObjectMetaDataImpl<D> managedObjectMetaData;

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
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeManagedObjects) {

		// Create copy of scope bound managed objects
		Map<String, RawBoundManagedObjectMetaData<?>> boundMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
		if (scopeManagedObjects != null) {
			boundMo.putAll(scopeManagedObjects);
		}

		// Obtain the bound managed object instances
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
			this.loadDependencies(moMetaData, issues, assetType, assetName,
					dependencyMo);

			// Load the meta-data
			moMetaData.loadManagedObjectMetaData();
		}

		// Return the bound managed object meta-data
		return boundMoList.toArray(new RawBoundManagedObjectMetaData[0]);
	}

	@Override
	public RawBoundManagedObjectMetaData<?>[] affixOfficeManagingManagedObjects(
			String officeName,
			RawBoundManagedObjectMetaData<?>[] processBoundManagedObjectMetaData,
			RawOfficeManagingManagedObjectMetaData[] officeManagingManagedObjects,
			OfficeFloorIssues issues) {

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
						.addIssue(AssetType.OFFICE, officeName,
								"Attempting to add Office managing Managed Objects to non-process scope");
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
		for (RawOfficeManagingManagedObjectMetaData officeMo : officeManagingManagedObjects) {

			// Obtain the process bound name
			String processBoundName = officeMo.getProcessBoundName();
			if (ConstructUtil.isBlank(processBoundName)) {
				issues
						.addIssue(
								AssetType.OFFICE,
								officeName,
								"Must provide process bound name for Managed Object being managed by the Office");
				continue; // Must have process bound name
			}

			// Obtain the raw managed object meta-data
			RawManagedObjectMetaData<?, ?> rawMoMetaData = officeMo
					.getRawManagedObjectMetaData();
			if (rawMoMetaData == null) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Must provide raw managed object meta-data for office managed object "
								+ processBoundName);
				continue; // Must have raw managed object meta-data
			}

			// Determine if managed by the office already
			RawBoundManagedObjectMetaData<?> rawBoundMo = processBoundMos
					.get(processBoundName);
			if (rawBoundMo != null) {
				// Already bound, so confirm the same managed object
				if (rawMoMetaData != rawBoundMo.getRawManagedObjectMetaData()) {
					issues
							.addIssue(
									AssetType.OFFICE,
									officeName,
									"Managed Object "
											+ processBoundName
											+ " is being managed by Office by Process bound Managed Object by name is different");
				}
			} else {
				// Not bound, so create bound managed object meta-data
				ManagedObjectIndex moIndex = new ManagedObjectIndexImpl(
						ManagedObjectScope.PROCESS, nextProcessBoundIndex++);
				RawBoundManagedObjectMetaDataImpl<?> rawBoundMoImpl = this
						.newRawBoundManagedObjectMetaDataImpl(processBoundName,
								moIndex, null, rawMoMetaData);

				// Ensure the managed object meta-data is loaded
				rawBoundMoImpl.loadManagedObjectMetaData();

				// Append to listing and map to make next office aware
				rawBoundMo = rawBoundMoImpl;
				managedObjects.add(rawBoundMo);
				processBoundMos.put(processBoundName, rawBoundMo);
			}
		}

		// Return the new listing of raw bound managed object meta-data
		return managedObjects.toArray(new RawBoundManagedObjectMetaData[0]);
	}

	/**
	 * Specifies the dependency keys.
	 * 
	 * @param dependencyKeyClass
	 *            Class specifying the dependency keys.
	 */
	private void setDependencyKeys(Class<D> dependencyKeyClass) {
		// Specify the dependencies in ordinal order
		this.dependencyKeys = dependencyKeyClass.getEnumConstants();
		Arrays.sort(this.dependencyKeys, new Comparator<D>() {
			@Override
			public int compare(D a, D b) {
				return a.ordinal() - b.ordinal();
			}
		});
	}

	/**
	 * Maps in the dependency.
	 * 
	 * @param dependencyKey
	 *            Dependency key.
	 * @param dependentManagedObject
	 *            {@link RawBoundManagedObjectMetaData} of the dependent
	 *            {@link ManagedObject}.
	 */
	private void mapDependency(D dependencyKey,
			RawBoundManagedObjectMetaData<?> dependentManagedObject) {
		this.dependencies.put(dependencyKey, dependentManagedObject);
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
			OfficeFloorIssues issues, AssetType assetType, String assetName,
			Map<String, RawBoundManagedObjectMetaData<?>> boundMo) {

		// Determine if dependencies for managed object
		Class<d> dependencyClass = moMetaData.getRawManagedObjectMetaData()
				.getManagedObjectSourceMetaData().getDependencyKeys();
		if (dependencyClass == null) {
			return; // no dependencies for managed object
		}

		// Specify the dependencies
		moMetaData.setDependencyKeys(dependencyClass);

		// Create the dependency mappings
		ManagedObjectDependencyConfiguration<d>[] dependencyMappings = moMetaData.managedObjectConfiguration
				.getDependencyConfiguration();
		Map<d, ManagedObjectDependencyConfiguration<d>> dependencies = new HashMap<d, ManagedObjectDependencyConfiguration<d>>();
		for (ManagedObjectDependencyConfiguration<d> dependencyMapping : dependencyMappings) {
			dependencies.put(dependencyMapping.getDependencyKey(),
					dependencyMapping);
		}

		// Load the dependencies
		for (d dependencyKey : dependencyClass.getEnumConstants()) {

			// Obtain the dependency mapping for the key
			ManagedObjectDependencyConfiguration<d> dependencyMapping = dependencies
					.get(dependencyKey);
			if (dependencyMapping == null) {
				issues.addIssue(assetType, assetName,
						"No mapping for dependency key '" + dependencyKey
								+ "' of managed object "
								+ moMetaData.boundManagedObjectName);
				return; // no dependency mapping
			}

			// Obtain the dependent managed object
			String dependentMoName = dependencyMapping
					.getScopeManagedObjectName();
			if (ConstructUtil.isBlank(dependentMoName)) {
				issues.addIssue(assetType, assetName,
						"No dependency name specified for dependency key '"
								+ dependencyKey + "' of managed object "
								+ moMetaData.boundManagedObjectName);
				return; // no dependency specified
			}
			RawBoundManagedObjectMetaData<?> dependencyMetaData = boundMo
					.get(dependentMoName);
			if (dependencyMetaData == null) {
				issues.addIssue(assetType, assetName,
						"No dependent managed object by name '"
								+ dependentMoName + "'");
				return; // no dependency
			}

			// Load the dependency
			moMetaData.mapDependency(dependencyKey, dependencyMetaData);
		}
	}

	/**
	 * Loads the {@link ManagedObjectMetaData} for the
	 * {@link RawBoundManagedObjectMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private void loadManagedObjectMetaData() {

		// Determine if already loaded
		if (this.managedObjectMetaData != null) {
			return; // already loaded
		}

		// Obtain the details of the managed object
		ManagedObjectSource<D, ?> managedObjectSource = this.rawMoMetaData
				.getManagedObjectSource();
		ManagedObjectPool managedObjectPool = this.rawMoMetaData
				.getManagedObjectPool();
		AssetManager sourcingAssetManager = this.rawMoMetaData
				.getSourcingAssetManager();
		AssetManager operationsAssetManager = this.rawMoMetaData
				.getOperationsAssetManager();
		boolean isManagedObjectAsynchronous = this.rawMoMetaData
				.isAsynchronous();
		boolean isManagedObjectCoordinating = this.rawMoMetaData
				.isCoordinating();
		long timeout = this.rawMoMetaData.getDefaultTimeout();

		// Obtain the dependency mapping
		Map<D, ManagedObjectIndex> dependencyMapping = null;
		for (D dependencyKey : this.dependencyKeys) {

			// Lazy create the dependency mapping based on key type
			if (dependencyMapping == null) {
				Class<D> dependencyKeyClass = (Class<D>) dependencyKey
						.getClass();
				dependencyMapping = new EnumMap<D, ManagedObjectIndex>(
						dependencyKeyClass);
			}

			// Load the dependency
			RawBoundManagedObjectMetaData<?> dependency = this.dependencies
					.get(dependencyKey);
			dependencyMapping.put(dependencyKey, dependency
					.getManagedObjectIndex());
		}
		if (dependencyMapping == null) {
			// Ensure have a dependency map
			dependencyMapping = Collections.emptyMap();
		}

		// Create and specify the managed object meta-data
		this.managedObjectMetaData = new ManagedObjectMetaDataImpl<D>(
				this.boundManagedObjectName, managedObjectSource,
				managedObjectPool, sourcingAssetManager,
				isManagedObjectAsynchronous, operationsAssetManager,
				isManagedObjectCoordinating, dependencyMapping, timeout);
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
	public D[] getDependencyKeys() {
		return this.dependencyKeys;
	}

	@Override
	public RawBoundManagedObjectMetaData<?> getDependency(D dependencyKey) {
		return this.dependencies.get(dependencyKey);
	}

	@Override
	public ManagedObjectMetaData<D> getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public void linkTasks(TaskMetaDataLocator taskMetaDataLocator,
			OfficeFloorIssues issues) {

		// Obtain the office meta-data
		OfficeMetaData officeMetaData = taskMetaDataLocator.getOfficeMetaData();

		// TODO Obtain the recycle task meta-data
		FlowMetaData<?> recycleFlowMetaData = null;

		// Load the remaining state of the managed object meta-data
		this.managedObjectMetaData.loadRemainingState(officeMetaData,
				recycleFlowMetaData);
	}

}