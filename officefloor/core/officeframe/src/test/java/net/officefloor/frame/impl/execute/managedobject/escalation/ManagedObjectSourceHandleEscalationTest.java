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
package net.officefloor.frame.impl.execute.managedobject.escalation;

import java.util.logging.Logger;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensures the {@link ManagedObjectSource} {@link FlowCallback} can handle
 * {@link Escalation}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceHandleEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures {@link Escalation} is handled by the {@link Office}
	 * {@link EscalationProcedure}.
	 */
	public void test_Escalation_HandledBy_OfficeEscalationProcedure() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Ensure {@link Escalation} able to be handled by
	 * {@link ManagedObjectSource} invoking {@link FlowCallback}.
	 */
	public void test_Escalation_HandledBy_FlowCallback() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Ensure if no {@link FlowCallback} provided, that handled immediately by
	 * {@link OfficeFloor} {@link EscalationHandler}.
	 */
	public void test_Escalation_HandledBy_OfficeFloorEscalation() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Ensure {@link Office} {@link EscalationProcedure} failure is handled by
	 * {@link FlowCallback}.
	 */
	public void test_OfficeEscalationProcedureFailure_HandledBy_FlowCallback() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Ensures if no {@link FlowCallback} provided, that {@link Office}
	 * {@link EscalationProcedure} failure is handled by {@link OfficeFloor}
	 * {@link EscalationHandler}.
	 */
	public void test_OfficeEscalationProcedureFailure_HandledBy_OfficeFloorEscalation() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Ensures the {@link ManagedObjectSource} failure to handle
	 * {@link Escalation} is handled by {@link OfficeFloor}
	 * {@link EscalationHandler}.
	 */
	public void test_FlowCallbackFailure_HandledBy_OfficeFloorEscalation() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Ensure failure of {@link OfficeFloor} {@link EscalationHandler} is
	 * handled by {@link Logger}.
	 */
	public void test_OfficeFloorEscalationFailure_HandledBy_Logging() throws Throwable {
		fail("TODO implement");
	}

	/**
	 * Ensures the {@link ManagedObjectSource} failure to handle
	 * {@link Escalation} is handled by managing {@link Office}
	 * {@link EscalationProcedure}.
	 */
	@Deprecated // to tests above
	public void testEscalationFailureHandledByOfficeEscalationProcedure() throws Throwable {

		// Create the escalation
		Throwable escalation = new Throwable("TEST");

		// Obtain the name of the office
		String officeName = this.getOfficeName();

		// Construct the managed object source
		EscalationManagedObjectSource managedObjectSource = new EscalationManagedObjectSource(escalation);
		ManagedObjectBuilder<Flows> moBuilder = this.constructManagedObject("MO", managedObjectSource, null);
		ManagingOfficeBuilder<Flows> managingOfficeBuilder = moBuilder.setManagingOffice(officeName);
		managingOfficeBuilder.setInputManagedObjectName("MO");
		managingOfficeBuilder.linkProcess(Flows.FUNCTION_TRIGGERING_ESCALATION, "task");

		// Flag managing office and invocation of flow

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
		managedObjectSource.invokeProcessing(FLOW_ARGUMENT);

		// Ensure argument passed to task
		assertEquals("Incorrect parameter value for task", FLOW_ARGUMENT, work.taskParameter);

		// Ensure managed object source escalation handled by OfficeFloor
		assertEquals("Incorrect handle escalation", escalation, officeFloorEscalation[0]);
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
	}

	/**
	 * Flows.
	 */
	public static enum Flows {
		FUNCTION_TRIGGERING_ESCALATION
	}

	/**
	 * Invokes flow with an {@link EscalationHandler}.
	 */
	@TestSource
	private class EscalationManagedObjectSource extends AbstractManagedObjectSource<None, Flows>
			implements ManagedObject, FlowCallback {

		/**
		 * Failure to be thrown from {@link EscalationHandler} of this
		 * {@link ManagedObjectSource}.
		 */
		private final Throwable escalationHandlerFailure;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<Flows> executeContext;

		/**
		 * {@link FlowCallback} handled {@link Escalation}.
		 */
		private Throwable handledEscalation = null;

		/**
		 * Resets for use.
		 * 
		 * @param escalationHandlerFailure
		 *            Failure to be thrown from {@link EscalationHandler} of
		 *            this {@link ManagedObjectSource}.
		 */
		public EscalationManagedObjectSource(Throwable escalationHandlerFailure) {
			this.escalationHandlerFailure = escalationHandlerFailure;
		}

		/**
		 * Invokes processing.
		 * 
		 * @param argument
		 *            Argument passed by {@link ManagedObjectSource}.
		 */
		public void invokeProcessing(String argument) {
			this.executeContext.invokeProcess(Flows.FUNCTION_TRIGGERING_ESCALATION, argument, this, 0, this);
		}

		/*
		 * ================= ManagedObjectSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
			context.setObjectClass(EscalationManagedObjectSource.class);
			context.addFlow(Flows.FUNCTION_TRIGGERING_ESCALATION, String.class);
		}

		@Override
		public void start(ManagedObjectExecuteContext<Flows> context) throws Exception {
			this.executeContext = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ================== ManagedObject ==================
		 */

		@Override
		public Object getObject() throws Exception {
			return this;
		}

		/*
		 * ================= FlowCompletion ====================================
		 */

		@Override
		public void run(Throwable escalation) throws Throwable {
			this.handledEscalation = escalation;

			// Determine if failure in handling escalation
			if (escalationHandlerFailure != null) {
				throw escalationHandlerFailure;
			}
		}
	}

}