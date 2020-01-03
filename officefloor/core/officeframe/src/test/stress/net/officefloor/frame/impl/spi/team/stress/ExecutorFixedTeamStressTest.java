package net.officefloor.frame.impl.spi.team.stress;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;

/**
 * Stress tests the {@link ExecutorCachedTeamSource}.
 *
 * @author Daniel Sagenschneider
 */
public class ExecutorFixedTeamStressTest extends AbstractTeamStressTest {

	@Override
	protected Team getTeamToTest() throws Exception {
		return new ExecutorFixedTeamSource().createTeam(5);
	}

}
