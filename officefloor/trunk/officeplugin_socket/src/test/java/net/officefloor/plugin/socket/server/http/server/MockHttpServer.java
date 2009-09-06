/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.ssl.SslEngineConfigurator;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 * <p>
 * Provides functionality to run a HTTP server for testing.
 * <p>
 * <b>This should never be used for production as it is focus is to simplify
 * writing tests.</b>
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

		// Specify the port
		port = portStart;
		portStart++; // increment for next test

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
					new OnePersonTeam("SSL_TASKS", 100));
			serverSocketBuilder.addProperty(
					SslCommunicationProtocol.PROPERTY_SSL_ENGINE_CONFIGURATOR,
					TestSslEngineConfigurator.class.getName());
		} else {
			// Setup unsecure HTTP server
			this.isServerSecure = false;
			serverSocketBuilder = this.constructManagedObject(MO_NAME,
					HttpServerSocketManagedObjectSource.class);
		}

		// Add common properties
		serverSocketBuilder.addProperty(
				HttpServerSocketManagedObjectSource.PROPERTY_PORT, String
						.valueOf(port));
		serverSocketBuilder.addProperty(
				HttpServerSocketManagedObjectSource.PROPERTY_BUFFER_SIZE,
				"1024");
		serverSocketBuilder.setDefaultTimeout(3000);

		// Have server socket managed by office
		ManagingOfficeBuilder<Indexed> managingOfficeBuilder = serverSocketBuilder
				.setManagingOffice(officeName);
		managingOfficeBuilder.setInputManagedObjectName(MO_NAME);

		// Register the necessary teams for socket listening
		this.constructManagedObjectSourceTeam(MO_NAME, "accepter",
				new OnePersonTeam("accepter", 100));
		this.constructManagedObjectSourceTeam(MO_NAME, "listener",
				new WorkerPerTaskTeam("Listener"));
		this.constructManagedObjectSourceTeam(MO_NAME, "cleanup",
				new OnePersonTeam("cleanup", 100));

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
		HttpClient client;
		if (this.isServerSecure()) {
			// Create secure client
			Protocol protocol = new Protocol("https",
					new TestProtocolSocketFactory(), this.port);
			Protocol.registerProtocol("https", protocol);
			client = new HttpClient();
			client.getHostConfiguration().setHost("localhost", this.port,
					protocol);
		} else {
			// Create non-secure client
			client = new HttpClient(new SimpleHttpConnectionManager());
		}
		return client;
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
	 * {@link SslEngineConfigurator} to setup {@link MockHttpServer} to work
	 * with {@link TestProtocolSocketFactory}.
	 */
	public static class TestSslEngineConfigurator implements
			SslEngineConfigurator {

		/*
		 * ==================== SslEngineConfigurator ========================
		 */

		@Override
		public void init(SSLContext context) throws Exception {
			// Do nothing
		}

		@Override
		public void configureSslEngine(SSLEngine engine) {
			// Setup for anonymous ciphers
			engine.setEnabledCipherSuites(engine.getSupportedCipherSuites());
		}
	}

	/**
	 * {@link ProtocolSocketFactory} to connect to the {@link MockHttpServer}
	 * over secure connection.
	 */
	private class TestProtocolSocketFactory implements ProtocolSocketFactory {

		/*
		 * ============== ProtocolSocketFactory ======================
		 */

		@Override
		public Socket createSocket(String host, int port) throws IOException,
				UnknownHostException {

			// Create the secure connected socket
			SSLContext context;
			try {
				context = SSLContext.getDefault();
			} catch (NoSuchAlgorithmException ex) {
				throw new IOException("Failed to obtain default "
						+ SSLContext.class.getSimpleName(), ex);
			}
			SSLSocketFactory socketFactory = context.getSocketFactory();
			Socket socket = socketFactory.createSocket();
			assertFalse("Socket should not be connected", socket.isConnected());

			// Enable all supported to allow anonymous for testing
			SSLSocket sslSocket = (SSLSocket) socket;
			sslSocket.setEnabledCipherSuites(sslSocket
					.getSupportedCipherSuites());

			// Connect the socket
			sslSocket.connect(new InetSocketAddress(host, port));
			assertTrue("Socket should now be connected", socket.isConnected());

			// Return the connected socket
			return socket;
		}

		@Override
		public Socket createSocket(String host, int port,
				InetAddress localAddress, int localPort) throws IOException,
				UnknownHostException {
			return this.createSocket(host, port);
		}

		@Override
		public Socket createSocket(String host, int port,
				InetAddress localAddress, int localPort,
				HttpConnectionParams params) throws IOException,
				UnknownHostException, ConnectTimeoutException {
			return this.createSocket(host, port);
		}
	}

}