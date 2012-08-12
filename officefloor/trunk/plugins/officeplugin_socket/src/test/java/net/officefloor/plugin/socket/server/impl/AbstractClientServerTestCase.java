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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.ConnectionManager;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;

/**
 * Functionality for testing server/client.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractClientServerTestCase extends OfficeFrameTestCase
		implements CommunicationProtocolContext,
		ManagedObjectExecuteContext<Indexed> {

	/**
	 * Port for {@link ServerSocketAccepter}.
	 */
	private int port;

	/**
	 * Send buffer size.
	 */
	private int sendBufferSize;

	/**
	 * {@link SocketListener}.
	 */
	private SocketListener listener;

	/**
	 * {@link ConnectionManager}.
	 */
	private ConnectionManagerImpl connectionManager;

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
	 * Read {@link ByteBuffer}.
	 */
	private ByteBuffer readBuffer;

	/**
	 * Obtains the {@link CommunicationProtocolSource}.
	 * 
	 * @return {@link CommunicationProtocolSource}.
	 */
	protected abstract CommunicationProtocolSource getCommunicationProtocolSource();

	/**
	 * Allows the test to be notified of {@link ProcessState} invocation.
	 * 
	 * @param parameter
	 *            Parameter.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 */
	protected abstract void handleInvokeProcess(Object parameter,
			ManagedObject managedObject, EscalationHandler escalationHandler);

	@Override
	protected void setUp() throws Exception {
		try {

			// TODO remove
			System.out.println("======= TEST: " + this.getName() + " =======");

			// Obtain port
			this.port = MockHttpServer.getAvailablePort();

			// Obtain the socket buffer send size
			Socket socket = new Socket();
			this.sendBufferSize = socket.getSendBufferSize();
			int receiveBufferSize = socket.getReceiveBufferSize();
			socket.close();

			// Create the read buffer
			this.readBuffer = ByteBuffer.allocate(receiveBufferSize);

			// Create the server listener
			this.listener = new SocketListener(this.sendBufferSize,
					receiveBufferSize);

			// Create the connection manager
			this.connectionManager = new ConnectionManagerImpl(3000,
					this.listener);

			// Obtain the communication protocol source
			CommunicationProtocolSource source = this
					.getCommunicationProtocolSource();

			// Create the managed object source context
			MetaDataContext<None, Indexed> configurationContext = ManagedObjectLoaderUtil
					.createMetaDataContext(None.class, Indexed.class);

			// Create the communication protocol
			CommunicationProtocol protocol = source
					.createCommunicationProtocol(configurationContext, this);
			protocol.setManagedObjectExecuteContext(this);

			// Create the accepter
			this.accepter = new ServerSocketAccepter(new InetSocketAddress(
					this.port), protocol, this.connectionManager, 128);

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
	 * Closes the client.
	 */
	protected void closeClient() throws IOException {
		this.getClientChannel().close();
	}

	/**
	 * Writes data to the server.
	 * 
	 * @param data
	 *            Data to write to the server.
	 */
	protected void writeDataFromClientToServer(String data) {
		this.writeDataFromClientToServer(data.getBytes());
	}

	/**
	 * Writes data to the server.
	 * 
	 * @param data
	 *            Data to write to the server.
	 */
	protected void writeDataFromClientToServer(byte[] data) {
		try {

			// Write the data to the socket
			int bytesWritten = this.clientChannel.write(ByteBuffer.wrap(data));
			assertEquals("Not all bytes written from client to server",
					data.length, bytesWritten);

		} catch (Exception ex) {
			fail(ex);
		}
	}

	/**
	 * Runs {@link SocketListener}.
	 */
	protected void runServerSelect() {

		// Execute the listener
		try {
			this.taskContext.execute(this.listener);
		} catch (Throwable ex) {
			fail(ex);
		}
	}

	/**
	 * Runs select for the client.
	 */
	protected void runClientSelect() {

		// Run select for the client
		try {
			this.clientKey.interestOps(SelectionKey.OP_READ);
			this.clientSelector.select(100);
			this.clientKey.interestOps(0);
		} catch (Throwable ex) {
			fail(ex);
		}
	}

	/**
	 * Runs select now for the client.
	 */
	protected void runClientSelectNow() {

		// Run select for the client
		try {
			this.clientKey.interestOps(SelectionKey.OP_READ);
			this.clientSelector.selectNow();
			this.clientKey.interestOps(0);
		} catch (Throwable ex) {
			fail(ex);
		}
	}

	/**
	 * Runs the heart beat for the {@link SocketListener}.
	 */
	protected void runServerHeartBeat() {

		// Execute the connection manager for the heart beat
		try {
			this.taskContext.execute(this.connectionManager);
		} catch (Throwable ex) {
			fail(ex);
		}
	}

	/**
	 * Runs client select to receive data from server and validate it.
	 * 
	 * @param expectedData
	 *            Expected data to be received from the server.
	 */
	protected void assertClientReceivedData(String expectedData) {
		this.assertClientReceivedData(expectedData.getBytes());
	}

	/**
	 * Runs client select to receive data from server and validate it.
	 * 
	 * @param expectedBytes
	 *            Expected data to be received from the server.
	 */
	protected void assertClientReceivedData(byte[] expectedBytes) {

		// Receive the bytes from the server
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		assertEquals("Incorrect number of bytes", (expectedBytes == null ? -1
				: expectedBytes.length), this.writeClientReceivedData(bytes));

		// Ensure correct data received
		assertEquals("Incorrect data received", new String(
				expectedBytes == null ? new byte[0] : expectedBytes),
				new String(bytes.toByteArray()));
	}

	/**
	 * Writes the client received data to the {@link OutputStream}.
	 * 
	 * @param outputStream
	 *            {@link OutputStream} to receive the client data.
	 * @return Number of bytes read or <code>-1</code> if end of stream.
	 */
	protected int writeClientReceivedData(OutputStream outputStream) {

		// Reset the read buffer
		this.readBuffer.position(0);
		this.readBuffer.limit(this.readBuffer.capacity());

		try {

			// Run client select now
			this.runClientSelectNow();

			// Read the data
			int bytesRead = this.clientChannel.read(this.readBuffer);
			this.readBuffer.flip();
			byte[] data = new byte[this.readBuffer.remaining()];
			this.readBuffer.get(data);

			// Write the data to the output stream
			outputStream.write(data);

			// Indicate the number of bytes read
			return bytesRead;

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	/*
	 * =================== CommunicationProtocolContext =====================
	 */

	@Override
	public int getSendBufferSize() {
		return this.sendBufferSize;
	}

	@Override
	public Charset getDefaultCharset() {
		return UsAsciiUtil.US_ASCII;
	}

	/*
	 * ===================== ManagedObjectExecuteContext ======================
	 */

	@Override
	public ProcessFuture invokeProcess(Indexed key, Object parameter,
			ManagedObject managedObject, long delay) {
		this.handleInvokeProcess(parameter, managedObject, null);
		return null;
	}

	@Override
	public ProcessFuture invokeProcess(int flowIndex, Object parameter,
			ManagedObject managedObject, long delay) {
		this.handleInvokeProcess(parameter, managedObject, null);
		return null;
	}

	@Override
	public ProcessFuture invokeProcess(Indexed key, Object parameter,
			ManagedObject managedObject, long delay,
			EscalationHandler escalationHandler) {
		this.handleInvokeProcess(parameter, managedObject, escalationHandler);
		return null;
	}

	@Override
	public ProcessFuture invokeProcess(int flowIndex, Object parameter,
			ManagedObject managedObject, long delay,
			EscalationHandler escalationHandler) {
		this.handleInvokeProcess(parameter, managedObject, escalationHandler);
		return null;
	}

}