/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.construct.managedobject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ThreadLocalConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Factory for the creation of {@link RawBoundManagedObjectMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawBoundManagedObjectMetaDataFactory {

	/**
	 * {@link AssetManagerRegistry}.
	 */
	private final AssetManagerRegistry assetManagerRegistry;

	/**
	 * Registered {@link ManagedObject} instances that may be selected for being
	 * bound.
	 */
	private final Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects;

	/**
	 * {@link RawGovernanceMetaData} by its {@link Office} registered name.
	 */
	private final Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param assetManagerRegistry     {@link AssetManagerRegistry}.
	 * @param registeredManagedObjects Registered {@link ManagedObject} instances
	 *                                 that may be selected for being bound.
	 * @param rawGovernanceMetaData    {@link RawGovernanceMetaData} by its
	 *                                 {@link Office} registered name.
	 */
	public RawBoundManagedObjectMetaDataFactory(AssetManagerRegistry assetManagerRegistry,
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects,
			Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData) {
		this.assetManagerRegistry = assetManagerRegistry;
		this.registeredManagedObjects = registeredManagedObjects;
		this.rawGovernanceMetaData = rawGovernanceMetaData;
	}

	/**
	 * Constructs the {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param boundManagedObjectConfiguration {@link ManagedObjectConfiguration} of
	 *                                        the
	 *                                        {@link RawBoundManagedObjectMetaData}
	 *                                        instances.
	 * @param scope                           {@link ManagedObjectScope} for the
	 *                                        {@link RawBoundManagedObjectMetaData}.
	 * @param scopeManagedObjects             Already bound {@link ManagedObject}
	 *                                        instances that may fulfill
	 *                                        dependencies of bound
	 *                                        {@link ManagedObject} instances.
	 * @param inputManagedObjects             Meta-data about input
	 *                                        {@link ManagedObject} instances by
	 *                                        {@link ManagedObjectSource} instances.
	 * @param boundInputManagedObjects        Default {@link ManagedObjectSource}
	 *                                        names for multiple input
	 *                                        {@link ManagedObject} instances bound
	 *                                        to the same name. Mapping is of input
	 *                                        {@link ManagedObject} name to the
	 *                                        default {@link ManagedObjectSource}
	 *                                        name.
	 * @param assetType                       {@link AssetType} that
	 *                                        {@link ManagedObject} instances are
	 *                                        being bound.
	 * @param assetName                       Name of the {@link Asset} that
	 *                                        {@link ManagedObject} instances are
	 *                                        being bound.
	 * @param defaultAsynchronousFlowTimeout  Default {@link AsynchronousFlow}
	 *                                        timeout.
	 * @param issues                          {@link OfficeFloorIssues}.
	 * @return {@link RawBoundManagedObjectMetaData} instances for the bound
	 *         {@link ManagedObject} instances.
	 */
	public RawBoundManagedObjectMetaData[] constructBoundManagedObjectMetaData(
			ManagedObjectConfiguration<?>[] boundManagedObjectConfiguration, ManagedObjectScope scope,
			Map<String, RawBoundManagedObjectMetaData> scopeManagedObjects,
			RawManagingOfficeMetaData<?>[] inputManagedObjects, Map<String, String> boundInputManagedObjects,
			AssetType assetType, String assetName, long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Handle if null scope managed objects
		if (scopeManagedObjects == null) {
			scopeManagedObjects = Collections.emptyMap();
		}

		// Create details for obtaining the managed object instances
		Map<String, RawBoundManagedObjectMetaData> boundMo = new HashMap<String, RawBoundManagedObjectMetaData>();
		List<RawBoundManagedObjectMetaData> boundMoList = new LinkedList<RawBoundManagedObjectMetaData>();

		// Obtain the bound managed object instances
		if (boundManagedObjectConfiguration != null) {
			NEXT_MO: for (ManagedObjectConfiguration<?> mo : boundManagedObjectConfiguration) {

				// Obtain the bound managed object name
				String boundMoName = mo.getBoundManagedObjectName();
				if (ConstructUtil.isBlank(boundMoName)) {
					issues.addIssue(assetType, assetName, "No bound name for managed object");
					continue NEXT_MO; // no bound managed object
				}

				// Ensure no name clash with another bound ManagedObject
				if (boundMo.containsKey(boundMoName)) {
					issues.addIssue(AssetType.MANAGED_OBJECT, boundMoName,
							"Name clash between bound Managed Objects (name=" + boundMoName + ")");
					continue NEXT_MO; // name clash
				}

				// Obtain the registered office managed object name
				String officeMoName = mo.getOfficeManagedObjectName();
				if (ConstructUtil.isBlank(officeMoName)) {
					issues.addIssue(assetType, assetName,
							"No office name for bound managed object of name '" + boundMoName + "'");
					continue NEXT_MO; // no managed object
				}

				// Obtain the raw managed object meta-data
				RawManagedObjectMetaData<?, ?> rawMoMetaData = (RawManagedObjectMetaData<?, ?>) registeredManagedObjects
						.get(officeMoName);
				if (rawMoMetaData == null) {
					issues.addIssue(assetType, assetName,
							"No managed object by name '" + officeMoName + "' registered with the Office");
					continue NEXT_MO; // no managed object
				}

				// Obtain the dependencies configuration
				ManagedObjectDependencyConfiguration<?>[] dependenciesConfiguration = mo.getDependencyConfiguration();

				// Obtain the governance configuration
				ManagedObjectGovernanceConfiguration[] governanceConfiguration = mo.getGovernanceConfiguration();

				// Obtain the pre-load administration configuration
				AdministrationConfiguration<?, ?, ?>[] preloadAdministrationConfiguration = mo
						.getPreLoadAdministration();

				// Obtain the thread local configuration
				ThreadLocalConfiguration threadLocalConfiguration = mo.getThreadLocalConfiguration();

				// Create the bound ManagedObject meta-data (with instance)
				RawBoundManagedObjectMetaData rawBoundMoMetaData = new RawBoundManagedObjectMetaData(boundMoName, false,
						threadLocalConfiguration);
				rawBoundMoMetaData.addInstance(boundMoName, rawMoMetaData, dependenciesConfiguration,
						governanceConfiguration, preloadAdministrationConfiguration);

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
				String boundMoName = inputConfiguration.getBoundManagedObjectName();
				if (ConstructUtil.isBlank(boundMoName)) {
					issues.addIssue(assetType, assetName, "No bound name for input managed object");
					continue NEXT_MO; // no bound managed object
				}

				// Ensure no name clash with bound ManagedObject
				RawBoundManagedObjectMetaData possibleClash = boundMo.get(boundMoName);
				if ((possibleClash != null) && (!possibleClash.isInput())) {

					// Only clash if not same managed object
					RawManagedObjectMetaData<?, ?> inputRawMetaData = inputManagedObject.getRawManagedObjectMetaData();
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
							"Name clash between bound and input Managed Objects (name=" + boundMoName + ")");
					continue NEXT_MO; // name clash
				}

				// Obtain the input ManagedObject meta-data
				RawManagedObjectMetaData<?, ?> rawMoMetaData = inputManagedObject.getRawManagedObjectMetaData();

				// Obtain the dependencies configuration
				ManagedObjectDependencyConfiguration<?>[] dependenciesConfiguration = inputConfiguration
						.getDependencyConfiguration();

				// Obtain the governance configuration
				ManagedObjectGovernanceConfiguration[] governanceConfiguration = inputConfiguration
						.getGovernanceConfiguration();

				// Obtain the pre-load administration configuration
				AdministrationConfiguration<?, ?, ?>[] preloadAdministration = inputConfiguration
						.getPreLoadAdministration();

				// Obtain the thread local configuration
				ThreadLocalConfiguration threadLocalConfiguration = inputConfiguration.getThreadLocalConfiguration();

				// Obtain the bound ManagedObject meta-data
				RawBoundManagedObjectMetaData rawBoundMoMetaData;
				if (possibleClash != null) {
					// Inputs bound to same name
					rawBoundMoMetaData = possibleClash;
				} else {
					// Create the bound ManagedObject meta-data
					rawBoundMoMetaData = new RawBoundManagedObjectMetaData(boundMoName, true, threadLocalConfiguration);

					// Register the input managed object
					boundMo.put(boundMoName, rawBoundMoMetaData);
					boundMoList.add(rawBoundMoMetaData);
				}

				// Add the Input ManagedObject instance
				rawBoundMoMetaData.addInstance(boundMoName, rawMoMetaData, dependenciesConfiguration,
						governanceConfiguration, preloadAdministration);
			}
		}

		// Load dependencies
		Set<String> invalidBoundMoNames = new HashSet<String>();
		NEXT_MO: for (RawBoundManagedObjectMetaData moMetaData : boundMoList) {

			// If only one instance than is the default instance
			if (moMetaData.instancesMetaData.size() == 1) {
				// Only the single instance
				moMetaData.defaultInstanceIndex = 0;
			} else {
				// Multiple instances, obtain default managed object source name
				String boundMoName = moMetaData.getBoundManagedObjectName();
				String defaultManagedObjectSourceName = null;
				if (boundInputManagedObjects != null) {
					defaultManagedObjectSourceName = boundInputManagedObjects.get(boundMoName);
				}
				if (ConstructUtil.isBlank(defaultManagedObjectSourceName)) {
					// Must have the name of the bound Managed Object Source
					issues.addIssue(AssetType.MANAGED_OBJECT, boundMoName,
							"Bound Managed Object Source must be specified for Input Managed Object '" + boundMoName
									+ "'");
					invalidBoundMoNames.add(moMetaData.getBoundManagedObjectName());
					continue NEXT_MO; // must have bound name

				} else {
					// Search for the instance containing managed object source
					NEXT_MOS: for (int i = 0; i < moMetaData.instancesMetaData.size(); i++) {
						RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = moMetaData.instancesMetaData.get(i);

						// Determine if instance
						String managedObjectSourceName = instanceMetaData.getRawManagedObjectMetaData()
								.getManagedObjectName();
						if (defaultManagedObjectSourceName.equals(managedObjectSourceName)) {
							// Have the instance, so specify it as default
							moMetaData.defaultInstanceIndex = i;
							break NEXT_MOS; // default instance found
						}
					}
					if (moMetaData.defaultInstanceIndex < 0) {
						issues.addIssue(AssetType.MANAGED_OBJECT, boundMoName,
								"Managed Object Source '" + defaultManagedObjectSourceName
										+ "' not linked to Input Managed Object '" + boundMoName
										+ "' for being the bound instance");
						invalidBoundMoNames.add(moMetaData.getBoundManagedObjectName());
						continue NEXT_MO; // must have bound instance index
					}
				}
			}

			// Load dependencies for each instance
			for (RawBoundManagedObjectInstanceMetaData<?> instanceMetaData : moMetaData.instancesMetaData) {

				// Create the mapping of scope managed objects
				Map<String, RawBoundManagedObjectMetaData> dependencyMo = new HashMap<String, RawBoundManagedObjectMetaData>();
				dependencyMo.putAll(scopeManagedObjects); // scope first
				dependencyMo.putAll(boundMo); // bound possibly overwrite scope

				// Load the dependencies
				instanceMetaData.loadDependencies(issues, dependencyMo);
			}
		}

		// Sort the managed objects for clean up (ie dependents first)
		try {
			Collections.sort(boundMoList, new Comparator<RawBoundManagedObjectMetaData>() {
				@Override
				public int compare(RawBoundManagedObjectMetaData a, RawBoundManagedObjectMetaData b) {

					// Obtain the names
					String aName = a.getBoundManagedObjectName();
					String bName = b.getBoundManagedObjectName();

					// Obtain the dependencies
					Set<String> aDependencies = this.getDependencyManagedObjectNames(a);
					Set<String> bDependencies = this.getDependencyManagedObjectNames(b);

					// Order based on dependencies for cleanup
					boolean isAdepB = aDependencies.contains(bName);
					boolean isBdepA = bDependencies.contains(aName);

					// Determine order (if dependency relationship)
					if (isAdepB && isBdepA) {
						// Cyclic dependency
						throw new CyclicDependencyException("Cyclic dependency between bound "
								+ ManagedObject.class.getSimpleName() + "s (" + aName + ", " + bName + ")");

					} else if (isAdepB) {
						// A needs to be cleaned up before B
						return -1;
					} else if (isBdepA) {
						// B needs to be cleaned up before A
						return 1;
					}

					/*
					 * No dependency relationship. As the sorting only changes on differences (non 0
					 * value) then need means to differentiate when no dependency relationship. This
					 * is especially the case with the merge sort used by default by Java.
					 */

					// Most number of dependencies first.
					// Note: this pushes no dependencies to end.
					int value = bDependencies.size() - aDependencies.size();
					if (value == 0) {
						// Same dependencies, so use name
						value = String.CASE_INSENSITIVE_ORDER.compare(aName, bName);
					}
					return value;
				}

				/**
				 * Obtains the names of the dependencies.
				 * 
				 * @param mo {@link RawBoundManagedObjectMetaData}.
				 * @return Names of the dependencies.
				 */
				private Set<String> getDependencyManagedObjectNames(RawBoundManagedObjectMetaData mo) {

					// Load all dependencies for the managed object
					Set<String> dependencies = new HashSet<String>();
					for (RawBoundManagedObjectInstanceMetaData<?> instanceMetaData : mo.instancesMetaData) {
						RawBoundManagedObjectMetaData[] dependenciesMetaData = instanceMetaData.getDependencies();
						if (dependenciesMetaData != null) {
							for (RawBoundManagedObjectMetaData dependency : dependenciesMetaData) {
								dependencies.add(dependency.getBoundManagedObjectName());
							}
						}
					}

					// Return the dependencies
					return dependencies;
				}
			});
		} catch (CyclicDependencyException ex) {
			// Provide issue of cyclic dependency
			issues.addIssue(assetType, assetName, ex.getMessage());
		}

		// Load the indexes for the bound managed objects (as ordered)
		int boundMoIndex = 0;
		for (RawBoundManagedObjectMetaData rawBoundManagedObject : boundMoList) {
			rawBoundManagedObject.setManagedObjectIndex(scope, boundMoIndex++);
		}

		// Load meta-data for the dependencies
		NEXT_MO_META_DATA: for (RawBoundManagedObjectMetaData moMetaData : boundMoList) {

			// Only load meta-data for valid managed object meta data
			if (invalidBoundMoNames.contains(moMetaData.getBoundManagedObjectName())) {
				continue NEXT_MO_META_DATA; // ignore
			}

			// Load meta-data for each instance
			for (RawBoundManagedObjectInstanceMetaData<?> instanceMetaData : moMetaData.instancesMetaData) {

				// Load the governance
				instanceMetaData.loadGovernance(this.rawGovernanceMetaData, issues);

				// Load the meta-data
				instanceMetaData.loadManagedObjectMetaData(assetType, assetName, this.assetManagerRegistry,
						defaultAsynchronousFlowTimeout, issues);
			}
		}

		// Return the bound managed object meta-data
		return boundMoList.toArray(new RawBoundManagedObjectMetaData[0]);
	}

	/**
	 * Thrown to indicate a cyclic dependency.
	 */
	private static class CyclicDependencyException extends RuntimeException {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Initiate.
		 * 
		 * @param message Initiate with description for {@link OfficeFloorIssues}.
		 */
		public CyclicDependencyException(String message) {
			super(message);
		}
	}

}
