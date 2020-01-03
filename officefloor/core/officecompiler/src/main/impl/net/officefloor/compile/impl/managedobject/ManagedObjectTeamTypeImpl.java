package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.frame.api.team.Team;

/**
 * {@link ManagedObjectTeamType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectTeamTypeImpl implements ManagedObjectTeamType {

	/**
	 * Name of the {@link Team}.
	 */
	private final String teamName;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 */
	public ManagedObjectTeamTypeImpl(String teamName) {
		this.teamName = teamName;
	}

	/*
	 * ================= ManagedObjectTeamType ===========================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

}