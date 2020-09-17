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

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Allows the {@link ManagedObjectSource} to service by invoking
 * {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectService<F extends Enum<F>> {

	/**
	 * <p>
	 * Starts the servicing.
	 * <p>
	 * Servicing should only use the invoking {@link Thread} of this method for
	 * service start up. After set up for servicing, should use the
	 * {@link ThreadFactory} instances provided by the
	 * {@link ManagedObjectExecuteContext}.
	 * <p>
	 * Note that blocking in this method will slow {@link OfficeFloor} start up
	 * times.
	 * 
	 * @param serviceContext {@link ManagedObjectServiceContext}.
	 * @throws Exception If fails to start servicing.
	 */
	void startServicing(ManagedObjectServiceContext<F> serviceContext) throws Exception;

	/**
	 * <p>
	 * Stops servicing.
	 * <p>
	 * This will be invoked in two circumstances:
	 * <ul>
	 * <li>{@link OfficeFloor} failed to start up, so clean up any servicing (note
	 * that start may have not been called)</li>
	 * <li>{@link OfficeFloor} is being closed</li>
	 * </ul>
	 */
	void stopServicing();

}
