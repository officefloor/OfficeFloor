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
import java.sql.Connection;
import java.sql.SQLException;

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
		 * @return {@link Connection}.
		 * @throws Exception If fails to create {@link Connection}.
		 */
		Connection createConnection() throws Exception;
	}

	/**
	 * Waits for the database to be available.
	 * 
	 * @param connectionFactory {@link ConnectionFactory}.
	 * @return {@link Connection}.
	 * @throws Exception If failed waiting on database or {@link Connection} issue.
	 */
	public static Connection waitForDatabaseAvailable(ConnectionFactory connectionFactory) throws Exception {

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
				try {

					// Obtain connection
					return connectionFactory.createConnection();

				} catch (Throwable ex) {

					// Failed setup, determine if try again
					long currentTimestamp = System.currentTimeMillis();
					if (currentTimestamp > (startTimestamp + MAX_SETUP_TIME)) {
						throw new RuntimeException("Timed out setting up JDBC test ("
								+ (currentTimestamp - startTimestamp) + " milliseconds)", ex);

					} else {
						// Try again in a little
						Thread.sleep(100);
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
				DataSourceRule.this.dataSource = DefaultDataSourceFactory
						.createDataSource(DataSourceRule.this.dataSourcePropertiesFilePath);

				// Obtain connection to keep potential in memory database active
				try (Connection conneciton = waitForDatabaseAvailable(
						() -> DataSourceRule.this.dataSource.getConnection())) {
					DataSourceRule.this.connection = conneciton;

					// Undertake the test
					base.evaluate();

				} finally {
					// Clean up
					DataSourceRule.this.connection.close();
					DataSourceRule.this.connection = null;
					DataSourceRule.this.dataSource = null;
				}
			}
		};
	}

}