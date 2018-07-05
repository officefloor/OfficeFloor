/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.jdbc.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.jdbc.AbstractConnectionManagedObject;

/**
 * {@link Connection} {@link ManagedObjectPool} implementation that uses
 * {@link ThreadLocal} instances to reduce contention of retrieving
 * {@link Connection} from singleton pool.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalJdbcConnectionPool implements ManagedObjectPool, ThreadCompletionListener {

	/**
	 * Obtains the delegate {@link Connection} from the proxy {@link Connection}.
	 * 
	 * @param connection
	 *            Proxy {@link Connection}.
	 * @return Delegate {@link Connection}.
	 * @throws SQLException
	 *             If failure in obtaining the delegate {@link Connection}.
	 */
	static Connection extractDelegateConnection(Connection connection) throws SQLException {
		PooledConnectionManagedObject managedObject = (PooledConnectionManagedObject) Proxy
				.getInvocationHandler(connection);
		return managedObject.getConnectionReference().connection;
	}

	/**
	 * Obtains the {@link ThreadLocalJdbcConnectionPool} for the proxy
	 * {@link Connection}.
	 * 
	 * @param connection
	 *            Proxy {@link Connection}.
	 * @return {@link ThreadLocalJdbcConnectionPool}.
	 */
	static ThreadLocalJdbcConnectionPool extractConnectionPool(Connection connection) {
		PooledConnectionManagedObject managedObject = (PooledConnectionManagedObject) Proxy
				.getInvocationHandler(connection);
		return managedObject.getPool();
	}

	/**
	 * {@link Proxy} interfaces for the {@link Connection}.
	 */
	private static final Class<?>[] PROXY_INTERFACES = new Class[] { Connection.class };

	/**
	 * {@link ConnectionPoolDataSource}.
	 */
	private final ConnectionPoolDataSource dataSource;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link ThreadLocal} {@link Connection} for this
	 * {@link ThreadLocalJdbcConnectionPool}.
	 */
	private final ThreadLocal<ConnectionReference> threadLocalConnection = new ThreadLocal<>();

	/**
	 * {@link ConnectionReference} instances idle within the pool.
	 */
	private final Deque<ConnectionReference> pooledConnections = new ConcurrentLinkedDeque<>();

	/**
	 * Instantiate.
	 * 
	 * @param dataSource
	 *            {@link ConnectionPoolDataSource}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public ThreadLocalJdbcConnectionPool(ConnectionPoolDataSource dataSource, ClassLoader classLoader) {
		this.dataSource = dataSource;
		this.classLoader = classLoader;
	}

	/**
	 * Obtains the {@link ThreadLocal} {@link Connection}.
	 * 
	 * @return {@link ThreadLocal} {@link Connection}.
	 */
	public Connection getThreadLocalConnection() {
		ConnectionReference reference = this.threadLocalConnection.get();
		return (reference == null ? null : reference.connection);
	}

	/*
	 * =================== ManagedObjectPool =====================
	 */

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		user.setManagedObject(new PooledConnectionManagedObject());
	}

	@Override
	public void returnManagedObject(ManagedObject managedObject) {
		this.returnToPool(managedObject);
	}

	@Override
	public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
		this.returnToPool(managedObject);
	}

	/**
	 * Returns the {@link ManagedObject} {@link Connection} to the pool.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	private void returnToPool(ManagedObject managedObject) {

		// Return the possible transaction connection to pool
		PooledConnectionManagedObject pooled = (PooledConnectionManagedObject) managedObject;
		ConnectionReference reference = pooled.transactionConnection;
		if (reference != null) {

			try {
				// Reset transaction connection for re-use
				reference.connection.rollback();
				reference.connection.setAutoCommit(false);

				// Return connection to pool
				this.pooledConnections.push(reference);

			} catch (SQLException ex) {
				// Failed to return connection to pool
			}
		}
	}

	/*
	 * ================== ThreadCompletionListener ===================
	 */

	@Override
	public void threadComplete() {

		// Return the possible connection to pool
		ConnectionReference reference = this.threadLocalConnection.get();
		if (reference != null) {
			this.threadLocalConnection.set(null);
			this.pooledConnections.push(reference);
		}
	}

	/**
	 * {@link Connection} {@link ManagedObject}.
	 */
	private class PooledConnectionManagedObject extends AbstractConnectionManagedObject implements InvocationHandler {

		/**
		 * Proxied {@link Connection}.
		 */
		private final Connection proxy;

		/**
		 * {@link ConnectionReference} bound to this {@link ManagedObject} as under
		 * transaction.
		 */
		private volatile ConnectionReference transactionConnection = null;

		/**
		 * Instantiate.
		 */
		private PooledConnectionManagedObject() {

			// Create the connection proxy
			this.proxy = (Connection) Proxy.newProxyInstance(ThreadLocalJdbcConnectionPool.this.classLoader,
					PROXY_INTERFACES, this);
		}

		/**
		 * Obtains the {@link ThreadLocalJdbcConnectionPool}.
		 * 
		 * @return {@link ThreadLocalJdbcConnectionPool}.
		 */
		private ThreadLocalJdbcConnectionPool getPool() {
			return ThreadLocalJdbcConnectionPool.this;
		}

		/**
		 * Obtains the {@link ConnectionReference}.
		 * 
		 * @return {@link ConnectionReference}.
		 * @throws SQLException
		 *             If fails to obtain the {@link ConnectionReference}.
		 */
		private ConnectionReference getConnectionReference() throws SQLException {

			// Determine if transaction connection
			ConnectionReference reference = this.transactionConnection;
			if (reference != null) {
				return reference;
			}

			// Obtain the thread local connection
			reference = ThreadLocalJdbcConnectionPool.this.threadLocalConnection.get();

			// Ensure the reference is valid
			if ((reference != null) && (!reference.isValid)) {
				reference = null; // no longer valid connection
			}

			// Ensure if not thread local, that create and associate connection to thread
			if (reference == null) {

				// Obtain the next valid pooled connection
				while ((reference == null) && (!ThreadLocalJdbcConnectionPool.this.pooledConnections.isEmpty())) {
					reference = ThreadLocalJdbcConnectionPool.this.pooledConnections.poll();

					// Determine if valid reference
					if ((reference != null) && (!reference.isValid)) {
						reference = null; // not valid connection
					}
				}

				// If no valid pooled connection, create a new connection
				if (reference == null) {
					PooledConnection pooledConnection = ThreadLocalJdbcConnectionPool.this.dataSource
							.getPooledConnection();
					Connection connection = pooledConnection.getConnection();
					reference = new ConnectionReference(connection, pooledConnection);
				}

				// Register the connection with the thread
				ThreadLocalJdbcConnectionPool.this.threadLocalConnection.set(reference);
			}

			// Return the connection
			return reference;
		}

		/*
		 * =============== AbstractConnectionManagedObject ==============
		 */

		@Override
		protected Connection getConnection() {
			return this.proxy;
		}

		/*
		 * ===================== InvocationHandler ======================
		 */

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			// Obtain the connection reference
			ConnectionReference reference = this.getConnectionReference();

			// Reference to return to pool
			ConnectionReference returnReference = null;

			// Handle specific methods
			switch (method.getName()) {
			case "close":
				return null; // ignore closing

			case "setAutoCommit":
				if (!(Boolean) args[0]) {
					// Within transaction, so lock to managed object
					this.transactionConnection = reference;
					ThreadLocalJdbcConnectionPool.this.threadLocalConnection.set(null);
				} else {
					// No longer transaction
					returnReference = this.transactionConnection;
					this.transactionConnection = null;
				}
			}

			// Obtain the delegate method
			Method delegateMethod = reference.connection.getClass().getMethod(method.getName(),
					method.getParameterTypes());
			try {

				// Invoke method (and retry with force access)
				try {
					return delegateMethod.invoke(reference.connection, args);
				} catch (IllegalAccessException ex) {
					// Access issues, so ensure access
					delegateMethod.setAccessible(true);
					return delegateMethod.invoke(reference.connection, args);
				}

			} finally {
				// Ensure return possible connection to thread / pool
				if (returnReference != null) {

					// Determine if current thread can re-use connection
					// (typically transaction runs on same thread)
					ConnectionReference threadReference = ThreadLocalJdbcConnectionPool.this.threadLocalConnection
							.get();
					if (threadReference == null) {
						// No thread connection, so bind to thread
						ThreadLocalJdbcConnectionPool.this.threadLocalConnection.set(returnReference);

					} else {
						// Thread has connection, so return to the pool
						ThreadLocalJdbcConnectionPool.this.pooledConnections.push(returnReference);
					}
				}
			}
		}
	}

	/**
	 * {@link Connection} reference.
	 */
	private static class ConnectionReference implements ConnectionEventListener {

		/**
		 * {@link Connection}.
		 */
		private final Connection connection;

		/**
		 * {@link PooledConnection}.
		 */
		private final PooledConnection pooledConnection;

		/**
		 * Indicates if the {@link PooledConnection} is valid.
		 */
		private volatile boolean isValid = true;

		/**
		 * Instantiate.
		 * 
		 * @param connection
		 *            {@link Connection}.
		 * @param pooledConnection
		 *            {@link PooledConnection}.
		 */
		private ConnectionReference(Connection connection, PooledConnection pooledConnection) {
			this.connection = connection;
			this.pooledConnection = pooledConnection;

			// Register as listener
			this.pooledConnection.addConnectionEventListener(this);
		}

		/*
		 * =============== ConnectionEventListener ======================
		 */

		@Override
		public void connectionClosed(ConnectionEvent event) {
			// Should not close connection, however leave active with thread
		}

		@Override
		public void connectionErrorOccurred(ConnectionEvent event) {
			if (event.getSource() == this.pooledConnection) {
				this.isValid = false;
			}
		}
	}

}