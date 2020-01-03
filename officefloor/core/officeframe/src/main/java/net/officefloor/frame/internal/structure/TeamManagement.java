package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.team.Team;

/**
 * Provides management of a particular {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamManagement {

	/**
	 * Obtains the identifier for the {@link Team}.
	 * 
	 * @return Identifier for the {@link Team}.
	 */
	Object getIdentifier();

	/**
	 * Obtains the {@link Team} under this management.
	 * 
	 * @return {@link Team} under this management.
	 */
	Team getTeam();

}