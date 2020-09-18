/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

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
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceFlow;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionInvocationImpl;
import net.officefloor.frame.impl.construct.managedobjectpool.ManagedObjectPoolBuilderImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionInvocation;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectFunctionDependencyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectPoolConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectServiceReady;

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
	 * {@link ManagedObjectFunctionDependency} instances.
	 */
	private final List<ManagedObjectFunctionDependencyImpl> functionDependencies = new LinkedList<>();

	/**
	 * {@link ManagedFunctionInvocation} instances for starting the
	 * {@link ManagedObjectSource}.
	 */
	private final List<ManagedFunctionInvocation> startupFunctions = new LinkedList<>();

	/**
	 * Possible issues in sourcing the {@link ManagedObjectSource}.
	 */
	private final List<String> issues = new LinkedList<>();

	/**
	 * Object to notify on start up completion.
	 */
	private final Object startupNotify;

	/**
	 * {@link ManagedObjectServiceReady} instances.
	 */
	private List<ManagedObjectServiceReady> serviceReadiness = new LinkedList<>();

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
	 * @param managedObjectSourceName     Name of the {@link ManagedObjectSource}.
	 * @param isLoadingType               Indicates if loading type.
	 * @param managedObjectName           Name of the {@link ManagedObject}.
	 * @param managingOfficeConfiguration {@link ManagingOfficeConfiguration}.
	 * @param additionalProfiles          Additional profiles.
	 * @param properties                  Properties.
	 * @param sourceContext               Delegate {@link SourceContext}.
	 * @param managingOfficeBuilder       {@link ManagingOfficeBuilder}.
	 * @param officeBuilder               {@link OfficeBuilder} for the office using
	 *                                    the {@link ManagedObjectSource}.
	 * @param startupNotify               Object to notify on start up completion.
	 */
	public ManagedObjectSourceContextImpl(String managedObjectSourceName, boolean isLoadingType,
			String managedObjectName, ManagingOfficeConfiguration<F> managingOfficeConfiguration,
			String[] additionalProfiles, SourceProperties properties, SourceContext sourceContext,
			ManagingOfficeBuilder<F> managingOfficeBuilder, OfficeBuilder officeBuilder, Object startupNotify) {
		super(managedObjectSourceName, isLoadingType, additionalProfiles, sourceContext, properties);
		this.managedObjectName = managedObjectName;
		this.managingOfficeConfiguration = managingOfficeConfiguration;
		this.managingOfficeBuilder = managingOfficeBuilder;
		this.officeBuilder = officeBuilder;
		this.startupNotify = startupNotify;
	}

	/**
	 * Indicates that the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} method has
	 * completed.
	 * 
	 * @return Listing of issues in sourcing the {@link ManagedObjectSource}.
	 */
	public String[] flagInitOver() {

		// Disallow further configuration
		this.managingOfficeBuilder = null;
		this.officeBuilder = null;

		// Return any issues
		return this.issues.toArray(new String[this.issues.size()]);
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
	 * Obtains the {@link ManagedObjectFunctionDependencyImpl} instances.
	 * 
	 * @return {@link ManagedObjectFunctionDependencyImpl} instances.
	 */
	public ManagedObjectFunctionDependencyImpl[] getManagedObjectFunctionDependencies() {
		return this.functionDependencies
				.toArray(new ManagedObjectFunctionDependencyImpl[this.functionDependencies.size()]);
	}

	/**
	 * Obtains the startup {@link ManagedFunctionInvocation} instances.
	 * 
	 * @return Startup {@link ManagedFunctionInvocation} instances.
	 */
	public ManagedFunctionInvocation[] getStartupFunctions() {
		return this.startupFunctions.toArray(new ManagedFunctionInvocation[this.startupFunctions.size()]);
	}

	/**
	 * Obtains the {@link ManagedObjectServiceReady} instances.
	 * 
	 * @return {@link ManagedObjectServiceReady} instances.
	 */
	public ManagedObjectServiceReady[] getServiceReadiness() {
		return this.serviceReadiness.toArray(new ManagedObjectServiceReady[this.serviceReadiness.size()]);
	}

	/**
	 * Obtains the input bound {@link ManagedObject} name.
	 * 
	 * @return Input bound {@link ManagedObject} name.
	 */
	private String getInputBoundManagedObjectName() {
		InputManagedObjectConfiguration<?> inputConfiguration = this.managingOfficeConfiguration
				.getInputManagedObjectConfiguration();
		return (inputConfiguration != null ? inputConfiguration.getBoundManagedObjectName() : this.managedObjectName);
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
	public ManagedObjectFunctionDependency addFunctionDependency(String name, Class<?> objectType) {

		// Ensure have name
		if ((name == null) || (name.trim().length() == 0)) {
			throw new IllegalArgumentException(
					"Must provide " + ManagedObjectFunctionDependency.class.getSimpleName() + " name");
		}

		// Ensure have type
		if (objectType == null) {
			throw new IllegalArgumentException(
					"Must provide " + ManagedObjectFunctionDependency.class.getSimpleName() + " type");
		}

		// Attempt to determine the scope managed object for dependency
		String scopeManagedObjectName = null;
		for (ManagedObjectFunctionDependencyConfiguration configuration : this.managingOfficeConfiguration
				.getFunctionDependencyConfiguration()) {
			if (name.equals(configuration.getFunctionObjectName())) {
				scopeManagedObjectName = configuration.getScopeManagedObjectName();
			}
		}

		// Add issue if no scope managed object
		if (scopeManagedObjectName == null) {
			this.issues.add("No dependency configured for " + ManagedObjectFunctionDependency.class.getSimpleName()
					+ " '" + name + "'");
		}

		// Create the function dependency
		ManagedObjectFunctionDependencyImpl dependency = new ManagedObjectFunctionDependencyImpl(name, objectType,
				scopeManagedObjectName);

		// Register the function dependency
		this.functionDependencies.add(dependency);

		// Return the function dependency
		return dependency;
	}

	@Override
	public ManagedObjectStartupCompletion createStartupCompletion() {
		ManagedObjectStartupCompletionImpl startupCompletion = new ManagedObjectStartupCompletionImpl();
		this.serviceReadiness.add(startupCompletion);
		return startupCompletion;
	}

	@Override
	public void addStartupFunction(String functionName, Object parameter) {
		String moFunctionName = this.getNamespacedName(functionName);
		this.startupFunctions.add(new ManagedFunctionInvocationImpl(moFunctionName, parameter));
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
	 * @param name Name to add name space.
	 * @return Name including the name space.
	 */
	private String getNamespacedName(String name) {
		return OfficeBuilderImpl.getNamespacedName(this.managedObjectName, name);
	}

	/**
	 * {@link ManagedObjectFunctionDependency} implementation.
	 */
	public static class ManagedObjectFunctionDependencyImpl implements ManagedObjectFunctionDependency {

		/**
		 * Name of the {@link ManagedObjectFunctionDependency}.
		 */
		private final String functionObjectName;

		/**
		 * Type of {@link ManagedObjectFunctionDependency}.
		 */
		private final Class<?> type;

		/**
		 * Name of the scoped {@link ManagedObject} for this dependency.
		 */
		private final String scopeManagedObjectName;

		/**
		 * Instantiate.
		 * 
		 * @param functionObjectName     Name of the
		 *                               {@link ManagedObjectFunctionDependency}.
		 * @param type                   Type of
		 *                               {@link ManagedObjectFunctionDependency}.
		 * @param scopeManagedObjectName Name of the scoped {@link ManagedObject} for
		 *                               this dependency.
		 */
		private ManagedObjectFunctionDependencyImpl(String functionObjectName, Class<?> type,
				String scopeManagedObjectName) {
			this.functionObjectName = functionObjectName;
			this.type = type;
			this.scopeManagedObjectName = scopeManagedObjectName;
		}

		/**
		 * Obtains the name of the {@link ManagedObjectFunctionDependency}.
		 * 
		 * @return Name of the {@link ManagedObjectFunctionDependency}.
		 */
		public String getFunctionObjectName() {
			return this.functionObjectName;
		}

		/**
		 * Obtains the type of {@link ManagedObjectFunctionDependency}.
		 * 
		 * @return Type of {@link ManagedObjectFunctionDependency}.
		 */
		public Class<?> getFunctionObjectType() {
			return this.type;
		}
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
		 * @param key       {@link Flow} key.
		 * @param flowIndex {@link Flow} index.
		 */
		private ManagedObjectSourceFlowImpl(F key, int flowIndex) {
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
		 * @param functionBuilder {@link ManagedFunctionBuilder}.
		 */
		private ManagedObjectFunctionBuilderImpl(ManagedFunctionBuilder<o, f> functionBuilder) {
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
		public void linkObject(o key, ManagedObjectFunctionDependency dependency) {
			this.linkObject(dependency, (scopeManagedObjectName, objectType) -> {
				this.functionBuilder.linkManagedObject(key, scopeManagedObjectName, objectType);
			});
		}

		@Override
		public void linkObject(int index, ManagedObjectFunctionDependency dependency) {
			this.linkObject(dependency, (scopeManagedObjectName, objectType) -> {
				this.functionBuilder.linkManagedObject(index, scopeManagedObjectName, objectType);
			});
		}

		/**
		 * Links the {@link ManagedObjectFunctionDependency}.
		 * 
		 * @param dependency {@link ManagedObjectFunctionDependency}.
		 * @param linker     Linker for the {@link ManagedObjectFunctionDependency}.
		 */
		private void linkObject(ManagedObjectFunctionDependency dependency, BiConsumer<String, Class<?>> linker) {

			// Obtain the mapped managed function
			if (!(dependency instanceof ManagedObjectFunctionDependencyImpl)) {
				throw new IllegalArgumentException(ManagedObjectFunctionDependency.class.getSimpleName()
						+ " must be added from " + ManagedObjectSourceContext.class.getSimpleName());
			}
			ManagedObjectFunctionDependencyImpl dependencyImpl = (ManagedObjectFunctionDependencyImpl) dependency;

			// Link the managed object function dependency
			linker.accept(dependencyImpl.scopeManagedObjectName, dependencyImpl.type);
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

	/**
	 * {@link ManagedObjectStartupCompletion} implementation.
	 */
	private class ManagedObjectStartupCompletionImpl
			implements ManagedObjectStartupCompletion, ManagedObjectServiceReady {

		/**
		 * Indicates if complete.
		 */
		private boolean isComplete = false;

		/**
		 * Possible start up failure.
		 */
		private Throwable startupFailure = null;

		/*
		 * ===================== ManagedObjectStartupCompletion ===================
		 */

		@Override
		public void complete() {
			synchronized (ManagedObjectSourceContextImpl.this.startupNotify) {

				// Flag complete
				this.isComplete = true;

				// Notify to continue start up
				ManagedObjectSourceContextImpl.this.startupNotify.notify();
			}
		}

		@Override
		public void failOpen(Throwable cause) {
			synchronized (ManagedObjectSourceContextImpl.this.startupNotify) {

				// Flag failure
				this.startupFailure = cause;

				// Notify to fail start up
				ManagedObjectSourceContextImpl.this.startupNotify.notify();
			}
		}

		/*
		 * ======================== ManagedObjectServiceReady =====================
		 */

		@Override
		public boolean isServiceReady() throws Exception {
			synchronized (ManagedObjectSourceContextImpl.this.startupNotify) {

				// Propagate possible failure
				if (this.startupFailure != null) {
					if (this.startupFailure instanceof Exception) {
						throw (Exception) this.startupFailure;
					} else if (this.startupFailure instanceof Error) {
						throw (Error) this.startupFailure;
					} else {
						throw new Exception(this.startupFailure);
					}
				}

				// Return whether complete
				return this.isComplete;
			}
		}
	}

}
