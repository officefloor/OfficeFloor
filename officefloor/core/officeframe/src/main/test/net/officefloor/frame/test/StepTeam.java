/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
