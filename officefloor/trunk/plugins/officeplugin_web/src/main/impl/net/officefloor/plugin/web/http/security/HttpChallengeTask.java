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
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.route.HttpUrlContinuation;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link Task} and {@link TaskFactory} to challenge the client.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpChallengeTask implements
		Task<HttpSecurityWork, Indexed, Indexed>,
		TaskFactory<HttpSecurityWork, Indexed, Indexed> {

	/*
	 * =================== HttpChallengeTask ======================
	 */

	@Override
	public Task<HttpSecurityWork, Indexed, Indexed> createTask(
			HttpSecurityWork work) {
		return this;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object doTask(TaskContext<HttpSecurityWork, Indexed, Indexed> context)
			throws Throwable {

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(1);
		HttpSession session = (HttpSession) context.getObject(2);
		HttpRequestState requestState = (HttpRequestState) context.getObject(3);

		// Save the request
		HttpUrlContinuation.saveRequest(
				HttpSecurityWork.ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO,
				connection, requestState, session);

		// Obtain the HTTP Security Source
		HttpSecuritySource<?, ?, ?, ?> httpSecuritySource = context.getWork()
				.getHttpSecuritySource();

		// Undertake challenge
		HttpChallengeContextImpl challengeContext = new HttpChallengeContextImpl(
				connection, session, context);
		try {
			httpSecuritySource.challenge(challengeContext);
		} catch (Throwable ex) {
			// Allow handling of the failure
			context.doFlow(0, ex);
		}

		// No further tasks
		return null;
	}

	/**
	 * {@link HttpChallengeContext} implementation.
	 */
	private static class HttpChallengeContextImpl<D extends Enum<D>, F extends Enum<F>>
			implements HttpChallengeContext<D, F> {

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
		private final TaskContext<HttpSecurityWork, Indexed, Indexed> context;

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
		public HttpChallengeContextImpl(ServerHttpConnection connection,
				HttpSession session,
				TaskContext<HttpSecurityWork, Indexed, Indexed> context) {
			this.connection = connection;
			this.session = session;
			this.context = context;
		}

		/*
		 * ================== HttpChallengeContext ======================
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
			// Obtain the index (offset by challenge dependencies)
			int index = key.ordinal() + 4;
			return this.context.getObject(index);
		}

		@Override
		public void doFlow(F key) {
			// Obtain the index (offset by challenge flows)
			int index = key.ordinal() + 1;
			this.context.doFlow(index, null);
		}
	}

}