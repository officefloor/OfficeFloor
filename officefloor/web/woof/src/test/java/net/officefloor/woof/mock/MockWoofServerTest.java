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
package net.officefloor.woof.mock;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.woof.WoofLoaderExtensionService;

/**
 * Tests the {@link WoofLoaderExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerTest extends OfficeFrameTestCase {

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
	@SuppressWarnings("unused")
	public void testWoOF() throws Exception {

		// Start WoOF application for testing
		this.server = MockWoofServer.open();

		// Ensure WoOF configuration loaded
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/template"));
		response.assertResponse(200, "TEMPLATE");

		// FIXME
		if (true) {
			System.err.println("TODO implement /objects /resources /teams to ensure WoOF loads appropriately");
			return;
		}

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

	/**
	 * Ensure can handle multiple requests.
	 */
	public void testMultipleRequests() throws Exception {

		// Start WoOF application for testing
		this.server = MockWoofServer.open();

		// Run multiple requests ensuring appropriately handles
		MockHttpResponse response = null;
		for (int i = 0; i < 100; i++) {

			// Undertake request
			MockHttpRequestBuilder request = MockWoofServer.mockRequest("/path?param=" + i);
			if (response != null) {
				request.cookies(response);
			}
			response = this.server.send(request);
			response.assertResponse(200, "param=" + i + ", previous=" + (i - 1));
		}
	}

}