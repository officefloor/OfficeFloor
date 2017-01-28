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
package net.officefloor.frame.impl.execute.governance;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure do not unload {@link ManagedObject} that is under {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectUnloadGovernanceTest extends AbstractGovernanceTestCase {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(ManagedObjectUnloadGovernanceTest.class);
	}

	/**
	 * {@link TransactionalObject}.
	 */
	private final TransactionalObject object = this.createSynchronizedMock(TransactionalObject.class);

	/**
	 * Flag indicating whether to provide commit after {@link ManagedFunction}.
	 */
	private boolean isCommit = false;

	/**
	 * Flag indicating whether to rollback after {@link ManagedFunction}.
	 */
	private boolean isRollback = false;

	/**
	 * Ensure able to commit transaction.
	 */
	public void testCommitTransaction() throws Exception {

		// Commit
		this.isCommit = true;

		// Record commit
		this.object.begin();
		this.object.doFunctionalityOne();
		this.object.begin();
		this.object.doFunctionalityTwo();
		this.object.commit();
		this.object.commit();
		this.object.recycled();
		this.object.recycled();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to rollback transaction.
	 */
	public void testRollbackTransaction() throws Exception {

		// Rollback
		this.isRollback = true;

		// Record rolling back the transaction
		this.object.begin();
		this.object.doFunctionalityOne();
		this.object.begin();
		this.object.doFunctionalityTwo();
		this.object.rollback();
		this.object.rollback();
		this.object.recycled();
		this.object.recycled();

		// Test
		this.doTest();
	}

	/**
	 * Ensure able to tidy up transaction.
	 */
	public void testTidyUpTransaction() throws Exception {

		// Ensure transaction governing functionality
		this.object.begin();
		this.object.doFunctionalityOne();
		this.object.begin();
		this.object.doFunctionalityTwo();
		this.object.rollback();
		this.object.rollback();
		this.object.recycled();
		this.object.recycled();

		// Test
		this.doTest();
	}

	/**
	 * Undertake the test.
	 */
	private void doTest() {

		// Test
		this.replayMockObjects();

		// Configure
		String officeName = this.getOfficeName();
		this.constructTeams();

		// Flag for manual governance management
		this.getOfficeBuilder().setManuallyManageGovernance(true);

		// Passive recycling to ensure happens at time of unload
		this.constructTeam("of-MO_ONE.RECYCLE", new PassiveTeam());
		this.constructTeam("of-MO_TWO.RECYCLE", new PassiveTeam());

		// Configure the Managed Objects (one for each work)
		TransactionalManagedObjectSource.object = this.object;
		this.constructManagedObject("MO_ONE", TransactionalManagedObjectSource.class, officeName);
		this.constructManagedObject("MO_TWO", TransactionalManagedObjectSource.class, officeName);

		// Configure the Work
		TransactionalWork work = new TransactionalWork();

		// Configure the first task
		ReflectiveFunctionBuilder taskOne = this.constructFunction(work, "doTaskOne");
		taskOne.getBuilder().setResponsibleTeam(TEAM_TASK);
		DependencyMappingBuilder dependenciesOne = taskOne.buildObject("MO_ONE", ManagedObjectScope.FUNCTION);
//		taskOne.getBuilder().linkPreFunctionAdministration("ADMIN", "BEGIN");
		taskOne.getBuilder().setNextFunction("doTaskTwo", null);

		// Configure the second task
		ReflectiveFunctionBuilder taskTwo = this.constructFunction(work, "doTaskTwo");
		taskTwo.getBuilder().setResponsibleTeam(TEAM_TASK);
		DependencyMappingBuilder dependenciesTwo = taskTwo.buildObject("MO_TWO", ManagedObjectScope.FUNCTION);
//		if (this.isCommit) {
//			taskTwo.getBuilder().linkPostFunctionAdministration("ADMIN", "COMMIT");
//		}
//		if (this.isRollback) {
//			taskTwo.getBuilder().linkPostFunctionAdministration("ADMIN", "ROLLBACK");
//		}

		// Configure the Governance
		GovernanceBuilder<None> governance = this.getOfficeBuilder().addGovernance("GOVERNANCE", MockTransaction.class,
				new MockTransactionalGovernanceFactory());
		governance.setResponsibleTeam(TEAM_GOVERNANCE);
		dependenciesOne.mapGovernance("GOVERNANCE");
		dependenciesTwo.mapGovernance("GOVERNANCE");

		// Configure the Administration
//		AdministrationBuilder<TransactionDutyKey> admin = this.constructAdministrator("ADMIN",
//				MockTransactionalAdministratorSource.class, TEAM_ADMINISTRATION);
//		admin.addDuty("BEGIN").linkGovernance(TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
//		admin.addDuty("COMMIT").linkGovernance(TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");
//		admin.addDuty("ROLLBACK").linkGovernance(TransactionGovernanceKey.TRANSACTION, "GOVERNANCE");

		// Execute the work
		try {
			this.invokeFunction("WORK", null);
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Verify
		this.verifyMockObjects();

		// Ensure the tasks are invoked
		assertTrue("Ensure first task invoked", work.isTaskOneInvoked);
		assertTrue("Ensure second task invoked", work.isTaskTwoInvoked);
	}

	/**
	 * {@link ManagedObjectSource} for the {@link MockTransaction}.
	 */
	public static class TransactionalManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject, ExtensionInterfaceFactory<MockTransaction>, ManagedFunctionFactory<None, None>,
			ManagedFunction<None, None> {

		/**
		 * {@link TransactionalObject}.
		 */
		private volatile static TransactionalObject object;

		/*
		 * ===================== ManagedObjectSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

			// Add recycle function
			context.getManagedObjectSourceContext().getRecycleFunction(this).setResponsibleTeam("RECYCLE");

			// Meta-data
			context.setObjectClass(TransactionalObject.class);
			context.addManagedObjectExtensionInterface(MockTransaction.class, this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ========================= ManagedObject ============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return object;
		}

		/*
		 * ==================== ExtensionInterfaceFactory =====================
		 */

		@Override
		public MockTransaction createExtensionInterface(ManagedObject managedObject) {
			return object;
		}

		/*
		 * ====================== ManagedFunctionFactory =======================
		 */

		@Override
		public ManagedFunction<None, None> createManagedFunction() {
			return this;
		}

		/*
		 * ======================== ManagedFunction ========================
		 */

		@Override
		public Object execute(ManagedFunctionContext<None, None> context) throws Throwable {
			// Flag recycling
			object.recycled();
			return null;
		}
	}

	/**
	 * Transactional functionality.
	 */
	public static class TransactionalWork {

		/**
		 * Indicates if the first {@link ManagedFunction} was invoked.
		 */
		public volatile boolean isTaskOneInvoked = false;

		/**
		 * Indicates if the second {@link ManagedFunction} was invoked.
		 */
		public volatile boolean isTaskTwoInvoked = false;

		/**
		 * {@link ManagedFunction} one.
		 */
		public void doTaskOne(TransactionalObject object) {

			// Do the functionality
			object.doFunctionalityOne();

			// Flag task invoked
			this.isTaskOneInvoked = true;
		}

		/**
		 * {@link ManagedFunction} two.
		 */
		public void doTaskTwo(TransactionalObject object) {

			// Do the functionality
			object.doFunctionalityTwo();

			// Flag task invoked
			this.isTaskTwoInvoked = true;
		}
	}

	/**
	 * Transactional {@link ManagedObject}.
	 */
	public static interface TransactionalObject extends MockTransaction {

		/**
		 * Invoked to undertake some functionality.
		 */
		void doFunctionalityOne();

		/**
		 * Invoked to undertake some functionality.
		 */
		void doFunctionalityTwo();

		/**
		 * Flags that recycled (occurs on unload).
		 */
		void recycled();
	}

}