package net.officefloor.frame.impl.spi.team.stress;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;

/**
 * Stress tests the {@link ExecutorCachedTeamSource}.
 *
 * @author Daniel Sagenschneider
 */
public class ExecutorCachedTeamStressTest extends AbstractTeamStressTest {

	@Override
	protected Team getTeamToTest() throws Exception {
		return new ExecutorCachedTeamSource().createTeam(0);
	}

}
