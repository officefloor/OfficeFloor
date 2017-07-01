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
package net.officefloor.plugin.socket.server.http.source;

import net.officefloor.compile.ManagedObjectSourceService;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpCommunicationProtocol;
import net.officefloor.plugin.socket.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.ssl.SslEngineSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;

/**
 * {@link ManagedObjectSource} for a secure {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpsServerSocketManagedObjectSource extends AbstractServerSocketManagedObjectSource
		implements ManagedObjectSourceService<None, Indexed, HttpsServerSocketManagedObjectSource> {

	/**
	 * Convenience method to configure the
	 * {@link HttpsServerSocketManagedObjectSource}.
	 * 
	 * @param deployer
	 *            {@link OfficeFloorDeployer}.
	 * @param port
	 *            Port to listen for HTTPS requests.
	 * @param sslEngineSourceClass
	 *            {@link SslEngineSource} class. May be <code>null</code>.
	 * @param office
	 *            {@link DeployedOffice}.
	 * @param sectionName
	 *            Name of the {@link OfficeSection} servicing the requests.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput} on the
	 *            {@link OfficeSection} servicing the requests.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configure(OfficeFloorDeployer deployer, int port,
			Class<? extends SslEngineSource> sslEngineSourceClass, DeployedOffice office, String sectionName,
			String sectionInputName) {

		// Add this managed object source
		OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("HTTPS_SOURCE",
				HttpsServerSocketManagedObjectSource.class.getName());
		mos.addProperty(PROPERTY_PORT, String.valueOf(port));
		if (sslEngineSourceClass != null) {
			mos.addProperty(SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE, sslEngineSourceClass.getName());
		}

		// Add teams for the managed object source
		deployer.link(mos.getManagedObjectTeam("accepter"),
				deployer.addTeam("ACCEPTER", ExecutorCachedTeamSource.class.getName()));
		deployer.link(mos.getManagedObjectTeam("listener"),
				deployer.addTeam("LISTENER", ExecutorCachedTeamSource.class.getName()));
		deployer.link(mos.getManagedObjectTeam("ssl_runnable"),
				deployer.addTeam("SSL_RUNNER", ExecutorCachedTeamSource.class.getName()));

		// Handle servicing of requests
		deployer.link(mos.getManagedObjectFlow("HANDLER"),
				office.getDeployedOfficeInput(sectionName, sectionInputName));

		// Create the input managed object
		OfficeFloorInputManagedObject input = deployer.addInputManagedObject("HTTP");
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
	 * @param sslEngineSourceClass
	 *            {@link SslEngineSource} class. May be <code>null</code>.
	 * @param office
	 *            {@link DeployedOffice}.
	 * @param sectionName
	 *            Name of the {@link OfficeSection} servicing the requests.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput} on the
	 *            {@link OfficeSection} servicing the requests.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configure(OfficeFloorDeployer deployer, int httpPort, int httpsPort,
			Class<? extends SslEngineSource> sslEngineSourceClass, DeployedOffice office, String sectionName,
			String sectionInputName) {

		// Add HTTP managed object source
		OfficeFloorManagedObjectSource http = deployer.addManagedObjectSource("HTTP_SOURCE",
				HttpServerSocketManagedObjectSource.class.getName());
		http.addProperty(PROPERTY_PORT, String.valueOf(httpPort));

		// Add HTTPS managed object source
		OfficeFloorManagedObjectSource https = deployer.addManagedObjectSource("HTTPS_SOURCE",
				HttpsServerSocketManagedObjectSource.class.getName());
		https.addProperty(PROPERTY_PORT, String.valueOf(httpsPort));
		if (sslEngineSourceClass != null) {
			https.addProperty(SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE, sslEngineSourceClass.getName());
		}

		// Add teams for the managed object source
		OfficeFloorTeam accepter = deployer.addTeam("ACCEPTER", ExecutorCachedTeamSource.class.getName());
		OfficeFloorTeam listener = deployer.addTeam("LISTENER", ExecutorCachedTeamSource.class.getName());

		// Configure the HTTP teams
		deployer.link(http.getManagedObjectTeam("accepter"), accepter);
		deployer.link(http.getManagedObjectTeam("listener"), listener);

		// Configure the HTTPS teams
		deployer.link(https.getManagedObjectTeam("accepter"), accepter);
		deployer.link(https.getManagedObjectTeam("listener"), listener);
		deployer.link(https.getManagedObjectTeam("ssl_runnable"),
				deployer.addTeam("SSL_RUNNER", ExecutorCachedTeamSource.class.getName()));

		// Handle servicing of requests
		DeployedOfficeInput servicer = office.getDeployedOfficeInput(sectionName, sectionInputName);
		deployer.link(http.getManagedObjectFlow("HANDLER"), servicer);
		deployer.link(https.getManagedObjectFlow("HANDLER"), servicer);

		// Create the input managed object
		OfficeFloorInputManagedObject input = deployer.addInputManagedObject("HTTP");
		deployer.link(http, input);
		deployer.link(https, input);

		// Return the input managed object
		return input;
	}

	/*
	 * ==================== ManagedObjectSourceService ====================
	 */

	@Override
	public String getManagedObjectSourceAlias() {
		return "HTTPS_SERVER";
	}

	@Override
	public Class<HttpsServerSocketManagedObjectSource> getManagedObjectSourceClass() {
		return HttpsServerSocketManagedObjectSource.class;
	}

	/*
	 * ============= AbstractServerSocketManagedObjectSource ===============
	 */

	@Override
	protected CommunicationProtocolSource createCommunicationProtocolSource() {
		return new SslCommunicationProtocol(new HttpCommunicationProtocol());
	}

}