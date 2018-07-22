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
package net.officefloor.jdbc.postgresql.test;

import java.sql.Connection;
import java.sql.ResultSet;

import org.junit.runners.model.Statement;
import org.postgresql.ds.PGSimpleDataSource;

import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link PostgreSqlRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlRuleTest extends OfficeFrameTestCase {

	/**
	 * {@link PostgreSqlRule} to test.
	 */
	private final PostgreSqlRule rule = new PostgreSqlRule("localhost", 5433, "database", "testuser", "testpassword");

	/**
	 * Ensure able to run PostgreSql.
	 */
	public void testPostgreSqlRule() throws Throwable {

		// Ensure can connect to rule
		Closure<Boolean> isRun = new Closure<>(false);
		this.rule.apply(new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Ensure able to connect to database via rule
				try (Connection connection = PostgreSqlRuleTest.this.rule.getConnection()) {
					connection.createStatement()
							.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
					connection.createStatement()
							.execute("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( 1, 'test' )");
				}

				// Ensure can connect to the create database in PostgreSql
				PGSimpleDataSource dataSource = new PGSimpleDataSource();
				dataSource.setPortNumber(5433);
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

				// Run
				isRun.value = true;
			}
		}, null).evaluate();

		// Ensure the test is run
		assertTrue("Failed to run wrapped test for rule", isRun.value);
	}

}