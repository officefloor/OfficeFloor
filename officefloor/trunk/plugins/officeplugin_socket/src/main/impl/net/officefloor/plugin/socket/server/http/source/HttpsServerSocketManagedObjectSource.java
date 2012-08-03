/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpCommunicationProtocol;
import net.officefloor.plugin.socket.server.http.protocol.HttpConnectionHandler;
import net.officefloor.plugin.socket.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;
import net.officefloor.plugin.socket.server.ssl.protocol.SslConnectionHandler;

/**
 * {@link ManagedObjectSource} for a secure {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpsServerSocketManagedObjectSource extends
		AbstractServerSocketManagedObjectSource
		implements
		ManagedObjectSourceService<None, Indexed, HttpsServerSocketManagedObjectSource> {

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