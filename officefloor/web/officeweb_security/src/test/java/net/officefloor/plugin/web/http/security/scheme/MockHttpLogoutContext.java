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
package net.officefloor.plugin.web.http.security.scheme;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpLogoutContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;

/**
 * Mock {@link HttpLogoutContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpLogoutContext<D extends Enum<D>> implements
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
	 * Dependencies.
	 */
	private final Map<D, Object> dependencies = new HashMap<D, Object>();

	/**
	 * Initiate.
	 * 
	 * @param testCase
	 *            {@link OfficeFrameTestCase} to create necessary mock objects.
	 */
	public MockHttpLogoutContext(OfficeFrameTestCase testCase) {

		// Create the necessary mock objects
		this.connection = testCase.createMock(ServerHttpConnection.class);
		this.session = testCase.createMock(HttpSession.class);
	}

	/**
	 * Registers and object.
	 * 
	 * @param key
	 *            Key for dependency.
	 * @param dependency
	 *            Dependency object.
	 */
	public void registerObject(D key, Object dependency) {
		this.dependencies.put(key, dependency);
	}

	/*
	 * ==================== HttpLogoutContext =========================
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
		return this.dependencies.get(key);
	}

}