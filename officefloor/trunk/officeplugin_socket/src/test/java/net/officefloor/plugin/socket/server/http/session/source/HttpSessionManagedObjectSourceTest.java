/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.session.source;

import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * Tests the {@link HttpSessionManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpSessionManagedObjectSourceTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Ensure state is maintained by the {@link HttpSession} across multiple
	 * {@link HttpRequest} calls made by a user.
	 */
	public void testHttpSessionStateAcrossCalls() {
		// TODO test HttpSession across HttpRequests
		fail("TODO test HttpSession across HttpRequests");
	}
}