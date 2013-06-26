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
package net.officefloor.plugin.socket.server.http.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.ssl.OfficeFloorDefaultSslEngineSource;
import net.officefloor.plugin.socket.server.ssl.SslEngineSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

/**
 * <p>
 * Provides functionality to run a HTTP server for testing.
 * <p>
 * <b>This should NEVER be used for Production as its focus is only to simplify
 * writing tests for HTTP server functionality.</b>
 * 
 * @author Daniel Sagenschneider
 */
public abstract class MockHttpServer extends AbstractOfficeConstructTestCase
		implements HttpServicerBuilder {

	/**
	 * Starting port number.
	 */
	private static int portStart = 12643;

	/**
	 * Obtains the next port number for testing.
	 * 
	 * @return Next port number for testing.
	 */
	public static synchronized int getAvailablePort() {
		int port = portStart;
		portStart++; // increment port for next text
		return port;
	}

	/**
	 * Obtains the {@link HttpEntity} content.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @return Content of {@link HttpEntity}.
	 * @throws IOException
	 *             If fails to obtain content.
	 */
	public static String getEntityBody(HttpResponse response)
			throws IOException {
		return getEntityBody(response, Charset.defaultCharset());
	}

	/**
	 * Obtains the {@link HttpEntity} content.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @param charset
	 *            {@link Charset}.
	 * @return Content of {@link HttpEntity}.
	 * @throws IOException
	 *             If fails to obtain content.
	 */
	public static String getEntityBody(HttpResponse response, Charset charset)
			throws IOException {

		// Obtain the entity
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null; // no entity so no content
		}

		// Obtain the entity content
		Reader body = new InputStreamReader(entity.getContent(), charset);
		StringWriter buffer = new StringWriter();
		for (int character = body.read(); character >= 0; character = body
				.read()) {
			buffer.write(character);
		}

		// Return the entity content
		return buffer.toString();
	}

	/**
	 * Configures the {@link HttpClient} with HTTPS {@link Scheme}.
	 * 
	 * @param client
	 *            {@link HttpClient}.
	 * @param port
	 *            Default port for the HTTPS.
	 * 
	 * @see #getSslEngineSourceClass()
	 */
	public static void configureHttps(HttpClient client, int port) {
		SocketFactory socketFactory = new OfficeFloorDefaultSocketFactory();
		Scheme scheme = new Scheme("https", socketFactory, port);
		client.getConnectionManager().getSchemeRegistry().register(scheme);
	}

	/**
	 * Obtains the {@link SslEngineSource} for the corresponding configured
	 * HTTPS.
	 * 
	 * @return {@link SslEngineSource} for the corresponding configured HTTPS.
	 * 
	 * @see #configureHttps(HttpClient, int)
	 */
	public static Class<? extends SslEngineSource> getSslEngineSourceClass() {
		return OfficeFloorDefaultSslEngineSource.class;
	}

	/**
	 * Port number to use for testing.
	 */
	private int port;

	/**
	 * Flag indicating if to setup secure {@link MockHttpServer}.
	 */
	private boolean isSetupSecure = false;

	/**
	 * Flag indicating if {@link MockHttpServer} is running with secure
	 * connections.
	 */
	private boolean isServerSecure = false;

	/**
	 * Listing of the {@link HttpClient} instances.
	 */
	private final List<HttpClient> httpClients = new LinkedList<HttpClient>();

	/**
	 * Flags to setup the {@link MockHttpServer} with secure connections.
	 */
	public void setupSecure() {
		this.isSetupSecure = true;
	}

	/**
	 * Starts up this {@link MockHttpServer}.
	 */
	public void startup(HttpServicerBuilder servicerBuilder) throws Exception {

		// Initiate for starting the office floor
		super.setUp();

		// Ensure any previous is shutdown
		AbstractServerSocketManagedObjectSource.closeConnectionManager();

		// Specify the port
		this.port = getAvailablePort();

		// Obtain the office name and builder
		String officeName = this.getOfficeName();

		// Determine if setup secure HTTP server
		final String MO_NAME = "MO";
		ManagedObjectBuilder<Indexed> serverSocketBuilder;
		if (this.isSetupSecure) {
			// Setup secure HTTP server
			this.isServerSecure = true;
			serverSocketBuilder = this.constructManagedObject(MO_NAME,
					HttpsServerSocketManagedObjectSource.class);
			this.constructManagedObjectSourceTeam(MO_NAME, "SSL_TASKS",
					MockTeamSource.createOnePersonTeam("SSL_TASKS"));
			serverSocketBuilder.addProperty(
					SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE,
					getSslEngineSourceClass().getName());
		} else {
			// Setup unsecure HTTP server
			this.isServerSecure = false;
			serverSocketBuilder = this.constructManagedObject(MO_NAME,
					HttpServerSocketManagedObjectSource.class);
		}

		// Add common properties
		serverSocketBuilder.addProperty(
				HttpServerSocketManagedObjectSource.PROPERTY_PORT,
				String.valueOf(this.port));
		serverSocketBuilder.setTimeout(3000);

		// Have server socket managed by office
		ManagingOfficeBuilder<Indexed> managingOfficeBuilder = serverSocketBuilder
				.setManagingOffice(officeName);
		managingOfficeBuilder.setInputManagedObjectName(MO_NAME);

		// Register the necessary teams for socket listening
		this.constructManagedObjectSourceTeam(MO_NAME, "accepter",
				MockTeamSource.createWorkerPerTaskTeam("accepter"));
		this.constructManagedObjectSourceTeam(MO_NAME, "listener",
				MockTeamSource.createWorkerPerTaskTeam("listener"));
		this.constructManagedObjectSourceTeam(MO_NAME, "cleanup",
				new PassiveTeam());

		// Register the task to service the HTTP requests
		HttpServicerTask task = servicerBuilder.buildServicer(MO_NAME, this);
		managingOfficeBuilder.linkProcess(0, task.workName, task.taskName);

		// Create and open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
	}

	/**
	 * Helper method to register a {@link Team} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObjectTeamName
	 *            Name of the {@link Team} for the {@link ManagedObjectSource}.
	 * @param team
	 *            {@link Team} to register.
	 */
	private void constructManagedObjectSourceTeam(String managedObjectName,
			String managedObjectTeamName, Team team) {
		this.constructTeam(managedObjectTeamName, team);
		this.getOfficeBuilder().registerTeam(
				"of-" + managedObjectName + "." + managedObjectTeamName,
				"of-" + managedObjectTeamName);
	}

	/**
	 * Flags if {@link MockHttpServer} is running with secure connections.
	 * 
	 * @return <code>true</code> if secure connections.
	 */
	public boolean isServerSecure() {
		return this.isServerSecure;
	}

	/**
	 * Obtains the URL of the server.
	 * 
	 * @return URL of the server.
	 */
	public String getServerUrl() {
		return (this.isServerSecure() ? "https" : "http") + "://localhost:"
				+ this.port;
	}

	/**
	 * Creates a {@link HttpClient} to connect to this {@link MockHttpServer}.
	 * 
	 * @return {@link HttpClient} to connect to this {@link MockHttpServer}.
	 */
	public HttpClient createHttpClient() {

		// Create the HTTP client
		HttpClient client = new DefaultHttpClient();
		if (this.isServerSecure()) {
			// Configure to be secure client
			configureHttps(client, this.port);
		}

		// Register the HTTP client for cleanup
		this.httpClients.add(client);

		// Return the HTTP client
		return client;
	}

	/**
	 * Obtains the local {@link InetSocketAddress} for this server.
	 * 
	 * @return Local {@link InetSocketAddress} for this server.
	 */
	public InetSocketAddress getLocalAddress() {
		try {
			return new InetSocketAddress(InetAddress.getLocalHost()
					.getHostAddress(), this.port);
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Shuts down this {@link MockHttpServer}.
	 */
	public void shutdown() throws Exception {
		this.tearDown();
	}

	/*
	 * =================== TestCase ======================================
	 */

	@Override
	protected void setUp() throws Exception {
		// Setup to use this as TestCase
		this.startup(this);
	}

	@Override
	protected void tearDown() throws Exception {
		// Shutdown the HTTP Clients
		for (HttpClient client : this.httpClients) {
			client.getConnectionManager().shutdown();
		}

		// Clean up parent
		super.tearDown();
	}

	/*
	 * =================== HttpServicerBuilder ==========================
	 */

	@Override
	public HttpServicerTask buildServicer(String managedObjectName,
			MockHttpServer server) throws Exception {
		// Must override if using as TestCase
		fail("Must override " + HttpServicerBuilder.class.getSimpleName()
				+ " test case implementation");
		return null;
	}

	/**
	 * <p>
	 * {@link LayeredSocketFactory} to connect to the {@link MockHttpServer}
	 * over secure connection.
	 * <p>
	 * This allows working with a {@link OfficeFloorDefaultSslEngineSource}.
	 */
	private static class OfficeFloorDefaultSocketFactory implements
			LayeredSocketFactory {

		/*
		 * ============== LayeredSocketFactory ======================
		 */

		@Override
		public Socket createSocket() throws IOException {

			// Create the secure connected socket
			SSLContext context;
			try {
				context = OfficeFloorDefaultSslEngineSource
						.createClientSslContext(null);

			} catch (Exception ex) {
				// Propagate failure in configuring OfficeFloor default key
				throw new IOException(ex);
			}

			// Create the socket
			SSLSocketFactory socketFactory = context.getSocketFactory();
			Socket socket = socketFactory.createSocket();
			assertFalse("Socket should not be connected", socket.isConnected());

			// Return the socket
			return socket;
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return socket;
		}

		@Override
		public Socket connectSocket(Socket socket, String host, int port,
				InetAddress localAddress, int localPort, HttpParams params)
				throws IOException, UnknownHostException,
				ConnectTimeoutException {

			// Connect the socket
			socket.connect(new InetSocketAddress(host, port));
			assertTrue("Socket should now be connected", socket.isConnected());

			// Return the connected socket
			return socket;
		}

		@Override
		public boolean isSecure(Socket socket) throws IllegalArgumentException {
			return (socket instanceof SSLSocket);
		}
	}

}