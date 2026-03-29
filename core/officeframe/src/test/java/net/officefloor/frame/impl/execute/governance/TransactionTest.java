/*-
 * #%L
 * OfficeFrame
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
