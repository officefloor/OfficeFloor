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

package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionReferenceImpl;
import net.officefloor.frame.impl.construct.managedobject.ManagedObjectAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectStartupFunctionImpl;
import net.officefloor.frame.impl.execute.officefloor.ManagedObjectExecuteManagerFactoryImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionInvocation;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectExecutionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteManagerFactory;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectStartupFunction;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link RawManagingOfficeMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagingOfficeMetaData<F extends Enum<F>> {

	/**
	 * Determines if the {@link ManagedObjectSource} instigates {@link Flow}
	 * instances.
	 * 
	 * @param flowMetaData {@link ManagedObjectFlowMetaData} instances of the
	 *                     {@link ManagedObjectSource}.
	 * @return <code>true</code> if {@link ManagedObjectSource} instigates
	 *         {@link Flow} instances.
	 */
	public static boolean isRequireFlows(ManagedObjectFlowMetaData<?>[] flowMetaData) {
		return ((flowMetaData != null) && (flowMetaData.length > 0));
	}

	/**
	 * Name of the managing {@link Office}.
	 */
	private final String managingOfficeName;

	/**
	 * Name of the {@link ManagedFunction} to recycle the {@link ManagedObject}.
	 */
	private final String recycleFunctionName;

	/**
	 * {@link InputManagedObjectConfiguration} to configure binding the input
	 * {@link ManagedObject} to the {@link ProcessState}.
	 */
	private final InputManagedObjectConfiguration<?> inputConfiguration;

	/**
	 * {@link ManagedObjectFlowMetaData} instances for the
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectFlowMetaData<F>[] flowMetaDatas;

	/**
	 * {@link ManagedObjectExecutionMetaData} instances for the
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectExecutionMetaData[] executionMetaDatas;

	/**
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeConfiguration<F> managingOfficeConfiguration;

	/**
	 * Start up {@link ManagedFunctionInvocation} instances.
	 */
	private final ManagedFunctionInvocation[] startupInvocations;

	/**
	 * {@link RawManagedObjectMetaData}.
	 */
	private RawManagedObjectMetaData<?, F> rawManagedObjectMetaData;

	/**
	 * {@link RawBoundManagedObjectInstanceMetaData} instances created before this
	 * is managed by the {@link Office}.
	 */
	private List<RawBoundManagedObjectInstanceMetaData<?>> managedObjectMetaDatas = new LinkedList<>();

	/**
	 * {@link OfficeMetaData} of the managing {@link Office}.
	 */
	private OfficeMetaData managingOffice = null;

	/**
	 * {@link FlowMetaData} of the recycle {@link Flow}.
	 */
	private FlowMetaData recycleFlowMetaData = null;

	/**
	 * {@link ManagedObjectStartupFunction} instances.
	 */
	private ManagedObjectStartupFunction[] startupFunctions = null;

	/**
	 * {@link ManagedObjectAdministrationMetaDataFactory}.
	 */
	private ManagedObjectAdministrationMetaDataFactory moAdminFactory = null;

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private OfficeFloorIssues issues = null;

	/**
	 * {@link ManagedObjectExecuteManagerFactory}.
	 */
	private ManagedObjectExecuteManagerFactory<F> managedObjectExecuteContextFactory = null;

	/**
	 * Initialise.
	 * 
	 * @param managingOfficeName          Name of the managing {@link Office}.
	 * @param recycleFunctionName         Name of the {@link ManagedFunction} to
	 *                                    recycle the {@link ManagedObject}.
	 * @param inputConfiguration          {@link InputManagedObjectConfiguration} to
	 *                                    configure binding the input
	 *                                    {@link ManagedObject} to the
	 *                                    {@link ProcessState}.
	 * @param flowMetaDatas               {@link ManagedObjectFlowMetaData}
	 *                                    instances for the
	 *                                    {@link ManagedObjectSource}.
	 * @param executionMetaDatas          {@link ManagedObjectExecutionMetaData}
	 *                                    instances for the
	 *                                    {@link ManagedObjectSource}.
	 * @param managingOfficeConfiguration {@link ManagingOfficeConfiguration}.
	 * @param startupInvocations          Start up {@link ManagedFunctionInvocation}
	 *                                    instances.
	 */
	public RawManagingOfficeMetaData(String managingOfficeName, String recycleFunctionName,
			InputManagedObjectConfiguration<?> inputConfiguration, ManagedObjectFlowMetaData<F>[] flowMetaDatas,
			ManagedObjectExecutionMetaData[] executionMetaDatas,
			ManagingOfficeConfiguration<F> managingOfficeConfiguration,
			ManagedFunctionInvocation[] startupInvocations) {
		this.managingOfficeName = managingOfficeName;
		this.recycleFunctionName = recycleFunctionName;
		this.inputConfiguration = inputConfiguration;
		this.flowMetaDatas = flowMetaDatas;
		this.executionMetaDatas = executionMetaDatas;
		this.managingOfficeConfiguration = managingOfficeConfiguration;
		this.startupInvocations = startupInvocations;
	}

	/**
	 * Specifies the {@link RawManagedObjectMetaData}.
	 * 
	 * @param rawManagedObjectMetaData {@link RawManagedObjectMetaData}.
	 */
	public void setRawManagedObjectMetaData(RawManagedObjectMetaData<?, F> rawManagedObjectMetaData) {
		this.rawManagedObjectMetaData = rawManagedObjectMetaData;
	}

	/**
	 * Adds a {@link ManagedObjectMetaData} to be managed by the managing
	 * {@link Office}.
	 * 
	 * @param boundInstanceMetaData          {@link RawBoundManagedObjectInstanceMetaData}
	 *                                       for the {@link ManagedObjectMetaData}.
	 * @param assetManagerRegistry           {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout Default {@link AsynchronousFlow}
	 *                                       timeout.
	 */
	public void manageManagedObject(RawBoundManagedObjectInstanceMetaData<?> boundInstanceMetaData,
			AssetManagerRegistry assetManagerRegistry, long defaultAsynchronousFlowTimeout) {

		// Determine if being managed by an office
		if (this.managedObjectMetaDatas != null) {
			// Not yet managed by an office
			this.managedObjectMetaDatas.add(boundInstanceMetaData);

		} else {
			// Already being managed, so load remaining state
			boundInstanceMetaData.loadRemainingState(this.managingOffice, this.startupFunctions,
					this.recycleFlowMetaData, this.moAdminFactory, assetManagerRegistry, defaultAsynchronousFlowTimeout,
					this.issues);
		}
	}

	/**
	 * Obtains the name for the {@link Office} managing the {@link ManagedObject}.
	 * 
	 * @return Name for the {@link Office} managing the {@link ManagedObject}.
	 */
	public String getManagingOfficeName() {
		return this.managingOfficeName;
	}

	/**
	 * <p>
	 * Indicates if the {@link ManagedObjectSource} requires instigating
	 * {@link Flow} instances.
	 * <p>
	 * If <code>true</code> it means the {@link ManagedObjectSource} must be bound
	 * to the {@link ProcessState} of the {@link Office}.
	 * 
	 * @return <code>true</code> if the {@link ManagedObjectSource} requires
	 *         instigating {@link Flow} instances.
	 */
	public InputManagedObjectConfiguration<?> getInputManagedObjectConfiguration() {
		return this.inputConfiguration;
	}

	/**
	 * <p>
	 * Obtains the {@link InputManagedObjectConfiguration} configuring the bind of
	 * the {@link ManagedObject} within the {@link ProcessState} of the
	 * {@link Office}.
	 * <p>
	 * Should the {@link ManagedObjectSource} instigate a {@link Flow}, a
	 * {@link ManagedObject} from the {@link ManagedObjectSource} is to be made
	 * available to the {@link ProcessState}. Whether the {@link Office} wants to
	 * make use of the {@link ManagedObject} is its choice but is available to do
	 * so.
	 * 
	 * @return {@link InputManagedObjectConfiguration} configuring the bind of the
	 *         {@link ManagedObject} within the {@link ProcessState} of the
	 *         {@link Office}.
	 */
	public RawManagedObjectMetaData<?, F> getRawManagedObjectMetaData() {
		return this.rawManagedObjectMetaData;
	}

	/**
	 * Obtains the {@link RawManagedObjectMetaData} for the {@link ManagedObject} to
	 * be managed by the {@link Office}.
	 * 
	 * @return {@link RawManagedObjectMetaData} for the {@link ManagedObject} to be
	 *         managed by the {@link Office}.
	 */
	public boolean isRequireFlows() {
		return isRequireFlows(this.flowMetaDatas);
	}

	/**
	 * Sets up the {@link ManagedObjectSource} to be managed by the {@link Office}
	 * of the input {@link ManagedFunctionLocator}.
	 * 
	 * @param officeMetaData                    {@link OfficeMetaData}.
	 * @param processBoundManagedObjectMetaData {@link RawBoundManagedObjectMetaData}
	 *                                          of the {@link ProcessState} bound
	 *                                          {@link ManagedObject} instances of
	 *                                          the managing {@link Office}.
	 * @param moAdminFactory                    {@link ManagedObjectAdministrationMetaDataFactory}.
	 * @param defaultExecutionStrategy          Default {@link ExecutionStrategy}.
	 * @param executionStrategies               {@link ExecutionStrategy} instances
	 *                                          by their name.
	 * @param assetManagerRegistry              {@link AssetManagerRegistry}.
	 * @param defaultAsynchronousFlowTimeout    Default {@link AsynchronousFlow}
	 *                                          timeout.
	 * @param issues                            {@link OfficeFloorIssues}.
	 */
	public void manageByOffice(OfficeMetaData officeMetaData,
			RawBoundManagedObjectMetaData[] processBoundManagedObjectMetaData,
			ManagedObjectAdministrationMetaDataFactory moAdminFactory, ThreadFactory[] defaultExecutionStrategy,
			Map<String, ThreadFactory[]> executionStrategies, AssetManagerRegistry assetManagerRegistry,
			long defaultAsynchronousFlowTimeout, OfficeFloorIssues issues) {

		// Obtain the name of the managed object source
		String managedObjectSourceName = this.rawManagedObjectMetaData.getManagedObjectName();

		// -----------------------------------------------------------
		// Load Remaining State to the Managed Object Meta-Data
		// -----------------------------------------------------------

		// Obtain the function locator
		ManagedFunctionLocator functionLocator = officeMetaData.getManagedFunctionLocator();

		// Obtain the recycle function meta-data
		FlowMetaData recycleFlowMetaData = null;
		if (this.recycleFunctionName != null) {

			// Locate the function meta-data
			ManagedFunctionMetaData<?, ?> recycleFunctionMetaData = functionLocator
					.getManagedFunctionMetaData(this.recycleFunctionName);
			if (recycleFunctionMetaData == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Recycle function '" + this.recycleFunctionName + "' not found");
				return; // must obtain recycle function
			}

			// Obtain the parameter type for the recycle function
			Class<?> parameterType = recycleFunctionMetaData.getParameterType();
			if (parameterType != null) {
				if (!parameterType.isAssignableFrom(RecycleManagedObjectParameter.class)) {
					issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
							"Incompatible parameter type for recycle function (parameter=" + parameterType.getName()
									+ ", required type=" + RecycleManagedObjectParameter.class.getName() + ", function="
									+ this.recycleFunctionName + ")");
					return; // can not be used as recycle function
				}
			}

			// Obtain the initial flow of work as recycle flow
			recycleFlowMetaData = ConstructUtil.newFlowMetaData(recycleFunctionMetaData, false);
		}

		// Obtain the start up function meta-data
		ManagedObjectStartupFunction[] startupFunctions = new ManagedObjectStartupFunction[this.startupInvocations.length];
		for (int i = 0; i < startupFunctions.length; i++) {
			ManagedFunctionInvocation startupInvocation = this.startupInvocations[i];

			// Locate the function meta-data
			String startupFunctionName = startupInvocation.getFunctionName();
			if (startupFunctionName == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Must provide name for start up function " + i);
				return; // must have start up function name
			}
			ManagedFunctionMetaData<?, ?> startupFunctionMetaData = functionLocator
					.getManagedFunctionMetaData(startupFunctionName);
			if (startupFunctionMetaData == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Start up function '" + startupFunctionName + "' not found");
				return; // must obtain startup function
			}

			// Obtain the argument
			Object startupArgument = startupInvocation.getArgument();

			// Ensure valid argument for the startup function
			Class<?> functionParameterType = startupFunctionMetaData.getParameterType();
			if ((functionParameterType != null) && (startupArgument != null)) {
				Class<?> argumentType = startupArgument.getClass();
				if (!functionParameterType.isAssignableFrom(argumentType)) {
					issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
							"Incompatible parameter type for startup function (parameter=" + argumentType.getName()
									+ ", required type=" + functionParameterType.getName() + ", function="
									+ startupFunctionName + ")");
					return; // can not be used as startup function
				}
			}

			// Create the startup function
			FlowMetaData startupFlowMetaData = ConstructUtil.newFlowMetaData(startupFunctionMetaData, false);
			startupFunctions[i] = new ManagedObjectStartupFunctionImpl(startupFlowMetaData, startupArgument);
		}

		// Load remaining state to existing managed object meta-data
		for (RawBoundManagedObjectInstanceMetaData<?> mo : this.managedObjectMetaDatas) {
			mo.loadRemainingState(officeMetaData, startupFunctions, recycleFlowMetaData, moAdminFactory,
					assetManagerRegistry, defaultAsynchronousFlowTimeout, issues);
		}

		// Obtain the managed object meta-data (for no flows)
		ManagedObjectMetaData<?> moMetaData = this.managedObjectMetaDatas.size() == 1
				? this.managedObjectMetaDatas.get(0).getManagedObjectMetaData()
				: null;

		// Setup for further managed object meta-data to be managed
		this.managingOffice = officeMetaData;
		this.startupFunctions = startupFunctions;
		this.recycleFlowMetaData = recycleFlowMetaData;
		this.moAdminFactory = moAdminFactory;
		this.issues = issues;
		this.managedObjectMetaDatas = null;

		// -----------------------------------------------------------
		// Create the Managed Object Execute Context
		// -----------------------------------------------------------

		// Obtain the execution configuration
		ManagedObjectExecutionConfiguration[] executionConfigurations = this.managingOfficeConfiguration
				.getExecutionConfiguration();

		// Create the execution mappings for the configuration
		Map<Integer, ManagedObjectExecutionConfiguration> executionMappings = new HashMap<>();
		for (int i = 0; i < executionConfigurations.length; i++) {
			ManagedObjectExecutionConfiguration executionConfiguration = executionConfigurations[i];
			executionMappings.put(Integer.valueOf(i), executionConfiguration);
		}

		// Create the executions
		ThreadFactory[][] threadFactories;
		if ((this.executionMetaDatas == null) || (this.executionMetaDatas.length == 0)) {

			// No execution strategies but issue if configuration
			if ((executionConfigurations != null) && (executionConfigurations.length > 0)) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						ManagedObjectSourceMetaData.class.getSimpleName()
								+ " specifies no execution strategies but execution strategies configured for it");
				return; // configuration does not align to meta-data
			}

			// No execution strategies
			threadFactories = new ThreadFactory[0][];

		} else {
			// Configure the thread factories
			threadFactories = new ThreadFactory[this.executionMetaDatas.length][];
			for (int i = 0; i < threadFactories.length; i++) {
				int index = i;
				ManagedObjectExecutionMetaData executionMetaData = this.executionMetaDatas[i];

				// Obtain the execution strategy
				ThreadFactory[] executionStrategy;
				if (defaultExecutionStrategy != null) {
					// Use default execution strategy
					executionStrategy = defaultExecutionStrategy;

				} else {
					// Use configured execution strategy
					String label = executionMetaData.getLabel();
					String executionLabel = "execution strategy " + index + " (label="
							+ (!ConstructUtil.isBlank(label) ? label : "<no label>") + ")";

					// Obtain the execution configuration
					ManagedObjectExecutionConfiguration executionConfiguration = executionMappings
							.get(Integer.valueOf(index));
					if (executionConfiguration == null) {
						issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
								"No execution strategy configured for " + executionLabel);
						return; // execution not configured
					}

					// Remove execution for later check no extra configured
					executionMappings.remove(Integer.valueOf(index));

					// Obtain the execution strategy
					String executionStrategyName = executionConfiguration.getExecutionStrategyName();
					if (executionStrategyName == null) {
						issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
								"No execution strategy name configured for " + executionLabel);
						return; // execution not configured
					}

					// Obtain the execution strategy
					executionStrategy = executionStrategies.get(executionStrategyName);
					if (executionStrategy == null) {
						issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
								"No execution strategy available by name '" + executionStrategyName + "' for "
										+ executionLabel);
						return; // execution not configured
					}
				}

				// Specify the execution strategy
				threadFactories[i] = executionStrategy;
			}

			// Ensure no extra execution configurations
			if (executionMappings.size() > 0) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Extra execution strategies configured than specified by "
								+ ManagedObjectSourceMetaData.class.getSimpleName());
				return; // should only have configurations for meta-data required
			}
		}

		// Obtain the execute logger
		Logger executeLogger = this.rawManagedObjectMetaData.getExecuteLogger();

		// Obtain the flow configuration
		ManagedObjectFlowConfiguration<F>[] flowConfigurations = this.managingOfficeConfiguration
				.getFlowConfiguration();

		// Determine if flows for the managed object source
		if (!this.isRequireFlows()) {

			// No flows but issue if flows or input configuration
			if ((flowConfigurations != null) && (flowConfigurations.length > 0)) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						ManagedObjectSourceMetaData.class.getSimpleName()
								+ " specifies no flows but flows configured for it");
				return; // configuration does not align to meta-data
			}

			// No flows, so provide empty execution context
			this.managedObjectExecuteContextFactory = new ManagedObjectExecuteManagerFactoryImpl<F>(moMetaData,
					threadFactories, executeLogger, officeMetaData);
			return;
		}

		// Obtain the bound input name for this managed object source
		String processBoundName = this.inputConfiguration.getBoundManagedObjectName();
		if (ConstructUtil.isBlank(processBoundName)) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName, ManagedObjectSource.class.getSimpleName()
					+ " invokes flows but does not provide input Managed Object binding name");
			return;
		}

		// Obtain the process bound index and Managed Object meta-data
		int processBoundIndex = -1;
		ManagedObjectMetaData<?> managedObjectMetaData = null;
		if (processBoundManagedObjectMetaData != null) {
			NEXT_BOUND_MO: for (int i = 0; i < processBoundManagedObjectMetaData.length; i++) {
				RawBoundManagedObjectMetaData boundMetaData = processBoundManagedObjectMetaData[i];
				if (processBoundName.equals(boundMetaData.getBoundManagedObjectName())) {
					// Found the bound configuration for this managed object
					processBoundIndex = i;

					// Find the particular instance
					for (RawBoundManagedObjectInstanceMetaData<?> instanceMetaData : boundMetaData
							.getRawBoundManagedObjectInstanceMetaData()) {
						if (managedObjectSourceName
								.equals(instanceMetaData.getRawManagedObjectMetaData().getManagedObjectName())) {
							// Found the instance meta-data
							managedObjectMetaData = instanceMetaData.getManagedObjectMetaData();
							break NEXT_BOUND_MO; // index and meta-data obtained
						}
					}
				}
			}
		}
		if ((processBoundIndex < 0) || (managedObjectMetaData == null)) {
			// Managed Object Source not in Office
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					ManagedObjectSource.class.getSimpleName() + " by input name '" + processBoundName
							+ "' not managed by Office " + officeMetaData.getOfficeName());
			return;
		}

		// Create the flow mappings for the configuration
		Map<Integer, ManagedObjectFlowConfiguration<F>> flowMappings = new HashMap<Integer, ManagedObjectFlowConfiguration<F>>();
		for (int i = 0; i < flowConfigurations.length; i++) {
			ManagedObjectFlowConfiguration<F> flowConfiguration = flowConfigurations[i];

			// Obtain the index to identify the flow
			F flowKey = flowConfiguration.getFlowKey();
			int index = (flowKey != null ? flowKey.ordinal() : i);

			// Load the flow at its index
			flowMappings.put(Integer.valueOf(index), flowConfiguration);
		}

		// Create the flows
		FlowMetaData[] flows = new FlowMetaData[this.flowMetaDatas.length];
		for (int i = 0; i < flows.length; i++) {
			ManagedObjectFlowMetaData<F> flowMetaData = this.flowMetaDatas[i];

			// Obtain the index for the flow
			F flowKey = flowMetaData.getKey();
			int index = (flowKey != null ? flowKey.ordinal() : i);

			// Create name to identify flow
			String label = flowMetaData.getLabel();
			String flowLabel = "flow " + index + " (key=" + (flowKey != null ? flowKey.toString() : "<indexed>")
					+ ", label=" + (!ConstructUtil.isBlank(label) ? label : "<no label>") + ")";

			// Obtain the flow configuration
			ManagedObjectFlowConfiguration<F> flowConfiguration = flowMappings.get(Integer.valueOf(index));
			if (flowConfiguration == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"No flow configured for " + flowLabel);
				return; // flow not configured
			}

			// Remove flow for later check no extra configured
			flowMappings.remove(Integer.valueOf(index));

			// Obtain the argument type passed to the task
			Class<?> argumentType = flowMetaData.getArgumentType();

			// Create the reference for the function.
			// Override argument type as managed object knows better.
			ManagedFunctionReference configurationFunctionReference = flowConfiguration.getManagedFunctionReference();
			ManagedFunctionReference flowFunctionReference = new ManagedFunctionReferenceImpl(
					configurationFunctionReference.getFunctionName(), argumentType);

			// Obtain the function meta-data of flow meta-data
			ManagedFunctionMetaData<?, ?> functionMetaData = ConstructUtil.getFunctionMetaData(flowFunctionReference,
					functionLocator, issues, AssetType.MANAGED_OBJECT, managedObjectSourceName, flowLabel);
			if (functionMetaData == null) {
				return; // can not find function of flow
			}

			// Create and specify the flow meta-data
			flows[i] = ConstructUtil.newFlowMetaData(functionMetaData, false);
		}

		// Ensure no extra flow configurations
		if (flowMappings.size() > 0) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Extra flows configured than specified by " + ManagedObjectSourceMetaData.class.getSimpleName());
			return; // should only have configurations for meta-data required
		}

		// Specify the managed object execute context
		this.managedObjectExecuteContextFactory = new ManagedObjectExecuteManagerFactoryImpl<F>(managedObjectMetaData,
				processBoundIndex, flows, threadFactories, executeLogger, officeMetaData);
	}

	/**
	 * Obtains the {@link ManagedObjectExecuteManagerFactory} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectExecuteManagerFactory} for the
	 *         {@link ManagedObjectSource}.
	 */
	public ManagedObjectExecuteManagerFactory<F> getManagedObjectExecuteManagerFactory() {
		return this.managedObjectExecuteContextFactory;
	}

}
