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
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

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
import net.officefloor.jdbc.ConnectionRecycleWrapper;

/**
 * {@link Connection} {@link ManagedObjectPool} implementation that uses
 * {@link ThreadLocal} instances to reduce contention of retrieving
 * {@link Connection} from singleton pool.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalJdbcConnectionPool implements ManagedObjectPool, ThreadCompletionListener {

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
		 * Indicates if real {@link Connection}.
		 * 
		 * @return <code>true</code> if real {@link Connection}.
		 */
		boolean isRealConnection();

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
	public static interface CompiledConnectionWrapper {

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
			Class<?>[] interfaces = new Class[] { Connection.class, ConnectionRecycleWrapper.class };
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
					+ compiler.getSourceName(Connection.class) + ", "
					+ compiler.getSourceName(ConnectionRecycleWrapper.class) + ","
					+ compiler.getSourceName(CompiledConnectionWrapper.class) + " {");

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
			compiler.writeMethodSignature(source, ConnectionRecycleWrapper.isRealConnectionMethod());
			source.println(" {");
			source.println("    return this.context.isRealConnection();");
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
					compiler.writeDelegateMethodImplementation(source,
							"this.context.getConnectionReference().getConnection()", method);
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
	 * {@link ThreadLocal} {@link Connection} for this
	 * {@link ThreadLocalJdbcConnectionPool}.
	 */
	private final ThreadLocal<ConnectionReferenceImpl> threadLocalConnection = new ThreadLocal<>();

	/**
	 * {@link ConnectionReferenceImpl} instances idle within the pool.
	 */
	private final Deque<ConnectionReferenceImpl> pooledConnections = new ConcurrentLinkedDeque<>();

	/**
	 * Instantiate.
	 * 
	 * @param dataSource     {@link ConnectionPoolDataSource}.
	 * @param wrapperFactory {@link PooledConnectionWrapperFactory}.
	 */
	public ThreadLocalJdbcConnectionPool(ConnectionPoolDataSource dataSource,
			PooledConnectionWrapperFactory wrapperFactory) {
		this.dataSource = dataSource;
		this.wrapperFactory = wrapperFactory;
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

				// Synchronize connection into pool
				synchronized (reference) {
					this.pooledConnections.push(reference);
				}

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
		ConnectionReferenceImpl reference = this.threadLocalConnection.get();
		if (reference != null) {
			this.threadLocalConnection.set(null);

			// Synchronize connection into pool
			synchronized (reference) {
				this.pooledConnections.push(reference);
			}
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
		public boolean isRealConnection() {
			/*
			 * Attempt to recycle (clean transaction). Therefore, only "real" connection if
			 * within transaction.
			 */
			return (this.transactionConnection != null);
		}

		@Override
		public ConnectionReference getConnectionReference() throws SQLException {

			// Determine if transaction connection
			ConnectionReferenceImpl reference = this.transactionConnection;
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
				boolean isAvailable = true;
				do {
					reference = ThreadLocalJdbcConnectionPool.this.pooledConnections.poll();
					isAvailable = (reference != null);

					// Determine if valid reference
					if ((isAvailable) && (!reference.isValid)) {
						reference = null; // not valid connection
					}
				} while ((isAvailable) && (reference == null));

				// Determine if have reference from pool
				if (reference != null) {
					// Ensure consistent state with other thread
					synchronized (reference) {
					}

				} else {
					// No valid pooled connection, create a new connection

					// Obtain the connection
					PooledConnection pooledConnection = ThreadLocalJdbcConnectionPool.this.dataSource
							.getPooledConnection();
					Connection connection = pooledConnection.getConnection();

					// Ensure not within transaction
					if (!connection.getAutoCommit()) {
						connection.setAutoCommit(true);
					}

					// Create reference to the connection
					reference = new ConnectionReferenceImpl(connection, pooledConnection);
				}

				// Register the connection with the thread
				ThreadLocalJdbcConnectionPool.this.threadLocalConnection.set(reference);
			}

			// Return the connection
			return reference;
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
						synchronized (returnReference) {
							ThreadLocalJdbcConnectionPool.this.pooledConnections.push(returnReference);
						}
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
			if (ConnectionRecycleWrapper.isRealConnectionMethod(method)) {
				return this.isRealConnection();
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