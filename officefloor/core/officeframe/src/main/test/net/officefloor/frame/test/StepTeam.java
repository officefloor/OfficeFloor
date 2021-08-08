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

package net.officefloor.frame.test;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * {@link Team} that executes the {@link Job} by the {@link TestCase}. This
 * effectively enables stepping through the logic to test.
 *
 * @author Daniel Sagenschneider
 */
@TestSource
public class StepTeam extends AbstractTeamSource implements Team {

	/**
	 * {@link Job} just assigned.
	 */
	private Job job = null;

	/**
	 * Executes the current {@link Job}.
	 */
	public void executeJob() {
		Assert.assertNotNull("No job to execute", this.job);
		Job job = this.job;
		this.job = null; // allow another job to be assigned
		job.run();
	}

	/*
	 * ====================== Team ========================
	 */

	@Override
	public void startWorking() {
		// Nothing to start
	}

	@Override
	public void assignJob(Job job) {
		Assert.assertNull("Job already assigned", this.job);
		this.job = job;
	}

	@Override
	public void stopWorking() {
		// Nothing to stop
		Assert.assertNull("Should be no assigned job", this.job);
	}

	/*
	 * ==================== TeamSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {
		return this;
	}

}
