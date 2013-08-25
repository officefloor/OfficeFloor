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
/*
 * Created on 24/02/2006
 */
package net.officefloor.plugin.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * JMS {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JmsManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Connection Factory for the JMS connection.
	 */
	private ConnectionFactory connectionFactory;

	/**
	 * Destination to send messages.
	 */
	private Destination destination;

	/**
	 * {@link Connection} that is lazy created.
	 */
	private Connection connection;

	/*
	 * ================ AbstractManagedObjectSource =======================
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {

		// Specify types
		context.setObjectClass(TextMessageProducer.class);
		context.setManagedObjectClass(JmsManagedObject.class);

		// Obtain the JMS admin object factory
		JmsAdminObjectFactory jmsAdminObjectFactory = JmsUtil
				.getJmsAdminObjectFactory(context
						.getManagedObjectSourceContext());

		// Obtain the connection factory
		this.connectionFactory = jmsAdminObjectFactory
				.createConnectionFactory();

		// Obtain the destination
		this.destination = jmsAdminObjectFactory.createDestination();
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context)
			throws Exception {
		// Start the connection
		this.connection = this.connectionFactory.createConnection();
		this.connection.start();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Create the session
		Session session = this.connection.createSession(true,
				Session.SESSION_TRANSACTED);

		// Create the producer
		TextMessageProducer producer = new TextMessageProducerImpl(session,
				this.destination);

		// Return the JMS managed object
		return new JmsManagedObject(session, producer);
	}

}