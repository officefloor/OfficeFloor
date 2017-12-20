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
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link ManagedFunctionFactory} to challenge the client.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpChallengeFunction<O extends Enum<O>, F extends Enum<F>>
		extends StaticManagedFunction<Indexed, Indexed> {

	/**
	 * {@link HttpSession} attribute for the challenge request state.
	 */
	public static final String ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO = "CHALLENGE_REQUEST_MOMENTO";

	/**
	 * <code>WWW-Authenticate</code> {@link HttpHeaderName}.
	 */
	private static final HttpHeaderName CHALLENGE_NAME = new HttpHeaderName("WWW-Authenticate");

	/**
	 * {@link ThreadLocal} {@link StringBuilder} to reduce GC.
	 */
	private static final ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
		@Override
		protected StringBuilder initialValue() {
			return new StringBuilder();
		}
	};

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<?, ?, ?, O, F> httpSecurity;

	/**
	 * Initiate.
	 * 
	 * @param httpSecurity
	 *            {@link HttpSecurity}.
	 */
	public HttpChallengeFunction(HttpSecurity<?, ?, ?, O, F> httpSecurity) {
		this.httpSecurity = httpSecurity;
	}

	/*
	 * =================== ManagedFunction ======================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the dependencies
		AuthenticationRequiredException exception = (AuthenticationRequiredException) context.getObject(0);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(1);
		HttpSession session = (HttpSession) context.getObject(2);
		HttpRequestState requestState = (HttpRequestState) context.getObject(3);

		// Save the request (if required)
		if (exception.isSaveRequest()) {
			Serializable momento = HttpRequestStateManagedObjectSource.exportHttpRequestState(requestState);
			session.setAttribute(ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO, momento);
		}

		// Obtain the challenge string builder
		StringBuilder challenge = stringBuilder.get();
		challenge.setLength(0); // reset for use

		// Undertake challenge
		try {
			this.httpSecurity.challenge(new HttpChallengeContextImpl(connection, session, context, challenge));
		} catch (Throwable ex) {
			// Allow handling of the failure
			context.doFlow(0, ex, null);
		}

		// Determine if challenge
		if (challenge.length() > 0) {
			HttpResponse response = connection.getResponse();
			response.setStatus(HttpStatus.UNAUTHORIZED);
			response.getHeaders().addHeader(CHALLENGE_NAME, challenge.toString());
		}

		// No further functions
		return null;
	}

	/**
	 * {@link ChallengeContext} implementation.
	 */
	private class HttpChallengeContextImpl implements ChallengeContext<O, F>, HttpChallenge {

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
		 * {@link StringBuilder} to load the {@link HttpChallenge}.
		 */
		private final StringBuilder challenge;

		/**
		 * Initiate.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @param session
		 *            {@link HttpSession}.
		 * @param context
		 *            {@link ManagedFunctionContext}.
		 * @param challenge
		 *            {@link StringBuilder} to be loaded with the
		 *            {@link HttpChallenge}.
		 */
		public HttpChallengeContextImpl(ServerHttpConnection connection, HttpSession session,
				ManagedFunctionContext<Indexed, Indexed> context, StringBuilder challenge) {
			this.connection = connection;
			this.session = session;
			this.context = context;
			this.challenge = challenge;
		}

		/*
		 * ================== HttpChallengeContext ======================
		 */

		@Override
		public HttpChallenge setChallenge(String authenticationScheme, String realm) {
			if (this.challenge.length() > 0) {
				this.challenge.append(", ");
			}
			this.challenge.append(authenticationScheme);
			this.challenge.append(" realm=\"");
			this.challenge.append(realm);
			this.challenge.append("\"");
			return null;
		}

		@Override
		public ServerHttpConnection getConnection() {
			return this.connection;
		}

		@Override
		public HttpSession getSession() {
			return this.session;
		}

		@Override
		public Object getObject(O key) {
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

		/*
		 * ==================== HttpChallenge ===========================
		 */

		@Override
		public void addParameter(String name, String value) {
			this.challenge.append(", ");
			this.challenge.append(name);
			this.challenge.append("=");
			this.challenge.append(value);
		}
	}

}