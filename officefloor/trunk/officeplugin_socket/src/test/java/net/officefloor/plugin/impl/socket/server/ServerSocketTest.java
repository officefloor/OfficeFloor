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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

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
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.IdleContext;
import net.officefloor.plugin.socket.server.spi.ReadContext;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;
import net.officefloor.plugin.socket.server.spi.WriteContext;
import net.officefloor.plugin.socket.server.spi.WriteMessage;
import net.officefloor.plugin.socket.server.spi.WriteMessageListener;

/**
 * Tests the {@link AbstractServerSocketManagedObjectSource}.
 * 
 * @author Daniel
 */
public class ServerSocketTest extends AbstractOfficeConstructTestCase implements
		ServerSocketHandler<Indexed>, Server<Indexed>, ConnectionHandler,
		WriteMessageListener {

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
	 * {@link ReadMessage} to be processed by the {@link Server}.
	 */
	private volatile ReadMessageImpl readMessage;

	/**
	 * {@link WriteMessage} created to write the response.
	 */
	private volatile WriteMessage writeMessage;

	/**
	 * {@link WriteMessage} notified to be written.
	 */
	private volatile WriteMessage writtenMessage;

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
				AbstractServerSocketManagedObjectSource.PROPERTY_MESSAGE_SIZE,
				"2");
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

		// Ensure the write and written message are the same
		assertEquals("Incorrect written message", this.writeMessage,
				this.writtenMessage);

		// Close the Office
		officeFloor.closeOfficeFloor();
	}

	/*
	 * =================== ServerSocketHandler ===========================
	 */

	@Override
	public Server<Indexed> createServer() {
		return this;
	}

	@Override
	public ConnectionHandler createConnectionHandler(Connection connection) {
		return this;
	}

	/*
	 * ================== ConnectionHandler ================================
	 */

	@Override
	public void handleRead(ReadContext context) {

		// Obtain the message
		ReadMessage message = context.getReadMessage();

		// Ensure data is on the message
		assertEquals("Message missing data", message.getFirstSegment()
				.getBuffer().position(), REQUEST_MSG.length);

		// Ensure data on message is correct
		byte[] data = new byte[10];
		ByteBuffer buffer = message.getFirstSegment().getBuffer();
		buffer.flip();
		buffer.get(data, 0, buffer.limit());

		// Validate message is correct
		for (int i = 0; i < REQUEST_MSG.length; i++) {
			assertEquals("Incorrect request message byte at " + i,
					REQUEST_MSG[i], data[i]);
		}

		// Flag the message read
		context.setReadComplete(true);
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
	 * ================== WriteMessageListener ============================
	 */

	@Override
	public void messageWritten(WriteMessage message) {
		// Ensure only written once
		assertNull("Message should only be written once", this.writtenMessage);

		// Flag message written
		this.writtenMessage = message;
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
	public void processReadMessage(ReadMessage message,
			ConnectionHandler connectionHandler) throws IOException {

		// Ensure connection handler
		assertSame("Incorrect connection handler", this, connectionHandler);

		// Specify the read message
		this.readMessage = (ReadMessageImpl) message;

		// Create the write message to send a response
		this.writeMessage = this.readMessage.getConnection()
				.createWriteMessage(this);
		this.writeMessage.appendSegment().getBuffer().put(RESPONSE_MSG);
		this.writeMessage.write();
	}

	/**
	 * Mock {@link AbstractServerSocketManagedObjectSource}.
	 */
	@TestSource
	public static class MockServerSocketManagedObjectSource extends
			AbstractServerSocketManagedObjectSource<Indexed> {

		/*
		 * ========== AbstractServerSocketManagedObjectSource ============
		 */

		@Override
		protected ServerSocketHandler<Indexed> createServerSocketHandler(
				MetaDataContext<None, Indexed> context) throws Exception {
			context.setObjectClass(Object.class);
			return ServerSocketTest.INSTANCE;
		}
	}

}