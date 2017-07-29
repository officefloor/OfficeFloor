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
package net.officefloor.server.tcp.protocol;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.server.protocol.CommunicationProtocol;
import net.officefloor.server.protocol.CommunicationProtocolContext;
import net.officefloor.server.protocol.CommunicationProtocolSource;
import net.officefloor.server.protocol.Connection;
import net.officefloor.server.tcp.ServerTcpConnection;
import net.officefloor.server.tcp.protocol.ServiceFunction.ServiceFunctionFlows;
import net.officefloor.server.tcp.protocol.ServiceFunction.ServiceFunctionObjects;

/**
 * TCP {@link CommunicationProtocolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TcpCommunicationProtocol implements CommunicationProtocolSource, CommunicationProtocol {

	/**
	 * Send buffer size.
	 */
	private int sendBufferSize;

	/**
	 * Flow index to handle a new connection.
	 */
	private int newConnectionFlowIndex;

	/*
	 * =================== CommunicationProtocolSource ========================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
	}

	@Override
	public CommunicationProtocol createCommunicationProtocol(MetaDataContext<None, Indexed> configurationContext,
			CommunicationProtocolContext protocolContext) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = configurationContext.getManagedObjectSourceContext();

		// Obtain the send buffer size
		this.sendBufferSize = protocolContext.getSendBufferSize();

		// Specify types
		configurationContext.setManagedObjectClass(TcpConnectionHandler.class);
		configurationContext.setObjectClass(ServerTcpConnection.class);

		// Provide the function to process a new connection
		ManagedObjectFunctionBuilder<ServiceFunctionObjects, ServiceFunctionFlows> servicer = mosContext
				.addManagedFunction("servicer", new ServiceFunction());
		servicer.linkManagedObject(ServiceFunctionObjects.CONNECTION);
		this.newConnectionFlowIndex = configurationContext.addFlow(ServerTcpConnection.class).getIndex();
		mosContext.getFlow(this.newConnectionFlowIndex).linkFunction("servicer");

		// Add flow
		int serviceIndex = configurationContext.addFlow(ServerTcpConnection.class).getIndex();
		servicer.linkFlow(ServiceFunctionFlows.SERVICE, mosContext.getFlow(serviceIndex), ServerTcpConnection.class,
				false);

		// Ensure connection is cleaned up when process finished
		mosContext.getRecycleFunction(new CleanupFunction());

		// Return this as the server socket handler
		return this;
	}

	/*
	 * ======================= CommunicationProtocol ===========================
	 */

	@Override
	public TcpConnectionHandler createConnectionHandler(Connection connection,
			ManagedObjectExecuteContext<Indexed> executeContext) {
		return new TcpConnectionHandler(connection, this.sendBufferSize, executeContext, this.newConnectionFlowIndex);
	}

}