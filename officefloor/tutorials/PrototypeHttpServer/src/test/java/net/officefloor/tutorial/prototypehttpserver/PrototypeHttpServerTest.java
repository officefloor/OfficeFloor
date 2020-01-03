/*-
 * #%L
 * Prototype HTTP Server Tutorial
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

package net.officefloor.tutorial.prototypehttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Prototype HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class PrototypeHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	/**
	 * {@link MockWoofServer}.
	 */
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	/**
	 * Ensure able to obtain end points.
	 */
	@Test
	public void ensurePrototypeEndPointsAvailable() {

		// Ensure able to obtain links
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/href"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure able to redirect to other template
		response = this.server.send(MockHttpServer.mockRequest("/href+link"));
		assertEquals("Should redirect", 303, response.getStatus().getStatusCode());
		assertEquals("Should redirect to form", "/form", response.getHeader("location").getValue());

		// Ensure able to obtain the form
		response = this.server.send(MockHttpServer.mockRequest("/form"));
		assertEquals("Should obtain form successfully", 200, response.getStatus().getStatusCode());

		// Ensure able to submit the form
		response = this.server.send(MockHttpServer.mockRequest("/form+handleSubmit").method(HttpMethod.POST));
		assertEquals("Should redirect on submit", 303, response.getStatus().getStatusCode());
		assertEquals("Should redirect to href", "/href", response.getHeader("location").getValue());
	}

}
