/*-
 * #%L
 * OfficeCompiler
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
