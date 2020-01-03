package net.officefloor.frame.impl.construct.team;

import junit.framework.TestCase;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;

/**
 * Adapter providing empty {@link TeamSource} methods.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class TeamSourceAdapter implements TeamSource, Team {

	/*
	 * ==================== TeamSource ==================================
	 */

	@Override
	public TeamSourceSpecification getSpecification() {
		return null;
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {
		return this;
	}

	/*
	 * ==================== TeamSource ==================================
	 */

	@Override
	public void startWorking() {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public void assignJob(Job job) {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public void stopWorking() {
		TestCase.fail("Should not be invoked");
	}

}