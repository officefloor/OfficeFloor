/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Service context for the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectServiceContext<F extends Enum<F>> {

	/**
	 * Instigates a {@link ProcessState}.
	 * 
	 * @param key           Key identifying the {@link Flow} to instigate.
	 * @param parameter     Parameter to first {@link ManagedFunction} of the
	 *                      {@link Flow}.
	 * @param managedObject {@link ManagedObject} for the {@link ProcessState} of
	 *                      the {@link Flow}.
	 * @param delay         Delay in milliseconds before the {@link Flow} is
	 *                      invoked. A <code>0</code> or negative value invokes the
	 *                      {@link Flow} immediately.
	 * @param callback      {@link FlowCallback} on completion of the {@link Flow}.
	 * @return {@link ProcessManager} for the {@link ProcessState}.
	 * @throws IllegalArgumentException If
	 *                                  <ul>
	 *                                  <li>unknown {@link Flow} key</li>
	 *                                  <li>parameter is incorrect type</li>
	 *                                  <li>no {@link ManagedObject} is
	 *                                  supplied</li>
	 *                                  </ul>
	 */
	ProcessManager invokeProcess(F key, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException;

	/**
	 * Instigates a {@link ProcessState}.
	 * 
	 * @param flowIndex     Index identifying the {@link Flow} to instigate.
	 * @param parameter     Parameter that to the first {@link ManagedFunction} of
	 *                      the {@link Flow}.
	 * @param managedObject {@link ManagedObject} for the {@link ProcessState} of
	 *                      the {@link Flow}.
	 * @param delay         Delay in milliseconds before the {@link Flow} is
	 *                      invoked. A <code>0</code> or negative value invokes the
	 *                      {@link Flow} immediately.
	 * @param callback      {@link FlowCallback} on completion of the {@link Flow}.
	 * @return {@link ProcessManager} for the {@link ProcessState}.
	 * @throws IllegalArgumentException If
	 *                                  <ul>
	 *                                  <li>unknown {@link Flow} index</li>
	 *                                  <li>parameter is incorrect type</li>
	 *                                  <li>no {@link ManagedObject} is
	 *                                  supplied</li>
	 *                                  </ul>
	 */
	ProcessManager invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException;

}
