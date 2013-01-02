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
package net.officefloor.frame.integrate.governance;

import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Typical use of {@link Governance} is for transaction management. This test to
 * provide transaction management example to ensure container management of
 * {@link Governance} handles transaction.
 * 
 * @author Daniel Sagenschneider
 */
public class TransactionGovernanceContextTest extends
		AbstractGovernanceTestCase {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(TransactionGovernanceContextTest.class);
	}

	/**
	 * Name of the {@link Work} to invoke for testing.
	 */
	private static final String WORK_NAME = "WORK";

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private static final String MANAGED_OBJECT_NAME = "MO";

	/**
	 * Name of {@link Governance}.
	 */
	private static final String GOVERNANCE_NAME = "GOVERNANCE";

	/**
	 * {@link TransactionalWork}.
	 */
	private final TransactionalWork work = new TransactionalWork();

	/**
	 * {@link TransactionalObject}.
	 */
	private final TransactionalObject object = this
			.createSynchronizedMock(TransactionalObject.class);

	/**
	 * {@link Task} one.
	 */
	private ReflectiveTaskBuilder taskOne;

	/**
	 * {@link Task} two.
	 */
	private ReflectiveTaskBuilder taskTwo;

	/**
	 * Flags for {@link Task} one to be constructed under {@link Governance}.
	 */
	private boolean isTaskOneGoverned = true;

	/**
	 * {@link FlowInstigationStrategyEnum} from {@link Task} one.
	 */
	private FlowInstigationStrategyEnum instigationStrategy = FlowInstigationStrategyEnum.SEQUENTIAL;

	/**
	 * Ensure commit transaction on completion.
	 */
	public void testCommitOnCompletion() throws Throwable {

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.object.commit();

		// Configure tasks
		this.constructRawTasks();

		// Test
		this.doTest(false);
	}

	/**
	 * Ensure commit transaction on next {@link Task} not requiring
	 * {@link Governance}.
	 */
	public void testCommitOnNextTaskDeactivation() throws Throwable {

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.object.commit();
		this.object.stepTwo();

		// Configure tasks
		this.constructRawTasks();
		this.taskOne.setNextTaskInFlow("taskTwo");

		// Test
		this.doTest(false);
	}

	/**
	 * Ensure {@link Governance} is continued to be active.
	 */
	public void testStayActiveOnNextTask() throws Throwable {

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.object.stepTwo();
		this.object.commit();

		// Configure tasks
		this.constructRawTasks();
		this.taskOne.setNextTaskInFlow("taskTwo");
		this.taskTwo.getBuilder().addGovernance(GOVERNANCE_NAME);

		// Test
		this.doTest(false);
	}

	/**
	 * Ensure commit transaction on flow {@link Task} not requiring
	 * {@link Governance}.
	 */
	public void testCommitOnFlowDeactivation() throws Throwable {

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.object.commit();
		this.object.stepTwo();

		// Configure tasks
		this.constructRawTasks();

		// Test
		this.doTest(true);
	}

	/**
	 * Ensure {@link Governance} is continued to be active.
	 */
	public void testStayActiveOnFlow() throws Throwable {

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.object.stepTwo();
		this.object.commit();

		// Configure tasks
		this.constructRawTasks();
		this.taskTwo.getBuilder().addGovernance(GOVERNANCE_NAME);

		// Test
		this.doTest(true);
	}

	/**
	 * Ensure rollback on {@link Escalation}.
	 */
	public void testRollbackOnEscalation() throws Throwable {

		final SQLException exception = new SQLException("TEST");

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.control(this.object).setThrowable(exception);
		this.object.rollback();

		// Configure tasks
		this.constructRawTasks();

		// Test
		try {
			this.doTest(false);
			fail("Should not be successful");
		} catch (SQLException ex) {
			assertSame("Incorrect exception", exception, ex);
		}
	}

	/**
	 * Ensure {@link Governance} is enforced for parallel flow.
	 */
	public void testParallelFlowTransaction() throws Throwable {

		// Record transaction steps
		this.object.stepOne();
		this.object.begin();
		this.object.stepTwo();
		this.object.commit();
		this.object.stepOne();

		// Configure tasks
		this.isTaskOneGoverned = false;
		this.instigationStrategy = FlowInstigationStrategyEnum.PARALLEL;
		this.work.isTaskOneToComplete = false;
		this.constructRawTasks();
		this.taskTwo.getBuilder().addGovernance(GOVERNANCE_NAME);

		// Test
		this.doTest(true);
	}

	/**
	 * Ensure {@link Governance} is deactivated for parallel flow and then
	 * reactivated for invokee {@link Task}.
	 */
	public void testReverseParallelFlowTransaction() throws Throwable {

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.object.commit();
		this.object.stepTwo();
		this.object.begin();
		this.object.stepOne();
		this.object.commit();

		// Configure tasks
		this.instigationStrategy = FlowInstigationStrategyEnum.PARALLEL;
		this.work.isTaskOneToComplete = false;
		this.constructRawTasks();

		// Test
		this.doTest(true);
	}

	/**
	 * Ensure not reactivate {@link Governance} on parallel flow returning to
	 * invokee {@link Task} which is complete and flowing onto next {@link Task}
	 * that does not have {@link Governance}.
	 */
	public void testNotReactivateOnParallelFlow() throws Throwable {

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.object.commit();
		this.object.stepTwo();
		this.object.stepTwo();

		// Configure tasks
		this.instigationStrategy = FlowInstigationStrategyEnum.PARALLEL;
		this.constructRawTasks();
		this.taskOne.setNextTaskInFlow("taskTwo");

		// Test
		this.doTest(true);
	}

	/**
	 * Ensure rollback on {@link OfficeFloor} escalation.
	 */
	public void testOfficeFloorEscalationRollback() throws Throwable {

		final RuntimeException exception = new RuntimeException("TEST");

		// Record transaction steps
		this.object.begin();
		this.object.stepOne();
		this.control(this.object).setThrowable(exception);
		this.object.rollback();

		// Add office floor escalation handler
		final Throwable[] escalation = new Throwable[1];
		this.getOfficeFloorBuilder().setEscalationHandler(
				new EscalationHandler() {
					@Override
					public void handleEscalation(Throwable cause)
							throws Throwable {
						synchronized (escalation) {
							escalation[0] = cause;
						}
					}
				});

		// Configure tasks
		this.constructRawTasks();

		// Test
		this.doTest(false);

		// Validate escalation handle by office floor
		synchronized (escalation) {
			assertEquals("Incorrect escalation", exception, escalation[0]);
		}
	}

	/**
	 * Undertake the test.
	 * 
	 * @param isInvokeFlow
	 *            Indicates to invoke the flow.
	 */
	private void doTest(boolean isInvokeFlow) throws Throwable {

		// Test
		this.replayMockObjects();

		// Configure
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();
		this.constructTeams();

		// Configure the Managed Object
		this.constructManagedObject(this.object, MANAGED_OBJECT_NAME,
				officeName);
		DependencyMappingBuilder dependencies = officeBuilder
				.addProcessManagedObject(MANAGED_OBJECT_NAME,
						MANAGED_OBJECT_NAME);

		// Configure the Governance
		GovernanceBuilder<None> governance = this.getOfficeBuilder()
				.addGovernance(GOVERNANCE_NAME,
						new MockTransactionalGovernanceFactory(),
						MockTransaction.class);
		governance.setTeam(TEAM_GOVERNANCE);
		dependencies.mapGovernance(GOVERNANCE_NAME);

		// Execute the work
		try {
			this.invokeWork(WORK_NAME, Boolean.valueOf(isInvokeFlow));
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Verify
		this.verifyMockObjects();

		// Throw handled exception
		if (this.work.exception != null) {
			throw this.work.exception;
		}
	}

	/**
	 * Constructs the {@link Task} with basic configuration.
	 */
	protected void constructRawTasks() throws Exception {

		// Construct the work and tasks (with basic content)
		ReflectiveWorkBuilder workBuilder = this.constructWork(this.work,
				WORK_NAME, "taskOne");

		// Construct task one
		this.taskOne = workBuilder.buildTask("taskOne", TEAM_TASK);
		this.taskOne.buildObject(MANAGED_OBJECT_NAME);
		this.taskOne.buildFlow("taskTwo", this.instigationStrategy, null);
		this.taskOne.buildParameter();
		this.taskOne.buildTaskContext();
		if (this.isTaskOneGoverned) {
			this.taskOne.getBuilder().addGovernance(GOVERNANCE_NAME);
		}

		// Construct task two
		this.taskTwo = workBuilder.buildTask("taskTwo", TEAM_TASK);
		this.taskTwo.buildObject(MANAGED_OBJECT_NAME);

		// Construct escalation handling
		workBuilder.buildTask("handleEscalation", TEAM_TASK).buildParameter();
		this.getOfficeBuilder().addEscalation(SQLException.class, WORK_NAME,
				"handleEscalation");
	}

	/**
	 * {@link Work}.
	 */
	public static class TransactionalWork {

		/**
		 * Handled {@link Escalation}.
		 */
		public volatile SQLException exception = null;

		/**
		 * Flag indicating if {@link Task} one is to completing.
		 */
		public boolean isTaskOneToComplete = true;

		/**
		 * Number of times {@link Task} one is invoked.
		 */
		private int taskOneInvokeCount = 0;

		/**
		 * {@link Task} one.
		 */
		public void taskOne(TransactionalObject object, ReflectiveFlow flow,
				Boolean isInvokeFlow, TaskContext<?, ?, ?> taskContext)
				throws Exception {

			// Undertake functionality
			object.stepOne();

			// Flag for task one to complete
			taskContext.setComplete(this.isTaskOneToComplete);
			this.isTaskOneToComplete = true; // complete on next

			// Invoke the flow (only on first invocation)
			if ((this.taskOneInvokeCount == 0) && (isInvokeFlow.booleanValue())) {
				flow.doFlow(null);
			}

			// Increment invoke count
			this.taskOneInvokeCount++;
		}

		/**
		 * {@link Task} two.
		 */
		public void taskTwo(TransactionalObject object) {
			object.stepTwo();
		}

		/**
		 * {@link EscalationFlow}.
		 */
		public void handleEscalation(SQLException ex) {
			this.exception = ex;
		}
	}

	/**
	 * Transactional {@link ManagedObject}.
	 */
	public static interface TransactionalObject extends MockTransaction {

		/**
		 * Invoked identifying {@link Task} one.
		 */
		void stepOne() throws Exception;

		/**
		 * Invoked identifying {@link Task} two.
		 */
		void stepTwo();
	}

}