/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.escalate.AsynchronousFlowTimedOutEscalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure {@link AsynchronousFlowTimedOutEscalation} on {@link AsynchronousFlow}
 * taking too long.
 * 
 * @author Daniel Sagenschneider
 */
public class TimedOutAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link AsynchronousFlowTimedOutEscalation} on {@link AsynchronousFlow}
	 * taking too long.
	 */
	public void testTimeoutBasedOnManagedFunction() throws Exception {
		this.doAsynchronousFlowTimeoutTest(true);
	}

	/**
	 * Ensure {@link AsynchronousFlowTimedOutEscalation} on {@link AsynchronousFlow}
	 * taking too long.
	 */
	public void testTimeoutBasedOnOffice() throws Exception {
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
		this.constructManagedObject(object, "MO", this.getOfficeName());

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();
		trigger.buildObject("MO", ManagedObjectScope.THREAD);
		trigger.setNextFunction("servicingComplete");
		this.constructFunction(work, "servicingComplete");

		// Flag time out on function / office
		if (isManagedFunction) {
			trigger.getBuilder().setAsynchronousFlowTimeout(10);
		} else {
			this.getOfficeBuilder().setDefaultAsynchronousFlowTimeout(10);
		}

		// Ensure halts execution until flow completes
		Closure<Throwable> escalation = new Closure<>();
		Office office = this.triggerFunction("triggerAsynchronousFlow", null, (error) -> escalation.value = error);
		assertFalse("Should halt on async flow and not complete servicing", work.isServicingComplete);
		assertNull("Should be no escalation: " + escalation.value, escalation.value);

		// Trigger timeout of asynchronous flow
		this.adjustCurrentTimeMillis(100);
		office.runAssetChecks();

		// Should be completed with escalation
		assertNotNull("Should fail", escalation.value);
		assertTrue("Should fail with time out", escalation.value instanceof AsynchronousFlowTimedOutEscalation);

		// Attempt to complete later
		work.complete.run();
		assertFalse("Should not run completion", object.isUpdated);
		assertFalse("Should not complete servicing", work.isServicingComplete);
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
