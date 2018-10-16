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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler;
import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler.JavaSource;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.jdbc.datasource.ConnectionPoolDataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.decorate.ConnectionDecorator;
import net.officefloor.jdbc.decorate.ConnectionDecoratorFactory;
import net.officefloor.jdbc.decorate.PooledConnectionDecorator;
import net.officefloor.jdbc.decorate.PooledConnectionDecoratorFactory;

/**
 * Abstract {@link ManagedObjectSource} for {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConnectionManagedObjectSource extends AbstractManagedObjectSource<None, None> {

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
	protected final DataSource createDataSource(SourceContext context) throws Exception {

		// Obtain the data source
		DataSourceFactory dataSourceFactory = this.getDataSourceFactory(context);
		DataSource dataSource = dataSourceFactory.createDataSource(context);

		// Obtain the decorator factories
		List<ConnectionDecorator> decoratorList = new ArrayList<>();
		ServiceLoader<ConnectionDecoratorFactory> decoratorFactories = ServiceLoader
				.load(ConnectionDecoratorFactory.class, context.getClassLoader());
		for (ConnectionDecoratorFactory decoratorFactory : decoratorFactories) {
			decoratorList.add(decoratorFactory.createConnectionDecorator(context));
		}
		ConnectionDecorator[] decorators = decoratorList.toArray(new ConnectionDecorator[decoratorList.size()]);

		// Determine if require decorating connections
		if (decorators.length == 0) {
			return dataSource;
		}

		// Provide compiled DataSource
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(context.getClassLoader());
		if (compiler == null) {

			// Fall back to proxy implementation
			return (DataSource) Proxy.newProxyInstance(context.getClassLoader(), new Class[] { DataSource.class },
					(object, method, args) -> {
						Method dataSourceMethod = dataSource.getClass().getMethod(method.getName(),
								method.getParameterTypes());
						Object result = dataSourceMethod.invoke(dataSource, args);
						if ("getConnection".equals(method.getName())) {

							// Decorate the connection
							Connection connection = (Connection) result;
							for (int i = 0; i < decorators.length; i++) {
								decorators[i].decorate(connection);
							}
						}
						return result;
					});

		} else {
			// Provide compiled wrapper implementation
			JavaSource javaSource = compiler.addWrapper(new Class[] { DataSource.class }, DataSource.class,
					"this.delegate", (constructorContext) -> {
						compiler.writeConstructor(constructorContext.getSource(),
								constructorContext.getClassName().getClassName(),
								compiler.createField(DataSource.class, "delegate"),
								compiler.createField(ConnectionDecorator[].class, "decorators"));
					}, (methodContext) -> {
						Method method = methodContext.getMethod();
						if ("getConnection".equals(method.getName())) {

							// Obtain the connection
							methodContext.write("    ");
							methodContext.write(compiler.getSourceName(Connection.class));
							methodContext.write(" connection = ");
							compiler.writeDelegateMethodCall(methodContext.getSource(), "this.delegate", method);
							methodContext.writeln(";");

							// Decorate the connection
							methodContext.writeln("    for (int i = 0; i < this.decorators.length; i++) {");
							methodContext.writeln("      this.decorators[i].decorate(connection);");
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
	protected final ConnectionPoolDataSource createConnectionPoolDataSource(SourceContext context) throws Exception {

		// Obtain the data source
		ConnectionPoolDataSourceFactory dataSourceFactory = this.getConnectionPoolDataSourceFactory(context);
		ConnectionPoolDataSource dataSource = dataSourceFactory.createConnectionPoolDataSource(context);

		// Obtain the decorator factories
		List<PooledConnectionDecorator> decoratorList = new ArrayList<>();
		ServiceLoader<PooledConnectionDecoratorFactory> decoratorFactories = ServiceLoader
				.load(PooledConnectionDecoratorFactory.class, context.getClassLoader());
		for (PooledConnectionDecoratorFactory decoratorFactory : decoratorFactories) {
			decoratorList.add(decoratorFactory.createPooledConnectionDecorator(context));
		}
		PooledConnectionDecorator[] decorators = decoratorList
				.toArray(new PooledConnectionDecorator[decoratorList.size()]);

		// Determine if require decorating connections
		if (decorators.length == 0) {
			return dataSource;
		}

		// Provide compiled ConnectionPooledDataSource
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(context.getClassLoader());
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
								decorators[i].decorate(connection);
							}
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
								compiler.createField(ConnectionDecorator[].class, "decorators"));
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
							methodContext.writeln("      this.decorators[i].decorate(connection);");
							methodContext.writeln("    }");

							// Return the connection
							methodContext.writeln("    return connection;");
						}
					});

			// Compile and return the class
			Class<?> wrapperClass = javaSource.compile();
			return (ConnectionPoolDataSource) wrapperClass
					.getConstructor(ConnectionPoolDataSource.class, ConnectionDecorator[].class)
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
	 * Enables overriding to load further meta-data.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @throws Exception If fails to loader further meta-data.
	 */
	protected abstract void loadFurtherMetaData(MetaDataContext<None, None> context) throws Exception;

	/**
	 * {@link ConnectivityFactory}.
	 */
	private ConnectivityFactory connectivityFactory;

	/**
	 * Factory for {@link Connection} in confirming connectivity.
	 */
	@FunctionalInterface
	public static interface ConnectivityFactory {

		/**
		 * Obtains the {@link Connection} for connectivity.
		 *
		 * @return {@link Connection}.
		 * @throws SQLException If fails to obtain connectivity.
		 */
		Connection createConnectivity() throws SQLException;
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
		mosContext.addManagedFunction(validateFunctionName, () -> (functionContext) -> {
			this.validateConnectivity(validateSql);
			return null;
		});
		mosContext.addStartupFunction(validateFunctionName);
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
		try (Connection connection = this.connectivityFactory.createConnectivity()) {
			if (sql != null) {
				connection.createStatement().execute(sql);
			}
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

		// Configure meta-data
		context.setObjectClass(Connection.class);
		context.setManagedObjectClass(AbstractConnectionManagedObject.class);

		// Only load data source (if not loading type)
		if (mosContext.isLoadingType()) {
			return;
		}

		// Create further meta-data
		this.loadFurtherMetaData(context);

		// Ensure connected on startup
		this.loadValidateConnectivity(context);
	}

}