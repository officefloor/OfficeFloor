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
package net.officefloor.frame.api.profile;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Profiled execution of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProfiledManagedFunction {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the time stamp in milliseconds when the {@link ManagedFunction}
	 * was started.
	 * 
	 * @return Time stamp in milliseconds when the {@link ManagedFunction} was
	 *         started.
	 */
	long getStartTimestampMilliseconds();

	/**
	 * Obtains the time stamp in nanoseconds when the {@link ManagedFunction}
	 * was started.
	 * 
	 * @return Time stamp in nanoseconds when the {@link ManagedFunction} was
	 *         started.
	 */
	long getStartTimestampNanoseconds();

	/**
	 * Obtains the name of the executing {@link Thread}.
	 * 
	 * @return Name of the executing {@link Thread}.
	 */
	String getExecutingThreadName();

}