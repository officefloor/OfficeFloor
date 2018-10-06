/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
	public void testNotAvailable() throws Exception {
		this.doManagedFunctionTest(false, false);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value.
	 */
	public void testAvailable() throws Exception {
		this.doManagedFunctionTest(true, false);
	}

	/**
	 * Ensure the {@link OptionalThreadLocal} no provide object if
	 * {@link ManagedObject} not instantiated via {@link Team} on another
	 * {@link Thread}.
	 */
	public void testNotAvailableWithTeam() throws Exception {
		this.doManagedFunctionTest(false, true);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value run by {@link Team} on another {@link Thread}.
	 */
	public void testAvailableWithTeam() throws Exception {
		this.doManagedFunctionTest(true, true);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param isExpectAvailable If object should be available.
	 * @param isTeam            If use {@link Team} for {@link ManagedFunction}.
	 */
	private void doManagedFunctionTest(boolean isExpectAvailable, boolean isTeam) throws Exception {

		// Create the managed object
		String object = "Should be available";
		this.constructManagedObject(object, "MOS", this.getOfficeName());
		ThreadDependencyMappingBuilder mo = this.getOfficeBuilder().addThreadManagedObject("MO", "MOS");

		// Construct the functions
		Work work = new Work(mo.getOptionalThreadLocal());
		ReflectiveFunctionBuilder expectObject = this.constructFunction(work, "expectObject");
		expectObject.buildObject("MO", ManagedObjectScope.THREAD);
		ReflectiveFunctionBuilder notExpectObject = this.constructFunction(work, "notExpectedObject");

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

		private String dependencyObject;

		private String threadLocalObject;

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