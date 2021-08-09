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
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param testCase          {@link AbstractOfficeConstructTestCase}.
	 */
	public TestObject(String managedObjectName, AbstractOfficeConstructTestCase testCase) {
		super(managedObjectName, testCase);
	}

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param testCase          {@link AbstractOfficeConstructTestCase}.
	 * @param isPool            <code>true</code> to construct a
	 *                          {@link ManagedObjectPool}.
	 */
	public TestObject(String managedObjectName, AbstractOfficeConstructTestCase testCase, boolean isPool) {
		super(managedObjectName, testCase, isPool);
	}

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param construct         {@link ConstructTestSupport}.
	 */
	public TestObject(String managedObjectName, ConstructTestSupport construct) {
		super(managedObjectName, construct);
	}

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param construct         {@link ConstructTestSupport}.
	 * @param isPool            <code>true</code> to construct a
	 *                          {@link ManagedObjectPool}.
	 */
	public TestObject(String managedObjectName, ConstructTestSupport construct, boolean isPool) {
		super(managedObjectName, construct, isPool);
	}

}
