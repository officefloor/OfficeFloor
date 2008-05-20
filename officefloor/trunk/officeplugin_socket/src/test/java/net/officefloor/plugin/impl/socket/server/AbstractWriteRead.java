/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.impl.socket.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.impl.socket.server.messagesegment.DirectBufferMessageSegmentPool;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;

/**
 * Abstract {@link TestCase} for setting up write/read testing.
 * 
 * @author Daniel
 */
public abstract class AbstractWriteRead extends OfficeFrameTestCase {

	/**
	 * Test {@link ConnectionHandler}.
	 */
	protected final MockConnectionHandler connectionHandler;

	/**
	 * Test {@link SelectionKey}.
	 */
	protected final MockSelectionKey selectionKey;

	/**
	 * Test {@link NonblockingSocketChannel}.
	 */
	protected final MockSocketChannel socketChannel;

	/**
	 * Test {@link Selector}.
	 */
	protected final MockSelector selector;

	/**
	 * {@link ServerSocketManagedObjectSource}.
	 */
	protected final ServerSocketManagedObjectSource mos;

	/**
	 * {@link SocketListener}.
	 */
	protected final SocketListener socketListener;

	/**
	 * {@link ConnectionImpl}.
	 */
	protected final ConnectionImpl<Indexed> connection;

	/**
	 * {@link TaskContext}.
	 */
	private final TaskContext<Object, ConnectionManager, None, Indexed> taskContext;

	/**
	 * Initiate.
	 */
	protected AbstractWriteRead() {
		this.connectionHandler = new MockConnectionHandler();
		this.selectionKey = new MockSelectionKey();
		this.socketChannel = new MockSocketChannel(this.selectionKey);
		this.selector = new MockSelector(this.selectionKey);
		this.mos = new ServerSocketManagedObjectSource(new SelectorFactory() {
			@Override
			public Selector createSelector() throws IOException {
				return AbstractWriteRead.this.selector;
			}
		});
		this.mos.server = new MockServer();
		this.socketListener = new SocketListener(this.mos, 1);
		this.connection = new ConnectionImpl<Indexed>(this.socketChannel,
				new MockServerSocketHandler(this.connectionHandler), 10,
				new DirectBufferMessageSegmentPool(100));
		this.taskContext = new MockTaskContext(this.connection);
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
		int bytesRead = this.connection.read(buffer, 0, buffer.length);

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
		this.connection.write(data, 0, data.length);
	}

	/**
	 * Flushes the {@link Connection}.
	 */
	protected void connectionFlush() throws IOException {
		// Flush the connection
		this.connection.flush();
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
