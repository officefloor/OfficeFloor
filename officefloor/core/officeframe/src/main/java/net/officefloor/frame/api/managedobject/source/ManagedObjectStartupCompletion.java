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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * <p>
 * Startup completion for a {@link ManagedObjectSource}.
 * <p>
 * This allows a {@link ManagedObjectSource} to block the {@link OfficeFloor}
 * from servicing until this is complete.
 * <p>
 * Ideally, {@link OfficeFloor} is aimed to startup as quick as possible to
 * allow for patterns such as scale to zero. However for example, having to
 * migrate the data store structure on start up of new version of an application
 * requires not servicing until the data store is migrated. This, therefore,
 * allows blocking servicing until these start up functionalities complete.
 * <p>
 * Methods on this interface are {@link Thread} safe, so may be called from
 * {@link ManagedFunction} instances.
 * <p>
 * For {@link ManagedObjectSource} implementors, please use this sparingly as it
 * does impact start up times.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectStartupCompletion {

	/**
	 * Flags the startup complete and {@link OfficeFloor} may start servicing.
	 */
	void complete();

	/**
	 * Flags to fail opening the {@link OfficeFloor}.
	 * 
	 * @param cause Cause of failing to open {@link OfficeFloor}.
	 */
	void failOpen(Throwable cause);

}
