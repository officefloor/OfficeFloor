/*-
 * #%L
 * OfficeFloor Flyway
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

package net.officefloor.flyway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.h2.H2DataSourceManagedObjectSource;

/**
 * {@link Extension} for {@link Flyway} testing.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayExtension implements BeforeEachCallback, FlywayConfigurerServiceFactory, FlywayConfigurer {

	/**
	 * JDBC URL.
	 */
	public static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

	/**
	 * User name.
	 */
	public static final String USERNAME = "sa";

	/**
	 * Password.
	 */
	public static final String PASSWORD = "";

	/**
	 * Obtains the {@link DataSource}.
	 * 
	 * @return {@link DataSource}.
	 */
	public DataSource getDataSource() {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setUrl(JDBC_URL);
		dataSource.setUser(USERNAME);
		return dataSource;
	}

	/**
	 * Adds the {@link DataSource} to {@link OfficeArchitect}.
	 * 
	 * @param architect {@link OfficeArchitect}.
	 */
	public void addDataSource(OfficeArchitect architect) {
		OfficeManagedObjectSource dataSource = architect.addOfficeManagedObjectSource("DATASOURCE",
				H2DataSourceManagedObjectSource.class.getName());
		dataSource.addProperty(H2DataSourceManagedObjectSource.PROPERTY_URL, JDBC_URL);
		dataSource.addProperty(H2DataSourceManagedObjectSource.PROPERTY_USER, USERNAME);
		dataSource.addOfficeManagedObject("DATASOURCE", ManagedObjectScope.THREAD);
	}

	/**
	 * Obtains the {@link Flyway}.
	 * 
	 * @return {@link Flyway}.
	 */
	public Flyway getFlyway() {
		Map<String, String> environment = ConfigUtils.environmentVariablesToPropertyMap();
		return Flyway.configure().cleanDisabled(false).configuration(environment).dataSource(this.getDataSource())
				.load();
	}

	/**
	 * Asserts the database has been migrated.
	 */
	public void assertMigration() throws SQLException {
		try (Connection connection = this.getDataSource().getConnection()) {
			ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM FLYWAY_SETUP");
			assertTrue(resultSet.next(), "Should have row");
			assertEquals("AVAILABLE", resultSet.getString("MESSAGE"), "Incorrect setup row");
		}
	}

	/*
	 * ====================== BeforeEachCallback ========================
	 */

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		this.getFlyway().clean();
	}

	/*
	 * ==================== FlywayConfigurerServiceFactory ====================
	 */

	private static boolean isConfigure = false;

	/**
	 * Setup logic.
	 */
	@FunctionalInterface
	public static interface FailLogic {
		void logic() throws Throwable;
	}

	/**
	 * Run {@link FailLogic}.
	 * 
	 * @param logic {@link FailLogic}.
	 * @throws Exception If fails open.
	 */
	public void runWithFailMigration(FailLogic logic) throws Throwable {
		try {
			isConfigure = true;
			logic.logic();
		} finally {
			isConfigure = false;
		}
	}

	@Override
	public FlywayConfigurer createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void configure(FluentConfiguration configuration) throws Exception {
		if (isConfigure) {
			configuration.locations("classpath:db/fail");
		}
	}

}
