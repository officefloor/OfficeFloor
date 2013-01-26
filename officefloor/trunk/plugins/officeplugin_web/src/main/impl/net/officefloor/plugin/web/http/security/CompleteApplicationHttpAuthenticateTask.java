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

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.route.HttpUrlContinuation;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link Task} and {@link TaskFactory} for completing authentication with
 * application specific credentials.
 * 
 * @author Daniel Sagenschneider
 */
public class CompleteApplicationHttpAuthenticateTask
		implements
		TaskFactory<HttpSecurityWork, CompleteApplicationHttpAuthenticateTask.Dependencies, CompleteApplicationHttpAuthenticateTask.Flows>,
		Task<HttpSecurityWork, CompleteApplicationHttpAuthenticateTask.Dependencies, CompleteApplicationHttpAuthenticateTask.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_AUTHENTICATION, SERVER_HTTP_CONNECTION, HTTP_SESSION, REQUEST_STATE
	}

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		FAILURE
	}

	/*
	 * ====================== TaskFactory ==========================
	 */

	@Override
	public Task<HttpSecurityWork, Dependencies, Flows> createTask(
			HttpSecurityWork work) {
		return this;
	}

	/*
	 * ========================= Task =============================
	 */

	@Override
	@SuppressWarnings("rawtypes")
	public Object doTask(
			TaskContext<HttpSecurityWork, Dependencies, Flows> context)
			throws Throwable {

		// Obtain the HTTP authentication to check on authentication
		HttpAuthentication authentication = (HttpAuthentication) context
				.getObject(Dependencies.HTTP_AUTHENTICATION);

		// Determine if security obtained from application authentication
		Object security;
		try {
			security = authentication.getHttpSecurity();
		} catch (Throwable ex) {
			// Handle the failure
			context.doFlow(Flows.FAILURE, ex);
			return null; // failed to authenticate
		}

		// Ensure have security
		if (security == null) {
			// Not authenticated
			throw new HttpAuthenticationRequiredException();
		}

		// Reinstate the request for servicing before authentication required
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		HttpSession session = (HttpSession) context
				.getObject(Dependencies.HTTP_SESSION);
		HttpRequestState requestState = (HttpRequestState) context
				.getObject(Dependencies.REQUEST_STATE);
		boolean isReinstated = HttpUrlContinuation.reinstateRequest(
				HttpSecurityWork.ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO,
				connection, requestState, session);

		// Determine if reinstated request
		if (!isReinstated) {
			// Failure as must reinstate request
			context.doFlow(Flows.FAILURE,
					new HttpAuthenticationContinuationException());
			return null; // continuation failure
		}

		// No further tasks
		return null;
	}

}