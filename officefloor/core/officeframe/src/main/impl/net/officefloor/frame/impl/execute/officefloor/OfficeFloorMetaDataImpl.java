package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeFloorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * {@link OfficeFloorMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMetaDataImpl implements OfficeFloorMetaData {

	/**
	 * Break chain {@link TeamManagement}.
	 */
	private final TeamManagement breakChainTeam;

	/**
	 * Listing of {@link TeamManagement} instances.
	 */
	private final TeamManagement[] teams;

	/**
	 * Listing of {@link ManagedObjectSourceInstance} instances.
	 */
	private final ManagedObjectSourceInstance<?>[] managedObjectSourceInstances;

	/**
	 * {@link OfficeMetaData} for the {@link Office} instances within the
	 * {@link OfficeFloor}.
	 */
	private final OfficeMetaData[] officeMetaData;

	/**
	 * Maximum time in milliseconds to wait for {@link OfficeFloor} to start.
	 */
	private final long maxStartupWaitTime;

	/**
	 * Initiate.
	 * 
	 * @param teams                        Listing of {@link TeamManagement}
	 *                                     instances.
	 * @param managedObjectSourceInstances Listing of
	 *                                     {@link ManagedObjectSourceInstance}
	 *                                     instances.
	 * @param officeMetaData               {@link OfficeMetaData} for the
	 *                                     {@link Office} instances within the
	 *                                     {@link OfficeFloor}.
	 * @param maxStartupWaitTime           Maximum time in milliseconds to wait for
	 *                                     {@link OfficeFloor} to start.
	 */
	public OfficeFloorMetaDataImpl(TeamManagement breakChainTeam, TeamManagement[] teams,
			ManagedObjectSourceInstance<?>[] managedObjectSourceInstances, OfficeMetaData[] officeMetaData,
			long maxStartupWaitTime) {
		this.breakChainTeam = breakChainTeam;
		this.teams = teams;
		this.managedObjectSourceInstances = managedObjectSourceInstances;
		this.officeMetaData = officeMetaData;
		this.maxStartupWaitTime = maxStartupWaitTime;
	}

	/*
	 * ================== OfficeFloorMetaData ==========================
	 */

	@Override
	public TeamManagement getBreakChainTeam() {
		return this.breakChainTeam;
	}

	@Override
	public TeamManagement[] getTeams() {
		return this.teams;
	}

	@Override
	public OfficeMetaData[] getOfficeMetaData() {
		return this.officeMetaData;
	}

	@Override
	public ManagedObjectSourceInstance<?>[] getManagedObjectSourceInstances() {
		return this.managedObjectSourceInstances;
	}

	@Override
	public long getMaxStartupWaitTime() {
		return this.maxStartupWaitTime;
	}

}
