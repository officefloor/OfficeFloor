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
package net.officefloor.web.security.impl;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpAuthenticateContext;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} for
 * {@link HttpAuthenticationManagedObjectSource} authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectHttpAuthenticateFunction extends StaticManagedFunction<Indexed, None> {

	/**
	 * {@link HttpSecurity}
	 */
	@SuppressWarnings("rawtypes")
	private final HttpSecurity httpSecurity;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurity
	 *            {@link HttpSecurity}.
	 */
	public ManagedObjectHttpAuthenticateFunction(HttpSecurity<?, ?, ?, ?, ?> httpSecurity) {
		this.httpSecurity = httpSecurity;
	}

	/*
	 * ======================= ManagedFunction ================================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

		// Obtain the function authenticate context
		FunctionAuthenticateContext functionAuthenticateContext = (FunctionAuthenticateContext) context.getObject(0);

		// Obtain the credentials
		Object credentials = functionAuthenticateContext.getCredentials();

		// Undertake authentication
		HttpAuthenticateContextImpl authenticateContext = new HttpAuthenticateContextImpl(functionAuthenticateContext,
				context);
		Throwable failure = null;
		try {
			this.httpSecurity.authenticate(credentials, authenticateContext);
		} catch (Throwable ex) {
			failure = ex;
		}

		// Provide the results of authentication
		if (failure != null) {
			// Notify of failure in authentication
			functionAuthenticateContext.setFailure(failure);
		} else {
			// Notify if obtained security from authentication
			functionAuthenticateContext.setAccessControl(authenticateContext.accessControl);
		}

		// No further tasks
		return null;
	}

	/**
	 * {@link HttpAuthenticateContext} implementation.
	 */
	private static class HttpAuthenticateContextImpl<AC, C, O extends Enum<O>>
			implements HttpAuthenticateContext<AC, O> {

		/**
		 * {@link FunctionAuthenticateContext}.
		 */
		private final FunctionAuthenticateContext<AC, C> functionAuthenticateContext;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, None> functionContext;

		/**
		 * Access control.
		 */
		private AC accessControl = null;

		/**
		 * Initiate.
		 * 
		 * @param functionAuthenticateContext
		 *            {@link FunctionAuthenticateContext}.
		 * @param functionContext
		 *            {@link ManagedFunctionContext}.
		 */
		public HttpAuthenticateContextImpl(FunctionAuthenticateContext<AC, C> functionAuthenticateContext,
				ManagedFunctionContext<Indexed, None> functionContext) {
			this.functionAuthenticateContext = functionAuthenticateContext;
			this.functionContext = functionContext;
		}

		/*
		 * ================== HttpAuthenticateContext ========================
		 */

		@Override
		public ServerHttpConnection getConnection() {
			return this.functionAuthenticateContext.getConnection();
		}

		@Override
		public HttpSession getSession() {
			return this.functionAuthenticateContext.getSession();
		}

		@Override
		public Object getObject(O key) {
			// Obtain the relative index (offset for task authenticate context)
			int index = key.ordinal() + 1;
			return this.functionContext.getObject(index);
		}

		@Override
		public void setAccessControl(AC accessControl) {
			this.accessControl = accessControl;
		}
	}

}