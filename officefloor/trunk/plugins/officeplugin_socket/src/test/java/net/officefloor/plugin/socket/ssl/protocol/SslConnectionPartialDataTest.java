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

import java.io.IOException;
import java.net.InetSocketAddress;

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
public class SslConnectionPartialDataTest extends AbstractSslConnectionTestCase {

	/**
	 * Stream of data from client to wire.
	 */
	private BufferStream clientToWireStream;

	/**
	 * Stream of data from wire to server.
	 */
	private BufferStream wireToServerStream;

	/**
	 * Stream of data from server to wire;
	 */
	private BufferStream serverToWireStream;

	/**
	 * Stream of data from wire to client.
	 */
	private BufferStream wireToClientStream;

	/*
	 * ================== AbstractSslConnectionTest ============================
	 */

	@Override
	protected void setUp() throws Exception {

		// Create the connection streams
		BufferSquirtFactory squirtFactory = new HeapByteBufferSquirtFactory(
				1024);
		this.clientToWireStream = new BufferStreamImpl(squirtFactory);
		this.wireToServerStream = new BufferStreamImpl(squirtFactory);
		this.serverToWireStream = new BufferStreamImpl(squirtFactory);
		this.wireToClientStream = new BufferStreamImpl(squirtFactory);

		// Obtain the SSL Context
		SSLContext sslContext = SSLContext.getDefault();

		// Addresses
		InetSocketAddress serverAddress = new InetSocketAddress("server", 443);
		InetSocketAddress clientAddress = new InetSocketAddress("client", 10000);

		// Create the server side of connection
		SSLEngine serverEngine = sslContext.createSSLEngine(clientAddress
				.getHostName(), clientAddress.getPort());
		serverEngine.setUseClientMode(false);
		serverEngine.setEnabledCipherSuites(serverEngine
				.getSupportedCipherSuites());
		this.server = new SslConnectionImpl(new Object(), serverAddress,
				clientAddress, this.wireToServerStream.getInputBufferStream(),
				this.serverToWireStream.getOutputBufferStream(), serverEngine,
				squirtFactory, this, this);

		// Create the client side of connection
		SSLEngine clientEngine = sslContext.createSSLEngine(serverAddress
				.getHostName(), serverAddress.getPort());
		clientEngine.setUseClientMode(true);
		clientEngine.setEnabledCipherSuites(clientEngine
				.getSupportedCipherSuites());
		this.client = new SslConnectionImpl(new Object(), clientAddress,
				serverAddress, this.wireToClientStream.getInputBufferStream(),
				this.clientToWireStream.getOutputBufferStream(), clientEngine,
				squirtFactory, this, this);
	}

	@Override
	protected void transferDataFromClientToServer() throws IOException {
		while (this.clientToWireStream.available() > 0) {

			// Transfer a byte at a time
			this.clientToWireStream.read(1, this.wireToServerStream
					.getOutputBufferStream());

			// Inform server that byte is available
			this.server.processDataFromPeer();
		}
	}

	@Override
	protected void transferDataFromServerToClient() throws IOException {
		while (this.serverToWireStream.available() > 0) {

			// Transfer a byte at a time
			this.serverToWireStream.read(1, this.wireToClientStream
					.getOutputBufferStream());

			// Inform client that byte is available
			this.client.processDataFromPeer();
		}
	}

}