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
package net.officefloor.plugin.socket.server.tcp.source;

import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.plugin.socket.server.ssl.OfficeFloorDefaultSslEngineSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;
import net.officefloor.plugin.socket.server.tcp.protocol.TcpCommunicationProtocol;

/**
 * Tests the {@link SecureTcpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecureTcpServerTest extends AbstractTcpServerTestCase {

	/*
	 * ================ AbstractTcpServerTestCase ===========================
	 */

	@Override
	protected void registerManagedObjectSource(int port,
			String managedObjectName, String workName, String taskName) {

		// Obtain the office name and builder
		String officeName = this.getOfficeName();

		// Register the Server Socket Managed Object
		ManagedObjectBuilder<Indexed> serverSocketBuilder = this
				.constructManagedObject(managedObjectName,
						SecureTcpServerSocketManagedObjectSource.class);
		serverSocketBuilder.addProperty(
				TcpServerSocketManagedObjectSource.PROPERTY_PORT,
				String.valueOf(port));
		serverSocketBuilder.addProperty(
				TcpCommunicationProtocol.PROPERTY_MAXIMUM_IDLE_TIME,
				String.valueOf(10000));
		serverSocketBuilder.addProperty(
				SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE,
				OfficeFloorDefaultSslEngineSource.class.getName());
		serverSocketBuilder.setTimeout(10000);

		// Register the necessary teams for socket listening
		this.constructManagedObjectSourceTeam(managedObjectName, "accepter",
				MockTeamSource.createOnePersonTeam("accepter"));
		this.constructManagedObjectSourceTeam(managedObjectName, "listener",
				MockTeamSource.createWorkerPerTaskTeam("listener"));
		this.constructManagedObjectSourceTeam(managedObjectName, "cleanup",
				MockTeamSource.createOnePersonTeam("cleanup"));
		this.constructManagedObjectSourceTeam(managedObjectName, "SSL_TASKS",
				MockTeamSource.createOnePersonTeam("SSL_TASKS"));

		// Have server socket managed by office
		ManagingOfficeBuilder<Indexed> managingOfficeBuilder = serverSocketBuilder
				.setManagingOffice(officeName);

		// Hook in work of test
		managingOfficeBuilder.setInputManagedObjectName(managedObjectName);
		managingOfficeBuilder.linkProcess(0, workName, taskName);
	}

	@Override
	protected Socket createClientSocket(InetAddress address, int port)
			throws Exception {

		// Create the secure connected socket
		SSLContext context = OfficeFloorDefaultSslEngineSource
				.createClientSslContext(null);
		SSLSocketFactory socketFactory = context.getSocketFactory();
		Socket socket = socketFactory.createSocket(address, port);

		// Return the connected socket
		return socket;
	}

}