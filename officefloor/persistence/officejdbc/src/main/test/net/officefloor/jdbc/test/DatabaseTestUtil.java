/*-
 * #%L
 * JDBC Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.jdbc.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

/**
 * Provide utility functionality for database testing.
 * 
 * @author Daniel Sagenschneider
 */
public class DatabaseTestUtil {

	/**
	 * Creates the {@link DataSource}.
	 */
	@FunctionalInterface
	public static interface DataSourceCreator {

		/**
		 * Creates the {@link DataSource}.
		 * 
		 * @param context {@link DataSourceCreatorContext}.
		 * @return {@link DataSource}.
		 * @throws Exception If fails to create {@link DataSource}.
		 */
		DataSource create(DataSourceCreatorContext context) throws Exception;
	}

	/**
	 * Context for the {@link DataSourceCreator}.
	 */
	public static interface DataSourceCreatorContext {

		/**
		 * Adds a {@link DataSourceCleanup}.
		 * 
		 * @param cleanup {@link DataSourceCleanup}.
		 */
		void addCleanup(DataSourceCleanup cleanup);
	}

	/**
	 * Cleans up the {@link DataSource} creation attempt.
	 */
	@FunctionalInterface
	public static interface DataSourceCleanup {

		/**
		 * Cleans up the {@link DataSource} creation attempt.
		 * 
		 * @throws Exception If fails clean up.
		 */
		void cleanup() throws Exception;
	}

	/**
	 * Validates the database.
	 */
	@FunctionalInterface
	public static interface DatabaseValidator {

		/**
		 * Validates the database.
		 * 
		 * @param connection {@link Connection}.
		 * @throws Exception If fails to validate the database.
		 */
		void validate(Connection connection) throws Exception;
	}

	/**
	 * Waits for the database to be available.
	 * 
	 * @param dataSourceCreator {@link DataSourceCreator}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static void waitForAvailableDatabase(DataSourceCreator dataSourceCreator) throws Exception {
		waitForAvailableDatabase(dataSourceCreator, null);
	}

	/**
	 * Waits for the database to be available.
	 * 
	 * @param dataSourceCreator {@link DataSourceCreator}.
	 * @param validator         {@link DatabaseValidator}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static void waitForAvailableDatabase(DataSourceCreator dataSourceCreator, DatabaseValidator validator)
			throws Exception {
		waitForAvailableDatabase(null, dataSourceCreator, validator);
	}

	/**
	 * Waits for the database to be available.
	 * 
	 * @param lock              To wait on to allow locking setup.
	 * @param dataSourceCreator {@link DataSourceCreator}.
	 * @param validator         {@link DatabaseValidator}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static void waitForAvailableDatabase(Object lock, DataSourceCreator dataSourceCreator,
			DatabaseValidator validator) throws Exception {
		waitForAvailableConnection(lock, dataSourceCreator, validator).close();
	}

	/**
	 * Waits for {@link Connection} to be available.
	 * 
	 * @param dataSourceCreator {@link DataSourceCreator}.
	 * @return Available {@link Connection}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static Connection waitForAvailableConnection(DataSourceCreator dataSourceCreator) throws Exception {
		return waitForAvailableConnection(null, dataSourceCreator, null);
	}

	/**
	 * Waits for {@link Connection} to be available.
	 * 
	 * @param dataSourceCreator {@link DataSourceCreator}.
	 * @param validator         {@link DatabaseValidator}.
	 * @return Available {@link Connection}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static Connection waitForAvailableConnection(DataSourceCreator dataSourceCreator,
			DatabaseValidator validator) throws Exception {
		return waitForAvailableConnection(null, dataSourceCreator, validator);
	}

	/**
	 * Waits for {@link Connection} to be available.
	 * 
	 * @param lock              To wait on to allow locking setup.
	 * @param dataSourceCreator {@link DataSourceCreator}.
	 * @param validator         {@link DatabaseValidator}.
	 * @return Available {@link Connection}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static Connection waitForAvailableConnection(Object lock, DataSourceCreator dataSourceCreator,
			DatabaseValidator validator) throws Exception {

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
				List<DataSourceCleanup> cleanups = new LinkedList<>();
				boolean isSuccessful = false;
				Connection connection = null;
				try {

					// Obtain the data source
					DataSource dataSource = dataSourceCreator.create((cleanup) -> cleanups.add(cleanup));

					// Obtain connection
					connection = dataSource.getConnection();

					// Return the connection
					if (connection == null) {
						throw new SQLException("No connection created");
					}

					// Validate the connection
					if (validator != null) {
						validator.validate(connection);
					}

					// Create proxy connection to clean up on close
					final Connection finalConnection = connection;
					Connection cleanupConnection = (Connection) Proxy.newProxyInstance(
							DatabaseTestUtil.class.getClassLoader(), new Class[] { Connection.class },
							(proxy, method, args) -> {

								// Clean connection if closing
								if ("close".equals(method.getName())) {
									for (int i = cleanups.size() - 1; i >= 0; i--) {
										try {
											cleanups.get(i).cleanup();
										} catch (Exception ignore) {
											// Ignore close failure
										}
									}
								}

								// Undertake connection method
								Method connectionMethod = finalConnection.getClass().getMethod(method.getName(),
										method.getParameterTypes());
								try {
									return connectionMethod.invoke(finalConnection, args);
								} catch (InvocationTargetException ex) {
									throw ex.getCause();
								}
							});

					// As here, successful
					isSuccessful = true;
					return cleanupConnection;

				} catch (Throwable ex) {

					// Ensure clean up connection on failure
					// (avoids too many connections/files open issue)
					if (connection != null) {
						try {
							connection.close();
						} catch (SQLException ignore) {
							// Ignore close failure
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
							Thread.sleep(10);
						} else {
							lock.wait(10);
						}
						continue NEXT_TRY;
					}

				} finally {

					// Ensure clean up failure (in reverse order)
					if (!isSuccessful) {
						for (int i = cleanups.size() - 1; i >= 0; i--) {
							try {
								cleanups.get(i).cleanup();
							} catch (Exception ignore) {
								// Ignore close failure
							}
						}
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
	 * Static methods only.
	 */
	private DatabaseTestUtil() {
		// All access via static methods
	}
}
