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
package net.officefloor.server.http.conversation;

import java.io.IOException;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * HTTP {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpManagedObject extends ManagedObject {

	/**
	 * Obtains the {@link ServerHttpConnection}.
	 *
	 * @return {@link ServerHttpConnection}.
	 */
	ServerHttpConnection getServerHttpConnection();

	/**
	 * Obtains the {@link FlowCallback} for handling completion of servicing.
	 *
	 * @return {@link FlowCallback} for handling completion of servicing.
	 */
	FlowCallback getFlowCallback();

	/**
	 * Cleans up this {@link HttpManagedObject}.
	 *
	 * @param cleanupEscalations
	 *            {@link CleanupEscalation} instances of previous cleanup of
	 *            {@link ManagedObject} instances.
	 * @throws IOException
	 *             If fails to clean up {@link HttpManagedObject}.
	 */
	void cleanup(CleanupEscalation[] cleanupEscalations) throws IOException;

}