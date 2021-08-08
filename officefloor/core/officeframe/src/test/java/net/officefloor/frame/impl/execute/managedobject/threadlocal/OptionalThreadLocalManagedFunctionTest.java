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

package net.officefloor.frame.impl.execute.managedobject.threadlocal;

import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests accessing the {@link OptionalThreadLocal} via the
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class OptionalThreadLocalManagedFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure the {@link OptionalThreadLocal} not provide object if
	 * {@link ManagedObject} not instantiated.
	 */
	public void test_Process_NotAvailable() throws Exception {
		this.doManagedFunctionTest(ManagedObjectScope.PROCESS, false, false);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value.
	 */
	public void test_Process_Available() throws Exception {
		this.doManagedFunctionTest(ManagedObjectScope.PROCESS, true, false);
	}

	/**
	 * Ensure the {@link OptionalThreadLocal} no provide object if
	 * {@link ManagedObject} not instantiated via {@link Team} on another
	 * {@link Thread}.
	 */
	public void test_Process_NotAvailableWithTeam() throws Exception {
		this.doManagedFunctionTest(ManagedObjectScope.PROCESS, false, true);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value run by {@link Team} on another {@link Thread}.
	 */
	public void test_Process_AvailableWithTeam() throws Exception {
		this.doManagedFunctionTest(ManagedObjectScope.PROCESS, true, true);
	}

	/**
	 * Ensure the {@link OptionalThreadLocal} not provide object if
	 * {@link ManagedObject} not instantiated.
	 */
	public void test_Thread_NotAvailable() throws Exception {
		this.doManagedFunctionTest(ManagedObjectScope.THREAD, false, false);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value.
	 */
	public void test_Thread_Available() throws Exception {
		this.doManagedFunctionTest(ManagedObjectScope.THREAD, true, false);
	}

	/**
	 * Ensure the {@link OptionalThreadLocal} no provide object if
	 * {@link ManagedObject} not instantiated via {@link Team} on another
	 * {@link Thread}.
	 */
	public void test_Thread_NotAvailableWithTeam() throws Exception {
		this.doManagedFunctionTest(ManagedObjectScope.THREAD, false, true);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value run by {@link Team} on another {@link Thread}.
	 */
	public void test_Thread_AvailableWithTeam() throws Exception {
		this.doManagedFunctionTest(ManagedObjectScope.THREAD, true, true);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param scope             {@link ManagedObjectScope}.
	 * @param isExpectAvailable If object should be available.
	 * @param isTeam            If use {@link Team} for {@link ManagedFunction}.
	 */
	private void doManagedFunctionTest(ManagedObjectScope scope, boolean isExpectAvailable, boolean isTeam)
			throws Exception {

		// Create the managed object
		String object = "Should be available";
		this.constructManagedObject(object, "MOS", this.getOfficeName());
		ThreadDependencyMappingBuilder mo;
		switch (scope) {
		case PROCESS:
			mo = this.getOfficeBuilder().addProcessManagedObject("MO", "MOS");
			break;
		case THREAD:
			mo = this.getOfficeBuilder().addThreadManagedObject("MO", "MOS");
			break;
		default:
			throw new IllegalArgumentException("Invalid managed object scope " + scope);
		}

		// Ensure the same optional thread local on multiple calls
		OptionalThreadLocal<String> threadLocal = mo.getOptionalThreadLocal();
		assertNotNull("Should have thread local", threadLocal);
		assertSame("Should be same thread local on multiple calls", threadLocal, mo.getOptionalThreadLocal());

		// Construct the functions
		Work work = new Work(threadLocal);
		ReflectiveFunctionBuilder expectObject = this.constructFunction(work, "expectObject");
		expectObject.buildObject("MO");
		ReflectiveFunctionBuilder notExpectObject = this.constructFunction(work, "notExpectObject");

		// Determine if team
		if (isTeam) {
			this.constructTeam("TEAM", WorkerPerJobTeamSource.class);
			expectObject.getBuilder().setResponsibleTeam("TEAM");
			notExpectObject.getBuilder().setResponsibleTeam("TEAM");
		}

		// Undertake function (and ensure correct dependencies)
		String methodName = isExpectAvailable ? "expectObject" : "notExpectObject";
		this.invokeFunction(methodName, null);
		if (isExpectAvailable) {
			assertSame("Should inject object", object, work.dependencyObject);
			assertSame("Thread local object should be available", object, work.threadLocalObject);
		} else {
			assertNull("Should not inject object", work.dependencyObject);
			assertNull("Should not be available", work.threadLocalObject);
		}
	}

	public static class Work {

		private final OptionalThreadLocal<String> threadLocal;

		private volatile String dependencyObject;

		private volatile String threadLocalObject;

		public Work(OptionalThreadLocal<String> threadLocal) {
			this.threadLocal = threadLocal;
		}

		public void expectObject(String object) {
			this.dependencyObject = object;
			this.threadLocalObject = this.threadLocal.get();
		}

		public void notExpectObject() {
			this.threadLocalObject = this.threadLocal.get();
		}
	}

}
