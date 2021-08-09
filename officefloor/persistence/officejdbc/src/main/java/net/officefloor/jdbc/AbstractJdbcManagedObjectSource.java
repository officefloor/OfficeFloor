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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler.JavaSource;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceTransformer;
import net.officefloor.jdbc.datasource.DataSourceTransformerContext;
import net.officefloor.jdbc.datasource.DataSourceTransformerServiceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.decorate.ConnectionDecorator;
import net.officefloor.jdbc.decorate.ConnectionDecoratorServiceFactory;
import net.officefloor.jdbc.decorate.PooledConnectionDecorator;
import net.officefloor.jdbc.decorate.PooledConnectionDecoratorServiceFactory;

/**
 * Abstract {@link ManagedObjectSource} for {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJdbcManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link Property} name to specify the {@link DataSourceFactory}
	 * implementation.
	 */
	public static final String PROPERTY_DATA_SOURCE_FACTORY = "datasource.factory";

	/**
	 * {@link Property} name to specify the SQL to run to validate the
	 * {@link DataSource} is configured correctly.
	 */
	public static final String PROPERTY_DATA_SOURCE_VALIDATE_SQL = "datasource.validate.sql";

	/**
	 * Creates the {@link DataSource}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Created {@link DataSource}.
	 * @throws Exception If fails to create the {@link DataSource}.
	 */
	protected final DataSource newDataSource(SourceContext context) throws Exception {

		// Obtain the data source
		DataSourceFactory dataSourceFactory = this.getDataSourceFactory(context);
		DataSource dataSource = dataSourceFactory.createDataSource(context);

		// Transform the data source
		for (DataSourceTransformer transformer : context
				.loadOptionalServices(DataSourceTransformerServiceFactory.class)) {

			// Undertake transform of data source
			DataSource finalDataSource = dataSource;
			dataSource = transformer.transformDataSource(new DataSourceTransformerContext() {

				@Override
				public DataSource getDataSource() {
					return finalDataSource;
				}

				@Override
				public SourceContext getSourceContext() {
					return context;
				}
			});

			// Ensure have resulting transformed data source
			if (dataSource == null) {
				throw new IllegalStateException("No " + DataSource.class.getSimpleName() + " provided from "
						+ DataSourceTransformer.class.getSimpleName() + " " + transformer.getClass().getName());
			}
		}
		DataSource finalDataSource = dataSource;

		// Obtain the decorator factories
		List<ConnectionDecorator> decoratorList = new ArrayList<>();
		for (ConnectionDecorator decorator : context.loadOptionalServices(ConnectionDecoratorServiceFactory.class)) {
			decoratorList.add(decorator);
		}
		ConnectionDecorator[] decorators = decoratorList.toArray(new ConnectionDecorator[decoratorList.size()]);

		// Determine if require decorating connections
		if (decorators.length == 0) {
			return dataSource;
		}

		// Provide compiled DataSource
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(context);
		if (compiler == null) {

			// Fall back to proxy implementation
			return (DataSource) Proxy.newProxyInstance(context.getClassLoader(),
					new Class[] { DataSource.class, DataSourceWrapper.class }, (object, method, args) -> {

						// Determine if real DataSource method
						if (DataSourceWrapper.isGetRealDataSourceMethod(method)) {
							return finalDataSource;
						}

						// Undertake DataSource methods
						Method dataSourceMethod = finalDataSource.getClass().getMethod(method.getName(),
								method.getParameterTypes());
						Object result = dataSourceMethod.invoke(finalDataSource, args);
						if ("getConnection".equals(method.getName())) {

							// Decorate the connection
							Connection connection = (Connection) result;
							for (int i = 0; i < decorators.length; i++) {
								connection = decorators[i].decorate(connection);
							}

							// Return the decorated connection
							result = connection;
						}
						return result;
					});

		} else {
			// Provide compiled wrapper implementation
			JavaSource javaSource = compiler.addWrapper(new Class[] { DataSource.class, DataSourceWrapper.class },
					DataSource.class, "this.delegate", (constructorContext) -> {
						compiler.writeConstructor(constructorContext.getSource(),
								constructorContext.getClassName().getClassName(),
								compiler.createField(DataSource.class, "delegate"),
								compiler.createField(ConnectionDecorator[].class, "decorators"));
					}, (methodContext) -> {
						Method method = methodContext.getMethod();

						// Obtain the real DataSource
						if (DataSourceWrapper.isGetRealDataSourceMethod(method)) {
							// Obtain the real DataSource
							methodContext.write("    return this.delegate;");
						}

						// Obtain the connection
						if ("getConnection".equals(method.getName())) {

							// Obtain the connection
							methodContext.write("    ");
							methodContext.write(compiler.getSourceName(Connection.class));
							methodContext.write(" connection = ");
							compiler.writeDelegateMethodCall(methodContext.getSource(), "this.delegate", method);
							methodContext.writeln(";");

							// Decorate the connection
							methodContext.writeln("    for (int i = 0; i < this.decorators.length; i++) {");
							methodContext.writeln("      connection = this.decorators[i].decorate(connection);");
							methodContext.writeln("    }");

							// Return the connection
							methodContext.writeln("    return connection;");
						}
					});

			// Compile and return the class
			Class<?> wrapperClass = javaSource.compile();
			return (DataSource) wrapperClass.getConstructor(DataSource.class, ConnectionDecorator[].class)
					.newInstance(dataSource, decorators);
		}
	}

	/**
	 * Allows overriding to configure a different {@link DataSourceFactory}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link DataSourceFactory}.
	 * @throws Exception If fails to obtain {@link DataSourceFactory}.
	 */
	protected DataSourceFactory getDataSourceFactory(SourceContext context) throws Exception {

		// Obtain the data source factory
		String dataSourceFactoryClassName = context.getProperty(PROPERTY_DATA_SOURCE_FACTORY,
				DefaultDataSourceFactory.class.getName());
		Class<?> dataSourceFactoryClass = context.loadClass(dataSourceFactoryClassName);
		if (!DataSourceFactory.class.isAssignableFrom(dataSourceFactoryClass)) {
			throw new Exception(dataSourceFactoryClassName + " must implement " + DataSourceFactory.class.getName());
		}
		DataSourceFactory dataSourceFactory = (DataSourceFactory) dataSourceFactoryClass.getDeclaredConstructor()
				.newInstance();

		// Return the data source factory
		return dataSourceFactory;
	}

	/**
	 * Creates the {@link ConnectionPoolDataSource}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return Created {@link ConnectionPoolDataSource}.
	 * @throws Exception If fails to create the {@link ConnectionPoolDataSource}.
	 */
	protected final ConnectionPoolDataSource newConnectionPoolDataSource(SourceContext context) throws Exception {

		// Obtain the data source
		ConnectionPoolDataSourceFactory dataSourceFactory = this.getConnectionPoolDataSourceFactory(context);
		ConnectionPoolDataSource dataSource = dataSourceFactory.createConnectionPoolDataSource(context);

		// Obtain the decorator factories
		List<PooledConnectionDecorator> decoratorList = new ArrayList<>();
		for (PooledConnectionDecorator decorator : context
				.loadOptionalServices(PooledConnectionDecoratorServiceFactory.class)) {
			decoratorList.add(decorator);
		}
		PooledConnectionDecorator[] decorators = decoratorList
				.toArray(new PooledConnectionDecorator[decoratorList.size()]);

		// Determine if require decorating connections
		if (decorators.length == 0) {
			return dataSource;
		}

		// Provide compiled ConnectionPooledDataSource
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(context);
		if (compiler == null) {

			// Fall back to proxy implementation
			return (ConnectionPoolDataSource) Proxy.newProxyInstance(context.getClassLoader(),
					new Class[] { ConnectionPoolDataSource.class }, (object, method, args) -> {
						Method dataSourceMethod = dataSource.getClass().getMethod(method.getName(),
								method.getParameterTypes());
						Object result = dataSourceMethod.invoke(dataSource, args);
						if ("getPooledConnection".equals(method.getName())) {

							// Decorate the connection
							PooledConnection connection = (PooledConnection) result;
							for (int i = 0; i < decorators.length; i++) {
								connection = decorators[i].decorate(connection);
							}

							// Return the decorated connection
							result = connection;
						}
						return result;
					});

		} else {
			// Provide compiled wrapper implementation
			JavaSource javaSource = compiler.addWrapper(new Class[] { ConnectionPoolDataSource.class },
					ConnectionPoolDataSource.class, "this.delegate", (constructorContext) -> {
						compiler.writeConstructor(constructorContext.getSource(),
								constructorContext.getClassName().getClassName(),
								compiler.createField(ConnectionPoolDataSource.class, "delegate"),
								compiler.createField(PooledConnectionDecorator[].class, "decorators"));
					}, (methodContext) -> {
						Method method = methodContext.getMethod();
						if ("getPooledConnection".equals(method.getName())) {

							// Obtain the connection
							methodContext.write("    ");
							methodContext.write(compiler.getSourceName(PooledConnection.class));
							methodContext.write(" connection = ");
							compiler.writeDelegateMethodCall(methodContext.getSource(), "this.delegate", method);
							methodContext.writeln(";");

							// Decorate the connection
							methodContext.writeln("    for (int i = 0; i < this.decorators.length; i++) {");
							methodContext.writeln("      connection = this.decorators[i].decorate(connection);");
							methodContext.writeln("    }");

							// Return the connection
							methodContext.writeln("    return connection;");
						}
					});

			// Compile and return the class
			Class<?> wrapperClass = javaSource.compile();
			return (ConnectionPoolDataSource) wrapperClass
					.getConstructor(ConnectionPoolDataSource.class, PooledConnectionDecorator[].class)
					.newInstance(dataSource, decorators);
		}
	}

	/**
	 * Allows overriding to configure a different
	 * {@link ConnectionPoolDataSourceFactory}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link ConnectionPoolDataSourceFactory}.
	 * @throws Exception If fails to obtain {@link ConnectionPoolDataSourceFactory}.
	 */
	protected ConnectionPoolDataSourceFactory getConnectionPoolDataSourceFactory(SourceContext context)
			throws Exception {

		// Obtain the data source factory
		String dataSourceFactoryClassName = context.getProperty(PROPERTY_DATA_SOURCE_FACTORY,
				DefaultDataSourceFactory.class.getName());
		Class<?> dataSourceFactoryClass = context.loadClass(dataSourceFactoryClassName);
		if (!ConnectionPoolDataSourceFactory.class.isAssignableFrom(dataSourceFactoryClass)) {
			throw new Exception(
					dataSourceFactoryClassName + " must implement " + ConnectionPoolDataSourceFactory.class.getName());
		}
		ConnectionPoolDataSourceFactory dataSourceFactory = (ConnectionPoolDataSourceFactory) dataSourceFactoryClass
				.getDeclaredConstructor().newInstance();

		// Return the data source factory
		return dataSourceFactory;
	}

	/**
	 * Sets up the meta-data.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @throws Exception If fails to loader further meta-data.
	 */
	protected abstract void setupMetaData(MetaDataContext<None, None> context) throws Exception;

	/**
	 * Sets up the {@link ManagedObjectSource} for active use.
	 * 
	 * @param mosContext {@link ManagedObjectSourceContext}.
	 * @throws Exception If fails to setup.
	 */
	protected abstract void setupActive(ManagedObjectSourceContext<None> mosContext) throws Exception;

	/**
	 * {@link ConnectivityFactory}.
	 */
	private ConnectivityFactory connectivityFactory;

	/**
	 * Connectivity.
	 */
	public static interface Connectivity extends AutoCloseable {

		/**
		 * Obtains the {@link Connection} to test connectivity.
		 * 
		 * @return {@link Connection} to test connectivity.
		 * @throws SQLException If fails to obtain {@link Connection}.
		 */
		Connection getConnection() throws SQLException;
	}

	/**
	 * Factory for {@link Connectivity}.
	 */
	@FunctionalInterface
	public static interface ConnectivityFactory {

		/**
		 * Obtains the {@link Connectivity}.
		 *
		 * @return {@link Connectivity}.
		 * @throws SQLException If fails to obtain {@link Connectivity}.
		 */
		Connectivity createConnectivity() throws SQLException;
	}

	/**
	 * Convenient {@link Connectivity} implementation to wrap a {@link Connection}.
	 */
	public static class ConnectionConnectivity implements Connectivity {

		/**
		 * {@link Connection}.
		 */
		private final Connection connection;

		/**
		 * Instantiate.
		 * 
		 * @param connection {@link Connection}.
		 */
		public ConnectionConnectivity(Connection connection) {
			this.connection = connection;
		}

		/*
		 * ================= Connectivity ==========================
		 */

		@Override
		public Connection getConnection() {
			return this.connection;
		}

		@Override
		public void close() throws Exception {
			this.connection.close();
		}
	}

	/**
	 * Loads validation of connectivity on start up.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @throws Exception If fails to load validation.
	 */
	protected void loadValidateConnectivity(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Provide start up function to ensure can connect
		String validateSql = mosContext.getProperty(PROPERTY_DATA_SOURCE_VALIDATE_SQL, null);
		final String validateFunctionName = "confirm";
		mosContext.addManagedFunction(validateFunctionName,
				() -> (functionContext) -> this.validateConnectivity(validateSql));
		mosContext.addStartupFunction(validateFunctionName, null);
	}

	/**
	 * Specifies {@link ConnectivityFactory} for validating connectivity on startup.
	 * 
	 * @param connectivityFactory {@link ConnectivityFactory}.
	 */
	public void setConnectivity(ConnectivityFactory connectivityFactory) {
		this.connectivityFactory = connectivityFactory;
	}

	/**
	 * Validates connectivity.
	 * 
	 * @param sql Optional SQL to be executed against the {@link Connection}. May be
	 *            <code>null</code>.
	 * @throws Exception If fails connectivity.
	 */
	protected void validateConnectivity(String sql) throws Exception {

		// Ensure have connectivity
		if (this.connectivityFactory == null) {
			throw new SQLException("Must specify " + ConnectivityFactory.class.getName());
		}

		// Undertake connectivity
		try (Connectivity connectivity = this.connectivityFactory.createConnectivity()) {
			Connection connection = connectivity.getConnection();
			if (sql != null) {
				connection.createStatement().execute(sql);
			}
		}
	}

	/**
	 * Closes the {@link DataSource}.
	 * 
	 * @param dataSource {@link DataSource} to be closed.
	 * @param logger     {@link Logger}.
	 */
	protected void closeDataSource(DataSource dataSource, Logger logger) {
		try {

			// Obtain the real DataSource
			DataSource realDataSource = DataSourceWrapper.getRealDataSource(dataSource);

			// Close DataSource if closeable
			if (realDataSource instanceof AutoCloseable) {
				((AutoCloseable) realDataSource).close();
			}

		} catch (Exception ex) {
			logger.log(Level.WARNING, "Failed to close " + DataSource.class.getSimpleName(), ex);
		}
	}

	/*
	 * ================== AbstractManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		this.setupMetaData(context);

		// Only load data source (if not loading type)
		if (mosContext.isLoadingType()) {
			return;
		}

		// Setup for active use
		this.setupActive(mosContext);

		// Ensure connected on startup
		this.loadValidateConnectivity(context);
	}

}
