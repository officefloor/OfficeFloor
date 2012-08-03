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
package net.officefloor.plugin.socket.ssl.protocol;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.junit.Ignore;

import net.officefloor.plugin.socket.server.impl.SimpleClientServerTest;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;

/**
 * Secure {@link SimpleClientServerTest}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO fix up after HTTP")
public class SecureSimpleClientServerTest extends SimpleClientServerTest {

	@Override
	protected CommunicationProtocolSource getCommunicationProtocolSource() {
		return new SslCommunicationProtocol(this);
	}

	// TODO override client methods to provide secure communication

	public void securingHelpfulCode() throws Exception {

		// Obtain the SSL Context
		SSLContext sslContext = SSLContext.getDefault();

		// Addresses
		InetSocketAddress serverAddress = new InetSocketAddress("server", 443);

		// Create the client side of connection
		SSLEngine clientEngine = sslContext.createSSLEngine(
				serverAddress.getHostName(), serverAddress.getPort());
		clientEngine.setUseClientMode(true);
		clientEngine.setEnabledCipherSuites(clientEngine
				.getSupportedCipherSuites());
		// this.client = new SslConnectionImpl(new Object(), clientAddress,
		// serverAddress,
		// this.serverToClientStream.getInputBufferStream(),
		// this.clientToServerStream.getOutputBufferStream(),
		// clientEngine, squirtFactory, this, this);

	}

}