/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.jpa.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.postgresql.PostgreSqlConnectionManagedObjectSource;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.hibernate.HibernateJpaManagedObjectSource;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Hibernate {@link AbstractJpaTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public class HibernatePostgreSqlJpaTest extends AbstractJpaTestCase {

	/**
	 * PostgreSql database.
	 */
	private PostgreSqlRule database = new PostgreSqlRule(5433, "testuser", "testpassword");

	@Override
	protected void setUp() throws Exception {

		// Set up the database
		this.database.startPostgreSql();

		// Setup JPA
		super.setUp();

		// Ignore hibernate logging
		Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Tear down
			super.tearDown();
		} finally {
			// Stop PostgreSql
			this.database.stopPostgreSql();
		}
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE DATABASE ALIVE"); // ensure available
		}
	}

	@Override
	protected Class<? extends JpaManagedObjectSource> getJpaManagedObjectSourceClass() {
		return HibernateJpaManagedObjectSource.class;
	}

	@Override
	protected void loadJpaProperties(PropertyConfigurable jpa) {
		jpa.addProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_UNIT, "test");
	}

	@Override
	protected void loadDatabaseProperties(PropertyConfigurable mos) {
		mos.addProperty(ConnectionManagedObjectSource.PROPERTY_DATA_SOURCE_FACTORY,
				PostgreSqlConnectionManagedObjectSource.class.getName());
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_SERVER_NAME, "localhost");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PORT, "5433");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_USER, "testuser");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PASSWORD, "testpassword");
	}

	@Override
	protected Class<? extends IMockEntity> getMockEntityClass() {
		return MockEntity.class;
	}

}