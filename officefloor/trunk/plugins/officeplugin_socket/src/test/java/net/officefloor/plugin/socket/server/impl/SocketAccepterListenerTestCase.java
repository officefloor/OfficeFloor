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
package net.officefloor.plugin.socket.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.ConnectionManager;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
import net.officefloor.plugin.socket.server.protocol.HeartBeatContext;
import net.officefloor.plugin.socket.server.protocol.ReadContext;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;

/**
 * Provides abstract functionality to test the {@link ServerSocketAccepter} and
 * {@link SocketListener}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class SocketAccepterListenerTestCase extends
		OfficeFrameTestCase implements CommunicationProtocol, ConnectionHandler {

	/**
	 * Starting port number for free pot allocation.
	 */
	private static int portStart = 13131;

	/**
	 * Obtains the next available port for testing.
	 * 
	 * @return Next available port for testing.
	 */
	public synchronized static int getNextAvailablePort() {
		return portStart++;
	}

	/**
	 * Port for {@link ServerSocketAccepter}.
	 */
	private int port;

	/**
	 * {@link SocketListener}.
	 */
	private SocketListener listener;

	/**
	 * {@link ConnectionManager}.
	 */
	private ConnectionManager connectionManager;

	/**
	 * {@link ServerSocketAccepter}.
	 */
	private ServerSocketAccepter accepter;

	/**
	 * {@link TaskContext}.
	 */
	private final MockTaskContext taskContext = new MockTaskContext();

	/**
	 * Client {@link SocketChannel}.
	 */
	private SocketChannel clientChannel;

	/**
	 * Client {@link Selector}.
	 */
	private Selector clientSelector;

	/**
	 * Client {@link SelectionKey}.
	 */
	private SelectionKey clientKey;

	/**
	 * {@link Connection}.
	 */
	private Connection connection;

	@Override
	protected void setUp() throws Exception {
		try {

			// Obtain port
			this.port = getNextAvailablePort();

			// Create the server listener
			this.listener = new SocketListener(2048, 2048);

			// Create the connection manager
			this.connectionManager = new ConnectionManagerImpl(this.listener);

			// Create the accepter
			this.accepter = new ServerSocketAccepter(new InetSocketAddress(
					this.port), this, this.connectionManager, 128);

			// Start listening and accepting
			this.connectionManager.openSocketSelectors();
			this.accepter.bindToSocket();

			// Trigger open of connection
			this.clientChannel = SocketChannel.open();
			this.clientChannel.configureBlocking(false);
			Socket clientSocket = this.clientChannel.socket();
			clientSocket.setReuseAddress(true);
			clientSocket.setSoTimeout(0); // wait forever
			clientSocket.setTcpNoDelay(false);
			this.clientChannel.connect(new InetSocketAddress(this.port));

			// Open selector for managing client connection
			this.clientSelector = Selector.open();

			// Register connection with selector to start requesting
			this.clientKey = clientChannel.register(this.clientSelector,
					SelectionKey.OP_CONNECT);

			// Run acceptance of connection
			this.taskContext.execute(this.accepter);

			// Accept connection
			this.clientSelector.select(100);

			// Finish the connection
			assertTrue("Should be read to connect",
					this.clientKey.isConnectable());
			this.clientChannel.finishConnect();

			// Connection now waiting for operations
			this.clientKey.interestOps(0);

			// Server to create connection
			this.taskContext.execute(this.listener);

		} catch (Throwable ex) {
			fail(ex);
		}
	}

	@Override
	protected void tearDown() throws Exception {

		// Release accepter resources
		this.accepter.unbindFromSocket();
		try {
			// Allow socket listener to close
			do {
				this.taskContext.execute(this.accepter);
			} while (!this.taskContext.isComplete());
		} catch (Throwable ex) {
			fail(ex);
		}

		// Release connection resources
		this.connectionManager.closeSocketSelectors();
		try {
			// Allow socket listener to close
			do {
				this.taskContext.execute(this.listener);
			} while (!this.taskContext.isComplete());
		} catch (Throwable ex) {
			fail(ex);
		}

		// Release client resources
		this.clientChannel.close();
		this.clientKey.cancel();
		this.clientSelector.select(100); // all close of connection
		this.clientSelector.close();
	}

	/**
	 * Obtains the client {@link SocketChannel}.
	 * 
	 * @return Client {@link SocketChannel}.
	 */
	protected SocketChannel getClientChannel() {
		return this.clientChannel;
	}

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 */
	protected Connection getServerConnection() {
		return this.connection;
	}

	/**
	 * Writes data to the server.
	 * 
	 * @param data
	 *            Data to write to the server.
	 */
	protected void writeDataFromClientToServer(String data) {
		try {

			// Write the data to the socket
			this.clientChannel.write(ByteBuffer.wrap(data.getBytes()));

		} catch (Exception ex) {
			fail(ex);
		}
	}

	/**
	 * Writes data to the client.
	 * 
	 * @param data
	 *            Data to write to the client.
	 */
	protected void writeDataFromServerToClient(String data) {

		// Obtain the content
		byte[] bytes = data.getBytes();

		// Write the data
		WriteBuffer buffer = this.connection.createWriteBuffer(bytes,
				data.length());
		this.connection.writeData(new WriteBuffer[] { buffer });
	}

	/**
	 * Data read by {@link SocketListener}.
	 */
	private byte[] readData = null;

	/**
	 * Runs {@link SocketListener}.
	 * 
	 * @return Read data by the {@link SocketListener}. <code>null</code> if no
	 *         data read.
	 */
	protected String runServerSelect() {

		// Reset the read data
		this.readData = null;

		// Execute the listener
		try {
			this.taskContext.execute(this.listener);
		} catch (Throwable ex) {
			fail(ex);
		}

		// Return the read data
		return this.readData == null ? null : new String(this.readData);
	}

	/**
	 * Runs client select to receive data from server.
	 * 
	 * @param expectedData
	 *            Expected data to be received from the server.
	 */
	protected void runClientSelect(String expectedData) {
		byte[] expectedBytes = expectedData.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(expectedBytes.length);
		byte[] actualData;
		try {

			// Read the data from the server
			this.clientKey.interestOps(SelectionKey.OP_READ);
			this.clientSelector.select(100);
			this.clientKey.interestOps(0);

			// Read the data
			assertEquals("Incorrect number of bytes", expectedBytes.length,
					this.clientChannel.read(buffer));
			actualData = new byte[expectedBytes.length];

		} catch (Throwable ex) {
			throw fail(ex);
		}

		// Ensure correct data received
		buffer.flip();
		buffer.get(actualData);
		assertEquals("Incorrect data received", expectedData, new String(
				actualData));
	}

	/**
	 * Have the server close the connection.
	 */
	protected void serverCloseConnection() {
		this.connection.close();
	}

	/*
	 * ===================== CommunicationProtocol ======================
	 */

	@Override
	public void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<Indexed> executeContext) {
		fail("Should not be invoked");
	}

	@Override
	public SocketAccepterListenerTestCase createConnectionHandler(
			Connection connection) {
		this.connection = connection;
		return this;
	}

	/*
	 * ===================== ConnectionHandler ==========================
	 */

	@Override
	public void handleRead(ReadContext context) throws IOException {
		this.readData = context.getData();
	}

	@Override
	public void handleHeartbeat(HeartBeatContext context) throws IOException {
		// TODO implement ConnectionHandler.handleHeartbeat
		throw new UnsupportedOperationException(
				"TODO implement ConnectionHandler.handleHeartbeat");
	}

}