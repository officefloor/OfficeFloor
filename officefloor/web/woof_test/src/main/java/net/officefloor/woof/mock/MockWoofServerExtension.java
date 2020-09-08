/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof.mock;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.clazz.state.StatePoint;
import net.officefloor.test.AbstractOfficeFloorJUnit;
import net.officefloor.test.FromOffice;
import net.officefloor.woof.WoofLoaderSettings.WoofLoaderRunnableContext;

/**
 * {@link Extension} for running the {@link MockWoofServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerExtension extends MockWoofServer
		implements ParameterResolver, BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * {@link MockWoofServerConfigurer} instances.
	 */
	private final List<MockWoofServerConfigurer> configurers = new LinkedList<>();

	/**
	 * Additional profiles.
	 */
	private final List<String> profiles = new LinkedList<>();

	/**
	 * Override {@link Properties}.
	 */
	private final Properties properties = new Properties();

	/**
	 * {@link JUnitDependencyInjection}.
	 */
	private final JUnitDependencyInjection dependencyInjection = new JUnitDependencyInjection();

	/**
	 * Indicates whether to start/stop {@link MockWoofServer} for each test.
	 */
	private boolean isEach = true;

	/**
	 * Default {@link Constructor} to use with {@link ExtendWith}.
	 */
	public MockWoofServerExtension() {
		this(new MockWoofServerConfigurer[0]);
	}

	/**
	 * Instantiate.
	 * 
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 */
	public MockWoofServerExtension(MockWoofServerConfigurer... configurers) {
		this.configurers.addAll(Arrays.asList(configurers));

		// Allow configuring the profiles
		this.configurers.add((context, compiler) -> {

			// Add the profiles
			for (String profile : this.profiles) {
				context.addProfile(profile);
			}

			// Add the properties
			for (String name : this.properties.stringPropertyNames()) {
				String value = this.properties.getProperty(name);
				compiler.getOfficeFloorCompiler().addProperty(name, value);
				context.addOverrideProperty(name, value);
			}
		});
	}

	/**
	 * Builder pattern for adding an additional profile.
	 * 
	 * @param profile Additional profile.
	 * @return <code>this</code>.
	 */
	public MockWoofServerExtension profile(String profile) {
		this.profiles.add(profile);
		return this;
	}

	/**
	 * Builder pattern for adding an override property.
	 * 
	 * @param name  Name.
	 * @param value Value.
	 * @return <code>this</code>.
	 */
	public MockWoofServerExtension property(String name, String value) {
		this.properties.setProperty(name, value);
		return this;
	}

	/**
	 * Opens the {@link MockWoofServer}.
	 * 
	 * @throws Exception If fails to open the {@link MockWoofServer}.
	 */
	private void openMockWoofServer() throws Exception {

		// Create listing of configurers
		List<MockWoofServerConfigurer> configurers = new ArrayList<>(this.configurers.size() + 1);
		configurers.addAll(this.configurers);
		configurers.add(this.dependencyInjection);
		MockWoofServerConfigurer[] config = configurers.toArray(new MockWoofServerConfigurer[configurers.size()]);

		try {
			// Start the mock server
			MockWoofServer.open(this, config);

			// Configure OfficeFloor
			this.dependencyInjection.loadOfficeFloor.accept(this.getOfficeFloor());

		} catch (Exception ex) {
			try {
				// Ensure attempt to close
				this.close();
			} catch (Throwable ignore) {
				// Made best attempt to close
			}

			// Propagate the failure
			throw ex;
		}
	}

	/*
	 * ======================== Extension =========================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Start only once for all tests
		this.isEach = false;

		// Start the server
		this.openMockWoofServer();
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this.dependencyInjection.supportsParameter(parameterContext);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this.dependencyInjection.resolveParameter(parameterContext);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Start the server if for each
		if (this.isEach) {
			this.openMockWoofServer();
		}

		// Inject the dependencies
		this.dependencyInjection.loadDependencies(context.getRequiredTestInstance());
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Stop the server if for each
		if (this.isEach) {
			this.close();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Stop server if all
		if (!this.isEach) {
			this.close();
		}
	}

	/**
	 * {@link FunctionalInterface} for parameter action.
	 */
	@FunctionalInterface
	private static interface ParameterAction<T> {
		T doAction(FromOffice fromOffice, StatePoint statePoint) throws Throwable;
	}

	/**
	 * Provides means to inject dependencies into JUnit test.
	 */
	private static class JUnitDependencyInjection extends AbstractOfficeFloorJUnit implements MockWoofServerConfigurer {

		/**
		 * {@link Consumer} to load the opened {@link OfficeFloor};
		 */
		private Consumer<OfficeFloor> loadOfficeFloor;

		/**
		 * Loads dependencies to the test instance.
		 * 
		 * @param testInstance Test instance.
		 * @throws Exception If fails to load the dependencies.
		 */
		private void loadDependencies(Object testInstance) throws Exception {
			this.beforeEach(testInstance);
		}

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
		 * ======================== MockWoofServerConfigurer ======================
		 */

		@Override
		public void configure(WoofLoaderRunnableContext context, CompileOfficeFloor compiler) throws Exception {
			this.loadOfficeFloor = this.initialiseOfficeFloorCompiler(compiler.getOfficeFloorCompiler());
		}

		/*
		 * ======================== AbstractOfficeFloorJUnit ======================
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
