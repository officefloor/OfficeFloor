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
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link Task} and {@link TaskFactory} to log out.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectHttpLogoutTask implements
		Task<HttpSecurityWork, Indexed, None>,
		TaskFactory<HttpSecurityWork, Indexed, None> {

	/*
	 * =================== HttpChallengeTask ======================
	 */

	@Override
	public Task<HttpSecurityWork, Indexed, None> createTask(
			HttpSecurityWork work) {
		return this;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object doTask(TaskContext<HttpSecurityWork, Indexed, None> context)
			throws Throwable {

		// Obtain the dependencies
		TaskLogoutContext logoutContext = (TaskLogoutContext) context
				.getObject(0);
		HttpLogoutRequest request = logoutContext.getHttpLogoutRequest();
		ServerHttpConnection connection = logoutContext.getConnection();
		HttpSession session = logoutContext.getSession();

		// Obtain the HTTP Security Source
		HttpSecuritySource<?, ?, ?, ?> httpSecuritySource = context.getWork()
				.getHttpSecuritySource();

		// Logout
		HttpLogoutContextImpl httpLogoutContext = new HttpLogoutContextImpl(
				connection, session, context);
		try {
			httpSecuritySource.logout(httpLogoutContext);

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
	private static class HttpLogoutContextImpl<D extends Enum<D>> implements
			HttpLogoutContext<D> {

		/**
		 * {@link ServerHttpConnection}.
		 */
		private final ServerHttpConnection connection;

		/**
		 * {@link HttpSession}.
		 */
		private final HttpSession session;

		/**
		 * {@link TaskContext}.
		 */
		private final TaskContext<HttpSecurityWork, Indexed, None> context;

		/**
		 * Initiate.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @param session
		 *            {@link HttpSession}.
		 * @param context
		 *            {@link TaskContext}.
		 */
		public HttpLogoutContextImpl(ServerHttpConnection connection,
				HttpSession session,
				TaskContext<HttpSecurityWork, Indexed, None> context) {
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