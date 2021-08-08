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

package net.officefloor.frame.impl.execute.function.asynchronous;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.escalate.AsynchronousFlowTimedOutEscalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.OfficeManagerTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensure {@link AsynchronousFlowTimedOutEscalation} on {@link AsynchronousFlow}
 * taking too long.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class TimedOutAsynchronousFlowTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final OfficeManagerTestSupport officeManager = new OfficeManagerTestSupport();

	/**
	 * Ensure {@link AsynchronousFlowTimedOutEscalation} on {@link AsynchronousFlow}
	 * taking too long.
	 */
	@Test
	public void timeoutBasedOnManagedFunction() throws Exception {
		this.doAsynchronousFlowTimeoutTest(true);
	}

	/**
	 * Ensure {@link AsynchronousFlowTimedOutEscalation} on {@link AsynchronousFlow}
	 * taking too long.
	 */
	@Test
	public void timeoutBasedOnOffice() throws Exception {
		this.doAsynchronousFlowTimeoutTest(false);
	}

	/**
	 * Undertakes timeout test on {@link AsynchronousFlow}.
	 * 
	 * @param isManagedFunction Indicates if configure timeout on
	 *                          {@link ManagedFunction}.
	 */
	private void doAsynchronousFlowTimeoutTest(boolean isManagedFunction) throws Exception {

		// Create object
		TestObject object = new TestObject();
		this.construct.constructManagedObject(object, "MO", this.construct.getOfficeName());

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();
		trigger.buildObject("MO", ManagedObjectScope.THREAD);
		trigger.setNextFunction("servicingComplete");
		this.construct.constructFunction(work, "servicingComplete");

		// Flag time out on function / office
		if (isManagedFunction) {
			trigger.getBuilder().setAsynchronousFlowTimeout(10);
		} else {
			this.construct.getOfficeBuilder().setDefaultAsynchronousFlowTimeout(10);
		}

		// Ensure halts execution until flow completes
		Closure<Throwable> escalation = new Closure<>();
		this.construct.triggerFunction("triggerAsynchronousFlow", null, (error) -> escalation.value = error);
		assertFalse(work.isServicingComplete, "Should halt on async flow and not complete servicing");
		assertNull(escalation.value, "Should be no escalation: " + escalation.value);

		// Trigger timeout of asynchronous flow
		this.construct.adjustCurrentTimeMillis(100);
		officeManager.getOfficeManager(0).runAssetChecks();

		// Should be completed with escalation
		assertNotNull(escalation.value, "Should fail");
		assertTrue(escalation.value instanceof AsynchronousFlowTimedOutEscalation, "Should fail with time out");

		// Attempt to complete later
		work.complete.run();
		assertFalse(object.isUpdated, "Should not run completion");
		assertFalse(work.isServicingComplete, "Should not complete servicing");
	}

	public class TestObject {
		private boolean isUpdated = false;
	}

	public class TestWork {

		private boolean isServicingComplete = false;

		private Runnable complete;

		public void triggerAsynchronousFlow(AsynchronousFlow flow, TestObject object) {
			this.complete = () -> {
				flow.complete(() -> object.isUpdated = true);
			};
		}

		public void servicingComplete() {
			this.isServicingComplete = true;
		}
	}

}
