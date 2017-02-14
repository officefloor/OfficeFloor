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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.NotificationEmitter;

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
		return 20;
	}

	/**
	 * Context for the stress {@link Test}.
	 */
	public static class StressContext {

		/**
		 * {@link AbstractStressTestCase}.
		 */
		private final AbstractStressTestCase test;

		/**
		 * Name of the {@link Team}.
		 */
		private final String teamName;

		/**
		 * Maximum number of iterations.
		 */
		private final int maxIterations;

		/**
		 * Report on progress when this many iterations have occurred.
		 */
		private final int reportEveryCount;

		/**
		 * {@link NumberFormat}.
		 */
		private final NumberFormat formatter;

		/**
		 * Number of iterations undertaken.
		 */
		private final AtomicInteger iterations = new AtomicInteger(0);

		/**
		 * Index of the other {@link Team} instances.
		 */
		private int otherTeamIndex = 1;

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
		 * @param test
		 *            {@link AbstractStressTestCase}.
		 * @param teamName
		 *            Name of the {@link Team}.
		 * @param maxIterations
		 *            Maximum number of iterations.
		 */
		private StressContext(AbstractStressTestCase test, String teamName, int maxIterations) {
			this.test = test;
			this.teamName = teamName;
			this.maxIterations = maxIterations;
			this.reportEveryCount = (maxIterations / 10);
			this.formatter = NumberFormat.getIntegerInstance();
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
		public void setInitialFunction(String functionName, Object parameter) {
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
		public void loadResponsibleTeam(ManagedFunctionBuilder<?, ?> builder) {
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
		public void loadResponsibleTeam(AdministrationBuilder<?, ?> builder) {
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
		public void loadResponsibleTeam(GovernanceBuilder<?> builder) {
			if (this.teamName != null) {
				builder.setResponsibleTeam(this.teamName);
			}
		}

		/**
		 * Loads another {@link Team} responsible for the
		 * {@link ManagedFunction}.
		 * 
		 * @param builder
		 *            {@link ManagedFunctionBuilder}.
		 */
		public void loadOtherTeam(ManagedFunctionBuilder<?, ?> builder) {
			int otherTeamIndex = this.otherTeamIndex++;
			String otherTeamName = "OTHER_" + otherTeamIndex;
			test.constructTeam(otherTeamName, OnePersonTeamSource.class);
		}

		/**
		 * Specifies the validation.
		 * 
		 * @param validation
		 *            {@link Runnable} containing the validation.
		 */
		public void setValidation(Runnable validation) {
			this.validation = validation;
		}

		/**
		 * Obtains the maximum number of iterations.
		 * 
		 * @return Maximum number of iterations.
		 */
		public int getMaximumIterations() {
			return this.maxIterations;
		}

		/**
		 * Reports on progress.
		 * 
		 * @param iteration
		 *            Current iteration.
		 */
		public void reportProgress(int iteration) {
			if ((iteration % this.reportEveryCount) == 0) {
				test.printMessage("Iterations " + this.formatter.format(iteration));
			}
		}

		/**
		 * Increments the number of iterations.
		 * 
		 * @return Number of iterations with increment.
		 */
		public int incrementIteration() {
			int count = this.iterations.incrementAndGet();
			this.reportProgress(count);
			return count;
		}

		/**
		 * Indicates if max iterations reached.
		 * 
		 * @return <code>true</code> if max iterations reached.
		 */
		public boolean isComplete() {
			return (this.iterations.get() >= this.maxIterations);
		}

		/**
		 * Convenience method to increment the iterations and check if complete.
		 * 
		 * @return <code>true</code> if complete after incrementing the
		 *         iteration.
		 */
		public boolean incrementIterationAndIsComplete() {
			int count = this.incrementIteration();
			return (count >= this.maxIterations);
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
		// Load the team constructors
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

		// Hook in for GC
		for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
			System.out.println("GC: " + gcBean.getName());
			NotificationEmitter emitter = (NotificationEmitter) gcBean;
			emitter.addNotificationListener((notification, handback) -> {
				System.out.println(" -> GC: " + gcBean.getName() + " (" + gcBean.getCollectionTime() + " ms) - " + notification.getType());
			}, null, null);
		}
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
			test.setName(test.teamName + "_i" + test.getIterationCount());

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
		StressContext context = new StressContext(this, this.teamName, this.getIterationCount());
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
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMinimumFractionDigits(5);
		String effectiveRunTimeText = formatter.format(effectiveRunTime);

		// Undertake optional validation
		if (context.validation != null) {
			context.validation.run();
		}

		// Ensure appropriate number of iterations undertaken
		assertEquals("Incorrect number of iterations", this.getIterationCount(), context.iterations.get());

		// Indicate details of run
		this.printMessage("Construct: " + constructionTime);
		this.printMessage("Run      : " + executionTime);
		this.printMessage("Effective: 1 iteration per " + effectiveRunTimeText + " milliseconds");
	}

}