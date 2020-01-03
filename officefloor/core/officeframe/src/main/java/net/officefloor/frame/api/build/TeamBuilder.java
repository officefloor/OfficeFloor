package net.officefloor.frame.api.build;

import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Builder of a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamBuilder<TS extends TeamSource> {

	/**
	 * Specifies the {@link Team} size (typically being the maximum number of
	 * {@link Thread} instances within the {@link Team}).
	 * 
	 * @param teamSize {@link Team} size.
	 */
	void setTeamSize(int teamSize);

	/**
	 * Specifies the {@link TeamOversight} for the {@link Team}.
	 * 
	 * @param teamOversightName Name of the {@link TeamOversight}.
	 */
	void setTeamOversight(String teamOversightName);

	/**
	 * Specifies a property for the {@link TeamSource}.
	 * 
	 * @param name  Name of property.
	 * @param value Value of property.
	 */
	void addProperty(String name, String value);

}
