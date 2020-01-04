/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.security.impl;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link ManagedFunctionFactory} to challenge the client.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpChallengeFunction<O extends Enum<O>, F extends Enum<F>> extends StaticManagedFunction<Indexed, F> {

	/**
	 * Name of the {@link HttpSecurity}.
	 */
	private final String httpSecurityName;

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity<?, ?, ?, O, F> httpSecurity;

	/**
	 * Initiate.
	 * 
	 * @param httpSecurityName Name of the {@link HttpSecurity}.
	 * @param httpSecurity     {@link HttpSecurity}.
	 */
	public HttpChallengeFunction(String httpSecurityName, HttpSecurity<?, ?, ?, O, F> httpSecurity) {
		this.httpSecurityName = httpSecurityName;
		this.httpSecurity = httpSecurity;
	}

	/*
	 * =================== ManagedFunction ======================
	 */

	@Override
	public void execute(ManagedFunctionContext<Indexed, F> context) throws Throwable {

		// Obtain the dependencies
		HttpChallengeContext httpChallengeContext = (HttpChallengeContext) context.getObject(0);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(1);
		HttpSession session = (HttpSession) context.getObject(2);
		HttpRequestState requestState = (HttpRequestState) context.getObject(3);

		// Undertake challenge
		this.httpSecurity.challenge(
				new HttpChallengeContextImpl(connection, session, requestState, context, httpChallengeContext));
	}

	/**
	 * {@link ChallengeContext} implementation.
	 */
	private class HttpChallengeContextImpl implements ChallengeContext<O, F> {

		/**
		 * {@link ServerHttpConnection}.
		 */
		private final ServerHttpConnection connection;

		/**
		 * {@link HttpSession}.
		 */
		private final HttpSession session;

		/**
		 * {@link HttpRequestState}.
		 */
		private final HttpRequestState requestState;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, F> context;

		/**
		 * {@link HttpChallengeContext}.
		 */
		private final HttpChallengeContext httpChallengeContext;

		/**
		 * Initiate.
		 * 
		 * @param connection           {@link ServerHttpConnection}.
		 * @param session              {@link HttpSession}.
		 * @param requestState         {@link HttpRequestState}.
		 * @param context              {@link ManagedFunctionContext}.
		 * @param httpChallengeContext {@link HttpChallengeContext}.
		 */
		public HttpChallengeContextImpl(ServerHttpConnection connection, HttpSession session,
				HttpRequestState requestState, ManagedFunctionContext<Indexed, F> context,
				HttpChallengeContext httpChallengeContext) {
			this.connection = connection;
			this.session = session;
			this.requestState = requestState;
			this.context = context;
			this.httpChallengeContext = httpChallengeContext;
		}

		/*
		 * ================== HttpChallengeContext ======================
		 */

		@Override
		public HttpChallenge setChallenge(String authenticationScheme, String realm) {
			return this.httpChallengeContext.setChallenge(authenticationScheme, realm);
		}

		@Override
		public ServerHttpConnection getConnection() {
			return this.connection;
		}

		@Override
		public String getQualifiedAttributeName(String attributeName) {
			return AuthenticationContextManagedObjectSource
					.getQualifiedAttributeName(HttpChallengeFunction.this.httpSecurityName, attributeName);
		}

		@Override
		public HttpSession getSession() {
			return this.session;
		}

		@Override
		public HttpRequestState getRequestState() {
			return this.requestState;
		}

		@Override
		public Object getObject(O key) {
			// Obtain the index (offset by challenge dependencies)
			int index = key.ordinal() + 4;
			return this.context.getObject(index);
		}

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			this.context.doFlow(key, parameter, callback);
		}
	}

}
