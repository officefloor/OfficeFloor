/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link AsynchronousManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousObjectStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AsynchronousObjectStressTest.class);
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

		// Construct the managed object
		this.constructManagedObject("ASYNCHRONOUS", null, () -> new Asynchronous()).setTimeout(1000);

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadResponsibleTeam(task.getBuilder());
		task.buildObject("ASYNCHRONOUS", context.getManagedObjectScope());
		task.buildFlow("spawn", Asynchronous.class, true);
		task.buildFlow("task", null, false);
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

		public void task(Asynchronous asynchronous, ReflectiveFlow spawn, ReflectiveFlow repeat) {

			// Ensure not within asynchronous operation
			assertFalse("No asynchronous operation for function", asynchronous.isWithinAsynchronousOperation);

			// Determine if continue
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Trigger asynchronous operation
			asynchronous.isWithinAsynchronousOperation = true;
			asynchronous.listener.start(null);

			// Spawn thread state to complete operation
			spawn.doFlow(asynchronous, (escalation) -> {
				assertNull("Should be now failure", escalation);
				assertFalse("No asynchronous operation for callback", asynchronous.isWithinAsynchronousOperation);

				// Repeat
				repeat.doFlow(null, null);
			});
		}

		public void spawn(Asynchronous asynchronous) {
			// Notify asynchronous complete
			asynchronous.isWithinAsynchronousOperation = false;
			asynchronous.listener.complete(null);
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