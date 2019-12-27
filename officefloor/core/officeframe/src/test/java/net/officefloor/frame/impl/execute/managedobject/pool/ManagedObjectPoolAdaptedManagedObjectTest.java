/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.managedobject.pool;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestManagedObject;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure able to adapt the {@link ManagedObject} when pooling them. This allows
 * for adapted objects that, for example, can be used for {@link ThreadLocal}
 * cache improvements.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolAdaptedManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link TestManagedObject}.
	 */
	private TestObject object;

	/**
	 * {@link ReflectiveFunctionBuilder} for the function.
	 */
	private ReflectiveFunctionBuilder function;;

	/**
	 * Indicates if adapted the {@link ManagedObject}.
	 */
	private boolean isAdapted = false;

	/**
	 * Indicates if administered the managed object.
	 */
	private boolean isAdministered = false;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Load the managed object
		this.object = new TestObject("MO", this, true);
		this.object.managedObjectBuilder
				.setManagedObjectPool((context) -> new MockAdaptManagedObjectPool(context.getManagedObjectSource()));

		// Construct the function
		this.function = this.constructFunction(new TestWork(), "task");
		this.function.buildObject("MO", ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure recycle is provided {@link ManagedObject} from
	 * {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public void testRecycle() throws Throwable {

		// Configure recycling
		this.object.isRecycleFunction = true;
		Closure<Boolean> isRecycled = new Closure<>(false);
		this.object.recycleConsumer = (parameter) -> {
			RecycleManagedObjectParameter<ManagedObject> recycle = (RecycleManagedObjectParameter<ManagedObject>) parameter;
			ManagedObject managedObject = recycle.getManagedObject();
			assertEquals("Should be sourced managed object (not adapted)", this.object, managedObject);
			isRecycled.value = true;
			recycle.reuseManagedObject();
		};

		// Undertake function
		this.invokeFunction("task", null);

		// Ensure adapted and appropriately recycled
		assertTrue("Should adapt managed object", this.isAdapted);
		assertTrue("Should be successfully recycled", isRecycled.value);
	}

	/**
	 * Ensure able to load contexts to the sourced {@link ManagedObject}.
	 */
	public void testLoadContexts() throws Throwable {

		// Configure context
		this.object.isContextAwareManagedObject = true;
		this.object.isAsynchronousManagedObject = true;
		this.object.isCoordinatingManagedObject = true;
		this.object.managedObjectBuilder.setTimeout(1000);

		// Undertake function
		this.invokeFunction("task", null);

		// Ensure the sourced managed object gets loaded
		assertTrue("Should adapt managed object", this.isAdapted);
		assertNotNull("Ensure have managed object context", this.object.managedObjectContext);
		assertEquals("Incorrect name", "MO", this.object.managedObjectContext.getBoundName());
		assertNotNull("Ensure have asynchronous context", this.object.asynchronousContext);
		assertNotNull("Ensure have object registry", this.object.objectRegistry);
	}

	/**
	 * Ensure able to retrieve extensions.
	 */
	public void testExtensions() throws Throwable {

		// Configure extension
		Closure<Boolean> isExtensionCreated = new Closure<>(false);
		this.object.enhanceMetaData = (metaData) -> {
			metaData.addManagedObjectExtension(Object.class, (mo) -> {
				assertSame("Should be source managed object", this.object, mo);
				isExtensionCreated.value = true;
				return mo;
			});
		};

		// Configure administration
		this.function.postAdminister("admin").administerManagedObject("MO");

		// Undertake function
		this.invokeFunction("task", null);

		// Should adapt the managed object
		assertTrue("Should adapt managed object", this.isAdapted);
		assertTrue("Should create extension from source managed object", isExtensionCreated.value);
		assertTrue("Should administer the managed object", this.isAdministered);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
		}

		public void admin(Object[] extensions) {
			Object extension = extensions[0];
			assertSame("Should be provided source managed object",
					ManagedObjectPoolAdaptedManagedObjectTest.this.object, extension);
			ManagedObjectPoolAdaptedManagedObjectTest.this.isAdministered = true;
		}
	}

	/**
	 * Mock {@link ManagedObjectPool} that adapts the {@link ManagedObject}.
	 */
	private class MockAdaptManagedObjectPool implements ManagedObjectPool {

		/**
		 * {@link ManagedObjectSource}.
		 */
		private final ManagedObjectSource<?, ?> managedObjectSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSource {@link ManagedObjectSource}.
		 */
		private MockAdaptManagedObjectPool(ManagedObjectSource<?, ?> managedObjectSource) {
			this.managedObjectSource = managedObjectSource;
		}

		/*
		 * ================ ManagedObjectPool ==================
		 */

		@Override
		public ManagedObject getSourcedManagedObject(ManagedObject pooledManagedObject) {
			return ((AdaptedManagedObject) pooledManagedObject).delegate;
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			this.managedObjectSource.sourceManagedObject(new ManagedObjectUser() {

				@Override
				public void setManagedObject(ManagedObject managedObject) {
					ManagedObjectPoolAdaptedManagedObjectTest.this.isAdapted = true;
					user.setManagedObject(new AdaptedManagedObject(managedObject));
				}

				@Override
				public void setFailure(Throwable cause) {
					user.setFailure(cause);
				}
			});
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			assertTrue("Should be provided the adapted managed object", managedObject instanceof AdaptedManagedObject);
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			assertTrue("Should be provided the adapted managed object", managedObject instanceof AdaptedManagedObject);
		}

		@Override
		public void empty() {
			// nothing to clean
		}
	}

	/**
	 * Adapted {@link ManagedObject}.
	 */
	private static class AdaptedManagedObject implements ManagedObject {

		/**
		 * {@link ManagedObject} being adapted.
		 */
		private final ManagedObject delegate;

		/**
		 * Instantiate.
		 * 
		 * @param delegate Delegate {@link ManagedObject}.
		 */
		private AdaptedManagedObject(ManagedObject delegate) {
			this.delegate = delegate;
		}

		/*
		 * =============== ManagedObject =======================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.delegate.getObject();
		}
	}

}