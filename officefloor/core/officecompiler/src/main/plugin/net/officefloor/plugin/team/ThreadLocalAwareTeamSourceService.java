package net.officefloor.plugin.team;

import net.officefloor.compile.TeamSourceService;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;

/**
 * {@link TeamSourceService} for a {@link ThreadLocalAwareTeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalAwareTeamSourceService implements TeamSourceService<ThreadLocalAwareTeamSource> {

	/*
	 * ======================= TeamSourceService ===========================
	 */

	@Override
	public String getTeamSourceAlias() {
		return "CONTEXT";
	}

	@Override
	public Class<ThreadLocalAwareTeamSource> getTeamSourceClass() {
		return ThreadLocalAwareTeamSource.class;
	}

}