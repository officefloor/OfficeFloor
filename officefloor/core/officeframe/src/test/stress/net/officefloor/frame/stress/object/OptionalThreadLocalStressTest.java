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

package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests the {@link OptionalThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public class OptionalThreadLocalStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(OptionalThreadLocalStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the object
		this.constructManagedObject("MOS", TestManagedObjectSource.class, this.getOfficeName());
		OptionalThreadLocal<TestManagedObjectSource> threadLocal = this.getOfficeBuilder()
				.addThreadManagedObject("MO", "MOS").getOptionalThreadLocal();

		// Create trigger function
		TestWork work = new TestWork(context, threadLocal);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildFlow("thread", TestManagedObjectSource.class, true);
		task.buildObject("MO");
		task.buildFlow("task", null, false);
		context.loadOtherTeam(task.getBuilder());

		// Create thread function
		ReflectiveFunctionBuilder thread = this.constructFunction(work, "thread");
		thread.buildParameter();
		thread.buildObject("MO");
		context.loadResponsibleTeam(task.getBuilder());

		// Test
		context.setInitialFunction("task", null);
	}

	@TestSource
	public static class TestManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new TestManagedObjectSource();
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	public static class TestWork {

		private final StressContext context;

		private final OptionalThreadLocal<TestManagedObjectSource> threadLocal;

		public TestWork(StressContext context, OptionalThreadLocal<TestManagedObjectSource> threadLocal) {
			this.context = context;
			this.threadLocal = threadLocal;
		}

		public void task(ReflectiveFlow flow, TestManagedObjectSource object, ReflectiveFlow repeat) {

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Undertake thread
			flow.doFlow(object, (exception) -> {

				// Ensure no failure
				if (exception != null) {
					throw exception;
				}

				// Undertake next thread
				repeat.doFlow(null, null);
			});
		}

		public void thread(TestManagedObjectSource parameter, TestManagedObjectSource object) {
			assertNotNull("Should have parameter", parameter);
			assertNotNull("Should have object", object);
			assertNotSame("Should be different object for thread", parameter, object);
			TestManagedObjectSource threadLocalObject = this.threadLocal.get();
			assertSame("Incorrect thread local", object, threadLocalObject);
		}
	}

}
