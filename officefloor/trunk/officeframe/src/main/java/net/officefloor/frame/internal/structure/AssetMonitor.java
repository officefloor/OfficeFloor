/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Monitor for 'synchronizing' the
 * {@link net.officefloor.frame.internal.structure.ThreadState} instances via
 * {@link net.officefloor.frame.spi.team.TaskContainer} on an
 * {@link net.officefloor.frame.internal.structure.Asset}.
 * 
 * @author Daniel
 */
public interface AssetMonitor extends LinkedListEntry<AssetMonitor> {

	/**
	 * Obtains the {@link Asset} for this {@link AssetMonitor}.
	 * 
	 * @return {@link Asset} for this {@link AssetMonitor}.
	 */
	Asset getAsset();

	/**
	 * Obtains the lock for synchronising the {@link Asset}.
	 * 
	 * @return Lock for synchronising the {@link Asset}.
	 */
	Object getAssetLock();

	/**
	 * Flags for the {@link ThreadState} of the input {@link TaskContainer} to
	 * wait and on being notified wake up the input {@link TaskContainer}.
	 * 
	 * @param taskContainer
	 *            {@link TaskContainer} to be notified on waking up the
	 *            {@link ThreadState}.
	 */
	boolean wait(TaskContainer taskContainer);

	/**
	 * Wakes up all the {@link ThreadState} instances waiting on this
	 * {@link AssetMonitor}.
	 */
	void notifyTasks();

	/**
	 * <p>
	 * Flags each {@link ThreadState} with the input failure.
	 * <p>
	 * This also wakes up all the {@link ThreadState} instances waiting on this
	 * {@link AssetMonitor}.
	 * 
	 * @param failure
	 *            Failure to propagate to the {@link ThreadState} instances.
	 */
	void failTasks(Throwable failure);

}
