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
 * @author Daniel Sagenschneider
 */
public class OfficeEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Flag to record task method invocations.
	 */
	public OfficeEscalationTest() {
		this.setRecordReflectiveTaskMethodsInvoked(true);
	}

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
		causeEscalation.setNextTaskInFlow("nextTask");
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
				FlowInstigationStrategyEnum.PARALLEL, null);
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