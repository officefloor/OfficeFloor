/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on Jan 25, 2006
 */
package net.officefloor.plugin.jdbc.connection;

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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.jdbc.util.ReflectionUtil;

/**
 * {@link ManagedObjectSource} for JDBC.
 * 
 * @author Daniel Sagenschneider
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
			ManagedObjectSourceContext<?> context) throws Exception {

		// Determine if a factory is configured
		String factoryClassName = context.getProperty(
				CONNECTION_POOL_DATA_SOURCE_FACTORY_PROPERTY, null);
		if (factoryClassName != null) {
			// Use the factory to obtain the connection pool data source
			Class<?> clazz = context.loadClass(factoryClassName);
			Object object = clazz.newInstance();
			ConnectionPoolDataSourceFactory factory = (ConnectionPoolDataSourceFactory) object;
			return factory.createConnectionPoolDataSource(context);
		}

		// No factory so use connection pool data source directly
		String className = context
				.getProperty(CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY);

		// Obtain the properties for the connection pool
		Properties properties = context.getProperties();

		// Create and return the connection pool data source
		return ReflectionUtil.createInitialisedBean(className,
				context.getClassLoader(), ConnectionPoolDataSource.class,
				properties);
	}

	/*
	 * ================== AbstractManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Ensure data source provided
		context.addProperty(CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY,
				ConnectionPoolDataSource.class.getSimpleName());
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the connection pool data source
		this.dataSource = this.getConnectionPoolDataSource(context
				.getManagedObjectSourceContext());

		// Determine if required to initialise the database
		String initialiseScript = mosContext.getProperty(
				DATA_SOURCE_INITIALISE_SCRIPT, null);
		if (initialiseScript != null) {
			// Obtain access to the initialise script contents
			this.initialiseScriptInputStream = mosContext
					.getResource(initialiseScript);
			if (this.initialiseScriptInputStream == null) {
				throw new Exception("Can not find initialise script '"
						+ initialiseScript + "'");
			}
		}

		// Create the recycle task
		new RecycleJdbcTask().registerAsRecycleTask(
				context.getManagedObjectSourceContext(), "jdbc.recycle");

		// Specify the meta-data
		context.setObjectClass(Connection.class);
		context.setManagedObjectClass(JdbcManagedObject.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context)
			throws Exception {

		// Obtain a connection to the database (to ensure working)
		PooledConnection connection = this.dataSource.getPooledConnection();
		connection.close();

		// Initialise data source
		this.initialiseDataSource();

		// Allow clean up of configuration
		this.initialiseScriptInputStream = null;
	}

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