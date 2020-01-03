package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;

/**
 * Configuration linking a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkedTeamConfiguration {

	/**
	 * Obtains the name of the {@link Team} on the {@link OfficeFloor}.
	 * 
	 * @return Name of the {@link Team} on the {@link OfficeFloor}.
	 */
	String getOfficeFloorTeamName();

	/**
	 * Obtains the name that the {@link Team} is registered within the
	 * {@link Office}.
	 * 
	 * @return Name that the {@link Team} is registered within the
	 *         {@link Office}.
	 */
	String getOfficeTeamName();

}
