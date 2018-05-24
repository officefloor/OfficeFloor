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

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link JpaEntityManagerManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: type
public class JpaEntityManagerManagedObjectSourceTest extends AbstractJpaTestCase {

	/**
	 * Validate the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(JpaEntityManagerManagedObjectSource.class,
				JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME, "Persistence Unit");
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addTeam(JpaEntityManagerManagedObjectSource.TEAM_CLOSE);
		type.addExtensionInterface(EntityTransaction.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, JpaEntityManagerManagedObjectSource.class,
				JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME, "test");
	}

	// END SNIPPET: type

	// START SNIPPET: tutorial

	/**
	 * Ensure able to read entry from database.
	 */
	public void testRead() throws Exception {

		// Add entry to database
		Statement statement = this.connection.createStatement();
		statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock read entry')");
		statement.close();

		// Configure the application
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource("JPA",
					JpaEntityManagerManagedObjectSource.class.getName());
			mos.addProperty(JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME, "test");
			mos.addProperty("datanucleus.ConnectionDriverName", jdbcDriver.class.getName());
			mos.addOfficeManagedObject("JPA", ManagedObjectScope.THREAD);
			context.addSection("READ", ReadWork.class);
		});

		// Invoke function to retrieve entity
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();
		Result result = new Result();
		officeFloor.getOffice("OFFICE").getFunctionManager("READ.function").invokeProcess(result, null);

		// Validate the result
		assertNotNull("Should have result", result.entity);
		assertEquals("Incorrect name", "test", result.entity.getName());
		assertEquals("Incorrect description", "mock read entry", result.entity.getDescription());
	}

	/**
	 * Holder for the result.
	 */
	public static class Result {
		public MockEntity entity = null;
	}

	/**
	 * Mock {@link ManagedFunction} for test by similar name.
	 */
	public static class ReadWork {
		public void task(@Parameter Result result, EntityManager entityManager) {

			// Retrieve the entity
			TypedQuery<MockEntity> query = entityManager.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'test'",
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
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();
			context.addSection("WRITE", WriteWork.class);
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource("JPA",
					JpaEntityManagerManagedObjectSource.class.getName());
			mos.addProperty(JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME, "test");
			mos.addProperty("datanucleus.ConnectionDriverName", jdbcDriver.class.getName());
			mos.addOfficeManagedObject("JPA", ManagedObjectScope.THREAD);
		});

		// Invoke function to retrieve entity
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();
		officeFloor.getOffice("OFFICE").getFunctionManager("WRITE.function").invokeProcess(null, null);

		// Validate the entry written to database
		Statement statement = this.connection.createStatement();
		ResultSet results = statement.executeQuery("SELECT NAME, DESCRIPTION FROM MOCKENTITY");
		assertTrue("Should have entry", results.next());
		assertEquals("Incorrect name", "write", results.getString("NAME"));
		assertEquals("Incorrect description", "mock write entity", results.getString("DESCRIPTION"));
	}

	/**
	 * Mock {@link ManagedFunction} for test by similar name.
	 */
	public static class WriteWork {
		public void function(EntityManager entityManager) {

			// Create entry to write to database
			MockEntity entity = new MockEntity("write", "mock write entity");

			// Write entry to database
			entityManager.persist(entity);
		}
	}

}
// END SNIPPET: tutorial