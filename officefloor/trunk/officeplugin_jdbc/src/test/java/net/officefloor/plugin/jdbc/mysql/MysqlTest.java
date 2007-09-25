/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.jdbc.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.jdbc.DataSourceFactory;

/**
 * Tests connecting to MySql.
 * 
 * @author Daniel
 */
public class MysqlTest extends OfficeFrameTestCase {

	/**
	 * Loads the connection properties.
	 * 
	 * @param properties
	 *            Properties to be loaded with the connection details.
	 * @return Input Properties with connection details.
	 * @throws Exception
	 *             If fails to fine password.
	 */
	protected Properties loadConnectionProperties(Properties properties)
			throws Exception {
		// Specify connection details
		properties.setProperty("server", "localhost");
		properties.setProperty("port", "3306");
		properties.setProperty("database", "officefloor");
		properties.setProperty("user", "officefloor");
		properties.setProperty("password", "password");

		// Return the properties
		return properties;
	}

	/**
	 * Ensure able to connect to Mysql database.
	 */
	public void testMySqlDataSourceFactoryConnect() throws Exception {

		// Create the mysql data source
		Properties properties = this.loadConnectionProperties(new Properties());
		DataSourceFactory factory = new MySqlDataSourceFactory();
		factory.init(properties);

		// Create the data source
		ConnectionPoolDataSource dataSource = factory
				.createConnectionPoolDataSource();

		// Obtain a connection
		PooledConnection pooledConnection = dataSource.getPooledConnection();
		Connection connection = pooledConnection.getConnection();

		// Create a statement and run
		Statement statement = connection.createStatement();
		statement.execute("SELECT * FROM INVOICE");

		// Close the connection
		connection.close();
		pooledConnection.close();
	}

	/**
	 * Ensure multi-threaded {@link Connection}.
	 */
	public void testMySqlMultiThreadedConnection() throws Exception {

		// Create the mysql data source
		Properties properties = this.loadConnectionProperties(new Properties());
		DataSourceFactory factory = new MySqlDataSourceFactory();
		factory.init(properties);

		// Create the data source
		ConnectionPoolDataSource dataSource = factory
				.createConnectionPoolDataSource();

		// Obtain a connection
		PooledConnection pooledConnection = dataSource.getPooledConnection();
		Connection connection = pooledConnection.getConnection();

		// Start transaction
		connection.setAutoCommit(false);

		// Number of requests
		final int REQUEST_COUNTS = 10;

		// Create X number of requests
		ThreadedRequest[] requests = new ThreadedRequest[REQUEST_COUNTS];
		for (int i = 0; i < REQUEST_COUNTS; i++) {

			// Determine if insert/select
			if ((i % 2) == 1) {
				requests[i] = new ThreadedRequest(connection,
						"INSERT INTO CUSTOMER (CUSTOMER_NAME) VALUES ('Customer "
								+ i + "')");
			} else {
				requests[i] = new ThreadedRequest(connection,
						"SELECT * FROM CUSTOMER");
			}

			// Start the request
			new Thread(requests[i]).start();
		}

		// Wait for the threads to complete
		boolean isComplete = false;
		while (!isComplete) {

			// Allow other threads to run
			synchronized (connection) {
				connection.wait(100);
			}

			// Flag to complete
			isComplete = true;

			// Determine if complete
			for (int i = 0; i < requests.length; i++) {
				isComplete &= requests[i].isComplete;
			}
		}

		// Rollback changes
		connection.rollback();

		// Close the connection
		connection.close();
		pooledConnection.close();
	}
}

/**
 * Request abled to be run within a {@link java.lang.Thread}.
 */
class ThreadedRequest implements Runnable {

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * SQL to execute.
	 */
	private final String sql;

	/**
	 * Flag indicating when complete.
	 */
	protected volatile boolean isComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 */
	public ThreadedRequest(Connection connection, String sql) {
		this.connection = connection;
		this.sql = sql.trim();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {

			// Create the statement
			Statement statement = connection.createStatement();

			// Record details
			StringBuilder builder = new StringBuilder();

			// Determine if to execute
			if (statement.execute(this.sql)) {

				// Obtain the results
				ResultSet resultSet = statement.getResultSet();

				// Record the results
				builder.append("SELECT:\n");
				while (resultSet.next()) {
					builder.append("\tCUSTOMER_ID: "
							+ resultSet.getInt("CUSTOMER_ID"));
					builder.append(" CUSTOMER_NAME: "
							+ resultSet.getString("CUSTOMER_NAME"));
					builder.append("\n");
				}

			} else {

				// Record number of records affected
				builder.append("UPDATED/INSERTED: "
						+ statement.getUpdateCount());
				builder.append("\n");
			}

			// Close the statement
			statement.close();

			// Output results
			synchronized (ThreadedRequest.class) {
				System.out.print(builder.toString());
			}

		} catch (SQLException ex) {
			// Report on failure
			synchronized (ThreadedRequest.class) {
				System.err.println("Failed SQL: " + this.sql);
				ex.printStackTrace(System.err);
			}
		} finally {
			// Flag complete
			this.isComplete = true;

			// Notify complete
			synchronized (this.connection) {
				this.connection.notify();
			}
		}
	}

}