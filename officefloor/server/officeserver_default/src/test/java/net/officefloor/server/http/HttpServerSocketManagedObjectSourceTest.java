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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;

/**
 * Tests the {@link HttpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerSocketManagedObjectSourceTest {

	private OfficeFloor officeFloor;

	@AfterEach
	protected void tearDown() throws Exception {

		// Ensure close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure valid {@link HttpServerSocketManagedObjectSource}
	 * {@link ManagedObjectType}.
	 */
	@Test
	public void validateHttpType() throws Exception {

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
	@Test
	public void validateHttpsType() throws Exception {

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
	@Test
	public void serviceRequest() throws Exception {

		// Start non-secure server
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTP_PORT, String.valueOf(7878));
		}, null);

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpPost request = new HttpPost("http://localhost:7878");
			request.setEntity(new StringEntity("REQUEST"));
			HttpResponse response = client.execute(request);
			assertEquals(HttpStatus.OK.getStatusCode(), response.getStatusLine().getStatusCode(),
					"Should be succesful");
			assertEquals("header", response.getFirstHeader("test").getValue(), "Incorrect header");
			assertEquals("test=cookie", response.getFirstHeader("set-cookie").getValue(), "Incorrect cookie");
			assertEquals("test", HttpClientTestUtil.entityToString(response), "Incorrect content");
		}
	}

	/**
	 * Ensure can create a secure connection.
	 */
	@Test
	public void secureConnection() throws Exception {

		// Start secure server
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
			deployer.link(httpMos.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
					deployer.addTeam("TEAM", ExecutorCachedTeamSource.class.getName()));
		}, null);

		// Create request
		byte[] request = UsAsciiUtil.convertToHttp("POST / HTTP/1.1\nContent-Length: 7\n\nREQUEST");

		// Create expected response (ignoring rest of data)
		byte[] expectedResponseStart = UsAsciiUtil.convertToHttp("HTTP/1.1 200 OK");

		// Ensure can create a secure connection
		try (Socket socket = OfficeFloorDefaultSslContextSource.createClientSslContext(null).getSocketFactory()
				.createSocket("localhost", 7979)) {
			socket.setSoTimeout(1000);

			// Send the request
			socket.getOutputStream().write(request);

			// Ensure have a response
			byte[] actualResponseStart = new byte[expectedResponseStart.length];
			socket.getInputStream().read(actualResponseStart);
			for (int i = 0; i < expectedResponseStart.length; i++) {
				assertEquals(expectedResponseStart[i], actualResponseStart[i], "Incorrect response byte " + i);
			}
		}
	}

	/**
	 * Ensure can configure and service secure request.
	 */
	@Test
	public void serviceSecureRequest() throws Exception {

		// Start secure server
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
			deployer.link(httpMos.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
					deployer.addTeam("TEAM", ExecutorCachedTeamSource.class.getName()));
		}, null);

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {
			HttpPost request = new HttpPost("https://localhost:7979");
			request.setEntity(new StringEntity("REQUEST"));
			HttpResponse response = client.execute(request);
			assertEquals(HttpStatus.OK.getStatusCode(), response.getStatusLine().getStatusCode(),
					"Should be succesful");
			assertEquals("test", HttpClientTestUtil.entityToString(response), "Incorrect content");
		}
	}

	/**
	 * Ensure reject insecure request.
	 */
	@Test
	public void rejectInsecureRequest() throws Exception {

		// Start secure server
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
			deployer.link(httpMos.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
					deployer.addTeam("TEAM", ExecutorCachedTeamSource.class.getName()));
		}, null);

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
	 * Ensure can configure and service request with a {@link ThreadLocalAwareTeam}
	 * configured.
	 */
	@Test
	public void serviceRequestWithBlockingTeam() throws Exception {

		// Start non-secure server
		final String THREAD_LOCAL_AWARE_TEAM = "BLOCKING_TEAM";
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTP_PORT, String.valueOf(7878));

			// Register thread local aware team (to block invoking thread until serviced)
			OfficeFloorTeam team = deployer.addTeam(THREAD_LOCAL_AWARE_TEAM, new ThreadLocalAwareTeamSource());
			team.requestNoTeamOversight();

			// Register team to the office
			DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
			deployer.link(office.getDeployedOfficeTeam(THREAD_LOCAL_AWARE_TEAM), team);

		}, (architect) -> {
			// Configure blocking team into office
			architect.addOfficeTeam(THREAD_LOCAL_AWARE_TEAM);
		});

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpPost request = new HttpPost("http://localhost:7878");
			request.setEntity(new StringEntity("REQUEST"));
			HttpResponse response = client.execute(request);
			assertEquals(HttpStatus.OK.getStatusCode(), response.getStatusLine().getStatusCode(),
					"Should be succesful");
		}
	}

	/**
	 * Ensure can configure and service secure request with a
	 * {@link ThreadLocalAwareTeam} configured.
	 */
	@Test
	public void serviceSecureRequestWithBlockingTeam() throws Exception {

		// Start secure server
		final String THREAD_LOCAL_AWARE_TEAM = "BLOCKING_TEAM";
		this.startServer((httpMos, deployer) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
			deployer.link(httpMos.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
					deployer.addTeam("TEAM", ExecutorCachedTeamSource.class.getName()));

			// Register thread local aware team (to block invoking thread until serviced)
			OfficeFloorTeam team = deployer.addTeam(THREAD_LOCAL_AWARE_TEAM, new ThreadLocalAwareTeamSource());
			team.requestNoTeamOversight();

			// Register team to the office
			DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
			deployer.link(office.getDeployedOfficeTeam(THREAD_LOCAL_AWARE_TEAM), team);

		}, (architect) -> {
			// Configure blocking team into office
			architect.addOfficeTeam(THREAD_LOCAL_AWARE_TEAM);
		});

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {
			HttpPost request = new HttpPost("https://localhost:7979");
			request.setEntity(new StringEntity("REQUEST"));
			HttpResponse response = client.execute(request);
			assertEquals(HttpStatus.OK.getStatusCode(), response.getStatusLine().getStatusCode(),
					"Should be succesful");
		}
	}

	/**
	 * Starts the {@link HttpServerSocketManagedObjectSource} to service requests.
	 * 
	 * @param httpConfigurer Configures the
	 *                       {@link HttpServerSocketManagedObjectSource}.
	 */
	private void startServer(BiConsumer<OfficeFloorManagedObjectSource, OfficeFloorDeployer> httpConfigurer,
			Consumer<OfficeArchitect> officeConfigurer) throws Exception {

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

			// Configure handling request
			extension.addSection("SECTION", MockSection.class);

			// Provide optional additional configuration
			if (officeConfigurer != null) {
				officeConfigurer.accept(extension.getOfficeArchitect());
			}
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();
	}

	/**
	 * Mock section.
	 */
	public static class MockSection {

		public void service(ServerHttpConnection connection) throws IOException {

			// Ensure have entity
			String requestEntity = EntityUtil.toString(connection.getRequest(), null);
			assertEquals("REQUEST", requestEntity, "Incorrect request entity");

			// Send response
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.getHeaders().addHeader("test", "header");
			response.getCookies().setCookie("test", "cookie");
			response.getEntityWriter().write("test");
		}
	}

}
