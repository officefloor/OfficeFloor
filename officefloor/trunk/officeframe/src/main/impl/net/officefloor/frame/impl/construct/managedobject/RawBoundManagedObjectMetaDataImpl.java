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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.OfficeFloorIssues;
import net.officefloor.frame.api.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;

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

		// Load dependencies of bound managed objects
		for (RawBoundManagedObjectMetaDataImpl<?> moMetaData : boundMoList) {

			// Create the mapping of scope managed objects
			Map<String, RawBoundManagedObjectMetaData<?>> dependencyMo = new HashMap<String, RawBoundManagedObjectMetaData<?>>();
			dependencyMo.putAll(scopeManagedObjects); // scope first
			dependencyMo.putAll(boundMo); // bound possibly overwrite scope

			// Load the dependencies
			this.loadDependencies(moMetaData, issues, assetType, assetName,
					dependencyMo);
		}

		// Return the bound managed object meta-data
		return boundMoList.toArray(new RawBoundManagedObjectMetaDataImpl[0]);
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
		ManagedObjectDependencyConfiguration<d>[] dependencyMappings = moMetaData
				.getManagedObjectConfiguration().getDependencyConfiguration();
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
	public ManagedObjectConfiguration<D> getManagedObjectConfiguration() {
		return this.managedObjectConfiguration;
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
	public ManagedObjectMetaData<?> getManagedObjectMetaData() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement RawBoundManagedObjectMetaData<D>.getManagedObjectMetaData");
	}

}