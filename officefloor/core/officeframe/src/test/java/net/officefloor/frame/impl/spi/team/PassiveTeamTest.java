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
