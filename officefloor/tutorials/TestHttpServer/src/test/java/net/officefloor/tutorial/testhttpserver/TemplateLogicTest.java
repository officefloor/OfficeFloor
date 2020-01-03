/*-
 * #%L
 * Test HTTP Server Tutorial
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

package net.officefloor.tutorial.testhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.OfficeFloorRule;
import net.officefloor.tutorial.testhttpserver.TemplateLogic.Parameters;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogicTest {

	/**
	 * Main to run for manual testing.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: unit
	@Test
	public void unitTest() {

		// Load the parameters
		Parameters parameters = new Parameters();
		parameters.setA("1");
		parameters.setB("2");
		assertNull("Shoud not have result", parameters.getResult());

		// Test
		TemplateLogic logic = new TemplateLogic();
		logic.add(parameters);
		assertEquals("Incorrect result", "3", parameters.getResult());
	}

	// END SNIPPET: unit

	// START SNIPPET: system
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void systemTest() throws Exception {

		// Send request to add
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/template+add?a=1&b=2").method(HttpMethod.POST));
		assertEquals("Should follow POST/GET pattern", 303, response.getStatus().getStatusCode());
		String redirect = response.getHeader("location").getValue();

		// Obtain the result
		response = server.send(MockHttpServer.mockRequest(redirect).cookies(response));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure added the values
		String entity = response.getEntity(null);
		assertTrue("Should have added the values", entity.contains("= 3"));
	}
	// END SNIPPET: system

	// START SNIPPET: full-system
	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void callingSystemTest() throws Exception {

		// Send request to add
		HttpResponse response = this.client.execute(new HttpPost(this.client.url("/template+add?a=1&b=2")));
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());

		// Ensure added the values
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue("Should have added the values: " + entity, entity.contains("= 3"));
	}
	// END SNIPPET: full-system

}
