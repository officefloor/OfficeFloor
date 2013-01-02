/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.security;

import java.io.IOException;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;

/**
 * {@link Task} for executing the authentication for a
 * {@link HttpSecurityWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityTask
		extends
		AbstractSingleTask<HttpSecurityTask, HttpSecurityTask.DependencyKeys, HttpSecurityTask.FlowKeys> {

	/**
	 * Dependency keys for he {@link HttpSecurityTask}.
	 */
	public static enum DependencyKeys {
		HTTP_SECURITY_SERVICE
	}

	/**
	 * Flow keys for the {@link HttpSecurityTask}.
	 */
	public static enum FlowKeys {
		AUTHENTICATED, UNAUTHENTICATED
	}

	/*
	 * ======================= Task ==============================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpSecurityTask, DependencyKeys, FlowKeys> context)
			throws IOException, AuthenticationException {

		// Obtain the HTTP Security Service
		HttpSecurityService service = (HttpSecurityService) context
				.getObject(DependencyKeys.HTTP_SECURITY_SERVICE);

		// Attempt to authenticate
		HttpSecurity security = service.authenticate();

		// Determine if authenticated
		if (security != null) {
			// Authenticated
			context.doFlow(FlowKeys.AUTHENTICATED, security);
		} else {
			// Not authenticated, so load unauthenticated details
			service.loadUnauthorised();

			// Handle not authenticated
			context.doFlow(FlowKeys.UNAUTHENTICATED, null);
		}

		// Return the security
		return security;
	}

}