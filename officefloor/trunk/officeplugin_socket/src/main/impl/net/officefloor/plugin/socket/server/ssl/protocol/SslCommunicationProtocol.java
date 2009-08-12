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
package net.officefloor.plugin.socket.server.ssl.protocol;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.plugin.socket.server.CommunicationProtocol;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.socket.server.ssl.SslTaskExecutor;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * SSL {@link CommunicationProtocol} that wraps another
 * {@link CommunicationProtocol}.
 *
 * @author Daniel Sagenschneider
 */
public class SslCommunicationProtocol<CH extends ConnectionHandler> implements
		CommunicationProtocol<CH>, ServerSocketHandler<CH>, Server<CH>,
		SslTaskExecutor {

	/**
	 * Wrapped {@link CommunicationProtocol}.
	 */
	private final CommunicationProtocol<CH> wrappedCommunicationProtocol;

	/**
	 * Wrapped {@link ServerSocketHandler}.
	 */
	private ServerSocketHandler<CH> wrappedServerSocketHandler;

	/**
	 * Wrapped {@link Server}.
	 */
	private Server<CH> wrappedServer;

	/**
	 * {@link SSLContext}.
	 */
	private SSLContext sslContext;

	/**
	 * {@link BufferSquirtFactory}.
	 */
	private BufferSquirtFactory bufferSquirtFactory;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * {@link Flow} index to run the SSL tasks.
	 */
	private int sslTaskFlowIndex;

	/**
	 * Initiate.
	 *
	 * @param wrappedCommunicationProtocol
	 *            {@link CommunicationProtocol} to be wrapped with this SSL
	 *            {@link CommunicationProtocol}.
	 */
	public SslCommunicationProtocol(
			CommunicationProtocol<CH> wrappedCommunicationProtocol) {
		this.wrappedCommunicationProtocol = wrappedCommunicationProtocol;
	}

	/*
	 * ====================== CommunicationProtocol ============================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		// TODO load SSL specification

		// Load wrapped communication specification
		this.wrappedCommunicationProtocol.loadSpecification(context);
	}

	@Override
	public ServerSocketHandler<CH> createServerSocketHandler(
			MetaDataContext<None, Indexed> context,
			BufferSquirtFactory bufferSquirtFactory) throws Exception {

		// Create the server socket handler to wrap
		this.wrappedServerSocketHandler = this.wrappedCommunicationProtocol
				.createServerSocketHandler(context, bufferSquirtFactory);

		// Create the SSL context
		// TODO consider allowing specifying specific SSL context
		this.sslContext = SSLContext.getDefault();

		// Store the buffer squirt factory for creating SSL connections
		this.bufferSquirtFactory = bufferSquirtFactory;

		// Create the flow to execute the SSL tasks
		this.sslTaskFlowIndex = context.addFlow(Runnable.class).setLabel(
				"SSL_TASKS").getIndex();

		// Return this wrapping the server socket handler
		return this;
	}

	/*
	 * ====================== ServerSocketHandler ============================
	 */

	@Override
	public Server<CH> createServer() {

		// Create the server to wrap
		this.wrappedServer = this.wrappedServerSocketHandler.createServer();

		// Return this wrapping the server
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public CH createConnectionHandler(Connection connection) {

		// Create the server SSL engine
		// TODO provide client host and port on connection
		SSLEngine engine = this.sslContext.createSSLEngine();
		engine.setUseClientMode(false);

		// Create the SSL connection wrapping the connection
		SslConnectionHandler<CH> connectionHandler = new SslConnectionHandler<CH>(
				connection, engine, this.bufferSquirtFactory, this,
				this.wrappedServerSocketHandler);

		// Return the SSL connection handler
		return (CH) connectionHandler;
	}

	/*
	 * ======================= Server ========================================
	 */

	@Override
	public void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<Indexed> executeContext) {
		// Provide execution context to wrapped server
		this.wrappedServer.setManagedObjectExecuteContext(executeContext);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void processRequest(CH connectionHandler, Object attachment)
			throws IOException {

		// Downcast connection handler to SSL connection handler
		SslConnectionHandler<CH> sslConnectionHandler = (SslConnectionHandler<CH>) connectionHandler;

		// Obtain the wrapped connection handler
		CH wrappedConnectionHandler = sslConnectionHandler
				.getWrappedConnectionHandler();

		// Have wrapped server process the request
		this.wrappedServer.processRequest(wrappedConnectionHandler, attachment);
	}

	/*
	 * ===================== SslTaskExecutor ==================================
	 */

	@Override
	public void beginTask(Runnable task) {
		// Invoke process to execute the task
		this.executeContext.invokeProcess(this.sslTaskFlowIndex, task, null);
	}
}