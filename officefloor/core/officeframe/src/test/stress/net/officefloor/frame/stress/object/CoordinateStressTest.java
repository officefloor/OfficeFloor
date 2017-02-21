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
package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.AsynchronousListener;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests coordination of {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CoordinateStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(CoordinateStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		fail("TODO fix up");

		final int MANAGED_OBJECT_WAIT_TIME = 1000;

		// Obtain the office name and builder
		String officeName = this.getOfficeName();

		// Construct the direct use managed object
		this.constructManagedObject("DIRECT_USE", DirectUseManagedObjectSource.class, officeName);

		// Construct the dependency managed object
		ManagedObjectBuilder<Indexed> dependencyBuilder = this.constructManagedObject("DEPENDENCY",
				DependencyManagedObjectSource.class, null);
		dependencyBuilder.setManagingOffice(officeName).setInputManagedObjectName("PROCESS_BOUND");
		dependencyBuilder.setTimeout(MANAGED_OBJECT_WAIT_TIME);
		this.constructTeam("of-DEPENDENCY.MO_TEAM", new OnePersonTeam("MO_TEAM", 100));

		// Construct the function
		CoordinateWork work = new CoordinateWork(context);
		ReflectiveFunctionBuilder function = this.constructFunction(work, "task");
		context.loadResponsibleTeam(function.getBuilder());
		function.buildObject("DIRECT_USE", ManagedObjectScope.FUNCTION).mapDependency(0, "DEPENDENCY");
		function.buildFlow("task", null, false);

		// Bind dependency to the function
		this.bindManagedObject("DEPENDENCY", ManagedObjectScope.FUNCTION, function.getBuilder());

		// Run the coordination
		context.setInitialFunction("task", null);
	}

	/**
	 * Test functionality.
	 */
	public class CoordinateWork {

		private final StressContext context;

		private volatile DirectUseManagedObject previousDirectUse;

		private volatile DependencyManagedObject previousDependency;

		public CoordinateWork(StressContext context) {
			this.context = context;
		}

		public void task(DirectUseManagedObject directUse, ReflectiveFlow flow) {

			// Ensure not the same previous direct use object
			assertNotSame("Should not be same previous direct use", this.previousDirectUse, directUse);
			this.previousDirectUse = directUse;

			// Obtain the dependency and ensure not same as previous
			DependencyManagedObject dependency = directUse.getDependency();
			assertNotNull("Ensure have dependency", dependency);
			assertNotSame("Should not be same previous dependency", this.previousDependency, dependency);
			this.previousDependency = dependency;

			// Ensure asynchronous operations complete
			assertEquals("Dependency should be ready", TransitionState.COORDINATE_COMPLETE, dependency.transitionState);

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Undertake another iteration
			flow.doFlow(null, null);
		}
	}

	/**
	 * {@link ManagedObject} used directly by {@link ManagedFunction}.
	 */
	public static class DirectUseManagedObject implements CoordinatingManagedObject<Indexed> {

		/**
		 * {@link DependencyManagedObject}.
		 */
		private DependencyManagedObject dependency;

		/**
		 * Obtain the {@link DependencyManagedObject}.
		 * 
		 * @return {@link DependencyManagedObject}.
		 */
		public DependencyManagedObject getDependency() {
			return this.dependency;
		}

		/*
		 * ==================== CoordinatingManagedObject ====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
			// Start operation to indicate object not ready for use by task
			this.dependency = (DependencyManagedObject) registry.getObject(0);
			this.dependency.startAsynchronousOperation(TransitionState.LOAD_COMPLETE, TransitionState.COORDINATE_START);
		}

		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

	/**
	 * {@link ManagedObjectSource} for the {@link DirectUseManagedObject}.
	 */
	@TestSource
	public static class DirectUseManagedObjectSource extends AbstractManagedObjectSource<Indexed, None> {

		/*
		 * =================== AbstractManagedObjectSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// Nothing required
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, None> context) throws Exception {
			context.setObjectClass(DirectUseManagedObject.class);
			context.setManagedObjectClass(DirectUseManagedObject.class);

			// Register dependency
			context.addDependency(DependencyManagedObject.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new DirectUseManagedObject();
		}
	}

	/**
	 * Dependency {@link ManagedObject}.
	 */
	public static class DependencyManagedObject implements AsynchronousManagedObject {

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<Indexed> executeContext;

		/**
		 * {@link AsynchronousListener}.
		 */
		private AsynchronousListener listener;

		/**
		 * {@link TransitionState}.
		 */
		public volatile TransitionState transitionState = TransitionState.INIT;

		/**
		 * Initiate.
		 * 
		 * @param executeContext
		 *            {@link ManagedObjectExecuteContext}.
		 */
		public DependencyManagedObject(ManagedObjectExecuteContext<Indexed> executeContext) {
			this.executeContext = executeContext;
		}

		/**
		 * Starts the asynchronous operation.
		 * 
		 * @param expectedState
		 *            Expected {@link TransitionState}.
		 * @param nextState
		 *            Next {@link TransitionState}.
		 */
		public void startAsynchronousOperation(TransitionState expectedState, TransitionState nextState) {
			// Flag asynchronous operation occurring
			this.listener.notifyStarted();

			// Ensure correct state and move to next state
			assertEquals("Incorrect state on start", expectedState, this.transitionState);
			this.transitionState = nextState;

			// Run process for asynchronous operation
			this.executeContext.invokeProcess(0, this, this, 0, null);
		}

		/**
		 * Completes the asynchronous operation.
		 * 
		 * @param nextState
		 *            Next {@link TransitionState}.
		 */
		public void completeAsynchronousOperation(TransitionState nextState) {

			// Flag asynchronous operation complete
			this.transitionState = nextState;
			this.listener.notifyComplete();
		}

		/*
		 * ================== AsynchronousManagedObject ====================
		 */

		@Override
		public void registerAsynchronousListener(AsynchronousListener listener) {
			// Only load the first listener (as second from invoked process)
			if (this.listener == null) {
				this.listener = listener;

				// Start operation on sourcing Managed Object.
				// In other words not ready to coordinate.
				this.startAsynchronousOperation(TransitionState.INIT, TransitionState.LOAD_START);
			}
		}

		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

	/**
	 * Dependency {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class DependencyManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
			implements ManagedFunctionFactory<Indexed, None>, ManagedFunction<Indexed, None> {

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<Indexed> executeContext;

		/*
		 * =================== AbstractManagedObjectSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// Nothing required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

			// Registry types
			context.setObjectClass(DependencyManagedObject.class);
			context.setManagedObjectClass(DependencyManagedObject.class);

			// Register function to active object
			ManagedObjectFunctionBuilder<Indexed, None> taskBuilder = mosContext.addManagedFunction("TASK", this);
			taskBuilder.linkParameter(0, DependencyManagedObject.class);
			taskBuilder.setResponsibleTeam("MO_TEAM");

			// Register flow to run task to activate object
			context.addFlow(DependencyManagedObject.class);
			mosContext.linkProcess(0, "TASK");
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {
			this.executeContext = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new DependencyManagedObject(this.executeContext);
		}

		/*
		 * ============== ManagedFunctionFactory, ManagedFunction ==============
		 */

		@Override
		public ManagedFunction<Indexed, None> createManagedFunction() {
			return this;
		}

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the dependency managed object
			DependencyManagedObject dependency = (DependencyManagedObject) context.getObject(0);

			// Indicate flag next state
			switch (dependency.transitionState) {
			case LOAD_START:
				dependency.completeAsynchronousOperation(TransitionState.LOAD_COMPLETE);
				break;
			case COORDINATE_START:
				dependency.completeAsynchronousOperation(TransitionState.COORDINATE_COMPLETE);
				break;
			default:
				fail("Invalid state " + dependency.transitionState);
			}

			// No further processing
			return null;
		}
	}

	/**
	 * States indicating state of coordination.
	 */
	private enum TransitionState {
		INIT, LOAD_START, LOAD_COMPLETE, COORDINATE_START, COORDINATE_COMPLETE
	}

}