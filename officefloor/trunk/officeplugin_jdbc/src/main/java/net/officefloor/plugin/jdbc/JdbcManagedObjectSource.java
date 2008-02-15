/*
 * Created on Jan 25, 2006
 */
package net.officefloor.plugin.jdbc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
 * for JDBC.
 * 
 * @author Daniel
 */
public class JdbcManagedObjectSource extends AbstractManagedObjectSource {

	/**
	 * Property name to obtain the class of the {@link DataSourceFactory}.
	 */
	public static final String DATA_SOURCE_FACTORY_CLASS_PROPERTY = "net.officefloor.plugin.jdbc.datasourcefactory";

	/**
	 * Property name to specify the initialise script for the {@link DataSource}.
	 */
	public static final String DATA_SOURCE_INITIALISE_SCRIPT = "net.officefloor.plugin.jdbc.datasource.initialise.script";

	/**
	 * {@link ConnectionPoolDataSource}.
	 */
	private ConnectionPoolDataSource poolDataSource;

	/**
	 * Default constructor as required.
	 */
	public JdbcManagedObjectSource() {
	}

	/**
	 * Obtains the {@link DataSourceFactory}from the input properties.
	 * 
	 * @param properties
	 *            Properties to create and configure the
	 *            {@link DataSourceFactory}.
	 * @return Configured {@link DataSourceFactory}.
	 * @throws Exception
	 *             Should there be a failure creating or configuring the
	 *             {@link DataSourceFactory}.
	 */
	protected DataSourceFactory getDataSourceFactory(Properties properties)
			throws Exception {

		// Obtain the name of the data source factory
		String className = properties
				.getProperty(DATA_SOURCE_FACTORY_CLASS_PROPERTY);

		// Create an instance of the data source factory
		DataSourceFactory dataSourceFactory = (DataSourceFactory) Class
				.forName(className).newInstance();

		// Return the configured data source factory
		return dataSourceFactory;
	}

	/*
	 * ====================================================================
	 * AbstractManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Ensure data source factory is provided
		context.addProperty(DATA_SOURCE_FACTORY_CLASS_PROPERTY,
				"DataSourceFactory");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadMetaData(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(MetaDataContext context) throws Exception {

		// Obtain the properties
		Properties properties = context.getManagedObjectSourceContext()
				.getProperties();

		// Obtain the Data Source Factory
		DataSourceFactory sourceFactory = this.getDataSourceFactory(properties);

		// Create the data source
		this.poolDataSource = sourceFactory
				.createConnectionPoolDataSource(properties);

		// Create the recycle task
		new RecycleJdbcTask().registerAsRecycleTask(context
				.getManagedObjectSourceContext(), "jdbc.recycle");

		// Specify the meta-data
		context.setObjectClass(Connection.class);
		context.setManagedObjectClass(JdbcManagedObject.class);

		// Initialise data source if required
		this.initialiseDataSource(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource#getManagedObject()
	 */
	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Obtain the pooled connection
		PooledConnection pooledConnection = this.poolDataSource
				.getPooledConnection();

		// Return the JDBC managed object
		return new JdbcManagedObject(pooledConnection);
	}

	/**
	 * Initialises the {@link DataSource}.
	 * 
	 * @param context
	 *            {@link MetaDataContext}.
	 * @throws Exception
	 *             If fails to initialise {@link DataSource}.
	 */
	protected void initialiseDataSource(MetaDataContext context)
			throws Exception {

		// Determine if required to initialise the data source
		String initialiseScript = context.getManagedObjectSourceContext()
				.getProperty(DATA_SOURCE_INITIALISE_SCRIPT, null);
		if (initialiseScript != null) {

			// Obtain access to the initialise script contents
			InputStream initialiseScriptInputStream = context
					.getManagedObjectSourceContext().getResourceLocator()
					.locateInputStream(initialiseScript);
			if (initialiseScriptInputStream == null) {
				throw new Exception("Can not find initialise script '"
						+ initialiseScript + "'");
			}

			// Read the statements for the initialise script
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					initialiseScriptInputStream));
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
			PooledConnection pooledConnection = this.poolDataSource
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

}