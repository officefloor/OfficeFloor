package net.officefloor.frame.test;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * Test {@link ManagedObject} that simplifies the {@link TestManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class TestObject extends TestManagedObject<None, None> {

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 */
	public TestObject(String managedObjectName, AbstractOfficeConstructTestCase testCase) {
		super(managedObjectName, testCase);
	}

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 * @param isPool
	 *            <code>true</code> to construct a {@link ManagedObjectPool}.
	 */
	public TestObject(String managedObjectName, AbstractOfficeConstructTestCase testCase, boolean isPool) {
		super(managedObjectName, testCase, isPool);
	}

}