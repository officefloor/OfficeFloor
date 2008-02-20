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
package net.officefloor.plugin.jms.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import net.officefloor.admin.transaction.Transaction;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.ManagedObjectExtensionInterfaceMetaDataImpl;
import net.officefloor.plugin.jms.AbstractManagedObjectSource;
import net.officefloor.plugin.jms.JmsAdminObjectFactory;

/**
 * JMS Server
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
 * 
 * TODO stop the ConnectionConsumer
 * 
 * @author Daniel
 */
public class JmsServerManagedObjectSource<D extends Enum<D>, H extends Enum<H>>
		extends AbstractManagedObjectSource implements ManagedObjectSource,
		ManagedObjectSourceMetaData<D, H>, HandlerFactory<Indexed>,
		Handler<Indexed>, ServerSessionPool {

	/**
	 * Property name to obtain the message selector.
	 */
	public static final String JMS_MESSAGE_SELECTOR = "net.officefloor.plugin.jms.message.selector";

	/**
	 * Property name to obtain the maximum number of {@link ServerSession}
	 * instances.
	 */
	public static final String JMS_MAX_SERVER_SESSION = "net.officefloor.plugin.jms.max.sessions";

	/**
	 * Name of {@link net.officefloor.frame.api.execute.Work} to process the
	 * {@link javax.jms.Message}.
	 */
	public static final String JMS_ON_MESSAGE_WORK = "net.officefloor.plugin.jms.onmessage.work";

	/**
	 * Name of {@link net.officefloor.frame.api.execute.Task} to process the
	 * {@link javax.jms.Message}.
	 */
	public static final String JMS_ON_MESSAGE_TASK = "net.officefloor.plugin.jms.onmessage.task";

	/**
	 * Connection Factory for the JMS connection.
	 */
	protected ConnectionFactory connectionFactory;

	/**
	 * Connection.
	 */
	protected Connection connection;

	/**
	 * Destination to consume messages from.
	 */
	protected Destination destination;

	/**
	 * {@link HandlerContext}.
	 */
	protected HandlerContext<?> handlerContext;

	/**
	 * {@link ConnectionConsumer}.
	 */
	protected ConnectionConsumer consumer;

	/**
	 * Message selector.
	 */
	protected String messageSelector;

	/**
	 * Pool of {@link ServerSession} instances.
	 */
	protected final List<JmsServerManagedObject<D, H>> serverSessionPool = new ArrayList<JmsServerManagedObject<D, H>>();

	/**
	 * Maximum number of {@link javax.jms.ServerSession} instances.
	 */
	protected int maxSessions;

	/**
	 * Indicates the number of {@link ServerSession} instances.
	 */
	protected int numberOfSessions = 0;

	/**
	 * Default constructor as required.
	 */
	public JmsServerManagedObjectSource() {
	}

	/**
	 * Returns the {@link JmsServerManagedObject} to its pool.
	 * 
	 * @param jmsServerManagedObject
	 *            {@link JmsServerManagedObject} to be returned to pool.
	 */
	protected void returnJmsServerManagedObject(
			JmsServerManagedObject<D, H> jmsServerManagedObject) {
		synchronized (this.serverSessionPool) {
			// Return to pool
			this.serverSessionPool.add(jmsServerManagedObject);

			// Notify a session has become available
			this.serverSessionPool.notify();
		}
	}

	/**
	 * Runs the {@link Session} to obtain the {@link javax.jms.Message}.
	 * 
	 * @param managedObject
	 *            {@link JmsServerManagedObject}.
	 */
	public void runSession(JmsServerManagedObject<D, H> managedObject) {
		// Invoke on message to run the session and process message
		this.handlerContext.invokeProcess(0, managedObject, managedObject);
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	public void init(ManagedObjectSourceContext context) throws Exception {

		// Obtain the properties
		Properties properties = context.getProperties();

		// Obtain the JMS admin object factory
		JmsAdminObjectFactory jmsAdminObjectFactory = this
				.getJmsAdminObjectFactory(context.getProperties());

		// Obtain the connection factory
		this.connectionFactory = jmsAdminObjectFactory
				.createConnectionFactory();

		// Obtain the destination
		this.destination = jmsAdminObjectFactory.createDestination();

		// Obtain the message selector
		this.messageSelector = properties.getProperty(JMS_MESSAGE_SELECTOR);

		// Obtains the maximum sessions
		this.maxSessions = Integer.parseInt(properties
				.getProperty(JMS_MAX_SERVER_SESSION));

		// Register the OnMessageTask
		ManagedObjectTaskBuilder<?> onMessageTask = new OnMessageTask()
				.registerTask("jms.server.onmessage", "onmessage",
						"jms.server.onmessage", context);
		onMessageTask.setNextTaskInFlow(properties
				.getProperty(JMS_ON_MESSAGE_WORK), properties
				.getProperty(JMS_ON_MESSAGE_TASK));

		// Register the handler (and link OnMessageTask)
		ManagedObjectHandlerBuilder<JmsServerHandlersEnum> managedObjectBuilder = context
				.getHandlerBuilder(JmsServerHandlersEnum.class);
		HandlerBuilder<Indexed> handler = managedObjectBuilder
				.registerHandler(JmsServerHandlersEnum.JMS_SERVER_HANDLER);
		handler.setHandlerFactory(this);
		handler.linkProcess(0, "jms.server.onmessage", "onmessage");

		// Register the recycle task
		new RecycleJmsServerTask(this).registerAsRecycleTask(context,
				"jms.server.recycle");
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

		// Obtain the handler (loads the handler context to this)
		context.getHandler(JmsServerHandlersEnum.JMS_SERVER_HANDLER);

		// TODO move the below into a startup task

		// Create the connection
		this.connection = this.connectionFactory.createConnection();

		// Create the connection consumer (only one message at a time)
		this.consumer = this.connection.createConnectionConsumer(
				this.destination, this.messageSelector, this, 1);

		// Start the connection
		this.connection.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	public void sourceManagedObject(ManagedObjectUser user) {
		// Can not source server managed object
		throw new UnsupportedOperationException(
				"Can not source a managed object from a "
						+ this.getClass().getName());
	}

	/*
	 * ====================================================================
	 * ManagedObjectSourceMetaData
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getManagedObjectClass()
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends ManagedObject> getManagedObjectClass() {
		return JmsServerManagedObject.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getObjectClass()
	 */
	public Class<?> getObjectClass() {
		// TODO Auto-generated method stub
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
		// No dependencies
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerKeys()
	 */
	@SuppressWarnings("unchecked")
	public Class<H> getHandlerKeys() {
		return (Class) JmsServerHandlersEnum.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerType(H)
	 */
	public Class<? extends Handler<?>> getHandlerType(H key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
	 */
	public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
		return new ManagedObjectExtensionInterfaceMetaData[] { new ManagedObjectExtensionInterfaceMetaDataImpl<Transaction>(
				Transaction.class,
				new ExtensionInterfaceFactory<Transaction>() {
					public Transaction createExtensionInterface(
							ManagedObject managedObject) {
						// Return as Transaction
						return (Transaction) managedObject;
					}
				}) };
	}

	/*
	 * ====================================================================
	 * HandlerFactory
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.HandlerFactory#createHandler()
	 */
	public Handler<Indexed> createHandler() {
		return this;
	}

	/*
	 * ====================================================================
	 * Handler
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Handler#setHandlerContext(net.officefloor.frame.api.execute.HandlerContext)
	 */
	public void setHandlerContext(HandlerContext<Indexed> context)
			throws Exception {
		// Store for use
		this.handlerContext = context;
	}

	/*
	 * ====================================================================
	 * ServerSessionPool
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.ServerSessionPool#getServerSession()
	 */
	public ServerSession getServerSession() throws JMSException {
		synchronized (this.serverSessionPool) {

			// Determine if max sessions reached
			if (this.numberOfSessions >= this.maxSessions) {
				// Wait for managed object
				for (;;) {
					if (!this.serverSessionPool.isEmpty()) {
						// Return from the pool
						return this.serverSessionPool.remove(0);
					} else {
						// Block waiting for session to become available
						try {
							this.serverSessionPool.wait(100);
						} catch (InterruptedException ex) {
							// Ignore interuptions
						}
					}
				}
			} else {
				// Pool not full but prioritise taking from pool over creating
				JmsServerManagedObject<D, H> serverMo;
				if (!this.serverSessionPool.isEmpty()) {
					// Source from pool
					serverMo = this.serverSessionPool.remove(0);
				} else {
					// Create the server session (transacted)
					serverMo = new JmsServerManagedObject<D, H>(this,
							this.connection.createSession(true,
									Session.SESSION_TRANSACTED));

					// Increment the number of sessions
					this.numberOfSessions++;
				}

				// Return the session
				return serverMo;
			}
		}
	}

	/**
	 * Provides the {@link net.officefloor.frame.api.execute.Handler} indexes.
	 */
	public static enum JmsServerHandlersEnum {

		/**
		 * Handles the JMS messages.
		 */
		JMS_SERVER_HANDLER
	}

}
