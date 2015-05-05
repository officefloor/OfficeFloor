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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import net.officefloor.plugin.socket.server.ConnectionManager;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.protocol.Connection;

/**
 * Abstract {@link ManagedObjectSource} for a {@link ServerSocketChannel}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractServerSocketManagedObjectSource extends
		AbstractManagedObjectSource<None, Indexed> implements
		CommunicationProtocolContext {

	/**
	 * {@link Logger}.
	 */
	private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

	/**
	 * Port property name.
	 */
	public static final String PROPERTY_PORT = "port";

	/**
	 * Name of property to obtain the send buffer size.
	 */
	public static final String PROPERTY_SEND_BUFFER_SIZE = "send.buffer.size";

	/**
	 * Name of property to obtain the receive buffer size.
	 */
	public static final String PROPERTY_RECEIVE_BUFFER_SIZE = "receive.buffer.size";

	/**
	 * Name of property to obtain the default {@link Charset}.
	 */
	public static final String PROPERTY_DEFAULT_CHARSET = "default.charset";

	/**
	 * Default {@link Charset} to use if one is not configured.
	 */
	public static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * Singleton {@link ConnectionManager} for all {@link Connection} instances.
	 */
	private static ConnectionManager singletonConnectionManager;

	/**
	 * Registered {@link AbstractServerSocketManagedObjectSource} instances.
	 */
	private static Set<AbstractServerSocketManagedObjectSource> registeredServerSockets = new HashSet<AbstractServerSocketManagedObjectSource>();

	/**
	 * Obtains the {@link ConnectionManager}.
	 * 
	 * @param mosContext
	 *            {@link ManagedObjectSourceContext}.
	 * @param instance
	 *            Instance of the
	 *            {@link AbstractServerSocketManagedObjectSource} using the
	 *            {@link ConnectionManager}.
	 * @param heartBeatInterval
	 *            Heart beat interval in milliseconds.
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param receiveBufferSize
	 *            Receive buffer size.
	 * @return {@link ConnectionManager}.
	 */
	@SuppressWarnings("rawtypes")
	private static synchronized ConnectionManager getConnectionManager(
			ManagedObjectSourceContext<Indexed> mosContext,
			AbstractServerSocketManagedObjectSource instance,
			long heartBeatInterval, int sendBufferSize, int receiveBufferSize) {

		final String listenerTeamName = "listener";

		// Provide dummy task for consistency of teams across all server sockets
		AbstractSingleTask<Work, None, None> dummy = new AbstractSingleTask<Work, None, None>() {
			@Override
			public Object doTask(TaskContext<Work, None, None> context)
					throws Throwable {
				throw new IllegalStateException(
						"Dummy auto-wire listener task should not be invoked");
			}
		};
		mosContext.addWork("SetupAutoWireListener", dummy)
				.addTask("SetupAutoWireListener", dummy)
				.setTeam(listenerTeamName);

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
						sendBufferSize, receiveBufferSize);

				// Register the socket listener
				socketListeners[i] = socketListener;

				// Register the listening of connections
				String listenerName = "listener-" + i;
				ManagedObjectTaskBuilder listenerTask = mosContext.addWork(
						listenerName, socketListener).addTask(listenerName,
						socketListener);
				listenerTask.setTeam(listenerTeamName);

				// Flag to start listener on server start up
				mosContext.addStartupTask(listenerName, listenerName);
			}

			// Create the connection manager
			ConnectionManagerImpl connectionManager = new ConnectionManagerImpl(
					heartBeatInterval, socketListeners);
			singletonConnectionManager = connectionManager;

			// Configure the connection manager for heart beat
			String heartBeatName = "heartbeat";
			ManagedObjectTaskBuilder heartBeatTask = mosContext.addWork(
					heartBeatName, connectionManager).addTask(heartBeatName,
					connectionManager);
			heartBeatTask.setTeam(listenerTeamName);

			// Flag to start heart beat on server start up
			mosContext.addStartupTask(heartBeatName, heartBeatName);

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
		singletonConnectionManager.openSocketSelectors();
	}

	/**
	 * <p>
	 * Closes the possible open {@link ConnectionManager} and releases all
	 * {@link Selector} instances for the {@link SocketListener} instances.
	 * <p>
	 * Made public so that tests may use to close.
	 * 
	 * @throws IOException
	 *             If fails to close the {@link ConnectionManager}.
	 */
	public static synchronized void closeConnectionManager() throws IOException {

		// Clear all registered server sockets
		registeredServerSockets.clear();

		// Determine if active connection manager
		if (singletonConnectionManager != null) {
			// Close the socket listener selectors
			singletonConnectionManager.closeSocketSelectors();
		}

		// Close (release) the connection manager to create again
		singletonConnectionManager = null;
	}

	/**
	 * <p>
	 * Releases the {@link ConnectionManager} for the
	 * {@link AbstractServerSocketManagedObjectSource} instance.
	 * <p>
	 * Once {@link ConnectionManager} is released for all
	 * {@link AbstractServerSocketManagedObjectSource} instances it is itself
	 * closed.
	 * 
	 * @param instance
	 *            {@link AbstractServerSocketManagedObjectSource}.
	 * @throws IOException
	 *             If fails to close the {@link ConnectionManager}.
	 */
	private static synchronized void releaseConnectionManager(
			AbstractServerSocketManagedObjectSource instance)
			throws IOException {

		// Unregister from the connection manager
		registeredServerSockets.remove(instance);

		// Close connection manager if no further server sockets
		if (registeredServerSockets.size() == 0) {
			closeConnectionManager();
		}
	}

	/**
	 * {@link CommunicationProtocolSource}.
	 */
	private final CommunicationProtocolSource communicationProtocolSource;

	/**
	 * {@link ServerSocketAccepter} that requires binding on starting.
	 */
	private ServerSocketAccepter serverSocketAccepter;

	/**
	 * Send buffer size.
	 */
	private int sendBufferSize;

	/**
	 * Default {@link Charset}.
	 */
	private Charset defaultCharset;

	/**
	 * {@link CommunicationProtocol}.
	 */
	private CommunicationProtocol communicationProtocol;

	/**
	 * Initiate.
	 */
	public AbstractServerSocketManagedObjectSource() {

		// Create the communication protocol source
		this.communicationProtocolSource = this
				.createCommunicationProtocolSource();
	}

	/**
	 * Creates the {@link CommunicationProtocolSource}.
	 * 
	 * @return {@link CommunicationProtocolSource}.
	 */
	protected abstract CommunicationProtocolSource createCommunicationProtocolSource();

	/*
	 * ====================== CommunicationProtocolContext =====================
	 */

	@Override
	public int getSendBufferSize() {
		return this.sendBufferSize;
	}

	@Override
	public Charset getDefaultCharset() {
		return this.defaultCharset;
	}

	/*
	 * =================== AbstractManagedObjectSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Add required properties for Server Socket
		context.addProperty(PROPERTY_PORT);

		// Add communication protocol required properties
		this.communicationProtocolSource.loadSpecification(context);
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Indexed> context)
			throws Exception {

		// Obtain the managed object source context
		ManagedObjectSourceContext<Indexed> mosContext = context
				.getManagedObjectSourceContext();

		// Create socket to obtain operating system details
		Socket socket = new Socket();
		int osSendBufferSize = socket.getSendBufferSize();
		int osReceiveBufferSize = socket.getReceiveBufferSize();
		socket.close();

		// Obtain the configuration
		int port = Integer.parseInt(mosContext.getProperty(PROPERTY_PORT));
		this.sendBufferSize = Integer.parseInt(mosContext.getProperty(
				PROPERTY_SEND_BUFFER_SIZE, String.valueOf(osSendBufferSize)));
		int receiveBufferSize = Integer.parseInt(mosContext.getProperty(
				PROPERTY_RECEIVE_BUFFER_SIZE,
				String.valueOf(osReceiveBufferSize)));

		// Obtain the default charset
		String defaultCharsetName = mosContext.getProperty(
				PROPERTY_DEFAULT_CHARSET, null);
		if (defaultCharsetName != null) {
			// Use the configured charset
			this.defaultCharset = Charset.forName(defaultCharsetName);
		} else {
			// Not configured, use default for flexibility, fall back to system
			defaultCharsetName = DEFAULT_CHARSET;
			try {
				this.defaultCharset = Charset.forName(defaultCharsetName);
			} catch (IllegalCharsetNameException ex) {
				// Use default charset
				this.defaultCharset = Charset.defaultCharset();
				defaultCharsetName = this.defaultCharset.name();
			}
		}

		// Obtain the server socket backlog
		int serverSocketBackLog = 25000; // TODO make configurable

		// Obtain the heart beat interval
		long heartBeatInterval = 10000; // TODO make configurable

		// Obtain the connection manager
		ConnectionManager connectionManager = getConnectionManager(mosContext,
				this, heartBeatInterval, this.sendBufferSize, receiveBufferSize);

		// Create the communication protocol
		this.communicationProtocol = this.communicationProtocolSource
				.createCommunicationProtocol(context, this);

		// Register the accepter of connections
		this.serverSocketAccepter = new ServerSocketAccepter(
				new InetSocketAddress(port), this.communicationProtocol,
				connectionManager, serverSocketBackLog);
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

		// Make the execute context available to the communication protocol
		this.communicationProtocol.setManagedObjectExecuteContext(context);

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
		try {
			this.serverSocketAccepter.unbindFromSocket();

		} catch (IOException ex) {
			// Shutting down so just log issue
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.log(Level.INFO, "Failed to unbind from Socket", ex);
			}
		}

		// Release connection manager (closes socket listeners when appropriate)
		try {
			releaseConnectionManager(this);

		} catch (IOException ex) {
			// Shutting down so just log issue
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.log(Level.INFO, "Failed to release "
						+ ConnectionManager.class.getSimpleName(), ex);
			}
		}
	}

}