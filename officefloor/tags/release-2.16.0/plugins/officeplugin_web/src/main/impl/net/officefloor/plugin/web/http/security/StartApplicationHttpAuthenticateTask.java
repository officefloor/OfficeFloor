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

/**
 * {@link Task} and {@link TaskFactory} for triggering authentication with
 * application specific credentials.
 * 
 * @author Daniel Sagenschneider
 */
public class StartApplicationHttpAuthenticateTask
		implements
		TaskFactory<HttpSecurityWork, StartApplicationHttpAuthenticateTask.Dependencies, StartApplicationHttpAuthenticateTask.Flows>,
		Task<HttpSecurityWork, StartApplicationHttpAuthenticateTask.Dependencies, StartApplicationHttpAuthenticateTask.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		CREDENTIALS, HTTP_AUTHENTICATION
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object doTask(
			TaskContext<HttpSecurityWork, Dependencies, Flows> context)
			throws Throwable {

		// Obtain the dependencies
		Object credentials = context.getObject(Dependencies.CREDENTIALS);
		HttpAuthentication authentication = (HttpAuthentication) context
				.getObject(Dependencies.HTTP_AUTHENTICATION);

		// Trigger authentication
		try {
			authentication.authenticate(new HttpAuthenticateRequestImpl(
					credentials));
		} catch (Throwable ex) {
			// Trigger failure
			context.doFlow(Flows.FAILURE, ex);
		}

		// No further tasks
		return null;
	}

	/**
	 * {@link HttpAuthenticateRequest} implementation.
	 */
	private static class HttpAuthenticateRequestImpl<C> implements
			HttpAuthenticateRequest<C> {

		/**
		 * Credentials.
		 */
		private final C credentials;

		/**
		 * Initiate.
		 * 
		 * @param credentials
		 *            Credentials.
		 */
		public HttpAuthenticateRequestImpl(C credentials) {
			this.credentials = credentials;
		}

		/*
		 * ==================== HttpAuthenticateRequest ====================
		 */

		@Override
		public C getCredentials() {
			return this.credentials;
		}

		@Override
		public void authenticationComplete() {
			// Do nothing as complete only run when authentication complete
		}
	}

}