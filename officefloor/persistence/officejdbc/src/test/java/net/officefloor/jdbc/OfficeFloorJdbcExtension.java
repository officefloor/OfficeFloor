/*-
 * #%L
 * JDBC Persistence
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

package net.officefloor.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.zaxxer.hikari.HikariDataSource;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.ValidateConnections;

/**
 * JDBC {@link Extension}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorJdbcExtension implements BeforeEachCallback, AfterEachCallback {

	/**
	 * {@link Connection}.
	 */
	private Connection connection;

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 */
	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * Creates a new {@link Connection}.
	 * 
	 * @return New {@link Connection}.
	 */
	public Connection newConnection() throws SQLException {
		return DriverManager.getConnection(this.getJdbcUrl());
	}

	/**
	 * Cleans the database.
	 * 
	 * @param connection {@link Connection}.
	 */
	public void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		}
	}

	/**
	 * Obtains the JDBC URL.
	 * 
	 * @return JDBC URL.
	 */
	public String getJdbcUrl() {
		return "jdbc:h2:mem:test";
	}

	/**
	 * Loads the properties for the {@link ConnectionManagedObjectSource}.
	 * 
	 * @param mos {@link PropertyConfigurable}.
	 */
	public void loadProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
		mos.addProperty("uRL", this.getJdbcUrl());
	}

	/**
	 * Ensure:
	 * <ul>
	 * <li>connectivity test is undertaken on opening the {@link OfficeFloor}</li>
	 * <li>close the {@link DataSource} on {@link OfficeFloor} close if implements
	 * {@link AutoCloseable}</li>
	 * </ul>
	 * 
	 * @param managedObjectSource          {@link ManagedObjectSource}
	 *                                     {@link Class}.
	 * @param startupAdditionalConnections Additional {@link Connection} instances
	 *                                     used at start up (beyond connectivity
	 *                                     test).
	 */
	public void doDataSourceManagementTest(Class<? extends ManagedObjectSource<?, ?>> managedObjectSource,
			int startupAdditionalConnections) throws Throwable {

		// Obtain connection count to setup test
		int setupCount = ValidateConnections.getConnectionsRegisteredCount();

		// Open the OfficeFloor
		HikariDataSourceFactory.dataSource = null;
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					managedObjectSource.getName());
			mos.addProperty(DataSourceManagedObjectSource.PROPERTY_DATA_SOURCE_FACTORY,
					HikariDataSourceFactory.class.getName());
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);
		});
		OfficeFloor officeFloor = compiler.compileOfficeFloor();

		// Ensure no connections created and pool open
		assertEquals(setupCount, ValidateConnections.getConnectionsRegisteredCount(),
				"Compiling should not open connection");
		assertFalse(HikariDataSourceFactory.dataSource.isClosed(), "DataSource should be open");

		// Open the OfficeFloor (should increment connection for connectivity test)
		officeFloor.openOfficeFloor();
		assertEquals(setupCount + 1 + startupAdditionalConnections, ValidateConnections.getConnectionsRegisteredCount(),
				"Should use connection for connectivity test");
		assertFalse(HikariDataSourceFactory.dataSource.isClosed(), "DataSource should still be open");

		// Should close DataSource if implements AutoCloseable
		assertTrue(HikariDataSourceFactory.dataSource instanceof AutoCloseable,
				"Hikari DataSource should be closeable");
		officeFloor.closeOfficeFloor();
		assertTrue(HikariDataSourceFactory.dataSource.isClosed(), "Hikari DataSource should be closed");
	}

	/**
	 * Mock {@link DataSourceFactory}.
	 */
	public static class HikariDataSourceFactory implements DataSourceFactory {

		private static HikariDataSource dataSource;

		@Override
		public DataSource createDataSource(SourceContext context) throws Exception {
			dataSource = new HikariDataSource();
			dataSource.setJdbcUrl("jdbc:h2:mem:test");
			return dataSource;
		}
	}

	/*
	 * =================== Extension ==============================
	 */

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		this.beforeEach();
	}

	public Connection beforeEach() throws Exception {

		// Determine if already setup
		if (this.connection != null) {
			return this.connection;
		}

		// Create the connection
		this.connection = this.newConnection();

		// Clean database for testing
		this.cleanDatabase(this.connection);

		// Create table for testing
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("CREATE TABLE TEST ( ID INT, NAME VARCHAR(255) )");
		}

		// Return the connection
		return this.connection;
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		this.afterEach();
	}

	public void afterEach() throws Exception {
		if (this.connection != null) {
			this.connection.close();
		}
	}

}
