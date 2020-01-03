package net.officefloor.plugin.team;

import net.officefloor.compile.TeamSourceService;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;

/**
 * {@link TeamSourceService} for a {@link PassiveTeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PassiveTeamSourceService implements
		TeamSourceService<PassiveTeamSource> {

	/*
	 * ======================= TeamSourceService ===========================
	 */

	@Override
	public String getTeamSourceAlias() {
		return "PASSIVE";
	}

	@Override
	public Class<PassiveTeamSource> getTeamSourceClass() {
		return PassiveTeamSource.class;
	}

}