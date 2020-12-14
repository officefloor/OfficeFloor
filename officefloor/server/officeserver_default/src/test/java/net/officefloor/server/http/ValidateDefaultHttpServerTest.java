/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Validate that the {@link HttpServer} is correctly using
 * {@link OfficeFloorHttpServerImplementation} as the default
 * {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateDefaultHttpServerTest {

	/**
	 * Ensure correct default {@link HttpServerImplementation}.
	 */
	@Test
	public void defaultHttpServerImplementation() {
		assertEquals(OfficeFloorHttpServerImplementation.class.getName(),
				HttpServer.DEFAULT_HTTP_SERVER_IMPLEMENTATION_CLASS_NAME,
				"Incorrect default HTTP server implementation");
	}

}
