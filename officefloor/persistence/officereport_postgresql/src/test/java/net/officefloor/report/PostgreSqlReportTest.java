/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.postgresql.core.CachedQuery;
import org.postgresql.core.Field;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.ResultCursor;
import org.postgresql.core.ResultHandler;
import org.postgresql.core.ResultHandlerBase;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.PostgreSqlAccess;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests PostgreSql report data generation.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlReportTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to connect to the database.
	 * 
	 * To manually start PostgreSql:
	 * 
	 * <pre>
	 * docker run --name postgresql-pipeline -e POSTGRES_PASSWORD=test -d -p5432:5432 postgres
	 * </pre>
	 */
	public void testSetup() throws SQLException {

		// Create connection and setup tables
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setDatabaseName("postgres");
		dataSource.setUser("postgres");
		dataSource.setPassword("test");
		try (Connection connection = dataSource.getConnection()) {

			// Create the tables
			Statement statement = connection.createStatement();
			statement.execute("CREATE TABLE INVOICE ( ID INT, INVOICE_DETAILS VARCHAR(2024) )");

			// Load invoices
			try (PreparedStatement insert = connection
					.prepareStatement("INSERT INTO INVOICE ( ID, INVOICE_DETAILS ) VALUES ( ?, ? )")) {
				for (int i = 1; i <= 100; i++) {
					insert.setInt(1, i);
					insert.setString(2, "Invoice " + i);
					insert.execute();
				}
			}
		}
	}

	/**
	 * Tests {@link Query} interaction with database.
	 */
	public void _testQueryConnection() throws Exception {

		// Create connection and setup tables
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setDatabaseName("postgres");
		dataSource.setUser("postgres");
		dataSource.setPassword("test");

		// Create the access
		Connection connection = dataSource.getConnection();
		PostgreSqlAccess access = new PostgreSqlAccess((PgConnection) connection);
		connection.close();

		// Create the query executor
		QueryExecutor executor = access.createQueryExecutor("localhost", 5432, "postgres", "postgres", "test");

		// Create the query
		CachedQuery query = executor.createQuery("SELECT ID, INVOICE_DETAILS FROM INVOICE", true, false, "ID",
				"INVOICE_DETAILS");
		int maxRows = 100;
		int fetchSize = 100;

		// Run multiple times
		final int RUN_COUNT = 1000;
		final int[] completed = new int[] { 0 };
		for (int i = 0; i < RUN_COUNT; i++) {
			int index = i;
			ParameterList parameters = query.query.createParameterList();

			// Handle results
			ResultHandler handler = new ResultHandlerBase() {

				@Override
				public void handleResultRows(Query fromQuery, Field[] fields, List<byte[][]> tuples,
						ResultCursor cursor) {
					synchronized (executor) {

						// Describe the fields
						System.out.println("handleResultRows " + index);
//					System.out.print("  fields:");
//					for (Field field : fields) {
//						System.out.print(" " + field.getColumnLabel());
//					}
//					System.out.println();

						try {
							// Wrap rows in result set
							ResultSet resultSet = access.createResultSet(query.query, fields, tuples, cursor, maxRows);
//							while (resultSet.next()) {
//							System.out.println(" " + resultSet.getInt(1) + " -> " + resultSet.getString(2));
//							}

						} catch (Exception ex) {
							System.err.println("Resultset failure");
							ex.printStackTrace();
						}
					}

					// Indicate number completed
					synchronized (completed) {
						completed[0]++;
						completed.notify();
					}
				}
			};

			// Execute the query
			int flags = QueryExecutor.QUERY_FORWARD_CURSOR | QueryExecutor.QUERY_SUPPRESS_BEGIN;
			executor.execute(query.query, parameters, handler, maxRows, fetchSize, flags);
		}

		// Wait until all queries complete
		synchronized (completed) {
			while (completed[0] < RUN_COUNT) {
				completed.wait(1000);
				System.out.println("Completed " + completed[0]);
			}
		}
	}

}