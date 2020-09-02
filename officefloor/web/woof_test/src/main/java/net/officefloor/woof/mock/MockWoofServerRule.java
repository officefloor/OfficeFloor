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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.test.AbstractOfficeFloorJUnit;
import net.officefloor.woof.WoofLoaderSettings.WoofLoaderRunnableContext;

/**
 * {@link TestRule} for running the {@link MockWoofServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerRule extends MockWoofServer implements TestRule {

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
	 * Test instance.
	 */
	private final Object testInstance;

	/**
	 * Instantiate.
	 * 
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 */
	public MockWoofServerRule(MockWoofServerConfigurer... configurers) {
		this(null, configurers);
	}

	/**
	 * Instantiate.
	 * 
	 * @param testInstance Test instance to dependency inject.
	 * @param configurers  {@link MockWoofServerConfigurer} instances.
	 */
	public MockWoofServerRule(Object testInstance, MockWoofServerConfigurer... configurers) {
		this.testInstance = testInstance;

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
	public MockWoofServerRule profile(String profile) {
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
	public MockWoofServerRule property(String name, String value) {
		this.properties.setProperty(name, value);
		return this;
	}

	/**
	 * =============== MockWoofServer =====================
	 */

	@Override
	public MockWoofServerRule timeout(int timeout) {
		super.timeout(timeout);
		return this;
	}

	/*
	 * =================== TestRule =======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Easy access to rule
				MockWoofServerRule rule = MockWoofServerRule.this;

				// Create listing of configurers
				List<MockWoofServerConfigurer> configurers = new ArrayList<>(rule.configurers.size() + 1);
				configurers.addAll(rule.configurers);
				JUnitDependencyInjection dependencyInjection = null;
				if (rule.testInstance != null) {

					// Have instance so provide dependency injection
					dependencyInjection = new JUnitDependencyInjection();
					configurers.add(dependencyInjection);
				}
				MockWoofServerConfigurer[] config = configurers
						.toArray(new MockWoofServerConfigurer[configurers.size()]);

				// Start the mock server
				try (MockWoofServer server = MockWoofServer.open(rule, config)) {

					// Provide dependency injection if necessary
					if (dependencyInjection != null) {
						dependencyInjection.loadOfficeFloor.accept(server.getOfficeFloor());
						dependencyInjection.loadDependencies(rule.testInstance);
					}

					// Run the test
					base.evaluate();
				}
			}
		};
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
			String message = cause.getMessage();
			fail(message != null ? message : cause.toString());
			return null; // should not return
		}
	}

}
