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
package net.officefloor.plugin.jdbc;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * Recycles the {@link JdbcManagedObject}.
 * 
 * @author Daniel
 */
public class RecycleJdbcTask extends
		AbstractSingleTask<RecycleJdbcTask, Indexed, Indexed> {

	/*
	 * ===================== Task =============================================
	 */

	@Override
	public Object doTask(TaskContext<RecycleJdbcTask, Indexed, Indexed> context)
			throws Exception {

		// Obtain the recycle parameter
		RecycleManagedObjectParameter<JdbcManagedObject> recycleParameter = this
				.getRecycleManagedObjectParameter(context,
						JdbcManagedObject.class);

		// Obtain the JDBC managed object
		JdbcManagedObject mo = recycleParameter.getManagedObject();

		// Recycle the JDBC managed object
		mo.recycle();

		// Recycled, may reuse
		recycleParameter.reuseManagedObject(mo);

		// No further tasks
		return null;
	}

}