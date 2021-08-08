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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Tests the {@link ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionManagedObjectSourceTest {

	@RegisterExtension
	public final OfficeFloorJdbcExtension jdbc = new OfficeFloorJdbcExtension();

	@BeforeEach
	public void resetMockConnection() throws Exception {

		// Reset mock section
		MockSection.connection = null;
	}

	/**
	 * Ensures the database is appropriately setup.
	 */
	@Test
	public void ensureDatabaseSetup() throws Exception {

		// Insert row into table
		try (Statement statement = this.jdbc.getConnection().createStatement()) {
			statement.execute("INSERT INTO TEST ( ID, NAME ) VALUES ( 1, 'test' )");
		}

		// Ensure can obtain row
		try (Statement statement = this.jdbc.getConnection().createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT NAME FROM TEST WHERE ID = 1");
			assertTrue(resultSet.next(), "Should have row");
			assertEquals("test", resultSet.getString("NAME"), "Incorrect row");
		}
	}

	/**
	 * Validate the specification.
	 */
	@Test
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(ConnectionManagedObjectSource.class);
	}

	/**
	 * Validate the type.
	 */
	@Test
	public void type() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Connection.class);
		type.addDependency(ConnectionManagedObjectSource.DependencyKeys.DATA_SOURCE, DataSource.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, ConnectionManagedObjectSource.class);
	}

	/**
	 * Ensures {@link Connection} with compiler.
	 */
	@Test
	public void connectionWithCompiler() throws Throwable {
		this.doConnectionTest();
	}

	/**
	 * Ensures {@link Connection} with {@link Proxy}.
	 */
	@Test
	public void connectionWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doConnectionTest());
	}

	/**
	 * Ensures {@link Connection}.
	 */
	public void doConnectionTest() throws Throwable {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the connection
			context.getOfficeArchitect()
					.addOfficeManagedObjectSource("mo", ConnectionManagedObjectSource.class.getName())
					.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Create the data source for the connection
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("DATA_SOURCE",
					DataSourceManagedObjectSource.class.getName());
			this.jdbc.loadProperties(mos);
			mos.addOfficeManagedObject("DATA_SOURCE", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockSection.class);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Undertake operation
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.section", null);

		// Ensure row inserted
		try (Statement statement = this.jdbc.getConnection().createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT NAME FROM TEST WHERE ID = 2");
			assertTrue(resultSet.next(), "Should have row");
			assertEquals("OfficeFloor", resultSet.getString("NAME"), "Incorrect row");
		}

		// Ensure the connection is closed
		assertNotNull(MockSection.connection, "Should have connection");
		assertTrue(MockSection.connection.isClosed(), "Connection used should be closed");
	}

	public static class MockSection {

		private static Connection connection = null;

		public void section(Connection connection) throws SQLException {
			try (Statement statement = connection.createStatement()) {
				statement.execute("INSERT INTO TEST ( ID, NAME ) VALUES ( 2, 'OfficeFloor' )");
			}
			MockSection.connection = connection;
		}
	}

}
