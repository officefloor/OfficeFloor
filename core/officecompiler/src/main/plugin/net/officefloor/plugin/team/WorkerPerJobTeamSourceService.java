/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.plugin.team;

import net.officefloor.compile.TeamSourceService;
import net.officefloor.compile.TeamSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeamSource;

/**
 * {@link TeamSourceService} for a {@link WorkerPerJobTeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkerPerJobTeamSourceService
		implements TeamSourceService<WorkerPerJobTeamSource>, TeamSourceServiceFactory {

	/*
	 * ====================== TeamSourceService ==================
	 */

	@Override
	public TeamSourceService<?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public String getTeamSourceAlias() {
		return "WORKER_PER_JOB";
	}

	@Override
	public Class<WorkerPerJobTeamSource> getTeamSourceClass() {
		return WorkerPerJobTeamSource.class;
	}

}
