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
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link ManagedFunctionFactory} to challenge the client.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpChallengeFunction<O extends Enum<O>, F extends Enum<F>>
		extends StaticManagedFunction<Indexed, Indexed> {

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
		HttpChallengeContext httpChallengeContext = (HttpChallengeContext) context.getObject(0);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(1);
		HttpSession session = (HttpSession) context.getObject(2);

		// Undertake challenge
		this.httpSecurity.challenge(new HttpChallengeContextImpl(connection, session, context, httpChallengeContext));

		// No further functions
		return null;
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
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<Indexed, Indexed> context;

		/**
		 * {@link HttpChallengeContext}.
		 */
		private final HttpChallengeContext httpChallengeContext;

		/**
		 * Initiate.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @param session
		 *            {@link HttpSession}.
		 * @param context
		 *            {@link ManagedFunctionContext}.
		 * @param httpChallengeContext
		 *            {@link HttpChallengeContext}.
		 */
		public HttpChallengeContextImpl(ServerHttpConnection connection, HttpSession session,
				ManagedFunctionContext<Indexed, Indexed> context, HttpChallengeContext httpChallengeContext) {
			this.connection = connection;
			this.session = session;
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
			this.context.doFlow(key.ordinal(), null, null);
		}
	}

}