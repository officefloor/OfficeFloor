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

package net.officefloor.frame.impl.execute.managedobject.asynchronous;

import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure issue if register {@link AsynchronousContext} fails.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_AsynchronousManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handle failure to register {@link AsynchronousContext}.
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
