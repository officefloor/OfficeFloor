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

package net.officefloor.frame.api.profile;

import java.util.List;

import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Profiled {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProfiledThreadState {

	/**
	 * Obtains the start time stamp.
	 * 
	 * @return Start time stamp in milliseconds.
	 */
	long getStartTimestampMilliseconds();

	/**
	 * Obtains the start time stamp.
	 * 
	 * @return Start time stamp in nanoseconds.
	 */
	long getStartTimestampNanoseconds();

	/**
	 * Obtains the {@link ProfiledManagedFunction} instances.
	 * 
	 * @return {@link ProfiledManagedFunction} instances.
	 */
	List<ProfiledManagedFunction> getProfiledManagedFunctions();

}
