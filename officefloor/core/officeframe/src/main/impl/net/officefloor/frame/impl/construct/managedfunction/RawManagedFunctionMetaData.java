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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaData;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionAdministrationMetaDataImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * Raw meta-data for a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagedFunctionMetaData<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = OfficeFrame.getLogger(RawManagedFunctionMetaData.class.getName());

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
	 * @param functionName                 Name of the {@link ManagedFunction}.
	 * @param configuration                {@link ManagedFunctionConfiguration}.
	 * @param functionScopedManagedObjects {@link ManagedFunction} scoped
	 *                                     {@link RawBoundManagedObjectMetaData}.
	 * @param requiredManagedObjects       Required
	 *                                     {@link RawBoundManagedObjectMetaData} for
	 *                                     this {@link ManagedFunction}.
	 * @param functionMetaData             {@link ManagedFunctionMetaDataImpl}.
	 */
	public RawManagedFunctionMetaData(String functionName, ManagedFunctionConfiguration<O, F> configuration,
			Map<String, RawBoundManagedObjectMetaData> functionScopedManagedObjects,
			Map<ManagedObjectIndex, RawBoundManagedObjectMetaData> requiredManagedObjects,
			ManagedFunctionMetaDataImpl<O, F> functionMetaData) {
		this.functionName = functionName;
		this.configuration = configuration;
		this.functionScopedManagedObjects = functionScopedManagedObjects;
		this.requiredManagedObjects = requiredManagedObjects;
		this.functionMetaData = functionMetaData;
	}

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	public String getFunctionName() {
		return this.functionName;
	}

	/**
	 * Loads meta-data regarding the containing {@link Office}.
	 * 
	 * @param officeMetaData                   {@link OfficeMetaData}.
	 * @param flowMetaDataFactory              {@link FlowMetaDataFactory}.
	 * @param escalationFlowFactory            {@link EscalationFlowFactory}.
	 * @param rawAdministrationMetaDataFactory {@link RawAdministrationMetaDataFactory}.
	 * @param assetManagerRegistry             {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout   Default {@link AsynchronousFlow}
	 *                                         timeout.
	 * @param issues                           {@link OfficeFloorIssues}.
	 * @return <code>true</code> if successfully loaded the {@link OfficeMetaData}.
	 */
	public boolean loadOfficeMetaData(OfficeMetaData officeMetaData, FlowMetaDataFactory flowMetaDataFactory,
			EscalationFlowFactory escalationFlowFactory,
			RawAdministrationMetaDataFactory rawAdministrationMetaDataFactory,
			AssetManagerRegistry assetManagerRegistry, long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Obtain the listing of flow meta-data
		FlowMetaData[] flowMetaDatas = flowMetaDataFactory.createFlowMetaData(this.configuration.getFlowConfiguration(),
				AssetType.FUNCTION, this.functionName, issues);
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
				this.configuration.getEscalations(), AssetType.FUNCTION, this.functionName, issues);
		if (escalationFlows == null) {
			return false;
		}
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(escalationFlows);

		// Create the administrations
		ManagedFunctionAdministrationMetaData<?, ?, ?>[] preAdministrations = this
				.constructAdministrationMetaDataAndRegisterAdministeredManagedObjects("pre",
						configuration.getPreAdministration(), rawAdministrationMetaDataFactory, assetManagerRegistry,
						defaultAsynchronousFlowTimeout, issues);
		ManagedFunctionAdministrationMetaData<?, ?, ?>[] postAdministrations = this
				.constructAdministrationMetaDataAndRegisterAdministeredManagedObjects("post",
						configuration.getPostAdministration(), rawAdministrationMetaDataFactory, assetManagerRegistry,
						defaultAsynchronousFlowTimeout, issues);

		// Create the required managed object indexes
		ManagedObjectIndex[] requiredManagedObjectIndexes = RawBoundManagedObjectMetaData
				.createSortedRequiredManagedObjects(this.requiredManagedObjects, AssetType.FUNCTION, functionName,
						issues);
		if (requiredManagedObjectIndexes == null) {
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

	/**
	 * Obtains the {@link ManagedFunctionMetaData}.
	 * 
	 * @return {@link ManagedFunctionMetaData}.
	 */
	public ManagedFunctionMetaData<O, F> getManagedFunctionMetaData() {
		return this.functionMetaData;
	}

	/**
	 * Constructs the {@link AdministrationMetaData} and registers the
	 * {@link RawBoundManagedObjectMetaData} instances for {@link Administration}.
	 * 
	 * @param administrationQualifier        {@link Administration} qualifier.
	 * @param configuration                  {@link AdministrationConfiguration}
	 *                                       instances.
	 * @param rawAdminFactory                {@link RawAdministrationMetaDataFactory}.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 * @param issues                         {@link OfficeFloorIssues}.
	 * @return Constructed {@link ManagedFunctionAdministrationMetaData} instances.
	 */
	private ManagedFunctionAdministrationMetaData<?, ?, ?>[] constructAdministrationMetaDataAndRegisterAdministeredManagedObjects(
			String administrationQualifier, AdministrationConfiguration<?, ?, ?>[] configuration,
			RawAdministrationMetaDataFactory rawAdminFactory, AssetManagerRegistry assetManagerRegistry,
			long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Construct the raw administration meta-data
		RawAdministrationMetaData[] rawAdministrations = rawAdminFactory.constructRawAdministrationMetaData(
				this.functionName, administrationQualifier, configuration, this.functionScopedManagedObjects,
				AssetType.FUNCTION, this.functionName, assetManagerRegistry, defaultAsynchronousFlowTimeout, issues);

		// Create array of administration meta-data and register managed objects
		ManagedFunctionAdministrationMetaData<?, ?, ?>[] administrations = new ManagedFunctionAdministrationMetaData[rawAdministrations.length];
		for (int i = 0; i < rawAdministrations.length; i++) {
			RawAdministrationMetaData rawAdministration = rawAdministrations[i];
			AdministrationMetaData<?, ?, ?> administrationMetaData = rawAdministration.getAdministrationMetaData();

			// Create the logger
			String functionAdminName = this.functionName + "." + administrationQualifier + "."
					+ administrationMetaData.getAdministrationName();
			Logger logger = OfficeFrame.getLogger(functionAdminName);

			// Add administration to listing
			administrations[i] = new ManagedFunctionAdministrationMetaDataImpl<>(logger,
					rawAdministration.getAdministrationMetaData());

			// Register the administered managed objects
			for (RawBoundManagedObjectMetaData boundManagedObject : rawAdministration
					.getRawBoundManagedObjectMetaData()) {
				RawBoundManagedObjectMetaData.loadRequiredManagedObjects(boundManagedObject,
						this.requiredManagedObjects);
			}
		}

		// Return the administration meta-data
		return administrations;
	}

}
