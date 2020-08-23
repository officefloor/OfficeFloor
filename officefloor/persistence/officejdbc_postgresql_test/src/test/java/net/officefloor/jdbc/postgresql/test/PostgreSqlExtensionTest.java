/*-
 * #%L
 * PostgreSQL Persistence Testing
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

package net.officefloor.jdbc.postgresql.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.postgresql.ds.PGSimpleDataSource;

import net.officefloor.frame.test.UsesDockerTest;
import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;

/**
 * Tests the {@link PostgreSqlExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlExtensionTest {

	/**
	 * Max connections.
	 */
	private static final int MAX_CONNECTIONS = 50;

	/**
	 * {@link Extension} to test.
	 */
	@RegisterExtension
	public static final PostgreSqlExtension postgreSql = new PostgreSqlExtension(
			new Configuration().server("localhost").port(5433).database("database").username("testuser")
					.password("testpassword").maxConnections(MAX_CONNECTIONS));

	/**
	 * Ensure able to run PostgreSql.
	 */
	@UsesDockerTest
	public void connectivity() throws Throwable {

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
			assertTrue(resultSet.next(), "Should have row");
			assertEquals(1, resultSet.getInt("ID"), "Incorrect row");
			assertEquals("test", resultSet.getString("NAME"), "Incorrect row value");
			assertFalse(resultSet.next(), "Should only be one row");
		}
	}

	/**
	 * Ensure able to configure the max connections.
	 */
	@UsesDockerTest
	public void adjustMaxConnections() throws Throwable {

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
				assertTrue(ex.getMessage().contains("FATAL: sorry, too many clients already"),
						"Incorrect cause: " + ex.getMessage());
			}

		} finally {
			// Close the connections
			for (Connection connection : connections) {
				connection.close();
			}
		}
	}

}
