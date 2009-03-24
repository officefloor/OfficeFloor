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
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.impl.socket.server.SocketListener.SocketListenerDependencies;
import net.officefloor.plugin.impl.socket.server.messagesegment.DirectBufferMessageSegmentPool;
import net.officefloor.plugin.impl.socket.server.messagesegment.HeapBufferMessageSegmentPool;
import net.officefloor.plugin.impl.socket.server.messagesegment.NotPoolMessageSegmentPool;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;

/**
 * Abstract {@link ManagedObjectSource} for a {@link ServerSocket}.
 * 
 * @author Daniel
 */
public abstract class AbstractServerSocketManagedObjectSource<F extends Enum<F>>
		extends AbstractManagedObjectSource<None, F> {

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
	public static final String PROPERTY_MAXIMUM_CONNECTIONS = "max.connections.per.listener";

	/**
	 * Strategy for {@link MessageSegmentPool}.
	 */
	public static final String PROPERTY_MESSAGE_SEGMENT_POOL_STRATEGY = "message.segment.pool.technique";

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * {@link Server}.
	 */
	private Server<F> server;

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
	}

	/**
	 * Obtains the {@link SelectorFactory}.
	 * 
	 * @return {@link SelectorFactory}.
	 */
	SelectorFactory getSelectorFactory() {
		return this.selectorFactory;
	}

	/*
	 * =================== AbstractManagedObjectSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PORT);
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, F> context)
			throws Exception {

		// Obtain the managed object source context
		ManagedObjectSourceContext<F> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the configuration
		int port = Integer.parseInt(mosContext.getProperty(PROPERTY_PORT));
		int bufferSize = Integer.parseInt(mosContext.getProperty(
				PROPERTY_BUFFER_SIZE, "1024"));
		int recommendedSegmentCount = Integer.parseInt(mosContext.getProperty(
				PROPERTY_MESSAGE_SIZE, "1024"));
		int maxConn = Integer.parseInt(mosContext.getProperty(
				PROPERTY_MAXIMUM_CONNECTIONS, String
						.valueOf(SocketListener.UNBOUNDED_MAX_CONNECTIONS)));
		String messageSegmentPoolStrategy = mosContext.getProperty(
				PROPERTY_MESSAGE_SEGMENT_POOL_STRATEGY, "heap");

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

		// Create the server socket handler and create the server
		ServerSocketHandler<F> serverSocketHandler = this
				.createServerSocketHandler(context);
		this.server = serverSocketHandler.createServer();

		// Create the connection manager
		ConnectionManager connectionManager = new ConnectionManager(
				this.selectorFactory, this.server, maxConn);

		// Register the accepter of connections
		ServerSocketAccepter accepter = new ServerSocketAccepter(
				new InetSocketAddress(port), serverSocketHandler,
				connectionManager, recommendedSegmentCount, messageSegmentPool);
		ManagedObjectTaskBuilder<None, ServerSocketAccepter.ServerSocketAccepterFlows> accepterTask = mosContext
				.addWork("accepter", accepter).addTask("accepter", accepter);
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
	public void start(ManagedObjectExecuteContext<F> context) throws Exception {
		// Make the execute context available to the server
		this.server.setManagedObjectExecuteContext(context);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Should never be directly used by a task
		throw new IllegalStateException("Can not source managed object from a "
				+ this.getClass().getSimpleName());
	}

	/**
	 * Creates the {@link ServerSocketHandler}.
	 * 
	 * @param context
	 *            {@link MetaDataContext}.
	 * @throws Exception
	 *             If fails to create the {@link ServerSocketHandler}.
	 */
	protected abstract ServerSocketHandler<F> createServerSocketHandler(
			MetaDataContext<None, F> context) throws Exception;

}