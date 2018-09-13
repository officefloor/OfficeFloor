/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.compile.FailServiceFactory;
import net.officefloor.compile.MissingServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.executive.ExecutionStrategyType;
import net.officefloor.compile.executive.ExecutiveLoader;
import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests loading the {@link ExecutiveType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadExecutiveTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Reset for each test
		MockLoadExecutiveSource.reset();
	}

	/**
	 * Ensure issue if missing property.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Must specify property 'missing'");

		// Attempt to load
		this.loadExecutiveType(false, (context) -> {
			context.getProperty("missing");
		});
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue("Can not load class 'missing'");

		// Attempt to load
		this.loadExecutiveType(false, (context) -> {
			context.loadClass("missing");
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing class
		this.issues.recordIssue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadExecutiveType(false, (context) -> {
			context.getResource("missing");
		});
	}

	/**
	 * Ensure issue if missing service.
	 */
	public void testMissingService() {

		// Record missing service
		this.issues.recordIssue(MissingServiceFactory.getIssueDescription());

		// Attempt to load
		this.loadExecutiveType(false, (context) -> context.loadService(MissingServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fail to load service.
	 */
	public void testFailLoadService() {

		// Record load issue for service
		this.issues.recordIssue(FailServiceFactory.getIssueDescription(), FailServiceFactory.getCause());

		// Attempt to load
		this.loadExecutiveType(false, (context) -> context.loadService(FailServiceFactory.class, null));
	}

	/**
	 * Ensure can load the {@link ExecutiveType}.
	 */
	public void testLoadExecutive() {

		// Load the executive
		ExecutiveType type = this.loadExecutiveType(true, null);

		// Ensure contains type information
		assertNotNull("Should have executive type");
		ExecutionStrategyType[] strategies = type.getExecutionStrategyTypes();
		assertNotNull("Should have execution strategies");
		assertEquals("Incorrect number of execution strategies", 1, strategies.length);
		assertEquals("Incorrect execution strategy", MockLoadExecutiveSource.DEFAULT_EXECUTION_STRATEGY_NAME,
				strategies[0].getExecutionStrategyName());
	}

	/**
	 * Ensure issue if no {@link ExecutionStrategy} instances provided by
	 * {@link Executive}.
	 */
	public void testIssueIfNoExecutionStrategy() {
		// Record missing class
		this.issues.recordIssue("Executive must provide at least one ExecutionStrategy");

		// Attempt to load
		MockLoadExecutiveSource.executionStrategyNames = new String[0];
		this.loadExecutiveType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link ExecutionStrategy} provided.
	 */
	public void testIssueIfNullExecutionStrategy() {
		// Record null executive strategy
		this.issues.recordIssue("Executive provided null ExecutionStrategy for index 0");

		// Attempt to load
		MockLoadExecutiveSource.executionStrategyNames = new String[] { null };
		this.loadExecutiveType(false, null);
	}

	/**
	 * Ensure issue if no {@link ExecutionStrategy} name.
	 */
	public void testIssueIfNoExecutionStrategyName() {
		// Return no strategy name
		this.issues.recordIssue("Executive had blank name for ExecutionStrategy for index 0");

		// Attempt to load
		MockLoadExecutiveSource.executionStrategyNames = new String[] { "" };
		this.loadExecutiveType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link TeamOversight} provided.
	 */
	public void testIssueIfNullTeamOversight() {
		// Record null team oversight
		this.issues.recordIssue("Executive provided null TeamOversight for index 0");

		// Attempt to load
		MockLoadExecutiveSource.teamOversightNames = new String[] { null };
		this.loadExecutiveType(false, null);
	}

	/**
	 * Ensure issue if no {@link TeamOversight} name.
	 */
	public void testIssueIfNoTeamOversightName() {
		// Return no strategy name
		this.issues.recordIssue("Executive had blank name for TeamOversight for index 0");

		// Attempt to load
		MockLoadExecutiveSource.teamOversightNames = new String[] { "" };
		this.loadExecutiveType(false, null);
	}

	/**
	 * Loads the {@link ExecutiveType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link ExecutiveType}.
	 * @param loader                 {@link Loader}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link ExecutiveType}.
	 */
	private ExecutiveType loadExecutiveType(boolean isExpectedToLoad, Loader loader, String... propertyNameValuePairs) {

		// Ensure have loader
		if (loader == null) {
			loader = (context) -> {
			};
		}

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the executive loader and load the executive
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ExecutiveLoader executiveLoader = compiler.getExecutiveLoader();
		MockLoadExecutiveSource.loader = loader;
		ExecutiveType executiveType = executiveLoader.loadExecutiveType("executive", MockLoadExecutiveSource.class,
				propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the executive type", executiveType);
		} else {
			assertNull("Should not load the executive type", executiveType);
		}

		// Return the executive type
		return executiveType;
	}

	/**
	 * Implemented to load the {@link ExecutiveType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link ExecutiveType}.
		 * 
		 * @param context {@link ExecutiveSourceContext}.
		 * @throws Exception If fails to source {@link ExecutiveType}.
		 */
		void sourceExecutive(ExecutiveSourceContext context) throws Exception;
	}

	/**
	 * Mock {@link ExecutiveSource} for testing.
	 */
	@TestSource
	public static class MockLoadExecutiveSource implements ExecutiveSource, Executive {

		/**
		 * Default {@link ExecutionStrategy} name.
		 */
		private static final String DEFAULT_EXECUTION_STRATEGY_NAME = "test";

		/**
		 * {@link Loader} to load the {@link ExecutiveType }.
		 */
		public static Loader loader;

		/**
		 * {@link ExecutionStrategy} names.
		 */
		private static String[] executionStrategyNames = new String[] { DEFAULT_EXECUTION_STRATEGY_NAME };

		/**
		 * {@link TeamOversight} names.
		 */
		private static String[] teamOversightNames = new String[0];

		/**
		 * Failure in instantiating an instance.
		 */
		public static RuntimeException instantiateFailure;

		/**
		 * Resets the state for the next test.
		 */
		public static void reset() {
			loader = null;
			executionStrategyNames = new String[] { DEFAULT_EXECUTION_STRATEGY_NAME };
			teamOversightNames = new String[0];
			instantiateFailure = null;
		}

		/**
		 * Default constructor.
		 */
		public MockLoadExecutiveSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ ExecutiveSource ======================================
		 */

		@Override
		public ExecutiveSourceSpecification getSpecification() {
			fail("Should not be invoked in obtaining executive type");
			return null;
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			loader.sourceExecutive(context);
			return this;
		}

		/*
		 * ======================== Executive =======================================
		 */

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {

			// Create the execution strategies
			ExecutionStrategy[] strategies = new ExecutionStrategy[executionStrategyNames.length];
			for (int i = 0; i < strategies.length; i++) {
				String name = executionStrategyNames[i];
				strategies[i] = name == null ? null : new ExecutionStrategy() {

					@Override
					public String getExecutionStrategyName() {
						return name;
					}

					@Override
					public ThreadFactory[] getThreadFactories() {
						fail("Should not require thread factories for type");
						return null;
					}
				};
			}

			// Return the strategies
			return strategies;
		}

		@Override
		public TeamOversight[] getTeamOversights() {

			// Create the team oversights
			TeamOversight[] oversights = new TeamOversight[teamOversightNames.length];
			for (int i = 0; i < oversights.length; i++) {
				String name = teamOversightNames[i];
				oversights[i] = name == null ? null : new TeamOversight() {

					@Override
					public String getTeamOversightName() {
						return name;
					}
				};
			}

			// Return the oversights
			return oversights;
		}
	}

}