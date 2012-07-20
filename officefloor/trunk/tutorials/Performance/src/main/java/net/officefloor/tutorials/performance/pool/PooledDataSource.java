/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorials.performance.pool;

import java.sql.SQLException;

/**
 * Simple object pool.
 * 
 * @author Daniel Sagenschneider
 */
public class PooledDataSource {

	/**
	 * Objects.
	 */
	private final int maxSize;

	/**
	 * Connection (singleton but should not be used).
	 */
	private final Connection connection = new Connection();

	/**
	 * Number released.
	 */
	private int numberReleased = 0;

	/**
	 * Initiate.
	 * 
	 * @param maxSize
	 *            Maximum size.
	 */
	public PooledDataSource(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * Obtains the connection.
	 * 
	 * @return Connection.
	 * @throws SQLException
	 *             Should not occur for testing.
	 */
	public synchronized Connection getConnection() throws SQLException {

		// Wait for available connection
		// TODO provide fairness
		while (this.numberReleased >= this.maxSize) {
			try {
				this.wait(100);
			} catch (InterruptedException ex) {
				throw new SQLException(ex);
			}
		}

		// Release a connection
		this.numberReleased++;
		return this.connection;
	}

	/**
	 * Connection.
	 */
	public class Connection {

		/**
		 * Return connection to pool.
		 * 
		 * @throws SQLException
		 *             Should not occur for testing.
		 */
		public void close() throws SQLException {
			synchronized (PooledDataSource.this) {
				// Return connection
				PooledDataSource.this.numberReleased--;
				PooledDataSource.this.notify();
			}
		}
	}

}