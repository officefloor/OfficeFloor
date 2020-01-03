package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;

/**
 * Meta-data for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorMetaData {

	/**
	 * Obtains the {@link OfficeMetaData} instances of the {@link Office} instances
	 * contained within the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeMetaData} instances.
	 */
	OfficeMetaData[] getOfficeMetaData();

	/**
	 * Obtains the {@link ManagedObjectSourceInstance} instances contained within
	 * the {@link OfficeFloor}.
	 * 
	 * @return {@link ManagedObjectSourceInstance} instances.
	 */
	ManagedObjectSourceInstance<?>[] getManagedObjectSourceInstances();

	/**
	 * Obtains the {@link TeamManagement} to break thread stack chain of execution.
	 * 
	 * @return {@link TeamManagement} to break thread stack chain of execution.
	 */
	TeamManagement getBreakChainTeam();

	/**
	 * Obtains the {@link TeamManagement} over the {@link Team} instances of the
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link TeamManagement} over the {@link Team} instances of the
	 *         {@link OfficeFloor}.
	 */
	TeamManagement[] getTeams();

	/**
	 * Obtains the maximum amount of time in milliseconds for {@link OfficeFloor} to
	 * start.
	 * 
	 * @return Maximum amount of time in milliseconds for {@link OfficeFloor} to
	 *         start.
	 */
	long getMaxStartupWaitTime();

}