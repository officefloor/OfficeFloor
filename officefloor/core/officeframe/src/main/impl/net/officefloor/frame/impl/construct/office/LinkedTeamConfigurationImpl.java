package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;

/**
 * {@link LinkedTeamConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkedTeamConfigurationImpl implements LinkedTeamConfiguration {

	/**
	 * {@link Team} name within the {@link Office}.
	 */
	private final String officeTeamName;

	/**
	 * {@link Team} name within the {@link OfficeFloor}.
	 */
	private final String officeFloorTeamName;

	/**
	 * Initiate.
	 * 
	 * @param officeTeamName
	 *            {@link Team} name within the {@link Office}.
	 * @param officeFloorTeamName
	 *            {@link Team} name within the {@link OfficeFloor}.
	 */
	public LinkedTeamConfigurationImpl(String officeTeamName,
			String officeFloorTeamName) {
		this.officeTeamName = officeTeamName;
		this.officeFloorTeamName = officeFloorTeamName;
	}

	/*
	 * ==================== LinkedTeamConfiguration ==========================
	 */

	@Override
	public String getOfficeTeamName() {
		return this.officeTeamName;
	}

	@Override
	public String getOfficeFloorTeamName() {
		return this.officeFloorTeamName;
	}

}