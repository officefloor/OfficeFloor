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
