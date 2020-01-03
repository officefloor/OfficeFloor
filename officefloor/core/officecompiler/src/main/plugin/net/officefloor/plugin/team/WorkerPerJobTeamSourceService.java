package net.officefloor.plugin.team;

import net.officefloor.compile.TeamSourceService;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeamSource;

/**
 * {@link TeamSourceService} for a {@link WorkerPerJobTeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkerPerJobTeamSourceService implements TeamSourceService<WorkerPerJobTeamSource> {

	/*
	 * ====================== TeamSourceService ==================
	 */

	@Override
	public String getTeamSourceAlias() {
		return "WORKER_PER_JOB";
	}

	@Override
	public Class<WorkerPerJobTeamSource> getTeamSourceClass() {
		return WorkerPerJobTeamSource.class;
	}

}