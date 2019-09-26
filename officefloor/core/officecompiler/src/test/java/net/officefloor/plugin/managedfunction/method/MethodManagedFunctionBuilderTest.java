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
package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Qualified;

/**
 * Tests the {@link MethodManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodManagedFunctionBuilderTest extends OfficeFrameTestCase {

	/**
	 * Ensure can invoke {@link Method} with no parameters and no return.
	 */
	public void testNoParametersAndNoReturn() {
		NoParametersAndNoReturnFunction instance = new NoParametersAndNoReturnFunction();
		MethodResult result = MethodManagedFunctionBuilderUtil.runMethod(instance, "noParametersAndNoReturn", null,
				null);
		assertNull("Should be no return value", result.getReturnValue());
		assertTrue("Should run method", instance.isRun);
	}

	public static class NoParametersAndNoReturnFunction {

		public boolean isRun = false;

		public void noParametersAndNoReturn() {
			isRun = true;
		}
	}

	/**
	 * Ensure can obtain default return value.
	 */
	public void testReturnValue() {
		MethodResult result = MethodManagedFunctionBuilderUtil.runMethod(new ReturnValueFunction(), "returnValue",
				(type) -> type.setReturnType(String.class), null);
		assertEquals("Incorrect return value", ReturnValueFunction.RETURN_VALUE, result.getReturnValue());
	}

	public static class ReturnValueFunction {

		public static final String RETURN_VALUE = "RETURN";

		public String returnValue() {
			return RETURN_VALUE;
		}
	}

	/**
	 * Ensure invoke static {@link Method}.
	 */
	public void testStatic() {
		StaticFunction.isRun = false; // ensure correct state
		MethodResult result = MethodManagedFunctionBuilderUtil.runStaticMethod(StaticFunction.class, "staticMethod",
				null, null);
		assertNull("Should be no return value", result.getReturnValue());
		assertTrue("Should run method", StaticFunction.isRun);
	}

	public static class StaticFunction {

		public static boolean isRun = false;

		public static void staticMethod() {
			isRun = true;
		}
	}

	/**
	 * Ensure inject dependency.
	 */
	public void testDependency() {
		DependencyFunction dependency = new DependencyFunction();
		final String object = "DEPENDENCY";
		MethodManagedFunctionBuilderUtil.runMethod(dependency, "method", (type) -> {
			type.addObject(String.class).setLabel(String.class.getName());
		}, (context) -> {
			context.setObject(0, object);
		});
		assertEquals("Incorrect dependency", object, dependency.object);
	}

	public static class DependencyFunction {

		public String object = null;

		public void method(String dependency) {
			this.object = dependency;
		}
	}

	/**
	 * Ensure inject qualified dependency.
	 */
	public void testQualifiedDependency() {
		QualifiedDependencyFunction dependency = new QualifiedDependencyFunction();
		final String object = "DEPENDENCY";
		MethodManagedFunctionBuilderUtil.runMethod(dependency, "method", (type) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> objectType = type.addObject(String.class);
			objectType.setLabel("qualified-" + String.class.getName());
			objectType.setTypeQualifier("qualified");
		}, (context) -> {
			context.setObject(0, object);
		});
		assertEquals("Incorrect dependency", object, dependency.object);
	}

	public static class QualifiedDependencyFunction {

		public String object = null;

		public void method(@Qualified("qualified") String dependency) {
			this.object = dependency;
		}
	}

}