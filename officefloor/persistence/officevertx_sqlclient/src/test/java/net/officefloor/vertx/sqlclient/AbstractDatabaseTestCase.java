/*-
 * #%L
 * Vertx SQL Client
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
