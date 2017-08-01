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
package net.officefloor.server.http.source;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.protocol.CommunicationProtocolSource;
import net.officefloor.server.http.protocol.HttpCommunicationProtocol;
import net.officefloor.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.server.ssl.protocol.SslCommunicationProtocol;

/**
 * {@link ManagedObjectSource} for a secure {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpsServerSocketManagedObjectSource extends AbstractServerSocketManagedObjectSource {

	/**
	 * Convenience method to configure the
	 * {@link HttpsServerSocketManagedObjectSource}.
	 * 
	 * @param deployer
	 *            {@link OfficeFloorDeployer}.
	 * @param port
	 *            Port to listen for HTTPS requests.
	 * @param sslContext
	 *            {@link SslContext}. May be <code>null</code>.
	 * @param officeInput
	 *            {@link DeployedOfficeInput} to service {@link HttpRequest}
	 *            instances.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configure(OfficeFloorDeployer deployer, int port, SSLContext sslContext,
			DeployedOfficeInput officeInput) {

		// Add this managed object source
		OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("HTTPS_SOURCE_" + port,
				new HttpsServerSocketManagedObjectSource(sslContext));
		mos.addProperty(PROPERTY_PORT, String.valueOf(port));

		// Obtain the office
		DeployedOffice office = officeInput.getDeployedOffice();

		// Managed by office
		deployer.link(mos.getManagingOffice(), office);

		// Add teams for the managed object source
		deployer.link(mos.getManagedObjectTeam("listener"),
				deployer.addTeam("LISTENER", ExecutorCachedTeamSource.class.getName()));
		deployer.link(mos.getManagedObjectTeam("SSL"),
				deployer.addTeam("SSL_RUNNER", ExecutorCachedTeamSource.class.getName()));

		// Handle servicing of requests
		deployer.link(mos.getManagedObjectFlow("HANDLE_HTTP_REQUEST"), officeInput);

		// Create the input managed object
		OfficeFloorInputManagedObject input = deployer.addInputManagedObject("HTTP_" + port);
		input.addTypeQualification(null, ServerHttpConnection.class.getName());
		deployer.link(mos, input);

		// Return the input managed object
		return input;
	}

	/**
	 * Convenience method to configure both
	 * {@link HttpServerSocketManagedObjectSource} and
	 * {@link HttpsServerSocketManagedObjectSource}.
	 * 
	 * @param deployer
	 *            {@link OfficeFloorDeployer}.
	 * @param httpPort
	 *            Port to listen for HTTP requests.
	 * @param httpsPort
	 *            Port to listen for HTTPS requests.
	 * @param sslContext
	 *            {@link SslContext}. May be <code>null</code>.
	 * @param officeInput
	 *            {@link DeployedOfficeInput} to service {@link HttpRequest}
	 *            instances.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configure(OfficeFloorDeployer deployer, int httpPort, int httpsPort,
			SSLContext sslContext, DeployedOfficeInput officeInput) {

		// Add HTTP managed object source
		OfficeFloorManagedObjectSource http = deployer.addManagedObjectSource("HTTP_SOURCE",
				HttpServerSocketManagedObjectSource.class.getName());
		http.addProperty(PROPERTY_PORT, String.valueOf(httpPort));

		// Add HTTPS managed object source
		OfficeFloorManagedObjectSource https = deployer.addManagedObjectSource("HTTPS_SOURCE",
				new HttpsServerSocketManagedObjectSource(sslContext));
		https.addProperty(PROPERTY_PORT, String.valueOf(httpsPort));

		// Obtain the office
		DeployedOffice office = officeInput.getDeployedOffice();

		// Managed by office
		deployer.link(http.getManagingOffice(), office);
		deployer.link(https.getManagingOffice(), office);

		// Add team for the managed object sources
		OfficeFloorTeam team = deployer.addTeam("LISTENER", ExecutorCachedTeamSource.class.getName());

		// Configure the HTTP teams
		deployer.link(http.getManagedObjectTeam("listener"), team);

		// Configure the HTTPS teams
		deployer.link(https.getManagedObjectTeam("listener"), team);
		deployer.link(https.getManagedObjectTeam("SSL"), team);

		// Handle servicing of requests
		deployer.link(http.getManagedObjectFlow("HANDLE_HTTP_REQUEST"), officeInput);
		deployer.link(https.getManagedObjectFlow("HANDLE_HTTP_REQUEST"), officeInput);

		// Create the input managed object
		OfficeFloorInputManagedObject input = deployer.addInputManagedObject("HTTP");
		input.setBoundOfficeFloorManagedObjectSource(http);
		input.addTypeQualification(null, ServerHttpConnection.class.getName());
		deployer.link(http, input);
		deployer.link(https, input);

		// Return the input managed object
		return input;
	}

	/**
	 * {@link SSLContext}.
	 */
	private final SSLContext sslContext;

	/**
	 * Instantiate.
	 * 
	 * @param sslContext
	 *            {@link SSLContext}.
	 */
	public HttpsServerSocketManagedObjectSource(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	/**
	 * Default constructor for {@link Class} name configuration.
	 */
	public HttpsServerSocketManagedObjectSource() {
		this(null);
	}

	/*
	 * ============= AbstractServerSocketManagedObjectSource ===============
	 */

	@Override
	protected CommunicationProtocolSource createCommunicationProtocolSource() {
		return new SslCommunicationProtocol(this.sslContext, new HttpCommunicationProtocol());
	}

}