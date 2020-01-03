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

package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.MonitorClock;

/**
 * {@link MonitorClock} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class MonitorClockImpl implements MonitorClock {

	/**
	 * Keeps approximate time for monitoring the {@link Office}.
	 */
	private volatile long currentTime = System.currentTimeMillis();

	/**
	 * Updates the current time.
	 */
	public void updateTime() {
		this.currentTime = System.currentTimeMillis();
	}

	/*
	 * ======================== OfficeClock =====================
	 */

	@Override
	public long currentTimeMillis() {
		return this.currentTime;
	}

}
