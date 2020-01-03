package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OnePersonTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeamTest extends OfficeFrameTestCase {

	/**
	 * Ensures runs the tasks.
	 */
	public void testRunning() throws Exception {

		// Create the team
		OnePersonTeam team = OnePersonTeamSource.createOnePersonTeam("TEAM");

		// Start processing
		team.startWorking();

		// Assign task and wait on it to be started for execution
		MockJob task = new MockJob();
		task.assignJobToTeam(team, 10);

		// Stop processing (should block until person stops working)
		team.stopWorking();

		// Ensure person stopped working
		assertNull("Person should be stopped working", team.getThreadName());

		// Should have execute the task at least once
		assertTrue("Should have execute the task at least once", task.doTaskInvocationCount >= 1);
	}

}