/*
 * Created on 24/02/2006
 */
package net.officefloor.plugin.jms;

import javax.jms.Session;

import net.officefloor.admin.transaction.Transaction;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link net.officefloor.core.spi.managedobject.ManagedObject} for a JMS
 * {@link javax.jms.Session}.
 * 
 * TODO close the session on disposing of managed object.
 * 
 * @author Daniel
 */
public class JmsManagedObject implements ManagedObject, Transaction {

	/**
	 * {@link Session} of the {@link javax.jms.Connection}.
	 */
	private final Session session;

	/**
	 * {@link TextMessageProducer}.
	 */
	private final TextMessageProducer producer;

	/**
	 * Initiate with {@link Session}.
	 * 
	 * @param session
	 *            {@link Session}.
	 * @param producer
	 *            {@link TextMessageProducer}.
	 */
	public JmsManagedObject(Session session, TextMessageProducer producer) {
		// Store state
		this.session = session;
		this.producer = producer;
	}

	/*
	 * ====================================================================
	 * ManagedObject
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.managedobject.ManagedObject#getObject()
	 */
	public Object getObject() throws Exception {
		return this.producer;
	}

	/*
	 * ====================================================================
	 * Transaction
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.ei.transaction.Transaction#begin()
	 */
	public void begin() throws Exception {
		// Always in a transaction
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.ei.transaction.Transaction#commit()
	 */
	public void commit() throws Exception {
		this.session.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.ei.transaction.Transaction#rollback()
	 */
	public void rollback() throws Exception {
		this.session.rollback();
	}

}
