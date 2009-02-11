/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.escalation;

import junit.framework.AssertionFailedError;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;

/**
 * Validates that escalations of tasks is appropriately managed by the
 * {@link EscalationHandler} instances.
 * 
 * @author Daniel
 */
public class EscalationHandlerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures handles escalation by the {@link Office}
	 * {@link EscalationHandler}.
	 */
	public void testOfficeEscalation() throws Throwable {

		// Create the escalation
		Throwable escalation = new Throwable("Escalation");

		// Register work for processing
		ReflectiveWorkBuilder workBuilder = this.constructWork(
				new EscalationHandlerWork(escalation), "WORK", "task");
		workBuilder.buildTask("task", "TEAM");

		// Register the team
		this.constructTeam("TEAM", new PassiveTeam());

		// Execute the task and ensure escalation handled
		this.invokeWork("WORK", null);

		// Ensure escalation is handled by office escalation handler
		try {
			this.validateNoTopLevelEscalation();
			fail("Should have a top level escalation");
		} catch (Throwable ex) {
			// Ensure appropriate escalation is captured
			assertEquals("Incorrect escalation", escalation, ex);
		}
	}

	/**
	 * Ensures the {@link ManagedObjectSource} can handle escalation on input
	 * {@link EscalationHandler}.
	 */
	public void testManagedObjectEscalation() throws Throwable {

		// Create the escalation
		Throwable escalation = new Throwable("Escalation");

		// Register the managed object
		ManagedObjectBuilder<EscalationManagedObjectSource.Handlers> moBuilder = this
				.constructManagedObject("MO",
						EscalationManagedObjectSource.class, "OFFICE");
		ManagedObjectHandlerBuilder<EscalationManagedObjectSource.Handlers> moHandlerBuilder = moBuilder
				.getManagedObjectHandlerBuilder();
		HandlerBuilder<Indexed> handlerBuilder = moHandlerBuilder
				.registerHandler(EscalationManagedObjectSource.Handlers.ESCALATE);
		handlerBuilder.setHandlerFactory(new EscalationManagedObjectSource());
		handlerBuilder.linkProcess(0, "WORK", "task");

		// Register work for processing
		ReflectiveWorkBuilder workBuilder = this.constructWork(
				new EscalationHandlerWork(escalation), "WORK", "task");
		workBuilder.buildTask("task", "TEAM");

		// Register the team
		this.constructTeam("TEAM", new PassiveTeam());

		// Create and open the office
		this.constructOfficeFloor().openOfficeFloor();

		// Invoke processing from the managed object
		EscalationManagedObjectSource.invokeProcessing();

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
	 * {@link Work} functionality to throw {@link Throwable} for escalation
	 * handling.
	 */
	public static class EscalationHandlerWork {

		/**
		 * Escalation to be thrown.
		 */
		private final Throwable escalation;

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
		 * @throws Throwable
		 *             Escalation.
		 */
		public void task() throws Throwable {
			throw this.escalation;
		}
	}
}
