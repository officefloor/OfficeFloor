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

package net.officefloor.frame.api.executive;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Optional interface for {@link Executive} to implement to indicate it supports
 * background scheduling of {@link Runnable} instances.
 * <p>
 * Some {@link Executive} implementations can only have {@link ProcessState}
 * scoped {@link Thread} instances that disallow long running background
 * {@link Thread}. However, long running background {@link Thread} allows more
 * efficiency in running scheduled {@link Runnable} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface BackgroundScheduler {

	/**
	 * Schedules the {@link Runnable} to be executed after the delay.
	 * 
	 * @param delay    Delay in milliseconds.
	 * @param runnable {@link Runnable}.
	 */
	void schedule(long delay, Runnable runnable);

}
