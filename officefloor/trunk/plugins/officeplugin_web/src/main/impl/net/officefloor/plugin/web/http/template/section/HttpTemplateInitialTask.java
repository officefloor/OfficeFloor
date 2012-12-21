/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.template.section;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Initial {@link Task} to ensure appropriate conditions for rendering the
 * {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInitialTask
		extends
		AbstractSingleTask<HttpTemplateInitialTask, HttpTemplateInitialTask.Dependencies, None> {

	/**
	 * Keys for the {@link HttpTemplateInitialTask} dependencies.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, HTTP_APPLICATION_LOCATION, HTTP_SESSION
	}

	/*
	 * ======================= Task ===============================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpTemplateInitialTask, Dependencies, None> context)
			throws Throwable {

		// TODO implement
		// Task<HttpTemplateWork,HttpTemplateInitialTaskDependencies,None>.doTask
		throw new UnsupportedOperationException(
				"TODO implement Task<HttpTemplateWork,HttpTemplateInitialTaskDependencies,None>.doTask");
	}

}