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
package net.officefloor.plugin.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * JDBC {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
 * 
 * @author Daniel
 */
public class JdbcManagedObject implements ManagedObject,
		ConnectionEventListener {

	/**
	 * {@link PooledConnection}.
	 */
	private final PooledConnection pooledConnection;

	/**
	 * {@link Connection}.
	 */
	private Connection connection;

	/**
	 * Failure of the {@link PooledConnection}.
	 */
	private SQLException failure = null;
	
	/**
	 * Initiate.
	 * 
	 * @param pooledConnection
	 *            {@link PooledConnection}.
	 */
	public JdbcManagedObject(PooledConnection pooledConnection) {
		this.pooledConnection = pooledConnection;

		// Register to listen to the pooled connection
		this.pooledConnection.addConnectionEventListener(this);
	}

	/**
	 * Recycles.
	 * 
	 * @throws SQLException
	 *             If fails to recycle.
	 */
	protected void recycle() throws SQLException {
		synchronized (this.pooledConnection) {
			
			// Determine if failure of pooled connection
			if (this.failure != null) {
				// Close the pooled connection
				this.pooledConnection.close();
				
				// Escalate failure
				throw this.failure;
			}
			
			// Close the connection if created
			if (this.connection != null) {
				this.connection.close();
				this.connection = null;
			}
		}
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
		// Lazy create the connection
		synchronized (this.pooledConnection) {
			if (this.connection == null) {
				this.connection = this.pooledConnection.getConnection();
			}
		}

		// Return the connection
		return this.connection;
	}

	/*
	 * ====================================================================
	 * ConnectionEventListener
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.ConnectionEventListener#connectionClosed(javax.sql.ConnectionEvent)
	 */
	public void connectionClosed(ConnectionEvent event) {
		// Flag the connection closed
		synchronized (this.pooledConnection) {
			this.connection = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.ConnectionEventListener#connectionErrorOccurred(javax.sql.ConnectionEvent)
	 */
	public void connectionErrorOccurred(ConnectionEvent event) {
		// Indicate failure of pooled connection
		synchronized(this.pooledConnection) {
			this.failure = event.getSQLException();
		}
	}

}
