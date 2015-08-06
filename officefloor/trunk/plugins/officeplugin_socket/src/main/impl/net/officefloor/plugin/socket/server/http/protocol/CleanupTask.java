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
package net.officefloor.plugin.socket.server.http.protocol;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
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

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(CleanupTask.class
			.getName());

	/*
	 * ======================= Task ===============================
	 */

	@Override
	public Object doTask(TaskContext<Work, None, None> context)
			throws IOException {

		// Obtain the recycle parameter
		RecycleManagedObjectParameter<HttpManagedObject> parameter = this
				.getRecycleManagedObjectParameter(context,
						HttpManagedObject.class);

		// Obtain the HTTP managed object
		HttpManagedObject managedObject = parameter.getManagedObject();

		// Obtain any potential cleanup escalations
		CleanupEscalation[] escalations = parameter.getCleanupEscalations();

		try {
			
			// TODO remove
			System.out.println("Cleanup connection: " + escalations.length + " escalations");

			// Clean up the HTTP managed object
			managedObject.cleanup(escalations);

		} catch (ClosedChannelException ex) {
			// Connection closed. Must handle as recycle task in new process
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, "Failed cleaning up "
						+ HttpManagedObject.class.getSimpleName()
						+ " as connection closed", ex);
			}
		}

		// No further processing
		return null;
	}
}