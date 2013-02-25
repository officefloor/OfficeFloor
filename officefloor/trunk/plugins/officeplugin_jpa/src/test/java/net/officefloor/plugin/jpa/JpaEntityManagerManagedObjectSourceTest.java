/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.jpa;

import java.sql.ResultSet;
import java.sql.Statement;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.hsqldb.jdbcDriver;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link JpaEntityManagerManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: type
public class JpaEntityManagerManagedObjectSourceTest extends
		AbstractJpaTestCase {

	/**
	 * Validate the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						JpaEntityManagerManagedObjectSource.class,
						JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME,
						"Persistence Unit");
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addTeam(JpaEntityManagerManagedObjectSource.TEAM_CLOSE);
		type.addExtensionInterface(EntityTransaction.class);

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						JpaEntityManagerManagedObjectSource.class,
						JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME,
						"test");
	}
	
	// END SNIPPET: type
	
	// START SNIPPET: tutorial

	/**
	 * Ensure able to read entry from database.
	 */
	public void testRead() throws Exception {

		// Add entry to database
		Statement statement = this.connection.createStatement();
		statement
				.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock read entry')");
		statement.close();

		// Configure the application
		AutoWireApplication app = new AutoWireOfficeFloorSource();
		app.addSection("READ", ClassSectionSource.class.getName(),
				ReadWork.class.getName());
		AutoWireObject jpa = app.addManagedObject(
				JpaEntityManagerManagedObjectSource.class.getName(),
				new ManagedObjectSourceWirer() {
					@Override
					public void wire(ManagedObjectSourceWirerContext context) {
						context.mapTeam(
								JpaEntityManagerManagedObjectSource.TEAM_CLOSE,
								new AutoWire(EntityManager.class));
					}
				}, new AutoWire(EntityManager.class));
		jpa.addProperty(
				JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME,
				"test");
		jpa.addProperty("datanucleus.ConnectionDriverName",
				jdbcDriver.class.getName());

		// Invoke task to retrieve entity
		AutoWireOfficeFloor officeFloor = app.openOfficeFloor();
		Result result = new Result();
		officeFloor.invokeTask("READ.WORK", "task", result);

		// Validate the result
		assertNotNull("Should have result", result.entity);
		assertEquals("Incorrect name", "test", result.entity.getName());
		assertEquals("Incorrect description", "mock read entry",
				result.entity.getDescription());
	}

	/**
	 * Holder for the result.
	 */
	public static class Result {
		public MockEntity entity = null;
	}

	/**
	 * Mock {@link Work} for test by similar name.
	 */
	public static class ReadWork {
		public void task(@Parameter Result result, EntityManager entityManager) {

			// Retrieve the entity
			TypedQuery<MockEntity> query = entityManager.createQuery(
					"SELECT M FROM MockEntity M WHERE M.name = 'test'",
					MockEntity.class);
			MockEntity entity = query.getSingleResult();

			// Specify the result
			result.entity = entity;
		}
	}

	/**
	 * Ensure able to write entry to database.
	 */
	public void testWrite() throws Exception {

		// Configure the application
		AutoWireApplication app = new AutoWireOfficeFloorSource();
		app.addSection("WRITE", ClassSectionSource.class.getName(),
				WriteWork.class.getName());
		AutoWireObject jpa = app.addManagedObject(
				JpaEntityManagerManagedObjectSource.class.getName(),
				new ManagedObjectSourceWirer() {
					@Override
					public void wire(ManagedObjectSourceWirerContext context) {
						context.mapTeam(
								JpaEntityManagerManagedObjectSource.TEAM_CLOSE,
								new AutoWire(EntityManager.class));
					}
				}, new AutoWire(EntityManager.class));
		jpa.addProperty(
				JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME,
				"test");
		jpa.addProperty("datanucleus.ConnectionDriverName",
				jdbcDriver.class.getName());

		// Invoke task to retrieve entity
		AutoWireOfficeFloor officeFloor = app.openOfficeFloor();
		officeFloor.invokeTask("WRITE.WORK", "task", null);

		// Validate the entry written to database
		Statement statement = this.connection.createStatement();
		ResultSet results = statement
				.executeQuery("SELECT NAME, DESCRIPTION FROM MOCKENTITY");
		assertTrue("Should have entry", results.next());
		assertEquals("Incorrect name", "write", results.getString("NAME"));
		assertEquals("Incorrect description", "mock write entity",
				results.getString("DESCRIPTION"));
	}

	/**
	 * Mock {@link Work} for test by similar name.
	 */
	public static class WriteWork {
		public void task(EntityManager entityManager) {

			// Create entry to write to database
			MockEntity entity = new MockEntity("write", "mock write entity");

			// Write entry to database
			entityManager.persist(entity);
		}
	}

}
// END SNIPPET: tutorial