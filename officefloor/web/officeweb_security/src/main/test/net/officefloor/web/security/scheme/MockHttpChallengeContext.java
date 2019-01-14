/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.scheme;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Flow;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.security.impl.AuthenticationContextManagedObjectSource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.state.HttpRequestState;

/**
 * Mock {@link ChallengeContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpChallengeContext<O extends Enum<O>, F extends Enum<F>>
		implements ChallengeContext<O, F>, HttpChallenge {

	/**
	 * {@link FunctionalInterface} to handle the {@link Flow}.
	 */
	@FunctionalInterface
	public static interface MockHttpChallengeContextFlow {

		/**
		 * Undertakes the {@link Flow}.
		 */
		void doFlow();
	}

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
	 * Dependencies.
	 */
	private final Map<O, Object> dependencies = new HashMap<>();

	/**
	 * {@link MockHttpChallengeContextFlow} instances.
	 */
	private final Map<F, MockHttpChallengeContextFlow> flows = new HashMap<>();

	/**
	 * Challenge.
	 */
	private final StringBuilder challenge = new StringBuilder();

	/**
	 * Initiate.
	 *
	 * @param connection {@link ServerHttpConnection}.
	 */
	public MockHttpChallengeContext(ServerHttpConnection connection) {
		this.connection = connection;
		this.session = MockWebApp.mockSession(this.connection);
		this.requestState = MockWebApp.mockRequestState(this.connection);
	}

	/**
	 * Initiate.
	 */
	public MockHttpChallengeContext() {
		this(MockHttpServer.mockConnection());
	}

	/**
	 * Registers an object.
	 * 
	 * @param key        Key for dependency.
	 * @param dependency Dependency object.
	 */
	public void registerObject(O key, Object dependency) {
		this.dependencies.put(key, dependency);
	}

	/**
	 * Registers a {@link Flow}.
	 * 
	 * @param key  Key to the {@link Flow}.
	 * @param flow {@link MockHttpChallengeContextFlow}.
	 */
	public void registerFlow(F key, MockHttpChallengeContextFlow flow) {
		this.flows.put(key, flow);
	}

	/**
	 * Obtains the <code>WWW-Authenticate</code> challenge.
	 * 
	 * @return Challenge.
	 */
	public String getChallenge() {
		return this.challenge.toString();
	}

	/*
	 * =================== HttpChallengeContext =====================
	 */

	@Override
	public HttpChallenge setChallenge(String authenticationScheme, String realm) {
		this.challenge.append(authenticationScheme + " realm=\"" + realm + "\"");
		return this;
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
		return this.dependencies.get(key);
	}

	@Override
	public void doFlow(F key) {
		MockHttpChallengeContextFlow flow = this.flows.get(key);
		if (flow == null) {
			throw new IllegalStateException("No flow registered for key " + key.name());
		}
		flow.doFlow();
	}

	/*
	 * ===================== HttpChallenge ===========================
	 */

	@Override
	public void addParameter(String name, String value) {
		this.challenge.append(", " + name + "=" + value);
	}

	@Override
	public String getQualifiedAttributeName(String attributeName) {
		return AuthenticationContextManagedObjectSource.getQualifiedAttributeName("mock", attributeName);
	}

	@Override
	public HttpRequestState getRequestState() {
		return this.requestState;
	}

}