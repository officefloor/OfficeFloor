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
import javax.persistence.PersistenceException;

/**
 * Ensure correct setup of JPA.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidJpaTest extends AbstractJpaTestCase {

	/**
	 * Ensure able to obtain data from {@link EntityManager}.
	 */
	public void testRead() throws Exception {

		// Provide entry
		Statement statement = this.connection.createStatement();
		statement
				.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock read entry')");
		statement.close();

		// Obtain object identifier
		statement = this.connection.createStatement();
		ResultSet results = statement.executeQuery("SELECT ID FROM MOCKENTITY");
		assertTrue("Should be entry in database", results.next());
		long identifier = results.getLong("ID");

		// Create the entity manager
		EntityManager entityManager = this.createEntityManager();

		// Obtain the object
		MockEntity entity = entityManager.find(MockEntity.class,
				Long.valueOf(identifier));
		assertNotNull("Should have entity", entity);
		assertEquals("Incorrect entity name", "test", entity.getName());
		assertEquals("Incorrect entity description", "mock read entry",
				entity.getDescription());
	}

	/**
	 * Ensure able write and retrieve the {@link MockEntity}.
	 */
	public void testWriteRead() throws Exception {

		// Create the entity to store
		MockEntity entity = new MockEntity("write", "mock write entry");

		// Store the entity
		EntityManager entityManager = this.createEntityManager();
		entityManager.persist(entity);
		entityManager.close();
		assertNotNull("Should now have identifier", entity.getId());

		// Ensure entry in database
		Statement statement = this.connection.createStatement();
		ResultSet results = statement
				.executeQuery("SELECT NAME FROM MOCKENTITY WHERE ID = "
						+ String.valueOf(entity.getId()));
		assertTrue("Should be entry in database", results.next());
		assertEquals("Incorrect name", "write", results.getString("NAME"));
		statement.close();

		// Obtain the entity
		EntityManager retrieveManager = this.createEntityManager();
		MockEntity retrieved = retrieveManager.find(MockEntity.class,
				entity.getId());
		assertNotNull("Should retrieve", retrieved);
		assertNotSame("Should be new instance", entity, retrieved);
		retrieveManager.close();
	}

	/**
	 * Ensure failure in attempting to write (for not null violation).
	 */
	public void testFailWrite() throws Exception {
		try {
			// Store entity causing not null violation
			EntityManager entityManager = this.createEntityManager();
			entityManager.persist(new MockEntity(null, "Not null violation"));
			entityManager.close();
			fail("Should not be successful");

		} catch (PersistenceException ex) {
			// Correctly fails
		}
	}

	/**
	 * Ensure able to use a transaction.
	 */
	public void testTransaction() throws Exception {

		// Create the entity to store
		MockEntity entity = new MockEntity("transaction",
				"Written in a transaction");

		// Create entity manager with transaction
		EntityManager entityManager = this.createEntityManager();
		entityManager.getTransaction().begin();

		// Write the entity
		entityManager.persist(entity);
		assertNull("Should not have identifier", entity.getId());

		// Ensure not in database
		Statement statement = this.connection.createStatement();
		ResultSet results = statement.executeQuery("SELECT * FROM MOCKENTITY");
		assertFalse("Should be no entries", results.next());
		statement.close();

		// Commit change to database
		entityManager.getTransaction().commit();

		// Ensure committed
		assertNotNull("Should now have identifier", entity.getId());

		// Ensure in database
		statement = this.connection.createStatement();
		results = statement
				.executeQuery("SELECT NAME FROM MOCKENTITY WHERE ID = "
						+ String.valueOf(entity.getId()));
		assertTrue("Should now be an entry", results.next());
		assertEquals("Incorrect entry", "transaction",
				results.getString("NAME"));
		statement.close();
	}

}