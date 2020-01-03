package net.officefloor.compile.impl.team;

import net.officefloor.compile.officefloor.OfficeFloorTeamSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.frame.api.team.Team;

/**
 * {@link OfficeFloorTeamSourceType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorTeamSourceTypeImpl implements OfficeFloorTeamSourceType {

	/**
	 * Name of {@link Team}.
	 */
	private final String name;

	/**
	 * Properties for the {@link Team}.
	 */
	private final OfficeFloorTeamSourcePropertyType[] properties;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of {@link Team}.
	 * @param properties
	 *            Properties for the {@link Team}.
	 */
	public OfficeFloorTeamSourceTypeImpl(String name,
			OfficeFloorTeamSourcePropertyType[] properties) {
		this.name = name;
		this.properties = properties;
	}

	/*
	 * ===================== OfficeFloorTeamSourceType ========================
	 */

	@Override
	public String getOfficeFloorTeamSourceName() {
		return this.name;
	}

	@Override
	public OfficeFloorTeamSourcePropertyType[] getOfficeFloorTeamSourcePropertyTypes() {
		return this.properties;
	}

}