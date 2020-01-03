package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * Tests the {@link WorkerPerJobTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkerPerTaskTeamTest extends OfficeFrameTestCase {

	/**
	 * Ensures runs the tasks.
	 */
	public void testRunning() throws Exception {

		// Create the worker per job team
		Team workPerTaskTeam = new TeamSourceStandAlone().loadTeam(WorkerPerJobTeamSource.class);

		// Start processing
		workPerTaskTeam.startWorking();

		// Assign task and wait on it to be started for execution
		MockJob task = new MockJob();
		task.assignJobToTeam(workPerTaskTeam, 10);

		// Stop processing
		workPerTaskTeam.stopWorking();
	}

}
