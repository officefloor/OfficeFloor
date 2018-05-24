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
package net.officefloor.plugin.woof.servlet.container;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.LogRecord;

import javax.servlet.Servlet;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.test.LoggerAssertion;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.woof.WoofApplicationExtensionService;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.plugin.woof.servlet.MockDependency;
import net.officefloor.plugin.woof.servlet.ServletContainerWoofApplicationExtensionService;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Ensure able to embed the {@link Servlet} container within WoOF as a chained
 * servicer.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContainerWoofApplicationExtensionServiceTest extends
		OfficeFrameTestCase {

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	/**
	 * Port application running on.
	 */
	private int port;

	/**
	 * Ensure can service from WoOF.
	 */
	public void testServiceViaWoOF() throws Exception {
		this.startServer(null, null);
		String responseText = this.doGetEntity("/test.woof");
		assertEquals(
				"Incorrect template content",
				"TEMPLATE TEST OnePersonTeam_"
						+ new AutoWire(MockDependency.class).getQualifiedType(),
				responseText);
	}

	/**
	 * Ensure able to service from the {@link Servlet} container.
	 */
	public void testServiceFromServletContainer() throws Exception {
		this.startServer(null, null);
		String responseText = this.doGetEntity("/servlet.html");
		assertEquals("Incorrect servlet response", "HTTP_SERVLET", responseText);
	}

	/**
	 * Ensure {@link Servlet} container is not loaded for marker
	 * <code>web.xml</code>.
	 */
	public void testNotLoadServletContainerForMarkerFile() throws Exception {

		// Ensure log invalid web.xml
		LoggerAssertion log = LoggerAssertion
				.setupLoggerAssertion(ServletContainerWoofApplicationExtensionService.class
						.getName());
		try {

			// Test
			this.startServer("src/test/invalid-webapp",
					"invalid-application.woof");
			String responseText = this.doGetEntity("/servlet.html");
			assertEquals(
					"Should be resource as servlet container not loaded for marker web.xml",
					"NOT HTTP_SERVLET", responseText);

		} finally {
			// Validate log records
			LogRecord[] records = log.disconnectFromLogger();
			assertEquals("Incorrect number of log reords", 1, records.length);
			LogRecord record = records[0];
			String cause = "Invalid web.xml configuration [XmlMarshallException]: Content is not allowed in prolog.";
			assertEquals("Incorrect log message",
					"Invalid WEB-INF/web.xml so not loading Servlet servicers: "
							+ cause, record.getMessage());
			assertNull("Should not be cause as contained in message",
					record.getThrown());
		}
	}

	/**
	 * Ensure able to service from {@link WoofApplicationExtensionService}.
	 */
	public void testServiceFromExtension() throws Exception {
		this.startServer(null, null);
		String responseText = this.doGetEntity("/chain.html");
		assertEquals("Incorrect chained response", "CHAINED", responseText);
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

	/**
	 * Starts the server.
	 * 
	 * @param webAppPath
	 *            Web app path relative from project directory. May be
	 *            <code>null</code> to use test webapp directory.
	 * @param applicationWoofPath
	 *            Path to the <code>application.woof</code> relative to the
	 *            project directory. May be <code>null</code> to use default
	 *            location.
	 */
	private void startServer(String webAppPath, String applicationWoofPath)
			throws Exception {

		// Configure the location of the web app directory
		File webAppDir = new File(".", (webAppPath == null ? "src/test/webapp"
				: webAppPath));
		System.setProperty(WoofOfficeFloorSource.PROPERTY_WEBAPP_LOCATION,
				webAppDir.getAbsolutePath());

		// Obtain the port for the application
		this.port = HttpTestUtil.getAvailablePort();

		// Start WoOF (should load servlet container as servicer)
		WoofOfficeFloorSource
				.start(WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
						(applicationWoofPath == null ? WoofOfficeFloorSource.DEFAULT_WOOF_CONFIGUARTION_LOCATION
								: applicationWoofPath),
						HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
						String.valueOf(this.port), "INIT_NAME", "INIT_VALUE");
	}

	@Override
	protected void tearDown() throws Exception {

		// Clear the web app location
		System.clearProperty(WoofOfficeFloorSource.PROPERTY_WEBAPP_LOCATION);

		try {
			// Stop the client
			this.client.close();

		} finally {
			// Stop the server
			WoofOfficeFloorSource.stop();
		}
	}

}