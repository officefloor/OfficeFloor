/*-
 * #%L
 * PostgreSQL Persistence Testing
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.jdbc.postgresql.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;
import net.officefloor.test.skip.SkipJUnit4;

/**
 * Tests the {@link PostgreSqlRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlRuleTest {

	/**
	 * Max connections.
	 */
	private static final int MAX_CONNECTIONS = 50;

	/**
	 * {@link Rule} to test.
	 */
	@ClassRule
	public static final PostgreSqlRule postgreSql = new PostgreSqlRule(
			new Configuration().server("localhost").port(5433).database("database").username("testuser")
					.password("testpassword").maxConnections(MAX_CONNECTIONS));

	/**
	 * Ensure able to run PostgreSql.
	 */
	@Test
	public void connectivity() throws Throwable {
		SkipJUnit4.skipDocker();

		// Ensure able to connect to database via rule
		try (Connection connection = postgreSql.getConnection()) {
			connection.createStatement().execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
			connection.createStatement()
					.execute("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( 1, 'test' )");
		}

		// Ensure can connect to the create database in PostgreSql
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setPortNumbers(new int[] { 5433 });
		dataSource.setDatabaseName("database");
		dataSource.setUser("testuser");
		dataSource.setPassword("testpassword");
		try (Connection connection = dataSource.getConnection()) {
			ResultSet resultSet = connection.createStatement()
					.executeQuery("SELECT ID, NAME FROM OFFICE_FLOOR_JDBC_TEST");
			assertTrue("Should have row", resultSet.next());
			assertEquals("Incorrect row", 1, resultSet.getInt("ID"));
			assertEquals("Incorrect row value", "test", resultSet.getString("NAME"));
			assertFalse("Should only be one row", resultSet.next());
		}
	}

	/**
	 * Ensure able to configure the max connections.
	 */
	@Test
	public void adjustMaxConnections() throws Throwable {
		SkipJUnit4.skipDocker();

		// Ensure can connect to the create database in PostgreSql
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setPortNumbers(new int[] { 5433 });
		dataSource.setUser("testuser");
		dataSource.setPassword("testpassword");

		// Obtain the first connection
		List<Connection> connections = new ArrayList<>(MAX_CONNECTIONS);
		connections.add(postgreSql.getConnection());

		// Ensure able to obtain max connections
		for (int i = 1; i < MAX_CONNECTIONS; i++) {
			connections.add(dataSource.getConnection());
		}
		try {

			// Should not be able to open further connection
			try {
				connections.add(dataSource.getConnection());
				fail("Should not successfully create connection");
			} catch (SQLException ex) {
				assertTrue("Incorrect cause: " + ex.getMessage(),
						ex.getMessage().contains("FATAL: sorry, too many clients already"));
			}

		} finally {
			// Close the connections
			for (Connection connection : connections) {
				connection.close();
			}
		}
	}

}
