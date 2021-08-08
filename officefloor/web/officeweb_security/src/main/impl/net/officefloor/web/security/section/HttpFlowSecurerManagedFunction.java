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

package net.officefloor.web.security.section;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.build.section.HttpFlowSecurer;

/**
 * {@link HttpFlowSecurer} {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFlowSecurerManagedFunction
		extends StaticManagedFunction<Indexed, HttpFlowSecurerManagedFunction.Flows> {

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		SECURE, INSECURE
	}

	/**
	 * Indicates if parameter to pass through.
	 */
	private final boolean isParameter;

	/**
	 * Any roles.
	 */
	private final String[] anyRoles;

	/**
	 * All roles.
	 */
	private final String[] allRoles;

	/**
	 * Instantiate.
	 * 
	 * @param isParameter Indicates that there is an argument to pass through.
	 * @param anyRoles    Any roles.
	 * @param allRoles    All roles.
	 */
	public HttpFlowSecurerManagedFunction(boolean isParameter, String[] anyRoles, String[] allRoles) {
		this.isParameter = isParameter;
		this.anyRoles = anyRoles;
		this.allRoles = allRoles;
	}

	/*
	 * ================== ManagedFunction =================
	 */

	@Override
	public void execute(ManagedFunctionContext<Indexed, Flows> context) throws Throwable {

		// Obtain the HTTP authentication
		HttpAuthentication<?> authentication = (HttpAuthentication<?>) context.getObject(0);

		// Obtain the possible argument
		Object argument = null;
		if (this.isParameter) {
			argument = context.getObject(1);
		}

		// Determine if authenticated
		boolean isAuthenticated = authentication.isAuthenticated();
		if (isAuthenticated) {

			// Authenticated, so check for access
			HttpAccessControl accessControl = authentication.getAccessControl();
			if (accessControl.isAccess(this.anyRoles, this.allRoles)) {

				// Have access to secure path
				context.doFlow(Flows.SECURE, argument, null);
				return;
			}
		}

		// No access
		context.doFlow(Flows.INSECURE, argument, null);
	}

}
