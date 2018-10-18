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
package net.officefloor.jdbc.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * <p>
 * {@link TestRule} for making {@link DataSource} available.
 * <p>
 * This is useful for in memory database implementations to keep a
 * {@link Connection} active so the in memory database stays active for the
 * entirety of the test.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceRule implements TestRule {

	/**
	 * Factory to create the {@link Connection}.
	 */
	public static interface ConnectionFactory {

		/**
		 * Create the {@link Connection}.
		 * 
		 * @param context {@link ConnectionFactoryContext}.
		 * @throws Exception If fails to create {@link Connection}.
		 */
		void createConnection(ConnectionFactoryContext context) throws Exception;
	}

	/**
	 * Context for the {@link ConnectionFactory}.
	 */
	public static interface ConnectionFactoryContext {

		/**
		 * <p>
		 * Specifies the {@link Connection}.
		 * <p>
		 * Allows for the {@link Connection} to cleaned up if further setup is required
		 * on the {@link Connection} to ensure it is available.
		 * 
		 * @param connection Created {@link Connection}.
		 * @return {@link Connection}.
		 */
		Connection setConnection(Connection connection);
	}

	/**
	 * Waits for the database to be available.
	 * 
	 * @param connectionFactory {@link ConnectionFactory}.
	 * @return {@link Connection}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static Connection waitForDatabaseAvailable(ConnectionFactory connectionFactory) throws Exception {
		return waitForDatabaseAvailable(null, connectionFactory);
	}

	/**
	 * Waits for the database to be available.
	 * 
	 * @param lock              To wait on to allow locking setup.
	 * @param connectionFactory {@link ConnectionFactory}.
	 * @return {@link Connection}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static Connection waitForDatabaseAvailable(Object lock, ConnectionFactory connectionFactory)
			throws Exception {

		// Ignore output
		OutputStream devNull = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// discard
			}
		};

		// Ignore errors in trying to start
		PrintStream stdout = System.out;
		PrintStream stderr = System.err;
		System.setOut(new PrintStream(devNull));
		System.setErr(new PrintStream(devNull));
		try {

			// Try until time out (as may take time for database to come up)
			final int MAX_SETUP_TIME = 30000; // milliseconds
			long startTimestamp = System.currentTimeMillis();
			NEXT_TRY: for (;;) {

				// Ensure clean up connection (if failure on further connection setup)
				Connection[] connectionReference = new Connection[1];
				try {

					// Obtain connection
					connectionFactory.createConnection((connection) -> {
						connectionReference[0] = connection;
						return connection;
					});

					// Return the connection
					if (connectionReference[0] != null) {
						return connectionReference[0];
					}

					// As here, no connection was created
					throw new SQLException("No connection created");

				} catch (Throwable ex) {

					// Ensure clean up connection
					// (avoids too many connections/files open issue)
					if (connectionReference[0] != null) {
						try {
							connectionReference[0].close();
						} catch (SQLException ignoreEx) {
							// ignore close failure
						}
					}

					// Failed setup, determine if try again
					long currentTimestamp = System.currentTimeMillis();
					if (currentTimestamp > (startTimestamp + MAX_SETUP_TIME)) {
						throw new RuntimeException("Timed out setting up JDBC test ("
								+ (currentTimestamp - startTimestamp) + " milliseconds)", ex);

					} else {
						// Try again in a little
						if (lock == null) {
							Thread.sleep(100);
						} else {
							lock.wait(100);
						}
						continue NEXT_TRY;
					}
				}
			}

		} finally {
			// Reinstate standard out / error
			System.setOut(stdout);
			System.setErr(stderr);
		}
	}

	/**
	 * Path to the {@link DataSource} properties file.
	 */
	private final String dataSourcePropertiesFilePath;

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * {@link Connection}.
	 */
	private Connection connection;

	/**
	 * Instantiate.
	 * 
	 * @param dataSourcePropertiesFilePath Path to the {@link DataSource} properties
	 *                                     file.
	 */
	public DataSourceRule(String dataSourcePropertiesFilePath) {
		this.dataSourcePropertiesFilePath = dataSourcePropertiesFilePath;
	}

	/**
	 * Obtains the {@link DataSource}.
	 * 
	 * @return {@link DataSource};
	 */
	public DataSource getDataSource() {
		if (this.dataSource == null) {
			throw new IllegalStateException(DataSource.class.getSimpleName() + " only available during test");
		}
		return this.dataSource;
	}

	/**
	 * Convenience method to obtain a {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 * @throws SQLException If fails to obtain {@link Connection}.
	 */
	public Connection getConnection() throws SQLException {
		return this.getDataSource().getConnection();
	}

	/**
	 * Obtains the active {@link Connection}.
	 * 
	 * @return Active {@link Connection}.
	 */
	public Connection getActiveConnection() {
		if (this.connection == null) {
			throw new IllegalStateException(
					"Active " + Connection.class.getSimpleName() + " only available during test");
		}
		return this.connection;
	}

	/*
	 * ================ TestRule ======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Load the data source
				DataSource rawDataSource = DefaultDataSourceFactory
						.createDataSource(DataSourceRule.this.dataSourcePropertiesFilePath);

				// Wrap data source to ensure close connections
				List<Connection> connections = new LinkedList<>();
				DataSourceRule.this.dataSource = (DataSource) Proxy.newProxyInstance(this.getClass().getClassLoader(),
						new Class[] { DataSource.class }, (proxy, method, args) -> {

							// Undertake method
							Method delegateMethod = rawDataSource.getClass().getMethod(method.getName(),
									method.getParameterTypes());
							Object result = delegateMethod.invoke(rawDataSource, args);

							// Register connection
							if ((result != null) && (result instanceof Connection)) {
								connections.add((Connection) result);
							}

							// Return the result
							return result;
						});
				try {

					// Obtain connection to keep potential in memory database active
					try (Connection connection = waitForDatabaseAvailable(
							(context) -> context.setConnection(DataSourceRule.this.dataSource.getConnection()))) {
						DataSourceRule.this.connection = connection;

						// Undertake the test
						base.evaluate();

					} finally {
						// Clean up
						DataSourceRule.this.connection = null;
						DataSourceRule.this.dataSource = null;
					}

				} finally {
					// Ensure connections are closed
					for (Connection connection : connections) {
						try {
							connection.close();
						} catch (SQLException ex) {
							// Ignore failure to close connection
						}
					}
				}
			}
		};
	}

}