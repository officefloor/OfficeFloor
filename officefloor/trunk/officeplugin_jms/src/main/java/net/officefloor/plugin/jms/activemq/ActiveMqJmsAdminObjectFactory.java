/*
 * Created on 2/03/2006
 */
package net.officefloor.plugin.jms.activemq;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import net.officefloor.plugin.jms.JmsAdminObjectFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;

/**
 * Factory for the creation of administered objects for ActiveMq.
 * 
 * @author Daniel
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
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jms.JmsAdminObjectFactory#init(java.util.Properties)
	 */
	public void init(Properties properties) {

		// Create the connection factory
		this.connectionFactory = new ActiveMQConnectionFactory();
		this.connectionFactory.buildFromProperties(properties);

		// Obtain the destination URL
		String destinationUrl = properties.getProperty(DESTINATION_PROPERTY);

		// Create the destination
		this.destination = ActiveMQDestination.createDestination(
				destinationUrl, ActiveMQDestination.QUEUE_TYPE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jms.JmsAdminObjectFactory#createConnectionFactory()
	 */
	public ConnectionFactory createConnectionFactory() {
		return this.connectionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.jms.JmsAdminObjectFactory#createDestination()
	 */
	public Destination createDestination() {
		return this.destination;
	}

}
