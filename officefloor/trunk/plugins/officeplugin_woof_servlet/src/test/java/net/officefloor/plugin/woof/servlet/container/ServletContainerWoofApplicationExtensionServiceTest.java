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
package net.officefloor.plugin.woof.servlet.container;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.servlet.Servlet;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.plugin.woof.servlet.MockDependency;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Ensure able to embed the {@link Servlet} container within WoOF as a chained
 * servicer.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContainerWoofApplicationExtensionServiceTest extends
		OfficeFrameTestCase {

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * Port application running on.
	 */
	private int port;

	/**
	 * Ensure can service from WoOF.
	 */
	public void testServiceViaWoOF() throws Exception {
		String responseText = this.doGetEntity("/test");
		assertEquals(
				"Incorrect template content",
				"TEMPLATE TEST OnePersonTeam_"
						+ new AutoWire(MockDependency.class).getQualifiedType(),
				responseText);
	}

	/**
	 * Ensure able to service from the {@link Servlet} container.
	 */
	public void _testServiceFromServletContainer() throws Exception {
		String responseText = this.doGetEntity("/servlet");
		assertEquals("Incorrect servlet response", "HTTP_SERVLET", responseText);
	}

	/**
	 * Executes a {@link HttpGet} against the URI.
	 * 
	 * @param uri
	 *            URI for the {@link HttpGet} request.
	 * @return Entity of response as text.
	 */
	private String doGetEntity(String uri) throws Exception {

		// Ensure serviced by HTTP template from WoOF configuration
		HttpGet request = new HttpGet("http://localhost:" + this.port + uri);
		HttpResponse response = this.client.execute(request);
		assertEquals("Must be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Obtain the response entity as text
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String responseText = new String(buffer.toByteArray());

		// Return the response entity
		return responseText;
	}

	@Override
	protected void setUp() throws Exception {

		// Configure the location of the web app directory
		File webAppDir = new File(".", "src/test/webapp");
		System.setProperty(WoofOfficeFloorSource.PROPERTY_WEBAPP_LOCATION,
				webAppDir.getAbsolutePath());

		// Obtain the port for the application
		this.port = MockHttpServer.getAvailablePort();

		// Start WoOF (should load servlet container as servicer)
		WoofOfficeFloorSource.start(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
				String.valueOf(this.port));
	}

	@Override
	protected void tearDown() throws Exception {

		// Clear the web app location
		System.clearProperty(WoofOfficeFloorSource.PROPERTY_WEBAPP_LOCATION);

		try {
			// Stop the client
			this.client.getConnectionManager().shutdown();

		} finally {
			// Stop the server
			WoofOfficeFloorSource.stop();
		}
	}

}