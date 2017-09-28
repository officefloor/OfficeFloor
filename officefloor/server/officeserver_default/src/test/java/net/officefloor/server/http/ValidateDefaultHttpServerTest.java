/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Validate that the {@link HttpServer} is correctly using
 * {@link OfficeFloorHttpServerImplementation} as the default
 * {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateDefaultHttpServerTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct default {@link HttpServerImplementation}.
	 */
	public void testDefaultHttpServerImplementation() {
		assertEquals("Incorrect default HTTP server implementation",
				OfficeFloorHttpServerImplementation.class.getName(),
				HttpServer.DEFAULT_HTTP_SERVER_IMPLEMENTATION_CLASS_NAME);
	}

}