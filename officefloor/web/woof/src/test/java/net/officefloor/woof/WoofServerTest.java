/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpInput;

/**
 * Tests the WoOF server.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServerTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can invoke {@link HttpRequest} on the WoOF server.
	 */
	public void testWoofServerDefaultPorts() throws IOException {

		// Open the OfficeFloor (on default ports)
		this.officeFloor = WoOF.open();

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/template"));
			assertEquals("Incorrect template", "TEMPLATE", HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Ensure can invoke {@link HttpRequest} on the WoOF server.
	 */
	public void testWoofServerNonDefaultPorts() throws IOException {

		// Open the OfficeFloor (on non-default ports)
		this.officeFloor = WoOF.open(8787, 9797);

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:8787/template"));
			assertEquals("Incorrect template", "TEMPLATE", HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Ensure can override context.
	 */
	public void testContextualOverload() throws IOException {

		// Run within context (without WoOF loads)
		this.officeFloor = WoofLoaderExtensionService.contextualLoad((context) -> {

			// Don't load WoOF
			context.notLoadWoof();

			// Register handling
			context.extend((woofContext) -> {
				OfficeArchitect office = woofContext.getOfficeArchitect();

				// Add the section
				OfficeSection section = office.addOfficeSection("SECTION", ClassSectionSource.class.getName(),
						ContextualOverloadSection.class.getName());

				// Configure to service request
				HttpInput input = woofContext.getWebArchitect().getHttpInput(false, "GET", "/extended");
				office.link(input.getInput(), section.getOfficeSectionInput("service"));
			});

			// Start the OfficeFloor
			return WoOF.open();
		});

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure WoOF template not loaded
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/template"));
			assertEquals("Should not be found", 404, response.getStatusLine().getStatusCode());

			// Ensure can obtain configured in service
			response = client.execute(new HttpGet("http://localhost:7878/extended"));
			assertEquals("Should obtain value", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect entity", "OVERRIDE", HttpClientTestUtil.entityToString(response));
		}
	}

	public static class ContextualOverloadSection {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("OVERRIDE");
		}
	}

}