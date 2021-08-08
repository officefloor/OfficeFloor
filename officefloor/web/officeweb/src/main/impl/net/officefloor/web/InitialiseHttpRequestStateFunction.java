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
import net.officefloor.web.state.HttpArgument;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link ManagedFunction} to initialise the {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
public class InitialiseHttpRequestStateFunction implements
		ManagedFunctionFactory<InitialiseHttpRequestStateFunction.InitialiseHttpRequestStateDependencies, None>,
		ManagedFunction<InitialiseHttpRequestStateFunction.InitialiseHttpRequestStateDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum InitialiseHttpRequestStateDependencies {
		PATH_ARGUMENTS, HTTP_REQUEST_STATE
	}

	/*
	 * ================ ManagedFunctionFactory ===============
	 */

	@Override
	public ManagedFunction<InitialiseHttpRequestStateDependencies, None> createManagedFunction() throws Throwable {
		return this;
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public void execute(ManagedFunctionContext<InitialiseHttpRequestStateDependencies, None> context) throws Throwable {

		// Obtain the dependencies
		HttpArgument arguments = (HttpArgument) context
				.getObject(InitialiseHttpRequestStateDependencies.PATH_ARGUMENTS);
		HttpRequestState requestState = (HttpRequestState) context
				.getObject(InitialiseHttpRequestStateDependencies.HTTP_REQUEST_STATE);

		// Initialise the request state
		HttpRequestStateManagedObjectSource.initialiseHttpRequestState(arguments, requestState);
	}

}
