/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
