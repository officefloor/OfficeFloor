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
package net.officefloor.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;

/**
 * {@link ManagedObjectSource} for {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionManagedObjectSource extends AbstractConnectionManagedObjectSource {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConnectionManagedObjectSource.class.getName());

	/**
	 * Wrapper factory.
	 */
	private static interface WrapperFactory {

		/**
		 * Wraps the {@link Connection}.
		 * 
		 * @param connection {@link Connection}.
		 * @return Wrapped {@link Connection}.
		 * @throws Exception If fails to wrap the {@link Connection}.
		 */
		Connection wrap(Connection connection) throws Exception;
	}

	/**
	 * {@link WrapperFactory}.
	 */
	private WrapperFactory wrapperFactory;

	/**
	 * {@link ManagedObjectSourceContext}.
	 */
	private ManagedObjectSourceContext<None> mosContext;

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * Obtains the {@link ConnectionPoolDataSource}.
	 * 
	 * @return {@link ConnectionPoolDataSource}.
	 * @throws Exception If fails to create the {@link ConnectionPoolDataSource}.
	 */
	public ConnectionPoolDataSource getConnectionPoolDataSource() throws Exception {
		ConnectionPoolDataSourceFactory factory = this.getConnectionPoolDataSourceFactory(this.mosContext);
		return factory.createConnectionPoolDataSource(this.mosContext);
	}

	/*
	 * =========== AbstractConnectionManagedObjectSource ===========
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadFurtherMetaData(MetaDataContext<None, None> context) throws Exception {
		this.mosContext = context.getManagedObjectSourceContext();

		// Load close handling
		context.getManagedObjectSourceContext().setDefaultManagedObjectPool(
				(poolContext) -> new DefaultManagedObjectPool(poolContext.getManagedObjectSource()));
		context.getManagedObjectSourceContext().getRecycleFunction(new RecycleFunction()).linkParameter(0,
				RecycleManagedObjectParameter.class);

		// Determine if java compiler
		ClassLoader classLoader = this.mosContext.getClassLoader();
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(classLoader);
		if (compiler == null) {

			// Fall back to proxy implementation
			Class<?>[] interfaces = new Class<?>[] { Connection.class };
			this.wrapperFactory = (connection) -> (Connection) Proxy.newProxyInstance(classLoader, interfaces,
					(proxy, method, args) -> {

						// As managed, can not change particular methods
						switch (method.getName()) {
						case "close":
							return null; // do not close
						}

						// Undertake method
						return connection.getClass().getMethod(method.getName(), method.getParameterTypes())
								.invoke(connection, args);
					});

		} else {
			// Use compiled wrapper
			Class<?> wrapperClass = compiler.addWrapper(Connection.class, (wrapperContext) -> {
				if ("close".equals(wrapperContext.getMethod().getName())) {
					wrapperContext.write(""); // do not close
				}
			}).compile();
			Constructor<?> constructor = wrapperClass.getConstructor(Connection.class);
			this.wrapperFactory = (connection) -> (Connection) constructor.newInstance(connection);
		}

		// Create the data source
		DataSourceFactory factory = this.getDataSourceFactory(this.mosContext);
		this.dataSource = factory.createDataSource(this.mosContext);

		// Provide connectivity
		this.setConnectivity(() -> this.dataSource.getConnection());
	}

	@Override
	protected ManagedObject getManagedObject() {
		return new ConnectionManagedObject();
	}

	/**
	 * {@link Connection} {@link ManagedObject}.
	 */
	private class ConnectionManagedObject extends AbstractConnectionManagedObject {

		/**
		 * {@link Proxy} {@link Connection}.
		 */
		private Connection proxy;

		@Override
		protected Connection getConnection() throws Throwable {

			// Lazy create the connection
			if (this.proxy == null) {

				// Obtain the connection
				this.connection = ConnectionManagedObjectSource.this.dataSource.getConnection();

				// Ensure within transaction
				if (this.connection.getAutoCommit()) {
					this.connection.setAutoCommit(false);
				}

				// Create proxy around connection
				this.proxy = ConnectionManagedObjectSource.this.wrapperFactory.wrap(this.connection);
			}

			// Return the proxy connection
			return this.proxy;
		}

		@Override
		public Object getObject() throws Throwable {
			return this.getConnection();
		}
	}

	/**
	 * Recycles the {@link Connection}.
	 */
	private static class RecycleFunction extends StaticManagedFunction<Indexed, None> {

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the connection
			RecycleManagedObjectParameter<AbstractConnectionManagedObject> recycle = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(context);
			Connection connection = recycle.getManagedObject().connection;

			// Determine if real connection to recycle
			if (connection instanceof ConnectionRecycleWrapper) {
				ConnectionRecycleWrapper wrapper = (ConnectionRecycleWrapper) connection;
				if (!wrapper.isRealConnection()) {
					// No "real" connection, so no need to clean up
					recycle.reuseManagedObject();
					return null;
				}
			}

			// Determine if within transaction
			if (!connection.getAutoCommit()) {

				// If clean up escalation, then rollback (otherwise commit)
				CleanupEscalation[] escalations = recycle.getCleanupEscalations();
				if ((escalations != null) && (escalations.length > 0)) {
					// Escalations, so rollback transaction
					connection.rollback();
				} else {
					// No escalations, so commit the transaction
					connection.commit();
				}
			}

			// Reuse the connection
			recycle.reuseManagedObject();

			// Nothing further
			return null;
		}
	}

	/**
	 * Default {@link ManagedObjectPool} for the
	 * {@link ConnectionManagedObjectSource}.
	 */
	private static class DefaultManagedObjectPool implements ManagedObjectPool {

		/**
		 * {@link ManagedObjectSource}.
		 */
		private final ManagedObjectSource<?, ?> managedObjectSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSource {@link ManagedObjectSource}.
		 */
		public DefaultManagedObjectPool(ManagedObjectSource<?, ?> managedObjectSource) {
			this.managedObjectSource = managedObjectSource;
		}

		/**
		 * Closes the {@link Connection}.
		 * 
		 * @param managedObject {@link ConnectionManagedObject}.
		 */
		private void closeConnection(ManagedObject managedObject) {
			try {
				((ConnectionManagedObject) managedObject).connection.close();
			} catch (SQLException ex) {
				LOGGER.log(Level.WARNING, "Failed to close connection", ex);
			}
		}

		/*
		 * ============== ManagedObjectPool ====================
		 */

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			this.managedObjectSource.sourceManagedObject(user);
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			this.closeConnection(managedObject);
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			this.closeConnection(managedObject);
		}
	}

}