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
