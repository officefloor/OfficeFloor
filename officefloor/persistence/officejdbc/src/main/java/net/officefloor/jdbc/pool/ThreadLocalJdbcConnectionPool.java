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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler;
import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler.ClassName;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.jdbc.AbstractConnectionManagedObject;
import net.officefloor.jdbc.ConnectionWrapper;

/**
 * {@link Connection} {@link ManagedObjectPool} implementation that uses
 * {@link ThreadLocal} instances to reduce contention of retrieving
 * {@link Connection} from singleton pool.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalJdbcConnectionPool implements ManagedObjectPool, ThreadCompletionListener {

	/**
	 * Creates the {@link PooledConnectionWrapperFactory}.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 * @return {@link PooledConnectionWrapperFactory}.
	 * @throws Exception If fails to create {@link PooledConnectionWrapperFactory}.
	 */
	public static PooledConnectionWrapperFactory createWrapperFactory(ClassLoader classLoader) throws Exception {

		// Determine if compiler available
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(classLoader);
		if (compiler == null) {

			// Fall back to proxy implementation
			Class<?>[] interfaces = new Class[] { Connection.class, ConnectionWrapper.class };
			return (wrapperContext) -> (Connection) Proxy.newProxyInstance(classLoader, interfaces, wrapperContext);

		} else {
			// Use compiler to create wrapper
			StringWriter sourceBuffer = new StringWriter();
			PrintWriter source = new PrintWriter(sourceBuffer);

			// Obtain the class name
			ClassName className = compiler.createClassName(Connection.class.getName());

			// Write the class definition
			source.println("package " + className.getPackageName() + ";");
			source.println("@" + compiler.getSourceName(SuppressWarnings.class) + "(\"unchecked\")");
			source.println("public class " + className.getClassName() + " implements "
					+ compiler.getSourceName(Connection.class) + ", " + compiler.getSourceName(ConnectionWrapper.class)
					+ "," + compiler.getSourceName(CompiledConnectionWrapper.class) + " {");

			// Write the constructor
			compiler.writeConstructor(source, className.getClassName(),
					compiler.createField(PooledConnectionContext.class, "context"));

			// Wrapper to obtain underlying connection
			source.println("  public " + compiler.getSourceName(Connection.class) + " _getConnection() throws "
					+ compiler.getSourceName(SQLException.class) + " {");
			source.println("    return this.context.getConnectionReference().getConnection();");
			source.println("  }");

			// Wrapper to obtain underlying pool
			source.println("  public " + compiler.getSourceName(ThreadLocalJdbcConnectionPool.class)
					+ " _getPool() throws " + compiler.getSourceName(SQLException.class) + " {");
			source.println("    return this.context.getPool();");
			source.println("  }");

			// Implement isRealConnection
			source.print("  public ");
			compiler.writeMethodSignature(source, ConnectionWrapper.getRealConnectionMethod());
			source.println(" {");
			source.println("    return this.context.getRealConnection();");
			source.println("  }");

			// Write the methods
			for (Method method : Connection.class.getMethods()) {

				// Write method signature
				source.print("  public ");
				compiler.writeMethodSignature(source, method);
				source.println(" {");

				// Handle specific methods
				switch (method.getName()) {
				case "close":
					break; // no operation
				case "setAutoCommit":
					source.println("    this.context.setAutoCommit(this.context.getConnectionReference(), p0);");
					break;
				case "setClientInfo":
					source.println("    throw new " + compiler.getSourceName(SQLClientInfoException.class)
							+ " (\"Can not set client info on thread local connection\", null);");
					break;
				default:
					// Default implementation
					compiler.writeMethodImplementation(source, "this.context.getConnectionReference().getConnection()",
							method);
					break;
				}

				// Complete method
				source.println("  }");
			}

			// Complete class
			source.print("}");
			source.flush();

			// Compile the class
			Class<?> wrapperClass = compiler.addSource(className, sourceBuffer.toString()).compile();
			Constructor<?> constructor = wrapperClass.getConstructor(PooledConnectionContext.class);
			return (wrapperContext) -> (Connection) constructor.newInstance(wrapperContext);
		}
	}

	/**
	 * Obtains the delegate {@link Connection} from the proxy {@link Connection}.
	 * 
	 * @param connection Proxy {@link Connection}.
	 * @return Delegate {@link Connection}.
	 * @throws SQLException If failure in obtaining the delegate {@link Connection}.
	 */
	static Connection extractDelegateConnection(Connection connection) throws SQLException {
		if (Proxy.isProxyClass(connection.getClass())) {
			// Extract from proxy
			PooledConnectionManagedObject managedObject = (PooledConnectionManagedObject) Proxy
					.getInvocationHandler(connection);
			return managedObject.getConnectionReference().getConnection();
		} else {
			// Extract from compiled connection wrapper
			return ((CompiledConnectionWrapper) connection)._getConnection();
		}
	}

	/**
	 * Obtains the {@link ThreadLocalJdbcConnectionPool} for the proxy
	 * {@link Connection}.
	 * 
	 * @param connection Proxy {@link Connection}.
	 * @return {@link ThreadLocalJdbcConnectionPool}.
	 * @throws SQLException If failure in obtaining the delegate
	 *                      {@link ThreadLocalJdbcConnectionPool}.
	 */
	static ThreadLocalJdbcConnectionPool extractConnectionPool(Connection connection) throws SQLException {
		if (Proxy.isProxyClass(connection.getClass())) {
			// Extract from proxy
			PooledConnectionManagedObject managedObject = (PooledConnectionManagedObject) Proxy
					.getInvocationHandler(connection);
			return managedObject.getPool();
		} else {
			// Extract from compiled connection wrapper
			return ((CompiledConnectionWrapper) connection)._getPool();
		}
	}

	/**
	 * {@link ConnectionPoolDataSource}.
	 */
	private final ConnectionPoolDataSource dataSource;

	/**
	 * {@link PooledConnectionWrapperFactory}.
	 */
	private final PooledConnectionWrapperFactory wrapperFactory;

	/**
	 * Maximum number of connections.
	 */
	private final int maximumConnections;

	/**
	 * Wait time in milliseconds for a {@link Connection} to become available.
	 */
	private final long connectionWaitTime;

	/**
	 * {@link ThreadLocal} {@link Connection} for this
	 * {@link ThreadLocalJdbcConnectionPool}.
	 */
	private final ThreadLocal<ConnectionReferenceImpl> threadLocalConnection = new ThreadLocal<>();

	/**
	 * {@link ConnectionReferenceImpl} instances idle within the pool.
	 */
	private final BlockingQueue<ConnectionReferenceImpl> pooledConnections;

	/**
	 * <p>
	 * All {@link ConnectionReferenceImpl} instances.
	 * <p>
	 * Some {@link Thread} instances may not have completed yet leaving a
	 * {@link PooledConnection} not in the idle list for closing. This tracks all
	 * {@link PooledConnection} instances created to ensure they are all closed.
	 */
	private final List<ConnectionReferenceImpl> allPooledConnections;

	/**
	 * Number of active {@link Connection} instances.
	 */
	private int connectionCount = 0;

	/**
	 * Instantiate.
	 * 
	 * @param dataSource         {@link ConnectionPoolDataSource}.
	 * @param wrapperFactory     {@link PooledConnectionWrapperFactory}.
	 * @param maximumConnections Maximum number of connections. <code>0</code> (or
	 *                           negative) for unbounded number of
	 *                           {@link Connection} instances.
	 * @param connectionWaitTime Wait time in milliseconds for a {@link Connection}
	 *                           to become available.
	 */
	public ThreadLocalJdbcConnectionPool(ConnectionPoolDataSource dataSource,
			PooledConnectionWrapperFactory wrapperFactory, int maximumConnections, long connectionWaitTime) {
		this.dataSource = dataSource;
		this.wrapperFactory = wrapperFactory;

		// Provide maximum number of connections
		if (maximumConnections < 0) {
			maximumConnections = 0; // unbounded
		}
		this.maximumConnections = maximumConnections;
		this.connectionWaitTime = connectionWaitTime;

		// Create the queue of idle connections
		// (attempt to be fair, however must also handled unbounded connections)
		this.pooledConnections = (this.maximumConnections == 0) ? new LinkedBlockingQueue<>()
				: new ArrayBlockingQueue<>(this.maximumConnections, true);
		this.allPooledConnections = (this.maximumConnections == 0) ? new LinkedList<>()
				: new ArrayList<>(this.maximumConnections);
	}

	/**
	 * Obtains the {@link ThreadLocal} {@link Connection}.
	 * 
	 * @return {@link ThreadLocal} {@link Connection}.
	 */
	public Connection getThreadLocalConnection() {
		ConnectionReferenceImpl reference = this.threadLocalConnection.get();
		return (reference == null ? null : reference.connection);
	}

	/**
	 * Ensures the {@link ConnectionReferenceImpl} is valid.
	 * 
	 * @param reference {@link ConnectionReferenceImpl}.
	 * @return {@link ConnectionReferenceImpl} or <code>null</code> if not valid.
	 */
	private ConnectionReferenceImpl ensureValid(ConnectionReferenceImpl reference) {

		// Determine if have reference
		if (reference == null) {
			return null;
		}

		// Determine if valid
		if (reference.isValid) {
			return reference; // valid so use
		}

		// Remove the connection
		synchronized (this.allPooledConnections) {
			this.allPooledConnections.remove(reference);
			this.connectionCount--;
		}

		// Clean up reference
		try {
			reference.pooledConnection.close();
		} catch (SQLException ex) {
			// Ignore error, as just attempting to clean up invalid connection
		}

		// No valid connection
		return null;
	}

	/**
	 * <p>
	 * Polls immediately for a {@link ConnectionReferenceImpl}.
	 * <p>
	 * This handles obtaining a valid {@link ConnectionReferenceImpl}.
	 * 
	 * @return {@link ConnectionReferenceImpl} or <code>null</code> if failed to
	 *         obtain valid {@link ConnectionReferenceImpl} immediately.
	 */
	private ConnectionReferenceImpl pollValid() {

		// Attempt to obtain immediately from idle pool
		boolean isRetrieved = false;
		do {
			ConnectionReferenceImpl reference = this.pooledConnections.poll();
			isRetrieved = (reference != null);

			// Determine if valid
			reference = this.ensureValid(reference);
			if (reference != null) {
				return reference; // use idle connection
			}
		} while (isRetrieved);

		// As nothing retrieved from idle
		return null;
	}

	/*
	 * =================== ManagedObjectPool =====================
	 */

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		try {
			user.setManagedObject(new PooledConnectionManagedObject());
		} catch (Exception ex) {
			user.setFailure(ex);
		}
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
	 * @param managedObject {@link ManagedObject}.
	 */
	private void returnToPool(ManagedObject managedObject) {

		// Return the possible transaction connection to pool
		PooledConnectionManagedObject pooled = (PooledConnectionManagedObject) managedObject;
		ConnectionReferenceImpl reference = pooled.transactionConnection;
		if (reference != null) {

			try {
				// Reset transaction connection for re-use
				reference.connection.rollback();
				reference.connection.setAutoCommit(true);

				// Add to pool
				this.pooledConnections.add(reference);

			} catch (SQLException ex) {
				// Failed to return connection to pool
			}
		}
	}

	@Override
	public void empty() {

		// Close all the pooled connections
		synchronized (this.allPooledConnections) {
			for (ConnectionReferenceImpl reference : this.allPooledConnections) {
				try {
					reference.pooledConnection.close();
				} catch (SQLException ex) {
					// Ignore error in closing (as shutting down)
				}
			}
			this.pooledConnections.clear();
			this.allPooledConnections.clear();
			this.connectionCount = -1; // flag closed
		}
	}

	/*
	 * ================== ThreadCompletionListener ===================
	 */

	@Override
	public void threadComplete() {

		// Return the possible connection to pool
		ConnectionReferenceImpl reference = this.threadLocalConnection.get();
		if (reference != null) {
			this.threadLocalConnection.set(null);

			// Add to pool
			this.pooledConnections.add(reference);
		}
	}

	/**
	 * {@link Connection} {@link ManagedObject}.
	 */
	private class PooledConnectionManagedObject extends AbstractConnectionManagedObject
			implements PooledConnectionContext {

		/**
		 * Proxied {@link Connection}.
		 */
		private final Connection proxy;

		/**
		 * {@link ConnectionReferenceImpl} bound to this {@link ManagedObject} as under
		 * transaction.
		 */
		private volatile ConnectionReferenceImpl transactionConnection = null;

		/**
		 * Instantiate.
		 * 
		 * @throws Exception If fails to create {@link PooledConnectionManagedObject}.
		 */
		private PooledConnectionManagedObject() throws Exception {

			// Create the connection proxy
			this.proxy = ThreadLocalJdbcConnectionPool.this.wrapperFactory.wrap(this);
		}

		/*
		 * =============== AbstractConnectionManagedObject ==============
		 */

		@Override
		protected Connection getConnection() {
			return this.proxy;
		}

		/*
		 * ================== PooledConnectionContext ====================
		 */

		@Override
		public Connection getRealConnection() {
			/*
			 * Attempt to recycle (clean transaction). Therefore, only "real" connection if
			 * within transaction.
			 */
			ConnectionReferenceImpl real = this.transactionConnection;
			return (real != null) ? real.connection : null;
		}

		@Override
		public ConnectionReference getConnectionReference() throws SQLException {

			// Determine if transaction connection
			ConnectionReferenceImpl reference = this.transactionConnection;
			if (reference != null) {
				return reference;
			}

			// Easy access to pool
			ThreadLocalJdbcConnectionPool pool = ThreadLocalJdbcConnectionPool.this;

			// Obtain the thread local connection
			reference = pool.ensureValid(ThreadLocalJdbcConnectionPool.this.threadLocalConnection.get());
			if (reference != null) {
				return reference; // use thread local connection
			}

			// Attempt to obtain immediately from idle pool
			reference = pool.pollValid();
			if (reference != null) {
				pool.threadLocalConnection.set(reference);
				return reference; // use idle connection
			}

			// Determine if able to create connection
			boolean isCreateConnection = (pool.maximumConnections == 0);
			synchronized (pool.allPooledConnections) {

				// Determine if closed
				if (pool.connectionCount < 0) {
					throw new SQLException(ThreadLocalJdbcConnectionPool.class.getSimpleName() + " is closed");
				}

				// Determine if reached limit
				// Allows connections to be created outside lock in parallel
				if (!isCreateConnection) {
					isCreateConnection = (pool.connectionCount < pool.maximumConnections);
					pool.connectionCount++;
				}
			}

			// Determine if able to create connection
			if (isCreateConnection) {
				try {

					// No valid pooled connection, create a new connection
					PooledConnection pooledConnection = ThreadLocalJdbcConnectionPool.this.dataSource
							.getPooledConnection();
					Connection connection = pooledConnection.getConnection();

					// Ensure not within transaction
					if (!connection.getAutoCommit()) {
						connection.setAutoCommit(true);
					}

					// Create and return reference to the connection
					reference = new ConnectionReferenceImpl(connection, pooledConnection);
					synchronized (pool.allPooledConnections) {
						pool.allPooledConnections.add(reference);
					}
					pool.threadLocalConnection.set(reference);
					return reference;

				} catch (SQLException | RuntimeException | Error ex) {
					// Failed to create connection (so decrement count)
					synchronized (pool.allPooledConnections) {
						pool.connectionCount--;
					}
				}
			}

			// Block waiting for connection (as maximum connections reached)
			try {
				reference = pool
						.ensureValid(pool.pooledConnections.poll(pool.connectionWaitTime, TimeUnit.MILLISECONDS));
			} catch (InterruptedException ex) {
				throw new SQLException(ex);
			}
			if (reference == null) {
				// Reference not valid, so give last chance to obtain (but wait no further)
				reference = pool.pollValid();
			}
			if (reference != null) {
				pool.threadLocalConnection.set(reference);
				return reference; // use returned connection to idle connection
			}

			// As here, timed out in obtaining connection
			throw new SQLException("Timed out after " + pool.connectionWaitTime + " milliseconds to obtain "
					+ Connection.class.getSimpleName());
		}

		@Override
		public void setAutoCommit(ConnectionReference reference, boolean isAutoCommit) throws SQLException {

			// Reference to return to pool
			ConnectionReferenceImpl returnReference = null;
			if (!isAutoCommit) {
				// Within transaction, so lock to managed object
				if (this.transactionConnection == null) {
					// Initiating the transaction
					this.transactionConnection = (ConnectionReferenceImpl) reference;
					ThreadLocalJdbcConnectionPool.this.threadLocalConnection.set(null);
				}
			} else {
				// No longer transaction
				if (this.transactionConnection != null) {
					// Releasing transaction
					returnReference = this.transactionConnection;
					this.transactionConnection = null;
				}
			}
			boolean isResetSuccessful = false;
			try {
				reference.getConnection().setAutoCommit(isAutoCommit);
				isResetSuccessful = true;
			} finally {
				// Ensure return possible connection to thread / pool
				if (returnReference != null) {

					// Determine if current thread can re-use connection
					// (typically transaction runs on same thread)
					ConnectionReferenceImpl threadReference = ThreadLocalJdbcConnectionPool.this.threadLocalConnection
							.get();
					if ((isResetSuccessful) && (threadReference == null)) {
						// No thread connection, so bind to thread
						ThreadLocalJdbcConnectionPool.this.threadLocalConnection.set(returnReference);

					} else {
						// Thread has connection (or invalid reset), so return to the pool
						ThreadLocalJdbcConnectionPool.this.pooledConnections.add(returnReference);
					}
				}
			}
		}

		@Override
		public ThreadLocalJdbcConnectionPool getPool() {
			return ThreadLocalJdbcConnectionPool.this;
		}

		/*
		 * ===================== InvocationHandler ======================
		 */

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			// Handle recycle wrapper
			if (ConnectionWrapper.isGetRealConnectionMethod(method)) {
				return this.getRealConnection();
			}

			// Obtain the connection reference
			ConnectionReferenceImpl reference = (ConnectionReferenceImpl) this.getConnectionReference();

			// Handle specific methods
			switch (method.getName()) {
			case "close":
				return null; // ignore closing

			case "setAutoCommit":
				this.setAutoCommit(reference, (boolean) args[0]);
				return null; // auto-commit managed
			}

			// Obtain the delegate method
			Method delegateMethod = reference.connection.getClass().getMethod(method.getName(),
					method.getParameterTypes());

			// Invoke method
			try {
				return delegateMethod.invoke(reference.connection, args);
			} catch (IllegalAccessException ex) {
				delegateMethod.setAccessible(true);
				return delegateMethod.invoke(reference.connection, args);
			}
		}
	}

	/**
	 * {@link Connection} reference.
	 */
	public static class ConnectionReferenceImpl implements ConnectionReference, ConnectionEventListener {

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
		 * @param connection       {@link Connection}.
		 * @param pooledConnection {@link PooledConnection}.
		 */
		private ConnectionReferenceImpl(Connection connection, PooledConnection pooledConnection) {
			this.connection = connection;
			this.pooledConnection = pooledConnection;

			// Register as listener
			this.pooledConnection.addConnectionEventListener(this);
		}

		/*
		 * ================= ConnectionReference ========================
		 */

		@Override
		public Connection getConnection() {
			return this.connection;
		}

		/*
		 * =============== ConnectionEventListener ======================
		 */

		@Override
		public void connectionClosed(ConnectionEvent event) {
			// Should not get closed events (either way, ignore as handled otherwise)
		}

		@Override
		public void connectionErrorOccurred(ConnectionEvent event) {
			this.isValid = false;
		}
	}

	/**
	 * Reference to a {@link Connection}.
	 */
	public static interface ConnectionReference {

		/**
		 * Obtains the {@link Connection}.
		 * 
		 * @return {@link Connection}.
		 */
		Connection getConnection();
	}

	/**
	 * Context for the pooled {@link Connection}.
	 */
	public static interface PooledConnectionContext extends InvocationHandler {

		/**
		 * Obtains the real {@link Connection}.
		 * 
		 * @return The real {@link Connection} or <code>null</code> if no real
		 *         {@link Connection}.
		 */
		Connection getRealConnection();

		/**
		 * Obtains the {@link ConnectionReference}.
		 * 
		 * @return {@link ConnectionReference}.
		 * @throws SQLException If fails to obtain the {@link ConnectionReference}.
		 */
		ConnectionReference getConnectionReference() throws SQLException;

		/**
		 * Sets auto-commit on the {@link Connection}.
		 * 
		 * @param reference    {@link ConnectionReference} to the {@link Connection}.
		 * @param isAutoCommit Indicates whether setting/unsetting auto-commit.
		 * @throws SQLException If fails to set auto-commit.
		 */
		void setAutoCommit(ConnectionReference reference, boolean isAutoCommit) throws SQLException;

		/**
		 * Obtains the {@link ThreadLocalJdbcConnectionPool}.
		 * 
		 * @return {@link ThreadLocalJdbcConnectionPool}.
		 */
		ThreadLocalJdbcConnectionPool getPool();
	}

	/**
	 * Interface on compiled {@link Connection} wrapper extract details of the
	 * {@link Connection}.
	 */
	public static interface CompiledConnectionWrapper extends ConnectionWrapper {

		/**
		 * Obtains the {@link Connection}.
		 * 
		 * @return {@link Connection}.
		 * @throws SQLException If fails to {@link Connection}.
		 */
		Connection _getConnection() throws SQLException;

		/**
		 * Obtains the {@link ThreadLocalJdbcConnectionPool} for the {@link Connection}.
		 * 
		 * @return {@link ThreadLocalJdbcConnectionPool} for the {@link Connection}.
		 * @throws SQLException If fails to obtain
		 *                      {@link ThreadLocalJdbcConnectionPool}.
		 */
		ThreadLocalJdbcConnectionPool _getPool() throws SQLException;
	}

	/**
	 * Factory to create wrapper for the {@link PooledConnection}.
	 */
	public static interface PooledConnectionWrapperFactory {

		/**
		 * Create the {@link Connection} wrapper.
		 * 
		 * @param context {@link PooledConnectionContext}.
		 * @return Wrapped {@link Connection}.
		 * @throws Exception If fails to wrap the {@link Connection}.
		 */
		Connection wrap(PooledConnectionContext context) throws Exception;
	}

}