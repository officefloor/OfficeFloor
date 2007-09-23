/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.jms.server;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * Recycles the {@link net.officefloor.plugin.jms.server.JmsServerManagedObject}.
 * 
 * @author Daniel
 */
class RecycleJmsServerTask
		extends
		AbstractSingleTask<RecycleManagedObjectParameter<JmsServerManagedObject>, RecycleJmsServerTask, Indexed, Indexed> {

	/**
	 * {@link JmsServerManagedObjectSource}.
	 */
	protected final JmsServerManagedObjectSource source;

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
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
	 */
	public Object doTask(
			TaskContext<RecycleManagedObjectParameter<JmsServerManagedObject>, RecycleJmsServerTask, Indexed, Indexed> context)
			throws Exception {

		// Obtain the recycle parameter
		RecycleManagedObjectParameter<JmsServerManagedObject> recycleParameter = context
				.getParameter();

		// Obtain the Jms Server Managed Object
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
