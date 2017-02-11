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
package net.officefloor.frame.impl.execute.managedobject.escalation;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
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