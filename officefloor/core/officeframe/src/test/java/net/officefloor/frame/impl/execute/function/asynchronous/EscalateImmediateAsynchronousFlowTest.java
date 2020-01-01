/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure propagates {@link Escalation} within
 * {@link AsynchronousFlowCompletion} within the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalateImmediateAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure propagates {@link Escalation} within
	 * {@link AsynchronousFlowCompletion} within the {@link ManagedFunction}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();

		// Ensure handle asynchronous flow failing on managed function thread
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("triggerAsynchronousFlow", null, (error) -> escalation.value = error);
		assertSame("Incorrect escalation", work.failure, escalation.value);
	}

	public class TestWork {

		private final Throwable failure = new Throwable("TEST");

		public void triggerAsynchronousFlow(AsynchronousFlow flow) {
			flow.complete(() -> {
				// Fail on the managed function thread
				throw this.failure;
			});
		}
	}

}