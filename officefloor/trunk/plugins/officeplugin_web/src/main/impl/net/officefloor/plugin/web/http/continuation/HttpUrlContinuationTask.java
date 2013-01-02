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
package net.officefloor.plugin.web.http.continuation;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * HTTP URL continuation {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationTask extends
		AbstractSingleTask<HttpUrlContinuationTask, None, None> {

	/*
	 * ==================== Task ========================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpUrlContinuationTask, None, None> context)
			throws Throwable {
		// Does nothing as next task will service URL continuation
		return null;
	}

}