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

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.socket.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;
import net.officefloor.plugin.socket.server.tcp.protocol.TcpCommunicationProtocol;

/**
 * {@link ManagedObjectSource} providing a {@link ServerTcpConnection} that is
 * wrapped with a {@link SslCommunicationProtocol}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecureTcpServerSocketManagedObjectSource extends AbstractServerSocketManagedObjectSource {

	/*
	 * ============== AbstractServerSocketManagedObjectSource ===============
	 */

	@Override
	protected CommunicationProtocolSource createCommunicationProtocolSource() {
		return new SslCommunicationProtocol(new TcpCommunicationProtocol());
	}

}