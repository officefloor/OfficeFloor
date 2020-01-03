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