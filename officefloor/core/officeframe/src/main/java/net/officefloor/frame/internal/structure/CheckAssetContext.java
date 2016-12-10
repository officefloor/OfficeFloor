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
package net.officefloor.frame.internal.structure;

/**
 * Context to check on the {@link Asset}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CheckAssetContext {

	/**
	 * <p>
	 * Obtains the time that check is being made.
	 * <p>
	 * As many {@link Asset} instances may be checked at the same time (or
	 * nanoseconds from each other) this provides optimisation to obtain the
	 * current time in milliseconds (equivalent to
	 * {@link System#currentTimeMillis()}).
	 * 
	 * @return Time of check.
	 */
	long getTime();

	/**
	 * Activates all {@link JobNode} instances waiting on the {@link Asset}.
	 * 
	 * 
	 * @param isPermanent
	 *            <code>true</code> indicates that all {@link JobNode} instances
	 *            added to the {@link AssetMonitor} from now on are activated
	 *            immediately. It is useful to flag an {@link AssetMonitor} in
	 *            this state when the {@link Asset} is no longer being used to
	 *            stop a {@link JobNode} from waiting forever.
	 */
	void activateJobNodes(boolean isPermanent);

	/**
	 * Activates all {@link JobNode} instances waiting on the {@link Asset}
	 * providing the {@link ThreadState} of the {@link JobNode} with input
	 * failure.
	 * 
	 * @param failure
	 *            Failure to propagate to the {@link ThreadState} of the
	 *            {@link JobNode} instances waiting on the {@link AssetMonitor}.
	 * @param isPermanent
	 *            <code>true</code> indicates that all {@link JobNode} instances
	 *            added to the {@link AssetMonitor} from now on are activated
	 *            immediately with the input failure. It is useful to flag an
	 *            {@link AssetMonitor} in this state when the {@link Asset} is
	 *            in a failed state that can not be recovered from.
	 */
	void failJobNodes(Throwable failure, boolean isPermanent);

}