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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadSafeClosure;

/**
 * Ensure propagates {@link Escalation} within
 * {@link AsynchronousFlowCompletion}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ThreadedEscalateAsynchronousFlowTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure propagates {@link Escalation} within
	 * {@link AsynchronousFlowCompletion}.
	 */
	@Test
	public void asynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.construct.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildManagedFunctionContext();

		// Trigger the function
		ThreadSafeClosure<Throwable> capture = new ThreadSafeClosure<>();
		this.construct.triggerFunction("triggerAsynchronousFlow", null, (escalation) -> {
			capture.set(escalation);
		});

		// Ensure provide escalation
		Throwable escalation = capture.waitAndGet();
		assertSame(FAILURE, escalation, "Incorrect exception");
	}

	private static final RuntimeException FAILURE = new RuntimeException("TEST");

	public class TestWork {

		public void triggerAsynchronousFlow(ManagedFunctionContext<?, ?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> flow.complete(() -> {
				throw FAILURE;
			}));
		}
	}

}
