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
package net.officefloor.frame.impl.execute.function.escalation;

import java.util.logging.Logger;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Validates that escalations of tasks is appropriately managed by the
 * {@link EscalationHandler} instances.
 *
 * @author Daniel Sagenschneider
 */
public class ThreadStateHandleEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures handles escalation by the {@link Office}
	 * {@link EscalationProcedure}.
	 */
	public void test_Escalation_HandledBy_OfficeEscalationProcedure() throws Throwable {

		// Create the escalation
		RuntimeException escalation = new RuntimeException("Escalation");

		// Add office escalation to handle escalation
		this.getOfficeBuilder().addEscalation(RuntimeException.class, "officeEscalation");

		// Construct work
		EscalationHandlerWork work = new EscalationHandlerWork(escalation);
		this.constructFunction(work, "task").buildParameter();
		this.constructFunction(work, "officeEscalation").buildParameter();

		// Execute the task to have escalation handled
		this.invokeFunction("task", null);

		// Ensure escalation is handled by office escalation procedure
		assertEquals("Incorrect escalation", escalation, work.officeException);
	}

	/**
	 * Ensure failure of {@link Office} {@link EscalationProcedure} is handled
	 * by {@link OfficeFloor} {@link EscalationHandler}.
	 */
	public void test_OfficeEscalationProcedureFailure_HandledBy_OfficeFloorEscalation() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Ensures handles escalation by the {@link OfficeFloor}
	 * {@link EscalationHandler}.
	 */
	public void test_Escalation_HandledBy_OfficeFloorEscalation() throws Throwable {

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Create the escalation
		RuntimeException escalation = new RuntimeException("Escalation");

		// Construct work
		EscalationHandlerWork work = new EscalationHandlerWork(escalation);
		this.constructFunction(work, "task").buildParameter();

		// Capture office floor escalation
		final Throwable[] failure = new Throwable[1];
		this.getOfficeFloorBuilder().setEscalationHandler(new EscalationHandler() {
			@Override
			public void handleEscalation(Throwable escalation) throws Throwable {
				failure[0] = escalation;
			}
		});

		// Must invoke directly via manager (to not provide callback)
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager("task");

		// Execute the task to have escalation handled
		function.invokeProcess(null, null);

		// Ensure escalation handled by OfficeFloor escalation handler
		assertSame("Incorrect escalation", escalation, failure[0]);
	}

	/**
	 * Ensure failure of {@link OfficeFloor} {@link EscalationHandler} is
	 * handled by {@link Logger}.
	 */
	public void test_OfficeFloorEscalationFailure_HandledBy_Logging() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Functionality to throw {@link Throwable} for escalation handling.
	 */
	public static class EscalationHandlerWork {

		/**
		 * Escalation to be thrown.
		 */
		private final Throwable escalation;

		/**
		 * Parameter on invoking the task method.
		 */
		public String taskParameter;

		/**
		 * Exception handled by the {@link Office}.
		 */
		public Exception officeException;

		/**
		 * Initiate.
		 *
		 * @param escalation
		 *            Escalation to be thrown by the task.
		 */
		public EscalationHandlerWork(Throwable escalation) {
			this.escalation = escalation;
		}

		/**
		 * Task causing an escalation.
		 *
		 * @param parameter
		 *            Argument passed from the {@link ManagedObjectSource}.
		 * @throws Throwable
		 *             Escalation.
		 */
		public void task(String parameter) throws Throwable {
			this.taskParameter = parameter;
			throw this.escalation;
		}

		/**
		 * Provides for the {@link Office} {@link EscalationProcedure}.
		 *
		 * @param exception
		 *            Failure to be handled.
		 */
		public void officeEscalation(Exception exception) {
			this.officeException = exception;
		}
	}

}