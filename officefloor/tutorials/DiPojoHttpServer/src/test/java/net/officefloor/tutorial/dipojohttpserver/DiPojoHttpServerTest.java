/*-
 * #%L
 * Dependency Injection of a POJO HTTP Server Tutorial
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

package net.officefloor.tutorial.dipojohttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Ensure correctly renders the page.
 * 
 * @author Daniel Sagenschneider
 */
public class DiPojoHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: test
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Test
	public void ensureRenderPage() throws Exception {

		// Obtain the page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/template"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure page contains correct rendered content
		String page = response.getEntity(null);
		assertTrue("Ensure correct page content", page.contains("Hello World"));
	}
	// END SNIPPET: test

}
