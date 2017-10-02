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
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} to log out.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectHttpLogoutFunction extends StaticManagedFunction<Indexed, None> {

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<?, ?, ?, ?> httpSecuritySource;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 */
	public ManagedObjectHttpLogoutFunction(HttpSecuritySource<?, ?, ?, ?> httpSecuritySource) {
		this.httpSecuritySource = httpSecuritySource;
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
		ServerHttpConnection connection = logoutContext.getConnection();
		HttpSession session = logoutContext.getSession();

		// Logout
		HttpLogoutContextImpl httpLogoutContext = new HttpLogoutContextImpl(connection, session, context);
		try {
			this.httpSecuritySource.logout(httpLogoutContext);

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
	private static class HttpLogoutContextImpl<D extends Enum<D>> implements HttpLogoutContext<D> {

		/**
		 * {@link ServerHttpConnection}.
		 */
		private final ServerHttpConnection connection;

		/**
		 * {@link HttpSession}.
		 */
		private final HttpSession session;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, None> context;

		/**
		 * Initiate.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @param session
		 *            {@link HttpSession}.
		 * @param context
		 *            {@link ManagedFunctionContext}.
		 */
		public HttpLogoutContextImpl(ServerHttpConnection connection, HttpSession session,
				ManagedFunctionContext<Indexed, None> context) {
			this.connection = connection;
			this.session = session;
			this.context = context;
		}

		/*
		 * ================== HttpLogoutContext ======================
		 */

		@Override
		public ServerHttpConnection getConnection() {
			return this.connection;
		}

		@Override
		public HttpSession getSession() {
			return this.session;
		}

		@Override
		public Object getObject(D key) {
			// Obtain the index (offset by logout dependencies)
			int index = key.ordinal() + 1;
			return this.context.getObject(index);
		}
	}

}