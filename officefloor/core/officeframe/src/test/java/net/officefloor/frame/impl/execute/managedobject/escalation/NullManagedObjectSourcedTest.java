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

package net.officefloor.frame.impl.execute.managedobject.escalation;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure failure if <code>null</code> {@link ManagedObject} was sourced.
 *
 * @author Daniel Sagenschneider
 */
public class NullManagedObjectSourcedTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can handle a <code>null</code> {@link ManagedObject} bound to
	 * {@link ProcessState}.
	 */
	public void testNullManagedObject_boundTo_Process() throws Exception {
		this.doNullManagedObjectTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can handle a <code>null</code> {@link ManagedObject} bound to
	 * {@link ThreadState}.
	 */
	public void testNullManagedObject_boundTo_Thread() throws Exception {
		this.doNullManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can handle a <code>null</code> {@link ManagedObject} bound to
	 * {@link ManagedFunction}.
	 */
	public void testNullManagedObject_boundTo_Function() throws Exception {
		this.doNullManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can handle <code>null</code> {@link ManagedObject}.
	 */
	private void doNullManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		this.constructManagedObject("MO", NullManagedObjectSource.class, this.getOfficeName());

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO");
		this.bindManagedObject("MO", scope, task.getBuilder());

		// Invoke the function
		Closure<Throwable> failure = new Closure<>();
		this.triggerFunction("task", null, (escalation) -> failure.value = escalation);

		// Ensure appropriate failure
		assertNotNull("Should be failure as null ManagedObject", failure.value);
		assertEquals("Incorrect failure",
				"Null ManagedObject provided for MO from source " + NullManagedObjectSource.class.getName(),
				failure.value.getMessage());
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(Object object) {
		}
	}

	/**
	 * {@link ManagedObjectSource} that provides a <code>null</code>
	 * {@link ManagedObject}.
	 */
	@TestSource
	public static class NullManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return null;
		}
	}

}
