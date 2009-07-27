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
package net.officefloor.plugin.socket.server.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.IdleContext;
import net.officefloor.plugin.socket.server.ReadContext;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.socket.server.WriteContext;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * Tests the {@link AbstractServerSocketManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class ServerSocketTest extends AbstractOfficeConstructTestCase implements
		ServerSocketHandler<Indexed, ConnectionHandler>,
		Server<Indexed, ConnectionHandler>, ConnectionHandler {

	/**
	 * Request message.
	 */
	private static final byte[] REQUEST_MSG = new byte[] { 1, 2, 3, 4, 5 };

	/**
	 * Response message.
	 */
	private static final byte[] RESPONSE_MSG = new byte[] { 6, 7, 8, 9, 10 };

	/**
	 * Current instance executing.
	 */
	private static ServerSocketTest INSTANCE;

	/**
	 * {@link Connection} for the {@link ConnectionHandler}.
	 */
	private Connection connection;

	/**
	 * Initiate and make current instance available.
	 */
	public ServerSocketTest() {
		INSTANCE = this;
	}

	/**
	 * Ensures a message is sent and received by the server.
	 */
	@SuppressWarnings("unchecked")
	public void testSendingMessage() throws Exception {

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Register the Server Socket Managed Object
		ManagedObjectBuilder serverSocketBuilder = this.constructManagedObject(
				"MO", MockServerSocketManagedObjectSource.class, officeName);
		serverSocketBuilder.addProperty(
				AbstractServerSocketManagedObjectSource.PROPERTY_PORT, "12345");
		serverSocketBuilder.addProperty(
				AbstractServerSocketManagedObjectSource.PROPERTY_BUFFER_SIZE,
				"16");
		serverSocketBuilder.setDefaultTimeout(3000);

		// Register the necessary teams
		this.constructTeam("ACCEPTER_TEAM", new OnePersonTeam(100));
		officeBuilder.registerTeam("of-MO.accepter", "of-ACCEPTER_TEAM");
		this.constructTeam("LISTENER_TEAM", new WorkerPerTaskTeam("Listener"));
		officeBuilder.registerTeam("of-MO.listener", "of-LISTENER_TEAM");

		// Create and open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Open socket to Office
		Socket socket = new Socket();
		socket.connect(
				new InetSocketAddress(InetAddress.getLocalHost(), 12345), 100);

		// Write a message
		socket.getOutputStream().write(REQUEST_MSG);

		// Read the response
		byte[] buffer = new byte[RESPONSE_MSG.length];
		socket.getInputStream().read(buffer);
		for (int i = 0; i < buffer.length; i++) {
			assertEquals("Incorrect response byte " + i, RESPONSE_MSG[i],
					buffer[i]);
		}

		// Ensure the connection is closed
		assertEquals("Connection should be closed", -1, socket.getInputStream()
				.read());

		// Close the Office
		officeFloor.closeOfficeFloor();
	}

	/*
	 * =================== ServerSocketHandler ===========================
	 */

	@Override
	public Server<Indexed, ConnectionHandler> createServer() {
		return this;
	}

	@Override
	public ConnectionHandler createConnectionHandler(Connection connection) {
		this.connection = connection;
		return this;
	}

	/*
	 * ================== ConnectionHandler ================================
	 */

	@Override
	public void handleRead(ReadContext context) throws IOException {

		// Obtain the data (for small amount should all be available)
		byte[] data = new byte[REQUEST_MSG.length];
		int readSize = context.getInputBufferStream().getBrowseStream().read(
				data);
		assertEquals("Message missing data", readSize, REQUEST_MSG.length);

		// Validate message is correct
		for (int i = 0; i < REQUEST_MSG.length; i++) {
			assertEquals("Incorrect request message byte at " + i,
					REQUEST_MSG[i], data[i]);
		}

		// Process the request
		context.processRequest(null);
	}

	@Override
	public void handleWrite(WriteContext context) {
		// Message being written so close connection (once done)
		context.setCloseConnection(true);
	}

	@Override
	public void handleIdleConnection(IdleContext context) {
		this.printMessage("Connection is idle");
	}

	/*
	 * ==================== Server ==========================================
	 */

	@Override
	public void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<Indexed> executeContext) {
		// Do nothing
	}

	@Override
	public void processRequest(ConnectionHandler connectionHandler,
			Object attachment) throws IOException {

		// Ensure inputs correct
		assertSame("Incorrect connection handler", this, connectionHandler);
		assertNull("Should not have attachment", attachment);

		// Ensure have the request content
		byte[] data = new byte[REQUEST_MSG.length];
		int readSize = this.connection.getInputBufferStream().read(data);
		assertEquals("Message missing data", readSize, REQUEST_MSG.length);
		for (int i = 0; i < REQUEST_MSG.length; i++) {
			assertEquals("Incorrect request message byte at " + i,
					REQUEST_MSG[i], data[i]);
		}

		// Write the response
		this.connection.getOutputBufferStream().write(RESPONSE_MSG);
	}

	/**
	 * Mock {@link AbstractServerSocketManagedObjectSource}.
	 */
	@TestSource
	public static class MockServerSocketManagedObjectSource extends
			AbstractServerSocketManagedObjectSource<Indexed, ConnectionHandler> {

		/*
		 * ========== AbstractServerSocketManagedObjectSource ============
		 */

		@Override
		protected ServerSocketHandler<Indexed, ConnectionHandler> createServerSocketHandler(
				MetaDataContext<None, Indexed> context,
				BufferSquirtFactory bufferSquirtFactory) throws Exception {
			context.setObjectClass(Object.class);
			return ServerSocketTest.INSTANCE;
		}
	}

}