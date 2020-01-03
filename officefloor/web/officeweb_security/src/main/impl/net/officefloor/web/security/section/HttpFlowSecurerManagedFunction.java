/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
