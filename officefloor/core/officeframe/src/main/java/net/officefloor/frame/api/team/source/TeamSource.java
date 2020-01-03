package net.officefloor.frame.api.team.source;

import net.officefloor.frame.api.team.Team;

/**
 * Source to obtain {@link Team} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamSource {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	TeamSourceSpecification getSpecification();

	/**
	 * Creates the {@link Team}.
	 * 
	 * @param context
	 *            {@link TeamSourceContext}.
	 * @return {@link Team}.
	 * @throws Exception
	 *             If fails to configure the {@link TeamSource}.
	 */
	Team createTeam(TeamSourceContext context) throws Exception;

}