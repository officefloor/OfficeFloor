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
import java.util.Properties;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.ConnectionManager;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;

/**
 * Functionality for testing server/client.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractClientServerTestCase extends OfficeFrameTestCase
		implements CommunicationProtocolContext, ManagedObjectExecuteContext<Indexed> {

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
	 * {@link ManagedFunctionContext}.
	 */
	private final MockManagedFunctionContext<?, ?> managedFunctionContext = new MockManagedFunctionContext<Indexed, Indexed>();

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
	 * Allows overriding to specify the {@link SourceProperties} for the
	 * {@link CommunicationProtocolSource}.
	 * 
	 * @return Empty {@link Properties} by default.
	 */
	protected Properties getCommunicationProtocolProperties() {
		return new Properties();
	}

	/**
	 * Allows the test to be notified of {@link ProcessState} invocation.
	 * 
	 * @param parameter
	 *            Parameter.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @param callback
	 *            {@link FlowCallback}.
	 */
	protected abstract void handleInvokeProcess(Object parameter, ManagedObject managedObject, FlowCallback callback);

	@Override
	protected void setUp() throws Exception {
		try {
			// Obtain port
			this.port = HttpTestUtil.getAvailablePort();

			// Obtain the socket buffer send size
			Socket socket = new Socket();
			this.sendBufferSize = socket.getSendBufferSize();
			int receiveBufferSize = socket.getReceiveBufferSize();
			socket.close();

			// Create the read buffer
			this.readBuffer = ByteBuffer.allocate(receiveBufferSize);

			// Create the server listener
			this.listener = new SocketListener(3000, this.sendBufferSize, receiveBufferSize);

			// Create the connection manager
			this.connectionManager = new ConnectionManagerImpl(this.listener);

			// Obtain the communication protocol source
			CommunicationProtocolSource source = this.getCommunicationProtocolSource();

			// Obtain the communication protocol properties
			Properties protocolProperties = this.getCommunicationProtocolProperties();
			String[] contextProperties = new String[protocolProperties.size() * 2];
			int contextPropertyIndex = 0;
			for (String propertyName : protocolProperties.stringPropertyNames()) {
				String propertyValue = protocolProperties.getProperty(propertyName);
				contextProperties[contextPropertyIndex++] = propertyName;
				contextProperties[contextPropertyIndex++] = propertyValue;
			}

			// Create the managed object source context
			MetaDataContext<None, Indexed> configurationContext = ManagedObjectLoaderUtil
					.createMetaDataContext(None.class, Indexed.class, contextProperties);

			// Create the communication protocol
			CommunicationProtocol protocol = source.createCommunicationProtocol(configurationContext, this);
			protocol.setManagedObjectExecuteContext(this);

			// Create the accepter
			this.accepter = new ServerSocketAccepter(new InetSocketAddress(this.port), protocol, this.connectionManager,
					128);

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
			this.clientKey = clientChannel.register(this.clientSelector, SelectionKey.OP_CONNECT);

			// Run acceptance of connection
			this.managedFunctionContext.execute(this.accepter);

			// Accept connection
			this.clientSelector.select(100);

			// Finish the connection
			assertTrue("Should be read to connect", this.clientKey.isConnectable());
			this.clientChannel.finishConnect();

			// Connection now waiting for operations
			this.clientKey.interestOps(0);

			// Server to create connection
			this.managedFunctionContext.execute(this.listener);

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
				this.managedFunctionContext.execute(this.accepter);
			} while (!this.managedFunctionContext.isComplete());
		} catch (Throwable ex) {
			fail(ex);
		}

		// Release connection resources
		this.connectionManager.closeSocketSelectors();
		try {
			// Allow socket listener to close
			do {
				this.managedFunctionContext.execute(this.listener);
			} while (!this.managedFunctionContext.isComplete());
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
			assertEquals("Not all bytes written from client to server", data.length, bytesWritten);

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
			this.managedFunctionContext.execute(this.listener);
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
		// TODO heart beat to be part of normal loop
		fail("TODO implement running server heart beat");
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
		int bytesReceived = this.writeClientReceivedData(bytes);

		// Ensure correct data received
		assertEquals("Incorrect data received", new String(expectedBytes == null ? new byte[0] : expectedBytes),
				new String(bytes.toByteArray()));
		assertEquals("Incorrect number of bytes", (expectedBytes == null ? -1 : expectedBytes.length), bytesReceived);
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
		return Charset.forName(AbstractServerSocketManagedObjectSource.DEFAULT_CHARSET);
	}

	/*
	 * ===================== ManagedObjectExecuteContext ======================
	 */

	@Override
	public void invokeProcess(Indexed key, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException {
		this.handleInvokeProcess(parameter, managedObject, callback);
	}

	@Override
	public void invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException {
		this.handleInvokeProcess(parameter, managedObject, callback);
	}

}