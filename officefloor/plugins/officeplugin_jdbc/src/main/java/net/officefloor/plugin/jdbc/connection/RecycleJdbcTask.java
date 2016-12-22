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
package net.officefloor.plugin.jdbc.connection;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * Recycles the {@link JdbcManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class RecycleJdbcTask extends
		AbstractSingleTask<RecycleJdbcTask, Indexed, Indexed> {

	/*
	 * ===================== Task =============================================
	 */

	@Override
	public Object execute(ManagedFunctionContext<RecycleJdbcTask, Indexed, Indexed> context)
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