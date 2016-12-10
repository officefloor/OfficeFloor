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
import java.util.Deque;
import java.util.LinkedList;

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
	 * Queued {@link Thread} instances.
	 */
	private final Deque<boolean[]> queuedThreads = new LinkedList<boolean[]>();

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
	public Connection getConnection() throws SQLException {

		// Provide connection on first come first serve basis
		boolean[] lock = null;
		synchronized (this) {
			// Determine if connection available
			if (this.numberReleased >= this.maxSize) {
				// Wait for available connection
				lock = new boolean[] { false };
				this.queuedThreads.add(lock);
			}
		}

		// Wait on available connection
		if (lock != null) {
			synchronized (lock) {
				if (!(lock[0])) {
					try {
						lock.wait();
					} catch (InterruptedException ex) {
						throw new SQLException(ex);
					}
				}
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

			// Release the connection
			boolean[] lock = null;
			synchronized (PooledDataSource.this) {
				// Return connection
				PooledDataSource.this.numberReleased--;
				lock = PooledDataSource.this.queuedThreads.pollFirst();
			}

			// Notify another thread that connection available
			if (lock != null) {
				synchronized (lock) {
					lock[0] = true;
					lock.notify();
				}
			}
		}
	}

}