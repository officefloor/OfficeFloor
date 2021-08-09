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

package net.officefloor.frame.impl.execute.managedobject.pool;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestManagedObject;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure {@link ManagedObjectSource} able to specify default
 * {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultManagedObjectPoolTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link TestManagedObject}.
	 */
	private TestObject object;

	/**
	 * Indicates if {@link DefaultManagedObjectPool}.
	 */
	private boolean isDefaultPool = false;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Load the managed object (with default managed object pool)
		this.object = new TestObject("MO", this);
		this.object.enhanceMetaData = (metaData) -> {
			metaData.getManagedObjectSourceContext().setDefaultManagedObjectPool(
					(context) -> new DefaultManagedObjectPool(context.getManagedObjectSource()));
		};

		// Construct the function
		this.constructFunction(new TestWork(), "task").buildObject("MO", ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure {@link ManagedObjectSource} can provide default
	 * {@link ManagedObjectPool}.
	 */
	public void testUseDefaultManagedObjectPool() throws Throwable {

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure default managed object pool is used
		assertTrue("Should use default managed object", this.isDefaultPool);
	}

	/**
	 * Ensure can override the default {@link ManagedObjectPool} from the
	 * {@link ManagedObjectSource}.
	 */
	public void testOverrideDefaultManagedObjectPool() throws Throwable {

		// Override the managed object pool
		this.object.managedObjectBuilder
				.setManagedObjectPool((context) -> new OverrideManagedObjectPool(context.getManagedObjectSource()));

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure override managed object pool
		assertFalse("Should not use default managed object pool as overridden", this.isDefaultPool);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {
		public void task(TestObject object) {
		}
	}

	/**
	 * Default {@link ManagedObjectPool}.
	 */
	private class DefaultManagedObjectPool implements ManagedObjectPool {

		/**
		 * {@link ManagedObjectSource}.
		 */
		private final ManagedObjectSource<?, ?> managedObjectSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSource {@link ManagedObjectSource}.
		 */
		private DefaultManagedObjectPool(ManagedObjectSource<?, ?> managedObjectSource) {
			this.managedObjectSource = managedObjectSource;
		}

		/*
		 * ================== ManagedObjectPool ==================
		 */

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			DefaultManagedObjectPoolTest.this.isDefaultPool = true;
			this.managedObjectSource.sourceManagedObject(user);
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			// ignored
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			// ignored
		}

		@Override
		public void empty() {
			// nothing to clean
		}
	}

	/**
	 * Override {@link ManagedObjectPool}.
	 */
	private static class OverrideManagedObjectPool implements ManagedObjectPool {

		/**
		 * {@link ManagedObjectSource}.
		 */
		private final ManagedObjectSource<?, ?> managedObjectSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSource {@link ManagedObjectSource}.
		 */
		private OverrideManagedObjectPool(ManagedObjectSource<?, ?> managedObjectSource) {
			this.managedObjectSource = managedObjectSource;
		}

		/*
		 * ================== ManagedObjectPool ==================
		 */

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			this.managedObjectSource.sourceManagedObject(user);
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
		}

		@Override
		public void empty() {
		}
	}

}
