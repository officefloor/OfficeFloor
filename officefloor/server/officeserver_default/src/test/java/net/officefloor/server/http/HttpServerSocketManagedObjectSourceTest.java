/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.function.Consumer;

import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpServerSocketManagedObjectSource.Flows;
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
	 * Ensure can configure and service request.
	 */
	public void testServiceRequest() throws Exception {

		// Start non-secure server
		this.startServer((httpMos) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTP_PORT, String.valueOf(7878));
		});

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878"));
			assertEquals("Should be succesful", HttpStatus.OK.getStatusCode(),
					response.getStatusLine().getStatusCode());
			assertEquals("Incorrect header", "header", response.getFirstHeader("test").getValue());
			assertEquals("Incorrect cookie", "test=cookie", response.getFirstHeader("set-cookie").getValue());
			assertEquals("Incorrect content", "test", HttpClientTestUtil.getEntityBody(response));
		}
	}

	/**
	 * Ensure can create a secure connection.
	 */
	public void testSecureConnection() throws Exception {

		// Start secure server
		this.startServer((httpMos) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
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
		this.startServer((httpMos) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
		});

		// Ensure can get response
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {
			HttpResponse response = client.execute(new HttpGet("https://localhost:7979"));
			assertEquals("Should be succesful", HttpStatus.OK.getStatusCode(),
					response.getStatusLine().getStatusCode());
			assertEquals("Incorrect content", "test", HttpClientTestUtil.getEntityBody(response));
		}
	}

	public void testRejectInsecureReqeust() throws Exception {

		// Start secure server
		this.startServer((httpMos) -> {
			httpMos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(7979));
			httpMos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, String.valueOf(true));
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
	 * Starts the {@link HttpServerSocketManagedObjectSource} to service
	 * requests.
	 * 
	 * @param httpConfigurer
	 *            Configures the {@link HttpServerSocketManagedObjectSource}.
	 */
	private void startServer(Consumer<OfficeFloorManagedObjectSource> httpConfigurer) throws Exception {

		// Compile the OfficeFloor to service request
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((extension) -> {

			// Obtain the OfficeFloor deployer
			OfficeFloorDeployer deployer = extension.getOfficeFloorDeployer();

			// Configure the HTTP managed object source
			OfficeFloorManagedObjectSource httpMos = deployer.addManagedObjectSource("HTTP",
					HttpServerSocketManagedObjectSource.class.getName());
			httpConfigurer.accept(httpMos);

			// Configure input
			OfficeFloorInputManagedObject inputHttp = deployer.addInputManagedObject("HTTP",
					ServerHttpConnection.class.getName());
			deployer.link(httpMos, inputHttp);

			// Configure office
			DeployedOffice office = extension.getDeployedOffice();
			deployer.link(httpMos.getManagingOffice(), office);

			// Configure handling request
			deployer.link(httpMos.getManagedObjectFlow(Flows.HANDLE_REQUEST.name()),
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