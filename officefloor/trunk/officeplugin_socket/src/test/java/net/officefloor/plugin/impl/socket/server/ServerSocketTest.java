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

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.impl.socket.server.ServerSocketManagedObjectSource;
import net.officefloor.plugin.impl.socket.server.ServerSocketManagedObjectSource.ServerSocketHandlersEnum;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.ReadContext;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;
import net.officefloor.plugin.socket.server.spi.WriteContext;
import net.officefloor.plugin.socket.server.spi.WriteMessage;
import net.officefloor.plugin.socket.server.spi.WriteMessageListener;

/**
 * Tests the
 * {@link net.officefloor.plugin.impl.socket.server.ServerSocketManagedObjectSource}.
 * 
 * @author Daniel
 */
public class ServerSocketTest<F extends Enum<F>> extends OfficeFrameTestCase
		implements HandlerFactory<F>, ServerSocketHandler<F>,
		ConnectionHandler, WriteMessageListener, Server {

	/**
	 * First request message.
	 */
	protected static final byte[] FIRST_REQUEST_MSG = new byte[] { 1, 2, 3, 4,
			5 };

	/**
	 * Second request message.
	 */
	protected static final byte[] FIRST_RESPONSE_MSG = new byte[] { 6, 7, 8, 9,
			10 };

	/**
	 * {@link ReadMessage} to be processed by the {@link Server}.
	 */
	protected volatile ReadMessage readMessage;

	/**
	 * {@link WriteMessage} sent by the {@link Server}.
	 */
	protected volatile WriteMessage writeMessage;

	/**
	 * {@link WriteMessage} notified to be written.
	 */
	protected volatile WriteMessage writtenMessage;

	/**
	 * Ensures a message is sent and received by the server.
	 */
	@SuppressWarnings("unchecked")
	public void testSendingMessage() throws Exception {

		// Obtain the builder factory
		BuilderFactory builderFactory = OfficeFrame.getInstance()
				.getBuilderFactory();

		// Create the Office Floor
		OfficeFloorBuilder officeFloorBuilder = builderFactory
				.createOfficeFloorBuilder();

		// Register the Server Socket Managed Object
		ManagedObjectBuilder serverSocketBuilder = builderFactory
				.createManagedObjectBuilder();
		officeFloorBuilder.addManagedObject("MO", serverSocketBuilder);

		// Configure the Server Socket Managed Object
		serverSocketBuilder
				.setManagedObjectSourceClass(ServerSocketManagedObjectSource.class);
		serverSocketBuilder.addProperty("port", "12345");
		serverSocketBuilder.setManagingOffice("OFFICE");

		// Register the handler
		HandlerBuilder handlerBuilder = serverSocketBuilder
				.getManagedObjectHandlerBuilder(ServerSocketHandlersEnum.class)
				.registerHandler(ServerSocketHandlersEnum.SERVER_SOCKET_HANDLER);
		handlerBuilder.setHandlerFactory(this);

		// Register the necessary teams
		officeFloorBuilder.addTeam("ACCEPTER_TEAM", new OnePersonTeam(100));
		officeFloorBuilder.addTeam("LISTENER_TEAM", new WorkerPerTaskTeam(
				"Listener"));

		// Create the Office
		OfficeBuilder officeBuilder = builderFactory.createOfficeBuilder();
		officeBuilder.registerManagedObject("O-MO", "MO");
		officeBuilder.registerTeam("MO.serversocket.12345.Accepter.TEAM",
				"ACCEPTER_TEAM");
		officeBuilder.registerTeam("MO.serversocket.12345.Listener.TEAM",
				"LISTENER_TEAM");
		officeBuilder.addProcessManagedObject("P-MO", "O-MO");
		officeFloorBuilder.addOffice("OFFICE", officeBuilder);

		// Create the Office Floor
		OfficeFloor officeFloor = OfficeFrame.getInstance()
				.registerOfficeFloor("OF-TEST", officeFloorBuilder);

		// Open the Office Floor
		officeFloor.openOfficeFloor();

		// Open socket to Office
		Socket socket = new Socket();
		socket.connect(
				new InetSocketAddress(InetAddress.getLocalHost(), 12345), 100);

		// Write a message
		socket.getOutputStream().write(FIRST_REQUEST_MSG);

		// Wait for the response
		while (this.writeMessage == null) {
			this.sleep(1);
		}

		// Validate message being processed
		assertNotNull("ReadMessage not being processed", this.readMessage);

		// Read a response
		InputStream inputStream = socket.getInputStream();
		assertTrue("No response", (inputStream.available() > 0));

		// Ensure the write message is same as one written
		assertSame("Written message not same as write message",
				this.writeMessage, this.writtenMessage);

		// Close the Office
		officeFloor.closeOfficeFloor();
	}

	/*
	 * ====================================================================
	 * HandlerFactory
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.HandlerFactory#createHandler()
	 */
	public Handler<F> createHandler() {
		return this;
	}

	/*
	 * ====================================================================
	 * ServerSocketHandler
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ServerSocketHandler#createServer()
	 */
	public Server createServer() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ServerSocketHandler#createConnectionHandler(net.officefloor.plugin.socket.server.spi.Communication)
	 */
	public ConnectionHandler createConnectionHandler(Connection communication) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Handler#setHandlerContext(net.officefloor.frame.api.execute.HandlerContext)
	 */
	public void setHandlerContext(HandlerContext<F> context) throws Exception {
		// Do nothing with context
	}

	/*
	 * ====================================================================
	 * ConnectionHandler
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ConnectionHandler#handleRead(net.officefloor.plugin.socket.server.spi.ReadContext)
	 */
	public void handleRead(ReadContext context) {

		// Obtain the message
		ReadMessage message = context.getReadMessage();

		// Ensure data is on the message
		assertEquals("Message missing data", message.getFirstSegment()
				.getBuffer().position(), FIRST_REQUEST_MSG.length);

		// Ensure data on message is correct
		byte[] data = new byte[10];
		ByteBuffer buffer = message.getFirstSegment().getBuffer();
		buffer.flip();
		buffer.get(data, 0, buffer.limit());

		// Validate message is correct
		for (int i = 0; i < FIRST_REQUEST_MSG.length; i++) {
			assertEquals("Incorrect request message byte at " + i,
					FIRST_REQUEST_MSG[i], data[i]);
		}

		// Flag the message read
		context.setReadComplete(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ConnectionHandler#handleWrite(net.officefloor.plugin.socket.server.spi.WriteContext)
	 */
	public void handleWrite(WriteContext context) {

		// Obtain the message
		WriteMessage message = context.getWriteMessage();

		// Determine if message is written
		if (message.isWritten()) {
			// Flag message written
			this.writeMessage = message;

			// Close connection
			context.setCloseConnection(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ConnectionHandler#handleIdleConnection(net.officefloor.plugin.socket.server.spi.Connection)
	 */
	public void handleIdleConnection(Connection connection) {
		System.out.println("Connection is idle");
	}

	/*
	 * ====================================================================
	 * WriteMessageListener
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessageListener#messageWritten(net.officefloor.plugin.socket.server.spi.WriteMessage)
	 */
	public void messageWritten(WriteMessage message) {
		// Flag message written
		this.writtenMessage = message;
	}

	/*
	 * ====================================================================
	 * Server
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Server#startRequest(net.officefloor.plugin.socket.server.spi.ReadMessage)
	 */
	public void processReadMessage(ReadMessage message,
			ConnectionHandler connectionHandler) {

		// Ensure connection handler is this
		assertSame("Incorrect connection handler", this, connectionHandler);

		// Specify the read message
		this.readMessage = message;

		// Create the write message to send a response
		WriteMessage writeMessage = this.readMessage.getConnection()
				.createWriteMessage(this);

		// Populate the write message
		writeMessage.appendSegment().getBuffer().put(FIRST_RESPONSE_MSG);

		// Write message
		writeMessage.write();
	}

}
