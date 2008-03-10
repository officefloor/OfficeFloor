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
import java.util.Properties;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.ManagedObjectSourcePropertyImpl;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;

/**
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
 * for a {@link java.net.ServerSocket}.
 * 
 * @author Daniel
 */
public class ServerSocketManagedObjectSource<D extends Enum<D>, H extends Enum<H>>
		implements ManagedObjectSource<D, H>, ManagedObjectSourceMetaData<D, H> {

	/**
	 * {@link ServerSocketAccepter} listening for connections.
	 */
	private ServerSocketAccepter serverSocketAccepter;

	/**
	 * {@link Server}.
	 */
	private Server server;

	/**
	 * Processes the completely read
	 * {@link net.officefloor.plugin.socket.server.spi.ReadMessage}.
	 * 
	 * @param readMessage
	 *            {@link net.officefloor.plugin.socket.server.spi.ReadMessage}.
	 */
	public void processMessage(ReadMessageImpl readMessage) {
		// Process the message
		this.server.processReadMessage(readMessage, readMessage.getConnection()
				.getConnectionHandler());
	}

	/*
	 * ====================================================================
	 * ManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getSpecification()
	 */
	public ManagedObjectSourceSpecification getSpecification() {
		// Provide specification
		return new ManagedObjectSourceSpecification() {
			public ManagedObjectSourceProperty[] getProperties() {
				return new ManagedObjectSourceProperty[] {
						new ManagedObjectSourcePropertyImpl("port", "port"),
						new ManagedObjectSourcePropertyImpl("max_conn",
								"max conn per listener") };
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	public void init(ManagedObjectSourceContext context) throws Exception {

		Properties properties = context.getProperties();

		// Obtain the port
		int port = Integer.parseInt(properties.getProperty("port"));

		// Obtain the maximum connections per listener
		int maxConn = Integer
				.parseInt(properties.getProperty("max_conn", "63"));

		// Create prefix name
		String prefix = "serversocket." + port + ".";

		// Create the message segment pool
		MessageSegmentPool messageSegmentPool = new MessageSegmentPool();

		// Create the connection manager
		ConnectionManager connectionManager = new ConnectionManager(this,
				maxConn);

		// Register the accepter of connections
		this.serverSocketAccepter = new ServerSocketAccepter(
				new InetSocketAddress(port), connectionManager,
				messageSegmentPool);
		ManagedObjectWorkBuilder<ServerSocketAccepter> accepterWork = context
				.addWork(prefix + "Accepter", ServerSocketAccepter.class);

		// Configure the accepter of connections
		accepterWork.setWorkFactory(this.serverSocketAccepter);
		ManagedObjectTaskBuilder<Indexed> accepterTask = accepterWork.addTask(
				"Accepter", Object.class, this.serverSocketAccepter);
		accepterTask.setTeam(prefix + "Accepter.TEAM");
		accepterTask.linkFlow(0, (prefix + "Listener"), "Listener",
				FlowInstigationStrategyEnum.ASYNCHRONOUS);

		// Register the listener of connections
		ManagedObjectWorkBuilder<ConnectionManager> listenerWork = context
				.addWork(prefix + "Listener", ConnectionManager.class);

		// Configure the listener of connections
		listenerWork.setWorkFactory(connectionManager);
		ManagedObjectTaskBuilder<Indexed> listenerTask = listenerWork.addTask(
				"Listener", Object.class, connectionManager);
		listenerTask.setTeam(prefix + "Listener.TEAM");

		// Flag to start accepter on server start up
		context.addStartupTask(prefix + "Accepter", "Accepter");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getMetaData()
	 */
	public ManagedObjectSourceMetaData<D, H> getMetaData() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	@SuppressWarnings("unchecked")
	public void start(ManagedObjectExecuteContext context) throws Exception {
		// Obtain the handler
		ServerSocketHandler serverSocketHandler = (ServerSocketHandler) context
				.getHandler(ServerSocketHandlersEnum.SERVER_SOCKET_HANDLER);

		// Specify the server
		this.server = serverSocketHandler.createServer();

		// Load handler to accepter
		this.serverSocketAccepter.setServerSocketHandler(serverSocketHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	public void sourceManagedObject(ManagedObjectUser user) {
		// Should never be sourced as input
		throw new IllegalStateException("Can not source managed object from a "
				+ ServerSocketManagedObjectSource.class.getSimpleName());
	}

	/*
	 * ====================================================================
	 * ManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getManagedObjectClass()
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends ManagedObject> getManagedObjectClass() {
		return AsynchronousManagedObject.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getObjectClass()
	 */
	public Class<?> getObjectClass() {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyKeys()
	 */
	public Class<D> getDependencyKeys() {
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyMetaData(D)
	 */
	public ManagedObjectDependencyMetaData getDependencyMetaData(D key) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerKeys()
	 */
	@SuppressWarnings("unchecked")
	public Class<H> getHandlerKeys() {
		return (Class) ServerSocketHandlersEnum.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerType(H)
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Handler<?>> getHandlerType(H key) {
		// Return handler type
		ServerSocketHandlersEnum handleKey = (ServerSocketHandlersEnum) (Enum) key;
		switch (handleKey) {
		case SERVER_SOCKET_HANDLER:
			return (Class) ServerSocketHandler.class;
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
	 */
	public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
		// No extensions
		return null;
	}

	/**
	 * Provides the {@link net.officefloor.frame.api.execute.Handler} indexes.
	 */
	public static enum ServerSocketHandlersEnum {

		/**
		 * Handles the server socket.
		 */
		SERVER_SOCKET_HANDLER
	}

}
