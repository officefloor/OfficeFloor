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