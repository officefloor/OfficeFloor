/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.http.request;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.OfficeFloorHttpServerImplementation;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.http.request.config.CommunicationConfig;
import net.officefloor.server.http.request.config.HeaderConfig;
import net.officefloor.server.http.request.config.ProcessConfig;
import net.officefloor.server.http.request.config.RequestConfig;
import net.officefloor.server.http.request.config.ResponseConfig;
import net.officefloor.server.http.request.config.RunConfig;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;

/**
 * Runs requests against a started instance based on XML configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestTest extends AbstractOfficeConstructTestCase {

	/**
	 * Create the {@link TestSuite} of the tests to be run.
	 * 
	 * @return {@link TestSuite} of tests to be run.
	 */
	public static Test suite() throws Exception {
		// Create the test suite
		TestSuite suite = new TestSuite(HttpRequestTest.class.getName());

		try {

			// Create an instance of the test to obtain test methods
			HttpRequestTest util = new HttpRequestTest(null, null, null, false);

			// Obtain the file containing XML Unmarshaller configuration
			File unmarshallerConfigFile = util.findFile(HttpRequestTest.class, "UnmarshallConfiguration.xml");

			// Obtain the current directory
			XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory.getInstance()
					.createUnmarshaller(new FileInputStream(unmarshallerConfigFile));

			// Create the test HTTP server location details
			HttpServerLocation serverLocation = new HttpServerLocationImpl();

			// Add test to start server
			Closure<OfficeFloor> officeFloor = new Closure<>();
			suite.addTest(new TestCase("Startup HTTP Server") {
				@Override
				protected void runTest() throws Throwable {

					// Compile and open the OfficeFloor
					CompileOfficeFloor compile = new CompileOfficeFloor();
					compile.officeFloor((extension) -> {
						DeployedOfficeInput serviceInput = extension.getDeployedOffice()
								.getDeployedOfficeInput("SECTION", "service");
						new HttpServer(new OfficeFloorHttpServerImplementation(), serverLocation, null, null, true,
								OfficeFloorDefaultSslContextSource.createServerSslContext(null), serviceInput,
								extension.getOfficeFloorDeployer(), extension.getOfficeFloorSourceContext());
					});
					compile.office((extension) -> {
						extension.addSection("SECTION", RequestWork.class);
					});
					officeFloor.value = compile.compileAndOpenOfficeFloor();
				}
			});

			// Load non-secure tests
			loadTests("http", unmarshallerConfigFile.getParentFile(), unmarshaller, false, serverLocation, suite);

			// Load the secure tests
			loadTests("https", unmarshallerConfigFile.getParentFile(), unmarshaller, true, serverLocation, suite);

			// Add test to shutdown server
			suite.addTest(new TestCase("Shutdown HTTP Server") {
				@Override
				protected void runTest() throws Throwable {
					if (officeFloor.value != null) {
						officeFloor.value.closeOfficeFloor();
					}
				}
			});

		} catch (Throwable ex) {
			// Add warning that failed to setup
			suite.addTest(TestSuite.warning("Failed setting up suite of tests: " + ex.getMessage() + " ["
					+ ex.getClass().getSimpleName() + "]"));
		}

		// Return the test suite
		return suite;
	}

	/**
	 * Loads the tests for the *Test.xml files.
	 * 
	 * @param testNamePrefix Prefix for the test name.
	 * @param directory      Directory to search for tests.
	 * @param unmarshaller   {@link XmlUnmarshaller} to unmarshal the test.
	 * @param isSecure       Indicates if to be secure.
	 * @param serverLocation {@link HttpServerLocation}.
	 * @param suite          {@link TestSuite} to add the tests.
	 */
	private static void loadTests(String testNamePrefix, File directory, XmlUnmarshaller unmarshaller, boolean isSecure,
			HttpServerLocation serverLocation, TestSuite suite) throws Exception {

		// Obtain the tests
		for (File file : directory.listFiles()) {

			// Obtain the corresponding test name for the file
			String testName = (testNamePrefix.length() == 0 ? "" : testNamePrefix + ".") + file.getName();

			// Determine if child directory
			if (file.isDirectory()) {
				// Child directory, so find tests recursively
				loadTests(testName, file, unmarshaller, isSecure, serverLocation, suite);

			} else {
				// File and ensure is a test file
				if (!file.getName().endsWith("Test.xml")) {
					// Not a test file
					continue;
				}

				// Load the configuration
				RunConfig configuration = new RunConfig();
				unmarshaller.unmarshall(new FileInputStream(file), configuration);

				// Create the test
				suite.addTest(new HttpRequestTest(testName, configuration, serverLocation.createClientUrl(isSecure, ""),
						isSecure));
			}
		}
	}

	/**
	 * {@link RunConfig}.
	 */
	private final RunConfig configuration;

	/**
	 * Base URL to the server.
	 */
	private String serverBaseUrl;

	/**
	 * Indicates if is to be secure.
	 */
	private final boolean isSecure;

	/**
	 * Instantiate.
	 * 
	 * @param testName      Name of the test.
	 * @param configuration {@link RunConfig}.
	 * @param isSecure      Indicates if is to be secure.
	 */
	public HttpRequestTest(String testName, RunConfig configuration, String serverBaseUrl, boolean isSecure) {
		this.setName(testName);
		this.configuration = configuration;
		this.serverBaseUrl = serverBaseUrl;
		this.isSecure = isSecure;
	}

	/*
	 * ==================== TestCase =================================
	 */

	@Override
	protected void runTest() throws Throwable {

		// Validate the configuration
		assertTrue("Must have at least 1 communication", this.configuration.communications.size() > 0);
		for (CommunicationConfig communication : this.configuration.communications) {
			assertNotNull("Communication must have a request", communication.request);
			assertNotNull("Communication must have a process", communication.process);
			assertNotNull("Communication must have a response", communication.response);
		}

		// Create the HTTP Client to send requests
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(this.isSecure)) {

			System.out.println("====== " + this.getName() + " ======");

			// Run the communications
			for (CommunicationConfig communication : this.configuration.communications) {

				// Indicate the request being run
				System.out.println("--- REQUEST ---");
				RequestConfig request = communication.request;
				System.out.println(request.method + " " + request.path + " " + request.version);
				for (HeaderConfig header : request.headers) {
					System.out.println(header.name + ": " + header.value);
				}
				System.out.println("\n" + request.body);

				// Specify the configuration to service the request
				RequestWork.setConfiguration(communication);

				// Create the method
				String requestUrl = this.serverBaseUrl + "/" + request.path;
				HttpUriRequest method;
				if ("GET".equals(request.method)) {
					method = new HttpGet(requestUrl);
				} else if ("POST".equals(request.method)) {
					HttpPost post = new HttpPost(requestUrl);
					post.setEntity(new StringEntity(request.body));
					method = post;
				} else {
					TestCase.fail("Unknown request method '" + request.method + "'");
					return;
				}

				// Provide any headers to the request
				for (HeaderConfig header : request.headers) {
					method.addHeader(header.name, header.value);
				}

				// Indicate the processing
				System.out.println("--- PROCESS ---");
				ProcessConfig process = communication.process;
				System.out.println(process.body);

				// Execute the method
				HttpResponse methodResponse = client.execute(method);

				// Obtain the response body
				String actualResponseBody = HttpClientTestUtil.entityToString(methodResponse);

				// Indicate the expected response
				ResponseConfig response = communication.response;
				System.out.println("--- EXPECTED RESPONSE ---");
				System.out.println(response.status + " " + response.version + " " + response.message);
				for (HeaderConfig header : response.headers) {
					System.out.println(header.name + ": " + header.value);
				}
				System.out.println("\n" + response.body);

				// Indicate the actual response
				System.out.println("--- ACTUAL RESPONSE ---");
				StatusLine status = methodResponse.getStatusLine();
				System.out.println(
						status.getStatusCode() + " " + status.getProtocolVersion() + " " + status.getReasonPhrase());
				for (Header header : methodResponse.getAllHeaders()) {
					System.out.println(header.getName() + ": " + header.getValue());
				}
				System.out.println();
				System.out.println(actualResponseBody);

				// Validate the response
				assertEquals("Incorrect response status", response.status, status.getStatusCode());
				assertEquals("Incorrect status message", response.message, status.getReasonPhrase());
				assertEquals("Incorrect response HTTP version", response.version,
						status.getProtocolVersion().toString());
				for (HeaderConfig header : response.headers) {
					assertEquals("Incorrect response header '" + header.name + "'", header.value,
							methodResponse.getFirstHeader(header.name).getValue());
				}
				if (response.body != null) {
					assertEquals("Incorrect response body", response.body, actualResponseBody);
				}
			}
		}
	}

}
