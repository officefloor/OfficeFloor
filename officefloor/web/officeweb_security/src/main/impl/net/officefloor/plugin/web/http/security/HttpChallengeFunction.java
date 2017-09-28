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
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.web.http.route.HttpUrlContinuation;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link ManagedFunctionFactory} to challenge the client.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpChallengeFunction extends StaticManagedFunction<Indexed, Indexed> {

	/**
	 * {@link HttpSession} attribute for the challenge request state.
	 */
	public static final String ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO = "CHALLENGE_REQUEST_MOMENTO";

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<?, ?, ?, ?> httpSecuritySource;

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 */
	public HttpChallengeFunction(HttpSecuritySource<?, ?, ?, ?> httpSecuritySource) {
		this.httpSecuritySource = httpSecuritySource;
	}

	/*
	 * =================== ManagedFunction ======================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the dependencies
		HttpAuthenticationRequiredException exception = (HttpAuthenticationRequiredException) context.getObject(0);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(1);
		HttpSession session = (HttpSession) context.getObject(2);
		HttpRequestState requestState = (HttpRequestState) context.getObject(3);

		// Save the request (if required)
		if (exception.isSaveRequest()) {
			HttpUrlContinuation.saveRequest(ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO, connection, requestState, session);
		}

		// Undertake challenge
		HttpChallengeContextImpl challengeContext = new HttpChallengeContextImpl(connection, session, context);
		try {
			this.httpSecuritySource.challenge(challengeContext);
		} catch (Throwable ex) {
			// Allow handling of the failure
			context.doFlow(0, ex, null);
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
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, Indexed> context;

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
		public HttpChallengeContextImpl(ServerHttpConnection connection, HttpSession session,
				ManagedFunctionContext<Indexed, Indexed> context) {
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
			this.context.doFlow(index, null, null);
		}
	}

}