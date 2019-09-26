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
package net.officefloor.compile.impl.managedfunction.clazz;

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.MethodManagedFunctionBuilder;

/**
 * Tests the {@link MethodManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodManagedFunctionBuilderTest extends OfficeFrameTestCase {

	/**
	 * Ensure no parameters and no return.
	 */
	public void testNoParametersAndNoReturn() throws Throwable {
		NoParametersAndNoReturn instance = new NoParametersAndNoReturn();
		MethodResult result = MethodManagedFunctionBuilderUtil.runMethod(instance, "noParametersAndNoReturn", null,
				null);
		assertNull("Should be no return value", result.getReturnValue());
		assertTrue("Should run method", instance.isRun);
	}

	public static class NoParametersAndNoReturn {

		public boolean isRun = false;

		public void noParametersAndNoReturn() {
			isRun = true;
		}
	}

	/**
	 * Ensure static.
	 */
	public void testStatic() throws Throwable {
		Static.isRun = false; // ensure correct state
		MethodResult result = MethodManagedFunctionBuilderUtil.runStaticMethod(Static.class, "staticMethod", null,
				null);
		assertNull("Should be no return value", result.getReturnValue());
		assertTrue("Should run method", Static.isRun);
	}

	public static class Static {

		public static boolean isRun = false;

		public static void staticMethod() {
			isRun = true;
		}
	}
	
	

}