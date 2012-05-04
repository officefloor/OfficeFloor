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
package net.officefloor.launch.woof;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.TreeLogger;

/**
 * Tests the {@link WoofServletContainerLauncher}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletContainerLauncherTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * Port listening for HTTP requests.
	 */
	private int port;

	/**
	 * Raw source files. Typically will be
	 * <code>[project]/src/main/webapp</code> directory containing web
	 * resources.
	 */
	private File webappDirectory;

	/**
	 * WAR files. Typically will be <code>[project]/target/gwt-war</code>
	 * containing GWT generated content. This allows a separate directory to
	 * keep raw source and generated content separate.
	 */
	private File warDirectory;

	/**
	 * {@link ServletContainer}.
	 */
	private ServletContainer servletContainer;

	@Override
	protected void setUp() throws Exception {

		// Specify the port
		this.port = MockHttpServer.getAvailablePort();

		// Obtain the temp location to create necessary directories
		File tempDir = new File(System.getProperty("java.io.tmpdir"), this
				.getClass().getSimpleName() + "-" + this.getName());
		if (tempDir.exists()) {
			this.deleteDirectory(tempDir);
		}
		assertTrue("Failed to setup test", tempDir.mkdir());

		// Create the webapp directory
		this.webappDirectory = new File(tempDir, "webapp");
		assertTrue("Failed to setup src directory",
				this.webappDirectory.mkdir());

		// Create file within webapp directory
		this.writeFile(this.webappDirectory, "source.html", "SOURCE");

		// Create the war target directory
		this.warDirectory = new File(tempDir, "war");
		assertTrue("Failed to setup target directory",
				this.warDirectory.mkdir());

		// Create file within target directory
		this.writeFile(this.warDirectory, "target.html", "TARGET");

		// Ensure class path resource available
		assertNotNull("Invalid testing as class path resource not available",
				Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("PUBLIC/classpath.html"));

		// Provide WoOF configuration for launcher
		WoofDevelopmentConfiguration configuration = new WoofDevelopmentConfiguration();
		configuration.addResourceDirectory(this.webappDirectory);
		configuration.storeConfiguration(new File(this.warDirectory,
				WoofServletContainerLauncher.CONFIGURATION_FILE_NAME));

		// Start WoOF
		WoofServletContainerLauncher launcher = new WoofServletContainerLauncher();
		this.servletContainer = launcher.start(new MockTreeLogger(false),
				this.port, this.warDirectory);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.getConnectionManager().shutdown();

		} finally {
			// Ensure also stop WoOF
			if (this.servletContainer != null) {
				this.servletContainer.stop();
			}
		}
	}

	/**
	 * Ensure {@link OfficeFloor} able to provide content.
	 */
	public void testOfficeFloorProvidingContent() throws Exception {

		// Ensure able to obtain the various files
		this.doHttpRequest("source.html", "SOURCE");
		this.doHttpRequest("target.html", "TARGET");
		this.doHttpRequest("classpath.html", "CLASSPATH");
	}

	/**
	 * Ensure {@link OfficeFloor} provides changed content.
	 */
	public void testOfficeFloorServingChangingContent() throws Exception {

		// Ensure correct content within target dirctory
		this.doHttpRequest("target.html", "TARGET");

		// Change the content of the resource
		this.writeFile(this.warDirectory, "target.html", "CHANGED");

		// Ensure received changed content
		this.doHttpRequest("target.html", "CHANGED");
	}

	/**
	 * Ensure {@link OfficeFloor} provides new content.
	 */
	public void testOfficeFloorServingNewContent() throws Exception {

		// Ensure can get existing content (ensure up and running)
		this.doHttpRequest("target.html", "TARGET");

		// Ensure resource not available
		HttpResponse response = this.client.execute(new HttpGet(
				"http://localhost:" + this.port + "/new.html"));
		assertEquals("Should not find resource", 404, response.getStatusLine()
				.getStatusCode());
		response.getEntity().consumeContent();

		// Provide new resource
		this.writeFile(this.warDirectory, "new.html", "NEW");

		// Ensure able to obtain new resource
		this.doHttpRequest("new.html", "NEW");
	}

	/**
	 * Ensure gracefully handles failure in starting.
	 */
	public void testFailureStarting() throws Exception {

		// Stop setup instance
		this.servletContainer.stop();

		// Create logger to receive failure to start
		MockTreeLogger logger = new MockTreeLogger(true);

		// Attempt to start new instance that has invalid configuration
		this.servletContainer = new WoofServletContainer(logger,
				MockHttpServer.getAvailablePort(), new File[0]) {
			@Override
			protected WebAutoWireApplication createWebAutoWireApplication() {

				// Create WoOF configured with invalid configuration
				WoofOfficeFloorSource source = new WoofOfficeFloorSource();
				source.getOfficeFloorCompiler()
						.addProperty(
								WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
								"fail.woof");

				// Return WoOF
				return source;
			}
		};

		// Validate the logging of failure to start
		String expectedMessage = "null null: Failed to source OfficeFloor from OfficeFloorSource (source="
				+ WoofOfficeFloorSource.class.getName()
				+ ", location=auto-wire)";
		assertEquals("Incorrect log message", expectedMessage, logger.message);
		assertEquals("Incorrect cause type", expectedMessage,
				logger.cause.getMessage());
	}

	/**
	 * Ensure able to refresh.
	 */
	public void testRefresh() throws Exception {

		// Refresh
		this.servletContainer.refresh();

		// Ensure able to obtain the various files
		this.doHttpRequest("source.html", "SOURCE");
		this.doHttpRequest("target.html", "TARGET");
		this.doHttpRequest("classpath.html", "CLASSPATH");
	}

	/**
	 * Undertakes the HTTP request.
	 * 
	 * @param resourceUri
	 *            URI of the resource.
	 * @param expectedResourceContent
	 *            Expected resource content.
	 */
	private void doHttpRequest(String resourceUri,
			String expectedResourceContent) throws IOException {

		// Do the request
		HttpResponse response = this.client.execute(new HttpGet(
				"http://localhost:" + this.port + "/" + resourceUri));

		// Validate the successful
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Validate correct content
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String responseText = new String(buffer.toByteArray());
		assertEquals("Incorrect resource content", expectedResourceContent,
				responseText);
	}

	/**
	 * Writes the {@link File} (either creating new or overwriting).
	 * 
	 * @param directory
	 *            Directory to contain the file.
	 * @param fileName
	 *            {@link File} name.
	 * @param fileContent
	 *            Content for {@link File}.
	 */
	private void writeFile(File directory, String fileName, String fileContent)
			throws IOException {
		Writer writer = new FileWriter(new File(directory, fileName));
		writer.append(fileContent);
		writer.close();
	}

	/**
	 * Mock {@link TreeLogger} for testing.
	 */
	private static class MockTreeLogger extends TreeLogger {

		/**
		 * Indicates if expecting logging within testing.
		 */
		private final boolean isExpectLogging;

		/**
		 * Message.
		 */
		public String message = null;

		/**
		 * Cause.
		 */
		public Throwable cause = null;

		/**
		 * Initiate.
		 * 
		 * @param isExpectLogging
		 *            Indicates if expecting logging within testing.
		 */
		public MockTreeLogger(boolean isExpectLogging) {
			this.isExpectLogging = isExpectLogging;
		}

		/*
		 * ===================== TreeLogger ==============================
		 */

		@Override
		public TreeLogger branch(Type type, String msg, Throwable caught,
				HelpInfo helpInfo) {
			fail("Should not require to branch");
			return this;
		}

		@Override
		public boolean isLoggable(Type type) {
			fail("Not expecting to check isLoggable");
			return true;
		}

		@Override
		public void log(Type type, String msg, Throwable caught,
				HelpInfo helpInfo) {

			// Ensure log
			if (!this.isExpectLogging) {
				fail("Not expecting to log");
			}

			// Log should always be for error
			assertEquals("Incorrect log type", Type.ERROR, type);

			// Ensure only the one log
			assertNull("Should only expect one log", this.message);

			// Provide details of log
			this.message = msg;
			this.cause = caught;
		}
	}

}