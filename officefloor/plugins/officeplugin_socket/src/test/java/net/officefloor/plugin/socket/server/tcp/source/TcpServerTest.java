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
import java.net.InetSocketAddress;
import java.net.Socket;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.plugin.socket.server.tcp.protocol.TcpCommunicationProtocol;

/**
 * Tests the {@link TcpServerSocketManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TcpServerTest extends AbstractTcpServerTestCase {

	/*
	 * ================ AbstractTcpServerTestCase =========================
	 */

	@Override
	protected void registerManagedObjectSource(int port, String managedObjectName, String functionName) {

		// Obtain the office name and builder
		String officeName = this.getOfficeName();

		// Register the Server Socket Managed Object
		ManagedObjectBuilder<Indexed> serverSocketBuilder = this.constructManagedObject(managedObjectName,
				TcpServerSocketManagedObjectSource.class, officeName);
		serverSocketBuilder.addProperty(TcpServerSocketManagedObjectSource.PROPERTY_PORT, String.valueOf(port));
		serverSocketBuilder.addProperty(TcpCommunicationProtocol.PROPERTY_MAXIMUM_IDLE_TIME, String.valueOf(1000));
		serverSocketBuilder.setTimeout(3000);

		// Register the necessary teams for socket listening
		this.constructManagedObjectSourceTeam(managedObjectName, "accepter",
				new ExecutorCachedTeamSource().createTeam());
		this.constructManagedObjectSourceTeam(managedObjectName, "listener",
				new ExecutorCachedTeamSource().createTeam());

		// Have server socket managed by office
		ManagingOfficeBuilder<Indexed> managingOfficeBuilder = serverSocketBuilder.setManagingOffice(officeName);

		// Hook in function of test
		managingOfficeBuilder.setInputManagedObjectName(managedObjectName);
		managingOfficeBuilder.linkProcess(0, functionName);
	}

	@Override
	protected Socket createClientSocket(InetAddress address, int port) throws Exception {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), port), 5000);
		return socket;
	}

}