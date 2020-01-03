package net.officefloor.frame.api.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;

/**
 * Context for the {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveContext extends TeamSourceContext {

	/**
	 * Obtains the {@link TeamSource} to create the {@link Team}.
	 * 
	 * @return {@link TeamSource} to create the {@link Team}.
	 */
	TeamSource getTeamSource();

	/**
	 * <p>
	 * Creates a {@link ThreadFactory} for the {@link Team} name.
	 * <p>
	 * The {@link Executive} may decide to create multiple {@link Team} instances
	 * for the actual {@link Team}. This allows identifying which {@link Thread}
	 * will belong to each {@link Team}.
	 * 
	 * @param teamName Name of the {@link Team}.
	 * @return {@link ThreadFactory}.
	 */
	ThreadFactory createThreadFactory(String teamName);

}