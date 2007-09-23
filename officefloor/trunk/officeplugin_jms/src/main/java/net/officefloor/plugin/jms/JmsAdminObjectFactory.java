/*
 * Created on 24/02/2006
 */
package net.officefloor.plugin.jms;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Factory for the creation of the {@link javax.jms.ConnectionFactory} and
 * {@link javax.jms.Destination} administered objects.
 * 
 * @author Daniel
 */
public interface JmsAdminObjectFactory {

	/**
	 * Enable configuration of JMS objects.
	 * 
	 * @param properties
	 *            Properties to initialise the JMS objects.
	 */
	void init(Properties properties);

	/**
	 * Creates the {@link ConnectionFactory} administered object.
	 * 
	 * @return {@link ConnectionFactory} administered object.
	 * @throws Exception
	 *             If fail to create.
	 */
	ConnectionFactory createConnectionFactory() throws Exception;

	/**
	 * Creates the {@link Destination} administered object.
	 * 
	 * @return {@link Destination} administered object.
	 * @throws Exception
	 *             If fail to create.
	 */
	Destination createDestination() throws Exception;
}
