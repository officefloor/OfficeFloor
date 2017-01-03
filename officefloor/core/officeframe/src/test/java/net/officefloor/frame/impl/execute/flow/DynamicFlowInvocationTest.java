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
package net.officefloor.frame.impl.execute.flow;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to invoke flow dynamically by {@link ManagedFunction} name.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicFlowInvocationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to dynamically invoke a {@link Flow}.
	 */
	public void testDynamicFlowInvocation() throws Exception {

		// Configure
		DynamicInvokeFlowWork work = new DynamicInvokeFlowWork();
		ReflectiveFunctionBuilder initialTask = this.constructFunction(work, "initialTask");
		initialTask.buildManagedFunctionContext();
		ReflectiveFunctionBuilder dynamicTask = this.constructFunction(work, "dynamicTask");
		dynamicTask.buildParameter();

		// Execute the function
		this.invokeFunction("initialTask", null);

		// Ensure the dynamic task is invoked with the parameter
		assertTrue("Dynamic task should be invoked", work.isDynamicTaskInvoked);
		assertEquals("Incorrect parameter for dynamic task", "PARAMETER", work.parameter);
	}

	/**
	 * Mock work.
	 */
	public static class DynamicInvokeFlowWork {

		/**
		 * Indicates if the dynamic {@link ManagedFunction} was invoked.
		 */
		public boolean isDynamicTaskInvoked = false;

		/**
		 * Parameter.
		 */
		public String parameter;

		/**
		 * Invokes the {@link Flow} dynamically.
		 * 
		 * @param context
		 *            {@link ManagedFunctionContext}.
		 */
		public void initialTask(ManagedFunctionContext<?, ?> context) throws Exception {
			context.doFlow("dynamicTask", "PARAMETER", null);
		}

		/**
		 * {@link ManagedFunction} to invoke dynamically.
		 * 
		 * @param parameter
		 *            Parameter.
		 */
		public void dynamicTask(String parameter) {
			this.isDynamicTaskInvoked = true;
			this.parameter = parameter;
		}
	}

	/**
	 * Ensures that dynamic {@link Flow} is invoked in the same
	 * {@link ThreadState}.
	 */
	public void testMaintainState() throws Exception {

		final String CONTEXT_VALUE = "VALUE";

		// Configure
		this.constructManagedObject("MO", new MaintainStateManagedObject(), this.getOfficeName());
		MaintainStateWork work = new MaintainStateWork("KEY", CONTEXT_VALUE);
		ReflectiveFunctionBuilder initialTask = this.constructFunction(work, "initialTask");
		initialTask.buildManagedFunctionContext();
		initialTask.buildObject("MO", ManagedObjectScope.THREAD);
		ReflectiveFunctionBuilder dynamicTask = this.constructFunction(work, "dynamicTask");
		dynamicTask.buildObject("MO");

		// Execute the function
		this.invokeFunction("initialTask", null);

		// Ensure the dynamic task is invoked with the parameter
		assertTrue("Dynamic task should be invoked", work.isDynamicTaskInvoked);
		assertEquals("Incorrect context value for dynamic task", CONTEXT_VALUE, work.obtainedContextValue);
	}

	/**
	 * Mock work.
	 */
	public static class MaintainStateWork {

		/**
		 * Key to set value to ensure maintains state.
		 */
		private final String contextKey;

		/**
		 * Context value to provide under key.
		 */
		private final Object contextValue;

		/**
		 * Indicates if the dynamic {@link ManagedFunction} was invoked.
		 */
		public boolean isDynamicTaskInvoked = false;

		/**
		 * Obtained context value.
		 */
		public Object obtainedContextValue;

		/**
		 * Initiate.
		 * 
		 * @param key
		 *            Key of context value.
		 * @param value
		 *            Context value.
		 */
		public MaintainStateWork(String key, Object value) {
			this.contextKey = key;
			this.contextValue = value;
		}

		/**
		 * Invokes the {@link Flow} dynamically.
		 * 
		 * @param context
		 *            {@link ManagedFunctionContext}.
		 * @param managedObject
		 *            {@link ManagedObject} value.
		 */
		public void initialTask(ManagedFunctionContext<?, ?> context, Map<String, Object> managedObject)
				throws Exception {

			// Provide context value
			managedObject.put(this.contextKey, this.contextValue);

			// Invoke the dynamic flow
			context.doFlow("dynamicTask", null, null);
		}

		/**
		 * {@link ManagedFunction} to invoke dynamically.
		 * 
		 * @param managedObject
		 *            {@link ManagedObject} object.
		 */
		public void dynamicTask(Map<String, Object> managedObject) {
			this.isDynamicTaskInvoked = true;

			// Obtain the context value
			this.obtainedContextValue = managedObject.get(this.contextKey);
		}
	}

	/**
	 * Mock {@link ManagedObject} for testing.
	 */
	public static class MaintainStateManagedObject implements ManagedObject {

		/*
		 * ==================== ManagedObject ========================
		 */

		@Override
		public Object getObject() throws Throwable {
			// Always return new instance to always provide new context
			return new HashMap<String, Object>();
		}
	}

}