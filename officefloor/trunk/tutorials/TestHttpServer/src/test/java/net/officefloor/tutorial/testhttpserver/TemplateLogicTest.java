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
package net.officefloor.tutorial.testhttpserver;

import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.tutorial.testhttpserver.TemplateLogic.Parameters;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogicTest extends Assert {

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
	@Test
	public void systemTest() throws Exception {

		// Start the application
		WoofOfficeFloorSource.start();

		try (CloseableHttpClient client = HttpTestUtil.createHttpClient()) {

			// Send request to add
			HttpGet request = new HttpGet(
					"http://localhost:7878/template-add.woof?a=1&b=2");
			HttpResponse response = client.execute(request);

			// Ensure added the values
			String entity = EntityUtils.toString(response.getEntity());
			assertTrue("Should have added the values", entity.contains("= 3"));

		} finally {
			// Stop the application
			WoofOfficeFloorSource.stop();
		}
	}
	// END SNIPPET: system

}