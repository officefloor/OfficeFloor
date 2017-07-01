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
package net.officefloor.plugin.socket.server.tcp.protocol;

import java.net.ConnectException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;

/**
 * TCP {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TcpCommunicationProtocol implements CommunicationProtocolSource, CommunicationProtocol {

	/**
	 * Property to obtain the maximum idle time before the {@link Connection} is
	 * closed.
	 */
	public static final String PROPERTY_MAXIMUM_IDLE_TIME = "max.idle.time";

	/**
	 * Default time before an idle {@link Connection} is closed.
	 */
	public static final int DEFAULT_MAXIMUM_IDLE_TIME = 60;

	/**
	 * Maximum idle time before the {@link Connection} is closed.
	 */
	private long maxIdleTime;

	/**
	 * Send buffer size.
	 */
	private int sendBufferSize;

	/**
	 * Flow index to handle a new connection.
	 */
	private int newConnectionFlowIndex;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * Triggers a {@link ProcessState} to service the {@link Connection}.
	 * 
	 * @param connectionHandler
	 *            {@link TcpConnectionHandler} for the {@link ConnectException}.
	 */
	public void serviceConnection(TcpConnectionHandler connectionHandler) {
		// Invokes the process to service the connection
		this.executeContext.invokeProcess(this.newConnectionFlowIndex, connectionHandler, connectionHandler, 0, null);
	}

	/*
	 * =================== CommunicationProtocolSource ========================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_MAXIMUM_IDLE_TIME);
	}

	@Override
	public CommunicationProtocol createCommunicationProtocol(MetaDataContext<None, Indexed> configurationContext,
			CommunicationProtocolContext protocolContext) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = configurationContext.getManagedObjectSourceContext();

		// Obtain the maximum idle time
		this.maxIdleTime = Long.parseLong(
				mosContext.getProperty(PROPERTY_MAXIMUM_IDLE_TIME, String.valueOf(DEFAULT_MAXIMUM_IDLE_TIME)));

		// Obtain the send buffer size
		this.sendBufferSize = protocolContext.getSendBufferSize();

		// Specify types
		configurationContext.setManagedObjectClass(TcpConnectionHandler.class);
		configurationContext.setObjectClass(ServerTcpConnection.class);

		// Provide the flow to process a new connection
		this.newConnectionFlowIndex = configurationContext.addFlow(ServerTcpConnection.class).setLabel("NEW_CONNECTION")
				.getIndex();

		// Ensure connection is cleaned up when process finished
		mosContext.getRecycleFunction(new CleanupFunction());

		// Return this as the server socket handler
		return this;
	}

	/*
	 * ======================= CommunicationProtocol ===========================
	 */

	@Override
	public void setManagedObjectExecuteContext(ManagedObjectExecuteContext<Indexed> executeContext) {
		this.executeContext = executeContext;
	}

	@Override
	public TcpConnectionHandler createConnectionHandler(Connection connection) {
		return new TcpConnectionHandler(this, connection, this.sendBufferSize, this.maxIdleTime);
	}

}