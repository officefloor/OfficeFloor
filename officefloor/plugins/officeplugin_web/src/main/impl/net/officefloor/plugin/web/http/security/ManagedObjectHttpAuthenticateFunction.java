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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} for
 * {@link HttpAuthenticationManagedObjectSource} authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectHttpAuthenticateFunction extends StaticManagedFunction<Indexed, None> {

	/**
	 * {@link HttpSecuritySource}
	 */
	private final HttpSecuritySource<?, ?, ?, ?> httpSecuritySource;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 */
	public ManagedObjectHttpAuthenticateFunction(HttpSecuritySource<?, ?, ?, ?> httpSecuritySource) {
		this.httpSecuritySource = httpSecuritySource;
	}

	/*
	 * ======================= ManagedFunction ================================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

		// Obtain the function authenticate context
		FunctionAuthenticateContext functionAuthenticateContext = (FunctionAuthenticateContext) context.getObject(0);

		// Undertake authentication
		HttpAuthenticateContextImpl authenticateContext = new HttpAuthenticateContextImpl(functionAuthenticateContext,
				context);
		Throwable failure = null;
		try {
			this.httpSecuritySource.authenticate(authenticateContext);
		} catch (Throwable ex) {
			failure = ex;
		}

		// Provide the results of authentication
		if (failure != null) {
			// Notify of failure in authentication
			functionAuthenticateContext.setFailure(failure);
		} else {
			// Notify if obtained security from authentication
			functionAuthenticateContext.setHttpSecurity(authenticateContext.security);
		}

		// No further tasks
		return null;
	}

	/**
	 * {@link HttpAuthenticateContext} implementation.
	 */
	private static class HttpAuthenticateContextImpl<S, C, D extends Enum<D>>
			implements HttpAuthenticateContext<S, C, D> {

		/**
		 * {@link FunctionAuthenticateContext}.
		 */
		private final FunctionAuthenticateContext<S, C> functionAuthenticateContext;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, None> functionContext;

		/**
		 * Security.
		 */
		private S security = null;

		/**
		 * Initiate.
		 * 
		 * @param functionAuthenticateContext
		 *            {@link FunctionAuthenticateContext}.
		 * @param functionContext
		 *            {@link ManagedFunctionContext}.
		 */
		public HttpAuthenticateContextImpl(FunctionAuthenticateContext<S, C> functionAuthenticateContext,
				ManagedFunctionContext<Indexed, None> functionContext) {
			this.functionAuthenticateContext = functionAuthenticateContext;
			this.functionContext = functionContext;
		}

		/*
		 * ================== HttpAuthenticateContext ========================
		 */

		@Override
		public C getCredentials() {
			return this.functionAuthenticateContext.getCredentials();
		}

		@Override
		public ServerHttpConnection getConnection() {
			return this.functionAuthenticateContext.getConnection();
		}

		@Override
		public HttpSession getSession() {
			return this.functionAuthenticateContext.getSession();
		}

		@Override
		public Object getObject(D key) {
			// Obtain the relative index (offset for task authenticate context)
			int index = key.ordinal() + 1;
			return this.functionContext.getObject(index);
		}

		@Override
		public void setHttpSecurity(S security) {
			this.security = security;
		}
	}

}