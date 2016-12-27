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
package net.officefloor.plugin.jms.server;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import net.officefloor.admin.transaction.Transaction;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.jms.JmsAdminObjectFactory;
import net.officefloor.plugin.jms.JmsUtil;
import net.officefloor.plugin.jms.server.OnMessageTask.OnMessageDependencies;
import net.officefloor.plugin.jms.server.OnMessageTask.OnMessageFlows;

/**
 * JMS Server {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JmsServerManagedObjectSource
		extends
		AbstractManagedObjectSource<None, JmsServerManagedObjectSource.JmsServerFlows>
		implements ServerSessionPool {

	/**
	 * Property name to obtain the message selector.
	 */
	public static final String JMS_MESSAGE_SELECTOR = "selector";

	/**
	 * Property name to obtain the maximum number of {@link ServerSession}
	 * instances.
	 */
	public static final String JMS_MAX_SERVER_SESSIONS = "jms.max.sessions";

	/**
	 * {@link JmsAdminObjectFactory}.
	 */
	private JmsAdminObjectFactory jmsAdminObjectFactory;

	/**
	 * Connection Factory for the JMS connection.
	 */
	private ConnectionFactory connectionFactory;

	/**
	 * Connection.
	 */
	private Connection connection;

	/**
	 * Destination to consume messages from.
	 */
	private Destination destination;

	/**
	 * Message selector.
	 */
	private String messageSelector;

	/**
	 * Pool of {@link ServerSession} instances.
	 */
	private final List<JmsServerManagedObject> serverSessionPool = new ArrayList<JmsServerManagedObject>();

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<JmsServerFlows> executeContext;

	/**
	 * Maximum number of {@link ServerSession} instances.
	 */
	private int maxSessions;

	/**
	 * Indicates the number of {@link ServerSession} instances.
	 */
	private int numberOfSessions = 0;

	/**
	 * Returns the {@link JmsServerManagedObject} to its pool.
	 * 
	 * @param jmsServerManagedObject
	 *            {@link JmsServerManagedObject} to be returned to pool.
	 */
	protected void returnJmsServerManagedObject(
			JmsServerManagedObject jmsServerManagedObject) {
		synchronized (this.serverSessionPool) {
			// Return to pool
			this.serverSessionPool.add(jmsServerManagedObject);

			// Notify a session has become available
			this.serverSessionPool.notify();
		}
	}

	/**
	 * Runs the {@link Session} to obtain the {@link Message}.
	 * 
	 * @param managedObject
	 *            {@link JmsServerManagedObject}.
	 */
	public void runSession(JmsServerManagedObject managedObject) {
		// Invoke on message to run the session and process message
		this.executeContext.invokeProcess(0, managedObject, managedObject, 0);
	}

	/*
	 * ===================== AbstractManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(JmsUtil.JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY,
				"Admin Object Factory");
		context.addProperty(JMS_MAX_SERVER_SESSIONS, "JMS Max Sessions");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, JmsServerFlows> context)
			throws Exception {

		// Obtain the managed object source context
		ManagedObjectSourceContext<JmsServerFlows> mosContext = context
				.getManagedObjectSourceContext();

		// Specify object types
		context.setManagedObjectClass(JmsServerManagedObject.class);
		context.setObjectClass(Void.class); // may not use

		// Obtain the JMS admin object factory
		this.jmsAdminObjectFactory = JmsUtil
				.getJmsAdminObjectFactory(mosContext);

		// Obtain the property values
		this.maxSessions = Integer.parseInt(mosContext
				.getProperty(JMS_MAX_SERVER_SESSIONS));
		this.messageSelector = mosContext.getProperty(JMS_MESSAGE_SELECTOR,
				null);

		// Link the on message task
		context.addFlow(JmsServerFlows.ON_MESSAGE, JmsServerManagedObject.class);
		mosContext
				.linkProcess(JmsServerFlows.ON_MESSAGE, "server", "onmessage");
		OnMessageTask onMessageTask = new OnMessageTask();
		ManagedObjectFunctionBuilder<OnMessageDependencies, OnMessageFlows> taskBuilder = mosContext
				.addWork("server", onMessageTask).addTask("onmessage",
						onMessageTask);
		taskBuilder.linkParameter(
				OnMessageDependencies.JMS_SERVER_MANAGED_OBJECT,
				JmsServerManagedObject.class);
		taskBuilder.linkFlow(OnMessageFlows.ON_MESSAGE, null,
				FlowInstigationStrategyEnum.SEQUENTIAL, Message.class);
		taskBuilder.setTeam("team");

		// Register the recycle task
		new RecycleJmsServerTask(this).registerAsRecycleTask(
				context.getManagedObjectSourceContext(), "team");

		// Specify extension interfaces
		context.addManagedObjectExtensionInterface(Transaction.class,
				new ExtensionInterfaceFactory<Transaction>() {
					@Override
					public Transaction createExtensionInterface(
							ManagedObject managedObject) {
						// Return as Transaction
						return (Transaction) managedObject;
					}
				});
	}

	@Override
	public void start(ManagedObjectExecuteContext<JmsServerFlows> context)
			throws Exception {

		// Maintain reference to context
		this.executeContext = context;

		// TODO consider moving the below into a startup task

		// Obtain the connection factory and destination
		this.connectionFactory = jmsAdminObjectFactory
				.createConnectionFactory();
		this.destination = jmsAdminObjectFactory.createDestination();

		// Create the connection
		this.connection = this.connectionFactory.createConnection();

		// Create the connection consumer (max of one message per session)
		this.connection.createConnectionConsumer(this.destination,
				this.messageSelector, this, 1);

		// Start the connection
		this.connection.start();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Can not source server managed object
		throw new UnsupportedOperationException(
				"Can not source a managed object from a "
						+ this.getClass().getName());
	}

	/*
	 * ==================== ServerSessionPool =============================
	 */

	@Override
	public ServerSession getServerSession() throws JMSException {

		// Lock on listing to ensure thread safe
		synchronized (this.serverSessionPool) {

			// Obtain the Server Session
			ServerSession session = null;
			while (session == null) {

				// Attempt to obtain from pool
				if (!this.serverSessionPool.isEmpty()) {
					// Return from the pool
					session = this.serverSessionPool.remove(0);

				} else if (this.numberOfSessions >= this.maxSessions) {
					// Block waiting for session to become available
					try {
						this.serverSessionPool.wait(100);
					} catch (InterruptedException ex) {
						// Ignore interruptions
					}

				} else {
					// Create the server session (transacted)
					session = new JmsServerManagedObject(this,
							this.connection.createSession(true,
									Session.SESSION_TRANSACTED));

					// Increment the number of sessions
					this.numberOfSessions++;
				}
			}

			// Found the session
			return session;
		}
	}

	/**
	 * Provides the flow instances.
	 */
	public static enum JmsServerFlows {

		/**
		 * Handles the {@link Message}.
		 */
		ON_MESSAGE
	}

}