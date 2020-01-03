package net.officefloor.frame.impl.spi.team.stress;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * Stress tests the {@link OnePersonTeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeamStressTest extends AbstractTeamStressTest {

	@Override
	protected Team getTeamToTest() throws Exception {
		return new TeamSourceStandAlone().loadTeam(OnePersonTeamSource.class);
	}

}
