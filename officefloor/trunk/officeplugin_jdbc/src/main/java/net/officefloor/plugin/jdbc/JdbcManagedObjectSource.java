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
	 * Property prefix for properties of this {@link JdbcManagedObjectSource}.
	 */
	public static final String JDBC_MANAGED_OBJECT_SOURCE_PREFIX = "net.officefloor.plugin.jdbc";

	/**
	 * Property name to obtain the class of the {@link DataSourceFactory}.
	 */
	public static final String DATA_SOURCE_FACTORY_CLASS_PROPERTY = JDBC_MANAGED_OBJECT_SOURCE_PREFIX
			+ ".datasourcefactory";

	/**
	 * Property name to specify the initialise script for the {@link DataSource}.
	 */
	public static final String DATA_SOURCE_INITIALISE_SCRIPT = JDBC_MANAGED_OBJECT_SOURCE_PREFIX
			+ ".datasource.initialise.script";

	/**
	 * {@link ConnectionPoolDataSource}.
	 */
	private ConnectionPoolDataSource poolDataSource;

	/**
	 * {@link Properties}.
	 */
	private Properties properties = null;

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
		this.properties = context.getManagedObjectSourceContext()
				.getProperties();

		// Determine if required to initialise the database
		String initialiseScript = context.getManagedObjectSourceContext()
				.getProperty(DATA_SOURCE_INITIALISE_SCRIPT, null);
		if (initialiseScript != null) {
			// Obtain access to the initialise script contents
			this.initialiseScriptInputStream = context
					.getManagedObjectSourceContext().getResourceLocator()
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
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.StartContext)
	 */
	@Override
	protected void start(StartContext startContext) throws Exception {

		// Obtain the Data Source Factory
		DataSourceFactory sourceFactory = this
				.getDataSourceFactory(this.properties);

		// Create the data source
		this.poolDataSource = sourceFactory
				.createConnectionPoolDataSource(this.properties);

		// Initialise data source
		this.initialiseDataSource();

		// Allow clean up of configuration
		this.properties = null;
		this.initialiseScriptInputStream = null;
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