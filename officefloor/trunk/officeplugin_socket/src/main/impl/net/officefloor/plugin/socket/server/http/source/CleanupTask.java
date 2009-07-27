/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;

/**
 * Cleans up the {@link HttpManagedObject} (particular {@link HttpRequest} /
 * {@link HttpResponse} combination).
 *
 * @author Daniel Sagenschneider
 */
public class CleanupTask extends AbstractSingleTask<Work, None, None> {

	@Override
	public Object doTask(TaskContext<Work, None, None> context)
			throws Throwable {

		// Flag to clean up the http managed object
		HttpManagedObject managedObject = this
				.getRecycleManagedObjectParameter(context,
						HttpManagedObject.class).getManagedObject();
		managedObject.cleanup();

		// No further processing
		return null;
	}

}