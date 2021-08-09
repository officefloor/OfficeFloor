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

package net.officefloor.frame.impl.construct.managedfunction;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionMetaDataImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Factory to construct {@link RawManagedFunctionMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagedFunctionMetaDataFactory {

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaData rawOfficeMetaData;

	/**
	 * {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	private final RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory;

	/**
	 * Instantiate.
	 * 
	 * @param rawOfficeMetaData            {@link RawOfficeMetaData}.
	 * @param rawBoundManagedObjectFactory {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	public RawManagedFunctionMetaDataFactory(RawOfficeMetaData rawOfficeMetaData,
			RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory) {
		this.rawOfficeMetaData = rawOfficeMetaData;
		this.rawBoundManagedObjectFactory = rawBoundManagedObjectFactory;
	}

	/**
	 * Constructs the {@link RawManagedFunctionMetaData}.
	 * 
	 * @param configuration                  {@link ManagedFunctionConfiguration}.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 * @param issues                         {@link OfficeFloorIssues}.
	 * @return {@link RawManagedFunctionMetaData}.
	 */
	public RawManagedFunctionMetaData<?, ?> constructRawManagedFunctionMetaData(
			ManagedFunctionConfiguration<?, ?> configuration, AssetManagerRegistry assetManagerRegistry,
			long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Obtain the function name
		String functionName = configuration.getFunctionName();
		if (ConstructUtil.isBlank(functionName)) {
			issues.addIssue(AssetType.OFFICE, this.rawOfficeMetaData.getOfficeName(),
					ManagedFunction.class.getSimpleName() + " added without name");
			return null; // no task name
		}

		// Obtain the function factory
		ManagedFunctionFactory<?, ?> functionFactory = configuration.getManagedFunctionFactory();
		if (functionFactory == null) {
			issues.addIssue(AssetType.FUNCTION, functionName,
					"No " + ManagedFunctionFactory.class.getSimpleName() + " provided");
			return null; // no function factory
		}

		// Obtain the annotations
		Object[] annotations = configuration.getAnnotations();

		// Obtain the team responsible for the function
		TeamManagement responsibleTeam = null;
		String officeTeamName = configuration.getResponsibleTeamName();
		if (!ConstructUtil.isBlank(officeTeamName)) {
			responsibleTeam = this.rawOfficeMetaData.getTeams().get(officeTeamName);
			if (responsibleTeam == null) {
				issues.addIssue(AssetType.FUNCTION, functionName, "Unknown " + Team.class.getSimpleName() + " '"
						+ officeTeamName + "' responsible for " + ManagedFunction.class.getSimpleName());
				return null; // no team
			}
		}

		// Obtain the office scoped managed objects
		Map<String, RawBoundManagedObjectMetaData> officeScopeMo = this.rawOfficeMetaData
				.getOfficeScopeManagedObjects();

		// Obtain the function bound managed objects
		ManagedObjectConfiguration<?>[] moConfiguration = configuration.getManagedObjectConfiguration();
		RawBoundManagedObjectMetaData[] functionBoundMo;
		if ((moConfiguration == null) || (moConfiguration.length == 0)) {
			functionBoundMo = new RawBoundManagedObjectMetaData[0];
		} else {
			functionBoundMo = this.rawBoundManagedObjectFactory.constructBoundManagedObjectMetaData(moConfiguration,
					ManagedObjectScope.FUNCTION, officeScopeMo, null, null, AssetType.FUNCTION, functionName,
					defaultAsynchronousFlowTimeout, issues);
		}

		// Create the function scope managed objects
		Map<String, RawBoundManagedObjectMetaData> functionScopeMo = new HashMap<String, RawBoundManagedObjectMetaData>();
		functionScopeMo.putAll(officeScopeMo); // include all office scoped
		for (RawBoundManagedObjectMetaData mo : functionBoundMo) {
			functionScopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Keep track of all the required managed objects
		final Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects = new HashMap<ManagedObjectIndex, RawBoundManagedObjectMetaData>();

		// Obtain the managed objects used directly by this function.
		// Also obtain the parameter type for the function if specified.
		ManagedFunctionObjectConfiguration<?>[] objectConfigurations = configuration.getObjectConfiguration();
		ManagedObjectIndex[] functionIndexedManagedObjects = new ManagedObjectIndex[objectConfigurations.length];
		Class<?> parameterType = null;
		NEXT_OBJECT: for (int i = 0; i < objectConfigurations.length; i++) {
			ManagedFunctionObjectConfiguration<?> objectConfiguration = objectConfigurations[i];

			// Ensure have configuration
			if (objectConfiguration == null) {
				issues.addIssue(AssetType.FUNCTION, functionName, "No object configuration at index " + i);
				return null; // must have configuration
			}

			// Obtain the type of object required
			Class<?> objectType = objectConfiguration.getObjectType();
			if (objectType == null) {
				issues.addIssue(AssetType.FUNCTION, functionName, "No type for object at index " + i);
				return null; // must have object type
			}

			// Determine if a parameter
			if (objectConfiguration.isParameter()) {
				// Parameter so use parameter index (note has no scope)
				functionIndexedManagedObjects[i] = new ManagedObjectIndexImpl(null,
						ManagedFunctionLogicImpl.PARAMETER_INDEX);

				// Specify the parameter type
				if (parameterType == null) {
					// Specify as not yet set
					parameterType = objectType;

				} else {
					// Parameter already used, so use most specific type
					if (parameterType.isAssignableFrom(objectType)) {
						// Just linked object is more specific type
						parameterType = objectType;
					} else if (objectType.isAssignableFrom(parameterType)) {
						// Existing parameter type is more specific
					} else {
						// Parameter use is incompatible
						issues.addIssue(AssetType.FUNCTION, functionName, "Incompatible parameter types ("
								+ parameterType.getName() + ", " + objectType.getName() + ")");
						return null;
					}
				}

				// Specified as parameter
				continue NEXT_OBJECT;
			}

			// Obtain the scope managed object name
			String scopeMoName = objectConfiguration.getScopeManagedObjectName();
			if (ConstructUtil.isBlank(scopeMoName)) {
				issues.addIssue(AssetType.FUNCTION, functionName,
						"No name for " + ManagedObject.class.getSimpleName() + " at index " + i);
				return null; // no managed object name
			}

			// Obtain the scope managed object
			RawBoundManagedObjectMetaData scopeMo = functionScopeMo.get(scopeMoName);
			if (scopeMo == null) {
				issues.addIssue(AssetType.FUNCTION, functionName,
						"Can not find scope managed object '" + scopeMoName + "'");
				return null; // no scope managed object
			}

			// Ensure the objects of all the managed objects are compatible
			boolean isCompatibleIssue = false;
			for (RawBoundManagedObjectInstanceMetaData<?> scopeMoInstance : scopeMo
					.getRawBoundManagedObjectInstanceMetaData()) {
				Class<?> moObjectType = scopeMoInstance.getRawManagedObjectMetaData().getObjectType();
				if (!objectType.isAssignableFrom(moObjectType)) {
					// Incompatible managed object
					isCompatibleIssue = true;
					issues.addIssue(AssetType.FUNCTION, functionName,
							ManagedObject.class.getSimpleName() + " " + scopeMoName + " is incompatible (require="
									+ objectType.getName() + ", object of " + ManagedObject.class.getSimpleName()
									+ " type=" + moObjectType.getName() + ", "
									+ ManagedObjectSource.class.getSimpleName() + "="
									+ scopeMoInstance.getRawManagedObjectMetaData().getManagedObjectName() + ")");
					return null;
				}
			}
			if (isCompatibleIssue) {
				// Incompatible managed object
				continue NEXT_OBJECT;
			}

			// Specify index for function translated index
			functionIndexedManagedObjects[i] = RawBoundManagedObjectMetaData.loadRequiredManagedObjects(scopeMo,
					requiredManagedObjects);
		}

		// Obtain the required governance
		boolean[] requiredGovernance;
		ManagedFunctionGovernanceConfiguration[] governanceConfigurations = configuration.getGovernanceConfiguration();
		boolean isManuallyManageGovernance = this.rawOfficeMetaData.isManuallyManageGovernance();
		if (isManuallyManageGovernance) {
			// Ensure no governance is configured
			if (governanceConfigurations.length > 0) {
				issues.addIssue(AssetType.FUNCTION, functionName,
						"Manually manage " + Governance.class.getSimpleName() + " but "
								+ Governance.class.getSimpleName() + " configured for "
								+ OfficeFloor.class.getSimpleName() + " management");
				return null;
			}

			// No OfficeFloor managed governance for task
			requiredGovernance = null;

		} else {
			// OfficeFloor to manage Governance, create base flags
			Map<String, RawGovernanceMetaData<?, ?>> rawGovernances = this.rawOfficeMetaData.getGovernanceMetaData();
			requiredGovernance = new boolean[rawGovernances.size()];
			for (int i = 0; i < requiredGovernance.length; i++) {
				requiredGovernance[i] = false;
			}

			// Configure activation of appropriate governance
			for (int i = 0; i < governanceConfigurations.length; i++) {
				ManagedFunctionGovernanceConfiguration governanceConfiguration = governanceConfigurations[i];

				// Obtain the name of the governance
				String governanceName = governanceConfiguration.getGovernanceName();
				if (ConstructUtil.isBlank(governanceName)) {
					issues.addIssue(AssetType.FUNCTION, functionName, "No " + Governance.class.getSimpleName()
							+ " name provided for " + Governance.class.getSimpleName() + " " + i);
					return null;
				}

				// Obtain the raw governance meta-data
				RawGovernanceMetaData<?, ?> rawGovernance = rawGovernances.get(governanceName);
				if (rawGovernance == null) {
					issues.addIssue(AssetType.FUNCTION, functionName,
							"Unknown " + Governance.class.getSimpleName() + " '" + governanceName + "'");
					return null;
				}

				// Flag activate the particular governance
				int governanceIndex = rawGovernance.getGovernanceIndex();
				requiredGovernance[governanceIndex] = true;
			}
		}

		// Create the function bound managed object meta-data
		ManagedObjectMetaData<?>[] functionBoundMoMetaData = new ManagedObjectMetaData[functionBoundMo.length];
		for (int i = 0; i < functionBoundMoMetaData.length; i++) {
			RawBoundManagedObjectMetaData rawMoMetaData = functionBoundMo[i];

			// Obtain the default managed object instance meta-data
			int defaultInstanceIndex = rawMoMetaData.getDefaultInstanceIndex();
			RawBoundManagedObjectInstanceMetaData<?> moInstanceMetaData = rawMoMetaData
					.getRawBoundManagedObjectInstanceMetaData()[defaultInstanceIndex];

			// Obtain the default managed object meta-data
			functionBoundMoMetaData[i] = moInstanceMetaData.getManagedObjectMetaData();
			if (functionBoundMoMetaData[i] == null) {
				issues.addIssue(AssetType.FUNCTION, functionName,
						"No managed object meta-data for function managed object "
								+ rawMoMetaData.getBoundManagedObjectName());
				return null;
			}
		}

		// Obtain the asynchronous flow timeout
		long asynchronousFlowTimeout = configuration.getAsynchronousFlowTimeout();
		if (asynchronousFlowTimeout <= 0) {
			asynchronousFlowTimeout = defaultAsynchronousFlowTimeout;
		}

		// Create the asset manager for asynchronous flows
		AssetManagerReference asynchronousFlowsAssetManagerReference = assetManagerRegistry
				.createAssetManager(AssetType.FUNCTION, functionName, AsynchronousFlow.class.getSimpleName(), issues);

		// Create the logger
		Logger logger = OfficeFrame.getLogger(functionName);

		// Create the function meta-data
		ManagedFunctionMetaDataImpl<?, ?> functionMetaData = new ManagedFunctionMetaDataImpl<>(functionName,
				functionFactory, annotations, parameterType, responsibleTeam, functionIndexedManagedObjects,
				functionBoundMoMetaData, requiredGovernance, asynchronousFlowTimeout,
				asynchronousFlowsAssetManagerReference, logger);

		// Return the raw function meta-data
		@SuppressWarnings({ "rawtypes", "unchecked" })
		RawManagedFunctionMetaData rawFunctionMetaData = new RawManagedFunctionMetaData(functionName, configuration,
				functionScopeMo, requiredManagedObjects, functionMetaData);
		return rawFunctionMetaData;
	}

}
