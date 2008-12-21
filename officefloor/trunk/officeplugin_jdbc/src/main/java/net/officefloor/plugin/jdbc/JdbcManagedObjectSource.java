/*
 * Created on Jan 25, 2006
 */
package net.officefloor.plugin.jdbc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for JDBC.
 * 
 * @author Daniel
 */
public class JdbcManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/**
	 * Property name of the {@link ConnectionPoolDataSourceFactory} class.
	 */
	public static final String CONNECTION_POOL_DATA_SOURCE_FACTORY_PROPERTY = "connectionpool.datasource.factory";

	/**
	 * Property name of the {@link ConnectionPoolDataSource} class.
	 */
	public static final String CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY = "connectionpool.datasource.class";

	/**
	 * Property name of script containing SQL to be run to initialise the
	 * {@link DataSource}.
	 */
	public static final String DATA_SOURCE_INITIALISE_SCRIPT = "initialise.script";

	/**
	 * {@link ConnectionPoolDataSource}.
	 */
	protected ConnectionPoolDataSource dataSource;

	/**
	 * {@link InputStream} to obtain the initialise script details.
	 */
	private InputStream initialiseScriptInputStream = null;

	/**
	 * Default constructor as required.
	 */
	public JdbcManagedObjectSource() {
	}

	/**
	 * Obtains the {@link DataSourceFactory}from the input properties.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 * @return Configured {@link ConnectionPoolDataSource}.
	 * @throws Exception
	 *             Should there be a failure creating or configuring the
	 *             {@link ConnectionPoolDataSource}.
	 */
	protected ConnectionPoolDataSource getConnectionPoolDataSource(
			ManagedObjectSourceContext context) throws Exception {

		// Determine if a factory is configured
		String factoryClassName = context.getProperty(
				CONNECTION_POOL_DATA_SOURCE_FACTORY_PROPERTY, null);
		if (factoryClassName != null) {
			// Use the factory to obtain the connection pool data source
			Class<?> clazz = this.getClass().getClassLoader().loadClass(
					factoryClassName);
			Object object = clazz.newInstance();
			ConnectionPoolDataSourceFactory factory = (ConnectionPoolDataSourceFactory) object;
			return factory.createConnectionPoolDataSource(context);
		}

		// No factory so use connection pool data source directly
		String className = context
				.getProperty(CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY);

		// Obtain the connection pool data source class
		Class<?> clazz = this.getClass().getClassLoader().loadClass(className);

		// Create an instance of the data source
		Object object = clazz.newInstance();
		ConnectionPoolDataSource dataSource = (ConnectionPoolDataSource) object;

		// Load the properties for the data source
		for (Method method : clazz.getMethods()) {

			// Ensure the method is a public setter with only one argument
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if (!method.getName().startsWith("set")) {
				continue;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				continue;
			}

			// Obtain the property name for the method
			String propertyName = method.getName().substring("set".length());
			propertyName = propertyName.substring(0, 1).toLowerCase()
					+ propertyName.substring(1);

			// Obtain the value for the property
			Object propertyValue;
			Class<?> parameterType = parameterTypes[0];
			if (String.class.isAssignableFrom(parameterType)) {
				String value = context.getProperty(propertyName);
				if (value.length() == 0) {
					continue; // do not set blank strings
				}
				propertyValue = value;
			} else if (Integer.class.isAssignableFrom(parameterType)
					|| int.class.isAssignableFrom(parameterType)) {
				propertyValue = Integer.valueOf(context
						.getProperty(propertyName));
			} else if (Boolean.class.isAssignableFrom(parameterType)
					|| boolean.class.isAssignableFrom(parameterType)) {
				propertyValue = Boolean.valueOf(context
						.getProperty(propertyName));
			} else {
				// Unknown property type, so do not provide
				continue;
			}

			// Load the property to the data source
			try {
				method.invoke(dataSource, propertyValue);
			} catch (InvocationTargetException ex) {
				// Throw cause (and attempt best cause)
				Throwable cause = ex.getCause();
				throw (cause instanceof Exception ? (Exception) cause : ex);
			}
		}

		// Return the configured data source
		return dataSource;
	}

	/*
	 * ================== AbstractManagedObjectSource ====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractAsyncManagedObjectSource
	 * #loadSpecification(net.officefloor.frame.spi
	 * .managedobject.source.impl.AbstractAsyncManagedObjectSource
	 * .SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Ensure data source provided
		context.addProperty(CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY,
				ConnectionPoolDataSource.class.getSimpleName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractAsyncManagedObjectSource
	 * #loadMetaData(net.officefloor.frame.spi.managedobject
	 * .source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the connection pool data source
		this.dataSource = this.getConnectionPoolDataSource(context
				.getManagedObjectSourceContext());

		// Determine if required to initialise the database
		String initialiseScript = mosContext.getProperty(
				DATA_SOURCE_INITIALISE_SCRIPT, null);
		if (initialiseScript != null) {
			// Obtain access to the initialise script contents
			this.initialiseScriptInputStream = mosContext.getResourceLocator()
					.locateInputStream(initialiseScript);
			if (this.initialiseScriptInputStream == null) {
				throw new Exception("Can not find initialise script '"
						+ initialiseScript + "'");
			}
		}

		// Create the recycle task
		new RecycleJdbcTask().registerAsRecycleTask(context
				.getManagedObjectSourceContext(), "jdbc.recycle");

		// Specify the meta-data
		context.setObjectClass(Connection.class);
		context.setManagedObjectClass(JdbcManagedObject.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractAsyncManagedObjectSource
	 * #start(net.officefloor.frame.spi.managedobject
	 * .source.impl.AbstractAsyncManagedObjectSource.StartContext)
	 */
	@Override
	protected void start(StartContext<None> startContext) throws Exception {

		// Obtain a connection to the database (to ensure working)
		PooledConnection connection = this.dataSource.getPooledConnection();
		connection.close();

		// Initialise data source
		this.initialiseDataSource();

		// Allow clean up of configuration
		this.initialiseScriptInputStream = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.impl.
	 * AbstractManagedObjectSource#getManagedObject()
	 */
	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Obtain the pooled connection
		PooledConnection pooledConnection = this.dataSource
				.getPooledConnection();

		// Return the JDBC managed object
		return new JdbcManagedObject(pooledConnection);
	}

	/**
	 * Initialises the {@link DataSource}.
	 * 
	 * @throws Exception
	 *             If fails to initialise {@link DataSource}.
	 */
	protected void initialiseDataSource() throws Exception {

		// Only initialise if have script
		if (this.initialiseScriptInputStream == null) {
			return;
		}

		// Read the statements for the initialise script
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				this.initialiseScriptInputStream));
		List<String> statements = new LinkedList<String>();
		StringBuilder currentStatement = new StringBuilder();
		String line;
		do {
			// Obtain the line
			line = reader.readLine();

			// Add line to current statement
			if (line != null) {
				currentStatement.append(line);
				currentStatement.append("\n");
			}

			// Determine if statement complete
			if ((line == null) || (line.trim().endsWith(";"))) {
				// Statement complete
				String statementText = currentStatement.toString();
				if (statementText.trim().length() > 0) {
					// Add the statement
					statements.add(statementText);
				}

				// Reset the statement for next
				currentStatement = new StringBuilder();
			}

		} while (line != null);
		reader.close();

		// Run the statements to initialise data source
		PooledConnection pooledConnection = this.dataSource
				.getPooledConnection();
		Connection connection = pooledConnection.getConnection();
		for (String sql : statements) {
			Statement statement = connection.createStatement();
			statement.execute(sql);
			statement.close();
		}
		pooledConnection.close();
	}

}