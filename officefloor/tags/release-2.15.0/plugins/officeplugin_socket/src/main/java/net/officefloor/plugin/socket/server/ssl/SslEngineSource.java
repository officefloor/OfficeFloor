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
package net.officefloor.plugin.socket.server.ssl;

import javax.net.ssl.SSLEngine;

import net.officefloor.frame.spi.source.SourceContext;

/**
 * Source for {@link SSLEngine} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SslEngineSource {

	/**
	 * Initialise this source.
	 * 
	 * @param context
	 *            {@link SourceContext}.
	 * @throws Exception
	 *             If fails to initialise (possibly because a protocol or cipher
	 *             is not supported).
	 */
	void init(SourceContext context) throws Exception;

	/**
	 * Creates a new {@link SSLEngine}.
	 * 
	 * @param peerHost
	 *            Peer host.
	 * @param peerPort
	 *            Peer port.
	 * @return New {@link SSLEngine} ready for use.
	 */
	SSLEngine createSslEngine(String peerHost, int peerPort);

}