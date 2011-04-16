/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.woof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.plugin.autowire.AutoWireAdministration;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link WoofMain}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofMainTest extends OfficeFrameTestCase {

	@Override
	protected void tearDown() throws Exception {
		AutoWireAdministration.closeAllOfficeFloors();
	}

	/**
	 * Ensure can load and run {@link WoofModel}.
	 */
	public void testLoadsAndRuns() throws Exception {

		// Run the application
		WoofMain.main(new String[0]);

		// Test
		this.doTestRequest("/test");
	}

	/**
	 * Ensure can provide additional configuration.
	 */
	public void testAdditionalConfiguration() throws Exception {

		// Run the additionally configured application
		MockWoofMain.main(new String[0]);

		// Test
		this.doTestRequest("/another");
	}

	/**
	 * Provides additional configuration to {@link WoofMain} for testing.
	 */
	private static class MockWoofMain extends WoofMain {

		/**
		 * Main for running.
		 * 
		 * @param args
		 *            Command line arguments.
		 */
		public static void main(String[] args) throws Exception {
			run(new MockWoofMain());
		}

		/*
		 * =================== WoofMain ==========================
		 */

		@Override
		protected void configure(HttpServerAutoWireOfficeFloorSource application) {
			AutoWireSection section = application.addSection("ANOTHER",
					ClassSectionSource.class, Section.class.getName());
			application.linkUri("another", section, "service");
		}
	}

	/**
	 * Sends request to test {@link WoofMain} running.
	 * 
	 * @param uri
	 *            URI.
	 */
	private void doTestRequest(String uri) throws Exception {

		// Ensure is configured by responding to request
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://localhost:7878" + uri);
		HttpResponse response = client.execute(request);
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		assertEquals("Incorrect response body", "TEST",
				new String(buffer.toByteArray()));
	}

	/**
	 * Class for {@link ClassSectionSource} in testing.
	 */
	public static class Section {
		public void service(ServerHttpConnection connection) throws IOException {
			net.officefloor.plugin.socket.server.http.HttpResponse response = connection
					.getHttpResponse();
			response.getBody().getOutputStream().write("TEST".getBytes());
		}
	}

}