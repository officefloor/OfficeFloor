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


/**
 * Monitor for 'synchronizing' the {@link ThreadState} instances via
 * {@link JobNode} on an {@link Asset}.
 * 
 * @author Daniel
 */
public interface AssetMonitor extends LinkedListEntry<AssetMonitor, Object> {

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
	 * Flags for the {@link ThreadState} of the input {@link JobNode} to wait
	 * and on being notified wake up the input {@link JobNode}.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to be notified on waking up the
	 *            {@link ThreadState}.
	 * @param notifySet
	 *            {@link JobActivateSet} that the {@link JobNode} is added to if
	 *            this {@link AssetMonitor} is permanently notified.
	 * @return <code>true</code> if the {@link JobNode} is waiting on this
	 *         {@link AssetMonitor}.
	 */
	boolean wait(JobNode jobNode, JobActivateSet notifySet);

	/**
	 * Adds all the {@link JobNode} instances waiting on this {@link AssetMonitor}
	 * to the input {@link JobActivateSet}.
	 * 
	 * @param notifySet
	 *            {@link JobActivateSet}.
	 */
	void notifyTasks(JobActivateSet notifySet);

	/**
	 * <p>
	 * Flags this {@link AssetMonitor} to permanently notify waiting {@link JobNode}
	 * instances.
	 * <p>
	 * Also adds all the {@link JobNode} instances waiting on this
	 * {@link AssetMonitor} to the input {@link JobActivateSet}.
	 * 
	 * @param notifySet
	 *            {@link JobActivateSet}.
	 */
	void notifyPermanently(JobActivateSet notifySet);

	/**
	 * <p>
	 * Flags each {@link JobNode} with the input failure.
	 * <p>
	 * Adds all the {@link JobNode} instances waiting on this {@link AssetMonitor}
	 * to the {@link JobActivateSet} with the input {@link Throwable}.
	 * 
	 * @param notifySet
	 *            {@link JobActivateSet}.
	 * @param failure
	 *            Failure to propagate to the {@link ThreadState} instances.
	 */
	void failTasks(JobActivateSet notifySet, Throwable failure);

	/**
	 * <p>
	 * Flags each {@link JobNode} with the input failure and all subsequent
	 * {@link JobNode} added via {@link #wait(JobNode, JobActivateSet)}.
	 * 
	 * @param notifySet
	 *            {@link JobActivateSet}.
	 * @param failure
	 *            Failure to propagate to the {@link ThreadState} instance.
	 */
	void failPermanently(JobActivateSet notifySet, Throwable failure);

}
