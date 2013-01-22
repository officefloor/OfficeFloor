/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.security;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;

/**
 * {@link Task} and {@link TaskFactory} to challenge the client.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpChallengeTask implements
		Task<HttpSecurityWork, Indexed, Indexed>,
		TaskFactory<HttpSecurityWork, Indexed, Indexed> {

	/*
	 * =================== HttpChallengeTask ======================
	 */

	@Override
	public Task<HttpSecurityWork, Indexed, Indexed> createTask(
			HttpSecurityWork work) {
		// TODO implement
		// TaskFactory<HttpSecurityWork,Indexed,Indexed>.createTask
		throw new UnsupportedOperationException(
				"TODO implement TaskFactory<HttpSecurityWork,Indexed,Indexed>.createTask");
	}

	@Override
	public Object doTask(TaskContext<HttpSecurityWork, Indexed, Indexed> context)
			throws Throwable {
		// TODO implement Task<HttpSecurityWork,Indexed,Indexed>.doTask
		throw new UnsupportedOperationException(
				"TODO implement Task<HttpSecurityWork,Indexed,Indexed>.doTask");
	}

}