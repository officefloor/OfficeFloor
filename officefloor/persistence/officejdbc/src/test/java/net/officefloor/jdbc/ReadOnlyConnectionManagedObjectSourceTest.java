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

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link ReadOnlyConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReadOnlyConnectionManagedObjectSourceTest extends AbstractConnectionTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Create row in database
		try (PreparedStatement statement = this.connection
				.prepareStatement("INSERT INTO TEST ( ID, NAME ) VALUES ( 1, 'test' )")) {
			statement.executeUpdate();
		}
	}

	/**
	 * Validate the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(ReadOnlyConnectionManagedObjectSource.class);
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Connection.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, ReadOnlyConnectionManagedObjectSource.class,
				DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
	}

	/**
	 * Ensure {@link Connection} with compiler.
	 */
	public void testConnectionWithCompiler() throws Throwable {
		this.doConnectionTest();
	}

	/**
	 * Ensure {@link Connection} with {@link Proxy}.
	 */
	public void testConnectionWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doConnectionTest());
	}

	/**
	 * Ensures {@link Connection}.
	 */
	public void doConnectionTest() throws Throwable {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					ReadOnlyConnectionManagedObjectSource.class.getName());
			this.loadProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockSection.class);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Undertake operation
		MockParameter parameter = new MockParameter();
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.section", parameter);

		// Ensure read row
		assertEquals("Should have read row", "test", parameter.value);

		// Ensure connection is open
		assertFalse("Connection should still be open", parameter.connection.isClosed());

		// Ensure unable to make non read only
		try {
			parameter.connection.setReadOnly(false);
			fail("Should not be able to change read-only");
		} catch (SQLException ex) {
			assertEquals("Incorrect cause", "Connection is re-used read-only so can not be changed", ex.getMessage());
		}

		// Ensure release connection on close OfficeFloor
		officeFloor.closeOfficeFloor();
		assertTrue("Connection should be closed with close of OfficeFloor", parameter.connection.isClosed());
	}

	public static class MockParameter {
		private volatile Connection connection;
		private volatile String value;
	}

	public static class MockSection {

		public void section(Connection connection, @Parameter MockParameter parameter) throws SQLException {
			parameter.connection = connection;
			try (PreparedStatement statement = connection.prepareStatement("SELECT NAME FROM TEST WHERE ID = 1")) {
				ResultSet resultSet = statement.executeQuery();
				assertTrue("Should have row", resultSet.next());
				parameter.value = resultSet.getString("NAME");
			}
		}
	}

	/**
	 * Ensures able to configure {@link DataSourceFactory}.
	 */
	public void testDataSourceFactory() throws Throwable {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					ReadOnlyConnectionManagedObjectSource.class.getName());
			mos.addProperty(DataSourceManagedObjectSource.PROPERTY_DATA_SOURCE_FACTORY,
					MockDataSourceFactory.class.getName());
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockSection.class);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Undertake operation
		MockParameter parameter = new MockParameter();
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.section", parameter);

		// Ensure read row
		assertEquals("Should have read row", "test", parameter.value);

		// Release connection with close of OfficeFloor
		officeFloor.closeOfficeFloor();
	}

	/**
	 * Mock {@link DataSourceFactory}.
	 */
	public static class MockDataSourceFactory implements DataSourceFactory {

		@Override
		public DataSource createDataSource(SourceContext context) throws Exception {
			JdbcDataSource dataSource = new JdbcDataSource();
			dataSource.setURL("jdbc:h2:mem:test");
			return dataSource;
		}
	}

	/**
	 * Ensure appropriate management of the {@link DataSource}.
	 */
	public void testDataSourceManagement() throws Throwable {
		this.doDataSourceManagementTest(ReadOnlyConnectionManagedObjectSource.class, 1);
	}

}
