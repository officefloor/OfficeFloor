/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import javax.persistence.TypedQuery;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

import org.junit.Ignore;

/**
 * Tests the {@link JpaEntityManagerManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore
public class JpaEntityManagerManagedObjectSourceTest extends
		AbstractJpaTestCase {

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
				JpaEntityManagerManagedObjectSource.class.getName(), null,
				new AutoWire(EntityManager.class));
		jpa.addProperty(
				JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME,
				"test");

		// Invoke task to retrieve entity
		AutoWireOfficeFloor officeFloor = app.openOfficeFloor();
		Result result = new Result();
		officeFloor.invokeTask("READ", "task", result);

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
					"SELECT MockEntity WHERE NAME = 'test'", MockEntity.class);
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
				JpaEntityManagerManagedObjectSource.class.getName(), null,
				new AutoWire(EntityManager.class));
		jpa.addProperty(
				JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME,
				"test");

		// Invoke task to retrieve entity
		AutoWireOfficeFloor officeFloor = app.openOfficeFloor();
		officeFloor.invokeTask("WRITE", "task", null);

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