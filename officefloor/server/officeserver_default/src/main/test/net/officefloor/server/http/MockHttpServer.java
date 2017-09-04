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
package net.officefloor.server.http;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.server.AbstractServerSocketManagedObjectSource;
import net.officefloor.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.server.ssl.SslCommunicationProtocol;

/**
 * <p>
 * Provides functionality to run a HTTP server for testing.
 * <p>
 * <b>This should NEVER be used for Production as its focus is only to simplify
 * writing tests for HTTP server functionality.</b>
 * 
 * @author Daniel Sagenschneider
 */
public abstract class MockHttpServer extends AbstractOfficeConstructTestCase implements HttpServicerBuilder {

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
	private final List<CloseableHttpClient> httpClients = new LinkedList<>();

	/**
	 * Flags to setup the {@link MockHttpServer} with secure connections.
	 */
	public void setupSecure() {
		this.isSetupSecure = true;
	}

	/**
	 * Starts up this {@link MockHttpServer}.
	 * 
	 * @param servicerBuilder
	 *            {@link HttpServicerBuilder}.
	 * @throws Exception
	 *             If fails to start up.
	 */
	public void startup(HttpServicerBuilder servicerBuilder) throws Exception {

		// Initiate for starting the office floor
		super.setUp();

		// Ensure any previous is shutdown
		AbstractServerSocketManagedObjectSource.closeSocketManager();

		// Specify the port
		this.port = HttpServerTestUtil.getAvailablePort();

		// Obtain the office name and builder
		String officeName = this.getOfficeName();

		// Determine if setup secure HTTP server
		final String MO_NAME = "MO";
		ManagedObjectBuilder<Indexed> serverSocketBuilder;
		if (this.isSetupSecure) {
			// Setup secure HTTP server
			this.isServerSecure = true;
			serverSocketBuilder = this.constructManagedObject(MO_NAME, HttpsServerSocketManagedObjectSource.class,
					officeName);
			this.constructManagedObjectSourceTeam(MO_NAME, "SSL", ExecutorCachedTeamSource.class);
			serverSocketBuilder.addProperty(SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE,
					HttpServerTestUtil.createTestServerSslContext().getClass().getName());
		} else {
			// Setup non-secure HTTP server
			this.isServerSecure = false;
			serverSocketBuilder = this.constructManagedObject(MO_NAME, HttpServerSocketManagedObjectSource.class,
					officeName);
		}

		// Add common properties
		serverSocketBuilder.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_PORT, String.valueOf(this.port));
		serverSocketBuilder.setTimeout(3000);

		// Have server socket managed by office
		ManagingOfficeBuilder<Indexed> managingOfficeBuilder = serverSocketBuilder.setManagingOffice(officeName);
		managingOfficeBuilder.setInputManagedObjectName(MO_NAME);

		// Register the necessary teams for socket listening
		this.constructManagedObjectSourceTeam(MO_NAME, "listener", ExecutorCachedTeamSource.class);

		// Register the function to service the HTTP requests
		HttpServicerFunction function = servicerBuilder.buildServicer(MO_NAME, this);
		managingOfficeBuilder.linkFlow(0, function.functionName);

		// Create and open the OfficeFloor
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
	 * @param teamSourceClass
	 *            {@link TeamSource} {@link Class} to register.
	 */
	private void constructManagedObjectSourceTeam(String managedObjectName, String managedObjectTeamName,
			Class<? extends TeamSource> teamSourceClass) {
		this.constructTeam(managedObjectTeamName, teamSourceClass);
		this.getOfficeBuilder().registerTeam("of-" + managedObjectName + "." + managedObjectTeamName,
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
		return (this.isServerSecure() ? "https" : "http") + "://localhost:" + this.port;
	}

	/**
	 * Creates a {@link HttpClient} to connect to this {@link MockHttpServer}.
	 * 
	 * @return {@link HttpClient} to connect to this {@link MockHttpServer}.
	 */
	public CloseableHttpClient createHttpClient() {

		// Create the HTTP client
		CloseableHttpClient client = HttpClientTestUtil.createHttpClient(this.isServerSecure);

		// Register the HTTP client for cleanup
		this.httpClients.add(client);

		// Return the HTTP client
		return client;
	}

	/**
	 * Shuts down this {@link MockHttpServer}.
	 * 
	 * @throws Exception
	 *             If fails to shutdown.
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
		for (CloseableHttpClient client : this.httpClients) {
			client.close();
		}

		// Clean up parent
		super.tearDown();
	}

	/*
	 * =================== HttpServicerBuilder ==========================
	 */

	@Override
	public HttpServicerFunction buildServicer(String managedObjectName, MockHttpServer server) throws Exception {
		// Must override if using as TestCase
		fail("Must override " + HttpServicerBuilder.class.getSimpleName() + " test case implementation");
		return null;
	}

}