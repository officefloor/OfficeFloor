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
package net.officefloor.frame.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Loads {@link ManagedObjectSource} for stand-alone use.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceStandAlone {

	/**
	 * Name of the {@link ManagedObjectSource} being loaded.
	 */
	public static final String STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME = "managed.object.source";

	/**
	 * Name of the {@link Office} managing the {@link ManagedObjectSource} being
	 * loaded.
	 */
	public static final String STAND_ALONE_MANAGING_OFFICE_NAME = "office";

	/**
	 * {@link SourceProperties}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * {@link InvokedProcessServicer} instances by their {@link ProcessState}
	 * invocation index.
	 */
	private final Map<Integer, InvokedProcessServicer> processes = new HashMap<>();

	/**
	 * {@link ThreadFactory} instances for the {@link ExecutionStrategy} index.
	 */
	private final Map<Integer, ThreadFactory[]> executionStrategies = new HashMap<>();

	/**
	 * Adds a property for the {@link ManagedObjectSource}.
	 * 
	 * @param name  Name of the property.
	 * @param value Value for the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/**
	 * Instantiates and initialises the {@link ManagedObjectSource}.
	 * 
	 * @param                          <O> Dependency key type.
	 * @param                          <F> Flow key type.
	 * @param                          <MS> {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass Class of the {@link ManagedObjectSource}.
	 * @return Initialised {@link ManagedObjectSource}.
	 * @throws Exception If fails to initialise {@link ManagedObjectSource}.
	 */
	public <O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> MS initManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Create a new instance of the managed object source
		MS moSource = managedObjectSourceClass.getDeclaredConstructor().newInstance();

		// Return the initialised managed object source
		return initManagedObjectSource(moSource);
	}

	/**
	 * Instantiates and initialises the {@link ManagedObjectSource}.
	 * 
	 * @param                     <O> Dependency key type.
	 * @param                     <F> Flow key type.
	 * @param                     <MS> {@link ManagedObjectSource} type.
	 * @param managedObjectSource {@link ManagedObjectSource} instance.
	 * @return Initialised {@link ManagedObjectSource}.
	 * @throws Exception If fails to initialise {@link ManagedObjectSource}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> MS initManagedObjectSource(
			MS managedObjectSource) throws Exception {

		// Create necessary builders
		OfficeFloorBuilder officeFloorBuilder = OfficeFrame.createOfficeFloorBuilder();
		ManagingOfficeBuilder<F> managingOfficeBuilder = officeFloorBuilder
				.addManagedObject(STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME, managedObjectSource)
				.setManagingOffice("STAND ALONE");
		OfficeBuilder officeBuilder = officeFloorBuilder.addOffice(STAND_ALONE_MANAGING_OFFICE_NAME);

		// Create the delegate source context
		SourceContext context = new SourceContextImpl(false, Thread.currentThread().getContextClassLoader());

		// Initialise the managed object source
		ManagedObjectSourceContextImpl sourceContext = new ManagedObjectSourceContextImpl(false,
				STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME, null, this.properties, context, managingOfficeBuilder,
				officeBuilder);
		managedObjectSource.init(sourceContext);

		// Return the initialised managed object source
		return managedObjectSource;
	}

	/**
	 * Starts the {@link ManagedObjectSource}.
	 *
	 * @param                     <O> Dependency key type.
	 * @param                     <F> Flow key type.
	 * @param                     <MS> {@link ManagedObjectSource} type.
	 * @param managedObjectSource {@link ManagedObjectSource}.
	 * @throws Exception If fails to start the {@link ManagedObjectSource}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> void startManagedObjectSource(
			MS managedObjectSource) throws Exception {
		// Start the managed object source
		managedObjectSource.start(new LoadExecuteContext());
	}

	/**
	 * Loads (init and start) the {@link ManagedObjectSource}.
	 *
	 * @param                          <O> Dependency key type.
	 * @param                          <F> Flow key type.
	 * @param                          <MS> {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} class.
	 * @return Loaded {@link ManagedObjectSource}.
	 * @throws Exception If fails to init and start the {@link ManagedObjectSource}.
	 */
	public <O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> MS loadManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Initialise the managed object source
		MS moSource = this.initManagedObjectSource(managedObjectSourceClass);

		// Start the managed object source
		this.startManagedObjectSource(moSource);

		// Return the loaded managed object source
		return moSource;
	}

	/**
	 * Loads (init and start) the {@link ManagedObjectSource}.
	 *
	 * @param                     <O> Dependency key type.
	 * @param                     <F> Flow key type.
	 * @param                     <MS> {@link ManagedObjectSource} type.
	 * @param managedObjectSource {@link ManagedObjectSource} instance.
	 * @return Loaded {@link ManagedObjectSource}.
	 * @throws Exception If fails to init and start the {@link ManagedObjectSource}.
	 */
	public <O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> MS loadManagedObjectSource(
			MS managedObjectSource) throws Exception {

		// Initialise the managed object source
		MS moSource = this.initManagedObjectSource(managedObjectSource);

		// Start the managed object source
		this.startManagedObjectSource(moSource);

		// Return the loaded managed object source
		return moSource;
	}

	/**
	 * Registers the initial {@link ManagedFunction} for the invoked
	 * {@link ProcessState}.
	 * 
	 * @param processKey      Key of the {@link ProcessState}.
	 * @param function        Initial {@link ManagedFunction} for the
	 *                        {@link ProcessState}.
	 * @param functionContext {@link ManagedFunctionContext} for the
	 *                        {@link ManagedFunction}. Allows for mocking the
	 *                        {@link ManagedFunctionContext} to validate
	 *                        functionality for the {@link ManagedFunction}.
	 */
	public void registerInvokeProcessFunction(Enum<?> processKey, ManagedFunction<?, ?> function,
			ManagedFunctionContext<?, ?> functionContext) {
		this.registerInvokeProcessFunction(processKey.ordinal(), function, functionContext);
	}

	/**
	 * Registers the initial {@link ManagedFunction} for the invoked
	 * {@link ProcessState}.
	 * 
	 * @param processIndex    Index of the {@link ProcessState}.
	 * @param function        Initial {@link ManagedFunction} for the
	 *                        {@link ProcessState}.
	 * @param functionContext {@link ManagedFunctionContext} for the
	 *                        {@link ManagedFunction}. Allows for mocking the
	 *                        {@link ManagedFunctionContext} to validate
	 *                        functionality for the {@link ManagedFunction}.
	 */
	public void registerInvokeProcessFunction(int processIndex, ManagedFunction<?, ?> function,
			ManagedFunctionContext<?, ?> functionContext) {
		this.registerInvokeProcessServicer(processIndex, new FunctionInvokedProcessServicer(function, functionContext));
	}

	/**
	 * Registers an {@link InvokedProcessServicer} for the invoked
	 * {@link ProcessState}.
	 * 
	 * @param processKey Key of the {@link ProcessState}.
	 * @param servicer   {@link InvokedProcessServicer}.
	 */
	public void registerInvokeProcessServicer(Enum<?> processKey, InvokedProcessServicer servicer) {
		this.registerInvokeProcessServicer(processKey.ordinal(), servicer);
	}

	/**
	 * Registers an {@link InvokedProcessServicer} for the invoked
	 * {@link ProcessState}.
	 * 
	 * @param processIndex Index of the {@link ProcessState}.
	 * @param servicer     {@link InvokedProcessServicer}.
	 */
	public void registerInvokeProcessServicer(int processIndex, InvokedProcessServicer servicer) {
		this.processes.put(Integer.valueOf(processIndex), servicer);
	}

	/**
	 * Registers an {@link ExecutionStrategy}.
	 * 
	 * @param strategyIndex   Index of the {@link ExecutionStrategy}.
	 * @param threadFactories {@link ThreadFactory} instances for the
	 *                        {@link ExecutionStrategy}.
	 */
	public void registerExecutionStrategy(int strategyIndex, ThreadFactory[] threadFactories) {
		this.executionStrategies.put(Integer.valueOf(strategyIndex), threadFactories);
	}

	/**
	 * {@link InvokedProcessServicer} containing the details for the initial
	 * {@link ManagedFunction} to be executed for the invoked {@link ProcessState}.
	 */
	private class FunctionInvokedProcessServicer implements InvokedProcessServicer {

		/**
		 * {@link ManagedFunction}.
		 */
		public final ManagedFunction<?, ?> function;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		public final ManagedFunctionContext<?, ?> functionContext;

		/**
		 * Initiate.
		 * 
		 * @param function        {@link ManagedFunction}.
		 * @param functionContext {@link ManagedFunctionContext}.
		 */
		public FunctionInvokedProcessServicer(ManagedFunction<?, ?> function,
				ManagedFunctionContext<?, ?> functionContext) {
			this.function = function;
			this.functionContext = functionContext;
		}

		/*
		 * =============== InvokedProcessServicer ==================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void service(int processIndex, Object parameter, ManagedObject managedObject) throws Throwable {
			this.function.execute((ManagedFunctionContext) this.functionContext);
		}
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private class LoadExecuteContext<F extends Enum<F>> implements ManagedObjectExecuteContext<F> {

		/**
		 * Processes the {@link ManagedFunction} for the invoked {@link ProcessState}.
		 * 
		 * @param processIndex  Index of the {@link ProcessState} to invoke.
		 * @param parameter     Parameter to initial {@link ManagedFunction} of the
		 *                      invoked {@link ProcessState}.
		 * @param managedObject {@link ManagedObject} for the {@link ProcessState}.
		 * @param delay         Delay to invoke {@link ProcessState}.
		 * @param callback      {@link FlowCallback}.
		 * @return {@link ProcessManager}.
		 */
		private ProcessManager process(int processIndex, Object parameter, ManagedObject managedObject, long delay,
				FlowCallback callback) {

			// Ignore delay and execute immediately

			// Obtain the details for invoking process
			InvokedProcessServicer servicer = ManagedObjectSourceStandAlone.this.processes
					.get(Integer.valueOf(processIndex));
			if (servicer == null) {
				throw new UnsupportedOperationException(
						"No function configured for process invocation index " + processIndex);
			}

			try {
				try {
					// Service the invoked process
					servicer.service(processIndex, parameter, managedObject);

					// Process complete
					if (callback != null) {
						callback.run(null);
					}

				} catch (Throwable ex) {
					// Determine if handle
					if (callback != null) {
						callback.run(ex);
					} else {
						throw ex; // not handled so propagate
					}
				}
			} catch (Throwable ex) {
				// Handle failure
				if (ex instanceof Error) {
					throw (Error) ex;
				} else if (ex instanceof RuntimeException) {
					throw (RuntimeException) ex;
				} else {
					// Propagate failure
					throw new Error(ex);
				}
			}

			// Return process manager
			return () -> {
			};
		}

		/*
		 * ================ ManagedObjectExecuteContext =====================
		 */

		@Override
		public ProcessManager invokeProcess(F key, Object parameter, ManagedObject managedObject, long delay,
				FlowCallback callback) {
			return this.process(key.ordinal(), parameter, managedObject, delay, callback);
		}

		@Override
		public ProcessManager invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
				FlowCallback callback) {
			return this.process(flowIndex, parameter, managedObject, delay, callback);
		}

		@Override
		public ThreadFactory[] getExecutionStrategy(int executionStrategyIndex) {
			return ManagedObjectSourceStandAlone.this.executionStrategies.get(executionStrategyIndex);
		}
	}

}