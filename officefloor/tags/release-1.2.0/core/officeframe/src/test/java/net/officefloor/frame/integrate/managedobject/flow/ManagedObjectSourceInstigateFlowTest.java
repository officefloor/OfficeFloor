/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.frame.integrate.managedobject.flow;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * Tests {@link ManagedObjectSource} invoking a {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceInstigateFlowTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Ensures {@link ManagedObjectSource} invokes process.
	 */
	public void testInvokeFlow() throws Exception {

		String officeName = this.getOfficeName();

		// Construct the managed object
		ManagedObjectBuilder<InputManagedObjectSource.Flows> moBuilder = this
				.constructManagedObject("INPUT", InputManagedObjectSource.class);

		// Provide flow for input managed object
		ManagingOfficeBuilder<InputManagedObjectSource.Flows> managingOfficeBuilder = moBuilder
				.setManagingOffice(officeName);
		managingOfficeBuilder.setInputManagedObjectName("INPUT");
		managingOfficeBuilder.linkProcess(InputManagedObjectSource.Flows.INPUT,
				"WORK", "TASK");

		// Provide task for managed object source input
		InputTask inputTask = new InputTask();
		this.constructWork("WORK", inputTask, "TASK");
		TaskBuilder<?, ?, ?> taskBuilder = this.constructTask("TASK",
				inputTask, "TEAM", "INPUT", Object.class, null, null);
		taskBuilder.linkParameter(1, Object.class);

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
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Input the parameter
		InputManagedObjectSource.input(PARAMETER, managedObject);

		// Close the Office
		officeFloor.closeOfficeFloor();

		// Validate the input
		assertEquals("Incorrect parameter", PARAMETER, inputTask.parameter);
		assertEquals("Incorrect object", OBJECT, inputTask.object);
	}

	/**
	 * Task to process {@link ManagedObjectSource} input.
	 */
	private static class InputTask extends
			AbstractSingleTask<Work, Indexed, Indexed> {

		/**
		 * Parameter.
		 */
		protected volatile Object parameter;

		/**
		 * Object.
		 */
		protected volatile Object object;

		/*
		 * ===================== Task ======================================
		 */

		@Override
		public Object doTask(TaskContext<Work, Indexed, Indexed> context)
				throws Throwable {

			// Obtain the object
			this.object = context.getObject(0);

			// Obtain the parameter
			this.parameter = context.getObject(1);

			// No return
			return null;
		}
	}

}