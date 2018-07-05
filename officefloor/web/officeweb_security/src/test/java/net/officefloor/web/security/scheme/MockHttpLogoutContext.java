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

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.LogoutContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Mock {@link LogoutContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpLogoutContext<D extends Enum<D>> implements LogoutContext<D> {

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
	 */
	public MockHttpLogoutContext() {
		this.connection = MockHttpServer.mockConnection();
		this.session = MockWebApp.mockSession(this.connection);
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