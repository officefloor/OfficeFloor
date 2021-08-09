/*-
 * #%L
 * JDBC Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionManagedObjectSource
		extends AbstractManagedObjectSource<ConnectionManagedObjectSource.DependencyKeys, None> {

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKeys {
		DATA_SOURCE
	}

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

	/*
	 * =========== AbstractConnectionManagedObjectSource ===========
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
		this.mosContext = context.getManagedObjectSourceContext();

		// Provide meta-data
		context.setObjectClass(Connection.class);
		context.setManagedObjectClass(ConnectionManagedObject.class);
		context.addDependency(DependencyKeys.DATA_SOURCE, DataSource.class);

		// Load close handling
		context.getManagedObjectSourceContext().setDefaultManagedObjectPool(
				(poolContext) -> new DefaultManagedObjectPool(poolContext.getManagedObjectSource(),
						this.mosContext.getLogger()));
		context.getManagedObjectSourceContext().getRecycleFunction(new RecycleFunction()).linkParameter(0,
				RecycleManagedObjectParameter.class);

		// Determine if java compiler
		ClassLoader classLoader = this.mosContext.getClassLoader();
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(this.mosContext);
		if (compiler == null) {

			// Fall back to proxy implementation
			Class<?>[] interfaces = new Class<?>[] { Connection.class, ConnectionWrapper.class };
			this.wrapperFactory = (connection) -> (Connection) Proxy.newProxyInstance(classLoader, interfaces,
					(proxy, method, args) -> {

						// Determine if getting real connection
						if (ConnectionWrapper.isGetRealConnectionMethod(method)) {
							return connection;
						}

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
			Class<?> wrapperClass = compiler.addWrapper(new Class[] { Connection.class, ConnectionWrapper.class },
					Connection.class, null, null, (wrapperContext) -> {
						if (ConnectionWrapper.class.equals(wrapperContext.getInterface())) {
							wrapperContext.write("  return this.delegate;");
						}
						if ("close".equals(wrapperContext.getMethod().getName())) {
							wrapperContext.write(""); // do not close
						}
					}).compile();
			Constructor<?> constructor = wrapperClass.getConstructor(Connection.class);
			this.wrapperFactory = (connection) -> (Connection) constructor.newInstance(connection);
		}
	}

	@Override
	protected ManagedObject getManagedObject() {
		return new ConnectionManagedObject();
	}

	/**
	 * {@link Connection} {@link ManagedObject}.
	 */
	private class ConnectionManagedObject implements CoordinatingManagedObject<DependencyKeys> {

		/**
		 * Actual {@link Connection}.
		 */
		private Connection connection;

		/**
		 * {@link Proxy} {@link Connection}.
		 */
		private Connection proxy;

		/*
		 * =================== ManagedObject ========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {

			// Obtain the dependencies
			DataSource dataSource = (DataSource) registry.getObject(DependencyKeys.DATA_SOURCE);

			// Obtain the connection
			this.connection = dataSource.getConnection();

			// Ensure within transaction
			if (this.connection.getAutoCommit()) {
				this.connection.setAutoCommit(false);
			}

			// Create proxy around connection
			this.proxy = ConnectionManagedObjectSource.this.wrapperFactory.wrap(this.connection);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.proxy;
		}
	}

	/**
	 * Recycles the {@link Connection}.
	 */
	private static class RecycleFunction extends StaticManagedFunction<Indexed, None> {

		@Override
		public void execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the connection
			RecycleManagedObjectParameter<ConnectionManagedObject> recycle = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(context);
			Connection connection = recycle.getManagedObject().connection;

			// Obtain the real connection
			connection = ConnectionWrapper.getRealConnection(connection);
			if (connection == null) {
				// No "real" connection, so no need to clean up
				recycle.reuseManagedObject();
				return;
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
		 * {@link Logger}.
		 */
		private final Logger logger;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSource {@link ManagedObjectSource}.
		 */
		public DefaultManagedObjectPool(ManagedObjectSource<?, ?> managedObjectSource, Logger logger) {
			this.managedObjectSource = managedObjectSource;
			this.logger = logger;
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
				this.logger.log(Level.WARNING, "Failed to close connection", ex);
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

		@Override
		public void empty() {
			// Does not pool, so always empty
		}
	}

}
