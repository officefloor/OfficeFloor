/*-
 * #%L
 * JPA Persistence on top of PostgreSql
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

package net.officefloor.jpa.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.postgresql.PostgreSqlDataSourceManagedObjectSource;
import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;
import net.officefloor.jdbc.postgresql.test.PostgreSqlExtension;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.hibernate.HibernateJpaManagedObjectSource;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;
import net.officefloor.test.UsesDockerTest;

/**
 * Hibernate {@link AbstractJpaTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class HibernatePostgreSqlJpaTest extends AbstractJpaTestCase {

	/**
	 * PostgreSql database.
	 */
	@RegisterExtension
	public static PostgreSqlExtension database = new PostgreSqlExtension(
			new Configuration().port(5433).username("testuser").password("testpassword"));

	@BeforeEach
	public void initiateLogger() throws Exception {

		// Ignore hibernate logging
		Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {

		// Ensure not in transaction
		if (!connection.getAutoCommit()) {
			connection.setAutoCommit(true);
		}

		// Create the database
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE IF EXISTS MOCKENTITY");
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
	protected void loadDataSourceProperties(PropertyConfigurable mos) {
		mos.addProperty(DataSourceManagedObjectSource.PROPERTY_DATA_SOURCE_FACTORY,
				PostgreSqlDataSourceManagedObjectSource.class.getName());
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_SERVER_NAME, "localhost");
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_PORT, "5433");
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_USER, "testuser");
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_PASSWORD, "testpassword");
	}

	@Override
	protected Class<? extends IMockEntity> getMockEntityClass() {
		return MockEntity.class;
	}

	@Override
	protected Class<?> getNoConnectionFactoryExceptionClass() {
		return ServiceException.class;
	}

	@Override
	protected String getNoConnectionFactoryExceptionMessage() {
		return "Unable to create requested service";
	}
}
