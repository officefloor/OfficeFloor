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

package net.officefloor.plugin.war.integrate;

import java.io.File;
import java.util.Properties;

import net.officefloor.building.console.OfficeFloorConsole;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Integration testing of running a WAR.
 * 
 * @author Daniel Sagenschneider
 */
public class WarIntegrateTest extends OfficeFrameTestCase {

	/**
	 * Ensure can start the WAR and have it service a {@link HttpRequest}.
	 */
	public void testWarStartAndService() throws Throwable {

		final int PORT = MockHttpServer.getAvailablePort();

		// Provide port to decorator
		System.setProperty("http.port", String.valueOf(PORT));

		// Obtain location of war directory
		File warDir = new File(".", "target/test-classes");
		assertTrue("Test invalid as WAR directory not available", warDir
				.isDirectory());

		// Open the OfficeBuilding
		OfficeFloorConsole console = new OfficeBuilding()
				.createOfficeFloorConsole(this.getName(), new Properties());
		console.run(System.out, System.err, null, null, "start");

		try {

			// Open the WAR by decoration of OfficeFloor
			console.run(System.out, System.err, null, null, "--jar", warDir
					.getAbsolutePath(), "open");

			// Request data from servlet
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet("http://localhost:" + PORT);
			HttpResponse response = client.execute(request);

			// Ensure valid response
			assertEquals("Response should be successful", 200, response
					.getStatusLine().getStatusCode());
			String body = MockHttpServer.getEntityBody(response);
			assertEquals("Incorrect response body", "WAR", body);

		} finally {
			// Ensure clean up system properties
			System.clearProperty("http.port");

			// Ensure stop the OfficeBuilding (and subsequently OfficeFloor)
			console.run(System.out, System.err, null, null, "stop");
		}
	}

}