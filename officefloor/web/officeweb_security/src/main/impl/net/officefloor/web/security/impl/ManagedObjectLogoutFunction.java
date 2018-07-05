/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.LogoutContext;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} to log out.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectLogoutFunction<AC extends Serializable, O extends Enum<O>>
		extends StaticManagedFunction<Indexed, None> {

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<?, AC, ?, O, ?> httpSecurity;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurity
	 *            {@link HttpSecurity}.
	 */
	public ManagedObjectLogoutFunction(HttpSecurity<?, AC, ?, O, ?> httpSecurity) {
		this.httpSecurity = httpSecurity;
	}

	/*
	 * =================== ManagedFunction ======================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

		// Obtain the dependencies
		final FunctionLogoutContext<AC> logoutContext = (FunctionLogoutContext<AC>) context.getObject(0);

		try {
			// Logout
			this.httpSecurity.logout(new LogoutContextImpl(logoutContext, context));

			// Notify of successful logout
			logoutContext.accessControlChange(null, null);

		} catch (Throwable ex) {
			logoutContext.accessControlChange(null, ex);
		}

		// No further functions
		return null;
	}

	/**
	 * {@link LogoutContext} implementation.
	 */
	private class LogoutContextImpl implements LogoutContext<O> {

		/**
		 * {@link FunctionLogoutContext}.
		 */
		private FunctionLogoutContext<AC> logoutContext;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, None> context;

		/**
		 * Initiate.
		 * 
		 * @param logoutContext
		 *            {@link FunctionLogoutContext}.
		 * @param context
		 *            {@link ManagedFunctionContext}.
		 */
		private LogoutContextImpl(FunctionLogoutContext<AC> logoutContext,
				ManagedFunctionContext<Indexed, None> context) {
			this.logoutContext = logoutContext;
			this.context = context;
		}

		/*
		 * ================== HttpLogoutContext ======================
		 */

		@Override
		public ServerHttpConnection getConnection() {
			return this.logoutContext.getConnection();
		}

		@Override
		public HttpSession getSession() {
			return this.logoutContext.getSession();
		}

		@Override
		public Object getObject(O key) {
			// Obtain the index (offset by logout dependencies)
			int index = key.ordinal() + 1;
			return this.context.getObject(index);
		}
	}

}