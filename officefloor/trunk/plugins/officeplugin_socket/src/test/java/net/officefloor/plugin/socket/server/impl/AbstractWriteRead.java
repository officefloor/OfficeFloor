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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.stream.squirtfactory.HeapByteBufferSquirtFactory;

/**
 * Abstract {@link TestCase} for setting up write/read testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWriteRead extends OfficeFrameTestCase {

	/**
	 * Test {@link ConnectionHandler}.
	 */
	protected final MockConnectionHandler connectionHandler = new MockConnectionHandler();

	/**
	 * Test {@link SelectionKey}.
	 */
	protected final MockSelectionKey selectionKey = new MockSelectionKey();

	/**
	 * Test {@link NonblockingSocketChannel}.
	 */
	protected final MockSocketChannel socketChannel = new MockSocketChannel(
			this.selectionKey);

	/**
	 * {@link Server}.
	 */
	@SuppressWarnings("rawtypes")
	protected final Server server = new MockServer(this.connectionHandler);

	/**
	 * {@link ConnectionImpl}.
	 */
	@SuppressWarnings("unchecked")
	protected final ConnectionImpl<ConnectionHandler> connection = new ConnectionImpl<ConnectionHandler>(
			this.socketChannel, this.server,
			new HeapByteBufferSquirtFactory(64));

	/**
	 * Test {@link Selector}.
	 */
	protected final MockSelector selector = new MockSelector(this.selectionKey);

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory = new SelectorFactory() {
		@Override
		public Selector createSelector() throws IOException {
			return AbstractWriteRead.this.selector;
		}
	};

	/**
	 * {@link SocketListener}.
	 */
	protected final SocketListener<ConnectionHandler> socketListener = new SocketListener<ConnectionHandler>(
			this.selectorFactory);

	/**
	 * {@link TaskContext}.
	 */
	private final TaskContext<SocketListener<ConnectionHandler>, None, Indexed> taskContext = new MockTaskContext();

	@Override
	protected void setUp() throws Exception {
		// Ensure socket listener selector open
		this.socketListener.openSelector();

		// Associate connection to selection key
		this.selectionKey.attach(this.connection);
	}

	/**
	 * Runs the {@link SocketListener}.
	 */
	protected void runSocketListener() throws Exception {
		this.socketListener.doTask(this.taskContext);
	}

	/**
	 * Mocks inputting text from the client.
	 * 
	 * @param text
	 *            Text from the client.
	 */
	protected void inputFromClient(String text) {
		this.socketChannel.input(text);
	}

	/**
	 * Validates the read on the {@link Connection}.
	 * 
	 * @param text
	 *            Expected text.
	 */
	protected void validateConnectionRead(String text) throws IOException {

		// Obtain the buffer to read data
		byte[] buffer = new byte[text.getBytes().length];

		// Read data from the connection
		int bytesRead = this.connection.getInputBufferStream().read(buffer);

		// Obtain text of bytes read
		String actualText = new String(buffer, 0, bytesRead);

		// Validate
		assertEquals("Incorrect number of bytes read [expected: " + text
				+ ", actual: " + actualText + "]", buffer.length, bytesRead);
		assertEquals("Incorrect bytes read", text, actualText);
	}

	/**
	 * Writes data to the {@link Connection}.
	 * 
	 * @param text
	 *            Text to be written to the {@link Connection}.
	 */
	protected void connectionWrite(String text) throws IOException {

		// Obtain the data
		byte[] data = text.getBytes();

		// Write the data to the connection
		this.connection.getOutputBufferStream().write(data);
	}

	/**
	 * Flags to close {@link Connection} by the {@link ConnectionHandler}.
	 */
	protected void flagCloseConnection() throws IOException {
		this.connectionHandler.flagClose();
	}

	/**
	 * Validates the data is written to the client.
	 * 
	 * @param text
	 *            Text expected to be written to the client.
	 */
	protected void validateOutputToClient(String text) {
		this.socketChannel.validateOutput(text);
	}

}