/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.stress;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeamSource;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract stress {@link TestCase}
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractStressTestCase extends AbstractOfficeConstructTestCase {

	/**
	 * Creates the {@link TestSuite} for the input {@link AbstractStressTestCase}
	 * class.
	 * 
	 * @param testClass {@link AbstractStressTestCase} implementation.
	 * @return {@link TestSuite}.
	 */
	public static TestSuite createSuite(Class<? extends AbstractStressTestCase> testClass) {
		TestSuite suite = new TestSuite();

		// Add no team test
		for (Test test : createTestCases(testClass, null, null)) {
			suite.addTest(test);
		}

		// Add the tests for different teams
		for (Class<? extends TeamSource> teamSourceClass : teamConstructors.keySet()) {
			TeamConstructor teamConstructor = teamConstructors.get(teamSourceClass);
			for (Test test : createTestCases(testClass, teamSourceClass, teamConstructor)) {
				suite.addTest(test);
			}
		}

		// Return the suite
		return suite;
	}

	/**
	 * Constructs the {@link Test}.
	 * 
	 * @param context {@link StressContext}.
	 * @throws Exception If failure in constructing the test.
	 */
	protected abstract void constructTest(StressContext context) throws Exception;

	/**
	 * Flags whether testing each {@link ManagedObjectScope}.
	 * 
	 * @return <code>true</code> to test each {@link ManagedObjectScope}.
	 */
	protected boolean isTestEachManagedObjectScope() {
		return false;
	}

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
	 * Overrides the iteration count for a particular {@link TeamSource}.
	 * 
	 * @param overrides Overrides.
	 */
	protected void overrideIterationCount(Map<Class<? extends TeamSource>, Integer> overrides) {
		// No overrides
	}

	/**
	 * Maximum wait time for the {@link Test} to complete in seconds.
	 * 
	 * @return Wait time in seconds for completion of {@link Test}.
	 */
	protected int getMaxWaitTime() {
		return 300;
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
		 * {@link ManagedObjectScope}. May be <code>null</code>.
		 */
		private final ManagedObjectScope managedObjectScope;

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
		 * Indicates whether a responsible {@link Team} was assigned.
		 */
		private boolean isResponsibleTeamAssigned = false;

		/**
		 * Indicates whether the {@link ManagedObjectScope} has been assigned.
		 */
		private boolean isManagedObjectScopeAssigned = false;

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
		 * {@link TeamSource} {@link Class} to only run. <code>null</code> indicates to
		 * run all {@link TeamSource} types.
		 */
		private Class<? extends TeamSource> onlyTeamClass = null;

		/**
		 * Instantiate.
		 * 
		 * @param test               {@link AbstractStressTestCase}.
		 * @param teamName           Name of the {@link Team}.
		 * @param maxIterations      Maximum number of iterations.
		 * @param managedObjectScope {@link ManagedObjectScope}.
		 */
		private StressContext(AbstractStressTestCase test, String teamName, int maxIterations,
				ManagedObjectScope managedObjectScope) {
			this.test = test;
			this.teamName = teamName;
			this.maxIterations = maxIterations;
			this.managedObjectScope = managedObjectScope;
			int reportCount = (maxIterations / 10);
			this.reportEveryCount = (reportCount == 0 ? 1 : reportCount);
			this.formatter = NumberFormat.getIntegerInstance();
		}

		/**
		 * Specifies the initial {@link ManagedFunction}.
		 * 
		 * @param functionName Name of the {@link ManagedFunction}.
		 * @param parameter    Parameter to the {@link ManagedFunction}. May be
		 *                     <code>null</code>.
		 */
		public void setInitialFunction(String functionName, Object parameter) {
			this.initialFunctionName = functionName;
			this.initialFunctionParameter = parameter;
		}

		/**
		 * Loads the {@link Team} as responsible for the {@link ManagedFunction}.
		 * 
		 * @param builder {@link ManagedFunctionBuilder}.
		 */
		public void loadResponsibleTeam(ManagedFunctionBuilder<?, ?> builder) {
			this.isResponsibleTeamAssigned = true;
			if (this.teamName != null) {
				builder.setResponsibleTeam(this.teamName);
			}
		}

		/**
		 * Loads the {@link Team} as responsible for the {@link Administration}.
		 * 
		 * @param builder {@link AdministrationBuilder}.
		 */
		public void loadResponsibleTeam(AdministrationBuilder<?, ?> builder) {
			this.isResponsibleTeamAssigned = true;
			if (this.teamName != null) {
				builder.setResponsibleTeam(this.teamName);
			}
		}

		/**
		 * Loads the {@link Team} as responsible for the {@link Governance}.
		 * 
		 * @param builder {@link GovernanceBuilder}.
		 */
		public void loadResponsibleTeam(GovernanceBuilder<?> builder) {
			this.isResponsibleTeamAssigned = true;
			if (this.teamName != null) {
				builder.setResponsibleTeam(this.teamName);
			}
		}

		/**
		 * Loads another {@link Team} responsible for the {@link ManagedFunction}.
		 * 
		 * @param builder {@link ManagedFunctionBuilder}.
		 */
		public void loadOtherTeam(ManagedFunctionBuilder<?, ?> builder) {
			if (this.teamName != null) {
				// Only load if have responsible team
				int otherTeamIndex = this.otherTeamIndex++;
				String otherTeamName = "OTHER_" + otherTeamIndex;
				test.constructTeam(otherTeamName, OnePersonTeamSource.class);
			}
		}

		/**
		 * Specifies the validation.
		 * 
		 * @param validation {@link Runnable} containing the validation.
		 */
		public void setValidation(Runnable validation) {
			this.validation = validation;
		}

		/**
		 * Specifies to run only the {@link TeamSource}.
		 * 
		 * @param teamSourceClass Only {@link TeamSource}.
		 */
		public void setOnly(Class<? extends TeamSource> teamSourceClass) {
			this.onlyTeamClass = teamSourceClass;
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
		 * Obtains the {@link ManagedObjectScope}.
		 * 
		 * @return {@link ManagedObjectScope}.
		 */
		public ManagedObjectScope getManagedObjectScope() {
			assertNotNull("No managed object scope available", this.managedObjectScope);
			this.isManagedObjectScopeAssigned = true;
			return this.managedObjectScope;
		}

		/**
		 * Reports on progress.
		 * 
		 * @param iteration Current iteration.
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
		 * @return <code>true</code> if complete after incrementing the iteration.
		 */
		public boolean incrementIterationAndIsComplete() {
			int count = this.incrementIteration();
			return (count >= this.maxIterations);
		}

		/**
		 * Convenience method to increment the iterations, check if complete and
		 * validate current iteration is correct.
		 * 
		 * @param validateCorrectIteration Current iteration to validate is correct.
		 * @return <code>true</code> if complete after incrementing the iteration.
		 */
		public boolean incrementIterationAndIsComplete(int validateCorrectIteration) {
			int count = this.incrementIteration();
			assertEquals("Incorrect iteration", count, validateCorrectIteration);
			return (count >= this.maxIterations);
		}
	}

	/*
	 * ====================== Helper Methods ============================
	 */

	/**
	 * Mapping of {@link TeamSource} {@link Class} to its {@link TeamConstructor}.
	 */
	private static final Map<Class<? extends TeamSource>, TeamConstructor> teamConstructors = new HashMap<>();

	/**
	 * Constructor for a {@link Team}.
	 */
	private static interface TeamConstructor {

		/**
		 * Constructs the {@link Team}.
		 * 
		 * @param teamName Name of the {@link Team}.
		 * @param testCase {@link AbstractStressTestCase}.
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
		teamConstructors.put(ExecutorFixedTeamSource.class, (name, test) -> test
				.constructTeam(name, ExecutorFixedTeamSource.class).setTeamSize(test.getTeamSize()));
		teamConstructors.put(OnePersonTeamSource.class,
				(name, test) -> test.constructTeam(name, OnePersonTeamSource.class));
		teamConstructors.put(ThreadLocalAwareTeamSource.class,
				(name, test) -> test.constructTeam(name, ThreadLocalAwareTeamSource.class));
		teamConstructors.put(WorkerPerJobTeamSource.class,
				(name, test) -> test.constructTeam(name, WorkerPerJobTeamSource.class));
		teamConstructors.put(LeaderFollowerTeamSource.class, (name, test) -> test
				.constructTeam(name, LeaderFollowerTeamSource.class).setTeamSize(test.getTeamSize()));
		teamConstructors.put(RequestScopedTeamSource.class, (name, test) -> {
			test.getOfficeFloorBuilder().setExecutive(RequestScopedExecutive.class);
			test.constructTeam(name, RequestScopedTeamSource.class);
		});
	}

	/**
	 * Creates the {@link Test}.
	 * 
	 * @param testClass          {@link Class} of the {@link Test}.
	 * @param teamSourceClass    {@link Class} of the {@link TeamSource}.
	 * @param teamConstructor    {@link TeamConstructor}.
	 * @param managedObjectScope {@link ManagedObjectScope}.
	 * @return {@link AbstractStressTestCase} for the {@link Test}.
	 */
	private static AbstractStressTestCase[] createTestCases(Class<? extends AbstractStressTestCase> testClass,
			Class<? extends TeamSource> teamSourceClass, TeamConstructor teamConstructor) {
		try {

			// Create a new instance of the test
			AbstractStressTestCase test = testClass.getDeclaredConstructor().newInstance();

			// Determine managed object scopes
			ManagedObjectScope[] managedObjectScopes = test.isTestEachManagedObjectScope()
					? new ManagedObjectScope[] { ManagedObjectScope.PROCESS, ManagedObjectScope.THREAD,
							ManagedObjectScope.FUNCTION }
					: new ManagedObjectScope[] { null };

			// Create the array of tests
			AbstractStressTestCase[] tests = new AbstractStressTestCase[managedObjectScopes.length];
			for (int i = 0; i < tests.length; i++) {
				ManagedObjectScope managedObjectScope = managedObjectScopes[i];

				// Create new test
				test = testClass.getDeclaredConstructor().newInstance();

				// Load values for testing
				test.teamName = (teamSourceClass != null ? teamSourceClass.getSimpleName() : null);

				// Avoid creating too many threads (but allow override)
				Map<Class<? extends TeamSource>, Integer> iterationCountOverrides = new HashMap<>();
				iterationCountOverrides.put(WorkerPerJobTeamSource.class, 100);

				// Obtain the iteration count
				test.overrideIterationCount(iterationCountOverrides);
				Integer iterationCount = iterationCountOverrides.get(teamSourceClass);
				if (iterationCount == null) {
					// No override, so use default
					iterationCount = test.getIterationCount();
				}

				// Set the name for the test
				test.setName(test.teamName + (managedObjectScope != null ? "_" + managedObjectScope.name() : "") + "_i"
						+ iterationCount);

				// Specify details
				test.teamSourceClass = teamSourceClass;
				test.teamConstructor = teamConstructor;
				test.iterationCount = iterationCount;
				test.managedObjectScope = managedObjectScope;

				// Load the test for return
				tests[i] = test;
			}

			// Return the tests
			return tests;

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Name of the {@link Team}.
	 */
	private String teamName;

	/**
	 * {@link TeamSource} {@link Class}.
	 */
	private Class<? extends TeamSource> teamSourceClass;

	/**
	 * {@link TeamConstructor}.
	 */
	private TeamConstructor teamConstructor;

	/**
	 * Iteration count.
	 */
	private int iterationCount;

	/**
	 * {@link ManagedObjectScope}.
	 */
	private ManagedObjectScope managedObjectScope;

	@Override
	protected void runTest() throws Throwable {

		// Determine if ignoring stress tests
		if (OfficeFrameTestCase.isSkipStressTests()) {
			System.out.println("Skipping stress test " + this.getName());
			return;
		}

		// Sleep some time to allow previous test to complete
		Thread.sleep(500);

		// Provide verbose output
		this.setVerbose(true);

		// Provide GC details
		this.setLogGC();

		// Capture start time
		long startTimestamp = System.currentTimeMillis();

		// Construct the team
		if (this.teamConstructor != null) {
			this.teamConstructor.constructTeam(this.teamName, this);
		}

		// Construct the test
		StressContext context = new StressContext(this, this.teamName, this.iterationCount, this.managedObjectScope);
		this.constructTest(context);
		assertNotNull("Must configure initial function name", context.initialFunctionName);
		assertTrue("Must assign responsible team", context.isResponsibleTeamAssigned);
		if (context.managedObjectScope != null) {
			assertTrue("Must set ManagedObject scope", context.isManagedObjectScopeAssigned);
		}

		// Determine if running the type
		if (context.onlyTeamClass != null) {
			assertEquals("Only running " + context.onlyTeamClass.getSimpleName(), context.onlyTeamClass,
					this.teamSourceClass);
		}

		// Capture construction time
		String constructionTime = this.getDisplayRunTime(startTimestamp);

		// Run the test
		startTimestamp = System.currentTimeMillis();
		this.invokeFunction(context.initialFunctionName, context.initialFunctionParameter, this.getMaxWaitTime());
		long endTimestamp = System.currentTimeMillis();
		String executionTime = this.getDisplayRunTime(startTimestamp, endTimestamp);

		// Obtain the effective run time
		float effectiveRunTime = (float) (endTimestamp - startTimestamp) / this.iterationCount;
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMinimumFractionDigits(5);
		String effectiveRunTimeText = formatter.format(effectiveRunTime);

		// Undertake optional validation
		if (context.validation != null) {
			context.validation.run();
		}

		// Ensure appropriate number of iterations undertaken
		assertEquals("Incorrect number of iterations", this.iterationCount, context.iterations.get());

		// Indicate details of run
		this.printMessage("Construct: " + constructionTime);
		this.printMessage("Run      : " + executionTime);
		this.printMessage("Effective: 1 iteration per " + effectiveRunTimeText + " milliseconds");
	}

	/**
	 * Constructs the {@link ManagedObject} for stress testing.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param configurer        {@link Consumer}.
	 * @param factory           {@link Supplier} to create the
	 *                          {@link ManagedObject}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	protected <O extends Enum<O>, F extends Enum<F>> ManagedObjectBuilder<F> constructManagedObject(
			String managedObjectName, Consumer<MetaDataContext<O, F>> configurer, Supplier<ManagedObject> factory) {
		return this.constructManagedObject(managedObjectName, new StressManagedObjectSource<O, F>(configurer, factory),
				this.getOfficeName());
	}

	/**
	 * Constructs the {@link ManagedObject} for stress testing.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param factory           {@link Supplier} to create the {@link Object} of the
	 *                          {@link ManagedObject}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	protected ManagedObjectBuilder<Indexed> constructObject(String managedObjectName, Supplier<Object> factory) {
		return this.constructManagedObject(managedObjectName, (metaData) -> {
		}, () -> {
			return new ManagedObject() {
				@Override
				public Object getObject() {
					return factory.get();
				}
			};
		});
	}

	/**
	 * Stress {@link ManagedObjectSource}.
	 */
	@TestSource
	private static class StressManagedObjectSource<O extends Enum<O>, F extends Enum<F>>
			extends AbstractManagedObjectSource<O, F> {

		/**
		 * {@link MetaDataContext} configurer.
		 */
		private final Consumer<MetaDataContext<O, F>> configurer;

		/**
		 * Factory to create the {@link ManagedObject}.
		 */
		private final Supplier<ManagedObject> factory;

		/**
		 * Instantiate.
		 * 
		 * @param configurer {@link MetaDataContext} configurer.
		 * @param factory    Factory to create the {@link ManagedObject}.
		 */
		public StressManagedObjectSource(Consumer<MetaDataContext<O, F>> configurer, Supplier<ManagedObject> factory) {
			this.configurer = configurer;
			this.factory = factory;
		}

		/*
		 * ================== ManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<O, F> context) throws Exception {

			// Create an instance to determine types
			ManagedObject instance = this.factory.get();

			// Provide meta-data
			try {
				context.setManagedObjectClass(instance.getClass());
				context.setObjectClass(instance.getObject().getClass());
			} catch (Throwable ex) {
				throw fail(ex);
			}

			// Configure the meta-data
			if (configurer != null) {
				configurer.accept(context);
			}
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this.factory.get();
		}
	}

}
