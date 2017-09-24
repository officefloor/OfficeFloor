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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} for triggering
 * authentication with application specific credentials.
 * 
 * @author Daniel Sagenschneider
 */
public class StartApplicationHttpAuthenticateFunction extends
		StaticManagedFunction<StartApplicationHttpAuthenticateFunction.Dependencies, StartApplicationHttpAuthenticateFunction.Flows> {

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
	 * ====================== ManagedFunction =============================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object execute(ManagedFunctionContext<Dependencies, Flows> context) throws Throwable {

		// Obtain the dependencies
		Object credentials = context.getObject(Dependencies.CREDENTIALS);
		HttpAuthentication authentication = (HttpAuthentication) context.getObject(Dependencies.HTTP_AUTHENTICATION);

		// Trigger authentication
		try {
			authentication.authenticate(new HttpAuthenticateRequestImpl(credentials));
		} catch (Throwable ex) {
			// Trigger failure
			context.doFlow(Flows.FAILURE, ex, null);
		}

		// No further tasks
		return null;
	}

	/**
	 * {@link HttpAuthenticateRequest} implementation.
	 */
	private static class HttpAuthenticateRequestImpl<C> implements HttpAuthenticateRequest<C> {

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