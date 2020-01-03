package net.officefloor.plugin.team;

import net.officefloor.compile.TeamSourceService;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;

/**
 * {@link TeamSourceService} for a {@link OnePersonTeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeamSourceService implements
		TeamSourceService<OnePersonTeamSource> {

	/*
	 * ================== TeamSourceService ==================================
	 */

	@Override
	public String getTeamSourceAlias() {
		return "ONE_PERSON";
	}

	@Override
	public Class<OnePersonTeamSource> getTeamSourceClass() {
		return OnePersonTeamSource.class;
	}

}