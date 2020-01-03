package net.officefloor.frame.impl.spi.team.stress;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeamSource;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * Stress tests the {@link OnePersonTeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeamStressTest extends AbstractTeamStressTest {

	@Override
	protected Team getTeamToTest() throws Exception {
		TeamSourceStandAlone standAlone = new TeamSourceStandAlone();
		standAlone.addProperty("name", "TEST");
		standAlone.addProperty("size", "10");
		return standAlone.loadTeam(LeaderFollowerTeamSource.class);
	}

}