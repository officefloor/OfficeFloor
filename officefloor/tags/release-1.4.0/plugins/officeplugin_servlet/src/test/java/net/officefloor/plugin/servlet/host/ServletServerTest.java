/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.servlet.host;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.resource.ResourceLocator;

/**
 * Tests the {@link ServletServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerTest extends OfficeFrameTestCase {

	/**
	 * Ensure appropriate values are returned.
	 */
	public void testState() {

		final String SERVER_NAME = "officefloor.net";
		final int SERVER_PORT = 80;
		final String CONTEXT_PATH = "/context";
		final ResourceLocator RESOURCE_LOCATOR = this
				.createMock(ResourceLocator.class);
		final Logger LOGGER = this.createMock(Logger.class);

		// Create the server
		ServletServer server = new ServletServerImpl(SERVER_NAME, SERVER_PORT,
				CONTEXT_PATH, RESOURCE_LOCATOR, LOGGER);

		// Validate server
		assertEquals("Incorrect server name", SERVER_NAME, server
				.getServerName());
		assertEquals("Incorrect server port", SERVER_PORT, server
				.getServerPort());
		assertEquals("Incorrect context path", CONTEXT_PATH, server
				.getContextPath());
		assertEquals("Incorrect resource locator", RESOURCE_LOCATOR, server
				.getResourceLocator());
		assertEquals("Incorrect logger", LOGGER, server.getLogger());
	}

}