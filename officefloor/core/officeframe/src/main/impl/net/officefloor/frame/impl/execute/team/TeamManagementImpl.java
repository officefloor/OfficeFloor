package net.officefloor.frame.impl.execute.team;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * {@link TeamManagement} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamManagementImpl implements TeamManagement {

	/**
	 * Identifier for the {@link Team} under this {@link TeamManagement}.
	 */
	private final Object teamIdentifier = new Object();

	/**
	 * {@link Team} under this {@link TeamManagement}.
	 */
	private final Team team;

	/**
	 * Initiate.
	 * 
	 * @param team
	 *            {@link Team} under this {@link TeamManagement}.
	 */
	public TeamManagementImpl(Team team) {
		this.team = team;
	}

	/*
	 * ====================== TeamManagement ================================
	 */

	@Override
	public Object getIdentifier() {
		return this.teamIdentifier;
	}

	@Override
	public Team getTeam() {
		return this.team;
	}

}