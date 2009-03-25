/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.TeamMember;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link LeaderFollowerTeam}.
 * 
 * @author Daniel
 */
public class LeaderFollowerTeamTest extends OfficeFrameTestCase {

	/**
	 * {@link LeaderFollowerTeam} to test.
	 */
	protected LeaderFollowerTeam team;

	/**
	 * Single member and single task.
	 */
	@StressTest
	public void testSingleMemberOneTask() {
		this.leaderFollowerTest(1, 1);
	}

	/**
	 * Multiple members and single task.
	 */
	@StressTest
	public void testMultipleMembersOneTask() {
		this.leaderFollowerTest(3, 1);
	}

	/**
	 * Single member and multiple tasks.
	 */
	@StressTest
	public void testSingleMemberMultipleTasks() {
		this.leaderFollowerTest(1, 6);
	}

	/**
	 * Multiple members and multiple tasks.
	 */
	@StressTest
	public void testMulitpleMembersMultipleTasks() {
		this.leaderFollowerTest(3, 6);
	}

	/**
	 * Runs the test on the {@link LeaderFollowerTeam}.
	 * 
	 * @param teamMemberCount
	 *            Count of workers in the team.
	 */
	private void leaderFollowerTest(int teamMemberCount, int taskCount) {
		// Create the team
		this.team = new LeaderFollowerTeam("Test", teamMemberCount, 10);

		// Start the team
		this.team.startWorking();

		// Assign tasks and wait on them to be started for execution
		MockTaskContainer[] tasks = new MockTaskContainer[taskCount];
		for (int i = 0; i < taskCount; i++) {
			tasks[i] = new MockTaskContainer();
			tasks[i].assignTaskToTeam(this.team, 10);
		}

		// Obtain the team member
		TeamMember teamMember = this.team.teamMembers[0];

		// Allow some time for processing
		this.sleep(1);

		// Stop processing
		this.team.stopWorking();

		// Ensure person stopped working
		assertTrue("Team member should be stopped working", teamMember.finished);

		// Ensure each task run at least twice
		int totalInvocations = 0;
		StringBuffer message = new StringBuffer();
		for (int i = 0; i < taskCount; i++) {
			assertTrue("Task " + i + " must be run at least twice",
					(tasks[i].doTaskInvocationCount >= 2));

			// Indicate number of invocations
			totalInvocations += tasks[i].doTaskInvocationCount;
			message.append("\n invocations[" + i + "]="
					+ tasks[i].doTaskInvocationCount);
		}
		if (taskCount > 1) {
			message.append("\n total=" + totalInvocations);
		}
		this.printMessage(message.toString());
	}

}