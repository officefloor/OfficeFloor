package net.officefloor.compile.impl.team;

import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.team.Team;

/**
 * {@link TeamType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamTypeImpl implements TeamType {

	/**
	 * Indicates if require {@link Team} size.
	 */
	private final boolean isRequireTeamSize;

	/**
	 * Instantiate.
	 * 
	 * @param isRequireTeamSize Indicates if require {@link Team} size.
	 */
	public TeamTypeImpl(boolean isRequireTeamSize) {
		this.isRequireTeamSize = isRequireTeamSize;
	}

	/*
	 * ============== TeamType ====================
	 */

	@Override
	public boolean isRequireTeamSize() {
		return this.isRequireTeamSize;
	}

}