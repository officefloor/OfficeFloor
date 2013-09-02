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
package net.officefloor.plugin.bayeux.publish;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * {@link Task} to publish a cometd event.
 * 
 * @author Daniel Sagenschneider
 */
public class CometdPublishTask
		extends
		AbstractSingleTask<CometdPublishTask, CometdPublishTask.Dependencies, Indexed> {

	public static enum Dependencies {
		PUBLISH_STATE
	}

	/*
	 * ===================== Task ==============================
	 */

	@Override
	public Object doTask(
			TaskContext<CometdPublishTask, Dependencies, Indexed> context)
			throws Throwable {

		// Obtain access to publish state (which has access to bayeux server)
		CometdPublishState state = (CometdPublishState) context
				.getObject(Dependencies.PUBLISH_STATE);

		// Trigger defined listeners (that may use dependencies)

		// Run dynamically added listeners (no use dependencies)

		// Send the message to the channels

		// Run listeners for the channels (single threaded with no
		// dependencies?)

		// TODO implement Task<CometdPublishTask,None,Indexed>.doTask
		throw new UnsupportedOperationException(
				"TODO implement Task<CometdPublishTask,None,Indexed>.doTask");
	}

}