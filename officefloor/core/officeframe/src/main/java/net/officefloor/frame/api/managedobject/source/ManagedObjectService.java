/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
