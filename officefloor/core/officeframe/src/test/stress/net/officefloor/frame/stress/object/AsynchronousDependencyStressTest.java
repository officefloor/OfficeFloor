package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link AsynchronousManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousDependencyStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AsynchronousDependencyStressTest.class);
	}

	@Override
	protected boolean isTestEachManagedObjectScope() {
		return true;
	}

	@Override
	protected int getIterationCount() {
		return 100000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the managed objects
		this.constructManagedObject("COORDINATE", (metaData) -> metaData.addDependency(Asynchronous.class),
				() -> new Coordinate());
		this.constructManagedObject("ASYNCHRONOUS", null, () -> new Asynchronous()).setTimeout(1000);

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadResponsibleTeam(task.getBuilder());
		task.buildObject("COORDINATE", ManagedObjectScope.FUNCTION).mapDependency(0, "ASYNCHRONOUS");
		task.buildFlow("spawn", Coordinate.class, true);
		task.buildFlow("task", null, false);
		this.bindManagedObject("ASYNCHRONOUS", context.getManagedObjectScope(), task.getBuilder());
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		context.loadOtherTeam(spawn.getBuilder());
		spawn.buildParameter();

		// Run
		context.setInitialFunction("task", null);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void task(Coordinate coordinate, ReflectiveFlow spawn, ReflectiveFlow repeat) {

			// Ensure not within asynchronous operation
			assertFalse("No asynchronous operation for function",
					coordinate.asynchronous.isWithinAsynchronousOperation);

			// Determine if continue
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Trigger asynchronous operation
			coordinate.asynchronous.isWithinAsynchronousOperation = true;
			coordinate.asynchronous.listener.start(null);

			// Spawn thread state to complete operation
			spawn.doFlow(coordinate, (escalation) -> {
				assertFalse("No asynchronous operation for callback",
						coordinate.asynchronous.isWithinAsynchronousOperation);

				// Repeat
				repeat.doFlow(null, null);
			});
		}

		public void spawn(Coordinate coordinate) {
			// Notify asynchronous complete
			coordinate.asynchronous.isWithinAsynchronousOperation = false;
			coordinate.asynchronous.listener.complete(null);
		}
	}

	/**
	 * {@link CoordinatingManagedObject}.
	 */
	public static class Coordinate implements CoordinatingManagedObject<Indexed> {

		private Asynchronous asynchronous;

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
			this.asynchronous = (Asynchronous) registry.getObject(0);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * {@link AsynchronousManagedObject}.
	 */
	private static class Asynchronous implements AsynchronousManagedObject {

		private AsynchronousContext listener;

		private volatile boolean isWithinAsynchronousOperation = false;

		@Override
		public void setAsynchronousContext(AsynchronousContext listener) {
			this.listener = listener;
		}

		@Override
		public Object getObject() {
			return this;
		}
	}

}