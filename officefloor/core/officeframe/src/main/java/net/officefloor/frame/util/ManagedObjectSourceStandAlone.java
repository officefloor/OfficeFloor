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

package net.officefloor.frame.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagingOfficeBuilderImpl;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Loads {@link ManagedObjectSource} for stand-alone use.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceStandAlone {

	/**
	 * Name of the {@link Office} managing the {@link ManagedObjectSource} being
	 * loaded.
	 */
	public static final String STAND_ALONE_MANAGING_OFFICE_NAME = "office";

	/**
	 * Profiles.
	 */
	private final List<String> profiles = new LinkedList<>();

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
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeBuilderImpl<?> managingOffice = new ManagingOfficeBuilderImpl<>(
			STAND_ALONE_MANAGING_OFFICE_NAME);

	/**
	 * {@link ClockFactory}.
	 */
	private ClockFactory clockFactory = new MockClockFactory();

	/**
	 * Adds a profile.
	 * 
	 * @param profile Profile.
	 */
	public void addProfile(String profile) {
		this.profiles.add(profile);
	}

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
	 * Specifies the {@link ClockFactory}.
	 * 
	 * @param clockFactory {@link ClockFactory}.
	 */
	public void setClockFactory(ClockFactory clockFactory) {
		this.clockFactory = clockFactory;
	}

	/**
	 * Obtains the {@link ManagingOfficeBuilder}.
	 * 
	 * @return {@link ManagingOfficeBuilder}.
	 */
	public ManagingOfficeBuilder<?> getManagingOffice() {
		return this.managingOffice;
	}

	/**
	 * Instantiates and initialises the {@link ManagedObjectSource}.
	 * 
	 * @param <O>                      Dependency key type.
	 * @param <F>                      Flow key type.
	 * @param <MS>                     {@link ManagedObjectSource} type.
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
	 * @param <O>                 Dependency key type.
	 * @param <F>                 Flow key type.
	 * @param <MS>                {@link ManagedObjectSource} type.
	 * @param managedObjectSource {@link ManagedObjectSource} instance.
	 * @return Initialised {@link ManagedObjectSource}.
	 * @throws Exception If fails to initialise {@link ManagedObjectSource}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> MS initManagedObjectSource(
			MS managedObjectSource) throws Exception {

		// Obtain the managed object source name
		final String managedObjectSourceName = this.getClass().getName();

		// Create necessary builders
		OfficeFloorBuilder officeFloorBuilder = OfficeFrame.createOfficeFloorBuilder();
		ManagingOfficeBuilder<F> managingOfficeBuilder = officeFloorBuilder
				.addManagedObject(managedObjectSourceName, managedObjectSource).setManagingOffice("STAND ALONE");
		OfficeBuilder officeBuilder = officeFloorBuilder.addOffice(STAND_ALONE_MANAGING_OFFICE_NAME);

		// Create the delegate source context
		SourceContext context = new SourceContextImpl(this.getClass().getName(), false, new String[0],
				Thread.currentThread().getContextClassLoader(), this.clockFactory);

		// Initialise the managed object source
		ManagedObjectSourceContextImpl sourceContext = new ManagedObjectSourceContextImpl(managedObjectSourceName,
				false, managedObjectSourceName, this.managingOffice,
				this.profiles.toArray(new String[this.profiles.size()]), this.properties, context,
				managingOfficeBuilder, officeBuilder, new Object());
		managedObjectSource.init(sourceContext);

		// Return the initialised managed object source
		return managedObjectSource;
	}

	/**
	 * Starts the {@link ManagedObjectSource}.
	 *
	 * @param <O>                 Dependency key type.
	 * @param <F>                 Flow key type.
	 * @param <MS>                {@link ManagedObjectSource} type.
	 * @param managedObjectSource {@link ManagedObjectSource}.
	 * @throws Exception If fails to start the {@link ManagedObjectSource}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> void startManagedObjectSource(
			MS managedObjectSource) throws Exception {

		// Obtain the logger
		final String managedObjectSourceName = this.getClass().getName();
		Logger logger = OfficeFrame.getLogger(managedObjectSourceName);

		// Start the managed object source
		managedObjectSource.start(new LoadExecuteContext(logger));
	}

	/**
	 * Loads (init and start) the {@link ManagedObjectSource}.
	 *
	 * @param <O>                      Dependency key type.
	 * @param <F>                      Flow key type.
	 * @param <MS>                     {@link ManagedObjectSource} type.
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
	 * @param <O>                 Dependency key type.
	 * @param <F>                 Flow key type.
	 * @param <MS>                {@link ManagedObjectSource} type.
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
	private class LoadExecuteContext<F extends Enum<F>>
			implements ManagedObjectExecuteContext<F>, ManagedObjectServiceContext<F>, ManagedObjectStartupProcess {

		/**
		 * {@link Logger}.
		 */
		private final Logger logger;

		/**
		 * Instantiate.
		 * 
		 * @param logger {@link Logger}.
		 */
		private LoadExecuteContext(Logger logger) {
			this.logger = logger;
		}

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
				return JUnitAgnosticAssert.fail(ex);
			}

			// Return process manager
			return () -> {
			};
		}

		/*
		 * ================ ManagedObjectExecuteContext =====================
		 */

		@Override
		public Logger getLogger() {
			return this.logger;
		}

		@Override
		public ThreadFactory[] getExecutionStrategy(int executionStrategyIndex) {
			return ManagedObjectSourceStandAlone.this.executionStrategies.get(executionStrategyIndex);
		}

		@Override
		public ManagedObjectStartupProcess invokeStartupProcess(F key, Object parameter, ManagedObject managedObject,
				FlowCallback callback) throws IllegalArgumentException {
			this.process(key.ordinal(), parameter, managedObject, 0, callback);
			return this;
		}

		@Override
		public ManagedObjectStartupProcess invokeStartupProcess(int flowIndex, Object parameter,
				ManagedObject managedObject, FlowCallback callback) throws IllegalArgumentException {
			this.process(flowIndex, parameter, managedObject, 0, callback);
			return this;
		}

		@Override
		public void addService(ManagedObjectService<F> service) {
			try {
				// Start service immediately
				service.startServicing(this);
			} catch (Exception ex) {
				JUnitAgnosticAssert.fail(ex);
			}
		}

		/*
		 * ================ ManagedObjectServiceContext =====================
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

		/*
		 * =================== ManagedObjectStartupProcess =================
		 */

		@Override
		public void setConcurrent(boolean isConcurrent) {
			// Do nothing, as already invoked.
			// Plus stand-alone so no concurrency co-ordination required.
		}
	}

}
