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

package net.officefloor.compile.impl.executive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
import net.officefloor.frame.api.executive.ExecutiveStartContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Tests loading the {@link ExecutiveType}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class LoadExecutiveTypeTest {

	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link CompilerIssues}.
	 */
	private MockCompilerIssues issues;

	@BeforeEach
	protected void setUp() throws Exception {
		this.issues = new MockCompilerIssues(this.mocks);

		// Reset for each test
		MockLoadExecutiveSource.reset();
	}

	/**
	 * Ensure issue if missing property.
	 */
	@Test
	public void missingProperty() {

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
	@Test
	public void missingClass() {

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
	@Test
	public void missingResource() {

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
	@Test
	public void missingService() {

		// Record missing service
		this.issues.recordIssue(MissingServiceFactory.getIssueDescription());

		// Attempt to load
		this.loadExecutiveType(false, (context) -> context.loadService(MissingServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fail to load service.
	 */
	@Test
	public void failLoadService() {

		// Record load issue for service
		this.issues.recordIssue(FailServiceFactory.getIssueDescription(), FailServiceFactory.getCause());

		// Attempt to load
		this.loadExecutiveType(false, (context) -> context.loadService(FailServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fail to create {@link Executive}.
	 */
	@Test
	public void failureInCreatingExecutive() {

		// Record failure to create executive
		Exception exception = new Exception("TEST");
		this.issues.recordIssue("Failed to create the Executive from " + MockLoadExecutiveSource.class.getName(),
				exception);

		// Attempt to load
		this.loadExecutiveType(false, (context) -> {
			throw exception;
		});
	}

	/**
	 * Ensure issue if no {@link Executive} created.
	 */
	@Test
	public void issueIfNoExecutive() {

		// Record no executive
		this.issues.recordIssue("No Executive provided from " + MockLoadExecutiveSource.class.getName());

		// Attempt to load
		MockLoadExecutiveSource.isCreateExecutive = false;
		this.loadExecutiveType(false, (context) -> {
		});
	}

	/**
	 * Ensure can load the {@link ExecutiveType}.
	 */
	@Test
	public void loadExecutive() {

		// Load the executive
		ExecutiveType type = this.loadExecutiveType(true, null);

		// Ensure contains type information
		assertNotNull(type, "Should have executive type");
		ExecutionStrategyType[] strategies = type.getExecutionStrategyTypes();
		assertNotNull(strategies, "Should have execution strategies");
		assertEquals(1, strategies.length, "Incorrect number of execution strategies");
		assertEquals(MockLoadExecutiveSource.DEFAULT_EXECUTION_STRATEGY_NAME, strategies[0].getExecutionStrategyName(),
				"Incorrect execution strategy");
		assertFalse(type.isProvidingTeamOversight(), "Should be no team oversight");
	}

	/**
	 * Ensure issue if no {@link ExecutionStrategy} instances provided by
	 * {@link Executive}.
	 */
	@Test
	public void issueIfNoExecutionStrategy() {
		// Record missing class
		this.issues.recordIssue("Executive must provide at least one ExecutionStrategy");

		// Attempt to load
		MockLoadExecutiveSource.executionStrategyNames = new String[0];
		this.loadExecutiveType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link ExecutionStrategy} provided.
	 */
	@Test
	public void issueIfNullExecutionStrategy() {
		// Record null executive strategy
		this.issues.recordIssue("Null ExecutionStrategy provided for index 0");

		// Attempt to load
		MockLoadExecutiveSource.executionStrategyNames = new String[] { null };
		this.loadExecutiveType(false, null);
	}

	/**
	 * Ensure issue if no {@link ExecutionStrategy} name.
	 */
	@Test
	public void issueIfNoExecutionStrategyName() {
		// Return no strategy name
		this.issues.recordIssue("No name for ExecutionStrategy at index 0");

		// Attempt to load
		MockLoadExecutiveSource.executionStrategyNames = new String[] { "" };
		this.loadExecutiveType(false, null);
	}

	/**
	 * Ensure no {@link TeamOversight}.
	 */
	@Test
	public void noTeamOversightName() {

		// Load the executive
		MockLoadExecutiveSource.teamOversight = null;
		ExecutiveType type = this.loadExecutiveType(true, null);

		// Ensure type indicates no team oversight
		assertFalse(type.isProvidingTeamOversight(), "Should not provide team oversight");
	}

	/**
	 * Ensure load {@link TeamOversight}.
	 */
	@Test
	public void loadTeamOversight() {

		// Load the executive
		MockLoadExecutiveSource.teamOversight = this.mocks.createMock(TeamOversight.class);
		ExecutiveType type = this.loadExecutiveType(true, null);

		// Ensure type indicates team oversight
		assertTrue(type.isProvidingTeamOversight(), "SHould provide team oversight");
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
		this.mocks.replayMockObjects();

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
		ExecutiveType executiveType = executiveLoader.loadExecutiveType(MockLoadExecutiveSource.class, propertyList);

		// Verify the mock objects
		this.mocks.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull(executiveType, "Expected to load the executive type");
		} else {
			assertNull(executiveType, "Should not load the executive type");
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
		 * Indicates whether will create the {@link Executive}.
		 */
		private static boolean isCreateExecutive = true;

		/**
		 * {@link ExecutionStrategy} names.
		 */
		private static String[] executionStrategyNames = new String[] { DEFAULT_EXECUTION_STRATEGY_NAME };

		/**
		 * {@link TeamOversight}.
		 */
		private static TeamOversight teamOversight = null;

		/**
		 * Failure in instantiating an instance.
		 */
		public static RuntimeException instantiateFailure;

		/**
		 * Resets the state for the next test.
		 */
		public static void reset() {
			loader = null;
			isCreateExecutive = true;
			executionStrategyNames = new String[] { DEFAULT_EXECUTION_STRATEGY_NAME };
			teamOversight = null;
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
			return fail("Should not be invoked in obtaining executive type");
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			loader.sourceExecutive(context);
			return isCreateExecutive ? this : null;
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
						return fail("Should not require thread factories for type");
					}
				};
			}

			// Return the strategies
			return strategies;
		}

		@Override
		public TeamOversight getTeamOversight() {
			return teamOversight;
		}

		@Override
		public void startManaging(ExecutiveStartContext context) throws Exception {
			fail("Should not be invoked for type");
		}

		@Override
		public Executor createExecutor(ProcessIdentifier processIdentifier) {
			return fail("Should not be invoked for type");
		}

		@Override
		public void schedule(ProcessIdentifier processIdentifier, long delay, Runnable runnable) {
			fail("Should not be invoked for type");
		}

		@Override
		public void stopManaging() throws Exception {
			fail("Should not be invoked for type");
		}
	}

}
