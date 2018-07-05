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
package net.officefloor.frame.impl.execute.managedobject.coordinate;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure issue if fail to co-ordinate the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_CoordinateManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handle failure to co-ordinate the {@link ManagedObject}.
	 */
	public void testHandleFailureToCoordinateManagedObject() throws Exception {

		// Configure the object
		TestObject object = new TestObject("MO", this);
		object.isCoordinatingManagedObject = true;
		object.loadObjectsFailure = new RuntimeException("TEST");

		// Configure the function
		this.constructFunction(new TestWork(), "task").buildObject("MO", ManagedObjectScope.FUNCTION);

		// Undertake the co-ordination
		Closure<Throwable> failure = new Closure<>();
		this.triggerFunction("task", null, (escalation) -> failure.value = escalation);

		// Ensure issue in co-ordination
		assertSame("Incorrect co-ordination failure", object.loadObjectsFailure, failure.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {
		public void task(TestObject object) {
		}
	}

}
