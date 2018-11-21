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
package net.officefloor.frame.impl.spi.team;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * Tests the {@link LeaderFollowerTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeamTest extends OfficeFrameTestCase {

	/**
	 * Single member and single task.
	 */
	public void testSingleMemberOneTask() throws Exception {
		this.leaderFollowerTest(1, 1);
	}

	/**
	 * Multiple members and single task.
	 */
	public void testMultipleMembersOneTask() throws Exception {
		this.leaderFollowerTest(3, 1);
	}

	/**
	 * Single member and multiple tasks.
	 */
	public void testSingleMemberMultipleTasks() throws Exception {
		this.leaderFollowerTest(1, 6);
	}

	/**
	 * Multiple members and multiple tasks.
	 */
	public void testMulitpleMembersMultipleTasks() throws Exception {
		this.leaderFollowerTest(3, 6);
	}

	/**
	 * High load test.
	 */
	public void testHighLoad() throws Exception {
		this.leaderFollowerTest(100, 100);
	}

	/**
	 * Runs the test on the {@link LeaderFollowerTeam}.
	 * 
	 * @param teamMemberCount Count of workers in the team.
	 */
	private void leaderFollowerTest(int teamMemberCount, int taskCount) throws Exception {

		final int teamSize = 10;
		final List<Thread> teamThreads = new ArrayList<>();

		// Create the team and start it working
		TeamSourceStandAlone standAlone = new TeamSourceStandAlone();
		standAlone.setThreadDecorator((thread) -> teamThreads.add(thread));
		standAlone.setTeamSize(teamSize);
		Team team = standAlone.loadTeam(LeaderFollowerTeamSource.class);
		team.startWorking();

		// Ensure have appropriate number of threads
		assertEquals("Incorrect number of threads", teamSize, teamThreads.size());

		// Wait some time before assigning tasks
		try {
			Thread.sleep(10);
		} catch (InterruptedException ex) {
			fail("Failed to wait before assigning tasks");
		}

		// Assign tasks and wait on them to be started for execution
		MockJob[] tasks = new MockJob[taskCount];
		for (int i = 0; i < taskCount; i++) {
			tasks[i] = new MockJob();
			tasks[i].assignJobToTeam(team, 10);
		}

		// Stop processing (should have all threads finished)
		team.stopWorking();

		// Ensure all threads are stopped
		this.waitForTrue(() -> {
			for (Thread thread : teamThreads) {
				if (!State.TERMINATED.equals(thread.getState())) {
					return false;
				}
			}
			return true;
		});

		// Should have invoked each task at least once
		for (int i = 0; i < tasks.length; i++) {
			assertTrue("Should have invoked task " + i + " at least once", tasks[i].doTaskInvocationCount >= 1);
		}
	}

}