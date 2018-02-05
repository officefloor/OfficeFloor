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
package net.officefloor.web.security.section;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
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
public class HttpFlowSecurerManagedFunction extends
		StaticManagedFunction<HttpFlowSecurerManagedFunction.Depdendencies, HttpFlowSecurerManagedFunction.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Depdendencies {
		HTTP_AUTHENTICATION
	}

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		SECURE, INSECURE
	}

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
	 * @param anyRoles
	 *            Any roles.
	 * @param allRoles
	 *            All roles.
	 */
	public HttpFlowSecurerManagedFunction(String[] anyRoles, String[] allRoles) {
		this.anyRoles = anyRoles;
		this.allRoles = allRoles;
	}

	/*
	 * ================== ManagedFunction =================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Depdendencies, Flows> context) throws Throwable {

		// Obtain the HTTP authentication
		HttpAuthentication<?> authentication = (HttpAuthentication<?>) context
				.getObject(Depdendencies.HTTP_AUTHENTICATION);

		// Determine if authenticated
		boolean isAuthenticated = authentication.isAuthenticated();
		if (isAuthenticated) {

			// Authenticated, so check for access
			HttpAccessControl accessControl = authentication.getAccessControl();
			if (accessControl.isAccess(this.anyRoles, this.allRoles)) {

				// Have access to secure path
				context.doFlow(Flows.SECURE, null, null);
				return null;
			}
		}

		// No access
		context.doFlow(Flows.INSECURE, null, null);
		return null;
	}

}