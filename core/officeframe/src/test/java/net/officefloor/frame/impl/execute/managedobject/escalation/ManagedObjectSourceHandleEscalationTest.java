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

package net.officefloor.frame.impl.execute.managedobject.escalation;

import java.util.logging.Logger;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.TestObject;

/**
 * Ensures the {@link ManagedObjectSource} {@link FlowCallback} can handle
 * {@link Escalation}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceHandleEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures {@link Escalation} is handled by the {@link Office}
	 * {@link EscalationProcedure} before the {@link FlowCallback}.
	 */
	public void test_Escalation_HandledBy_OfficeEscalationProcedure() throws Throwable {

		// Fail sourcing managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addFlow(Exception.class);
		};
		object.managingOfficeBuilder.setInputManagedObjectName("MO");
		object.managingOfficeBuilder.linkFlow(0, "escalate");

		// Construct functions
		TestWork work = new TestWork();
		this.constructFunction(work, "escalate").buildParameter();
		this.constructFunction(work, "handle").buildParameter();

		// Construct escalation procedure
		this.getOfficeBuilder().addEscalation(Exception.class, "handle");

		// Invoke the task
		final Exception exception = new Exception("TEST");
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		this.constructOfficeFloor().openOfficeFloor();
		object.managedObjectServiceContext.invokeProcess(0, exception, object, 0, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Ensure escalation handled by office escalation procedure
		assertTrue("Process should be complete", isComplete.value);
		assertNull("Should be no process escalation", failure.value);
		assertSame("Escalation handled by office escalation procedure", exception, work.escalation);
	}

	/**
	 * Ensure {@link Escalation} able to be handled by {@link ManagedObjectSource}
	 * invoking {@link FlowCallback}.
	 */
	public void test_Escalation_HandledBy_FlowCallback() throws Throwable {

		// Fail sourcing managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addFlow(Exception.class);
		};
		object.managingOfficeBuilder.setInputManagedObjectName("MO");
		object.managingOfficeBuilder.linkFlow(0, "escalate");

		// Construct functions
		TestWork work = new TestWork();
		this.constructFunction(work, "escalate").buildParameter();

		// Invoke the task
		final Exception exception = new Exception("TEST");
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		this.constructOfficeFloor().openOfficeFloor();
		object.managedObjectServiceContext.invokeProcess(0, exception, object, 0, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Ensure escalation handled by flow callback
		assertTrue("Process should be complete", isComplete.value);
		assertSame("Should escalate to flow callback", exception, failure.value);
	}

	/**
	 * Ensure if no {@link FlowCallback} provided, that handled immediately by
	 * {@link OfficeFloor} {@link EscalationHandler}.
	 */
	public void test_Escalation_HandledBy_OfficeFloorEscalation() throws Throwable {

		// Fail sourcing managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addFlow(Exception.class);
		};
		object.managingOfficeBuilder.setInputManagedObjectName("MO");
		object.managingOfficeBuilder.linkFlow(0, "escalate");

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "escalate").buildParameter();

		// Handle escalation
		Closure<Throwable> failure = new Closure<>();
		this.getOfficeFloorBuilder().setEscalationHandler((escalation) -> {
			failure.value = escalation;
		});

		// Invoke the task
		final Exception exception = new Exception("TEST");
		this.constructOfficeFloor().openOfficeFloor();
		object.managedObjectServiceContext.invokeProcess(0, exception, object, 0, null);

		// Ensure escalation handled by OfficeFloor escalation
		assertSame("Should escalate to OfficeFloor escalation", exception, failure.value);
	}

	/**
	 * Ensure {@link Office} {@link EscalationProcedure} failure is handled by
	 * {@link FlowCallback}.
	 */
	public void test_OfficeEscalationProcedureFailure_HandledBy_FlowCallback() throws Throwable {

		// Fail sourcing managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addFlow(Exception.class);
		};
		object.managingOfficeBuilder.setInputManagedObjectName("MO");
		object.managingOfficeBuilder.linkFlow(0, "escalate");

		// Construct functions
		TestWork work = new TestWork();
		work.isHandleEscalate = true;
		this.constructFunction(work, "escalate").buildParameter();
		this.constructFunction(work, "handle").buildParameter();

		// Construct escalation procedure
		this.getOfficeBuilder().addEscalation(Exception.class, "handle");

		// Invoke the task
		final Exception exception = new Exception("TEST");
		Closure<Boolean> isComplete = new Closure<>(false);
		Closure<Throwable> failure = new Closure<>();
		this.constructOfficeFloor().openOfficeFloor();
		object.managedObjectServiceContext.invokeProcess(0, exception, object, 0, (escalation) -> {
			isComplete.value = true;
			failure.value = escalation;
		});

		// Ensure escalation handled by flow callback
		assertTrue("Process should be complete", isComplete.value);
		assertSame("Should attempt to handle escalation", exception, work.escalation);
		assertSame("Should escalate to flow callback", exception, failure.value);
	}

	/**
	 * Ensures if no {@link FlowCallback} provided, that {@link Office}
	 * {@link EscalationProcedure} failure is handled by {@link OfficeFloor}
	 * {@link EscalationHandler}.
	 */
	public void test_OfficeEscalationProcedureFailure_HandledBy_OfficeFloorEscalation() throws Throwable {

		// Fail sourcing managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addFlow(Exception.class);
		};
		object.managingOfficeBuilder.setInputManagedObjectName("MO");
		object.managingOfficeBuilder.linkFlow(0, "escalate");

		// Construct functions
		TestWork work = new TestWork();
		work.isHandleEscalate = true;
		this.constructFunction(work, "escalate").buildParameter();
		this.constructFunction(work, "handle").buildParameter();

		// Construct escalation procedure
		this.getOfficeBuilder().addEscalation(Exception.class, "handle");

		// Handle escalation
		Closure<Throwable> failure = new Closure<>();
		this.getOfficeFloorBuilder().setEscalationHandler((escalation) -> {
			failure.value = escalation;
		});

		// Invoke the task
		final Exception exception = new Exception("TEST");
		this.constructOfficeFloor().openOfficeFloor();
		object.managedObjectServiceContext.invokeProcess(0, exception, object, 0, null);

		// Ensure escalation handled by OfficeFloor handler
		assertSame("Should attempt to handle escalation", exception, work.escalation);
		assertSame("Should escalate to OfficeFloor handler", exception, failure.value);
	}

	/**
	 * Ensures the {@link ManagedObjectSource} failure to handle {@link Escalation}
	 * is handled by {@link OfficeFloor} {@link EscalationHandler}.
	 */
	public void test_FlowCallbackFailure_HandledBy_OfficeFloorEscalation() throws Throwable {

		// Fail sourcing managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addFlow(Exception.class);
		};
		object.managingOfficeBuilder.setInputManagedObjectName("MO");
		object.managingOfficeBuilder.linkFlow(0, "escalate");

		// Construct functions
		TestWork work = new TestWork();
		this.constructFunction(work, "escalate").buildParameter();

		// Handle escalation
		Closure<Throwable> failure = new Closure<>();
		this.getOfficeFloorBuilder().setEscalationHandler((escalation) -> {
			failure.value = escalation;
		});

		// Invoke the task
		final Exception exception = new Exception("TEST");
		Closure<Boolean> isCallbackInvoked = new Closure<>(false);
		this.constructOfficeFloor().openOfficeFloor();
		object.managedObjectServiceContext.invokeProcess(0, exception, object, 0, (escalation) -> {
			isCallbackInvoked.value = true;
			FlowCallback.ESCALATE.run(escalation);
		});

		// Ensure escalation handled by OfficeFloor handler
		assertTrue("Flow callback should be invoked", isCallbackInvoked.value);
		assertSame("Should escalate to OfficeFloor handler", exception, failure.value);
	}

	/**
	 * Ensure failure of {@link OfficeFloor} {@link EscalationHandler} is handled by
	 * {@link Logger}.
	 */
	public void test_OfficeFloorEscalationFailure_HandledBy_Logging() throws Throwable {

		// Fail sourcing managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addFlow(Exception.class);
		};
		object.managingOfficeBuilder.setInputManagedObjectName("MO");
		object.managingOfficeBuilder.linkFlow(0, "escalate");

		// Construct functions
		TestWork work = new TestWork();
		this.constructFunction(work, "escalate").buildParameter();

		// Handle escalation
		Closure<Throwable> failure = new Closure<>();
		this.getOfficeFloorBuilder().setEscalationHandler((escalation) -> {
			failure.value = escalation;
			throw escalation;
		});

		// Invoke the task
		final Exception exception = new Exception("TEST");
		Closure<Boolean> isCallbackInvoked = new Closure<>(false);
		this.constructOfficeFloor().openOfficeFloor();
		String log = this.captureLoggerOutput(() -> {
			object.managedObjectServiceContext.invokeProcess(0, exception, object, 0, (escalation) -> {
				isCallbackInvoked.value = true;
				FlowCallback.ESCALATE.run(escalation);
			});
		});

		// Ensure escalation handled by logging
		assertTrue("Flow callback should be invoked", isCallbackInvoked.value);
		assertSame("Should escalate to OfficeFloor handler", exception, failure.value);
		assertTrue("Ensure finally handled by logging: " + log, log.contains(exception.getMessage()));
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public Exception escalation = null;

		public boolean isHandleEscalate = false;

		public void escalate(Exception exception) throws Exception {
			throw exception;
		}

		public void handle(Exception escalation) throws Exception {
			this.escalation = escalation;

			if (this.isHandleEscalate) {
				throw this.escalation;
			}
		}
	}

}
