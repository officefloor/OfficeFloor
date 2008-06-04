/*
 * Created on 24/02/2006
 */
package net.officefloor.plugin.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;

/**
 * Factory for the creation of the {@link ConnectionFactory} and
 * {@link Destination} administered objects.
 * 
 * @author Daniel
 */
public interface JmsAdminObjectFactory {

	/**
	 * Enable configuration of JMS objects.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 */
	void init(ManagedObjectSourceContext context);

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
