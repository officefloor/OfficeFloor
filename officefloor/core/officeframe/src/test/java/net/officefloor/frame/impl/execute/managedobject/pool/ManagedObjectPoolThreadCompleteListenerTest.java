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

package net.officefloor.frame.impl.execute.managedobject.pool;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;

/**
 * Tests the {@link Thread} complete listener of the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ManagedObjectPoolThreadCompleteListenerTest {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * {@link ThreadedTestSupport}.
	 */
	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Ensure {@link ThreadCompletionListener} is invoked.
	 */
	@Test
	public void threadCompleteListener() throws Exception {

		// Create the test object
		TestObject object = new TestObject("MO", this.construct);
		Closure<TestManagedObjectPool> pool = new Closure<>();
		ManagedObjectPoolBuilder poolBuilder = object.managedObjectBuilder.setManagedObjectPool((context) -> {
			pool.value = new TestManagedObjectPool(context);
			return pool.value;
		});
		poolBuilder.addThreadCompletionListener((listener) -> (ThreadCompletionListener) listener);

		// Create worker per job team (allows thread to clean up)
		this.construct.constructTeam("TEAM", WorkerPerJobTeamSource.class);

		// Create the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.construct.constructFunction(work, "function");
		function.buildObject("MO", ManagedObjectScope.THREAD);
		function.getBuilder().setResponsibleTeam("TEAM");

		// Invoke the function
		try (OfficeFloor officeFloor = this.construct.invokeFunction("function", null)) {

			// Wait for function to be invoked
			this.threading.waitForTrue(() -> work.isInvoked);
		}

		// Wait for thread complete managed object (on thread completion)
		this.threading.waitForTrue(() -> pool.value.threadCompleteManagedObject != null);
	}

	public class TestWork {

		private volatile boolean isInvoked = false;

		public void function(TestObject object) {
			this.isInvoked = true;
		}
	}

	public class TestManagedObjectPool implements ManagedObjectPool, ThreadCompletionListener {

		private final ManagedObjectPoolContext context;

		private final ThreadLocal<ManagedObject> cachedManagedObject = new ThreadLocal<>();

		private volatile ManagedObject threadCompleteManagedObject = null;

		public TestManagedObjectPool(ManagedObjectPoolContext context) {
			this.context = context;
		}

		/*
		 * ================= ManagedObjectPool =======================
		 */

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			assertTrue(this.context.isCurrentThreadManaged(), "Should be managed thread");
			this.context.getManagedObjectSource().sourceManagedObject(user);
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			this.cachedManagedObject.set(managedObject);
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			fail("Nothing should be lost");
		}

		@Override
		public void empty() {
			// nothing to clean
		}

		/*
		 * =============== ThreadCompletionListener =================
		 */

		@Override
		public void threadComplete() {
			// Threads may complete in any order
			ManagedObject mo = this.cachedManagedObject.get();
			if (mo != null) {
				assertNull(this.threadCompleteManagedObject, "Should only complete thread once for managed object");
				this.threadCompleteManagedObject = mo;
			}
		}
	}

}
