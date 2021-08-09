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
