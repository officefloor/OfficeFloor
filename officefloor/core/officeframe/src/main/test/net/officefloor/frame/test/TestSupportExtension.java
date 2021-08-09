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

package net.officefloor.frame.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import net.officefloor.test.module.ModuleAccessible;

/**
 * {@link Extension} for {@link TestSupport}.
 * 
 * @author Daniel Sagenschneider
 */
public class TestSupportExtension implements TestInstancePostProcessor, BeforeEachCallback, BeforeTestExecutionCallback,
		AfterTestExecutionCallback, AfterEachCallback {

	/**
	 * {@link Namespace} for {@link TestSupportExtension}.
	 */
	private static final Namespace NAMESPACE = Namespace.create(TestSupportExtension.class);

	/**
	 * Obtains the particular {@link TestSupport}.
	 * 
	 * @param <T>             {@link TestSupport} type.
	 * @param testSupportType {@link TestSupport} type.
	 * @param context         {@link ExtensionContext}.
	 * @return Particular {@link TestSupport}.
	 */
	public static <T extends TestSupport> T getTestSupport(Class<T> testSupportType, ExtensionContext context) {

		// Obtain the test support state
		TestSupportState testSupportState = getTestSupportState(context);

		// Not found, so create instance
		Function<Throwable, T> fail = (ex) -> Assertions.fail("Failed to instantiate "
				+ TestSupport.class.getSimpleName() + " " + testSupportType.getName() + " by default constructor", ex);
		try {

			// Attempt to find the particular existing test support
			T testSupport = testSupportState.getTestSupport(testSupportType);
			if (testSupport == null) {

				// Create and register the instance
				testSupport = testSupportType.getConstructor().newInstance();
				testSupportState.testSupports.add(testSupport);
			}

			// Ensure the test support is initialsied
			testSupportState.init(testSupport, context);

			// Return the instance
			return testSupport;

		} catch (InvocationTargetException ex) {
			return fail.apply(ex.getCause());
		} catch (Exception ex) {
			return fail.apply(ex);
		}
	}

	/**
	 * Obtains the {@link TestSupportState} for the test.
	 * 
	 * @param context {@link ExtensionContext}.
	 * @return {@link TestSupportState} for the test.
	 */
	private static TestSupportState getTestSupportState(ExtensionContext context) {
		Store store = context.getStore(NAMESPACE);
		return (TestSupportState) store.getOrComputeIfAbsent(context.getRequiredTestClass(),
				key -> new TestSupportState());
	}

	/**
	 * Action on the particular {@link Extension} type.
	 */
	@FunctionalInterface
	private static interface ExtensionAction<E extends Extension> {
		void action(E extension) throws Exception;
	}

	/**
	 * Actions the particular {@link Extension} type.
	 * 
	 * @param <E>           {@link Extension} type.
	 * @param context       {@link ExtensionContext}.
	 * @param extensionType {@link Extension} type.
	 * @param action        {@link ExtensionAction}.
	 * @throws Exception If fails action.
	 */
	@SuppressWarnings("unchecked")
	private <E extends Extension> void action(ExtensionContext context, Class<E> extensionType,
			ExtensionAction<E> action) throws Exception {

		// Obtain the list of extensions
		TestSupportState testSupportState = getTestSupportState(context);

		// Action each if supports extension
		for (TestSupport testSupport : testSupportState.testSupports) {
			if (extensionType.isAssignableFrom(testSupport.getClass())) {
				E extension = (E) testSupport;
				action.action(extension);
			}
		}
	}

	/*
	 * ==================== TestInstancePostProcessor ==========================
	 */

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

		// Obtain the test support state for test
		TestSupportState testSupportState = getTestSupportState(context);

		// Reset for this test
		testSupportState.reset();

		// Interrogate for test support instances
		Class<?> testClass = testInstance.getClass();
		do {
			for (Field field : testClass.getDeclaredFields()) {
				if (TestSupport.class.isAssignableFrom(field.getType())) {

					// Obtain the test support
					TestSupport testSupport = (TestSupport) ModuleAccessible.getFieldValue(testInstance, field,
							"Processing " + TestSupport.class.getSimpleName() + " instances");

					// Register the test support instance from test
					if (testSupport != null) {
						testSupportState.testSupports.add(testSupport);
					}
				}
			}

			// Check super class
			testClass = testClass.getSuperclass();
		} while (testClass != null);

		// Initialise the test supports
		for (TestSupport testSupport : new ArrayList<>(testSupportState.testSupports)) {
			testSupportState.init(testSupport, context);
		}
	}

	/**
	 * ========================= Extension Lifecycle ===============================
	 */

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		this.action(context, BeforeEachCallback.class, extension -> extension.beforeEach(context));
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		this.action(context, BeforeTestExecutionCallback.class, extension -> extension.beforeTestExecution(context));
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		this.action(context, AfterTestExecutionCallback.class, extension -> extension.afterTestExecution(context));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		this.action(context, AfterEachCallback.class, extension -> extension.afterEach(context));
	}

	/**
	 * State of {@link TestSupport} for a test class.
	 */
	private static class TestSupportState {

		/**
		 * {@link TestSupport} instances required for testing.
		 */
		private List<TestSupport> testSupports = new LinkedList<>();

		/**
		 * Initialised {@link TestSupport} instances.
		 */
		private Set<Class<?>> initialised = new HashSet<>();

		/**
		 * Resets for next test.
		 */
		public void reset() {
			this.testSupports.clear();
			this.initialised.clear();
		}

		/**
		 * Ensures the {@link TestSupport} is initialised.
		 * 
		 * @param testSupport {@link TestSupport} to ensure initialised.
		 * @param context     {@link ExtensionContext}.
		 * @throws Exception If fails initialising.
		 */
		public void init(TestSupport testSupport, ExtensionContext context) throws Exception {

			// Only initialise once
			Class<?> testSupportClass = testSupport.getClass();
			if (this.initialised.contains(testSupportClass)) {
				return;
			}

			// Initialise and register initialised
			testSupport.init(context);
			this.initialised.add(testSupportClass);
		}

		/***
		 * Obtains the particular {@link TestSupport}.
		 * 
		 * @param <T>             Type of {@link TestSupport}.
		 * @param testSupportType {@link Class} of the {@link TestSupport}.
		 * @return Particular {@link TestSupport} or <code>null</code> if not
		 *         registered.
		 */
		@SuppressWarnings("unchecked")
		public <T extends TestSupport> T getTestSupport(Class<T> testSupportType) {

			// Determine if registered
			for (TestSupport testSupport : this.testSupports) {
				if (testSupportType.equals(testSupport.getClass())) {
					return (T) testSupport;
				}
			}

			// As here, not found
			return null;
		}
	}

}
