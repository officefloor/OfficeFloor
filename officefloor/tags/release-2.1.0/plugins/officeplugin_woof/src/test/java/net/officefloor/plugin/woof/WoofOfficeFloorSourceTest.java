/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link WoofOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofOfficeFloorSourceTest extends OfficeFrameTestCase {

	@Override
	protected void setUp() throws Exception {
		WoofLoaderTest.ignoreExtensionServiceFailures();
	}

	@Override
	protected void tearDown() throws Exception {
		AutoWireManagement.closeAllOfficeFloors();
	}

	/**
	 * Ensure can load and run {@link WoofModel}.
	 */
	public void testLoadsAndRuns() throws Exception {

		// Run the application
		WoofOfficeFloorSource.main(new String[0]);

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
	 * Provides additional configuration to {@link WoofOfficeFloorSource} for
	 * testing.
	 */
	private static class MockWoofMain extends WoofOfficeFloorSource {

		/**
		 * Main for running.
		 * 
		 * @param args
		 *            Command line arguments.
		 */
		public static void main(String... args) throws Exception {
			run(new MockWoofMain());
		}

		/*
		 * =================== WoofMain ==========================
		 */

		@Override
		protected void configure(HttpServerAutoWireOfficeFloorSource application) {
			AutoWireSection section = application
					.addSection("ANOTHER", ClassSectionSource.class.getName(),
							Section.class.getName());
			application.linkUri("another", section, "service");
		}
	}

	/**
	 * Sends request to test {@link WoofOfficeFloorSource} running.
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
		assertEquals("Incorrect response body", "WOOF TEST OnePersonTeam_"
				+ new AutoWire(MockDependency.class).getQualifiedType(),
				new String(buffer.toByteArray()));
	}

	/**
	 * Class for {@link ClassSectionSource} in testing.
	 */
	public static class Section {
		public void service(ServerHttpConnection connection,
				MockDependency dependency) throws IOException {

			// Obtain content to validate objects and teams
			Thread thread = Thread.currentThread();
			String content = "WOOF " + dependency.getMessage() + " "
					+ thread.getName();

			// Write response
			net.officefloor.plugin.socket.server.http.HttpResponse response = connection
					.getHttpResponse();
			response.getBody().getOutputStream().write(content.getBytes());
		}
	}

}