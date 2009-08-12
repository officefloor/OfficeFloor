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
package net.officefloor.plugin.socket.server.ssl.source;

import java.nio.channels.ServerSocketChannel;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.socket.server.CommunicationProtocol;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;

/**
 * Abstract {@link ManagedObjectSource} for a SSL {@link ServerSocketChannel}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSslSeverSocketManagedObjectSource<CH extends ConnectionHandler>
		extends AbstractServerSocketManagedObjectSource<CH> {

	/*
	 * ============ AbstractSslSeverSocketManagedObjectSource ===============
	 */

	@Override
	protected CommunicationProtocol<CH> createWrappingCommunicationProtocol(
			CommunicationProtocol<CH> communicationProtocol) {
		return new SslCommunicationProtocol<CH>(communicationProtocol);
	}

}