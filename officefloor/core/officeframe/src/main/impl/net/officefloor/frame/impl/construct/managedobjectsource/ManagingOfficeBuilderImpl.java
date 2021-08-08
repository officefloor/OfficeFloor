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
import java.util.Map;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionReferenceImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectExecutionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectFunctionDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link ManagingOfficeBuilder} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagingOfficeBuilderImpl<F extends Enum<F>>
		implements ManagingOfficeBuilder<F>, ManagingOfficeConfiguration<F> {

	/**
	 * Name of the {@link Office} managing the {@link ManagedObject}.
	 */
	private final String officeName;

	/**
	 * {@link InputManagedObjectConfiguration} configuring binding the input
	 * {@link ManagedObject} to the {@link ProcessState}.
	 */
	private InputManagedObjectConfiguration<?> inputManagedObjectConfiguration = null;

	/**
	 * {@link ManagedObjectFunctionDependencyConfiguration} instances by
	 * {@link ManagedObjectFunctionDependency} name.
	 */
	private final Map<String, ManagedObjectFunctionDependencyConfiguration> functionDependencies = new HashMap<>();

	/**
	 * {@link ManagedObjectFlowConfiguration} instances by their index.
	 */
	private final Map<Integer, ManagedObjectFlowConfiguration<F>> flows = new HashMap<>();

	/**
	 * {@link ManagedObjectExecutionConfiguration} instances by their index.
	 */
	private final Map<Integer, ManagedObjectExecutionConfiguration> executions = new HashMap<>();

	/**
	 * Initiate.
	 *
	 * @param officeName Name of the {@link Office} managing the
	 *                   {@link ManagedObject}.
	 */
	public ManagingOfficeBuilderImpl(String officeName) {
		this.officeName = officeName;
	}

	/*
	 * ============== ManagingOfficeBuilder ===============================
	 */

	@Override
	@SuppressWarnings("rawtypes")
	public ThreadDependencyMappingBuilder setInputManagedObjectName(String inputManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl(inputManagedObjectName);
		this.inputManagedObjectConfiguration = builder;
		return builder;
	}

	@Override
	public void mapFunctionDependency(String functionObjectName, String scopedManagedObjectName) {

		// Create the dependency
		ManagedObjectFunctionDependencyConfigurationImpl dependency = new ManagedObjectFunctionDependencyConfigurationImpl(
				functionObjectName, scopedManagedObjectName);

		// Map the dependency by function object name
		this.functionDependencies.put(functionObjectName, dependency);
	}

	@Override
	public void linkFlow(F key, String functionName) {
		this.linkFlow(key.ordinal(), key, functionName);
	}

	@Override
	public void linkFlow(int flowIndex, String functionName) {
		this.linkFlow(flowIndex, null, functionName);
	}

	/**
	 * Links in a {@link Flow}.
	 *
	 * @param index        Index for the {@link Flow}.
	 * @param key          Key identifying the {@link Flow}. May be
	 *                     <code>null</code>.
	 * @param functionName Name of {@link ManagedFunction}.
	 */
	private void linkFlow(int index, F key, String functionName) {

		// Create the managed object flow configuration
		ManagedObjectFlowConfiguration<F> flow = new ManagedObjectFlowConfigurationImpl(key, null,
				new ManagedFunctionReferenceImpl(functionName, null));

		// Register the flow at its index
		this.flows.put(Integer.valueOf(index), flow);
	}

	@Override
	public void linkExecutionStrategy(int strategyIndex, String executionStrategyName) {

		// Create the managed object execution configuration
		ManagedObjectExecutionConfiguration execution = new ManagedObjectExecutionConfigurationImpl(
				executionStrategyName);

		// Register the execution at its index
		this.executions.put(Integer.valueOf(strategyIndex), execution);
	}

	/*
	 * ============= ManagingOfficeConfiguration ==========================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public InputManagedObjectConfiguration<?> getInputManagedObjectConfiguration() {
		return this.inputManagedObjectConfiguration;
	}

	@Override
	public ManagingOfficeBuilder<F> getBuilder() {
		return this;
	}

	@Override
	public ManagedObjectFunctionDependencyConfiguration[] getFunctionDependencyConfiguration() {
		return this.functionDependencies.values().toArray(new ManagedObjectFunctionDependencyConfiguration[0]);
	}

	@Override
	public ManagedObjectFlowConfiguration<F>[] getFlowConfiguration() {
		return ConstructUtil.toArray(this.flows, new ManagedObjectFlowConfiguration[0]);
	}

	@Override
	public ManagedObjectExecutionConfiguration[] getExecutionConfiguration() {
		return ConstructUtil.toArray(this.executions, new ManagedObjectExecutionConfiguration[0]);
	}

	/**
	 * {@link ManagedObjectFunctionDependencyConfiguration} implementation.
	 */
	private class ManagedObjectFunctionDependencyConfigurationImpl
			implements ManagedObjectFunctionDependencyConfiguration {

		/**
		 * Name of the {@link ManagedObjectFunctionDependency}.
		 */
		private final String functionObjectName;

		/**
		 * {@link ManagedObject} name.
		 */
		private final String managedObjectName;

		/**
		 * Initiate.
		 * 
		 * @param functionObjectName Name of the
		 *                           {@link ManagedObjectFunctionDependency}.
		 * @param managedObjectName  {@link ManagedObject} name.
		 */
		private ManagedObjectFunctionDependencyConfigurationImpl(String functionObjectName, String managedObjectName) {
			this.functionObjectName = functionObjectName;
			this.managedObjectName = managedObjectName;
		}

		/*
		 * ============= ManagedObjectFunctionDependencyConfiguration ================
		 */

		@Override
		public String getFunctionObjectName() {
			return this.functionObjectName;
		}

		@Override
		public String getScopeManagedObjectName() {
			return this.managedObjectName;
		}
	}

	/**
	 * {@link ManagedObjectFlowConfiguration} implementation.
	 */
	private class ManagedObjectFlowConfigurationImpl implements ManagedObjectFlowConfiguration<F> {

		/**
		 * Flow key.
		 */
		private final F flowKey;

		/**
		 * Flow name.
		 */
		private final String flowName;

		/**
		 * {@link ManagedFunctionReference}.
		 */
		public ManagedFunctionReference functionReference;

		/**
		 * Initiate with flow key.
		 *
		 * @param flowKey           Flow key.
		 * @param flowName          Name of flow.
		 * @param functionReference {@link ManagedFunctionReference}.
		 */
		private ManagedObjectFlowConfigurationImpl(F flowKey, String flowName,
				ManagedFunctionReference functionReference) {
			this.flowKey = flowKey;
			this.flowName = flowName;
			this.functionReference = functionReference;
		}

		/*
		 * ================= ManagedObjectFlowConfiguration ===================
		 */

		@Override
		public F getFlowKey() {
			return this.flowKey;
		}

		@Override
		public String getFlowName() {
			return this.flowName;
		}

		@Override
		public ManagedFunctionReference getManagedFunctionReference() {
			return this.functionReference;
		}
	}

	/**
	 * {@link ManagedObjectExecutionConfiguration} implementation.
	 */
	private class ManagedObjectExecutionConfigurationImpl implements ManagedObjectExecutionConfiguration {

		/**
		 * {@link ExecutionStrategy} name.
		 */
		private final String executionStrategyName;

		private ManagedObjectExecutionConfigurationImpl(String executionStrategyName) {
			this.executionStrategyName = executionStrategyName;
		}

		/*
		 * =============== ManagedObjectExecutionConfiguration ================
		 */

		@Override
		public String getExecutionStrategyName() {
			return this.executionStrategyName;
		}
	}

}
