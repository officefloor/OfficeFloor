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

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure propagates only the first {@link Escalation} from any
 * {@link AsynchronousFlowCompletion}.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleAsynchronousFlowFailingTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure propagates only the first {@link Escalation} from any
	 * {@link AsynchronousFlowCompletion}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork(10);
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlows");
		trigger.buildManagedFunctionContext();
		trigger.buildFlow("servicingComplete", null, false);
		this.constructFunction(work, "servicingComplete");

		// Ensure halts execution until flow completes
		Closure<Throwable> escalation = new Closure<>();
		this.triggerFunction("triggerAsynchronousFlows", null, (error) -> escalation.value = error);
		assertFalse("Should halt on async flow and not complete servicing", work.isServicingComplete);
		assertNull("Should be no escalation", escalation.value);

		// Complete first with failure
		final Exception exception = new Exception("TEST");
		work.flows[0].complete(() -> {
			throw exception;
		});
		assertSame("Incorrect escalation", exception, escalation.value);
		assertFalse("Should not complete servicing", work.isServicingComplete);

		// Complete the remaining flows (ensuring all ignored, as failed)
		for (int i = 1; i < work.flows.length; i++) {
			final int flowIndex = i;
			work.flows[flowIndex].complete(() -> {
				switch (flowIndex % 3) {
				case 0:
					throw new Exception("TEST FAILURE " + flowIndex);

				case 1:
					work.flow.doFlow(null, null);
					break;

				case 2:
					work.context.setNextFunctionArgument("IGNORED");
					break;
				}
			});
		}

		// Ensure no changes
		assertSame("Should be first escalation only", exception, escalation.value);
		assertFalse("Should continue to not complete servicing", work.isServicingComplete);
	}

	public class TestWork {

		private final int asyncFlowCount;

		private boolean isServicingComplete = false;

		private ManagedFunctionContext<?, ?> context;

		private ReflectiveFlow flow;

		private AsynchronousFlow[] flows;

		private TestWork(int asyncFlowCount) {
			this.asyncFlowCount = asyncFlowCount;
		}

		public void triggerAsynchronousFlows(ManagedFunctionContext<?, ?> context, ReflectiveFlow flow) {
			this.context = context;
			this.flow = flow;
			this.flows = new AsynchronousFlow[this.asyncFlowCount];
			for (int i = 0; i < this.flows.length; i++) {
				this.flows[i] = context.createAsynchronousFlow();
			}
		}

		public void servicingComplete() {
			this.isServicingComplete = true;
		}
	}

}
