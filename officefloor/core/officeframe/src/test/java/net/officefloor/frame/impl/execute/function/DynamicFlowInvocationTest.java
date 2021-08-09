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

package net.officefloor.frame.impl.execute.function;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
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
	public static class MaintainStateManagedObject extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Map.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

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
