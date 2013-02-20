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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.hsqldb.jdbcDriver;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.test.governance.GovernanceLoaderUtil;
import net.officefloor.compile.test.governance.GovernanceTypeBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link JpaTransactionGovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaTransactionGovernanceSourceTest extends AbstractJpaTestCase {

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {

		// Setup the connection
		super.setUp();

		// Configure the application
		AutoWireApplication app = new AutoWireOfficeFloorSource();
		AutoWireSection transactionSection = app.addSection("TRANSACTION",
				ClassSectionSource.class.getName(),
				TransactionWork.class.getName());
		AutoWireSection nonTransactionSection = app.addSection(
				"NON_TRANSACTION", ClassSectionSource.class.getName(),
				NonTransactionWork.class.getName());
		app.link(transactionSection, "check", nonTransactionSection, "check");
		app.linkEscalation(PersistenceException.class, nonTransactionSection,
				"handleFailure");
		app.addObject(this.connection, new AutoWire(Connection.class));
		AutoWireObject jpaObject = app.addManagedObject(
				JpaEntityManagerManagedObjectSource.class.getName(),
				new ManagedObjectSourceWirer() {
					@Override
					public void wire(ManagedObjectSourceWirerContext context) {
						context.mapTeam(
								JpaEntityManagerManagedObjectSource.TEAM_CLOSE,
								new AutoWire(EntityManager.class));
					}
				}, new AutoWire(EntityManager.class));
		jpaObject
				.addProperty(
						JpaEntityManagerManagedObjectSource.PROPERTY_PERSISTENCE_UNIT_NAME,
						"test");
		jpaObject.addProperty("datanucleus.ConnectionDriverName",
				jdbcDriver.class.getName());
		app.addGovernance("TRANSACTION",
				JpaTransactionGovernanceSource.class.getName()).governSection(
				transactionSection);

		// Open the OfficeFloor
		this.officeFloor = app.openOfficeFloor();
	}

	/**
	 * Validate the specification.
	 */
	public void testSpecification() {
		GovernanceLoaderUtil
				.validateSpecification(JpaTransactionGovernanceSource.class);
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		GovernanceTypeBuilder<?> type = GovernanceLoaderUtil
				.createGovernanceTypeBuilder();
		type.setExtensionInterface(EntityTransaction.class);

		// Validate the type
		GovernanceLoaderUtil.validateGovernanceType(type,
				JpaTransactionGovernanceSource.class);
	}

	/**
	 * Ensure {@link Governance} commits transaction.
	 */
	public void testCommitTransaction() throws Exception {

		// Invoke task to write entity within transaction
		this.officeFloor.invokeTask("TRANSACTION.WORK", "task", null);

		// Validate the entry is within the database
		assertEntry(this.connection);
	}

	/**
	 * Ensure {@link Governance} rolls back transaction.
	 */
	public void testRollbackTransaction() throws Exception {

		// Invoke task that fails rolling back transaction
		this.officeFloor.invokeTask("TRANSACTION.WORK", "failingTask", null);

		// Validate no entries as rollback
		assertNoEntries(this.connection);
	}

	/**
	 * Validate no entries in database.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 */
	private static void assertNoEntries(Connection connection)
			throws SQLException {
		Statement statement = connection.createStatement();
		ResultSet results = statement.executeQuery("SELECT * FROM MOCKENTITY");
		assertFalse("Should not be an entry", results.next());
		statement.close();
	}

	/**
	 * Validate entry within database.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @return Identifier for {@link MockEntity}.
	 */
	private static long assertEntry(Connection connection) throws SQLException {

		// Validate entry in database
		Statement statement = connection.createStatement();
		ResultSet results = statement
				.executeQuery("SELECT ID, NAME, DESCRIPTION FROM MOCKENTITY");
		assertTrue("Should have entry", results.next());
		assertEquals("Incorrect name", "transaction", results.getString("NAME"));
		assertEquals("Incorrect description", "mock transaction entity",
				results.getString("DESCRIPTION"));
		long identifier = results.getLong("ID");
		statement.close();

		// Return the identifier
		return identifier;
	}

	/**
	 * Mock {@link Work} for test by similar name.
	 */
	public static class TransactionWork {

		@NextTask("check")
		public void task(EntityManager entityManager, Connection connection)
				throws SQLException {

			// Ensure within transaction
			assertTrue("Should be within transaction", entityManager
					.getTransaction().isActive());

			// Persist the entity
			entityManager.persist(new MockEntity("transaction",
					"mock transaction entity"));

			// Ensure entity not yet written as within transaction
			assertNoEntries(connection);
		}

		public void failingTask(EntityManager entityManager) {

			// Ensure within transaction
			assertTrue("Should be within transaction", entityManager
					.getTransaction().isActive());

			// Write an entry that will persist (but be rolled back)
			entityManager.persist(new MockEntity("VALID",
					"Valid entry to rollback"));

			// Write an entry to fail persisting (rolling back transaction)
			entityManager.persist(new MockEntity(null,
					"Causes transaction rollback"));
		}
	}

	/**
	 * Mock {@link Work} that does not have transaction {@link Governance}.
	 */
	public static class NonTransactionWork {

		public void check(Connection connection, EntityManager entityManager)
				throws SQLException {

			// Ensure not within transaction
			assertFalse("Should not be within a transaction", entityManager
					.getTransaction().isActive());

			// Ensure entity written after transaction
			long identifier = assertEntry(connection);

			// Ensure able to continue to use entity manager
			MockEntity entity = entityManager.find(MockEntity.class,
					Long.valueOf(identifier));
			assertEquals("Incorrect name", "transaction", entity.getName());
			assertEquals("Incorrect description", "mock transaction entity",
					entity.getDescription());
		}

		public void handleFailure(@Parameter PersistenceException ex,
				EntityManager entityManager, Connection connection)
				throws SQLException {

			// Ensure not within transaction
			assertFalse("Should not be within transaction", entityManager
					.getTransaction().isActive());

			// Ensure no entries on rollback
			assertNoEntries(connection);
		}
	}

}