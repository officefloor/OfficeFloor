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
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.impl.socket.server.messagesegment.DirectBufferMessageSegmentPool;
import net.officefloor.plugin.impl.socket.server.messagesegment.HeapBufferMessageSegmentPool;
import net.officefloor.plugin.impl.socket.server.messagesegment.NotPoolMessageSegmentPool;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;

/**
 * <p>
 * {@link ManagedObjectSource} for a {@link ServerSocket}.
 * <p>
 * This provides the functionality for the spi interfaces. Please consider using
 * one of the {@link ManagedObjectSource} inheriting from this that provide a
 * protocol specific api.
 * 
 * @author Daniel
 */
public class ServerSocketManagedObjectSource extends
		AbstractManagedObjectSource<None, ServerSocketHandlerEnum> {

	/**
	 * Port property name.
	 */
	public static final String PROPERTY_PORT = "port";

	/**
	 * Buffer size property name.
	 */
	public static final String PROPERTY_BUFFER_SIZE = "buffer.size";

	/**
	 * Message size property name.
	 */
	public static final String PROPERTY_MESSAGE_SIZE = "message.size";

	/**
	 * Maximum connections property name.
	 */
	public static final String PROPERTY_MAXIMUM_CONNECTIONS = "max.connections";

	/**
	 * Strategy for {@link MessageSegmentPool}.
	 */
	public static final String PROPERTY_MESSAGE_SEGMENT_POOL_STRATEGY = "message.segment.pool.technique";

	/**
	 * {@link ServerSocketAccepter} listening for connections.
	 */
	private ServerSocketAccepter serverSocketAccepter;

	/**
	 * {@link Server}.
	 */
	protected Server server;

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * Default constructor necessary as per {@link ManagedObjectSource}.
	 */
	public ServerSocketManagedObjectSource() {
		this(new SelectorFactory());
	}

	/**
	 * Allow for hooking in for testing.
	 * 
	 * @param selectorFactory
	 *            {@link SelectorFactory}.
	 */
	ServerSocketManagedObjectSource(SelectorFactory selectorFactory) {
		this.selectorFactory = selectorFactory;
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
	 * Processes the completely read {@link ReadMessage}.
	 * 
	 * @param readMessage
	 *            {@link ReadMessage}.
	 * @throws IOException
	 *             If fails to process {@link ReadMessage}.
	 */
	void processMessage(ReadMessageImpl readMessage) throws IOException {
		// Process the message
		this.server.processReadMessage(readMessage,
				readMessage.stream.connection.connectionHandler);
	}

	/*
	 * =================== AbstractManagedObjectSource ==================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractAsyncManagedObjectSource
	 * #loadSpecification(net.officefloor.frame.spi
	 * .managedobject.source.impl.AbstractAsyncManagedObjectSource
	 * .SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PORT);
		context.addProperty(PROPERTY_BUFFER_SIZE, "Buffer size");
		context.addProperty(PROPERTY_MESSAGE_SIZE,
				"Recommended segments per message");
		context.addProperty(PROPERTY_MAXIMUM_CONNECTIONS,
				"Maximum connextions per listener");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractAsyncManagedObjectSource
	 * #loadMetaData(net.officefloor.frame.spi.managedobject
	 * .source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(
			MetaDataContext<None, ServerSocketHandlerEnum> context)
			throws Exception {

		// Obtain the managed object source context
		ManagedObjectSourceContext mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the configuration
		int port = Integer.parseInt(mosContext.getProperty(PROPERTY_PORT));
		int bufferSize = Integer.parseInt(mosContext
				.getProperty(PROPERTY_BUFFER_SIZE));
		int recommendedSegmentCount = Integer.parseInt(mosContext
				.getProperty(PROPERTY_MESSAGE_SIZE));
		int maxConn = Integer.parseInt(mosContext.getProperty(
				PROPERTY_MAXIMUM_CONNECTIONS, "63"));
		String messageSegmentPoolStrategy = mosContext.getProperty(
				PROPERTY_MESSAGE_SEGMENT_POOL_STRATEGY, "heap");

		// Create prefix name
		String prefix = "serversocket." + port + ".";

		// Create the message segment pool based on specified strategy
		MessageSegmentPool messageSegmentPool;
		if ("heap".equalsIgnoreCase(messageSegmentPoolStrategy)) {
			messageSegmentPool = new HeapBufferMessageSegmentPool(bufferSize);
		} else if ("direct".equalsIgnoreCase(messageSegmentPoolStrategy)) {
			messageSegmentPool = new DirectBufferMessageSegmentPool(bufferSize);
		} else if ("none".equalsIgnoreCase(messageSegmentPoolStrategy)) {
			messageSegmentPool = new NotPoolMessageSegmentPool(bufferSize);
		} else {
			throw new Exception("Unknown "
					+ MessageSegmentPool.class.getSimpleName() + " strategy '"
					+ messageSegmentPoolStrategy + "'");
		}

		// Create the connection manager
		ConnectionManager connectionManager = new ConnectionManager(this,
				maxConn);

		// Register the accepter of connections
		this.serverSocketAccepter = new ServerSocketAccepter(
				new InetSocketAddress(port), connectionManager,
				recommendedSegmentCount, messageSegmentPool);
		ManagedObjectWorkBuilder<ServerSocketAccepter> accepterWork = mosContext
				.addWork(prefix + "Accepter", ServerSocketAccepter.class);
		accepterWork.setWorkFactory(this.serverSocketAccepter);
		ManagedObjectTaskBuilder<Indexed> accepterTask = accepterWork.addTask(
				"Accepter", Object.class, this.serverSocketAccepter);
		accepterTask.setTeam(prefix + "Accepter.TEAM");
		accepterTask.linkFlow(0, (prefix + "Listener"), "Listener",
				FlowInstigationStrategyEnum.ASYNCHRONOUS);

		// Register the listening of connections
		ManagedObjectWorkBuilder<ConnectionManager> listenerWork = mosContext
				.addWork(prefix + "Listener", ConnectionManager.class);
		listenerWork.setWorkFactory(connectionManager);
		ManagedObjectTaskBuilder<Indexed> listenerTask = listenerWork.addTask(
				"Listener", Object.class, connectionManager);
		listenerTask.setTeam(prefix + "Listener.TEAM");

		// Flag to start accepter on server start up
		mosContext.addStartupTask(prefix + "Accepter", "Accepter");

		// Provide for linking in a Server Socket handler
		context.getHandlerLoader(ServerSocketHandlerEnum.class).mapHandlerType(
				ServerSocketHandlerEnum.SERVER_SOCKET_HANDLER,
				ServerSocketHandler.class);

		// Register the server socket handler
		this.registerServerSocketHandler(context);
	}

	/**
	 * <p>
	 * Registers the {@link ServerSocketHandler}. By default specifies for a
	 * {@link ServerSocketHandler} to be configured in.
	 * <p>
	 * A specific protocol {@link ServerSocketManagedObjectSource} should
	 * override this method to provide a specific {@link ServerSocketHandler}.
	 * <p>
	 * Protocol specific overriding should at minimum provide:<il>
	 * <li>Type of object</li>
	 * <li>{@link ServerSocketHandler} implementation</li> </il>
	 * 
	 * @param context
	 *            {@link MetaDataContext}.
	 * @throws Exception
	 *             If fails.
	 */
	protected void registerServerSocketHandler(
			MetaDataContext<None, ServerSocketHandlerEnum> context)
			throws Exception {
		// By default do not register
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start
	 * (net
	 * .officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	@SuppressWarnings("unchecked")
	public void start(ManagedObjectExecuteContext context) throws Exception {
		// Obtain the handler
		ServerSocketHandler serverSocketHandler = (ServerSocketHandler) context
				.getHandler(ServerSocketHandlerEnum.SERVER_SOCKET_HANDLER);

		// Specify the server
		this.server = serverSocketHandler.createServer();

		// Bind to socket and start handling connections
		this.serverSocketAccepter.bind(serverSocketHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractManagedObjectSource#getManagedObject()
	 */
	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Should never be directly used by a task
		throw new IllegalStateException("Can not source managed object from a "
				+ this.getClass().getSimpleName());
	}

}
