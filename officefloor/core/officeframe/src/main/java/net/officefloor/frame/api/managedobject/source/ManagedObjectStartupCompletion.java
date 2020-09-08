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
