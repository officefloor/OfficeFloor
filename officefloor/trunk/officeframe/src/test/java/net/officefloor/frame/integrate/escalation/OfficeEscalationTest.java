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
package net.officefloor.frame.integrate.escalation;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests handling escalations.
 * 
 * @author Daniel
 */
public class OfficeEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures handles escalation by same {@link Task}.
	 */
	public void testEscalationHandledBySameTask() throws Exception {

		// Create the work object
		EscalationWorkObject workObject = new EscalationWorkObject(
				new Exception("test"));

		// Construct the office
		this.constructTeam("team", new PassiveTeam());
		ReflectiveWorkBuilder workBuilder = this.constructWork(workObject,
				"work", "causeEscalation");
		ReflectiveTaskBuilder causeEscalation = workBuilder.buildTask(
				"causeEscalation", "team");
		causeEscalation.getBuilder().setNextTaskInFlow("nextTask");
		causeEscalation.getBuilder().addEscalation(
				workObject.failure.getClass(), "handleEscalation");
		workBuilder.buildTask("nextTask", "team");
		ReflectiveTaskBuilder escalationHandler = workBuilder.buildTask(
				"handleEscalation", "team");
		escalationHandler.buildParameter();

		// Invoke the work
		this.invokeWork("work", null);

		// Validate appropriate methods called
		this.validateReflectiveMethodOrder("causeEscalation",
				"handleEscalation");

		// Validate failure handled
		assertEquals("Incorrect exception", workObject.failure,
				workObject.handledFailure);
	}

	/**
	 * <p>
	 * Ensures a parallel owner {@link JobNode} has opportunity to handle the
	 * escalation.
	 * <p>
	 * This is represents a method call allowing the caller to handle exceptions
	 * from the invoked method.
	 */
	public void testEscalationHandledByParallelOwner() throws Exception {

		// Create the work object
		EscalationWorkObject workObject = new EscalationWorkObject(
				new Exception("test"));

		// Construct the office
		this.constructTeam("team", new PassiveTeam());
		ReflectiveWorkBuilder workBuilder = this.constructWork(workObject,
				"work", "parallelOwner");
		ReflectiveTaskBuilder parallelOwner = workBuilder.buildTask(
				"parallelOwner", "team");
		parallelOwner.buildFlow("causeEscalation",
				FlowInstigationStrategyEnum.PARALLEL);
		parallelOwner.getBuilder().addEscalation(workObject.failure.getClass(),
				"handleEscalation");
		workBuilder.buildTask("causeEscalation", "team");
		ReflectiveTaskBuilder escalationHandler = workBuilder.buildTask(
				"handleEscalation", "team");
		escalationHandler.buildParameter();

		// Invoke the work
		this.invokeWork("work", null);

		// Validate appropriate methods called
		this.validateReflectiveMethodOrder("parallelOwner", "causeEscalation",
				"handleEscalation");

		// Validate failure handled
		assertEquals("Incorrect exception", workObject.failure,
				workObject.handledFailure);
	}

	/**
	 * Work object with methods.
	 */
	public static class EscalationWorkObject {

		public final Throwable failure;

		public Throwable handledFailure = null;

		public EscalationWorkObject(Throwable failure) {
			this.failure = failure;
		}

		public void parallelOwner(ReflectiveFlow flow) {
			flow.doFlow(null);
		}

		public void causeEscalation() throws Throwable {
			throw failure;
		}

		public void nextTask() {
		}

		public void handleEscalation(Throwable handledfailure) {
			this.handledFailure = handledfailure;
		}
	}

}