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

import java.io.Serializable;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} for
 * {@link AuthenticationContextManagedObjectSource} authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectAuthenticateFunction<AC extends Serializable, C>
		extends StaticManagedFunction<Indexed, None> {

	/**
	 * {@link HttpSecurity}
	 */
	private final HttpSecurity<?, AC, C, ?, ?> httpSecurity;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurity
	 *            {@link HttpSecurity}.
	 */
	public ManagedObjectAuthenticateFunction(HttpSecurity<?, AC, C, ?, ?> httpSecurity) {
		this.httpSecurity = httpSecurity;
	}

	/*
	 * ======================= ManagedFunction ================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

		// Obtain the function authenticate context
		FunctionAuthenticateContext<AC, C> functionAuthenticateContext = (FunctionAuthenticateContext<AC, C>) context
				.getObject(0);

		// Obtain the credentials
		C credentials = functionAuthenticateContext.getCredentials();

		// Undertake authentication
		try {
			this.httpSecurity.authenticate(credentials,
					new AuthenticateContextImpl<>(functionAuthenticateContext, context));
		} catch (Throwable ex) {
			// Notify of failure in authentication
			functionAuthenticateContext.accessControlChange(null, ex);
		}

		// Nothing further
		return null;
	}

	/**
	 * {@link AuthenticateContext} implementation.
	 */
	private static class AuthenticateContextImpl<AC extends Serializable, C, O extends Enum<O>>
			implements AuthenticateContext<AC, O> {

		/**
		 * {@link FunctionAuthenticateContext}.
		 */
		private final FunctionAuthenticateContext<AC, C> functionAuthenticateContext;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, None> functionContext;

		/**
		 * Initiate.
		 * 
		 * @param functionAuthenticateContext
		 *            {@link FunctionAuthenticateContext}.
		 * @param functionContext
		 *            {@link ManagedFunctionContext}.
		 */
		public AuthenticateContextImpl(FunctionAuthenticateContext<AC, C> functionAuthenticateContext,
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
			return this.functionContext.getObject(key.ordinal());
		}

		@Override
		public void accessControlChange(AC accessControl, Throwable escalation) {
			this.functionAuthenticateContext.accessControlChange(accessControl, escalation);
		}
	}

}