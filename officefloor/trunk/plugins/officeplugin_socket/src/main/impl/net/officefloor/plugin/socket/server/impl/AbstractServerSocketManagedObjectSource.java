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
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.CommunicationProtocol;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.Server;
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
	 * Singleton {@link ConnectionManager} for all {@link Connection} instances.
	 */
	private static ConnectionManager<?> singletonConnectionManager;

	/**
	 * Obtains the {@link ConnectionManager}.
	 * 
	 * @param mosContext
	 *            {@link ManagedObjectSourceContext}.
	 * @return {@link ConnectionManager}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static synchronized ConnectionManager getConnectionManager(
			ManagedObjectSourceContext<Indexed> mosContext,
			SelectorFactory selectorFactory) {

		// Lazy create the singleton connection manager
		if ((!mosContext.isLoadingType())
				&& (singletonConnectionManager == null)) {

			// One socket lister per processor to spread load
			int numberOfSocketListeners = Runtime.getRuntime()
					.availableProcessors();

			// Create the socket listeners
			SocketListener[] socketListeners = new SocketListener[numberOfSocketListeners];
			for (int i = 0; i < socketListeners.length; i++) {

				// Create the socket listener
				SocketListener socketListener = new SocketListener(
						selectorFactory);

				// Register the socket listener
				socketListeners[i] = socketListener;

				// Register the listening of connections
				String listenerName = "listener-" + i;
				ManagedObjectTaskBuilder listenerTask = mosContext.addWork(
						listenerName, socketListener).addTask(listenerName,
						socketListener);
				listenerTask.setTeam("listener");

				// Flag to start listener on server start up
				mosContext.addStartupTask(listenerName, listenerName);
			}

			// Create the connection manager
			singletonConnectionManager = new ConnectionManager(socketListeners);
		}

		// Provide dummy task for consistency across all server sockets
		AbstractSingleTask<Work, None, None> dummy = new AbstractSingleTask<Work, None, None>() {
			@Override
			public Object doTask(TaskContext<Work, None, None> context)
					throws Throwable {
				throw new IllegalStateException(
						"Dummy auto-wire listener task should not be invoked");
			}
		};
		mosContext.addWork("SetupAutoWireListener", dummy)
				.addTask("SetupAutoWireListener", dummy).setTeam("listener");

		// Return the singleton connection manager
		return singletonConnectionManager;
	}

	/**
	 * Opens the {@link Selector} instances for the {@link SocketListener}
	 * instances.
	 * 
	 * @throws IOException
	 *             If fails to open all the {@link Selector} instances.
	 */
	private static synchronized void openSocketListenerSelectors()
			throws IOException {
		// Should be created at this time
		singletonConnectionManager.openSocketListenerSelectors();
	}

	/**
	 * <p>
	 * Closes the possible open {@link ConnectionManager} and releases all
	 * {@link Selector} instances for the {@link SocketListener} instances.
	 * <p>
	 * Made public so that tests may use to close.
	 */
	public static synchronized void closeConnectionManager() {

		// Determine if active connection manager
		if (singletonConnectionManager != null) {
			// Close the socket listener selectors
			singletonConnectionManager.closeSocketListenerSelectors();
		}

		// Close (release) the connection manager to create again
		singletonConnectionManager = null;
	}

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
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<None, Indexed> context)
			throws Exception {

		// Obtain the managed object source context
		ManagedObjectSourceContext<Indexed> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the configuration
		int port = Integer.parseInt(mosContext.getProperty(PROPERTY_PORT));
		final int bufferSize = Integer.parseInt(mosContext.getProperty(
				PROPERTY_BUFFER_SIZE, "1024"));

		// Obtain the connection manager
		ConnectionManager<CH> connectionManager = getConnectionManager(
				mosContext, this.selectorFactory);

		// Create the buffer squirt factory
		BufferSquirtFactory bufferSquirtFactory = new HeapByteBufferSquirtFactory(
				bufferSize);

		// Create the server
		this.server = this.communicationProtocol.createServer(context,
				bufferSquirtFactory);

		// Register the accepter of connections
		this.serverSocketAccepter = new ServerSocketAccepter<CH>(
				new InetSocketAddress(port), this.server, connectionManager,
				bufferSquirtFactory);
		ManagedObjectTaskBuilder<None, None> accepterTask = mosContext.addWork(
				"accepter", this.serverSocketAccepter).addTask("accepter",
				this.serverSocketAccepter);
		accepterTask.setTeam("accepter");

		// Flag to start accepter on server start up
		mosContext.addStartupTask("accepter", "accepter");
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context)
			throws Exception {
		// Make the execute context available to the server
		this.server.setManagedObjectExecuteContext(context);

		// Open selectors for socket listeners
		openSocketListenerSelectors();

		// Bind to server socket to accept connections
		this.serverSocketAccepter.bindToSocket();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Should never be directly used by a task
		throw new IllegalStateException("Can not source managed object from a "
				+ this.getClass().getSimpleName());
	}

	@Override
	public void stop() {
		// Unbind acceptor socket to listen for new connections
		this.serverSocketAccepter.unbindFromSocket();

		// Close connection manager and all socket listener selectors
		closeConnectionManager();
	}

}