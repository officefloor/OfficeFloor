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
package net.officefloor.plugin.jms.server;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.Session;

import net.officefloor.admin.transaction.Transaction;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link ManagedObject} for the JMS server.
 * 
 * @author Daniel Sagenschneider
 */
public class JmsServerManagedObject implements ManagedObject, ServerSession,
		MessageListener, Transaction {

	/**
	 * {@link JmsServerManagedObjectSource}.
	 */
	private final JmsServerManagedObjectSource moSource;

	/**
	 * {@link Session}.
	 */
	private final Session session;

	/**
	 * {@link Message} to be processed.
	 */
	private Message message = null;

	/**
	 * Flag indicating if this has been committed.
	 */
	private boolean isCommitted = false;

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
	protected synchronized void reset() throws JMSException {
		// Handle if not committed
		if (!this.isCommitted) {

			// Flag not committed
			this.isCommitted = false;

			// Roll back any action on this session
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
	public synchronized Message getMessage() {
		return this.message;
	}

	/*
	 * ================== ManagedObject ===================================
	 */

	@Override
	public synchronized Object getObject() throws Exception {
		// Return the message being processed
		return this.message;
	}

	/*
	 * ================== ServerSession ===================================
	 */

	@Override
	public Session getSession() throws JMSException {
		return this.session;
	}

	@Override
	public void start() throws JMSException {
		this.moSource.runSession(this);
	}

	/*
	 * ================ MessageListener ===================================
	 */

	@Override
	public synchronized void onMessage(Message message) {
		this.message = message;
	}

	/*
	 * ================ Transaction =======================================
	 */

	@Override
	public void begin() throws Exception {
		// Always within a transaction
	}

	@Override
	public synchronized void commit() throws Exception {
		// Commit the transaction
		this.session.commit();

		// Flag that committed
		this.isCommitted = true;
	}

	@Override
	public synchronized void rollback() throws Exception {
		// Roll back the transaction
		this.session.rollback();
	}

}