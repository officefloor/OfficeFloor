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
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam.TeamMember;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link LeaderFollowerTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeamTest extends OfficeFrameTestCase {

	/**
	 * {@link LeaderFollowerTeam} to test.
	 */
	protected LeaderFollowerTeam team;

	/**
	 * Single member and single task.
	 */
	public void testSingleMemberOneTask() {
		this.leaderFollowerTest(1, 1);
	}

	/**
	 * Multiple members and single task.
	 */
	public void testMultipleMembersOneTask() {
		this.leaderFollowerTest(3, 1);
	}

	/**
	 * Single member and multiple tasks.
	 */
	public void testSingleMemberMultipleTasks() {
		this.leaderFollowerTest(1, 6);
	}

	/**
	 * Multiple members and multiple tasks.
	 */
	public void testMulitpleMembersMultipleTasks() {
		this.leaderFollowerTest(3, 6);
	}

	/**
	 * High load test.
	 */
	public void testHighLoad() {
		this.leaderFollowerTest(100, 100);
	}

	/**
	 * Runs the test on the {@link LeaderFollowerTeam}.
	 * 
	 * @param teamMemberCount
	 *            Count of workers in the team.
	 */
	private void leaderFollowerTest(int teamMemberCount, int taskCount) {

		// Create the team and start it working
		this.team = new LeaderFollowerTeam("Test",
				MockTeamSource.createTeamIdentifier(), teamMemberCount, 10);
		this.team.startWorking();

		// Wait some time before assigning tasks
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			fail("Failed to wait before assigning tasks");
		}

		// Assign tasks and wait on them to be started for execution
		MockTaskContainer[] tasks = new MockTaskContainer[taskCount];
		for (int i = 0; i < taskCount; i++) {
			tasks[i] = new MockTaskContainer();
			tasks[i].assignJobToTeam(this.team, 10);
		}

		// Obtain the team members
		TeamMember[] teamMembers = new TeamMember[this.team.teamMembers.length];
		for (int i = 0; i < teamMembers.length; i++) {
			teamMembers[i] = this.team.teamMembers[i];
		}

		// Flag the tasks to stop processing
		for (int i = 0; i < taskCount; i++) {
			tasks[i].stopProcessing = true;
		}

		// Stop processing (should have all teams finished)
		this.team.stopWorking();

		// Ensure each team member has stopped working
		for (int i = 0; i < teamMembers.length; i++) {
			assertTrue("Team member " + i + " should be finished working",
					teamMembers[i].finished);
		}

		// Should have invoked each task at least once
		for (int i = 0; i < tasks.length; i++) {
			assertTrue("Should have invoked task " + i + " at least once",
					tasks[i].doTaskInvocationCount >= 1);
		}
	}

}