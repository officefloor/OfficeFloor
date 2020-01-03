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

package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure executes {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_AdministrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Administration} {@link Escalation} handled by
	 * {@link EscalationProcedure}.
	 */
	public void testPreAdministrationFailure_handledByEscalationProcedure() throws Exception {

		// Construct the functions
		TestWork work = new TestWork(new Exception("TEST"));
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");
		this.constructFunction(work, "handle").buildParameter();

		// Construct the administration
		task.preAdminister("preTask").getBuilder().addEscalation(Exception.class, "handle");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "preTask", "handle", "task", "complete");
		assertSame("Incorrect handle exception", work.exception, work.handledException);
	}

	/**
	 * Ensure {@link Administration} {@link Escalation} handled by
	 * {@link FlowCallback}.
	 */
	public void testPreAdministrationFailure_handledByFlowCallback() throws Exception {

		// Construct the functions
		TestWork work = new TestWork(new Exception("TEST"));
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");
		this.constructFunction(work, "handle").buildParameter();

		// Construct the administration
		task.preAdminister("preTask");

		// Ensure handle escalation
		Closure<Throwable> failure = new Closure<>();
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		this.triggerFunction("trigger", null, (escalation) -> failure.value = escalation);

		// Incorrect escalation
		this.validateReflectiveMethodOrder("trigger", "preTask");
		assertSame("Incorrect escalation", work.exception, failure.value);
	}

	/**
	 * Ensure {@link Administration} {@link Escalation} handled by
	 * {@link EscalationProcedure}.
	 */
	public void testPostAdministrationFailure_handledByEscalationProcedure() throws Exception {

		// Construct the functions
		TestWork work = new TestWork(new Exception("TEST"));
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");
		this.constructFunction(work, "handle").buildParameter();

		// Construct the administration
		task.postAdminister("postTask").getBuilder().addEscalation(Exception.class, "handle");

		// Ensure undertakes administration before
		this.invokeFunctionAndValidate("trigger", null, "trigger", "task", "postTask", "handle", "complete");
		assertSame("Incorrect handle exception", work.exception, work.handledException);
	}

	/**
	 * Ensure {@link Administration} {@link Escalation} handled by
	 * {@link FlowCallback}.
	 */
	public void testPostAdministrationFailure_handledByFlowCallback() throws Exception {

		// Construct the functions
		TestWork work = new TestWork(new Exception("TEST"));
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.setNextFunction("complete");
		this.constructFunction(work, "complete");

		// Construct the administration
		task.postAdminister("postTask");

		// Ensure handle escalation
		Closure<Throwable> failure = new Closure<>();
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		this.triggerFunction("trigger", null, (escalation) -> failure.value = escalation);

		// Incorrect escalation
		this.validateReflectiveMethodOrder("trigger", "task", "postTask");
		assertSame("Incorrect escalation", work.exception, failure.value);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final Exception exception;

		private Exception handledException = null;

		public TestWork(Exception exception) {
			this.exception = exception;
		}

		public void trigger() {
		}

		public void preTask(Object[] extensions) throws Exception {
			throw this.exception;
		}

		public void task() {
		}

		public void postTask(Object[] extensions) throws Exception {
			throw this.exception;
		}

		public void complete() {
		}

		public void handle(Exception exception) {
			this.handledException = exception;
		}
	}

}
