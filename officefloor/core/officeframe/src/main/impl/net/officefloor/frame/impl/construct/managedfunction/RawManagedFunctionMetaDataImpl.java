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
package net.officefloor.frame.impl.construct.managedfunction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionMetaDataImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.EscalationFlowFactory;
import net.officefloor.frame.internal.construct.FlowMetaDataFactory;
import net.officefloor.frame.internal.construct.RawAdministrationMetaData;
import net.officefloor.frame.internal.construct.RawAdministrationMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawManagedFunctionMetaData;
import net.officefloor.frame.internal.construct.RawManagedFunctionMetaDataFactory;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Raw meta-data for a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagedFunctionMetaDataImpl<O extends Enum<O>, F extends Enum<F>>
		implements RawManagedFunctionMetaDataFactory, RawManagedFunctionMetaData<O, F> {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(RawManagedFunctionMetaDataImpl.class.getName());

	/**
	 * Obtains the {@link RawManagedFunctionMetaDataFactory}.
	 * 
	 * @return {@link RawManagedFunctionMetaDataFactory}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static RawManagedFunctionMetaDataFactory getFactory() {
		return new RawManagedFunctionMetaDataImpl(null, null, null, null, null);
	}

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * {@link ManagedFunctionConfiguration}.
	 */
	private final ManagedFunctionConfiguration<O, F> configuration;

	/**
	 * {@link ManagedFunction} scoped {@link RawBoundManagedObjectMetaData}.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> functionScopedManagedObjects;

	/**
	 * Required {@link RawBoundManagedObjectMetaData} for this
	 * {@link ManagedFunction}.
	 */
	private final Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects;

	/**
	 * {@link ManagedFunctionMetaDataImpl}.
	 */
	private final ManagedFunctionMetaDataImpl<O, F> functionMetaData;

	/**
	 * Initiate.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param configuration
	 *            {@link ManagedFunctionConfiguration}.
	 * @param functionScopedManagedObjects
	 *            {@link ManagedFunction} scoped
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param requiredManagedObjects
	 *            Required {@link RawBoundManagedObjectMetaData} for this
	 *            {@link ManagedFunction}.
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaDataImpl}.
	 */
	private RawManagedFunctionMetaDataImpl(String functionName, ManagedFunctionConfiguration<O, F> configuration,
			Map<String, RawBoundManagedObjectMetaData> functionScopedManagedObjects,
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects,
			ManagedFunctionMetaDataImpl<O, F> functionMetaData) {
		this.functionName = functionName;
		this.configuration = configuration;
		this.functionScopedManagedObjects = functionScopedManagedObjects;
		this.requiredManagedObjects = requiredManagedObjects;
		this.functionMetaData = functionMetaData;
	}

	/*
	 * ============ RawManagedFunctionMetaDataFactory ============
	 */

	@Override
	public RawManagedFunctionMetaData<?, ?> constructRawManagedFunctionMetaData(
			ManagedFunctionConfiguration<?, ?> configuration, RawOfficeMetaData rawOfficeMetaData,
			AssetManagerFactory assetManagerFactory, RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			OfficeFloorIssues issues) {

		// Obtain the function name
		String functionName = configuration.getFunctionName();
		if (ConstructUtil.isBlank(functionName)) {
			issues.addIssue(AssetType.OFFICE, rawOfficeMetaData.getOfficeName(),
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

		// Obtain the differentiator
		Object differentiator = configuration.getDifferentiator();

		// Obtain the team responsible for the function
		TeamManagement responsibleTeam = null;
		String officeTeamName = configuration.getResponsibleTeamName();
		if (!ConstructUtil.isBlank(officeTeamName)) {
			responsibleTeam = rawOfficeMetaData.getTeams().get(officeTeamName);
			if (responsibleTeam == null) {
				issues.addIssue(AssetType.FUNCTION, functionName, "Unknown " + Team.class.getSimpleName() + " '"
						+ officeTeamName + "' responsible for " + ManagedFunction.class.getSimpleName());
				return null; // no team
			}
		}

		// Obtain the office scoped managed objects
		Map<String, RawBoundManagedObjectMetaData> officeScopeMo = rawOfficeMetaData.getOfficeScopeManagedObjects();

		// Obtain the function bound managed objects
		ManagedObjectConfiguration<?>[] moConfiguration = configuration.getManagedObjectConfiguration();
		RawBoundManagedObjectMetaData[] functionBoundMo;
		if ((moConfiguration == null) || (moConfiguration.length == 0)) {
			functionBoundMo = new RawBoundManagedObjectMetaData[0];
		} else {
			functionBoundMo = rawBoundManagedObjectFactory.constructBoundManagedObjectMetaData(moConfiguration, issues,
					ManagedObjectScope.FUNCTION, AssetType.FUNCTION, functionName, assetManagerFactory,
					rawOfficeMetaData.getManagedObjectMetaData(), officeScopeMo, null, null,
					rawOfficeMetaData.getGovernanceMetaData());
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
		// Also obtain the parameter type for the task if specified.
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
			functionIndexedManagedObjects[i] = this.loadRequiredManagedObjects(scopeMo, requiredManagedObjects);
		}

		// Obtain the required governance
		boolean[] requiredGovernance;
		ManagedFunctionGovernanceConfiguration[] governanceConfigurations = configuration.getGovernanceConfiguration();
		boolean isManuallyManageGovernance = rawOfficeMetaData.isManuallyManageGovernance();
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
			Map<String, RawGovernanceMetaData<?, ?>> rawGovernances = rawOfficeMetaData.getGovernanceMetaData();
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

		// Create the function meta-data
		ManagedFunctionMetaDataImpl<?, ?> functionMetaData = new ManagedFunctionMetaDataImpl<>(functionName,
				functionFactory, differentiator, parameterType, responsibleTeam, functionIndexedManagedObjects,
				functionBoundMoMetaData, requiredGovernance);

		// Return the raw function meta-data
		@SuppressWarnings({ "rawtypes", "unchecked" })
		RawManagedFunctionMetaData rawFunctionMetaData = new RawManagedFunctionMetaDataImpl(functionName, configuration,
				functionScopeMo, requiredManagedObjects, functionMetaData);
		return rawFunctionMetaData;
	}

	/*
	 * ============== RawManagedFunctionMetaData ==============
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public boolean loadOfficeMetaData(OfficeMetaData officeMetaData, FlowMetaDataFactory flowMetaDataFactory,
			EscalationFlowFactory escalationFlowFactory,
			RawAdministrationMetaDataFactory rawAdministrationMetaDataFactory, Map<String, TeamManagement> officeTeams,
			OfficeFloorIssues issues) {

		// Obtain the listing of flow meta-data
		FlowMetaData[] flowMetaDatas = flowMetaDataFactory.createFlowMetaData(this.configuration.getFlowConfiguration(),
				officeMetaData, AssetType.FUNCTION, this.functionName, issues);
		if (flowMetaDatas == null) {
			return false;
		}

		// Obtain the function locator
		ManagedFunctionLocator functionLocator = officeMetaData.getManagedFunctionLocator();

		// Obtain the next function
		ManagedFunctionMetaData<?, ?> nextFunction = null;
		ManagedFunctionReference nextFunctionReference = this.configuration.getNextFunction();
		if (nextFunctionReference != null) {
			nextFunction = ConstructUtil.getFunctionMetaData(nextFunctionReference, functionLocator, issues,
					AssetType.FUNCTION, this.functionName, "next function");
			if (nextFunction == null) {
				return false;
			}
		}

		// Create the escalation procedure
		EscalationFlow[] escalationFlows = escalationFlowFactory.createEscalationFlows(
				this.configuration.getEscalations(), officeMetaData, AssetType.FUNCTION, this.functionName, issues);
		if (escalationFlows == null) {
			return false;
		}
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(escalationFlows);

		// Create the administrations
		AdministrationMetaData<?, ?, ?>[] preAdministrations = this
				.constructAdministrationMetaDataAndRegisterAdministeredManagedObjects(
						configuration.getPreAdministration(), rawAdministrationMetaDataFactory, officeMetaData,
						flowMetaDataFactory, escalationFlowFactory, officeTeams, issues);
		AdministrationMetaData<?, ?, ?>[] postAdministrations = this
				.constructAdministrationMetaDataAndRegisterAdministeredManagedObjects(
						configuration.getPostAdministration(), rawAdministrationMetaDataFactory, officeMetaData,
						flowMetaDataFactory, escalationFlowFactory, officeTeams, issues);

		// Create the required managed object indexes
		ManagedObjectIndex[] requiredManagedObjectIndexes = new ManagedObjectIndex[this.requiredManagedObjects.size()];
		int requiredIndex = 0;
		for (ManagedObjectIndex requiredManagedObjectIndex : this.requiredManagedObjects.keySet()) {
			requiredManagedObjectIndexes[requiredIndex++] = requiredManagedObjectIndex;
		}

		// Sort the required managed objects
		if (!this.sortRequiredManagedObjects(requiredManagedObjectIndexes, this.requiredManagedObjects,
				this.functionName, issues)) {
			return false;
		}

		// Provide details of each function (to aid debugging application)
		Level logLevel = Level.FINE;
		if (LOGGER.isLoggable(logLevel)) {
			// Log ordering of dependencies for task
			StringBuilder log = new StringBuilder();
			Class<?> parameterType = this.functionMetaData.getParameterType();
			log.append("FUNCTION: " + this.functionName + "(" + (parameterType == null ? "" : parameterType.getName())
					+ ")\n");
			int sequence = 1;
			log.append("  Continuations:\n");
			for (FlowMetaData flow : flowMetaDatas) {
				log.append("   " + (sequence++) + ") ");
				if (flow == null) {
					log.append("<none>\n");
				} else {
					log.append(flow.getInitialFunctionMetaData().getFunctionName()
							+ (flow.isSpawnThreadState() ? " [spawn]" : "") + "\n");
				}
			}
			if (nextFunction != null) {
				log.append("   next) " + nextFunction.getFunctionName() + "\n");
			}
			sequence = 1;
			for (EscalationFlow flow : escalationFlows) {
				log.append("   " + flow.getTypeOfCause().getName() + ") "
						+ flow.getManagedFunctionMetaData().getFunctionName());
			}
			sequence = 1;
			log.append("  Dependency load order:\n");
			for (ManagedObjectIndex index : requiredManagedObjectIndexes) {
				// Obtain the managed object for index
				RawBoundManagedObjectMetaData managedObject = this.requiredManagedObjects.get(index);
				log.append("   " + (sequence++) + ") " + managedObject.getBoundManagedObjectName() + " ["
						+ index.getManagedObjectScope().name() + "," + index.getIndexOfManagedObjectWithinScope()
						+ "]\n");
				LOGGER.log(logLevel, log.toString());
			}
			LOGGER.log(logLevel, log.toString());
		}

		// Load the remaining state for the function meta-data
		this.functionMetaData.loadOfficeMetaData(officeMetaData, flowMetaDatas, nextFunction, escalationProcedure,
				preAdministrations, postAdministrations, requiredManagedObjectIndexes);

		// As here, successfully loaded office meta-data
		return true;
	}

	@Override
	public ManagedFunctionMetaData<O, F> getManagedFunctionMetaData() {
		return this.functionMetaData;
	}

	/**
	 * Constructs the {@link AdministrationMetaData} and registers the
	 * {@link RawBoundManagedObjectMetaData} instances for
	 * {@link Administration}.
	 * 
	 * @param configuration
	 *            {@link AdministrationConfiguration} instances.
	 * @param rawAdministrationMetaDataFactory
	 *            {@link RawAdministrationMetaDataFactory}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param flowMetaDataFactory
	 *            {@link FlowMetaDataFactory}.
	 * @param escalationFlowFactory
	 *            {@link EscalationFlowFactory}.
	 * @param officeTeams
	 *            {@link Map} of {@link TeamManagement} instances within the
	 *            {@link Office}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return Constructed {@link AdministrationMetaData} instances.
	 */
	private AdministrationMetaData<?, ?, ?>[] constructAdministrationMetaDataAndRegisterAdministeredManagedObjects(
			AdministrationConfiguration<?, ?, ?>[] configuration,
			RawAdministrationMetaDataFactory rawAdministrationMetaDataFactory, OfficeMetaData officeMetaData,
			FlowMetaDataFactory flowMetaDataFactory, EscalationFlowFactory escalationFlowFactory,
			Map<String, TeamManagement> officeTeams, OfficeFloorIssues issues) {

		// Construct the raw administration meta-data
		RawAdministrationMetaData[] rawAdministrations = rawAdministrationMetaDataFactory
				.constructRawAdministrationMetaData(configuration, AssetType.FUNCTION, functionName, officeMetaData,
						flowMetaDataFactory, escalationFlowFactory, officeTeams, this.functionScopedManagedObjects,
						issues);

		// Create array of administration meta-data and register managed objects
		AdministrationMetaData<?, ?, ?>[] administrations = new AdministrationMetaData[rawAdministrations.length];
		for (int i = 0; i < rawAdministrations.length; i++) {
			RawAdministrationMetaData rawAdministration = rawAdministrations[i];

			// Add administration to listing
			administrations[i] = rawAdministration.getAdministrationMetaData();

			// Register the administered managed objects
			for (RawBoundManagedObjectMetaData boundManagedObject : rawAdministration
					.getRawBoundManagedObjectMetaData()) {
				this.loadRequiredManagedObjects(boundManagedObject, this.requiredManagedObjects);
			}
		}

		// Return the administration meta-data
		return administrations;
	}

	/**
	 * Recursively loads all the {@link ManagedObjectIndex} instances for the
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundMo
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param requiredManagedObjects
	 *            Mapping of the required {@link ManagedObjectIndex} instances
	 *            by the {@link ManagedFunction} to their respective
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @return {@link ManagedObjectIndex} of the input
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	private ManagedObjectIndex loadRequiredManagedObjects(RawBoundManagedObjectMetaData boundMo,
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects) {

		// Obtain the bound managed object index
		ManagedObjectIndex boundMoIndex = boundMo.getManagedObjectIndex();
		if (!requiredManagedObjects.containsKey(boundMoIndex)) {

			// Not yet required, so add and include all its dependencies
			requiredManagedObjects.put(boundMoIndex, boundMo);
			for (RawBoundManagedObjectInstanceMetaData<?> boundMoInstance : boundMo
					.getRawBoundManagedObjectInstanceMetaData()) {
				RawBoundManagedObjectMetaData[] dependencies = boundMoInstance.getDependencies();
				if (dependencies != null) {
					for (RawBoundManagedObjectMetaData dependency : dependencies) {
						this.loadRequiredManagedObjects(dependency, requiredManagedObjects);
					}
				}
			}
		}

		// Return the managed object index for the bound managed object
		return boundMoIndex;
	}

	/**
	 * <p>
	 * Sorts the required {@link ManagedObjectIndex} instances for the
	 * {@link ManagedFunction} so that dependency {@link ManagedObject}
	 * instances are before the {@link ManagedObject} instances using them. In
	 * essence this is a topological sort so that dependencies are first.
	 * <p>
	 * This is necessary for coordinating so that dependencies are coordinated
	 * before the {@link ManagedObject} instances using them are coordinated.
	 * 
	 * @param requiredManagedObjectIndexes
	 *            Listing of required {@link ManagedObject} instances to be
	 *            sorted.
	 * @param requiredManagedObjects
	 *            Mapping of the {@link ManagedObjectIndex} to its
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param functionName
	 *            Name of {@link ManagedFunction} to issues.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return <code>true</code> indicating that able to sort.
	 *         <code>false</code> indicates unable to sort, possible because of
	 *         cyclic dependencies.
	 */
	private boolean sortRequiredManagedObjects(ManagedObjectIndex[] requiredManagedObjectIndexes,
			final Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects, String functionName,
			OfficeFloorIssues issues) {

		// Initially sort by scope and index
		Arrays.sort(requiredManagedObjectIndexes, new Comparator<ManagedObjectIndex>() {
			@Override
			public int compare(ManagedObjectIndex a, ManagedObjectIndex b) {
				int value = a.getManagedObjectScope().ordinal() - b.getManagedObjectScope().ordinal();
				if (value == 0) {
					value = a.getIndexOfManagedObjectWithinScope() - b.getIndexOfManagedObjectWithinScope();
				}
				return value;
			}
		});

		// Create the set of dependencies for each required managed object
		final Map<ManagedObjectIndex, Set<ManagedObjectIndex>> dependencies = new HashMap<ManagedObjectIndex, Set<ManagedObjectIndex>>();
		for (ManagedObjectIndex index : requiredManagedObjectIndexes) {

			// Obtain the managed object for index
			RawBoundManagedObjectMetaData managedObject = requiredManagedObjects.get(index);

			// Load the dependencies
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> moDependencies = new HashMap<ManagedObjectIndex, RawBoundManagedObjectMetaData>();
			this.loadRequiredManagedObjects(managedObject, moDependencies);

			// Register the dependencies for the index
			dependencies.put(index, new HashSet<ManagedObjectIndex>(moDependencies.keySet()));
		}

		try {
			// Sort so dependencies are first (detecting cyclic dependencies)
			Arrays.sort(requiredManagedObjectIndexes, new Comparator<ManagedObjectIndex>() {
				@Override
				public int compare(ManagedObjectIndex a, ManagedObjectIndex b) {

					// Obtain the dependencies
					Set<ManagedObjectIndex> aDep = dependencies.get(a);
					Set<ManagedObjectIndex> bDep = dependencies.get(b);

					// Determine dependency relationship
					boolean isAdepB = bDep.contains(a);
					boolean isBdepA = aDep.contains(b);

					// Compare based on relationship
					if (isAdepB && isBdepA) {
						// Cyclic dependency
						String[] names = new String[2];
						names[0] = requiredManagedObjects.get(a).getBoundManagedObjectName();
						names[1] = requiredManagedObjects.get(b).getBoundManagedObjectName();
						Arrays.sort(names);
						throw new CyclicDependencyException(
								"Can not have cyclic dependencies (" + names[0] + ", " + names[1] + ")");
					} else if (isAdepB) {
						// A dependent on B, so B must come first
						return -1;
					} else if (isBdepA) {
						// B dependent on A, so A must come first
						return 1;
					} else {
						/*
						 * No dependency relationship. As the sorting only
						 * changes on differences (non 0 value) then need means
						 * to differentiate when no dependency relationship.
						 * This is especially the case with the merge sort used
						 * by default by Java.
						 */

						// Least number of dependencies first.
						// Note: this pushes no dependencies to start.
						int value = aDep.size() - bDep.size();
						if (value == 0) {
							// Same dependencies, so base on scope
							value = a.getManagedObjectScope().ordinal() - b.getManagedObjectScope().ordinal();
							if (value == 0) {
								// Same scope, so arbitrary order
								value = a.getIndexOfManagedObjectWithinScope() - b.getIndexOfManagedObjectWithinScope();
							}
						}
						return value;
					}
				}
			});

		} catch (CyclicDependencyException ex) {
			// Register issue that cyclic dependency
			issues.addIssue(AssetType.FUNCTION, functionName, ex.getMessage());

			// Not sorted as cyclic dependency
			return false;
		}

		// As here must be sorted
		return true;
	}

	/**
	 * Thrown to indicate a cyclic dependency.
	 */
	private static class CyclicDependencyException extends RuntimeException {

		/**
		 * Initiate.
		 * 
		 * @param message
		 *            Initiate with description for {@link OfficeFloorIssues}.
		 */
		public CyclicDependencyException(String message) {
			super(message);
		}
	}

}