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

import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
import net.officefloor.plugin.socket.server.protocol.ReadContext;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;

/**
 * Provides abstract functionality to test the {@link ServerSocketAccepter} and
 * {@link SocketListener}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class SocketAccepterListenerTestCase extends AbstractClientServerTestCase
		implements CommunicationProtocolSource, CommunicationProtocol, ConnectionHandler {

	/**
	 * {@link Connection}.
	 */
	private Connection connection;

	/**
	 * Data read by {@link SocketListener}.
	 */
	private byte[] readData = null;

	/**
	 * Writes data to the client.
	 * 
	 * @param data
	 *            Data to write to the client.
	 */
	protected void writeDataFromServerToClient(String data) throws IOException {
		this.writeDataFromServerToClient(data.getBytes());
	}

	/**
	 * Writes data to the client.
	 * 
	 * @param data
	 *            Data to write to the client.
	 */
	protected void writeDataFromServerToClient(byte[] data) throws IOException {

		// Write the data
		WriteBuffer buffer = this.connection.createWriteBuffer(data, data.length);
		this.connection.writeData(new WriteBuffer[] { buffer });
	}

	/**
	 * Writes data to the client.
	 * 
	 * @param buffer
	 *            Data to write to the client.
	 */
	protected void writeDataFromServerToClient(ByteBuffer buffer) throws IOException {

		// Write the data
		WriteBuffer writeBuffer = this.connection.createWriteBuffer(buffer);
		this.connection.writeData(new WriteBuffer[] { writeBuffer });
	}

	/**
	 * Validates the data is received by the server.
	 * 
	 * @param expectedData
	 *            Expected data to be received.
	 */
	protected void assertServerReceivedData(String expectedData) {

		// Reset the read data
		this.readData = null;

		// Execute the listener
		this.runServerSelect();

		// Obtain the data
		String data = (this.readData == null) ? null : new String(this.readData);
		assertEquals("Incorrect data received by the server", expectedData, data);
	}

	/**
	 * Obtains the server {@link Connection}.
	 * 
	 * @return Server {@link Connection}.
	 */
	protected Connection getServerSideConnection() {
		return this.connection;
	}

	/*
	 * ====================== AbstractClientServerTestCase =================
	 */

	@Override
	protected CommunicationProtocolSource getCommunicationProtocolSource() {
		return this;
	}

	@Override
	protected void handleInvokeProcess(Object parameter, ManagedObject managedObject, FlowCallback callback) {
		fail("Process should not be invoked");
	}

	/*
	 * ============== CommunicationProtocolSource =============================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	public CommunicationProtocol createCommunicationProtocol(MetaDataContext<None, Indexed> configurationContext,
			CommunicationProtocolContext protocolContext) throws Exception {
		return this;
	}

	/*
	 * ===================== CommunicationProtocol ======================
	 */

	@Override
	public ConnectionHandler createConnectionHandler(Connection connection,
			ManagedObjectExecuteContext<Indexed> executeContext) {
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

}