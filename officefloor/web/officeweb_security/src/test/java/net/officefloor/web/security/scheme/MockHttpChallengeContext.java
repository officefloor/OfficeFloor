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
package net.officefloor.web.security.scheme;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpChallenge;
import net.officefloor.web.spi.security.ChallengeContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Mock {@link ChallengeContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpChallengeContext<O extends Enum<O>, F extends Enum<F>>
		implements ChallengeContext<O, F>, HttpChallenge {

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session;

	/**
	 * Dependencies.
	 */
	private final Map<O, Object> dependencies = new HashMap<O, Object>();

	/**
	 * Flows.
	 */
	private final MockHttpChallengeContextFlows<F> flows;

	/**
	 * Challenge.
	 */
	private final StringBuilder challenge = new StringBuilder();

	/**
	 * Initiate.
	 *
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param testCase
	 *            {@link OfficeFrameTestCase} to create necessary mock objects.
	 */
	@SuppressWarnings("unchecked")
	public MockHttpChallengeContext(ServerHttpConnection connection, OfficeFrameTestCase testCase) {
		this.connection = connection;
		this.session = MockWebApp.mockSession(this.connection);

		// Create the necessary mock objects
		this.flows = testCase.createMock(MockHttpChallengeContextFlows.class);
	}

	/**
	 * Initiate.
	 * 
	 * @param testCase
	 *            {@link OfficeFrameTestCase} to create necessary mock objects.
	 */
	public MockHttpChallengeContext(OfficeFrameTestCase testCase) {
		this(MockHttpServer.mockConnection(), testCase);
	}

	/**
	 * Registers and object.
	 * 
	 * @param key
	 *            Key for dependency.
	 * @param dependency
	 *            Dependency object.
	 */
	public void registerObject(O key, Object dependency) {
		this.dependencies.put(key, dependency);
	}

	/**
	 * Records undertaking the flow.
	 * 
	 * @param key
	 *            Key to the flow.
	 */
	public void recordDoFlow(F key) {
		this.doFlow(key);
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
		this.flows.doFlow(key);
	}

	/**
	 * Interface to create mock object for mocking flows.
	 */
	private static interface MockHttpChallengeContextFlows<F extends Enum<F>> {

		/**
		 * Undertakes the flow.
		 * 
		 * @param key
		 *            Key for the flow.
		 */
		void doFlow(F key);
	}

	/*
	 * ===================== HttpChallenge ===========================
	 */

	@Override
	public void addParameter(String name, String value) {
		this.challenge.append(", " + name + "=" + value);
	}

}