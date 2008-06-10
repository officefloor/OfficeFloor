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
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * JMS {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public class JmsManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Connection Factory for the JMS connection.
	 */
	protected ConnectionFactory connectionFactory;

	/**
	 * Destination to send messages.
	 */
	protected Destination destination;

	/**
	 * {@link Connection} that is lazy created.
	 */
	protected Connection connection;

	/**
	 * Default constructor as required.
	 */
	public JmsManagedObjectSource() {
	}

	/*
	 * ====================================================================
	 * AbstractManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadMetaData(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.StartContext)
	 */
	@Override
	protected void start(StartContext<None> startContext) throws Exception {
		// Start the connection
		this.connection = this.connectionFactory.createConnection();
		this.connection.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource#getManagedObject()
	 */
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
