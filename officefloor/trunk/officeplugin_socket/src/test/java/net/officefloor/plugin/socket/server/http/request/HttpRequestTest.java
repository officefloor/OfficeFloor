/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http.request;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;
import net.officefloor.plugin.socket.server.http.HttpServerStartup;
import net.officefloor.plugin.socket.server.http.request.config.CommunicationConfig;
import net.officefloor.plugin.socket.server.http.request.config.HeaderConfig;
import net.officefloor.plugin.socket.server.http.request.config.ProcessConfig;
import net.officefloor.plugin.socket.server.http.request.config.RequestConfig;
import net.officefloor.plugin.socket.server.http.request.config.ResponseConfig;
import net.officefloor.plugin.socket.server.http.request.config.RunConfig;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * Runs requests against a started instance based on XML configuration.
 * 
 * @author Daniel
 */
public class HttpRequestTest extends AbstractOfficeConstructTestCase {

	/**
	 * Create the {@link TestSuite} of the tests to be run.
	 * 
	 * @return {@link TestSuite} of tests to be run.
	 */
	public static Test suite() throws Exception {
		// Create the test suite
		TestSuite suite = new TestSuite();

		try {

			// Create an instance of the test to obtain test methods
			HttpRequestTest util = new HttpRequestTest(null, null, null);

			// Obtain the file containing XML Unmarshaller configuration
			File unmarshallerConfigFile = util.findFile(HttpRequestTest.class,
					"UnmarshallConfiguration.xml");

			// Obtain the current directory
			XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory
					.getInstance().createUnmarshaller(
							new FileInputStream(unmarshallerConfigFile));

			// All tests are loaded, so start up the HTTP service
			final HttpServerStartup startup = new HttpServerStartup() {
				@Override
				protected TaskReference registerHttpServiceTask()
						throws Exception {
					// Register team to do the work
					this.constructTeam("WORKER", new OnePersonTeam(100));

					// Register the work to process messages
					ReflectiveWorkBuilder workBuilder = this.constructWork(
							new RequestWork(), "servicer", "service");
					ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask(
							"service", "WORKER");
					taskBuilder.buildObject("P-MO", "MO");

					// Return the reference to the service task
					return new TaskReference("servicer", "service");
				}
			};
			startup.setUp();

			// Load the tests
			loadTests("", unmarshallerConfigFile.getParentFile(), unmarshaller,
					startup, suite);

			// Add a task to shutdown the HTTP service
			suite.addTest(new TestCase("Shutdown HTTP Server") {
				@Override
				protected void runTest() throws Throwable {
					startup.tearDown();
				}
			});

		} catch (Throwable ex) {
			ex.printStackTrace();
		}

		// Return the test suite
		return suite;
	}

	/**
	 * Loads the tests for the *Test.xml files.
	 * 
	 * @param testNamePrefix
	 *            Prefix for the test name.
	 * @param directory
	 *            Directory to search for tests.
	 * @param unmarshaller
	 *            {@link XmlUnmarshaller} to unmarshal the test.
	 * @param startup
	 *            {@link HttpServerStartup}.
	 * @param suite
	 *            {@link TestSuite} to add the tests.
	 */
	private static void loadTests(String testNamePrefix, File directory,
			XmlUnmarshaller unmarshaller, HttpServerStartup startup,
			TestSuite suite) throws Exception {

		// Obtain the tests
		for (File file : directory.listFiles()) {

			// Obtain the corresponding test name for the file
			String testName = (testNamePrefix.length() == 0 ? ""
					: testNamePrefix + ".")
					+ file.getName();

			// Determine if child directory
			if (file.isDirectory()) {
				// Child directory, so find tests recursively
				loadTests(testName, file, unmarshaller, startup, suite);

			} else {
				// File and ensure is a test file
				if (!file.getName().endsWith("Test.xml")) {
					// Not a test file
					continue;
				}

				// Load the configuration
				RunConfig configuration = new RunConfig();
				unmarshaller.unmarshall(new FileInputStream(file),
						configuration);

				// Create the test
				suite.addTest(new HttpRequestTest(testName, configuration,
						startup));
			}
		}
	}

	/**
	 * {@link RunConfig}.
	 */
	private final RunConfig configuration;

	/**
	 * Allows for validating exceptions of processing request.
	 */
	private final HttpServerStartup startup;

	/**
	 * Instantiate.
	 * 
	 * @param testName
	 *            Name of the test.
	 * @param configuration
	 *            {@link RunConfig}.
	 * @param startup
	 *            {@link HttpServerStartup}.
	 */
	public HttpRequestTest(String testName, RunConfig configuration,
			HttpServerStartup startup) {
		this.setName(testName);
		this.configuration = configuration;
		this.startup = startup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {

		// Validate the configuration
		assertTrue("Must have at least 1 communication",
				this.configuration.communications.size() > 0);
		for (CommunicationConfig communication : this.configuration.communications) {
			assertNotNull("Communication must have a request",
					communication.request);
			assertNotNull("Communication must have a process",
					communication.process);
			assertNotNull("Communication must have a response",
					communication.response);
		}

		// Create the HTTP Client to send requests
		HttpClient client = new HttpClient();

		System.out.println("====== " + this.getName() + " ======");

		// Run the communications
		for (CommunicationConfig communication : this.configuration.communications) {

			// Indicate the request being run
			System.out.println("--- REQUEST ---");
			RequestConfig request = communication.request;
			System.out.println(request.method + " " + request.path + " "
					+ request.version);
			for (HeaderConfig header : request.headers) {
				System.out.println(header.name + ": " + header.value);
			}
			System.out.println("\n" + request.body);

			// Specify the configuration to service the request
			RequestWork.setConfiguration(communication);

			// Create the method
			String requestUrl = this.startup.getServerUrl() + request.path;
			HttpMethod method;
			if ("GET".equals(request.method)) {
				method = new GetMethod(requestUrl);
			} else if ("POST".equals(request.method)) {
				PostMethod post = new PostMethod(requestUrl);
				post.setRequestEntity(new StringRequestEntity(request.body,
						null, null));
				method = post;
			} else {
				TestCase
						.fail("Unknown request method '" + request.method + "'");
				return;
			}

			// Provide any headers to the request
			for (HeaderConfig header : request.headers) {
				method.addRequestHeader(header.name, header.value);
			}

			// Indicate the processing
			System.out.println("--- PROCESS ---");
			ProcessConfig process = communication.process;
			System.out.println("isClose=" + process.isClose);
			System.out.println(process.body);

			// Execute the method
			client.executeMethod(method);
			try {

				// Obtain the response
				String actualResponseBody = method.getResponseBodyAsString();

				// Validate no failure in processing
				this.startup.validateNoTopLevelEscalation();

				// Indicate the expected response
				ResponseConfig response = communication.response;
				System.out.println("--- EXPECTED RESPONSE (isClosed="
						+ response.isClosed + ") ---");
				System.out.println(response.status + " " + response.version
						+ " " + response.message);
				for (HeaderConfig header : response.headers) {
					System.out.println(header.name + ": " + header.value);
				}
				System.out.println("\n" + response.body);

				// Indicate the actual response
				System.out.println("--- ACTUAL RESPONSE ---");
				StatusLine status = method.getStatusLine();
				System.out.println(status.getStatusCode() + " "
						+ status.getHttpVersion() + " "
						+ status.getReasonPhrase());
				for (Header header : method.getResponseHeaders()) {
					System.out.println(header.getName() + ": "
							+ header.getValue());
				}
				System.out.println();
				System.out.println(actualResponseBody);

				// Validate the response
				assertEquals("Incorrect response status", response.status,
						status.getStatusCode());
				assertEquals("Incorrect status message", response.message,
						status.getReasonPhrase());
				assertEquals("Incorrect response HTTP version",
						response.version, status.getHttpVersion());
				for (HeaderConfig header : response.headers) {
					assertEquals("Incorrect response header '" + header.name
							+ "'", header.value, method.getResponseHeader(
							header.name).getValue());
				}
				assertEquals("Incorrect response body", response.body,
						actualResponseBody);

			} finally {
				// Release the method from the connection
				method.releaseConnection();
			}
		}
	}
}
