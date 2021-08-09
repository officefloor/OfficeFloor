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

package net.officefloor.frame.stress.pool;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestSuite;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests the {@link Thread} complete listener of the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalManagedObjectPoolStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(ThreadLocalManagedObjectPoolStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 1000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Allow larger chain due to threads
		this.getOfficeBuilder().setMaximumFunctionStateChainLength(10000);

		// Create the test object
		ManagedObjectBuilder<?> object = this.constructManagedObject("MO", TestManagedObjectSource.class,
				this.getOfficeName());
		object.setManagedObjectPool((poolContext) -> new TestManagedObjectPool(poolContext));

		// Create the function
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.buildFlow("first", null, true);
		context.loadOtherTeam(function.getBuilder());

		// Create the first function in another thread
		ReflectiveFunctionBuilder first = this.constructFunction(work, "first");
		first.buildObject("MO", ManagedObjectScope.FUNCTION);
		first.setNextFunction("second");
		context.loadResponsibleTeam(first.getBuilder());

		// Create the second function (using same thread)
		ReflectiveFunctionBuilder second = this.constructFunction(work, "second");
		second.buildObject("MO", ManagedObjectScope.FUNCTION);
		// any team will use thread of first function

		// Test
		context.setInitialFunction("function", null);

		// Validate (+1 as increments before loop check)
		context.setValidation(() -> assertEquals("Incorrect number of increments", context.getMaximumIterations(),
				work.invocationCount.get() + 1));
	}

	public static class TestWork {

		private final StressContext context;

		private AtomicInteger invocationCount = new AtomicInteger(0);

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void function(ReflectiveFlow flow) {
			while (!this.context.incrementIterationAndIsComplete()) {
				flow.doFlow(null, (failure) -> {
					assertNull("Should be no failure: " + failure, failure);
				});
			}
		}

		public void first(ThreadObject object) {
			assertEquals("Incorrect thread for object", Thread.currentThread(), object.creationThread);
			assertEquals("Should be first use of object", 1, object.usedCount);
		}

		public void second(ThreadObject object) {
			assertEquals("Incorrect thread for object", Thread.currentThread(), object.creationThread);
			assertEquals("Should be re-use of object from pool", 2, object.usedCount);
			this.invocationCount.incrementAndGet();

			// Reset the used count (as thread pools re-use threads)
			object.usedCount = 0;
		}
	}

	@TestSource
	public static class TestManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(ThreadObject.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new ThreadObject(Thread.currentThread());
		}
	}

	public static class ThreadObject implements ManagedObject {

		public final Thread creationThread;

		public int usedCount = 1;

		public ThreadObject(Thread creationThread) {
			this.creationThread = creationThread;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	public static class TestManagedObjectPool implements ManagedObjectPool {

		private final ManagedObjectPoolContext context;

		private final ThreadLocal<ManagedObject> cachedManagedObject = new ThreadLocal<>();

		public TestManagedObjectPool(ManagedObjectPoolContext context) {
			this.context = context;
		}

		/*
		 * ================= ManagedObjectPool =======================
		 */

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {

			// Must be managed thread
			assertTrue("Must be managed thread", this.context.isCurrentThreadManaged());

			// Determine if cached on thread
			ManagedObject cached = this.cachedManagedObject.get();
			if (cached != null) {
				// Clear cache
				this.cachedManagedObject.set(null);

				// Use the cached managed object
				((ThreadObject) cached).usedCount++;
				user.setManagedObject(cached);

			} else {
				// Source the managed object
				this.context.getManagedObjectSource().sourceManagedObject(user);
			}
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			this.cachedManagedObject.set(managedObject);
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			fail("Should not loose object");
		}

		@Override
		public void empty() {
			// Nothing to clean
		}
	}

}
