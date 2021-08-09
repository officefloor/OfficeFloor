/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;

/**
 * {@link ManagedFunction} to trigger the interception before routing.
 * 
 * @author Daniel Sagenschneider
 */
public class InterceptFunction implements ManagedFunctionFactory<None, None>, ManagedFunction<None, None> {

	/*
	 * ================ ManagedFunctionFactory =================
	 */

	@Override
	public ManagedFunction<None, None> createManagedFunction() throws Throwable {
		return this;
	}

	/*
	 * =================== ManagedFunction =====================
	 */

	@Override
	public void execute(ManagedFunctionContext<None, None> context) throws Throwable {
		// Do nothing, as just linked next to interception
	}

}
