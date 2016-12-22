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
package net.officefloor.plugin.socket.server.ssl.protocol;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * {@link ManagedFunction} to execute the SSL tasks.
 *
 * @author Daniel Sagenschneider
 */
public class SslTaskWork extends
		AbstractSingleTask<SslTaskWork, SslTaskWork.SslTaskDependencies, None> {

	/**
	 * Key to the SSL task to run.
	 */
	public static enum SslTaskDependencies {
		TASK
	}

	/*
	 * ======================== AbstactSingleTask =====================
	 */

	@Override
	public Object execute(
			ManagedFunctionContext<SslTaskWork, SslTaskDependencies, None> context) {

		// Obtain the task
		Object task = context.getObject(SslTaskDependencies.TASK);

		// Run the task
		Runnable runnable = (Runnable) task;
		runnable.run();

		// Task run (should be only task of process)
		return null;
	}

}