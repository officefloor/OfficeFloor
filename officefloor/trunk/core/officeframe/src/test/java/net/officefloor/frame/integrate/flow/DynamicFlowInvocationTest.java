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

package net.officefloor.frame.integrate.flow;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Ensure able to invoke flow dynamically by {@link Work} and {@link Task} name.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicFlowInvocationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to dynamically invoke a {@link Flow}.
	 */
	public void testDynamicFlowInvocation() throws Exception {

		// Configure
		this.constructTeam("TEAM", new PassiveTeam());
		DynamicInvokeFlowWork work = new DynamicInvokeFlowWork();
		ReflectiveWorkBuilder builder = this.constructWork(work, "WORK",
				"initialTask");
		ReflectiveTaskBuilder initialTask = builder.buildTask("initialTask",
				"TEAM");
		initialTask.buildTaskContext();
		ReflectiveTaskBuilder dynamicTask = builder.buildTask("dynamicTask",
				"TEAM");
		dynamicTask.buildParameter();

		// Execute the work
		this.invokeWork("WORK", null);

		// Ensure the dynamic task is invoked with the parameter
		assertTrue("Dynamic task should be invoked", work.isDynamicTaskInvoked);
		assertEquals("Incorrect parameter for dynamic task", "PARAMETER",
				work.parameter);
	}

	/**
	 * Mock {@link Work} for testing.
	 */
	public static class DynamicInvokeFlowWork {

		/**
		 * Indicates if the dynamic {@link Task} was invoked.
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
		 *            {@link TaskContext}.
		 */
		public void initialTask(TaskContext<?, ?, ?> context) throws Exception {
			context.doFlow("WORK", "dynamicTask", "PARAMETER");
		}

		/**
		 * {@link Task} to invoke dynamically.
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
		this.constructTeam("TEAM", new PassiveTeam());
		this.constructManagedObject("MO", new MaintainStateManagedObject(),
				this.getOfficeName());
		MaintainStateWork work = new MaintainStateWork("KEY", CONTEXT_VALUE);
		ReflectiveWorkBuilder builder = this.constructWork(work, "WORK",
				"initialTask");
		ReflectiveTaskBuilder initialTask = builder.buildTask("initialTask",
				"TEAM");
		initialTask.buildTaskContext();
		initialTask.buildObject("MO", ManagedObjectScope.THREAD);
		ReflectiveTaskBuilder dynamicTask = builder.buildTask("dynamicTask",
				"TEAM");
		dynamicTask.buildObject("MO");

		// Execute the work
		this.invokeWork("WORK", null);

		// Ensure the dynamic task is invoked with the parameter
		assertTrue("Dynamic task should be invoked", work.isDynamicTaskInvoked);
		assertEquals("Incorrect context value for dynamic task", CONTEXT_VALUE,
				work.obtainedContextValue);
	}

	/**
	 * Mock {@link Work} for testing.
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
		 * Indicates if the dynamic {@link Task} was invoked.
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
		 *            {@link TaskContext}.
		 * @param managedObject
		 *            {@link ManagedObject} value.
		 */
		public void initialTask(TaskContext<?, ?, ?> context,
				Map<String, Object> managedObject) throws Exception {

			// Provide context value
			managedObject.put(this.contextKey, this.contextValue);

			// Invoke the dynamic flow
			context.doFlow("WORK", "dynamicTask", null);
		}

		/**
		 * {@link Task} to invoke dynamically.
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