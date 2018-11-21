/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.api.thread;

import net.officefloor.frame.api.team.Team;

/**
 * Synchronises the {@link ThreadLocal} instances on {@link Thread} to
 * {@link Thread} interaction between {@link Team} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadSynchroniser {

	/**
	 * Suspends the current {@link Thread} by:
	 * <ol>
	 * <li>storing {@link ThreadLocal} state, and then</li>
	 * <li>clearing state off the {@link Thread}</li>
	 * </ol>
	 */
	void suspendThread();

	/**
	 * Resumes the {@link Thread} by loading the suspended state into the
	 * {@link ThreadLocal} instances of the current {@link Thread}.
	 */
	void resumeThread();

}