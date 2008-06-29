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
package net.officefloor.plugin.jms.activemq;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.plugin.jms.JmsAdminObjectFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;

/**
 * {@link JmsAdminObjectFactory} for testing.
 * 
 * @author Daniel
 */
public class VmJmsAdminObjectFactory implements JmsAdminObjectFactory {

	/**
	 * URL of the broker.
	 */
	private static final String BROKER_URL = "vm://localhost";

	/**
	 * URL of the destination.
	 */
	private static final String DESTINATION_URL = "queue://test";

	/**
	 * Broker Service.
	 */
	private static BrokerService service;

	/**
	 * Connection factory.
	 */
	private static ActiveMQConnectionFactory connectionFactory;

	/**
	 * Destination.
	 */
	private static Destination destination;

	/**
	 * Starts the {@link JmsAdminObjectFactory}.
	 * 
	 * @throws Exception
	 *             If fails to start.
	 */
	public synchronized static void start() throws Exception {

		// Ensure stopped
		if (service != null) {
			stop();
		}

		// Create the broker
		service = new BrokerService();
		service.setPersistent(false);
		service.start();

		// Create the connection factory
		connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(BROKER_URL);

		// Create the destination
		destination = ActiveMQDestination.createDestination(DESTINATION_URL,
				ActiveMQDestination.QUEUE_TYPE);
	}

	/**
	 * Stops the {@link JmsAdminObjectFactory}.
	 * 
	 * @throws Exception
	 *             If fails to stop.
	 */
	public synchronized static void stop() throws Exception {
		// Stop the broker
		service.stop();

		// Release the services
		service = null;
		connectionFactory = null;
		destination = null;
	}

	/*
	 * ====================================================================
	 * JmsAdminObjectFactory
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jms.JmsAdminObjectFactory#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	public void init(ManagedObjectSourceContext context) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jms.JmsAdminObjectFactory#createConnectionFactory()
	 */
	public ConnectionFactory createConnectionFactory() throws Exception {
		synchronized (VmJmsAdminObjectFactory.class) {
			validateStarted();
			return connectionFactory;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jms.JmsAdminObjectFactory#createDestination()
	 */
	public Destination createDestination() throws Exception {
		synchronized (VmJmsAdminObjectFactory.class) {
			validateStarted();
			return destination;
		}
	}

	/**
	 * Ensures the {@link BrokerService} has been started.
	 * 
	 * @throws Exception
	 *             If not started.
	 */
	private synchronized void validateStarted() throws Exception {
		if (service == null) {
			throw new JMSException(BrokerService.class.getSimpleName()
					+ " has not been started for this "
					+ VmJmsAdminObjectFactory.class.getName());
		}
	}

}
