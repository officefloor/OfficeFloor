/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.managedobjectsource;

import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceFlow;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.managedobjectpool.ManagedObjectPoolBuilderImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectPoolConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Implementation of the {@link ManagedObjectSourceContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceContextImpl<F extends Enum<F>> extends SourceContextImpl
		implements ManagedObjectSourceContext<F> {

	/**
	 * Name of the recycle the {@link ManagedFunction}.
	 */
	public static final String MANAGED_OBJECT_RECYCLE_FUNCTION_NAME = "#recycle#";

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeConfiguration<F> managingOfficeConfiguration;

	/**
	 * {@link ManagingOfficeBuilder}.
	 */
	private ManagingOfficeBuilder<F> managingOfficeBuilder;

	/**
	 * {@link OfficeBuilder} for the office using the {@link ManagedObjectSource}.
	 */
	private OfficeBuilder officeBuilder;

	/**
	 * Default {@link ManagedObjectPoolConfiguration}.
	 */
	private ManagedObjectPoolConfiguration defaultManagedObjectPool;

	/**
	 * Name of the {@link ManagedFunction} to clean up this {@link ManagedObject}.
	 */
	private String recycleFunctionName = null;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managingOfficeConfiguration
	 *            {@link ManagingOfficeConfiguration}.
	 * @param properties
	 *            Properties.
	 * @param sourceContext
	 *            Delegate {@link SourceContext}.
	 * @param managingOfficeBuilder
	 *            {@link ManagingOfficeBuilder}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} for the office using the
	 *            {@link ManagedObjectSource}.
	 */
	public ManagedObjectSourceContextImpl(boolean isLoadingType, String managedObjectName,
			ManagingOfficeConfiguration<F> managingOfficeConfiguration, SourceProperties properties,
			SourceContext sourceContext, ManagingOfficeBuilder<F> managingOfficeBuilder, OfficeBuilder officeBuilder) {
		super(isLoadingType, sourceContext, properties);
		this.managedObjectName = managedObjectName;
		this.managingOfficeConfiguration = managingOfficeConfiguration;
		this.managingOfficeBuilder = managingOfficeBuilder;
		this.officeBuilder = officeBuilder;
	}

	/**
	 * Indicates that the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} method has
	 * completed.
	 */
	public void flagInitOver() {
		// Disallow further configuration
		this.managingOfficeBuilder = null;
		this.officeBuilder = null;
	}

	/**
	 * Obtains the default {@link ManagedObjectPoolConfiguration}.
	 * 
	 * @return Default {@link ManagedObjectPoolConfiguration}. May be
	 *         <code>null</code> if no default {@link ManagedObjectPool}.
	 */
	public ManagedObjectPoolConfiguration getDefaultManagedObjectPoolConfiguration() {
		return this.defaultManagedObjectPool;
	}

	/**
	 * Obtains the name of the {@link ManagedFunction} to recycle this
	 * {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedFunction} to recycle this
	 *         {@link ManagedObject} or <code>null</code> if no recycling of this
	 *         {@link ManagedObject}.
	 */
	public String getRecycleFunctionName() {
		return this.recycleFunctionName;
	}

	/**
	 * Obtains the input bound {@link ManagedObject} name.
	 * 
	 * @return Input bound {@link ManagedObject} name.
	 */
	private String getInputBoundManagedObjectName() {
		InputManagedObjectConfiguration<?> inputConfiguration = this.managingOfficeConfiguration
				.getInputManagedObjectConfiguration();
		return (inputConfiguration != null ? inputConfiguration.getBoundManagedObjectName() : null);
	}

	/*
	 * =============== ManagedObjectSourceContext =====================
	 */

	@Override
	public ManagedObjectPoolBuilder setDefaultManagedObjectPool(ManagedObjectPoolFactory managedObjectPoolFactory) {
		ManagedObjectPoolBuilderImpl poolBuilder = new ManagedObjectPoolBuilderImpl(managedObjectPoolFactory);
		this.defaultManagedObjectPool = poolBuilder;
		return poolBuilder;
	}

	@Override
	public <O extends Enum<O>, f extends Enum<f>> ManagedObjectFunctionBuilder<O, f> getRecycleFunction(
			ManagedFunctionFactory<O, f> managedFunctionFactory) {

		// Ensure not already created
		if (this.recycleFunctionName != null) {
			throw new IllegalStateException("Only one clean up per Managed Object");
		}

		// Name the recycle function
		this.recycleFunctionName = this.getNamespacedName(MANAGED_OBJECT_RECYCLE_FUNCTION_NAME);

		// Add and return the recycle function
		return this.addManagedFunction(MANAGED_OBJECT_RECYCLE_FUNCTION_NAME, managedFunctionFactory);
	}

	@Override
	public <o extends Enum<o>, f extends Enum<f>> ManagedObjectFunctionBuilder<o, f> addManagedFunction(
			String functionName, ManagedFunctionFactory<o, f> managedFunctionFactory) {

		// Obtain the name of the function
		String namespacedFunctionName = this.getNamespacedName(functionName);

		// Create the managed function
		ManagedFunctionBuilder<o, f> functionBuilder = this.officeBuilder.addManagedFunction(namespacedFunctionName,
				managedFunctionFactory);

		// Return the managed object function builder
		return new ManagedObjectFunctionBuilderImpl<o, f>(functionBuilder);
	}

	@Override
	public void addStartupFunction(String functionName) {
		this.officeBuilder.addStartupFunction(this.getNamespacedName(functionName));
	}

	@Override
	public ManagedObjectSourceFlow getFlow(F key) {
		return new ManagedObjectSourceFlowImpl(key, key.ordinal());
	}

	@Override
	public ManagedObjectSourceFlow getFlow(int flowIndex) {
		return new ManagedObjectSourceFlowImpl(null, flowIndex);
	}

	/**
	 * Obtains the name including the name space for this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name to add name space.
	 * @return Name including the name space.
	 */
	private String getNamespacedName(String name) {
		return OfficeBuilderImpl.getNamespacedName(this.managedObjectName, name);
	}

	/**
	 * {@link ManagedObjectSourceFlow} implementation.
	 */
	private class ManagedObjectSourceFlowImpl implements ManagedObjectSourceFlow {

		/**
		 * {@link Flow} key.
		 */
		private final F key;

		/**
		 * {@link Flow} index.
		 */
		private final int flowIndex;

		/**
		 * Instantiate.
		 * 
		 * @param key
		 *            {@link Flow} key.
		 * @param flowIndex
		 *            {@link Flow} index.
		 */
		public ManagedObjectSourceFlowImpl(F key, int flowIndex) {
			this.key = key;
			this.flowIndex = flowIndex;
		}

		/**
		 * Obtains the linked {@link ManagedFunction} name for the
		 * {@link ManagedObjectSourceFlow}.
		 * 
		 * @return Linked {@link ManagedFunction} name or <code>null</code>.
		 */
		private String getFlowLinkedFunctionName() {

			// Search flows for matching linked function
			ManagedObjectFlowConfiguration<F>[] flows = ManagedObjectSourceContextImpl.this.managingOfficeConfiguration
					.getFlowConfiguration();
			for (int flowIndex = 0; flowIndex < flows.length; flowIndex++) {
				ManagedObjectFlowConfiguration<F> flow = flows[flowIndex];

				// Determine if matching flow
				if (this.key != null) {
					if (this.key.equals(flow.getFlowKey())) {
						// Matching key
						return flow.getManagedFunctionReference().getFunctionName();
					}

				} else if (this.flowIndex == flowIndex) {
					// Matching on index
					return flow.getManagedFunctionReference().getFunctionName();
				}
			}

			// As here, no matching flow
			return null;
		}

		/*
		 * =================== ManagedObjectSourceFlow =======================
		 */

		@Override
		public void linkFunction(String functionName) {
			String managedObjectSourceFunctionName = ManagedObjectSourceContextImpl.this
					.getNamespacedName(functionName);
			if (this.key != null) {
				ManagedObjectSourceContextImpl.this.managingOfficeBuilder.linkFlow(this.key,
						managedObjectSourceFunctionName);
			} else {
				ManagedObjectSourceContextImpl.this.managingOfficeBuilder.linkFlow(this.flowIndex,
						managedObjectSourceFunctionName);
			}
		}
	}

	/**
	 * {@link ManagedObjectFunctionBuilder} implementation.
	 */
	private class ManagedObjectFunctionBuilderImpl<o extends Enum<o>, f extends Enum<f>>
			implements ManagedObjectFunctionBuilder<o, f> {

		/**
		 * {@link ManagedFunctionBuilder}.
		 */
		private final ManagedFunctionBuilder<o, f> functionBuilder;

		/**
		 * Initiate.
		 * 
		 * @param functionBuilder
		 *            {@link ManagedFunctionBuilder}.
		 */
		public ManagedObjectFunctionBuilderImpl(ManagedFunctionBuilder<o, f> functionBuilder) {
			this.functionBuilder = functionBuilder;
		}

		/*
		 * ============== ManagedObjectFunctionBuilder =====================
		 */

		@Override
		public void linkParameter(o key, Class<?> parameterType) {
			this.functionBuilder.linkParameter(key, parameterType);
		}

		@Override
		public void linkParameter(int index, Class<?> parameterType) {
			this.functionBuilder.linkParameter(index, parameterType);
		}

		@Override
		public void linkManagedObject(o key) {
			this.functionBuilder.linkManagedObject(key,
					ManagedObjectSourceContextImpl.this.getInputBoundManagedObjectName(), Object.class);
		}

		@Override
		public void linkManagedObject(int index) {
			this.functionBuilder.linkManagedObject(index,
					ManagedObjectSourceContextImpl.this.getInputBoundManagedObjectName(), Object.class);
		}

		@Override
		public void setResponsibleTeam(String teamName) {
			this.functionBuilder.setResponsibleTeam(ManagedObjectSourceContextImpl.this.getNamespacedName(teamName));
		}

		@Override
		public void setNextFunction(String functionName, Class<?> argumentType) {
			this.functionBuilder.setNextFunction(ManagedObjectSourceContextImpl.this.getNamespacedName(functionName),
					argumentType);
		}

		@Override
		public void linkFlow(f key, String functionName, Class<?> argumentType, boolean isSpawnThreadState) {
			this.functionBuilder.linkFlow(key, ManagedObjectSourceContextImpl.this.getNamespacedName(functionName),
					argumentType, isSpawnThreadState);
		}

		@Override
		public void linkFlow(int flowIndex, String functionName, Class<?> argumentType, boolean isSpawnThreadState) {
			this.functionBuilder.linkFlow(flowIndex,
					ManagedObjectSourceContextImpl.this.getNamespacedName(functionName), argumentType,
					isSpawnThreadState);
		}

		@Override
		public void addEscalation(Class<? extends Throwable> typeOfCause, String functionName) {
			this.functionBuilder.addEscalation(typeOfCause,
					ManagedObjectSourceContextImpl.this.getNamespacedName(functionName));
		}

		@Override
		@SuppressWarnings("unchecked")
		public void linkFlow(f key, ManagedObjectSourceFlow flow, Class<?> argumentType, boolean isSpawnThreadState) {
			String functionName = ((ManagedObjectSourceFlowImpl) flow).getFlowLinkedFunctionName();
			this.functionBuilder.linkFlow(key, functionName, argumentType, isSpawnThreadState);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void linkFlow(int flowIndex, ManagedObjectSourceFlow flow, Class<?> argumentType,
				boolean isSpawnThreadState) {
			String functionName = ((ManagedObjectSourceFlowImpl) flow).getFlowLinkedFunctionName();
			this.functionBuilder.linkFlow(flowIndex, functionName, argumentType, isSpawnThreadState);
		}
	}

}