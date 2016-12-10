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
 * {@link Task} and {@link TaskFactory} for
 * {@link HttpAuthenticationManagedObjectSource} authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectHttpAuthenticateTask implements
		Task<HttpSecurityWork, Indexed, None>,
		TaskFactory<HttpSecurityWork, Indexed, None> {

	/*
	 * ==================== TaskFactory ============================
	 */

	@Override
	public Task<HttpSecurityWork, Indexed, None> createTask(
			HttpSecurityWork work) {
		return this;
	}

	/*
	 * ======================= Task ================================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object doTask(TaskContext<HttpSecurityWork, Indexed, None> context)
			throws Throwable {

		// Obtain the HTTP Security Source
		HttpSecuritySource<?, ?, ?, ?> httpSecuritySource = context.getWork()
				.getHttpSecuritySource();

		// Obtain the task authenticate context
		TaskAuthenticateContext taskAuthenticateContext = (TaskAuthenticateContext) context
				.getObject(0);

		// Undertake authentication
		HttpAuthenticateContextImpl authenticateContext = new HttpAuthenticateContextImpl(
				taskAuthenticateContext, context);
		Throwable failure = null;
		try {
			httpSecuritySource.authenticate(authenticateContext);
		} catch (Throwable ex) {
			failure = ex;
		}

		// Provide the results of authentication
		if (failure != null) {
			// Notify of failure in authentication
			taskAuthenticateContext.setFailure(failure);
		} else {
			// Notify if obtained security from authentication
			taskAuthenticateContext
					.setHttpSecurity(authenticateContext.security);
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
		 * {@link TaskAuthenticateContext}.
		 */
		private final TaskAuthenticateContext<S, C> taskAuthenticateContext;

		/**
		 * {@link TaskContext}.
		 */
		private final TaskContext<HttpSecurityWork, Indexed, None> taskContext;

		/**
		 * Security.
		 */
		private S security = null;

		/**
		 * Initiate.
		 * 
		 * @param taskAuthenticateContext
		 *            {@link TaskAuthenticateContext}.
		 * @param taskContext
		 *            {@link TaskContext}.
		 */
		public HttpAuthenticateContextImpl(
				TaskAuthenticateContext<S, C> taskAuthenticateContext,
				TaskContext<HttpSecurityWork, Indexed, None> taskContext) {
			this.taskAuthenticateContext = taskAuthenticateContext;
			this.taskContext = taskContext;
		}

		/*
		 * ================== HttpAuthenticateContext ========================
		 */

		@Override
		public C getCredentials() {
			return this.taskAuthenticateContext.getCredentials();
		}

		@Override
		public ServerHttpConnection getConnection() {
			return this.taskAuthenticateContext.getConnection();
		}

		@Override
		public HttpSession getSession() {
			return this.taskAuthenticateContext.getSession();
		}

		@Override
		public Object getObject(D key) {
			// Obtain the relative index (offset for task authenticate context)
			int index = key.ordinal() + 1;
			return this.taskContext.getObject(index);
		}

		@Override
		public void setHttpSecurity(S security) {
			this.security = security;
		}
	}

}