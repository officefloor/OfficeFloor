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
import java.util.HashSet;
import java.util.Set;

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
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
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
	 * Singleton {@link ConnectionManagerImpl} for all {@link Connection} instances.
	 */
	private static ConnectionManagerImpl<?> singletonConnectionManager;

	/**
	 * Registered {@link AbstractServerSocketManagedObjectSource} instances.
	 */
	private static Set<AbstractServerSocketManagedObjectSource<?>> registeredServerSockets = new HashSet<AbstractServerSocketManagedObjectSource<?>>();

	/**
	 * Obtains the {@link ConnectionManagerImpl}.
	 * 
	 * @param mosContext
	 *            {@link ManagedObjectSourceContext}.
	 * @param selectorFactory
	 *            {@link SelectorFactory}.
	 * @param instance
	 *            Instance of the
	 *            {@link AbstractServerSocketManagedObjectSource} using the
	 *            {@link ConnectionManagerImpl}.
	 * @return {@link ConnectionManagerImpl}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static synchronized ConnectionManagerImpl getConnectionManager(
			ManagedObjectSourceContext<Indexed> mosContext,
			SelectorFactory selectorFactory,
			AbstractServerSocketManagedObjectSource<?> instance) {

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

		// Do nothing if just loading type
		if (mosContext.isLoadingType()) {
			return null;
		}

		// Lazy create the singleton connection manager
		if (singletonConnectionManager == null) {

			// Spread load if have multiple processors
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
			singletonConnectionManager = new ConnectionManagerImpl(socketListeners);
		}

		// Register the instance for use of the connection manager
		registeredServerSockets.add(instance);

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
	 * Closes the possible open {@link ConnectionManagerImpl} and releases all
	 * {@link Selector} instances for the {@link SocketListener} instances.
	 * <p>
	 * Made public so that tests may use to close.
	 */
	public static synchronized void closeConnectionManager() {

		// Clear all registered server sockets
		registeredServerSockets.clear();

		// Determine if active connection manager
		if (singletonConnectionManager != null) {
			// Close the socket listener selectors
			singletonConnectionManager.closeSocketListenerSelectors();
		}

		// Close (release) the connection manager to create again
		singletonConnectionManager = null;
	}

	/**
	 * <p>
	 * Releases the {@link ConnectionManagerImpl} for the
	 * {@link AbstractServerSocketManagedObjectSource} instance.
	 * <p>
	 * Once {@link ConnectionManagerImpl} is released for all
	 * {@link AbstractServerSocketManagedObjectSource} instances it is itself
	 * closed.
	 * 
	 * @param instance
	 *            {@link AbstractServerSocketManagedObjectSource}.
	 */
	private static synchronized void releaseConnectionManager(
			AbstractServerSocketManagedObjectSource<?> instance) {

		// Unregister from the connection manager
		registeredServerSockets.remove(instance);

		// Close connection manager if no further server sockets
		if (registeredServerSockets.size() == 0) {
			closeConnectionManager();
		}
	}

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * {@link CommunicationProtocolSource}.
	 */
	private final CommunicationProtocolSource<CH> communicationProtocol;

	/**
	 * {@link ServerSocketAccepter} that requires binding on starting.
	 */
	private ServerSocketAccepter<CH> serverSocketAccepter;

	/**
	 * {@link CommunicationProtocol}.
	 */
	private CommunicationProtocol<CH> server;

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
		CommunicationProtocolSource<CH> commProtocol = this
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
	 * Creates the {@link CommunicationProtocolSource}.
	 * 
	 * @return {@link CommunicationProtocolSource}.
	 */
	protected abstract CommunicationProtocolSource<CH> createCommunicationProtocol();

	/**
	 * <p>
	 * Creates a wrapping {@link CommunicationProtocolSource} around the input
	 * {@link CommunicationProtocolSource}.
	 * <p>
	 * An example would be to add SSL {@link CommunicationProtocolSource}.
	 * <p>
	 * This default implementation returns the input
	 * {@link CommunicationProtocolSource} as is.
	 * 
	 * @param communicationProtocol
	 *            {@link CommunicationProtocolSource} to possibly wrap.
	 * @return This default implementation returns the input
	 *         {@link CommunicationProtocolSource} (no wrapping).
	 */
	protected CommunicationProtocolSource<CH> createWrappingCommunicationProtocol(
			CommunicationProtocolSource<CH> communicationProtocol) {
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
				PROPERTY_BUFFER_SIZE, "2048"));

		// Obtain the connection manager
		ConnectionManagerImpl<CH> connectionManager = getConnectionManager(
				mosContext, this.selectorFactory, this);

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

		// Release connection manager (closes socket listeners when appropriate)
		releaseConnectionManager(this);
	}

}