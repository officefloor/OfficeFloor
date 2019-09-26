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

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure can inject {@link ManagedFunctionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionContextInjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to inject {@link ManagedFunctionContext}.
	 */
	public void testManagedFunctionContext() {
		Closure<ManagedFunctionContext<?, ?>> managedFunctionContext = new Closure<>();
		ManagedFunctionContextFunction instance = new ManagedFunctionContextFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", null, (context) -> {
			managedFunctionContext.value = (ManagedFunctionContext<?, ?>) context;
		});
		assertSame("Should inject the managed function context", managedFunctionContext.value, instance.context);
	}

	public static class ManagedFunctionContextFunction {

		public ManagedFunctionContext<?, ?> context;

		public void method(ManagedFunctionContext<?, ?> context) {
			this.context = context;
		}
	}

}