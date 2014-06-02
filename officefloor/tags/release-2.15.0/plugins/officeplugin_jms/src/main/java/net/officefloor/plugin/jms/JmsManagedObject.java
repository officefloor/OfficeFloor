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
 * @author Daniel Sagenschneider
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
