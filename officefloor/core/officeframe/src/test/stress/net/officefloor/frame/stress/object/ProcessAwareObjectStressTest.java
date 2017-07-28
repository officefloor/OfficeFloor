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
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link ProcessAwareManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareObjectStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(ProcessAwareObjectStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 100000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the managed object
		StressProcessAwareManagedObject managedObject = new StressProcessAwareManagedObject();
		this.constructManagedObject("MO", null, () -> managedObject);

		// Construct the functions
		TestWork work = new TestWork(context, 10);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadOtherTeam(task.getBuilder());
		task.buildObject("MO", ManagedObjectScope.PROCESS);
		task.buildFlow("spawn", StressProcessAwareManagedObject.class, true);
		task.buildFlow("task", null, false);
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		context.loadResponsibleTeam(spawn.getBuilder());
		spawn.buildParameter();

		// Test
		context.setInitialFunction("task", null);

		// Validate (+1 for additional increment to get value)
		context.setValidation(() -> assertEquals("Incorrect number of increments",
				(context.getMaximumIterations() * work.spawnCount) + 1, managedObject.increment()));
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final StressContext context;

		private final int spawnCount;

		public TestWork(StressContext context, int spawnCount) {
			this.context = context;
			this.spawnCount = spawnCount;
		}

		public void task(StressProcessAwareManagedObject managedObject, ReflectiveFlow spawn, ReflectiveFlow repeat) {

			// Spawn process (a few times)
			for (int i = 0; i < 10; i++) {
				spawn.doFlow(managedObject, (escalation) -> {
				});
			}

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Repeat
			repeat.doFlow(null, null);
		}

		public void spawn(StressProcessAwareManagedObject parameter) {
			parameter.increment();
		}
	}

	/**
	 * Stress {@link ProcessAwareManagedObject}.
	 */
	public static class StressProcessAwareManagedObject implements ProcessAwareManagedObject {

		private ProcessAwareContext context;

		private long value = 0;

		public long increment() {
			return this.context.run(() -> {
				value++;
				return value;
			});
		}

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
			this.context = context;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}