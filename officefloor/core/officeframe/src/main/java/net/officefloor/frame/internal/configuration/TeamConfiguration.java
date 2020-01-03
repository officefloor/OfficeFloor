package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Configuration of a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamConfiguration<TS extends TeamSource> {

	/**
	 * Obtains the name of the {@link Team}.
	 * 
	 * @return Name of the {@link Team}.
	 */
	String getTeamName();

	/**
	 * Obtains the size of the {@link Team}.
	 * 
	 * @return {@link Team} size.
	 */
	int getTeamSize();

	/**
	 * Obtains the name of the {@link TeamOversight}.
	 * 
	 * @return Name of the {@link TeamOversight}.
	 */
	String getTeamOversightName();

	/**
	 * Obtains the {@link TeamSource} instance to use.
	 * 
	 * @return {@link TeamSource} instance to use. This may be <code>null</code> and
	 *         therefore the {@link #getTeamSourceClass()} should be used to obtain
	 *         the {@link TeamSource}.
	 */
	TS getTeamSource();

	/**
	 * Obtains the {@link Class} of the {@link TeamSource}.
	 * 
	 * @return {@link Class} of the {@link TeamSource}.
	 */
	Class<TS> getTeamSourceClass();

	/**
	 * Obtains the {@link SourceProperties} for initialising the {@link TeamSource}.
	 * 
	 * @return {@link SourceProperties} for initialising the {@link TeamSource}.
	 */
	SourceProperties getProperties();

}
