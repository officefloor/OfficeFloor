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

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.plugin.jms.JmsAdminObjectFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;

/**
 * Factory for the creation of administered objects for ActiveMq.
 *
 * @author Daniel Sagenschneider
 */
public class ActiveMqJmsAdminObjectFactory implements JmsAdminObjectFactory {

	/**
	 * Name of property containing the URL of the destination.
	 */
	public static String DESTINATION_PROPERTY = "destinationUrl";

	/**
	 * ActiveMq {@link ConnectionFactory}.
	 */
	protected ActiveMQConnectionFactory connectionFactory;

	/**
	 * ActiveMq {@link Destination}.
	 */
	protected ActiveMQDestination destination;

	/*
	 * =================== JmsAdminObjectFactory =============================
	 */

	@Override
	public void init(ManagedObjectSourceContext<?> context) {

		// Create the connection factory
		this.connectionFactory = new ActiveMQConnectionFactory();
		this.connectionFactory.buildFromProperties(context.getProperties());

		// Obtain the destination URL
		String destinationUrl = context.getProperty(DESTINATION_PROPERTY);

		// Create the destination
		this.destination = ActiveMQDestination.createDestination(
				destinationUrl, ActiveMQDestination.QUEUE_TYPE);
	}

	@Override
	public ConnectionFactory createConnectionFactory() {
		return this.connectionFactory;
	}

	@Override
	public Destination createDestination() {
		return this.destination;
	}

}