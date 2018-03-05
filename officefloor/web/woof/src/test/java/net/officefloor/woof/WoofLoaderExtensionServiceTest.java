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
package net.officefloor.woof;

import java.io.IOException;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link WoofLoaderExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderExtensionServiceTest extends OfficeFrameTestCase {

	/**
	 * {@link MockWoofServer}.
	 */
	private MockWoofServer server;

	@Override
	protected void tearDown() throws Exception {
		if (this.server != null) {
			this.server.close();
		}
	}

	/**
	 * Ensure can run the application from default configuration.
	 */
	public void testWoOF() throws Exception {

		// Start WoOF application for testing
		this.server = MockWoofServer.open();

		// Ensure WoOF configuration loaded
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/template"));
		response.assertResponse(200, "TEMPLATE");

		// Ensure Objects loaded
		response = this.server.send(MockWoofServer.mockRequest("/objects"));
		response.assertResponse(200, "OBJECT");

		// Ensure Resources loaded
		response = this.server.send(MockWoofServer.mockRequest("/resource"));
		response.assertResponse(200, "RESOURCE");

		// Ensure Teams loaded
		response = this.server.send(MockWoofServer.mockRequest("/teams"));
		response.assertResponse(200, "TEAMS");
	}

}