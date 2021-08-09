/*-
 * #%L
 * H2 Test
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
