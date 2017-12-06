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
import net.officefloor.web.spi.security.HttpLogoutContext;
import net.officefloor.web.spi.security.HttpLogoutRequest;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} to log out.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectHttpLogoutFunction extends StaticManagedFunction<Indexed, None> {

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<?, ?, ?, ?, ?> httpSecurity;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurity
	 *            {@link HttpSecurity}.
	 */
	public ManagedObjectHttpLogoutFunction(HttpSecurity<?, ?, ?, ?, ?> httpSecurity) {
		this.httpSecurity = httpSecurity;
	}

	/*
	 * =================== ManagedFunction ======================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

		// Obtain the dependencies
		FunctionLogoutContext logoutContext = (FunctionLogoutContext) context.getObject(0);
		HttpLogoutRequest request = logoutContext.getHttpLogoutRequest();

		// Logout
		HttpLogoutContextImpl httpLogoutContext = new HttpLogoutContextImpl(logoutContext, context);
		try {
			this.httpSecurity.logout(httpLogoutContext);

			// Notify of successful logout
			if (request != null) {
				request.logoutComplete(null);
			}

		} catch (Throwable ex) {
			// Notify failure in logging out
			if (request != null) {
				request.logoutComplete(ex);
			}
		}

		// No further tasks
		return null;
	}

	/**
	 * {@link HttpLogoutContext} implementation.
	 */
	private static class HttpLogoutContextImpl<O extends Enum<O>> implements HttpLogoutContext<O> {

		/**
		 * {@link FunctionLogoutContext}.
		 */
		private FunctionLogoutContext logoutContext;

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
		private HttpLogoutContextImpl(FunctionLogoutContext logoutContext,
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