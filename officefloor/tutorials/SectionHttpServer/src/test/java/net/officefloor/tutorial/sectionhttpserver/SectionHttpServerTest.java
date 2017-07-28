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
package net.officefloor.tutorial.sectionhttpserver;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import junit.framework.TestCase;
import net.officefloor.OfficeFloorMain;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionHttpServerTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		// Start server
		OfficeFloorMain.open();
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop server
		OfficeFloorMain.close();
	}

	public void testPageRendering() throws Exception {

		try (CloseableHttpClient client = HttpTestUtil.createHttpClient()) {

			// Send request for dynamic page
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/example.woof"));

			// Ensure request is successful
			assertEquals("Request should be successful", 200, response.getStatusLine().getStatusCode());

			// Obtain the response
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			response.getEntity().writeTo(buffer);
			String responseText = new String(buffer.toByteArray());

			// Ensure correct response
			assertTrue("Missing template section", responseText.contains("<p>Hi</p>"));
			assertTrue("Missing Hello section", responseText.contains("<p>Hello</p>"));
			assertFalse("NotRender section should not be rendered", responseText.contains("<p>Not rendered</p>"));
			assertTrue("Missing NoBean section", responseText.contains("<p>How are you?</p>"));
		}
	}

}