package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * Tests the {@link PassiveTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class PassiveTeamTest extends OfficeFrameTestCase {

	/**
	 * Ensures that passively executes the {@link Job}.
	 */
	public void testPassiveExecute() throws Exception {

		// Create the team
		Team team = new TeamSourceStandAlone().loadTeam(PassiveTeamSource.class);

		// Create the mock task (completes immediately)
		MockJob task = new MockJob();

		// Run team and execute a task
		team.startWorking();
		team.assignJob(task);
		team.stopWorking();

		// Ensure the task executed
		assertEquals("Task should be executed once", 1, task.doTaskInvocationCount);
	}

}