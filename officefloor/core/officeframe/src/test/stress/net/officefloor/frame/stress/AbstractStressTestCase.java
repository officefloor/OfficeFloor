/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.stress;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Abstract stress {@link TestCase}
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractStressTestCase extends AbstractOfficeConstructTestCase {

	/**
	 * Creates the {@link TestSuite} for the input
	 * {@link AbstractStressTestCase} class.
	 * 
	 * @param testClass
	 *            {@link AbstractStressTestCase} implementation.
	 * @return {@link TestSuite}.
	 */
	public static TestSuite createSuite(Class<? extends AbstractStressTestCase> testClass) {
		TestSuite suite = new TestSuite();

		// Add no team test
		suite.addTest(createTestCase(testClass, null, null));

		// Add the tests for different teams
		for (Class<? extends TeamSource> teamSourceClass : teamConstructors.keySet()) {
			TeamConstructor teamConstructor = teamConstructors.get(teamSourceClass);
			suite.addTest(createTestCase(testClass, teamSourceClass, teamConstructor));
		}

		// Return the suite
		return suite;
	}

	/**
	 * Constructs the {@link Test}.
	 * 
	 * @param context
	 *            {@link StressContext}.
	 * @throws Exception
	 *             If failure in constructing the test.
	 */
	protected abstract void constructTest(StressContext context) throws Exception;

	/**
	 * Obtains the {@link Team} size.
	 * 
	 * @return {@link Team} size.
	 */
	protected int getTeamSize() {
		return 10;
	}

	/**
	 * Obtains the iteration count.
	 * 
	 * @return Iteration count.
	 */
	protected int getIterationCount() {
		return 1000000;
	}

	/**
	 * Maximum wait time for the {@link Test} to complete in seconds.
	 * 
	 * @return Wait time in seconds for completion of {@link Test}.
	 */
	protected int getMaxWaitTime() {
		return 100;
	}

	/**
	 * Context for the stress {@link Test}.
	 */
	protected static class StressContext {

		/**
		 * Name of the {@link Team}.
		 */
		private final String teamName;

		/**
		 * Initial {@link ManagedFunction} name.
		 */
		private String initialFunctionName;

		/**
		 * Initial {@link ManagedFunction} parameter.
		 */
		private Object initialFunctionParameter;

		/**
		 * Optional validation.
		 */
		private Runnable validation;

		/**
		 * Instantiate.
		 * 
		 * @param teamName
		 *            Name of the {@link Team}.
		 */
		private StressContext(String teamName) {
			this.teamName = teamName;
		}

		/**
		 * Specifies the initial {@link ManagedFunction}.
		 * 
		 * @param functionName
		 *            Name of the {@link ManagedFunction}.
		 * @param parameter
		 *            Parameter to the {@link ManagedFunction}. May be
		 *            <code>null</code>.
		 */
		protected void setInitialFunction(String functionName, Object parameter) {
			this.initialFunctionName = functionName;
			this.initialFunctionParameter = parameter;
		}

		/**
		 * Loads the {@link Team} as responsible for the
		 * {@link ManagedFunction}.
		 * 
		 * @param builder
		 *            {@link ManagedFunctionBuilder}.
		 */
		protected void loadResponsibleTeam(ManagedFunctionBuilder<?, ?> builder) {
			if (this.teamName != null) {
				builder.setResponsibleTeam(this.teamName);
			}
		}

		/**
		 * Loads the {@link Team} as responsible for the {@link Administration}.
		 * 
		 * @param builder
		 *            {@link AdministrationBuilder}.
		 */
		protected void loadResponsibleTeam(AdministrationBuilder<?, ?> builder) {
			if (this.teamName != null) {
				builder.setResponsibleTeam(this.teamName);
			}
		}

		/**
		 * Loads the {@link Team} as responsible for the {@link Governance}.
		 * 
		 * @param builder
		 *            {@link GovernanceBuilder}.
		 */
		protected void loadResponsibleTeam(GovernanceBuilder<?> builder) {
			if (this.teamName != null) {
				builder.setResponsibleTeam(this.teamName);
			}
		}

		/**
		 * Specifies the validation.
		 * 
		 * @param validation
		 *            {@link Runnable} containing the validation.
		 */
		protected void setValidation(Runnable validation) {
			this.validation = validation;
		}
	}

	/*
	 * ====================== Helper Methods ============================
	 */

	/**
	 * Mapping of {@link TeamSource} {@link Class} to its
	 * {@link TeamConstructor}.
	 */
	private static final Map<Class<? extends TeamSource>, TeamConstructor> teamConstructors = new HashMap<>();

	/**
	 * Constructor for a {@link Team}.
	 */
	private static interface TeamConstructor {

		/**
		 * Constructs the {@link Team}.
		 * 
		 * @param teamName
		 *            Name of the {@link Team}.
		 * @param testCase
		 *            {@link AbstractStressTestCase}.
		 * @return {@link Class} of the {@link TeamSource}.
		 */
		void constructTeam(String teamName, AbstractStressTestCase testCase);
	}

	/**
	 * Load the {@link TeamConstructor} instances.
	 */
	static {
		teamConstructors.put(PassiveTeamSource.class,
				(name, test) -> test.constructTeam(name, PassiveTeamSource.class));
		teamConstructors.put(ExecutorCachedTeamSource.class,
				(name, test) -> test.constructTeam(name, ExecutorCachedTeamSource.class));
		teamConstructors.put(ExecutorFixedTeamSource.class,
				(name, test) -> test.constructTeam(name, ExecutorFixedTeamSource.class)
						.addProperty(ExecutorFixedTeamSource.PROPERTY_TEAM_SIZE, String.valueOf(test.getTeamSize())));
		teamConstructors.put(OnePersonTeamSource.class,
				(name, test) -> test.constructTeam(name, OnePersonTeamSource.class));
		teamConstructors.put(ThreadLocalAwareTeamSource.class,
				(name, test) -> test.constructTeam(name, ThreadLocalAwareTeamSource.class));
		teamConstructors.put(WorkerPerJobTeamSource.class,
				(name, test) -> test.constructTeam(name, WorkerPerJobTeamSource.class));
	}

	/**
	 * Creates the {@link Test}.
	 * 
	 * @param testClass
	 *            {@link Class} of the {@link Test}.
	 * @param teamSourceClass
	 *            {@link Class} of the {@link TeamSource}.
	 * @param teamConstructor
	 *            {@link TeamConstructor}.
	 * @return {@link AbstractStressTestCase} for the {@link Test}.
	 */
	private static AbstractStressTestCase createTestCase(Class<? extends AbstractStressTestCase> testClass,
			Class<? extends TeamSource> teamSourceClass, TeamConstructor teamConstructor) {
		try {

			// Create a new instance of the test
			AbstractStressTestCase test = testClass.newInstance();

			// Load values for testing
			test.teamName = (teamSourceClass != null ? teamSourceClass.getSimpleName() : null);

			// Set the name for the test
			test.setName(test.teamName + "_i" + test.getIterationCount() + "_" + test.getClass().getSimpleName());

			// Specify details
			test.teamConstructor = teamConstructor;

			// Return the test
			return test;

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Name of the {@link Team}.
	 */
	private String teamName;

	/**
	 * {@link TeamConstructor}.
	 */
	private TeamConstructor teamConstructor;

	@Override
	protected void runTest() throws Throwable {

		// Provide verbose output
		this.setVerbose(true);

		// Capture start time
		long startTimestamp = System.currentTimeMillis();

		// Construct the team
		if (this.teamConstructor != null) {
			this.teamConstructor.constructTeam(this.teamName, this);
		}

		// Construct the test
		StressContext context = new StressContext(this.teamName);
		this.constructTest(context);
		assertNotNull("Must configure initial function name", context.initialFunctionName);

		// Capture construction time
		String constructionTime = this.getDisplayRunTime(startTimestamp);

		// Run the test
		startTimestamp = System.currentTimeMillis();
		this.invokeFunction(context.initialFunctionName, context.initialFunctionParameter, this.getMaxWaitTime());
		long endTimestamp = System.currentTimeMillis();
		String executionTime = this.getDisplayRunTime(startTimestamp, endTimestamp);

		// Obtain the effective run time
		float effectiveRunTime = (float) (endTimestamp - startTimestamp) / this.getIterationCount();
		String effectiveRunTimeText = NumberFormat.getInstance().format(effectiveRunTime);

		// Indicate details of run
		this.printMessage("Construct: " + constructionTime);
		this.printMessage("Run      : " + executionTime);
		this.printMessage("Effective: 1 iteration per " + effectiveRunTimeText + " ms");

		// Undertake validation
		if (context.validation != null) {
			context.validation.run();
		}
	}

}