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
 * @author Daniel Sagenschneider
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

		// Allow time for the broker to stop
		Thread.sleep(200);

		// Release the services
		service = null;
		connectionFactory = null;
		destination = null;
	}

	/*
	 * ====================== JmsAdminObjectFactory ============================
	 */

	@Override
	public void init(ManagedObjectSourceContext<?> context) {
		// Do nothing
	}

	@Override
	public ConnectionFactory createConnectionFactory() throws Exception {
		synchronized (VmJmsAdminObjectFactory.class) {
			validateStarted();
			return connectionFactory;
		}
	}

	@Override
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