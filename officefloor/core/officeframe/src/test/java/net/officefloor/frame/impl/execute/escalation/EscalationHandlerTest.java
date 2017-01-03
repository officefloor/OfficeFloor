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
package net.officefloor.frame.impl.execute.escalation;

import java.io.IOException;

import junit.framework.AssertionFailedError;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.escalate.EscalationHandler;
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
public class EscalationHandlerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures handles escalation by the {@link OfficeFloor}
	 * {@link EscalationHandler}.
	 */
	public void testOfficeFloorEscalation() throws Throwable {

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

		// Execute the task to have escalation handled
		this.invokeFunction("task", null);

		// Ensure escalation handled by office floor escalation handler
		assertEquals("Incorrect escalation", escalation, failure[0]);
	}

	/**
	 * Ensures handles escalation by the {@link Office}
	 * {@link EscalationProcedure}.
	 */
	public void testOfficeEscalation() throws Throwable {

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
	 * Ensures the {@link ManagedObjectSource} can handle escalation on input
	 * {@link EscalationHandler}.
	 */
	public void testManagedObjectEscalation() throws Throwable {

		// Create the escalation
		Throwable escalation = new Throwable("Escalation");

		// Obtain the name of the office
		String officeName = this.getOfficeName();

		// Construct the managed object source
		EscalationManagedObjectSource.reset(null);
		ManagedObjectBuilder<EscalationManagedObjectSource.Flows> moBuilder = this.constructManagedObject("MO",
				EscalationManagedObjectSource.class);

		// Flag managing office and invocation of flow
		ManagingOfficeBuilder<EscalationManagedObjectSource.Flows> managingOfficeBuilder = moBuilder
				.setManagingOffice(officeName);
		managingOfficeBuilder.setInputManagedObjectName("MO");
		managingOfficeBuilder.linkProcess(EscalationManagedObjectSource.Flows.TASK_TO_ESCALATE, "task");

		// Construct the work
		EscalationHandlerWork work = new EscalationHandlerWork(escalation);
		this.constructFunction(work, "task").buildParameter();

		// Create and open the office
		this.constructOfficeFloor().openOfficeFloor();

		final String FLOW_ARGUMENT = "FLOW_ARGUMENT";

		// Invoke processing from the managed object
		EscalationManagedObjectSource.invokeProcessing(FLOW_ARGUMENT);

		// Ensure argument passed to task
		assertEquals("Incorrect parameter value for task", FLOW_ARGUMENT, work.taskParameter);

		// Ensure escalation is handled by managed object escalation handler
		try {
			// Check that escalation handled by managed object
			EscalationManagedObjectSource.throwPossibleEscalation();

			// Indicate should have error
			fail("Should have a managed object escalation");
		} catch (AssertionFailedError ex) {
			throw ex;
		} catch (Throwable ex) {
			// Ensure appropriate escalation is captured
			assertEquals("Incorrect escalation", escalation, ex);
		}
	}

	/**
	 * Ensures the {@link ManagedObjectSource} failure is handled by
	 * {@link OfficeFloor} {@link EscalationHandler}.
	 */
	public void testManagedObjectEscalationFailure() throws Throwable {

		// Create the escalation
		Throwable escalation = new Throwable("Escalation");
		IOException handleEscalation = new IOException("Handle Escalation");

		// Obtain the name of the office
		String officeName = this.getOfficeName();

		// Construct the managed object source
		EscalationManagedObjectSource.reset(handleEscalation);
		ManagedObjectBuilder<EscalationManagedObjectSource.Flows> moBuilder = this.constructManagedObject("MO",
				EscalationManagedObjectSource.class);

		// Flag managing office and invocation of flow
		ManagingOfficeBuilder<EscalationManagedObjectSource.Flows> managingOfficeBuilder = moBuilder
				.setManagingOffice(officeName);
		managingOfficeBuilder.setInputManagedObjectName("MO");
		managingOfficeBuilder.linkProcess(EscalationManagedObjectSource.Flows.TASK_TO_ESCALATE, "task");

		// Construct the work
		EscalationHandlerWork work = new EscalationHandlerWork(escalation);
		this.constructFunction(work, "task").buildParameter();

		// Capture office floor escalation (from managed object source)
		final Throwable[] officeFloorEscalation = new Throwable[1];
		this.getOfficeFloorBuilder().setEscalationHandler(new EscalationHandler() {
			@Override
			public void handleEscalation(Throwable escalation) throws Throwable {
				officeFloorEscalation[0] = escalation;
			}
		});

		// Create and open the office
		this.constructOfficeFloor().openOfficeFloor();

		final String FLOW_ARGUMENT = "FLOW_ARGUMENT";

		// Invoke processing from the managed object
		EscalationManagedObjectSource.invokeProcessing(FLOW_ARGUMENT);

		// Ensure argument passed to task
		assertEquals("Incorrect parameter value for task", FLOW_ARGUMENT, work.taskParameter);

		// Ensure escalation is handled by managed object escalation handler
		try {
			EscalationManagedObjectSource.throwPossibleEscalation();
			fail("Should have a managed object escalation");
		} catch (Throwable ex) {
			assertEquals("Incorrect escalation", escalation, ex);
		}

		// Ensure managed object source escalation handled by office floor
		assertEquals("Incorrect handle escalation", handleEscalation, officeFloorEscalation[0]);
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