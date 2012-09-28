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
import java.io.File;
import java.io.IOException;
import java.util.logging.LogRecord;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.frame.spi.source.UnknownResourceError;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link WoofOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofOfficeFloorSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * {@link LoggerAssertion} for the {@link WoofLoader}.
	 */
	private LoggerAssertion loaderLoggerAssertion;

	/**
	 * {@link LoggerAssertion} for the {@link WoofOfficeFloorSource}.
	 */
	private LoggerAssertion sourceLoggerAssertion;

	@Override
	protected void setUp() throws Exception {

		// Provide chain servicer extension property
		System.setProperty("CHAIN.TEST", "VALUE");

		// Create the logger assertions
		this.loaderLoggerAssertion = LoggerAssertion
				.setupLoggerAssertion(WoofLoaderImpl.class.getName());
		this.sourceLoggerAssertion = LoggerAssertion
				.setupLoggerAssertion(WoofOfficeFloorSource.class.getName());
	}

	@Override
	protected void tearDown() throws Exception {

		// Clear property
		System.clearProperty("CHAIN.TEST");

		// Shutdown
		try {
			try {
				this.client.getConnectionManager().shutdown();
			} finally {
				WoofOfficeFloorSource.stop();
			}
		} finally {
			// Disconnect from loggers
			this.sourceLoggerAssertion.disconnectFromLogger();
			this.loaderLoggerAssertion.disconnectFromLogger();
		}
	}

	/**
	 * Ensure convenience constants are correctly specified.
	 */
	public void testConvenienceConstants() {
		assertEquals("Incorrect convenience HTTP port property name",
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
				WoofOfficeFloorSource.PROPERTY_HTTP_PORT);
	}

	/**
	 * Ensure can load and run {@link WoofModel}.
	 */
	public void testLoadsAndRuns() throws Exception {

		// Run the application
		WoofOfficeFloorSource.start();

		// Test
		this.doTestRequest("/test");
	}

	/**
	 * Ensure can run the application from default configuration.
	 */
	public void testDefaultApplicationConfiguration() throws Exception {

		// Run the application with default configuration
		WoofOfficeFloorSource.start();

		// Test
		this.doTestRequest("/test");
	}

	/**
	 * Ensure can extend the {@link WebAutoWireApplication}.
	 */
	public void testWebApplicationExtension() throws Exception {

		// Run the application (extended by MockWoofApplicationExtensionService)
		WoofOfficeFloorSource.start();

		// Validate log not loading unknown extension
		LogRecord[] records = this.sourceLoggerAssertion.getLogRecords();
		StringBuilder messages = new StringBuilder();
		for (LogRecord record : records) {
			messages.append("\t" + record.getMessage() + "\n");
		}
		assertEquals("Incorrect number of records:\n" + messages.toString(), 1,
				records.length);
		LogRecord record = records[0];
		assertEquals(
				"Incorrect unknown extension log record",
				WoofApplicationExtensionService.class.getName()
						+ ": Provider woof.application.extension.not.available.Service not found",
				record.getMessage());

		// Test that loaded servicer
		String response = this.doRequest("/chain");
		assertEquals("Should be serviced by chained servicer", "CHAINED",
				response);
	}

	/**
	 * Ensure unit test methods appropriately start and stop.
	 */
	public void testUnitTestMethods() throws Exception {

		// Should not successfully connect
		try {
			this.doRequest("/test");
			fail("Should not be successful");
		} catch (HttpHostConnectException ex) {
			assertEquals("Incorrect cause",
					"Connection to http://localhost:7878 refused",
					ex.getMessage());
		}

		// Run the application
		WoofOfficeFloorSource.start();

		// Should now be running
		this.doTestRequest("/test");

		// Stop the application
		WoofOfficeFloorSource.stop();

		// Should not successfully connect
		HttpClient refreshedClient = new DefaultHttpClient();
		try {
			refreshedClient.execute(new HttpGet("http://localhost:7878/test"));
			fail("Should not be successful");
		} catch (HttpHostConnectException ex) {
			assertEquals("Incorrect cause",
					"Connection to http://localhost:7878 refused",
					ex.getMessage());
		} finally {
			refreshedClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * Ensure can run the application with additional configuration.
	 */
	public void testAdditionalConfiguration() throws Exception {

		// Run the additionally configured application
		MockWoofMain.main();

		// Test
		this.doTestRequest("/another");
	}

	/**
	 * Ensure can render template.
	 */
	public void testTemplateNoLogic() throws Exception {

		// Obtain no logic template WoOF configuration
		String noLogicTemplateConfigurationLocation = this
				.getPackageRelativePath(this.getClass())
				+ "/NoLogicTemplate.woof";

		// Run the application with no logic template
		WoofOfficeFloorSource.start(
				WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				noLogicTemplateConfigurationLocation);

		// Test
		String response = this.doRequest("/template");
		assertEquals("Incorrect response", "NO LOGIC TEMPLATE", response);
	}

	/**
	 * Ensure can provide command line configuration.
	 */
	public void testCommandLineConfiguration() throws Exception {

		// Obtain alternate configuration location
		String alternateConfigurationLocation = this
				.getPackageRelativePath(this.getClass()) + "/commandline.woof";

		// Run the application with properties for alternate configuration
		WoofOfficeFloorSource.main(
				WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation);

		// Test
		this.doTestRequest("/commandline");
	}

	/**
	 * Ensure can provide command line configuration with property name of the
	 * form <code>-name</code>.
	 */
	public void testCommandLineConfigurationWithPropertyNameHyphenPrefix()
			throws Exception {

		// Obtain alternate configuration location
		String alternateConfigurationLocation = this
				.getPackageRelativePath(this.getClass()) + "/commandline.woof";

		// Run the application with properties for alternate configuration
		WoofOfficeFloorSource.main("-"
				+ WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation,
				"only.property.name.to.have.blank.value");

		// Test
		this.doTestRequest("/commandline");
	}

	/**
	 * Ensure can retrieve <code>*.woof.html</code> files from the
	 * <code>src/main/webapp</code> directory for running within unit tests for
	 * maven.
	 */
	public void testSrcMainWebappWoofResources() throws Exception {

		// Obtain webapp configuration location
		String alternateConfigurationLocation = this
				.getPackageRelativePath(this.getClass()) + "/webapp.woof";

		// Run the application for woof resource
		WoofOfficeFloorSource.main("-"
				+ WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation);

		// Test
		String response = this.doRequest("/woofResource");
		assertEquals(
				"Incorrect response",
				"This is only for testing and should not be included in built jar for project",
				response);
	}

	/**
	 * Ensure can retrieve configured resource from the
	 * <code>src/main/webapp</code> directory for running within unit tests for
	 * maven.
	 */
	public void testSrcMainWebappConfiguredResource() throws Exception {

		// Obtain webapp configuration location
		String alternateConfigurationLocation = this
				.getPackageRelativePath(this.getClass()) + "/webapp.woof";

		// Run the application for woof resource
		WoofOfficeFloorSource.main("-"
				+ WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation);

		// Test
		String response = this.doRequest("/resource");
		assertEquals(
				"Incorrect response",
				"Configured resource for testing that should not be included in built jar for project",
				response);
	}

	/**
	 * Ensure can retrieve static files from the <code>src/main/webapp</code>
	 * directory for running within unit tests for maven.
	 */
	public void testSrcMainWebappStaticResources() throws Exception {

		// Obtain webapp configuration location
		String alternateConfigurationLocation = this
				.getPackageRelativePath(this.getClass()) + "/webapp.woof";

		// Run the application for woof resource
		WoofOfficeFloorSource.main("-"
				+ WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation);

		// Test
		String response = this.doRequest("/NonWoof.html");
		assertEquals(
				"Incorrect response",
				"Not a WoOF resource.  Also should not be included in resulting built jar.",
				response);
	}

	/**
	 * Ensure only retrieve <code>*.woof.html</code> files from the
	 * <code>src/main/webapp</code> directory.
	 */
	public void testSrcMainWebappIgnoreOtherResources() throws Exception {

		// Ensure the resource is available
		File nonWoofResource = new File(".", "src/main/webapp/NonWoof.html");
		assertTrue("Invalid test as non-WoOF resource not exists",
				(nonWoofResource.exists() && nonWoofResource.isFile()));

		// Obtain webapp configuration location
		String alternateConfigurationLocation = this
				.getPackageRelativePath(this.getClass()) + "/webappOther.woof";

		try {
			// Run the application for woof resource
			WoofOfficeFloorSource
					.main("-"
							+ WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
							alternateConfigurationLocation);
			fail("Should not successfully find resource and therefore should not start");

		} catch (FailCompilerIssues ex) {
			// Should not start due to unknown resource
			Throwable cause = ex.getCause();
			assertTrue("Incorrect cause",
					(cause instanceof UnknownResourceError));
			assertEquals("Incorrect reason", "Unknown resource 'NonWoof.html'",
					cause.getMessage());
		}
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
			MockWoofMain main = new MockWoofMain();
			main.getOfficeFloorCompiler().addSystemProperties();
			run(main);
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

		// Undertake the request
		String response = this.doRequest(uri);

		// Ensure is configured correctly by correct response
		assertEquals("Incorrect response body", "WOOF TEST OnePersonTeam_"
				+ new AutoWire(MockDependency.class).getQualifiedType(),
				response);
	}

	/**
	 * Does the request to test the running {@link WoofOfficeFloorSource}.
	 * 
	 * @param uri
	 *            URI.
	 * @return Response entity for request.
	 */
	private String doRequest(String uri) throws Exception {
		HttpGet request = new HttpGet("http://localhost:7878" + uri);
		HttpResponse response = this.client.execute(request);
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		return new String(buffer.toByteArray());
	}

	/**
	 * Class for {@link ClassSectionSource} in testing.
	 */
	public static class LinkToResource {
		@NextTask("resource")
		public void service() {
		}
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
			response.getEntity().write(content.getBytes());
		}
	}

}