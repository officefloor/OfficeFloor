/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.compile.state.autowire.AutoWireStateManagerFactory;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.interrogate.ClassInjections;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogation;
import net.officefloor.plugin.clazz.state.StatePoint;
import net.officefloor.test.module.ModuleAccessible;

/**
 * Abstract JUnit functionality for {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorJUnit implements OfficeFloorJUnit {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	/**
	 * {@link SourceContext}.
	 */
	private SourceContext sourceContext;

	/**
	 * {@link TypeQualifierInterrogation}.
	 */
	private TypeQualifierInterrogation typeQualification;

	/**
	 * Timeout for loading a dependency.
	 */
	private long dependencyLoadTimeout = 3000;

	/**
	 * {@link AutoWireStateManagerFactory}.
	 */
	private Map<String, AutoWireStateManagerFactory> stateManagerFactories = new HashMap<>();

	/**
	 * {@link AutoWireStateManager}.
	 */
	private Map<String, AutoWireStateManager> stateManagers = new HashMap<>();

	/**
	 * Added {@link TestDependencyService} instances.
	 */
	private final List<TestDependencyService> testDependencyServices = new LinkedList<>();

	/**
	 * Indicates if {@link OfficeFloor} for each test.
	 */
	private boolean isEach = false;

	/**
	 * Adds a {@link TestDependencyService}.
	 * 
	 * @param testDependencyService {@link TestDependencyService}.
	 */
	public void addTestDependencyService(TestDependencyService testDependencyService) {
		this.testDependencyServices.add(testDependencyService);
	}

	/**
	 * Undertake JUnit version specific fail.
	 * 
	 * @param message Message for the fail.
	 */
	protected abstract void doFail(String message);

	/**
	 * Undertake JUnit version specific fail.
	 * 
	 * @param cause Cause of the failure.
	 * @return {@link Error} to propagate.
	 */
	protected abstract Error doFail(Throwable cause);

	/**
	 * Specifies the dependency load timeout.
	 * 
	 * @param dependencyLoadTimeout Dependency load timeout.
	 */
	protected void setDependencyLoadTimeout(long dependencyLoadTimeout) {
		this.dependencyLoadTimeout = dependencyLoadTimeout;
	}

	/**
	 * Undertakes the before all logic.
	 * 
	 * @throws Exception If fails.
	 */
	protected void beforeAll() throws Exception {
		this.openOfficeFloor();
	}

	/**
	 * Undertakes the before each logic.
	 * 
	 * @param testInstance Test instance.
	 * @throws Exception If fails.
	 */
	protected void beforeEach(Object testInstance) throws Exception {

		// Determine if for each test
		this.isEach = (this.officeFloor == null);

		// Open OfficeFloor if for each test
		if (this.isEach) {
			this.openOfficeFloor();
		}

		// Undertake dependency injection
		ClassInjections injections = new ClassInjections(testInstance.getClass(), this.sourceContext);

		// Inject the fields
		for (Field field : injections.getInjectionFields()) {

			// Obtain the dependency
			FromOffice fromOffice = field.getAnnotation(FromOffice.class);
			Object dependency = this.getDependency(fromOffice, StatePoint.of(field));

			// Inject the dependency
			ModuleAccessible.setFieldValue(testInstance, field, dependency, "Injecting dependencies for test");
		}

		// Inject the methods
		for (Method method : injections.getInjectionMethods()) {

			// Obtain the list of parameters
			Class<?>[] parameterTypes = method.getParameterTypes();
			Object[] parameters = new Object[parameterTypes.length];
			for (int i = 0; i < parameters.length; i++) {

				// Obtain the dependency
				FromOffice fromOffice = method.getParameters()[i].getAnnotation(FromOffice.class);
				Object dependency = this.getDependency(fromOffice, StatePoint.of(method, i));

				// Load the parameter
				parameters[i] = dependency;
			}

			// Invoke the method
			ModuleAccessible.invokeMethod(testInstance, method, "Inject dependencies for test", parameters);
		}
	}

	/**
	 * Undertakes the after each logic.
	 * 
	 * @throws Exception If fails.
	 */
	protected void afterEach() throws Exception {

		// Close OfficeFloor if for each test
		if (this.isEach) {
			this.closeOfficeFloor();
		}
	}

	/**
	 * Undertakes the after all logic.
	 * 
	 * @throws Exception If fails.
	 */
	protected void afterAll() throws Exception {
		this.closeOfficeFloor();
	}

	/**
	 * Initialises the {@link OfficeFloorCompiler}.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 * @return {@link Consumer} to load the opened {@link OfficeFloor}.
	 */
	protected Consumer<OfficeFloor> initialiseOfficeFloorCompiler(OfficeFloorCompiler compiler) {

		// Provide means to get dependencies
		compiler.addAutoWireStateManagerVisitor(
				(officeName, factory) -> this.stateManagerFactories.put(officeName, factory));

		// Provide details for test injection
		this.sourceContext = compiler.createRootSourceContext();
		this.typeQualification = new TypeQualifierInterrogation(this.sourceContext);

		// Return consumer to load OfficeFloor
		return (officeFloor) -> this.officeFloor = officeFloor;
	}

	/**
	 * Opens the {@link OfficeFloor}.
	 * 
	 * @throws Exception If fails to open the {@link OfficeFloor}.
	 */
	protected void openOfficeFloor() throws Exception {

		// Open the OfficeFloor
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		this.initialiseOfficeFloorCompiler(compiler);
		this.officeFloor = compiler.compile("OfficeFloor");
		try {

			// Open the office
			this.officeFloor.openOfficeFloor();

		} catch (Exception ex) {
			// Ensure close and clear the OfficeFloor
			try {
				this.officeFloor.closeOfficeFloor();
			} catch (Throwable ignore) {
				// Ignore failure to close as doing best attempt to clean up
			} finally {
				this.officeFloor = null;
			}

			// Propagate the failure
			throw ex;
		}
	}

	/**
	 * Determines if the dependency is available.
	 * 
	 * @param fromOffice {@link FromOffice}.
	 * @param statePoint {@link StatePoint}.
	 * @return <code>true</code> if the dependency is available.
	 * @throws Exception If fails to determine dependency available.
	 */
	protected boolean isDependencyAvailable(FromOffice fromOffice, StatePoint statePoint) throws Exception {

		// Obtain the qualifier
		String qualifier = this.typeQualification.extractTypeQualifier(statePoint);

		// Obtain the object type
		Class<?> objectType = statePoint.getField() != null ? statePoint.getField().getType()
				: statePoint.getExecutable().getParameterTypes()[statePoint.getExecutableParameterIndex()];

		// Obtain the state manager
		AutoWireStateManager stateManager = this.getStateManager(fromOffice);

		// Determine if extra test dependency
		TestDependencyServiceContext serviceContext = new TestDependencyServiceContextImpl(qualifier, objectType,
				stateManager);
		for (TestDependencyService service : this.getTestDependencyServices()) {
			if (service.isObjectAvailable(serviceContext)) {
				return true; // extra test dependency available
			}
		}

		// Return whether available
		return stateManager.isObjectAvailable(qualifier, objectType);
	}

	/**
	 * Obtains the qualifier for the {@link StatePoint}.
	 * 
	 * @param fromOffice {@link FromOffice}.
	 * @param statePoint {@link StatePoint}.
	 * @return Dependency.
	 * @throws Exception If fails to obtain the dependency.
	 */
	protected Object getDependency(FromOffice fromOffice, StatePoint statePoint) throws Exception {

		// Obtain the qualifier
		String qualifier = this.typeQualification.extractTypeQualifier(statePoint);

		// Obtain the object type
		Class<?> objectType = statePoint.getField() != null ? statePoint.getField().getType()
				: statePoint.getExecutable().getParameterTypes()[statePoint.getExecutableParameterIndex()];

		// Obtain the state manager
		AutoWireStateManager stateManager = this.getStateManager(fromOffice);

		try {

			// Determine if extra test dependency
			TestDependencyServiceContext serviceContext = new TestDependencyServiceContextImpl(qualifier, objectType,
					stateManager);
			for (TestDependencyService service : this.getTestDependencyServices()) {
				if (service.isObjectAvailable(serviceContext)) {
					return service.getObject(serviceContext);
				}
			}

			// Return the dependency
			return stateManager.getObject(qualifier, objectType, this.dependencyLoadTimeout);
		} catch (Exception ex) {
			throw ex;
		} catch (Throwable ex) {
			throw this.doFail(ex);
		}
	}

	/**
	 * Obtains the ordered list of {@link TestDependencyService} instances.
	 * 
	 * @return Ordered list of {@link TestDependencyService} instances.
	 */
	protected List<TestDependencyService> getTestDependencyServices() {

		// Create list of test dependencies
		List<TestDependencyService> services = new ArrayList<>();

		// Test added dependencies take priority
		services.addAll(this.testDependencyServices);

		// Next is loaded services
		for (TestDependencyService testDependencyService : this.sourceContext
				.loadOptionalServices(TestDependencyServiceFactory.class)) {
			services.add(testDependencyService);
		}

		// Return the dependencies
		return services;
	}

	/**
	 * Closes the {@link OfficeFloor}.
	 * 
	 * @throws Exception If fails to close the {@link OfficeFloor}.
	 */
	protected void closeOfficeFloor() throws Exception {

		// Close the OfficeFloor and ensure released
		try {
			if (this.officeFloor != null) {

				// Close the state managers
				for (AutoWireStateManager stateManager : this.stateManagers.values()) {
					stateManager.close();
				}

				// Close the OfficeFloor
				this.officeFloor.closeOfficeFloor();
			}
		} finally {
			this.officeFloor = null;
		}
	}

	/**
	 * Obtains the {@link AutoWireStateManager} for the {@link FromOffice}.
	 * 
	 * @param fromOffice {@link FromOffice}. May be <code>null</code> to use default
	 *                   {@link Office}.
	 * @return {@link AutoWireStateManager} for the {@link FromOffice}.
	 */
	private AutoWireStateManager getStateManager(FromOffice fromOffice) {

		// Obtain the office name
		String officeName = fromOffice != null ? fromOffice.value() : ApplicationOfficeFloorSource.OFFICE_NAME;

		// Determine if already have
		AutoWireStateManager stateManager = this.stateManagers.get(officeName);
		if (stateManager != null) {
			return stateManager;
		}

		// Attempt to create the state manager
		AutoWireStateManagerFactory factory = this.stateManagerFactories.get(officeName);
		if (factory == null) {
			this.doFail("No " + Office.class.getSimpleName() + " by name " + officeName);
		}
		stateManager = factory.createAutoWireStateManager();

		// Register and return the state manager
		this.stateManagers.put(officeName, stateManager);
		return stateManager;
	}

	/*
	 * ====================== OfficeFloorJUnit ==========================
	 */

	@Override
	public OfficeFloor getOfficeFloor() {
		if (this.officeFloor == null) {
			throw new IllegalStateException("OfficeFloor only available within test");
		}
		return this.officeFloor;
	}

	@Override
	public void invokeProcess(String functionName, Object parameter) {
		this.invokeProcess(functionName, parameter, 3000);
	}

	@Override
	public void invokeProcess(String functionName, Object parameter, long waitTime) {
		this.invokeProcess("OFFICE", functionName, parameter, waitTime);
	}

	@Override
	public void invokeProcess(String officeName, String functionName, Object parameter, long waitTime) {

		// Obtain the OfficeFloor
		OfficeFloor officeFloor = this.getOfficeFloor();

		try {
			// Obtain the function
			FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager(functionName);

			// Invoke the function (ensuring completes within reasonable time)
			long startTimestamp = System.currentTimeMillis();
			boolean[] isComplete = new boolean[] { false };
			Throwable[] failure = new Throwable[] { null };
			function.invokeProcess(parameter, (exception) -> {
				synchronized (isComplete) {
					failure[0] = exception;
					isComplete[0] = true;
					isComplete.notify(); // wake up immediately
				}
			});
			synchronized (isComplete) {
				while (!isComplete[0]) {

					// Determine if timed out
					long currentTimestamp = System.currentTimeMillis();
					if ((startTimestamp + waitTime) < currentTimestamp) {
						throw new Exception("Timed out waiting on process (" + officeName + "." + functionName
								+ ") to complete (" + (currentTimestamp - startTimestamp) + " milliseconds)");
					}

					// Sleep some time
					isComplete.wait(100);
				}

				// Determine if failure
				if (failure[0] != null) {
					throw failure[0];
				}
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Error error) {
			throw error;
		} catch (Throwable ex) {
			throw this.doFail(ex);
		}
	}

	/**
	 * {@link TestDependencyServiceContext} implementation.
	 */
	private class TestDependencyServiceContextImpl implements TestDependencyServiceContext {

		/**
		 * Qualifier. May be <code>null</code>.
		 */
		private final String qualifier;

		/**
		 * Object type.
		 */
		private final Class<?> objectType;

		/**
		 * {@link AutoWireStateManager}.
		 */
		private final AutoWireStateManager stateManager;

		/**
		 * Instantiate.
		 * 
		 * @param qualifier    Qualifier.
		 * @param objectType   Object type.
		 * @param stateManager {@link AutoWireStateManager}.
		 */
		private TestDependencyServiceContextImpl(String qualifier, Class<?> objectType,
				AutoWireStateManager stateManager) {
			this.qualifier = qualifier;
			this.objectType = objectType;
			this.stateManager = stateManager;
		}

		/*
		 * ================= TestDependencyServiceContext ==================
		 */

		@Override
		public String getQualifier() {
			return this.qualifier;
		}

		@Override
		public Class<?> getObjectType() {
			return this.objectType;
		}

		@Override
		public AutoWireStateManager getStateManager() {
			return this.stateManager;
		}

		@Override
		public long getLoadTimeout() {
			return AbstractOfficeFloorJUnit.this.dependencyLoadTimeout;
		}
	}

}
