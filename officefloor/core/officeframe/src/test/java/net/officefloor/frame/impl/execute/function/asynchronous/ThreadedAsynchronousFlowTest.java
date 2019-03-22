/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure can complete {@link AsynchronousFlow} on another {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadedAsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can complete {@link AsynchronousFlow} on another {@link Thread}.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the object (ensure thread safe changes)
		TestObject object = new TestObject();
		this.constructManagedObject(object, "MO", this.getOfficeName());

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();
		trigger.buildObject("MO", ManagedObjectScope.THREAD);
		trigger.setNextFunction("servicingComplete");
		ReflectiveFunctionBuilder servicing = this.constructFunction(work, "servicingComplete");
		servicing.buildObject("MO");

		// Ensure completes flow
		this.triggerFunction("triggerAsynchronousFlow", null, null);
		this.waitForTrue(() -> work.isServicingComplete);
	}

	public class TestObject {
		private boolean isUpdated = false;
	}

	public class TestWork {

		private volatile boolean isServicingComplete = false;

		public void triggerAsynchronousFlow(AsynchronousFlow flow, TestObject object) {
			new Thread(() -> flow.complete(() -> object.isUpdated = true)).start();
		}

		public void servicingComplete(TestObject object) {
			assertTrue("Should be updated before continue from function", object.isUpdated);
			this.isServicingComplete = true;
		}
	}

}