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
package net.officefloor.plugin.socket.ssl.protocol;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import net.officefloor.plugin.socket.server.ssl.SslConnection;
import net.officefloor.plugin.socket.server.ssl.protocol.SslConnectionImpl;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.squirtfactory.HeapByteBufferSquirtFactory;

/**
 * Tests the {@link SslConnection}.
 *
 * @author Daniel Sagenschneider
 */
public class SslConnectionTest extends AbstractSslConnectionTestCase {

	/**
	 * Stream of data from client to server.
	 */
	private BufferStream clientToServerStream;

	/**
	 * Stream of data from server to client;
	 */
	private BufferStream serverToClientStream;

	/*
	 * ================== AbstractSslConnectionTest ============================
	 */

	@Override
	protected void setUp() throws Exception {

		// Create the connection streams
		BufferSquirtFactory squirtFactory = new HeapByteBufferSquirtFactory(
				1024);
		this.clientToServerStream = new BufferStreamImpl(squirtFactory);
		this.serverToClientStream = new BufferStreamImpl(squirtFactory);

		// Obtain the SSL Context
		SSLContext sslContext = SSLContext.getDefault();

		// Create the server side of connection
		SSLEngine serverEngine = sslContext.createSSLEngine("client", 10000);
		serverEngine.setUseClientMode(false);
		serverEngine.setEnabledCipherSuites(serverEngine
				.getSupportedCipherSuites());
		this.server = new SslConnectionImpl(new Object(), null, 10000,
				this.clientToServerStream.getInputBufferStream(),
				this.serverToClientStream.getOutputBufferStream(),
				serverEngine, squirtFactory, this, this);

		// Create the client side of connection
		SSLEngine clientEngine = sslContext.createSSLEngine("server", 443);
		clientEngine.setUseClientMode(true);
		clientEngine.setEnabledCipherSuites(clientEngine
				.getSupportedCipherSuites());
		this.client = new SslConnectionImpl(new Object(), null, 443,
				this.serverToClientStream.getInputBufferStream(),
				this.clientToServerStream.getOutputBufferStream(),
				clientEngine, squirtFactory, this, this);
	}

	@Override
	protected void transferDataFromClientToServer() throws IOException {
		// Data already available, only notify server available
		this.server.processDataFromPeer();
	}

	@Override
	protected void transferDataFromServerToClient() throws IOException {
		// Data already available, only notify client available
		this.client.processDataFromPeer();
	}

}