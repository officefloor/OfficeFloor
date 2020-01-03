package net.officefloor.compile.impl.executive;

import net.officefloor.compile.executive.TeamOversightType;
import net.officefloor.frame.api.executive.TeamOversight;

/**
 * {@link TeamOversightType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamOversightTypeImpl implements TeamOversightType {

	/**
	 * {@link TeamOversight} name.
	 */
	private final String teamOversightName;

	/**
	 * Instantiate.
	 * 
	 * @param teamOversightName {@link TeamOversight} name.
	 */
	public TeamOversightTypeImpl(String teamOversightName) {
		this.teamOversightName = teamOversightName;
	}

	/*
	 * =============== TeamOversightType ===================
	 */

	@Override
	public String getTeamOversightName() {
		return this.teamOversightName;
	}

}