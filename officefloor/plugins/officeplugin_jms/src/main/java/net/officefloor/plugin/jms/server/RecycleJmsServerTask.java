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
package net.officefloor.plugin.jms.server;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * Recycles the {@link JmsServerManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class RecycleJmsServerTask extends
		AbstractSingleTask<RecycleJmsServerTask, Indexed, None> {

	/**
	 * {@link JmsServerManagedObjectSource}.
	 */
	private final JmsServerManagedObjectSource source;

	/**
	 * Initiate.
	 * 
	 * @param source
	 *            {@link JmsServerManagedObjectSource}.
	 */
	public RecycleJmsServerTask(JmsServerManagedObjectSource source) {
		this.source = source;
	}

	/*
	 * ===================== Task ===========================================
	 */

	@Override
	public Object execute(
			ManagedFunctionContext<RecycleJmsServerTask, Indexed, None> context)
			throws Exception {

		// Obtain the recycle parameter
		RecycleManagedObjectParameter<JmsServerManagedObject> recycleParameter = this
				.getRecycleManagedObjectParameter(context,
						JmsServerManagedObject.class);

		// Obtain the JMS Server Managed Object
		JmsServerManagedObject mo = recycleParameter.getManagedObject();

		// Reset the session
		mo.reset();

		// Return to the source
		// Note: not returned to pool as initiated by source
		this.source.returnJmsServerManagedObject(mo);

		// No further tasks
		return null;
	}

}