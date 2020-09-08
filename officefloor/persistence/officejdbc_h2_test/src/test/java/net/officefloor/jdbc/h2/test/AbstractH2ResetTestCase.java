/*-
 * #%L
 * H2 Test
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

package net.officefloor.jdbc.h2.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.plugin.clazz.Dependency;

/**
 * Abstract test functionality for {@link H2Reset}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractH2ResetTestCase {

	/**
	 * {@link DataSource}.
	 */
	private @Dependency DataSource dataSource;

	/**
	 * Undertakes reset test.
	 * 
	 * @param isReset Indicates if reset.
	 * @param logic   Test logic.
	 */
	protected void doTest(boolean isReset, Runnable logic) throws Exception {

		// Set up previous test state to be reset
		DatabaseTestUtil.waitForAvailableDatabase((cleanUp) -> this.dataSource, (connection) -> {
			try (Statement statement = connection.createStatement()) {
				statement.execute("DROP ALL OBJECTS");
				statement.execute("CREATE TABLE REMOVE ( ID BIGINT IDENTITY PRIMARY KEY )");
			}
		});

		// Ensure the previous state
		final String PREVIOUS_STATE_TABLE_NAME = "REMOVE";
		assertTrue(this.isTableExists(PREVIOUS_STATE_TABLE_NAME), "Ensure previous test state exists");

		// Undertake test logic
		logic.run();

		// Should clean database
		assertFalse(this.isTableExists(PREVIOUS_STATE_TABLE_NAME), "Should clean database");

		// Determine whether just clean or reset
		final String MIGRATE = "MIGRATE";
		if (isReset) {
			assertTrue(this.isTableExists(MIGRATE), "Should reset the database");
		} else {
			assertFalse(this.isTableExists(MIGRATE), "Should only clean the database");
		}
	}

	/**
	 * Indicates if table exists.
	 * 
	 * @param tableName Name of table.
	 * @return <code>true</code> if table exists.
	 */
	private boolean isTableExists(String tableName) throws SQLException {
		try (Connection connection = this.dataSource.getConnection()) {
			ResultSet resultSet = connection.createStatement().executeQuery(
					"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "'");
			return resultSet.next();
		}
	}

}
