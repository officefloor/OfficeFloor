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

import java.sql.Statement;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Enables reseting the H2 database for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class H2Reset {

	/**
	 * {@link DataSource}.
	 */
	private final DataSource dataSource;

	/**
	 * {@link Flyway}.
	 */
	private final Flyway flyway;

	/**
	 * Instantiate.
	 * 
	 * @param dataSource {@link DataSource}.
	 * @param flyway     {@link Flyway}.
	 */
	public H2Reset(DataSource dataSource, Flyway flyway) {
		this.dataSource = dataSource;
		this.flyway = flyway;
	}

	/**
	 * Cleans for testing.
	 */
	public void clean() {
		try {
			DatabaseTestUtil.waitForAvailableDatabase((context) -> this.dataSource, (connection) -> {
				try (Statement statement = connection.createStatement()) {
					statement.execute("DROP ALL OBJECTS");
				}
			});
		} catch (Exception ex) {
			JUnitAgnosticAssert.fail(ex);
		}
	}

	/**
	 * Resets for testing.
	 */
	public void reset() {

		// Clean
		this.clean();

		// Migrate only if have flyway
		if (this.flyway != null) {
			this.flyway.migrate();
		}
	}
}
