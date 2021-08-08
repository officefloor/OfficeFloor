/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.spi.security.AuthenticationContext;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} for triggering
 * authentication with application specific credentials.
 * 
 * @author Daniel Sagenschneider
 */
public class StartApplicationHttpAuthenticateFunction<AC extends Serializable, C>
		extends StaticManagedFunction<StartApplicationHttpAuthenticateFunction.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		AUTHENTICATION_CONTEXT, CREDENTIALS
	}

	/*
	 * ====================== ManagedFunction =============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void execute(ManagedFunctionContext<Dependencies, None> context) throws Throwable {

		// Obtain the dependencies
		AuthenticationContext<AC, C> authenticationContext = (AuthenticationContext<AC, C>) context
				.getObject(Dependencies.AUTHENTICATION_CONTEXT);
		C credentials = (C) context.getObject(Dependencies.CREDENTIALS);

		// Trigger authentication
		authenticationContext.authenticate(credentials, null);
	}

}
