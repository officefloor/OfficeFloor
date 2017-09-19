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
package net.officefloor.plugin.woof;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.LogRecord;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.OfficeFloorMain;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.LoggerAssertion;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Tests the {@link WoofLoaderExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderExtensionServiceTest extends OfficeFrameTestCase {

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpClientTestUtil.createHttpClient();

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Flags to close the {@link OfficeFloor} via {@link OfficeFloorMBean}.
	 */
	private boolean isCloseOfficeFloorViaMbeans = false;

	/**
	 * {@link LoggerAssertion} for the {@link WoofLoader}.
	 */
	private LoggerAssertion loaderLoggerAssertion;

	/**
	 * {@link LoggerAssertion} for the {@link WoofLoaderExtensionService}.
	 */
	private LoggerAssertion sourceLoggerAssertion;

	@Override
	protected void setUp() throws Exception {

		// Provide chain servicer extension property
		System.setProperty("CHAIN.TEST", "VALUE");

		// Create the logger assertions
		this.loaderLoggerAssertion = LoggerAssertion.setupLoggerAssertion(WoofLoaderImpl.class.getName());
		this.sourceLoggerAssertion = LoggerAssertion.setupLoggerAssertion(WoofLoaderExtensionService.class.getName());

		// Clear implicit template extension
		MockImplicitWoofTemplateExtensionSourceService.reset();
	}

	@Override
	protected void tearDown() throws Exception {

		// Clear property
		System.clearProperty("CHAIN.TEST");

		// Shutdown
		try {
			try {
				this.client.close();
			} finally {
				if (this.officeFloor != null) {
					this.officeFloor.closeOfficeFloor();
				} else if (this.isCloseOfficeFloorViaMbeans) {
					fail("TODO implement closing OfficeFloor via MBeans");
				}
			}
		} finally {
			// Disconnect from loggers
			this.sourceLoggerAssertion.disconnectFromLogger();
			this.loaderLoggerAssertion.disconnectFromLogger();
		}
	}

	/**
	 * Ensure can run the application from default configuration.
	 */
	public void testDefaultApplicationConfiguration() throws Exception {

		// Run the application with default configuration
		this.openOfficeFloor();

		// Test
		this.doTestRequest("/test");
	}

	/**
	 * Ensure can extend the {@link WebArchitect}.
	 */
	public void testWebApplicationExtension() throws Exception {

		// Run the application (extended by MockWoofApplicationExtensionService)
		this.openOfficeFloor();

		// Validate log not loading unknown extension
		LogRecord[] records = this.sourceLoggerAssertion.getLogRecords();
		StringBuilder messages = new StringBuilder();
		for (LogRecord record : records) {
			messages.append("\t" + record.getMessage() + "\n");
		}
		assertEquals("Incorrect number of records:\n" + messages.toString(), 1, records.length);
		LogRecord record = records[0];
		assertEquals("Incorrect unknown extension log record", WoofLoaderExtensionService.class.getName()
				+ ": Provider woof.application.extension.not.available.Service not found", record.getMessage());

		// Test that loaded servicer
		String response = this.doRequest("/chain");
		assertEquals("Should be serviced by chained servicer", "CHAINED", response);
	}

	/**
	 * Ensure unit test methods appropriately start and stop.
	 */
	public void testUnitTestMethods() throws Exception {

		final String connectionMessageSuffix = "failed: Connection refused";

		// Should not successfully connect
		try {
			this.doRequest("/test");
			fail("Should not be successful");
		} catch (HttpHostConnectException ex) {
			assertTrue("Incorrect cause: " + ex.getMessage(), ex.getMessage().endsWith(connectionMessageSuffix));
		}

		// Run the application
		this.openOfficeFloor();

		// Should now be running
		this.doTestRequest("/test");

		// Stop the application
		this.officeFloor.closeOfficeFloor();
		this.officeFloor = null;

		// Should not successfully connect
		CloseableHttpClient refreshedClient = HttpClientTestUtil.createHttpClient();
		try {
			refreshedClient.execute(new HttpGet("http://localhost:7878/test"));
			fail("Should not be successful");
		} catch (HttpHostConnectException ex) {
			assertTrue("Incorrect cause: " + ex.getMessage(), ex.getMessage().endsWith(connectionMessageSuffix));
		} finally {
			refreshedClient.close();
		}
	}

	/**
	 * Ensure can service template.
	 */
	public void testTemplate() throws Exception {

		// Configure implicit template extension
		MockImplicitWoofTemplateExtensionSourceService.reset("/template");

		// Obtain no logic template WoOF configuration
		String noLogicTemplateConfigurationLocation = this.getPackageRelativePath(this.getClass())
				+ "/NoLogicTemplate.woof";

		// Run the application with no logic template
		this.openOfficeFloor(WoofLoaderExtensionService.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				noLogicTemplateConfigurationLocation);

		// Test
		String response = this.doRequest("/template.woof");
		assertEquals("Incorrect response", "NO LOGIC TEMPLATE", response);
	}

	/**
	 * Ensure can provide command line configuration.
	 */
	public void testCommandLineConfiguration() throws Exception {

		// Obtain alternate configuration location
		String alternateConfigurationLocation = this.getPackageRelativePath(this.getClass()) + "/commandline.woof";

		// Run the application with properties for alternate configuration
		OfficeFloorMain.open(WoofLoaderExtensionService.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation);

		// Test
		this.doTestRequest("/commandline");

		// Stop the OfficeFloor
		fail("TODO stop the OfficeFloor via MBeans");
	}

	/**
	 * Ensure can provide command line configuration with property name of the
	 * form <code>-name</code>.
	 */
	public void testCommandLineConfigurationWithPropertyNameHyphenPrefix() throws Exception {

		// Obtain alternate configuration location
		String alternateConfigurationLocation = this.getPackageRelativePath(this.getClass()) + "/commandline.woof";

		// Run the application with properties for alternate configuration
		this.isCloseOfficeFloorViaMbeans = true;
		OfficeFloorMain.open("-" + WoofLoaderExtensionService.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation, "only.property.name.to.have.blank.value");

		// Test
		this.doTestRequest("/commandline");
	}

	/**
	 * Ensure can retrieve <code>*.woof.html</code> files from the
	 * <code>src/main/webapp</code> directory for running within unit tests for
	 * maven.
	 */
	public void testSrcMainWebappWoofResources() throws Exception {

		// Configure implicit template extension
		MockImplicitWoofTemplateExtensionSourceService.reset("/woofResource");

		// Obtain webapp configuration location
		String alternateConfigurationLocation = this.getPackageRelativePath(this.getClass()) + "/webapp.woof";

		// Run the application for woof resource
		this.isCloseOfficeFloorViaMbeans = true;
		OfficeFloorMain.open("-" + WoofLoaderExtensionService.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation);

		// Test
		String response = this.doRequest("/woofResource.woof");
		assertEquals("Incorrect response",
				"This is only for testing and should not be included in built jar for project", response);
	}

	/**
	 * Ensure can retrieve configured resource from the
	 * <code>src/main/webapp</code> directory for running within unit tests for
	 * maven.
	 */
	public void testSrcMainWebappConfiguredResource() throws Exception {

		// Configure implicit template extension
		MockImplicitWoofTemplateExtensionSourceService.reset("/woofResource");

		// Obtain webapp configuration location
		String alternateConfigurationLocation = this.getPackageRelativePath(this.getClass()) + "/webapp.woof";

		// Run the application for woof resource
		this.isCloseOfficeFloorViaMbeans = true;
		OfficeFloorMain.open("-" + WoofLoaderExtensionService.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation);

		// Test
		String response = this.doRequest("/resource");
		assertEquals("Incorrect response",
				"Configured resource for testing that should not be included in built jar for project", response);
	}

	/**
	 * Ensure can retrieve static files from the <code>src/main/webapp</code>
	 * directory for running within unit tests for maven.
	 */
	public void testSrcMainWebappStaticResources() throws Exception {

		// Configure implicit template extension
		MockImplicitWoofTemplateExtensionSourceService.reset("/woofResource");

		// Obtain webapp configuration location
		String alternateConfigurationLocation = this.getPackageRelativePath(this.getClass()) + "/webapp.woof";

		// Run the application for woof resource
		this.isCloseOfficeFloorViaMbeans = true;
		OfficeFloorMain.open("-" + WoofLoaderExtensionService.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				alternateConfigurationLocation);

		// Test
		String response = this.doRequest("/NonWoof.html");
		assertEquals("Incorrect response", "Not a WoOF resource.  Also should not be included in resulting built jar.",
				response);
	}

	/**
	 * Ensure only retrieve <code>*.woof.html</code> files from the
	 * <code>src/main/webapp</code> directory.
	 */
	public void testSrcMainWebappIgnoreOtherResources() throws Exception {

		// Configure implicit template extension
		MockImplicitWoofTemplateExtensionSourceService.reset("/not/found/resource");

		// Ensure the resource is available
		File nonWoofResource = new File(".", "src/main/webapp/NonWoof.html");
		assertTrue("Invalid test as non-WoOF resource not exists",
				(nonWoofResource.exists() && nonWoofResource.isFile()));

		// Obtain webapp configuration location
		String alternateConfigurationLocation = this.getPackageRelativePath(this.getClass()) + "/webappOther.woof";

		try {
			// Run the application for woof resource
			this.isCloseOfficeFloorViaMbeans = true;
			OfficeFloorMain.open("-" + WoofLoaderExtensionService.PROPERTY_WOOF_CONFIGURATION_LOCATION,
					alternateConfigurationLocation);
			fail("Should not successfully find resource and therefore should not start");

		} catch (Error ex) {
			CompileException exception = (CompileException) ex.getCause();

			// Should not start due to unknown resource
			assertEquals("Should not load office",
					"Failure loading OfficeType from source " + OfficeSource.class.getName(), ex.getMessage());
			DefaultCompilerIssue issue = exception.getCompilerIssue();
			DefaultCompilerIssue officeSectionLoadIssue = (DefaultCompilerIssue) issue.getCauses()[0];
			DefaultCompilerIssue sectionLoadIssue = (DefaultCompilerIssue) officeSectionLoadIssue.getCauses()[0];
			DefaultCompilerIssue missingResourceIssue = (DefaultCompilerIssue) sectionLoadIssue.getCauses()[0];
			assertEquals("Incorrect cause", "Can not obtain resource at location 'NonWoof.html' for SectionSource "
					+ HttpTemplateSectionSource.class.getName(), missingResourceIssue.getIssueDescription());
		}
	}

	/**
	 * Opens the {@link OfficeFloor} with the configured {@link PropertyList}.
	 * 
	 * @param propertyNameValuePairs
	 *            {@link PropertyList} name/value pairs.
	 */
	private void openOfficeFloor(String... propertyNameValuePairs) throws Exception {
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			compiler.addProperty(propertyNameValuePairs[i], propertyNameValuePairs[i + 1]);
		}
		this.officeFloor = compiler.compile("OfficeFloor");
		this.officeFloor.openOfficeFloor();
	}

	/**
	 * Sends request to test {@link WoofLoaderExtensionService} running.
	 * 
	 * @param uri
	 *            URI.
	 */
	private void doTestRequest(String uri) throws Exception {

		// Undertake the request
		String response = this.doRequest(uri);

		// Ensure is configured correctly by correct response
		assertEquals("Incorrect response body",
				"WOOF TEST OnePersonTeam_" + new AutoWire(MockDependency.class).toString(), response);
	}

	/**
	 * Does the request to test the running {@link WoofLoaderExtensionService}.
	 * 
	 * @param uri
	 *            URI.
	 * @return Response entity for request.
	 */
	private String doRequest(String uri) throws Exception {
		HttpGet request = new HttpGet("http://localhost:7878" + uri);
		HttpResponse response = this.client.execute(request);
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		return new String(buffer.toByteArray());
	}

	/**
	 * Class for {@link ClassSectionSource} in testing.
	 */
	public static class LinkToResource {
		@NextFunction("resource")
		public void service() {
		}
	}

	/**
	 * Class for {@link ClassSectionSource} in testing.
	 */
	public static class Section {
		public void service(ServerHttpConnection connection, MockDependency dependency) throws IOException {

			// Obtain content to validate objects and teams
			Thread thread = Thread.currentThread();
			String content = "WOOF " + dependency.getMessage() + " " + thread.getName();

			// Write response
			net.officefloor.server.http.HttpResponse response = connection.getHttpResponse();
			response.getEntity().write(content.getBytes());
		}
	}

}