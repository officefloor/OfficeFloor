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
package net.officefloor.frame.impl.execute.handler;

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.AbstractMockHandler;
import net.officefloor.frame.impl.AbstractMockTask;
import net.officefloor.frame.impl.AbstractOfficeConstructTestCase;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests {@link net.officefloor.frame.api.execute.Handler} invoking a
 * {@link net.officefloor.frame.internal.structure.ProcessState}.
 * 
 * @author Daniel
 */
public class HandlerExecutionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures handler invokes process.
	 */
	@SuppressWarnings("unchecked")
	public void testHandler() throws Exception {

		// Construct the managed object
		ManagedObjectBuilder moBuilder = this.constructManagedObject("INPUT",
				InputManagedObjectSource.class, "OFFICE");

		// Make process bound managed object
		this.getOfficeBuilder().addProcessManagedObject("P-INPUT", "INPUT");

		// Provide handler for input managed object
		HandlerBuilder handlerBuilder = moBuilder
				.registerHandler(Handlers.INPUT);
		handlerBuilder.setHandlerFactory(new MockHandler());
		handlerBuilder.linkProcess(0, "WORK", "TASK");

		// Provide task for handler input
		InputTask inputTask = new InputTask();
		WorkBuilder<InputTask> workBuilder = this.constructWork("WORK",
				inputTask, "TASK");
		workBuilder.registerProcessManagedObject("W-INPUT", "P-INPUT");
		this.constructTask("TASK", Object.class, inputTask, "TEAM", "W-INPUT",
				null);

		// Register the team
		this.constructTeam("TEAM", new PassiveTeam());

		// Source input details
		final Object PARAMETER = new Object();
		final Object OBJECT = new Object();
		final ManagedObject managedObject = new ManagedObject() {
			public Object getObject() throws Exception {
				return OBJECT;
			}
		};

		// Build and open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor("OFFICE");
		officeFloor.openOfficeFloor();

		// Input the parameter
		InputManagedObjectSource.input(PARAMETER, managedObject);

		// Close the Office
		officeFloor.closeOfficeFloor();

		// Validate the input
		assertEquals("Incorrect parameter", PARAMETER, inputTask.parameter);
		assertEquals("Incorrect object", OBJECT, inputTask.object);
	}
}

/**
 * Mock {@link net.officefloor.frame.api.execute.Handler}.
 */
class MockHandler extends AbstractMockHandler<Indexed> {

	/**
	 * Handles the {@link ManagedObject}.
	 * 
	 * @param parameter
	 *            Parameter.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	public void handle(Object parameter, ManagedObject managedObject) {
		// Invoke the process
		this.getContext().invokeProcess(0, parameter, managedObject);
	}
}

/**
 * Task to process handler input.
 */
class InputTask extends AbstractMockTask<Object> {

	/**
	 * Parameter.
	 */
	protected volatile Object parameter;

	/**
	 * Object.
	 */
	protected volatile Object object;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.impl.AbstractMockTask#doTask()
	 */
	protected Object doTask() throws Exception {
		// Obtain the parameter
		this.parameter = this.getTaskContext().getParameter();

		// Obtain the object
		this.object = this.getTaskContext().getObject(0);

		// No return
		return null;
	}

}