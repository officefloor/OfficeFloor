/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.socket.server.CommunicationProtocol;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.socket.server.impl.SocketListener.SocketListenerDependencies;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.squirtfactory.HeapByteBufferSquirtFactory;

/**
 * Abstract {@link ManagedObjectSource} for a {@link ServerSocketChannel}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractServerSocketManagedObjectSource<CH extends ConnectionHandler>
		extends AbstractManagedObjectSource<None, Indexed> {

	/**
	 * Port property name.
	 */
	public static final String PROPERTY_PORT = "port";

	/**
	 * Buffer size property name.
	 */
	public static final String PROPERTY_BUFFER_SIZE = "buffer.size";

	/**
	 * Maximum connections property name.
	 */
	public static final String PROPERTY_MAXIMUM_CONNECTIONS_PER_LISTENER = "max.connections.per.listener";

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * {@link CommunicationProtocol}.
	 */
	private final CommunicationProtocol<CH> communicationProtocol;

	/**
	 * {@link ServerSocketAccepter} that requires binding on starting.
	 */
	private ServerSocketAccepter<CH> serverSocketAccepter;

	/**
	 * {@link Server}.
	 */
	private Server<CH> server;

	/**
	 * Default constructor necessary as per {@link ManagedObjectSource}.
	 */
	public AbstractServerSocketManagedObjectSource() {
		this(new SelectorFactory());
	}

	/**
	 * Allow for hooking in for testing.
	 *
	 * @param selectorFactory
	 *            {@link SelectorFactory}.
	 */
	AbstractServerSocketManagedObjectSource(SelectorFactory selectorFactory) {
		this.selectorFactory = selectorFactory;

		// Create the communication protocol
		CommunicationProtocol<CH> commProtocol = this
				.createCommunicationProtocol();

		// Provide possible wrapping around the communication protocol
		this.communicationProtocol = this
				.createWrappingCommunicationProtocol(commProtocol);
	}

	/**
	 * Obtains the {@link SelectorFactory}.
	 *
	 * @return {@link SelectorFactory}.
	 */
	SelectorFactory getSelectorFactory() {
		return this.selectorFactory;
	}

	/**
	 * Creates the {@link CommunicationProtocol}.
	 *
	 * @return {@link CommunicationProtocol}.
	 */
	protected abstract CommunicationProtocol<CH> createCommunicationProtocol();

	/**
	 * <p>
	 * Creates a wrapping {@link CommunicationProtocol} around the input
	 * {@link CommunicationProtocol}.
	 * <p>
	 * An example would be to add SSL {@link CommunicationProtocol}.
	 * <p>
	 * This default implementation returns the input
	 * {@link CommunicationProtocol} as is.
	 *
	 * @param communicationProtocol
	 *            {@link CommunicationProtocol} to possibly wrap.
	 * @return This default implementation returns the input
	 *         {@link CommunicationProtocol} (no wrapping).
	 */
	protected CommunicationProtocol<CH> createWrappingCommunicationProtocol(
			CommunicationProtocol<CH> communicationProtocol) {
		return communicationProtocol;
	}

	/*
	 * =================== AbstractManagedObjectSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Add required properties for Server Socket
		context.addProperty(PROPERTY_PORT);

		// Add communication protocol required properties
		this.communicationProtocol.loadSpecification(context);
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Indexed> context)
			throws Exception {

		// Obtain the managed object source context
		ManagedObjectSourceContext<Indexed> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the configuration
		int port = Integer.parseInt(mosContext.getProperty(PROPERTY_PORT));
		final int bufferSize = Integer.parseInt(mosContext.getProperty(
				PROPERTY_BUFFER_SIZE, "1024"));
		int maxConn = Integer.parseInt(mosContext.getProperty(
				PROPERTY_MAXIMUM_CONNECTIONS_PER_LISTENER, String
						.valueOf(SocketListener.UNBOUNDED_MAX_CONNECTIONS)));

		// Create the buffer squirt factory
		BufferSquirtFactory bufferSquirtFactory = new HeapByteBufferSquirtFactory(
				bufferSize);

		// Create the server socket handler and create the server
		ServerSocketHandler<CH> serverSocketHandler = this.communicationProtocol
				.createServerSocketHandler(context, bufferSquirtFactory);
		this.server = serverSocketHandler.createServer();

		// Create the connection manager
		ConnectionManager<CH> connectionManager = new ConnectionManager<CH>(
				this.selectorFactory, this.server, maxConn);

		// Register the accepter of connections
		this.serverSocketAccepter = new ServerSocketAccepter<CH>(
				new InetSocketAddress(port), serverSocketHandler,
				connectionManager, bufferSquirtFactory);
		ManagedObjectTaskBuilder<None, ServerSocketAccepter.ServerSocketAccepterFlows> accepterTask = mosContext
				.addWork("accepter", this.serverSocketAccepter).addTask(
						"accepter", this.serverSocketAccepter);
		accepterTask.setTeam("accepter");
		accepterTask.linkFlow(
				ServerSocketAccepter.ServerSocketAccepterFlows.LISTEN,
				"listener", "listener",
				FlowInstigationStrategyEnum.ASYNCHRONOUS, ConnectionImpl.class);

		// Register the listening of connections
		ManagedObjectTaskBuilder<SocketListenerDependencies, Indexed> listenerTask = mosContext
				.addWork("listener", connectionManager).addTask("listener",
						connectionManager);
		listenerTask.linkParameter(SocketListenerDependencies.CONNECTION,
				ConnectionImpl.class);
		listenerTask.setTeam("listener");

		// Flag to start accepter on server start up
		mosContext.addStartupTask("accepter", "accepter");
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context)
			throws Exception {
		// Make the execute context available to the server
		this.server.setManagedObjectExecuteContext(context);

		// Bind to socket to listen for connections
		this.serverSocketAccepter.bindToSocket();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Should never be directly used by a task
		throw new IllegalStateException("Can not source managed object from a "
				+ this.getClass().getSimpleName());
	}

}