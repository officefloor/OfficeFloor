/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
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
		trigger.buildManagedFunctionContext();
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

		public void triggerAsynchronousFlow(ManagedFunctionContext<?, ?> context, TestObject object) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> flow.complete(() -> object.isUpdated = true));
		}

		public void servicingComplete(TestObject object) {
			assertTrue("Should be updated before continue from function", object.isUpdated);
			this.isServicingComplete = true;
		}
	}

}
