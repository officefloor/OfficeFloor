/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.managedobject.clazz;

import java.sql.Connection;

import junit.framework.TestCase;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link ClassManagedObjectSource}.
 * 
 * @author Daniel
 */
public class ClassManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification correct.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensure able to load the {@link ManagedObjectType} for the
	 * {@link ClassManagedObjectSource}.
	 */
	public void testManagedObjectType() {

		// Create the managed object type builder for the expected type
		ManagedObjectTypeBuilder expected = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();

		// Ensure correct object type
		expected.setObjectClass(MockClass.class);

		// Dependencies
		expected.addDependency("connection", Connection.class, 0, null);
		expected.addDependency("sqlQuery", String.class, 1, null);

		// Class should be the extension interface to allow administration
		// (Allows implemented interfaces to also be extension interfaces)
		expected.addExtensionInterface(MockClass.class);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(expected,
				ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
	}

	/**
	 * Ensure able to load the {@link ManagedObjectType} when child class has
	 * same field name.
	 */
	public void testOverrideField() {

		// Create the managed object type builder for the expected type
		ManagedObjectTypeBuilder expected = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();

		// Ensure correct object type
		expected.setObjectClass(OverrideMockClass.class);

		// Dependencies
		expected.addDependency("OverrideMockClass.connection", Integer.class,
				0, null);
		expected.addDependency("ParentMockClass.connection", Connection.class,
				1, null);

		// Verify extension interface
		expected.addExtensionInterface(OverrideMockClass.class);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(expected,
				ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				OverrideMockClass.class.getName());
	}

	/**
	 * Ensures can inject dependencies into the object.
	 */
	@SuppressWarnings("unchecked")
	public void testInjectDependencies() throws Throwable {

		final String SQL_QUERY = "SELECT * FROM TABLE";
		final Connection connection = this.createMock(Connection.class);
		final ObjectRegistry<Indexed> objectRegistry = this
				.createMock(ObjectRegistry.class);

		// Record obtaining the dependencies
		this.recordReturn(objectRegistry, objectRegistry.getObject(0),
				connection);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1),
				SQL_QUERY);

		// Replay mocks
		this.replayMockObjects();

		// Load the class managed object source
		ManagedObjectSourceStandAlone standAlone = new ManagedObjectSourceStandAlone();
		standAlone.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ManagedObjectSource<Indexed, None> source = standAlone
				.loadManagedObjectSource(ClassManagedObjectSource.class);

		// Source the managed object
		ManagedObject managedObject = ManagedObjectUserStandAlone
				.sourceManagedObject(source);
		assertTrue("Managed object must be coordinating",
				managedObject instanceof CoordinatingManagedObject);
		CoordinatingManagedObject<Indexed> coordinatingManagedObject = (CoordinatingManagedObject<Indexed>) managedObject;

		// Coordinate the managed object
		coordinatingManagedObject.loadObjects(objectRegistry);

		// Obtain the object and validate correct type
		Object object = managedObject.getObject();
		assertTrue("Incorrect object type", object instanceof MockClass);
		MockClass mockClass = (MockClass) object;

		// Verify the dependencies injected
		mockClass.verify(SQL_QUERY, connection);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to instantiate a new instances for unit testing.
	 */
	public void testNewInstance() throws Exception {

		final Connection connection = this.createMock(Connection.class);
		final String SQL_QUERY = "SELECT * FROM TABLE";

		// Create the instance
		MockClass mockClass = ClassManagedObjectSource.newInstance(
				MockClass.class, "sqlQuery", SQL_QUERY, "connection",
				connection);

		// Verify the dependencies injected
		mockClass.verify(SQL_QUERY, connection);
	}

	/**
	 * Mock class for testing.
	 */
	public static class MockClass extends ParentMockClass {

		/**
		 * Ensure can inject parent dependencies.
		 */
		@Dependency
		private String sqlQuery;

		/**
		 * Verifies the dependencies.
		 * 
		 * @param sqlQuery
		 *            Expected SQL query.
		 * @param connection
		 *            Expected {@link Connection}.
		 */
		public void verify(String sqlQuery, Connection connection) {
			TestCase.assertEquals("Incorrect sql query", sqlQuery,
					this.sqlQuery);
			this.verify(connection);
		}
	}

	/**
	 * Parent mock class for testing.
	 */
	public static class ParentMockClass {

		/**
		 * {@link Connection}.
		 */
		@Dependency
		private Connection connection;

		/**
		 * Field not a dependency.
		 */
		protected String notDependency;

		/**
		 * Verifies the dependencies.
		 * 
		 * @param connection
		 *            Expected {@link Connection}.
		 */
		public void verify(Connection connection) {
			TestCase.assertEquals("Incorrect connection", connection,
					this.connection);
		}
	}

	/**
	 * Override mock class.
	 */
	public static class OverrideMockClass extends ParentMockClass {

		/**
		 * Overriding connection field.
		 */
		@Dependency
		protected Integer connection;
	}
}