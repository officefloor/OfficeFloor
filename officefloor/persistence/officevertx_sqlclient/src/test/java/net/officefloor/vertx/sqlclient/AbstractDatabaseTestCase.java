/*-
 * #%L
 * Vertx SQL Client
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

package net.officefloor.vertx.sqlclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;
import net.officefloor.jdbc.postgresql.test.PostgreSqlExtension;

/**
 * Abstract test functionality for working with database.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractDatabaseTestCase {

	/**
	 * Time out of blocking.
	 */
	protected final static Duration TIMEOUT = Duration.ofSeconds(3);

	/**
	 * Port.
	 */
	protected final static int PORT = 5433;

	/**
	 * Name of database.
	 */
	protected final static String DATABASE = "test";

	/**
	 * User.
	 */
	protected final static String USER = "SA";

	/**
	 * Password.
	 */
	protected final static String PASSWORD = "Password";

	/**
	 * PostgreSql database.
	 */
	@RegisterExtension
	public static PostgreSqlExtension database = new PostgreSqlExtension(
			new Configuration().port(PORT).database(DATABASE).username(USER).password(PASSWORD));

	/**
	 * Sets up the database.
	 */
	protected void setupDatabase() throws Exception {
		try (Connection connection = database.getConnection()) {
			Statement statement = connection.createStatement();
			statement.execute("DROP TABLE IF EXISTS test");
			statement.execute("CREATE TABLE test (id INT, message VARCHAR(50))");
			int insertCount = statement.executeUpdate("INSERT INTO test (id, message) VALUES (1, 'TEST')");
			assertEquals(1, insertCount, "Should insert row");
		}
	}

	/**
	 * Configures the {@link VertxSqlPoolManagedObjectSource}.
	 * 
	 * @param office    {@link OfficeArchitect}.
	 * @param pgPoolMos {@link VertxSqlPoolManagedObjectSource}.
	 * @return {@link OfficeManagedObject} for the
	 *         {@link VertxSqlPoolManagedObjectSource}.
	 */
	protected OfficeManagedObject configureSqlPoolSource(OfficeArchitect office,
			VertxSqlPoolManagedObjectSource pgPoolMos) {
		OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource("MOS", pgPoolMos);
		mos.addProperty(VertxSqlPoolManagedObjectSource.PROPERTY_USERNAME, USER);
		mos.addProperty(VertxSqlPoolManagedObjectSource.PROPERTY_PASSWORD, PASSWORD);
		return mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
	}

}
