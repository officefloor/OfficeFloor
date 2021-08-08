/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link ContextAwareManagedObject} for
 * {@link ProcessSafeOperation}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessSafeContextAwareObjectStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(ProcessSafeContextAwareObjectStressTest.class);
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
	 * Stress {@link ContextAwareManagedObject}.
	 */
	public static class StressProcessAwareManagedObject implements ContextAwareManagedObject {

		private ManagedObjectContext context;

		private long value = 0;

		public long increment() {
			return this.context.run(() -> {
				value++;
				return value;
			});
		}

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			this.context = context;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
