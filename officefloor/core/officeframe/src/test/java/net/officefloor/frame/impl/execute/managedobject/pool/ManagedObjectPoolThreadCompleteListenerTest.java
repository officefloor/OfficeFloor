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

import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Tests the {@link Thread} complete listener of the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolThreadCompleteListenerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link ThreadCompletionListener} is invoked.
	 */
	public void testThreadCompleteListener() throws Exception {

		// Create the test object
		TestObject object = new TestObject("MO", this);
		ManagedObjectPoolBuilder poolBuilder = object.managedObjectBuilder
				.setManagedObjectPool((context) -> new TestManagedObjectPool(context));
		poolBuilder.addThreadCompletionListener((pool) -> (ThreadCompletionListener) pool);

		// Create worker per job team (allows thread to clean up)
		this.constructTeam("TEAM", WorkerPerJobTeamSource.class);

		// Create the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.buildObject("MO", ManagedObjectScope.THREAD);
		function.getBuilder().setResponsibleTeam("TEAM");

		// Invoke the function
		this.invokeFunction("function", null);

		// Wait for thread completion listener (on thread completion)
		this.waitForTrue(() -> this.threadCompleteManagedObject != null);
	}

	private volatile ManagedObject threadCompleteManagedObject = null;

	public class TestWork {
		public void function(TestObject object) {
		}
	}

	public class TestManagedObjectPool implements ManagedObjectPool, ThreadCompletionListener {

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
			assertTrue("Should be managed thread", this.context.isCurrentThreadManaged());
			this.context.getManagedObjectSource().sourceManagedObject(user);
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			this.cachedManagedObject.set(managedObject);
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
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
			ManagedObjectPoolThreadCompleteListenerTest.this.threadCompleteManagedObject = this.cachedManagedObject
					.get();
		}
	}

}
