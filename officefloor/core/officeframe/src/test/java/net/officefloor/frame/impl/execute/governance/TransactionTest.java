/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.governance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;

/**
 * Typical use of {@link Governance} is for transactions.
 *
 * @author Daniel Sagenschneider
 */
public class TransactionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can manage transaction.
	 */
	public void testTransaction() throws Exception {

		final Connection connection = this.createMock(Connection.class);
		final PreparedStatement statement = this.createMock(PreparedStatement.class);

		// Record executing with transaction
		connection.setAutoCommit(false);
		this.recordReturn(connection, connection.prepareStatement("UPDATE TEST SET TEST = 'passed'"), statement);
		this.recordReturn(statement, statement.executeUpdate(), 1);
		connection.commit();
		this.replayMockObjects();

		// Construct the managed object
		this.constructManagedObject("CONNECTION", new ConnectionManagedObjectSource(connection), this.getOfficeName());

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.getBuilder().addGovernance("TRANSACTION");
		task.buildObject("CONNECTION", ManagedObjectScope.THREAD).mapGovernance("TRANSACTION");

		// Construct the transaction governance
		TestTransaction transaction = new TestTransaction();
		ReflectiveGovernanceBuilder govern = this.constructGovernance(transaction, "TRANSACTION");
		govern.register("begin");
		govern.enforce("commit");

		// Ensure runs task within transaction
		this.invokeFunction("task", null);
		this.verifyMockObjects();
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(Connection connection) throws SQLException {
			connection.prepareStatement("UPDATE TEST SET TEST = 'passed'").executeUpdate();
		}
	}

	/**
	 * Test transaction {@link Governance}.
	 */
	public class TestTransaction {

		public void begin(Connection connection) throws SQLException {
			connection.setAutoCommit(false);
		}

		public void commit(Connection[] connections) throws SQLException {
			for (Connection connection : connections) {
				connection.commit();
			}
		}
	}

	/**
	 * {@link ManagedObjectSource} for the {@link Connection}.
	 */
	@TestSource
	private static class ConnectionManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		/**
		 * {@link Connection}.
		 */
		private final Connection connection;

		/**
		 * Instantiate.
		 * 
		 * @param connection
		 *            {@link Connection}.
		 */
		public ConnectionManagedObjectSource(Connection connection) {
			this.connection = connection;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Connection.class);
			context.addManagedObjectExtension(Connection.class, (managedObject) -> this.connection);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this.connection;
		}
	}

}