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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * {@link Extension} for running {@link OfficeFloor} around tests.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExtension implements OfficeFloorJUnit, ParameterResolver, BeforeAllCallback, BeforeEachCallback,
		AfterEachCallback, AfterAllCallback {

	/**
	 * {@link Namespace} for {@link OfficeFloorExtension}.
	 */
	private static final Namespace NAMESPACE = Namespace.create(OfficeFloorExtension.class);

	/**
	 * {@link SingletonOfficeFloorJUnit} for testing.
	 */
	private SingletonOfficeFloorJUnit singleton;

	/**
	 * Provides an override to object load timeout.
	 */
	private long overrideObjectLoadTimeout = -1;

	/**
	 * Allow overriding the default timeout on loading dependencies.
	 * 
	 * @param dependencyLoadTimeout Dependency load timeout.
	 * @return <code>this</code> for builder pattern with {@link RegisterExtension}
	 *         {@link Field} instantiation.
	 */
	public OfficeFloorExtension dependencyLoadTimeout(long dependencyLoadTimeout) {
		this.overrideObjectLoadTimeout = dependencyLoadTimeout;
		return this;
	}

	/**
	 * Obtains the delegate {@link SingletonOfficeFloorJUnit}.
	 * 
	 * @param context {@link ExtensionContext}. May be <code>null</code>.
	 * @return {@link SingletonOfficeFloorJUnit}.
	 */
	private SingletonOfficeFloorJUnit getSingleton(ExtensionContext context) {

		// Obtain the existing extension
		Store store = null;
		Class<?> testClass = null;
		SingletonOfficeFloorJUnit existing = null;
		if (context != null) {
			store = context.getStore(NAMESPACE);
			testClass = context.getRequiredTestClass();
			existing = store.get(testClass, SingletonOfficeFloorJUnit.class);
		}

		// Determine if have cached singleton
		if (this.singleton != null) {
			// Determine if have existing
			if (existing != null) {
				// Ensure same (otherwise indeterminant behaviour)
				assertSame(existing, this.singleton,
						"INVALID TEST STATE: cached extension does not match context extension");
			}

		} else {
			// Determine if have existing
			if (existing != null) {
				// Use the existing
				this.singleton = existing;

			} else {
				// Use this
				this.singleton = new SingletonOfficeFloorJUnit();
			}
		}

		// Ensure register existing if not yet registered
		if ((existing == null) && (store != null)) {
			store.put(testClass, this.singleton);
		}

		// Ensure override the default timeout
		if (this.overrideObjectLoadTimeout > 0) {
			this.singleton.setDependencyLoadTimeout(this.overrideObjectLoadTimeout);
		}

		// Undertake action
		return this.singleton;
	}

	/*
	 * ==================== OfficeFloorJUnit ====================
	 */

	@Override
	public OfficeFloor getOfficeFloor() {
		return this.getSingleton(null).getOfficeFloor();
	}

	@Override
	public void invokeProcess(String functionName, Object parameter) {
		this.getSingleton(null).invokeProcess(functionName, parameter);
	}

	@Override
	public void invokeProcess(String functionName, Object parameter, long waitTime) {
		this.getSingleton(null).invokeProcess(functionName, parameter, waitTime);
	}

	@Override
	public void invokeProcess(String officeName, String functionName, Object parameter, long waitTime) {
		this.getSingleton(null).invokeProcess(officeName, functionName, parameter, waitTime);
	}

	/*
	 * ==================== Extension ==========================
	 */

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this.getSingleton(extensionContext).supportsParameter(parameterContext);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this.getSingleton(extensionContext).resolveParameter(parameterContext);
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		this.getSingleton(context).beforeAll();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		this.getSingleton(context).beforeEach(context.getRequiredTestInstance());
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		this.getSingleton(context).afterEach();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		this.getSingleton(context).afterAll();
	}

	/**
	 * {@link FunctionalInterface} for parameter action.
	 */
	@FunctionalInterface
	private static interface ParameterAction<T> {
		T doAction(FromOffice fromOffice, StatePoint statePoint) throws Throwable;
	}

	/**
	 * Singleton {@link OfficeFloorJUnit}.
	 */
	private static class SingletonOfficeFloorJUnit extends AbstractOfficeFloorJUnit {

		/**
		 * Indicates if supports the parameter.
		 * 
		 * @param parameterContext {@link ParameterContext}.
		 * @return <code>true</code> if supports the parameter.
		 * @throws ParameterResolutionException If fails parameter resolution.
		 */
		protected boolean supportsParameter(ParameterContext parameterContext) throws ParameterResolutionException {
			return this.doParameterAction(parameterContext,
					(fromOffice, statePoint) -> this.isDependencyAvailable(fromOffice, statePoint));
		}

		/**
		 * Resolves the parameter.
		 * 
		 * @param parameterContext {@link ParameterContext}.
		 * @return Resolved parameter.
		 */
		private Object resolveParameter(ParameterContext parameterContext) throws ParameterResolutionException {
			return this.doParameterAction(parameterContext,
					(fromOffice, statePoint) -> this.getDependency(fromOffice, statePoint));
		}

		/**
		 * Undertakes the parameter action.
		 * 
		 * @param <T>              Result type.
		 * @param parameterContext {@link ParameterContext}.
		 * @param action           {@link ParameterAction}.
		 * @return Result.
		 * @throws ParameterResolutionException If failure.
		 */
		private <T> T doParameterAction(ParameterContext parameterContext, ParameterAction<T> action)
				throws ParameterResolutionException {

			// Obtain the parameter details
			FromOffice fromOffice = parameterContext.getParameter().getAnnotation(FromOffice.class);
			StatePoint statePoint = StatePoint.of(parameterContext.getDeclaringExecutable(),
					parameterContext.getIndex());

			// Return the action result
			try {
				return action.doAction(fromOffice, statePoint);
			} catch (Throwable ex) {
				throw new ParameterResolutionException("Parameter action failed", ex);
			}
		}

		/*
		 * ==================== AbstractOfficeFloorJUnit =====================
		 */

		@Override
		protected void doFail(String message) {
			fail(message);
		}

		@Override
		protected Error doFail(Throwable cause) {
			return fail(cause);
		}
	}

}
