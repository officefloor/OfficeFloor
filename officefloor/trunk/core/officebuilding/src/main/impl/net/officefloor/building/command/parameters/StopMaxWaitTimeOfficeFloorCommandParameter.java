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
package net.officefloor.building.command.parameters;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for the stop max wait time.
 * 
 * @author Daniel Sagenschneider
 */
public class StopMaxWaitTimeOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {

	/**
	 * Default stop wait time.
	 */
	public static final long DEFAULT_STOP_WAIT_TIME = 10000;

	/**
	 * Initiate.
	 */
	public StopMaxWaitTimeOfficeFloorCommandParameter() {
		super("stop_max_wait_time", null,
				"Maximum time in milliseconds to wait to stop. Default is "
						+ DEFAULT_STOP_WAIT_TIME);
	}

	/**
	 * Obtains the stop max wait time.
	 * 
	 * @return Stop max wait time.
	 */
	public long getStopMaxWaitTime() {

		// Obtain the stop max wait time
		long stopMaxWaitTime = DEFAULT_STOP_WAIT_TIME;
		String value = this.getValue();
		if (value != null) {
			stopMaxWaitTime = Long.parseLong(value);
		}

		// Return the stop max wait time
		return stopMaxWaitTime;
	}

}