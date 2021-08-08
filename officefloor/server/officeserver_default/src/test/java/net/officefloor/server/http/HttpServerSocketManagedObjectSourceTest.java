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

package net.officefloor.server.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.function.BiConsumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;

/**
 * Tests the {@link HttpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerSocketManagedObjectSourceTest extends OfficeFrameTestCase {

	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {

		// Ensure close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Remaining tear down
		super.tearDown();
	}

	/**
	 * Ensure valid {@link HttpServerSocketManagedObjectSource}
	 * {@link ManagedObjectType}.
	 */
	public void testValidateHttpType() throws Exception {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(ServerHttpConnection.class);
		type.setInput(true);
		type.addFlow("HANDLE_REQUEST", null, 0, null);
		type.addExecutionStrategy("HTTP_SOCKET_SERVICING");

		// Validate the type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, HttpServerSocketManagedObjectSource.class);
	}

	/**
	 * Ensure valid secure {@link HttpServerSocketManagedObjectSource}
	 * {@link ManagedObjectType}.
	 */
	public void testValidateHttpsType() throws Exception {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(ServerHttpConnection.class);
		type.setInput(true);
		type.addFlow("HANDLE_REQUEST", null, 0, null);
		type.addTeam("SSL_TEAM");
		type.addExecutionStrategy("HTTP_SOCKET_SERVICING");

		// Validate the type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, new HttpServerSocketManagedObjectSource(
				new HttpServerLocationImpl(), null, null, false, SSLContext.getDefault()));
	}

	/**
	 * Ensure can configure and service request.
	 */
	public void testServiceRequest() throws Exception {

		// Start non-secure server
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTP_PORT, String.valueOf(7878));
		});

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878"));
			assertEquals("Should be succesful", HttpStatus.OK.getStatusCode(),
					response.getStatusLine().getStatusCode());
			assertEquals("Incorrect header", "header", response.getFirstHeader("test").getValue());
			assertEquals("Incorrect cookie", "test=cookie", response.getFirstHeader("set-cookie").getValue());
			assertEquals("Incorrect content", "test", HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Ensure can create a secure connection.
	 */
	public void testSecureConnection() throws Exception {

		// Start secure server
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
			deployer.link(httpMos.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
					deployer.addTeam("TEAM", ExecutorCachedTeamSource.class.getName()));
		});

		// Create request
		byte[] request = UsAsciiUtil.convertToHttp("GET / HTTP/1.1\n\n");

		// Create expected response (ignoring rest of data)
		byte[] expectedResponseStart = UsAsciiUtil.convertToHttp("HTTP/1.1 200 OK");

		// Ensure can create a secure connection
		try (Socket socket = OfficeFloorDefaultSslContextSource.createClientSslContext(null).getSocketFactory()
				.createSocket(InetAddress.getLocalHost(), 7979)) {
			socket.setSoTimeout(1000);

			// Send the request
			socket.getOutputStream().write(request);

			// Ensure have a response
			byte[] actualResponseStart = new byte[expectedResponseStart.length];
			socket.getInputStream().read(actualResponseStart);
			for (int i = 0; i < expectedResponseStart.length; i++) {
				assertEquals("Incorrect response byte " + i, expectedResponseStart[i], actualResponseStart[i]);
			}
		}
	}

	/**
	 * Ensure can configure and service secure request.
	 */
	public void testServiceSecureRequest() throws Exception {

		// Start secure server
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
			deployer.link(httpMos.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
					deployer.addTeam("TEAM", ExecutorCachedTeamSource.class.getName()));
		});

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {
			HttpResponse response = client.execute(new HttpGet("https://localhost:7979"));
			assertEquals("Should be succesful", HttpStatus.OK.getStatusCode(),
					response.getStatusLine().getStatusCode());
			assertEquals("Incorrect content", "test", HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Ensure reject insecure request.
	 */
	public void testRejectInsecureReqeust() throws Exception {

		// Start secure server
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
			deployer.link(httpMos.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
					deployer.addTeam("TEAM", ExecutorCachedTeamSource.class.getName()));
		});

		// Use default SslContext which should not match on cypher
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(false)) {

			// Send request
			try {
				client.execute(new HttpGet("https://localhost:7979"));
				fail("Should not be successful");

			} catch (SSLException ex) {
				// Should have issue connecting
			}
		}
	}

	/**
	 * Starts the {@link HttpServerSocketManagedObjectSource} to service requests.
	 * 
	 * @param httpConfigurer Configures the
	 *                       {@link HttpServerSocketManagedObjectSource}.
	 */
	private void startServer(BiConsumer<OfficeFloorManagedObjectSource, OfficeFloorDeployer> httpConfigurer)
			throws Exception {

		// Compile the OfficeFloor to service request
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((extension) -> {

			// Obtain the OfficeFloor deployer
			OfficeFloorDeployer deployer = extension.getOfficeFloorDeployer();

			// Configure the HTTP managed object source
			OfficeFloorManagedObjectSource httpMos = deployer.addManagedObjectSource("HTTP",
					HttpServerSocketManagedObjectSource.class.getName());
			httpConfigurer.accept(httpMos, deployer);

			// Configure input
			OfficeFloorInputManagedObject inputHttp = deployer.addInputManagedObject("HTTP",
					ServerHttpConnection.class.getName());
			deployer.link(httpMos, inputHttp);

			// Configure office
			DeployedOffice office = extension.getDeployedOffice();
			deployer.link(httpMos.getManagingOffice(), office);

			// Configure handling request
			deployer.link(
					httpMos.getOfficeFloorManagedObjectFlow(
							HttpServerSocketManagedObjectSource.HANDLE_REQUEST_FLOW_NAME),
					office.getDeployedOfficeInput("SECTION", "service"));

		});
		compile.office((extension) -> {
			extension.addSection("SECTION", MockSection.class);
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();
	}

	/**
	 * Mock section.
	 */
	public static class MockSection {

		public void service(ServerHttpConnection connection) throws IOException {
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.getHeaders().addHeader("test", "header");
			response.getCookies().setCookie("test", "cookie");
			response.getEntityWriter().write("test");
		}
	}

}
