package net.officefloor.frame.stress;

import static org.junit.jupiter.api.Assertions.fail;

import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * Request scoped {@link TeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class RequestScopedTeamSource extends AbstractTeamSource {

	/*
	 * ================== TeamSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {
		return fail("Should provide " + TeamOversight.class.getSimpleName());
	}

}
