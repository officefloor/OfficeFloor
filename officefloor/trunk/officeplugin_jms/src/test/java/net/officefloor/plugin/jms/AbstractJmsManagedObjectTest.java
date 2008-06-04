/*
 * Created on 22/02/2006
 */
package net.officefloor.plugin.jms;

import java.io.Serializable;

import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.jms.activemq.VmJmsAdminObjectFactory;

/**
 * Abstract {@link junit.framework.TestCase} for testing JMS
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
 * 
 * @author Daniel
 */
public class AbstractJmsManagedObjectTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Connection.
	 */
	private QueueConnection connection;

	/**
	 * Session.
	 */
	private QueueSession session;

	/**
	 * Destination.
	 */
	private Queue destination;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// Start the service
		VmJmsAdminObjectFactory.start();

		// Create the JMS admin object factory
		JmsAdminObjectFactory factory = new VmJmsAdminObjectFactory();

		// Create the connection and start it
		this.connection = (QueueConnection) factory.createConnectionFactory()
				.createConnection();
		this.connection.start();

		// Session
		this.session = this.connection.createQueueSession(true,
				Session.SESSION_TRANSACTED);

		// Destination
		this.destination = (Queue) factory.createDestination();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		// Stop the service
		VmJmsAdminObjectFactory.stop();
	}

	/**
	 * Pops an object from the {@link Queue}.
	 * 
	 * @param waitTime
	 *            Wait time for a message containing the object.
	 * @return Object from the {@link Queue}.
	 * @throws Exception
	 *             If fails to obtain the object.
	 */
	protected Object popObject(long waitTime) throws Exception {

		// Create the consumer
		QueueReceiver receiver = this.session.createReceiver(this.destination);

		// Return the object on the queue
		ObjectMessage message = (ObjectMessage) receiver.receive(waitTime);

		// Obtain the object in the message payload
		Object object = message.getObject();

		// Close the consumer
		receiver.close();
		
		// Commit
		this.session.commit();

		// Return object
		return object;
	}

	/**
	 * Pops text from the {@link Queue}.
	 * 
	 * @param waitTime
	 *            Wait time for a message containing the object.
	 * @return Text from the {@link javax.jms.Message} popped from the
	 *         {@link Queue}.
	 * @throws Exception
	 *             If fails to obtain the text.
	 */
	protected String popText(long waitTime) throws Exception {

		// Create the consumer
		QueueReceiver receiver = this.session.createReceiver(this.destination);

		// Return the text on the queue
		TextMessage message = (TextMessage) receiver.receive(waitTime);

		// Obtain the text in the message payload
		String text = message.getText();

		// Close the consumer
		receiver.close();

		// Commit
		this.session.commit();

		// Return text
		return text;
	}

	/**
	 * Pushes an object onto the {@link Queue}.
	 * 
	 * @param object
	 *            Object to push onto the {@link Queue}.
	 * @throws Exception
	 *             If fails push the object.
	 */
	protected void pushObject(Object object) throws Exception {
		// Create the producer
		QueueSender sender = this.session.createSender(this.destination);

		// Create the message to wrap the object
		ObjectMessage message = session.createObjectMessage();
		message.setObject((Serializable) object);

		// Push object onto the queue
		sender.send(message);

		// Close the producer
		sender.close();

		// Commit
		this.session.commit();
	}

	/**
	 * Pushes text onto the {@link Queue}.
	 * 
	 * @param text
	 *            Text of {@link javax.jms.Message} to push onto the
	 *            {@link Queue}.
	 * @throws Exception
	 *             If fails push the object.
	 */
	protected void pushText(String text) throws Exception {
		// Create the producer
		QueueSender sender = this.session.createSender(this.destination);

		// Create the message to wrap the text
		TextMessage message = session.createTextMessage();
		message.setText(text);

		// Push text onto the queue
		sender.send(message);

		// Close the producer
		sender.close();

		// Commit
		this.session.commit();
	}

	/**
	 * Validate the methods available.
	 */
	public void test() throws Exception {

		// Validate can push and pop text
		final String TEXT = "Test data";
		this.pushText(TEXT);
		String returnedText = this.popText(1000);
		assertEquals("Incorrect returned text", TEXT, returnedText);

		// Validate can push and pop an object
		final TestObject OBJECT = new TestObject();
		this.pushObject(OBJECT);
		TestObject returnedObject = (TestObject) this.popObject(1000);
		assertEquals("Incorrect return object", OBJECT.VALUE,
				returnedObject.VALUE);
	}

	/**
	 * Object for test push/pop object test.
	 */
	private static class TestObject implements Serializable {
		public int VALUE = 1;
	}

}
