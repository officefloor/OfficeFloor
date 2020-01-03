package net.officefloor.frame.api.executive;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Oversight for a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamOversight {

	/**
	 * Obtains the name of the {@link TeamOversight}.
	 * 
	 * @return Name of the {@link TeamOversight}.
	 */
	String getTeamOversightName();

	/**
	 * <p>
	 * Creates the {@link Team}.
	 * <p>
	 * This is expected to delegate to the {@link TeamSource} to create the
	 * {@link Team}. However, the {@link Executive} may decide to wrap the
	 * {@link Team} or provide multiple {@link Team} instances with assigning
	 * algorithm (such as taking advantage of {@link Thread} affinity). The choice
	 * is, however, ultimately left to the {@link Executive} to manage the
	 * {@link Team} instances.
	 *
	 * @param context {@link ExecutiveContext}.
	 * @return {@link Team}.
	 * @throws Exception If fails to configure the {@link TeamSource}.
	 */
	default Team createTeam(ExecutiveContext context) throws Exception {
		return context.getTeamSource().createTeam(context);
	}

}