/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.managedobject.asynchronous;

import net.officefloor.frame.api.managedobject.AsynchronousListener;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure issue if register {@link AsynchronousListener} fails.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_AsynchronousManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handle failure to register {@link AsynchronousListener}.
	 */
	public void testHandleFailureToRegisterAsynchronousListener() throws Exception {

		// Configure the object
		TestObject object = new TestObject("MO", this);
		object.isAsynchronousManagedObject = true;
		object.registerAsynchronousListenerFailure = new RuntimeException("TEST");
		object.managedObjectBuilder.setTimeout(10);

		// Configure the function
		this.constructFunction(new TestWork(), "task").buildObject("MO", ManagedObjectScope.FUNCTION);

		// Undertake the co-ordination
		Closure<Throwable> failure = new Closure<>();
		this.triggerFunction("task", null, (escalation) -> failure.value = escalation);

		// Ensure issue in registering asynchronous listener
		assertSame("Incorrect co-ordination failure", object.registerAsynchronousListenerFailure, failure.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {
		public void task(TestObject object) {
		}
	}

}
