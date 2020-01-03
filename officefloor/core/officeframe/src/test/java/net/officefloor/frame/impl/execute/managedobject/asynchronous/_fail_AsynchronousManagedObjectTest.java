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
