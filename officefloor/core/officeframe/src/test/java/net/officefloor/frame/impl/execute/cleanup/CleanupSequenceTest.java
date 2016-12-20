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
package net.officefloor.frame.impl.execute.cleanup;

import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.impl.execute.managedobject.CleanupSequenceImpl;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ManagedObjectCleanup}.
 *
 * @author Daniel Sagenschneider
 */
public class CleanupSequenceTest extends OfficeFrameTestCase {

	/**
	 * {@link ManagedObjectCleanup} to test.
	 */
	private final CleanupSequenceImpl cleanupSequence = new CleanupSequenceImpl();

	/**
	 * Ensure can register a cleanup {@link FunctionState}.
	 */
	public void testRegisterCleanupJob() {

		// Record activating the clean up job immediately
		final CleanupStruct cleanup = new CleanupStruct();
		cleanup.recordActivateJobNode();

		// Register job
		this.replayMockObjects();
		this.cleanupSequence.registerCleanUpJob(cleanup.jobNode, cleanup.team);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can register a cleanup {@link FunctionState}.
	 */
	public void testCompleteCleanupJob() {

		// Record activating the clean up job immediately
		final CleanupStruct cleanup = new CleanupStruct();
		cleanup.recordActivateJobNode();

		// Register job and flag that it completed
		this.replayMockObjects();
		this.cleanupSequence.registerCleanUpJob(cleanup.jobNode, cleanup.team);
		this.cleanupSequence.processComplete(cleanup.team);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can register multiple cleanup {@link FunctionState} instances. Also
	 * ensures each is activated on the completion of the previous
	 * {@link FunctionState}.
	 */
	public void testRegisterMultipleCleanupJobs() {

		// Record registering multiple cleanup jobs
		final CleanupStruct cleanupOne = new CleanupStruct();
		final CleanupStruct cleanupTwo = new CleanupStruct();
		final CleanupStruct cleanupThree = new CleanupStruct();

		// Record the first cleanup activated immediately
		cleanupOne.recordActivateJobNode();

		// Next Job is registered in the list

		// Final Job is linked after the second job
		cleanupTwo.jobNode.setNext(cleanupThree.jobNode);

		// Register jobs
		this.replayMockObjects();
		this.cleanupSequence.registerCleanUpJob(cleanupOne.jobNode,
				cleanupOne.team);
		this.cleanupSequence.registerCleanUpJob(cleanupTwo.jobNode,
				cleanupTwo.team);
		this.cleanupSequence.registerCleanUpJob(cleanupThree.jobNode,
				cleanupThree.team);
		this.verifyMockObjects();
	}

	/**
	 * Ensure that on completion of the first cleanup {@link FunctionState}, that the
	 * next clean up {@link FunctionState} is activated.
	 */
	public void testActivateNextCleanupJob() {

		// Record registering multiple cleanup jobs
		final CleanupStruct cleanupOne = new CleanupStruct();
		final CleanupStruct cleanupTwo = new CleanupStruct();
		final CleanupStruct cleanupThree = new CleanupStruct();
		final TeamIdentifier completeTeam = this
				.createMock(TeamIdentifier.class);

		// Record the first cleanup activated immediately
		cleanupOne.recordActivateJobNode();

		// Next Job is registered in the list

		// Final Job is linked after the second job
		cleanupTwo.jobNode.setNext(cleanupThree.jobNode);

		// Record removing second job from queue and activating
		this.recordReturn(cleanupTwo.jobNode, cleanupTwo.jobNode.getNext(),
				cleanupThree.jobNode);
		cleanupTwo.jobNode.setNext(null);
		cleanupTwo.recordActivateJobNode();

		// Record activating last job
		this.recordReturn(cleanupThree.jobNode, cleanupThree.jobNode.getNext(),
				null);
		cleanupThree.jobNode.setNext(null);
		cleanupThree.recordActivateJobNode();

		// Replay
		this.replayMockObjects();

		// Register jobs
		this.cleanupSequence.registerCleanUpJob(cleanupOne.jobNode,
				cleanupOne.team);
		this.cleanupSequence.registerCleanUpJob(cleanupTwo.jobNode,
				cleanupTwo.team);
		this.cleanupSequence.registerCleanUpJob(cleanupThree.jobNode,
				cleanupThree.team);

		// Complete the first job
		this.cleanupSequence.processComplete(cleanupTwo.team);

		// Complete the second job
		this.cleanupSequence.processComplete(cleanupThree.team);

		// Cleanup the last job
		this.cleanupSequence.processComplete(completeTeam);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain {@link CleanupEscalation} instances.
	 */
	public void testRegisterCleanupEscalation() {

		// Ensure initially no cleanup escalations
		CleanupEscalation[] escalations = this.cleanupSequence
				.getCleanupEscalations();
		assertEquals("Should be no cleanup escalations", 0, escalations.length);

		// Register cleanup escalation
		final Throwable testFailure = new Throwable("TEST");
		this.cleanupSequence.registerCleanupEscalation(String.class,
				testFailure);
		escalations = this.cleanupSequence.getCleanupEscalations();
		assertEquals("Should have a cleanup escalation", 1, escalations.length);
		assertCleanupEscalation(escalations[0], String.class, testFailure);

		// Register another cleanup escalation
		final Exception anotherFailure = new SQLException("TEST");
		this.cleanupSequence.registerCleanupEscalation(Connection.class,
				anotherFailure);
		escalations = this.cleanupSequence.getCleanupEscalations();
		assertEquals("Should have cleanup escalations", 2, escalations.length);
		assertCleanupEscalation(escalations[0], String.class, testFailure);
		assertCleanupEscalation(escalations[1], Connection.class,
				anotherFailure);
	}

	/**
	 * Ensures the {@link CleanupEscalation} has correct information.
	 * 
	 * @param escalation
	 *            {@link CleanupEscalation} to validate.
	 * @param expectedObjectType
	 *            Expected object type.
	 * @param expectedEscalation
	 *            Expected {@link Escalation}.
	 */
	private static void assertCleanupEscalation(CleanupEscalation escalation,
			Class<?> expectedObjectType, Throwable expectedEscalation) {
		assertEquals("Incorrect object type", expectedObjectType,
				escalation.getObjectType());
		assertEquals("Incorrect escalation", expectedEscalation,
				escalation.getEscalation());
	}

	/**
	 * Helper class for testing.
	 */
	private class CleanupStruct {

		/**
		 * Mock {@link FunctionState}.
		 */
		public final FunctionState jobNode = CleanupSequenceTest.this
				.createMock(FunctionState.class);

		/**
		 * Mock {@link Flow}.
		 */
		public final Flow jobSequence = CleanupSequenceTest.this
				.createMock(Flow.class);

		/**
		 * Mock {@link ThreadState}.
		 */
		public final ThreadState thread = CleanupSequenceTest.this
				.createMock(ThreadState.class);

		/**
		 * Mock {@link ProcessState}.
		 */
		public final ProcessState process = CleanupSequenceTest.this
				.createMock(ProcessState.class);

		/**
		 * Mock {@link TeamIdentifier}.
		 */
		public final TeamIdentifier team = CleanupSequenceTest.this
				.createMock(TeamIdentifier.class);

		/**
		 * Records activating the cleanup {@link FunctionState}.
		 */
		public void recordActivateJobNode() {
			CleanupSequenceTest.this.recordReturn(this.jobNode,
					this.jobNode.getJobSequence(), this.jobSequence);
			CleanupSequenceTest.this.recordReturn(this.jobSequence,
					this.jobSequence.getThreadState(), this.thread);
			CleanupSequenceTest.this.recordReturn(this.thread,
					this.thread.getProcessState(), this.process);
			this.process
					.registerProcessCompletionListener(CleanupSequenceTest.this.cleanupSequence);
			this.jobNode.activateJob(this.team);
		}
	}

}