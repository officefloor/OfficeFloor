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
package net.officefloor.plugin.jms.server;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.Session;

import net.officefloor.admin.transaction.Transaction;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
 * for the JMS server.
 * 
 * @author Daniel
 */
public class JmsServerManagedObject implements ManagedObject, ServerSession,
		MessageListener, Transaction {

	/**
	 * {@link JmsServerManagedObjectSource}.
	 */
	protected final JmsServerManagedObjectSource moSource;

	/**
	 * {@link Session}.
	 */
	protected final Session session;

	/**
	 * {@link Message} to be procssed.
	 */
	protected Message message = null;

	/**
	 * Flag indicating if this has been committed.
	 */
	protected boolean isCommitted = false;

	/**
	 * Initiate.
	 * 
	 * @param moSource
	 *            {@link JmsServerManagedObjectSource}.
	 * @param session
	 *            {@link Session} of the {@link ServerSession}.
	 * @throws JMSException
	 *             If fails to initiate.
	 */
	protected JmsServerManagedObject(JmsServerManagedObjectSource moSource,
			Session session) throws JMSException {
		// Store state
		this.moSource = moSource;
		this.session = session;

		// Specify this as the sessions message listener
		this.session.setMessageListener(this);
	}

	/**
	 * Resets this {@link JmsServerManagedObject}.
	 * 
	 * @throws JMSException
	 *             If fails to reset.
	 */
	protected void reset() throws JMSException {
		// Handle if not committed
		if (!this.isCommitted) {

			// Flag not committed
			this.isCommitted = false;

			// Rollback any action on this session
			this.session.rollback();

			// Release the message
			this.message = null;
		}
	}

	/**
	 * Obtains the {@link Message}.
	 * 
	 * @return {@link Message}.
	 */
	public Message getMessage() {
		return this.message;
	}

	/*
	 * ====================================================================
	 * ManagedObject
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.ManagedObject#getObject()
	 */
	public Object getObject() throws Exception {
		// Return the message being processed
		return this.message;
	}

	/*
	 * ====================================================================
	 * ServerSession
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.ServerSession#getSession()
	 */
	public Session getSession() throws JMSException {
		return this.session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.ServerSession#start()
	 */
	public void start() throws JMSException {
		// Run the session
		this.moSource.runSession(this);
	}

	/*
	 * ====================================================================
	 * ServerSession
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message message) {
		// Store the message
		this.message = message;
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
		// Always within a transaction
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.ei.transaction.Transaction#commit()
	 */
	public void commit() throws Exception {
		// Commit the transaction
		this.session.commit();

		// Flag that committed
		this.isCommitted = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.ei.transaction.Transaction#rollback()
	 */
	public void rollback() throws Exception {
		// Rollback the transaction
		this.session.rollback();
	}

}
